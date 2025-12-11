package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.User;
import com.library.strategy.BookFineStrategy;
import com.library.strategy.CDFineStrategy;

class DomainTest {

    @Test
    void testBookEqualityAndHash() {
        Book b1 = new Book("123", "Title", "Author");
        Book b2 = new Book("123", "Title", "Author");
        Book b3 = new Book("456", "Other", "Other");

        assertEquals(b1, b2);
        assertNotEquals(b1, b3);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1.hashCode(), b3.hashCode());
        assertNotEquals(b1, null);
        assertNotEquals(b1, "Some String");
    }

    @Test
    void testBookToString() {
        Book b = new Book("1", "Java", "Me");
        assertTrue(b.toString().contains("Java"));
        assertTrue(b.toString().contains("Me"));
        assertTrue(b.toString().contains("Book"));
    }

    @Test
    void testCDEqualityAndHash() {
        CD c1 = new CD("1", "Album", "Artist");
        CD c2 = new CD("1", "Album", "Artist");
        
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testCDToString() {
        CD c = new CD("1", "Album", "Artist");
        assertTrue(c.toString().contains("Album"));
        assertTrue(c.toString().contains("Artist"));
        assertTrue(c.toString().contains("CD"));
    }

    @Test
    void testUserEqualityAndHash() {
        User u1 = new User("u1", "Name", "pass");
        User u2 = new User("u1", "Name", "pass");
        User u3 = new User("u2", "Other", "pass");

        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertTrue(u1.toString().contains("u1"));
    }
    
    @Test
    void testStrategies() {
        BookFineStrategy bs = new BookFineStrategy();
        assertEquals(50.0, bs.calculateFine(5));
        
        CDFineStrategy cs = new CDFineStrategy();
        assertEquals(100.0, cs.calculateFine(5));
    }
}