package com.library.repository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import com.library.domain.User;

public class InMemoryUserRepository implements UserRepository {
    private static final Logger LOGGER = Logger.getLogger(InMemoryUserRepository.class.getName());
    private static final String FILE_NAME = "users.dat";
    private Map<String, User> userStore;

    public InMemoryUserRepository() {
        this.userStore = loadFromFile();
    }

    @Override
    public User save(User user) {
        userStore.put(user.getId(), user);
        saveToFile();
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public void delete(User user) {
        userStore.remove(user.getId());
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(userStore);
        } catch (IOException e) {
            LOGGER.severe("Could not save users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, User> loadFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Map<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.severe("Could not load users: " + e.getMessage());
            }
        }
        return new HashMap<>();
    }
}