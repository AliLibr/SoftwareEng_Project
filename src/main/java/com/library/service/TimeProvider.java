package com.library.service;

import java.time.LocalDate;

/**
 * Interface for decoupling time logic to allow mocking.
 */
public interface TimeProvider {
    LocalDate getDate();
}