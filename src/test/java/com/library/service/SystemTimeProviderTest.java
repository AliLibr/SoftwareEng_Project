package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SystemTimeProviderTest {

    @Test
    void testGetDateReturnsCurrentDate() {
        SystemTimeProvider provider = new SystemTimeProvider();
        
        LocalDate actualDate = provider.getDate();
        LocalDate expectedDate = LocalDate.now();
        
        assertEquals(expectedDate, actualDate, 
            "SystemTimeProvider should return the actual current date.");
    }
}