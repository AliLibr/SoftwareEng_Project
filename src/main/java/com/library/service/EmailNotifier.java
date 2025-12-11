package com.library.service;

import java.util.logging.Logger;
import com.library.domain.User;
import com.library.observer.Observer;

public class EmailNotifier implements Observer {
    
    private static final Logger LOGGER = Logger.getLogger(EmailNotifier.class.getName());

    @Override
    public void update(User user, String message) {
        LOGGER.info("--- [EMAIL SERVER] ---");
        LOGGER.info(() -> "To: " + user.getId() + "@school.edu");
        LOGGER.info("Message: " + message);
        LOGGER.info("----------------------");
    }
}