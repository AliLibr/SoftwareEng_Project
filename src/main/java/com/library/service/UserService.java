package com.library.service;

import java.util.List;
import com.library.domain.Loan;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;

/**
 * Service for User lifecycle management.
 */
public class UserService {
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public UserService(UserRepository userRepository, LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    public String unregisterUser(String userId) {
        return userRepository.findById(userId).map(user -> {
            if (user.getFinesOwed() > 0) {
                return "Error: Cannot unregister. User has unpaid fines.";
            }
            List<Loan> activeLoans = loanRepository.findActiveLoansByUser(user);
            if (!activeLoans.isEmpty()) {
                return "Error: Cannot unregister. User has active loans.";
            }
            userRepository.delete(user);
            return "Success: User " + user.getName() + " unregistered.";
        }).orElse("Error: User not found.");
    }
}