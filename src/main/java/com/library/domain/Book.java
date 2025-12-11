package com.library.domain;

import com.library.strategy.BookFineStrategy;

/**
 * Represents a Book entity.
 */
public class Book extends LibraryItem {
    private String author;

    public Book(String isbn, String title, String author) {
        super(isbn, title, new BookFineStrategy());
        this.author = author;
    }

    public String getAuthor() { return author; }

    @Override
    public int getLoanPeriodDays() {
        return 28;
    }

    @Override
    public String toString() {
        return super.toString() + " by " + author;
    }
}