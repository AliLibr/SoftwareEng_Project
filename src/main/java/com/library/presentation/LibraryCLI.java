package com.library.presentation;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.library.domain.*;
import com.library.repository.*;
import com.library.service.*;

public class LibraryCLI {

    private static final Logger LOGGER = Logger.getLogger(LibraryCLI.class.getName());

    private static final ItemRepository itemRepo = new InMemoryItemRepository();
    private static final UserRepository userRepo = new InMemoryUserRepository();
    private static final LoanRepository loanRepo = new InMemoryLoanRepository();

    private static final AuthService authService = new AuthService();
    private static final FineService fineService = new FineService();
    private static final TimeProvider timeProvider = new SystemTimeProvider();
    
    private static final LoanService loanService = new LoanService(loanRepo, timeProvider);
    private static final UserService userService = new UserService(userRepo, loanRepo);
    private static final ReminderService reminderService = new ReminderService(loanRepo, timeProvider);

    private static final EmailNotifier emailNotifier = new EmailNotifier();

    static {
        reminderService.registerObserver(emailNotifier);
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        LOGGER.info("=== Library Management System ===");
        runMainMenu();
        scanner.close();
    }

    private static void runMainMenu() {
        boolean running = true;
        while (running) {
            if (authService.isAdminLoggedIn()) {
                handleAdminMenu();
            } else if (currentUser != null) {
                handleUserMenu();
            } else {
                running = handleGuestMenu();
            }
        }
    }

    private static boolean handleGuestMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Admin Login");
        System.out.println("2. User Login");
        System.out.println("3. Sign Up (New User)");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.print("Username: "); String u = scanner.nextLine();
                System.out.print("Password: "); String p = scanner.nextLine();
                if (authService.login(u, p)) {
                    LOGGER.info("Admin logged in successfully.");
                } else {
                    LOGGER.warning("Invalid credentials.");
                }
                break;
            case "2":
                handleUserLogin();
                break;
            case "3":
                handleUserSignUp();
                break;
            case "0":
                return false;
            default:
                LOGGER.warning("Invalid choice.");
        }
        return true;
    }

    private static void handleUserSignUp() {
        System.out.println("\n--- User Sign Up ---");
        System.out.print("Enter Desired User ID: ");
        String id = scanner.nextLine();
        
        if (userRepo.findById(id).isPresent()) {
            LOGGER.warning("Error: User ID already exists.");
            return;
        }
        
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        User newUser = new User(id, name, password);
        userRepo.save(newUser);
        LOGGER.info("Sign up successful! Please log in.");
    }

    private static void handleUserLogin() {
        System.out.print("Enter User ID: ");
        String uid = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine();
        
        userRepo.findById(uid).ifPresentOrElse(
            user -> {
                if (user.getPassword().equals(pass)) {
                    currentUser = user;
                    LOGGER.info(() -> "Welcome back, " + user.getName());
                } else {
                    LOGGER.warning("Error: Incorrect password.");
                }
            },
            () -> LOGGER.warning("Error: User not found.")
        );
    }

    private static void handleAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. Add Book");
        System.out.println("2. Add CD");
        System.out.println("3. Check Overdues");
        System.out.println("4. Send Reminders");
        System.out.println("5. Unregister User");
        System.out.println("6. Logout");
        System.out.print("Choice: ");

        switch (scanner.nextLine()) {
            case "1":
                System.out.print("ISBN: "); String isbn = scanner.nextLine();
                System.out.print("Title: "); String title = scanner.nextLine();
                System.out.print("Author: "); String author = scanner.nextLine();
                itemRepo.save(new Book(isbn, title, author));
                LOGGER.info("Book added.");
                break;
            case "2":
                System.out.print("Serial: "); String serial = scanner.nextLine();
                System.out.print("Title: "); String t = scanner.nextLine();
                System.out.print("Artist: "); String artist = scanner.nextLine();
                itemRepo.save(new CD(serial, t, artist));
                LOGGER.info("CD added.");
                break;
            case "3":
                List<String> overdues = loanService.checkOverdueItems();
                if (overdues.isEmpty()) LOGGER.info("No items overdue.");
                else overdues.forEach(LOGGER::info);
                break;
            case "4":
                LOGGER.info("Sending reminders...");
                reminderService.sendOverdueReminders();
                break;
            case "5":
                System.out.print("User ID to remove: ");
                System.out.println(userService.unregisterUser(scanner.nextLine()));
                break;
            case "6":
                authService.logout();
                break;
            default:
                LOGGER.warning("Invalid choice.");
        }
    }

    private static void handleUserMenu() {
        System.out.println("\n--- User Menu (" + currentUser.getName() + ") ---");
        System.out.println("1. Search Item (US1.4)");
        System.out.println("2. Borrow Item");
        System.out.println("3. Pay Fine");
        System.out.println("4. Logout");
        System.out.print("Choice: ");

        switch (scanner.nextLine()) {
            case "1":
                System.out.print("Enter search term (Title/Author/ID): ");
                String query = scanner.nextLine();
                List<LibraryItem> results = itemRepo.searchByTitle(query); 
                
                if (results.isEmpty()) {
                    LOGGER.info(() -> "No items found matching '" + query + "'.");
                } else {
                    LOGGER.info(() -> "Found " + results.size() + " item(s):");
                    results.forEach(item -> LOGGER.info(item::toString));
                }
                break;
            case "2":
                System.out.print("Enter Item ID to borrow: ");
                itemRepo.findById(scanner.nextLine()).ifPresentOrElse(
                    item -> System.out.println(loanService.borrowItem(currentUser, item)),
                    () -> LOGGER.warning("Item not found.")
                );
                break;
            case "3":
                System.out.println("Current Fines: " + currentUser.getFinesOwed());
                if (currentUser.getFinesOwed() > 0) {
                    System.out.print("Amount to pay: ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        if (fineService.payFine(currentUser, amount)) {
                            LOGGER.info(() -> "Payment accepted. New Balance: " + currentUser.getFinesOwed());
                        } else {
                            LOGGER.warning("Payment failed (Invalid amount).");
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid number.");
                    }
                }
                break;
            case "4":
                currentUser = null;
                break;
            default:
                LOGGER.warning("Invalid choice.");
        }
    }
}