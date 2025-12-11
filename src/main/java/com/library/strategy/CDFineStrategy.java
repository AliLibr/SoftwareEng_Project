package com.library.strategy;

import java.io.Serializable;

public class CDFineStrategy implements FineStrategy, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 20.0;
    }
}