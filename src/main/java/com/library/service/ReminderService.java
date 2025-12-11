package com.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.library.domain.Loan;
import com.library.domain.User;
import com.library.observer.Observer;
import com.library.observer.Subject;
import com.library.repository.LoanRepository;

/**
 * Service for sending overdue reminders. Acts as a Subject.
 */
public class ReminderService implements Subject {
    private final LoanRepository loanRepository;
    private final TimeProvider timeProvider;
    private final List<Observer> observers;

    public ReminderService(LoanRepository loanRepository, TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.timeProvider = timeProvider;
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(User user, String message) {
        for (Observer observer : observers) {
            observer.update(user, message);
        }
    }

    public void sendOverdueReminders() {
        LocalDate today = timeProvider.getDate();
        List<Loan> activeLoans = loanRepository.findAllActiveLoans();

        for (Loan loan : activeLoans) {
            if (loan.isOverdue(today)) {
                long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), today);
                String message = String.format("Item '%s' is overdue by %d days. Please return it.", 
                    loan.getItem().getTitle(), daysLate);
                notifyObservers(loan.getUser(), message);
            }
        }
    }
}