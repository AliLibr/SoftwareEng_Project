package com.library.repository;

import java.util.List;
import com.library.domain.Loan;
import com.library.domain.User;

public interface LoanRepository {
    void save(Loan loan);
    List<Loan> findAllActiveLoans();
    List<Loan> findActiveLoansByUser(User user);
}