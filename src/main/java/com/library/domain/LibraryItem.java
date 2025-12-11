package com.library.domain;

import com.library.strategy.FineStrategy;

/**
 * Abstract base class for all library items.
 */
public abstract class LibraryItem {
    protected String id;
    protected String title;
    protected boolean isBorrowed;
    protected FineStrategy fineStrategy;

    public LibraryItem(String id, String title, FineStrategy fineStrategy) {
        this.id = id;
        this.title = title;
        this.fineStrategy = fineStrategy;
        this.isBorrowed = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isBorrowed() { return isBorrowed; }
    public void setBorrowed(boolean borrowed) { isBorrowed = borrowed; }

    public double calculateFine(long overdueDays) {
        return fineStrategy.calculateFine(overdueDays);
    }

    public abstract int getLoanPeriodDays();

    @Override
    public String toString() {
        return String.format("[%s] %s (ID: %s)", this.getClass().getSimpleName(), title, id);
    }
}