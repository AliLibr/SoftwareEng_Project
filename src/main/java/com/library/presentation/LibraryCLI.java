package com.library.presentation;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.library.domain.*;
import com.library.repository.*;
import com.library.service.*;

public class LibraryCLI {

    private static final Logger LOGGER = Logger.getLogger(LibraryCLI.class.getName());
    private static final String CHOICE_PROMPT = "Choice: ";
    private static final String INVALID_CHOICE_MSG = "Invalid choice.";

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

    private static void printPrompt(String message) {
        LOGGER.info(message);
    }

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
        LOGGER.info("\n--- Main Menu ---");
        LOGGER.info("1. Admin Login");
        LOGGER.info("2. User Login");
        LOGGER.info("3. Sign Up (New User)");
        LOGGER.info("0. Exit");
        printPrompt(CHOICE_PROMPT);
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                printPrompt("Username: "); String u = scanner.nextLine();
                printPrompt("Password: "); String p = scanner.nextLine();
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
                LOGGER.warning(INVALID_CHOICE_MSG);
        }
        return true;
    }

    private static void handleUserSignUp() {
        LOGGER.info("\n--- User Sign Up ---");
        printPrompt("Enter Desired User ID: ");
        String id = scanner.nextLine();
        
        if (userRepo.findById(id).isPresent()) {
            LOGGER.warning("Error: User ID already exists.");
            return;
        }
        
        printPrompt("Enter Name: ");
        String name = scanner.nextLine();
        printPrompt("Enter Password: ");
        String password = scanner.nextLine();
        
        User newUser = new User(id, name, password);
        userRepo.save(newUser);
        LOGGER.info("Sign up successful! Please log in.");
    }

    private static void handleUserLogin() {
        printPrompt("Enter User ID: ");
        String uid = scanner.nextLine();
        printPrompt("Enter Password: ");
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
        LOGGER.info("\n--- Admin Menu ---");
        LOGGER.info("1. Add Book");
        LOGGER.info("2. Add CD");
        LOGGER.info("3. Check Overdues");
        LOGGER.info("4. Send Reminders");
        LOGGER.info("5. Unregister User");
        LOGGER.info("6. Logout");
        printPrompt(CHOICE_PROMPT);

        switch (scanner.nextLine()) {
            case "1":
                printPrompt("ISBN: "); String isbn = scanner.nextLine();
                printPrompt("Title: "); String title = scanner.nextLine();
                printPrompt("Author: "); String author = scanner.nextLine();
                itemRepo.save(new Book(isbn, title, author));
                LOGGER.info("Book added.");
                break;
            case "2":
                printPrompt("Serial: "); String serial = scanner.nextLine();
                printPrompt("Title: "); String t = scanner.nextLine();
                printPrompt("Artist: "); String artist = scanner.nextLine();
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
                printPrompt("User ID to remove: ");
                LOGGER.info(userService.unregisterUser(scanner.nextLine())); 
                break;
            case "6":
                authService.logout();
                break;
            default:
                LOGGER.warning(INVALID_CHOICE_MSG);
        }
    }

    private static void handleUserMenu() {
        LOGGER.info(() -> "\n--- User Menu (" + currentUser.getName() + ") ---");
        LOGGER.info("1. Search Item (US1.4)");
        LOGGER.info("2. Borrow Item");
        LOGGER.info("3. Pay Fine");
        LOGGER.info("4. Logout");
        printPrompt(CHOICE_PROMPT);

        switch (scanner.nextLine()) {
            case "1":
                printPrompt("Enter search term (Title/Author/ID): ");
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
                printPrompt("Enter Item ID to borrow: ");
                itemRepo.findById(scanner.nextLine()).ifPresentOrElse(
                    item -> LOGGER.info(loanService.borrowItem(currentUser, item)),
                    () -> LOGGER.warning("Item not found.")
                );
                break;
            case "3":
                LOGGER.info(() -> "Current Fines: " + currentUser.getFinesOwed());
                if (currentUser.getFinesOwed() > 0) {
                    printPrompt("Amount to pay: ");
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
                LOGGER.warning(INVALID_CHOICE_MSG);
        }
    }
}