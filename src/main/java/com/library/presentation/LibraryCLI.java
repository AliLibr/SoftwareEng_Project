package com.library.presentation;

import java.util.List;
import java.util.Scanner;
import com.library.domain.*;
import com.library.repository.*;
import com.library.service.*;

/**
 * Main command-line interface.
 */
public class LibraryCLI {

    // 1. Initialize Repositories
    private static final ItemRepository itemRepo = new InMemoryItemRepository();
    private static final UserRepository userRepo = new InMemoryUserRepository();
    private static final LoanRepository loanRepo = new InMemoryLoanRepository();

    // 2. Initialize Services
    private static final AuthService authService = new AuthService();
    private static final FineService fineService = new FineService();
    private static final TimeProvider timeProvider = new SystemTimeProvider();
    
    // 3. Initialize Orchestration Services with Dependencies
    private static final LoanService loanService = new LoanService(loanRepo, timeProvider);
    private static final UserService userService = new UserService(userRepo, loanRepo);
    private static final ReminderService reminderService = new ReminderService(loanRepo, timeProvider);

    // 4. Initialize Observers
    private static final EmailNotifier emailNotifier = new EmailNotifier();

    static {
        // Register the observer
        reminderService.registerObserver(emailNotifier);
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        seedData();
        System.out.println("=== Library Management System ===");
        runMainMenu();
        scanner.close();
    }

    private static void seedData() {
        // Sample Data for testing
        itemRepo.save(new Book("b1", "Clean Code", "Robert C. Martin"));
        itemRepo.save(new CD("cd1", "Dark Side of the Moon", "Pink Floyd"));
        userRepo.save(new User("u1", "Alice Student"));
        userRepo.save(new User("u2", "Bob Builder"));
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
                System.out.print("Enter User ID: ");
                String uid = scanner.nextLine();
                userRepo.findById(uid).ifPresentOrElse(
                    user -> {
                        currentUser = user;
                        System.out.println("Welcome, " + user.getName());
                    },
                    () -> System.out.println("User not found.")
                );
                break;
            case "0":
                return false;
            default:
                System.out.println("Invalid choice.");
        }
        return true;
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
                // Changed 'var' to 'List<String>' for Java 8 compatibility
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
        System.out.println("1. Search Item");
        System.out.println("2. Borrow Item");
        System.out.println("3. Pay Fine");
        System.out.println("4. Logout");
        System.out.print("Choice: ");

        switch (scanner.nextLine()) {
            case "1":
                System.out.print("Search Title: ");
                // Changed 'var' to 'List<LibraryItem>' for Java 8 compatibility
                List<LibraryItem> results = itemRepo.searchByTitle(scanner.nextLine());
                if (results.isEmpty()) System.out.println("No items found.");
                else results.forEach(System.out::println);
                break;
            case "2":
                System.out.print("Enter Item ID: ");
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
                            System.out.println("Payment accepted.");
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