package com.library.observer;

import com.library.domain.User;

/**
 * Subject interface for managing observers.
 * Pattern: Observer
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(User user, String message);
}