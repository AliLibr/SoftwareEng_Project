package com.library.domain;

import java.util.Objects;

/**
 * Represents a library user.
 */
public class User {
    private String id;
    private String name;
    private double finesOwed;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.finesOwed = 0.0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getFinesOwed() { return finesOwed; }
    public void setFinesOwed(double finesOwed) { this.finesOwed = finesOwed; }

    @Override
    public String toString() {
        return "User [ID=" + id + ", Name=" + name + ", Fines=" + finesOwed + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}