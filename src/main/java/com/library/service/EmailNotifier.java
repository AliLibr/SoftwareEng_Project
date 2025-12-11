package com.library.service;

import com.library.domain.User;
import com.library.observer.Observer;

/**
 * Observer implementation that simulates sending emails.
 */
public class EmailNotifier implements Observer {
    @Override
    public void update(User user, String message) {
        System.out.println("--- [EMAIL SERVER] ---");
        System.out.println("To: " + user.getId() + "@school.edu");
        System.out.println("Message: " + message);
        System.out.println("----------------------");
    }
}