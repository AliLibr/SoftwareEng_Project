package com.library.service;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.library.presentation.LibraryCLI;

class CLITest {

    @Test
    void testMainExit() {

        String input = "0\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);


        LibraryCLI.main(new String[]{});
        

    }
}