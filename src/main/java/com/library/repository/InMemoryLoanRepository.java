package com.library.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.library.domain.Loan;
import com.library.domain.User;

public class InMemoryLoanRepository implements LoanRepository {
    private static final Logger LOGGER = Logger.getLogger(InMemoryLoanRepository.class.getName());
    private static final String FILE_NAME = "loans.dat";
    private List<Loan> loanStore;

    public InMemoryLoanRepository() {
        this.loanStore = loadFromFile();
    }

    @Override
    public void save(Loan loan) {
        loanStore.add(loan);
        saveToFile();
    }

    @Override
    public List<Loan> findAllActiveLoans() {
        return loanStore.stream()
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> findActiveLoansByUser(User user) {
        return loanStore.stream()
                .filter(loan -> loan.isActive() && loan.getUser().equals(user))
                .collect(Collectors.toList());
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(new ArrayList<>(loanStore));
        } catch (IOException e) {
            LOGGER.severe("Could not save loans: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Loan> loadFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<Loan>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.severe("Could not load loans: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }
}