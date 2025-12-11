package com.library.service;

import org.junit.jupiter.api.Test;
import com.library.domain.User;

class EmailNotifierTest {

    @Test
    void testUpdateExecutesNotificationLogic() {
        EmailNotifier notifier = new EmailNotifier();
        User user = new User("testuser", "Test Name", "password");
        String message = "This is a test message.";
        
        notifier.update(user, message);
    }
}