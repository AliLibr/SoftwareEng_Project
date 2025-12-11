package com.library.strategy;

/**
 * Strategy interface for calculating fines.
 * Pattern: Strategy
 */
public interface FineStrategy {
    double calculateFine(long overdueDays);
}