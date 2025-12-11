package com.library.presentation;

import java.util.List;
import java.util.Scanner;
import com.library.domain.*;
import com.library.repository.*;
import com.library.service.*;

public class LibraryCLI {

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
        System.out.println("=== Library Management System ===");
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
        System.out.println("3. Sign Up (New User)"); // New Feature
        System.out.println("0. Exit");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.print("Username: "); String u = scanner.nextLine();
                System.out.print("Password: "); String p = scanner.nextLine();
                if (authService.login(u, p)) {
                    System.out.println("Admin logged in successfully.");
                } else {
                    System.out.println("Invalid credentials.");
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
                System.out.println("Invalid choice.");
        }
        return true;
    }

    private static void handleUserSignUp() {
        System.out.println("\n--- User Sign Up ---");
        System.out.print("Enter Desired User ID: ");
        String id = scanner.nextLine();
        
        if (userRepo.findById(id).isPresent()) {
            System.out.println("Error: User ID already exists.");
            return;
        }
        
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        User newUser = new User(id, name, password);
        userRepo.save(newUser);
        System.out.println("Sign up successful! Please log in.");
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
                    System.out.println("Welcome back, " + user.getName());
                } else {
                    System.out.println("Error: Incorrect password.");
                }
            },
            () -> System.out.println("Error: User not found.")
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
                System.out.println("Book added.");
                break;
            case "2":
                System.out.print("Serial: "); String serial = scanner.nextLine();
                System.out.print("Title: "); String t = scanner.nextLine();
                System.out.print("Artist: "); String artist = scanner.nextLine();
                itemRepo.save(new CD(serial, t, artist));
                System.out.println("CD added.");
                break;
            case "3":
                List<String> overdues = loanService.checkOverdueItems();
                if (overdues.isEmpty()) System.out.println("No items overdue.");
                else overdues.forEach(System.out::println);
                break;
            case "4":
                System.out.println("Sending reminders...");
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
                System.out.println("Invalid choice.");
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
                    System.out.println("No items found matching '" + query + "'.");
                } else {
                    System.out.println("Found " + results.size() + " item(s):");
                    results.forEach(System.out::println);
                }
                break;
            case "2":
                System.out.print("Enter Item ID to borrow: ");
                itemRepo.findById(scanner.nextLine()).ifPresentOrElse(
                    item -> System.out.println(loanService.borrowItem(currentUser, item)),
                    () -> System.out.println("Item not found.")
                );
                break;
            case "3":
                System.out.println("Current Fines: " + currentUser.getFinesOwed());
                if (currentUser.getFinesOwed() > 0) {
                    System.out.print("Amount to pay: ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        if (fineService.payFine(currentUser, amount)) {
                            System.out.println("Payment accepted. New Balance: " + currentUser.getFinesOwed());
                        } else {
                            System.out.println("Payment failed (Invalid amount).");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                break;
            case "4":
                currentUser = null;
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}