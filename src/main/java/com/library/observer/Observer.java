package com.library.observer;

import com.library.domain.User;


public interface Observer {
    void update(User user, String message);
}