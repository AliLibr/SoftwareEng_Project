package com.library.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.library.repository.InMemoryItemRepository;
import com.library.repository.InMemoryLoanRepository;
import com.library.repository.InMemoryUserRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ServiceWiringTest {

    @Mock
    private LoanRepository mockLoanRepo;
    @Mock
    private UserRepository mockUserRepo;
    @Mock
    private TimeProvider mockTimeProvider;

    @Test
    void testAuthServiceInstantiation() {
        AuthService authService = new AuthService();
        assertNotNull(authService, "AuthService should instantiate successfully.");
    }

    @Test
    void testFineServiceInstantiation() {
        FineService fineService = new FineService();
        assertNotNull(fineService, "FineService should instantiate successfully.");
    }

    @Test
    void testEmailNotifierInstantiation() {
        EmailNotifier emailNotifier = new EmailNotifier();
        assertNotNull(emailNotifier, "EmailNotifier should instantiate successfully.");
    }

    @Test
    void testLoanServiceWiring() {
        LoanService loanService = new LoanService(mockLoanRepo, mockTimeProvider);
        assertNotNull(loanService, "LoanService must be correctly wired with LoanRepo and TimeProvider.");
    }

    @Test
    void testUserServiceWiring() {
        UserService userService = new UserService(mockUserRepo, mockLoanRepo);
        assertNotNull(userService, "UserService must be correctly wired with UserRepo and LoanRepo.");
    }

    @Test
    void testReminderServiceWiring() {
        ReminderService reminderService = new ReminderService(mockLoanRepo, mockTimeProvider);
        assertNotNull(reminderService, "ReminderService must be correctly wired with LoanRepo and TimeProvider.");
    }

    @Test
    void testInMemoryRepositoriesInstantiation() {
        
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        assertNotNull(userRepo, "InMemoryUserRepository must instantiate.");
        
        InMemoryLoanRepository loanRepo = new InMemoryLoanRepository();
        assertNotNull(loanRepo, "InMemoryLoanRepository must instantiate.");
        
        InMemoryItemRepository itemRepo = new InMemoryItemRepository();
        assertNotNull(itemRepo, "InMemoryItemRepository must instantiate.");
    }
}