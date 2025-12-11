package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.library.domain.LibraryItem;
import com.library.domain.Loan;
import com.library.domain.User;

@ExtendWith(MockitoExtension.class)
class LoanTest {

    @Mock
    private LibraryItem mockItem;

    private User testUser;
    private final LocalDate BORROW_DATE = LocalDate.of(2025, 10, 10);

    @BeforeEach
    void setUp() {
        testUser = new User("u1", "Test User", "pass");
    }

    @Test
    void testDueDateCalculationFor28Days() {
        when(mockItem.getLoanPeriodDays()).thenReturn(28);

        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);

        LocalDate expectedDueDate = LocalDate.of(2025, 11, 7);
        assertEquals(expectedDueDate, loan.getDueDate(), "Due date should be calculated correctly for 28 days.");
    }

    @Test
    void testDueDateCalculationFor7Days() {
        when(mockItem.getLoanPeriodDays()).thenReturn(7);

        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);

        LocalDate expectedDueDate = LocalDate.of(2025, 10, 17);
        assertEquals(expectedDueDate, loan.getDueDate(), "Due date should be calculated correctly for 7 days.");
    }

    @Test
    void testIsNotOverdueBeforeDueDate() {
        when(mockItem.getLoanPeriodDays()).thenReturn(28);
        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);

        LocalDate currentDate = LocalDate.of(2025, 11, 6);
        assertFalse(loan.isOverdue(currentDate), "Should not be overdue before the due date.");
    }
    
    @Test
    void testIsNotOverdueOnDueDate() {
        when(mockItem.getLoanPeriodDays()).thenReturn(28);
        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);

        LocalDate currentDate = LocalDate.of(2025, 11, 7);
        assertFalse(loan.isOverdue(currentDate), "Should not be overdue exactly on the due date.");
    }

    @Test
    void testIsOverdueAfterDueDate() {
        when(mockItem.getLoanPeriodDays()).thenReturn(28);
        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);

        LocalDate currentDate = LocalDate.of(2025, 11, 8);
        assertTrue(loan.isOverdue(currentDate), "Should be overdue one day after the due date.");
    }
    
    @Test
    void testIsNotOverdueIfReturned() {
        when(mockItem.getLoanPeriodDays()).thenReturn(28);
        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);
        loan.returnItem();

        LocalDate currentDate = LocalDate.of(2025, 12, 1);
        assertFalse(loan.isOverdue(currentDate), "Should not be overdue if the item has been returned.");
    }

    @Test
    void testReturnItemSetsStatusToInactive() {
        when(mockItem.getLoanPeriodDays()).thenReturn(1);
        Loan loan = new Loan(mockItem, testUser, BORROW_DATE);
        assertTrue(loan.isActive(), "Loan should start as active.");

        loan.returnItem();

        assertFalse(loan.isActive(), "Loan should be inactive after returning.");
    }
}