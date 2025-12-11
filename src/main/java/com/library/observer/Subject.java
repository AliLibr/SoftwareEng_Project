package com.library.observer;

import com.library.domain.User;


public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(User user, String message);
}