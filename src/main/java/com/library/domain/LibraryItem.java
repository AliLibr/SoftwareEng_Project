package com.library.domain;

import java.io.Serializable;
import com.library.strategy.FineStrategy;

public abstract class LibraryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String id;
    protected String title;
    protected boolean isBorrowed;
    protected FineStrategy fineStrategy;

    protected LibraryItem(String id, String title, FineStrategy fineStrategy) {
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