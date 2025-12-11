package com.library.observer;

import com.library.domain.User;

/**
 * Observer interface for receiving notifications.
 * Pattern: Observer
 */
public interface Observer {
    void update(User user, String message);
}