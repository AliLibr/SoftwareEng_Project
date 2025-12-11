package com.library.repository;

import java.util.List;
import java.util.Optional;
import com.library.domain.LibraryItem;

public interface ItemRepository {
    void save(LibraryItem item);
    Optional<LibraryItem> findById(String id);
    List<LibraryItem> searchByTitle(String title);
}