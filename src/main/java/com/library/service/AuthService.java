package com.library.service;

import java.util.logging.Logger;

public class AuthService {
    
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private boolean isLoggedIn = false;

    public boolean login(String username, String password) {
        String validUser = System.getProperty("LIBRARY_ADMIN_USER");
        String validPass = System.getProperty("LIBRARY_ADMIN_PASS");

        if (validUser == null) validUser = System.getenv("LIBRARY_ADMIN_USER");
        if (validPass == null) validPass = System.getenv("LIBRARY_ADMIN_PASS");

        if (validUser == null || validPass == null) {
            LOGGER.severe("ERROR: Could not find credentials.");
            LOGGER.severe("Please set LIBRARY_ADMIN_USER and LIBRARY_ADMIN_PASS in Eclipse Run Configurations -> Environment.");
            return false;
        }

        if (validUser.equals(username) && validPass.equals(password)) {
            isLoggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        isLoggedIn = false;
    }

    public boolean isAdminLoggedIn() {
        return isLoggedIn;
    }
}