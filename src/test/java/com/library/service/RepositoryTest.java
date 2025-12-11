package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.library.domain.*;
import com.library.repository.*;
import com.library.strategy.BookFineStrategy;

/**
 * Tests for the Repository Layer (Data Storage).
 * Raises code coverage by testing CRUD operations.
 */
class RepositoryTest {

    private InMemoryItemRepository itemRepo;
    private InMemoryUserRepository userRepo;
    private InMemoryLoanRepository loanRepo;

    @BeforeEach
    void setUp() {
        itemRepo = new InMemoryItemRepository();
        userRepo = new InMemoryUserRepository();
        loanRepo = new InMemoryLoanRepository();
    }

    // --- ITEM REPO TESTS ---
    @Test
    void testSaveAndFindItem() {
        Book book = new Book("1", "Title", "Author");
        itemRepo.save(book);
        
        Optional<LibraryItem> found = itemRepo.findById("1");
        assertTrue(found.isPresent());
        assertEquals("Title", found.get().getTitle());
    }

    @Test
    void testSearchByTitle() {
        itemRepo.save(new Book("1", "Harry Potter 1", "JKR"));
        itemRepo.save(new Book("2", "Harry Potter 2", "JKR"));
        itemRepo.save(new CD("3", "Pink Floyd", "Band"));
        
        List<LibraryItem> results = itemRepo.searchByTitle("Harry");
        assertEquals(2, results.size());
    }
    
    @Test
    void testFindMissingItem() {
        assertFalse(itemRepo.findById("999").isPresent());
    }

    // --- USER REPO TESTS ---
    @Test
    void testSaveAndFindUser() {
        User user = new User("u1", "Bob");
        userRepo.save(user);
        
        Optional<User> found = userRepo.findById("u1");
        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }
    
    @Test
    void testDeleteUser() {
        User user = new User("u1", "Bob");
        userRepo.save(user);
        userRepo.delete(user);
        
        assertFalse(userRepo.findById("u1").isPresent());
    }

    // --- LOAN REPO TESTS ---
    @Test
    void testLoanQueries() {
        User user = new User("u1", "Bob");
        Book book = new Book("1", "T", "A");
        Loan loan = new Loan(book, user, LocalDate.now());
        
        loanRepo.save(loan);
        
        // Test findAllActiveLoans
        assertEquals(1, loanRepo.findAllActiveLoans().size());
        
        // Test findActiveLoansByUser
        assertEquals(1, loanRepo.findActiveLoansByUser(user).size());
        
        // Test filtering
        User otherUser = new User("u2", "Alice");
        assertEquals(0, loanRepo.findActiveLoansByUser(otherUser).size());
    }
    
    @Test
    void testInactiveLoansAreIgnored() {
        User user = new User("u1", "Bob");
        Book book = new Book("1", "T", "A");
        Loan loan = new Loan(book, user, LocalDate.now());
        loan.returnItem(); // Make it inactive
        
        loanRepo.save(loan);
        
        assertEquals(0, loanRepo.findAllActiveLoans().size());
    }
}