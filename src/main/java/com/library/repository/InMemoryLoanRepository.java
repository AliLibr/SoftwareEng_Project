package com.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.library.domain.Loan;
import com.library.domain.User;

public class InMemoryLoanRepository implements LoanRepository {
    private static final List<Loan> loanStore = new ArrayList<>();

    @Override
    public void save(Loan loan) {
        loanStore.add(loan);
    }

    @Override
    public List<Loan> findAllActiveLoans() {
        return loanStore.stream()
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> findActiveLoansByUser(User user) {
        return loanStore.stream()
                .filter(loan -> loan.isActive() && loan.getUser().equals(user))
                .collect(Collectors.toList());
    }
}