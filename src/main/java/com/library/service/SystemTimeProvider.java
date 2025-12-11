package com.library.service;

import java.time.LocalDate;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDate getDate() {
        return LocalDate.now();
    }
}