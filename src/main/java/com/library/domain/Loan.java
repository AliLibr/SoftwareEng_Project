package com.library.domain;

import java.time.LocalDate;

/**
 * Represents a loan transaction.
 */
public class Loan {
    private LibraryItem item;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean isActive;

    public Loan(LibraryItem item, User user, LocalDate borrowDate) {
        this.item = item;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(item.getLoanPeriodDays());
        this.isActive = true;
    }

    public LibraryItem getItem() { return item; }
    public User getUser() { return user; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isActive() { return isActive; }

    public void returnItem() {
        this.isActive = false;
    }

    public boolean isOverdue(LocalDate currentDate) {
        return isActive && currentDate.isAfter(dueDate);
    }
}