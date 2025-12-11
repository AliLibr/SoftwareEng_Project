package com.library.strategy;

/**
 * Implementation of fine strategy for CDs (20 NIS/day).
 */
public class CDFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 20.0;
    }
}