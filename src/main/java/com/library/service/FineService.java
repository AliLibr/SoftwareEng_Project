package com.library.service;

import com.library.domain.User;

public class FineService {

    public boolean payFine(User user, double amount) {
        if (amount <= 0) return false;
        
        double currentFines = user.getFinesOwed();
        if (currentFines <= 0) return false;

        double newBalance = Math.max(0, currentFines - amount);
        user.setFinesOwed(newBalance);
        return true;
    }
}