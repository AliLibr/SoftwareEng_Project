package com.library.domain;

import com.library.strategy.BookFineStrategy;

public class Book extends LibraryItem {
    private static final long serialVersionUID = 1L;
    public static final int BOOK_LOAN_PERIOD_DAYS = 28;
    
    private String author;

    public Book(String isbn, String title, String author) {
        super(isbn, title, new BookFineStrategy());
        this.author = author;
    }

    public String getAuthor() { return author; }

    @Override
    public int getLoanPeriodDays() {
        return BOOK_LOAN_PERIOD_DAYS;
    }

    @Override
    public String toString() {
        return super.toString() + " by " + author;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return getId().equals(book.getId()); // Assuming getId() returns ISBN
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}