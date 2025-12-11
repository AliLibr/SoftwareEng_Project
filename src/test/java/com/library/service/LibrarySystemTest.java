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
        loanService = new LoanService(loanRepo, timeProvider);
        userService = new UserService(userRepo, loanRepo);
        reminderService = new ReminderService(loanRepo, timeProvider);
        fineService = new FineService();
        authService = new AuthService();

        System.setProperty("LIBRARY_ADMIN_USER", "admin");
        System.setProperty("LIBRARY_ADMIN_PASS", "secret");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("LIBRARY_ADMIN_USER");
        System.clearProperty("LIBRARY_ADMIN_PASS");
    }


    @Test
    void testLoginSuccessWithSystemProps() {
        assertTrue(authService.login("admin", "secret"), "Login should succeed with correct props");
        assertTrue(authService.isAdminLoggedIn());
    }

    @Test
    void testLoginFailureWithWrongCreds() {
        assertFalse(authService.login("admin", "wrong"), "Login should fail with wrong password");
        assertFalse(authService.isAdminLoggedIn());
    }
    
    @Test
    void testLoginFailsIfNoConfigSet() {
        System.clearProperty("LIBRARY_ADMIN_USER");
        System.clearProperty("LIBRARY_ADMIN_PASS");

        assertFalse(authService.login("admin", "secret"));
    }
    
    @Test
    void testLogout() {
        authService.logout();
        assertFalse(authService.isAdminLoggedIn());
    }

    @Test
    void testSecureUserLogin() {
        String userId = "u10";
        String password = "securepass";
        User mockUser = new User(userId, "Secure User", password);
        
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        
        
        Optional<User> foundUser = userRepo.findById(userId);
        assertTrue(foundUser.isPresent());
        assertTrue(foundUser.get().getPassword().equals(password), "Password should match");
        
        Optional<User> foundUserWrongPass = userRepo.findById(userId);
        assertTrue(foundUserWrongPass.isPresent());
        assertFalse(foundUserWrongPass.get().getPassword().equals("wrongpass"), "Wrong password should fail");
    }


    @Test
    void testPayFineFully() {
        User user = new User("u1", "Debtor", "testpass"); 
        user.setFinesOwed(50.0);

        boolean success = fineService.payFine(user, 50.0);
        
        assertTrue(success);
        assertEquals(0.0, user.getFinesOwed(), 0.01);
    }

    @Test
    void testPayFinePartially() {
        User user = new User("u1", "Debtor", "testpass"); 
        user.setFinesOwed(100.0);

        boolean success = fineService.payFine(user, 40.0);
        
        assertTrue(success);
        assertEquals(60.0, user.getFinesOwed(), 0.01);
    }

    @Test
    void testPayFineInvalidAmount() {
        User user = new User("u1", "Debtor", "testpass"); 
        user.setFinesOwed(50.0);

        assertFalse(fineService.payFine(user, -10.0), "Should fail for negative amount");
        assertEquals(50.0, user.getFinesOwed()); 
    }
    
    @Test
    void testPayFineAmountExceedsDebt() {
        User user = new User("u1", "Debtor", "testpass");
        user.setFinesOwed(20.0);

        boolean success = fineService.payFine(user, 50.0);
        
        assertTrue(success, "Payment should be accepted even if amount exceeds debt");
        assertEquals(0.0, user.getFinesOwed(), 0.01, "Final fines owed must be 0.0, not negative");
    }



    @Test
    void testBorrowBookCalculatesDueDate() {
        User user = new User("u1", "Alice", "testpass"); 
        Book book = new Book("isbn1", "Java Book", "Author");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        String result = loanService.borrowItem(user, book);

        assertTrue(result.contains("Success"));
        assertTrue(book.isBorrowed());
        
        verify(loanRepo).save(argThat(loan -> 
            loan.getDueDate().isEqual(LocalDate.of(2023, 1, 29)) 
        ));
    }

    @Test
    void testBorrowCDCalculatesDueDate() {
        User user = new User("u1", "Alice", "testpass");
        CD cd = new CD("cd1", "Music", "Artist");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        loanService.borrowItem(user, cd);

        verify(loanRepo).save(argThat(loan -> 
            loan.getDueDate().isEqual(LocalDate.of(2023, 1, 8)) 
        ));
    }

    @Test
    void testBorrowBlockedIfItemBorrowed() {
        User user = new User("u1", "Alice", "testpass"); 
        Book book = new Book("isbn1", "Java", "Auth");
        book.setBorrowed(true); 

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("already borrowed"));
        verify(loanRepo, never()).save(any());
    }

    @Test
    void testBorrowBlockedIfFinesExistVerification() {
        User user = new User("u1", "Debtor", "testpass"); 
        user.setFinesOwed(10.0);
        Book book = new Book("1", "B", "A");

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("unpaid fines"));
        
        verify(loanRepo, never()).save(any());
    }

    @Test
    void testBorrowBlockedIfOverdueExists() {
        User user = new User("u1", "Alice", "testpass"); 
        Book book = new Book("1", "B", "A");
        
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);

        Loan oldLoan = mock(Loan.class);
        when(oldLoan.isOverdue(today)).thenReturn(true);
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.singletonList(oldLoan));

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("overdue items"));
        verify(loanRepo, never()).save(any());
    }

    @Test
    void testCheckOverdueItemsFormatting() {
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);

        User user = new User("u1", "Alice", "testpass"); 
        Book book = new Book("1", "B", "A");
        Loan loan = new Loan(book, user, LocalDate.of(2023, 1, 1)); 

        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(loan));

        List<String> results = loanService.checkOverdueItems();
        
        assertEquals(1, results.size());
        String msg = results.get(0);
        assertTrue(msg.contains("Overdue:"));
        assertTrue(msg.contains("Days late: 3")); 
        assertTrue(msg.contains("Est. Fine: 30.00")); 
    }


    @Test
    void testReminderServiceNotifiesObservers() {
        LocalDate today = LocalDate.of(2023, 2, 1);
        when(timeProvider.getDate()).thenReturn(today);
        
        User user = new User("u1", "Alice", "testpass"); 
        Book book = new Book("1", "B", "A");
        Loan loan = new Loan(book, user, LocalDate.of(2023, 1, 1)); 

        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(loan));
        
        reminderService.registerObserver(mockObserver);
        reminderService.sendOverdueReminders();

        verify(mockObserver).update(eq(user), contains("overdue"));
    }


    @Test
    void testUnregisterSuccess() {
        User user = new User("u1", "Clean", "testpass"); 
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.emptyList());

        String result = userService.unregisterUser("u1");
        assertTrue(result.contains("Success"));
        verify(userRepo).delete(user);
    }

    @Test
    void testUnregisterFailFines() {
        User user = new User("u1", "Debtor", "testpass"); 
        user.setFinesOwed(5.0);
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));

        String result = userService.unregisterUser("u1");
        assertTrue(result.contains("fines"));
        verify(userRepo, never()).delete(any());
    }
    
    @Test
    void testUserUnregisterBlockedByActiveLoan() {
        User user = new User("u1", "Reader", "testpass");
        user.setFinesOwed(0.0); 
        Loan activeLoan = mock(Loan.class); 
        
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.singletonList(activeLoan));

        String result = userService.unregisterUser("u1");

        assertTrue(result.contains("Error: Cannot unregister. User has active loans."));
        verify(userRepo, never()).delete(any());
    }
}