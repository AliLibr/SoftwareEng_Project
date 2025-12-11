package com.library.strategy;

import java.io.Serializable;

public class BookFineStrategy implements FineStrategy, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 10.0;
    }
}