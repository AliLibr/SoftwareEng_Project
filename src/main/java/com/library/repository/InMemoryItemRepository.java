package com.library.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.library.domain.LibraryItem;

public class InMemoryItemRepository implements ItemRepository {
    private static final Map<String, LibraryItem> inventory = new HashMap<>();

    @Override
    public void save(LibraryItem item) {
        inventory.put(item.getId(), item);
    }

    @Override
    public Optional<LibraryItem> findById(String id) {
        return Optional.ofNullable(inventory.get(id));
    }

    @Override
    public List<LibraryItem> searchByTitle(String title) {
        return inventory.values().stream()
            .filter(item -> item.getTitle().toLowerCase().contains(title.toLowerCase()))
            .collect(Collectors.toList());
    }
}