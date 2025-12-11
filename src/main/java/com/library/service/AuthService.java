package com.library.service;

/**
 * Service for Admin Authentication.
 * SECURITY UPDATE: Checks System Properties first (for testing), then Env Vars.
 * This prevents hardcoding passwords in the source code.
 */
public class AuthService {
    
    private boolean isLoggedIn = false;

    /**
     * Attempts to log in using credentials from the environment.
     * @param username Input username
     * @param password Input password
     * @return true if credentials match the environment settings
     */
    public boolean login(String username, String password) {
        // 1. Try to get credentials from System Properties (Used by JUnit Tests)
        String validUser = System.getProperty("LIBRARY_ADMIN_USER");
        String validPass = System.getProperty("LIBRARY_ADMIN_PASS");

        // 2. If not found, try Environment Variables (Used by Real App/Eclipse Run Config)
        if (validUser == null) validUser = System.getenv("LIBRARY_ADMIN_USER");
        if (validPass == null) validPass = System.getenv("LIBRARY_ADMIN_PASS");

        // Safety check: If no config is found anywhere, fail securely.
        if (validUser == null || validPass == null) {
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