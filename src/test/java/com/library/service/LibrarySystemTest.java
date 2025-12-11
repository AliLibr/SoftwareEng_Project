package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.library.domain.*;
import com.library.observer.Observer;
import com.library.repository.*;

@ExtendWith(MockitoExtension.class)
class LibrarySystemTest {

    @Mock private LoanRepository loanRepo;
    @Mock private UserRepository userRepo;
    @Mock private TimeProvider timeProvider;
    @Mock private Observer mockObserver;

    private LoanService loanService;
    private UserService userService;
    private ReminderService reminderService;
    private FineService fineService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Initialize all services before each test
        loanService = new LoanService(loanRepo, timeProvider);
        userService = new UserService(userRepo, loanRepo);
        reminderService = new ReminderService(loanRepo, timeProvider);
        fineService = new FineService();
        authService = new AuthService();

        // AUTOMATIC SETUP: Configure valid credentials for every test by default
        System.setProperty("LIBRARY_ADMIN_USER", "admin");
        System.setProperty("LIBRARY_ADMIN_PASS", "secret");
    }

    @AfterEach
    void tearDown() {
        // Clear system properties after tests to ensure clean state
        System.clearProperty("LIBRARY_ADMIN_USER");
        System.clearProperty("LIBRARY_ADMIN_PASS");
    }

    // ==========================================
    // 1. AUTHENTICATION TESTS
    // ==========================================
    @Test
    void testLoginSuccessWithSystemProps() {
        // Credentials are now automatically set by setUp(), so we just test the logic
        assertTrue(authService.login("admin", "secret"), "Login should succeed with correct props");
        assertTrue(authService.isAdminLoggedIn());
    }

    @Test
    void testLoginFailureWithWrongCreds() {
        // Credentials are automatically set by setUp()
        assertFalse(authService.login("admin", "wrong"), "Login should fail with wrong password");
        assertFalse(authService.isAdminLoggedIn());
    }
    
    @Test
    void testLoginFailsIfNoConfigSet() {
        // We must explicitly clear the automatic setup to test this specific failure scenario
        System.clearProperty("LIBRARY_ADMIN_USER");
        System.clearProperty("LIBRARY_ADMIN_PASS");

        // No properties set
        assertFalse(authService.login("admin", "secret"));
    }
    
    @Test
    void testLogout() {
        authService.logout();
        assertFalse(authService.isAdminLoggedIn());
    }

    // ==========================================
    // 2. FINE SERVICE TESTS
    // ==========================================
    @Test
    void testPayFineFully() {
        User user = new User("u1", "Debtor");
        user.setFinesOwed(50.0);

        boolean success = fineService.payFine(user, 50.0);
        
        assertTrue(success);
        assertEquals(0.0, user.getFinesOwed(), 0.01);
    }

    @Test
    void testPayFinePartially() {
        User user = new User("u1", "Debtor");
        user.setFinesOwed(100.0);

        boolean success = fineService.payFine(user, 40.0);
        
        assertTrue(success);
        assertEquals(60.0, user.getFinesOwed(), 0.01);
    }

    @Test
    void testPayFineInvalidAmount() {
        User user = new User("u1", "Debtor");
        user.setFinesOwed(50.0);

        assertFalse(fineService.payFine(user, -10.0), "Should fail for negative amount");
        assertEquals(50.0, user.getFinesOwed()); // Balance unchanged
    }

    // ==========================================
    // 3. LOAN & STRATEGY TESTS
    // ==========================================
    @Test
    void testBorrowBookCalculatesDueDate() {
        User user = new User("u1", "Alice");
        Book book = new Book("isbn1", "Java Book", "Author");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        String result = loanService.borrowItem(user, book);

        assertTrue(result.contains("Success"));
        assertTrue(book.isBorrowed());
        
        // Verify 28 day loan period
        verify(loanRepo).save(argThat(loan -> 
            loan.getDueDate().isEqual(LocalDate.of(2023, 1, 29)) 
        ));
    }

    @Test
    void testBorrowCDCalculatesDueDate() {
        User user = new User("u1", "Alice");
        CD cd = new CD("cd1", "Music", "Artist");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        loanService.borrowItem(user, cd);

        // Verify 7 day loan period
        verify(loanRepo).save(argThat(loan -> 
            loan.getDueDate().isEqual(LocalDate.of(2023, 1, 8)) 
        ));
    }

    @Test
    void testBorrowBlockedIfItemBorrowed() {
        User user = new User("u1", "Alice");
        Book book = new Book("isbn1", "Java", "Auth");
        book.setBorrowed(true); // Already borrowed

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("already borrowed"));
        verify(loanRepo, never()).save(any());
    }

    @Test
    void testBorrowBlockedIfFinesExist() {
        User user = new User("u1", "Debtor");
        user.setFinesOwed(10.0);
        Book book = new Book("1", "B", "A");

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("unpaid fines"));
    }

    @Test
    void testBorrowBlockedIfOverdueExists() {
        User user = new User("u1", "Alice");
        Book book = new Book("1", "B", "A");
        
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);

        // User has an existing active loan that is overdue
        Loan oldLoan = mock(Loan.class);
        when(oldLoan.isOverdue(today)).thenReturn(true);
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.singletonList(oldLoan));

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("overdue items"));
    }

    @Test
    void testCheckOverdueItemsFormatting() {
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);

        User user = new User("u1", "Alice");
        Book book = new Book("1", "B", "A");
        Loan loan = new Loan(book, user, LocalDate.of(2023, 1, 1)); // Due Jan 29

        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(loan));

        List<String> results = loanService.checkOverdueItems();
        
        assertEquals(1, results.size());
        String msg = results.get(0);
        assertTrue(msg.contains("Overdue:"));
        assertTrue(msg.contains("Days late: 3")); // Jan 29 to Feb 1
        assertTrue(msg.contains("Est. Fine: 30.00")); // 3 days * 10 NIS
    }

    // ==========================================
    // 4. OBSERVER TESTS
    // ==========================================
    @Test
    void testReminderServiceNotifiesObservers() {
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);
        
        User user = new User("u1", "Alice");
        Book book = new Book("1", "B", "A");
        Loan loan = new Loan(book, user, LocalDate.of(2023, 1, 1)); // Overdue

        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(loan));
        
        reminderService.registerObserver(mockObserver);
        reminderService.sendOverdueReminders();

        verify(mockObserver).update(eq(user), contains("overdue"));
    }

    // ==========================================
    // 5. USER SERVICE TESTS
    // ==========================================
    @Test
    void testUnregisterSuccess() {
        User user = new User("u1", "Clean");
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.emptyList());

        String result = userService.unregisterUser("u1");
        assertTrue(result.contains("Success"));
        verify(userRepo).delete(user);
    }

    @Test
    void testUnregisterFailFines() {
        User user = new User("u1", "Debtor");
        user.setFinesOwed(5.0);
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));

        String result = userService.unregisterUser("u1");
        assertTrue(result.contains("fines"));
        verify(userRepo, never()).delete(any());
    }
}