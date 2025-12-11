package com.library.domain;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String password; 
    private double finesOwed;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.finesOwed = 0.0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
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