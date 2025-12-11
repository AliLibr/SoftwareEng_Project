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
    void testLoginSuccess() {
        assertTrue(authService.login("admin", "secret"));
    }

    @Test
    void testLoginFailure() {
        assertFalse(authService.login("admin", "wrong"));
    }
    
    @Test
    void testLoginMissingEnv() {
        System.clearProperty("LIBRARY_ADMIN_USER");
        System.clearProperty("LIBRARY_ADMIN_PASS");
        assertFalse(authService.login("admin", "secret"));
    }

    @Test
    void testSecureUserLogin() {
        String userId = "u10";
        String password = "securepass";
        User mockUser = new User(userId, "Secure User", password);
        
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        
        User foundUser = userRepo.findById(userId).get();
        assertEquals(password, foundUser.getPassword());
        assertNotEquals("wrong", foundUser.getPassword());
    }

    @Test
    void testPayFineSuccess() {
        User user = new User("u1", "Debtor", "pass");
        user.setFinesOwed(50.0);
        assertTrue(fineService.payFine(user, 50.0));
        assertEquals(0.0, user.getFinesOwed());
    }
    
    @Test
    void testPayFinePartial() {
        User user = new User("u1", "Debtor", "pass");
        user.setFinesOwed(50.0);
        assertTrue(fineService.payFine(user, 25.0));
        assertEquals(25.0, user.getFinesOwed());
    }
    
    @Test
    void testPayFineInvalid() {
        User user = new User("u1", "Debtor", "pass");
        user.setFinesOwed(50.0);
        assertFalse(fineService.payFine(user, -10.0));
        assertEquals(50.0, user.getFinesOwed());
        
        user.setFinesOwed(0.0);
        assertFalse(fineService.payFine(user, 10.0));
    }

    @Test
    void testBorrowBookSuccess() {
        User user = new User("u1", "Alice", "pass");
        Book book = new Book("1", "Title", "Auth");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("Success"));
        verify(loanRepo).save(any(Loan.class));
    }
    
    @Test
    void testBorrowCDSuccess() {
        User user = new User("u1", "Alice", "pass");
        CD cd = new CD("1", "Title", "Artist");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);

        loanService.borrowItem(user, cd);
        verify(loanRepo).save(any(Loan.class));
    }

    @Test
    void testBorrowBlockedIfFines() {
        User user = new User("u1", "Debtor", "pass");
        user.setFinesOwed(10.0);
        Book book = new Book("1", "Title", "Auth");

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("unpaid fines"));
        verify(loanRepo, never()).save(any());
    }
    
    @Test
    void testBorrowBlockedIfAlreadyBorrowed() {
        User user = new User("u1", "Alice", "pass");
        Book book = new Book("1", "Title", "Auth");
        book.setBorrowed(true);

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("already borrowed"));
        verify(loanRepo, never()).save(any());
    }
    
    @Test
    void testBorrowBlockedIfOverdue() {
        User user = new User("u1", "Alice", "pass");
        Book book = new Book("1", "Title", "Auth");
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);
        
        Loan overdueLoan = mock(Loan.class);
        when(overdueLoan.isOverdue(today)).thenReturn(true);
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.singletonList(overdueLoan));

        String result = loanService.borrowItem(user, book);
        assertTrue(result.contains("overdue items"));
        verify(loanRepo, never()).save(any());
    }

    @Test
    void testUnregisterSuccess() {
        User user = new User("u1", "Clean", "pass");
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.emptyList());

        assertTrue(userService.unregisterUser("u1").contains("Success"));
        verify(userRepo).delete(user);
    }
    
    @Test
    void testUnregisterFailUserNotFound() {
        when(userRepo.findById("u99")).thenReturn(Optional.empty());
        assertTrue(userService.unregisterUser("u99").contains("Error: User not found"));
    }
    
    @Test
    void testUnregisterFailFines() {
        User user = new User("u1", "Debtor", "pass");
        user.setFinesOwed(10.0);
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        
        assertTrue(userService.unregisterUser("u1").contains("unpaid fines"));
        verify(userRepo, never()).delete(any());
    }
    
    @Test
    void testUnregisterFailActiveLoans() {
        User user = new User("u1", "Debtor", "pass");
        when(userRepo.findById("u1")).thenReturn(Optional.of(user));
        when(loanRepo.findActiveLoansByUser(user)).thenReturn(Collections.singletonList(mock(Loan.class)));
        
        assertTrue(userService.unregisterUser("u1").contains("active loans"));
        verify(userRepo, never()).delete(any());
    }
    
    @Test
    void testSendReminders() {
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);
        
        User user = new User("u1", "A", "p");
        Book book = new Book("1", "T", "A");
        Loan mockLoan = mock(Loan.class);
        when(mockLoan.isOverdue(today)).thenReturn(true);
        when(mockLoan.getUser()).thenReturn(user);
        when(mockLoan.getItem()).thenReturn(book);
        when(mockLoan.getDueDate()).thenReturn(today.minusDays(1));
        
        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(mockLoan));
        
        reminderService.registerObserver(mockObserver);
        reminderService.sendOverdueReminders();
        
        verify(mockObserver).update(eq(user), contains("overdue"));
    }
    
    @Test
    void testCheckOverdueItems() {
        LocalDate today = LocalDate.of(2023, 1, 1);
        when(timeProvider.getDate()).thenReturn(today);
        
        Loan mockLoan = mock(Loan.class);
        when(mockLoan.isOverdue(today)).thenReturn(true);
        when(mockLoan.getDueDate()).thenReturn(today.minusDays(5));
        when(mockLoan.getItem()).thenReturn(new Book("1","T","A"));
        
        when(loanRepo.findAllActiveLoans()).thenReturn(Collections.singletonList(mockLoan));
        
        List<String> report = loanService.checkOverdueItems();
        assertEquals(1, report.size());
        assertTrue(report.get(0).contains("Days late: 5"));
    }
}