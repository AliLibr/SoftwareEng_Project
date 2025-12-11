package com.library.strategy;


public class BookFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 10.0;
    }
}