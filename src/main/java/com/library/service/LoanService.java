package com.library.service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.library.domain.LibraryItem;
import com.library.domain.Loan;
import com.library.domain.User;
import com.library.repository.LoanRepository;


public class LoanService {

    private final LoanRepository loanRepository;
    private final TimeProvider timeProvider;

    public LoanService(LoanRepository loanRepository, TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.timeProvider = timeProvider;
    }

    public String borrowItem(User user, LibraryItem item) {
        if (item.isBorrowed()) {
            return "Error: Item is already borrowed.";
        }
        
        if (user.getFinesOwed() > 0) {
            return "Error: Cannot borrow. You have unpaid fines.";
        }

        boolean hasOverdue = loanRepository.findActiveLoansByUser(user).stream()
                .anyMatch(loan -> loan.isOverdue(timeProvider.getDate()));

        if (hasOverdue) {
            return "Error: Cannot borrow. You have overdue items.";
        }

        Loan loan = new Loan(item, user, timeProvider.getDate());
        item.setBorrowed(true);
        loanRepository.save(loan);
        
        return "Success: Borrowed " + item.getTitle() + ". Due: " + loan.getDueDate();
    }

    public List<String> checkOverdueItems() {
        return loanRepository.findAllActiveLoans().stream()
            .filter(loan -> loan.isOverdue(timeProvider.getDate()))
            .map(loan -> {
                long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), timeProvider.getDate());
                double fine = loan.getItem().calculateFine(daysOverdue);
                return String.format("Overdue: %s (%s) - Days late: %d - Est. Fine: %.2f", 
                    loan.getItem().getTitle(), 
                    loan.getItem().getClass().getSimpleName(),
                    daysOverdue,
                    fine);
            })
            .collect(Collectors.toList());
    }
}