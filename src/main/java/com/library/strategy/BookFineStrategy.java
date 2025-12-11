package com.library.strategy;

/**
 * Implementation of fine strategy for Books (10 NIS/day).
 */
public class BookFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 10.0;
    }
}