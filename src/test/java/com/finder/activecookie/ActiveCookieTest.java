package com.finder.activecookie;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActiveCookieTest {

    private PrintStream stdout = System.out;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreSystemOutStream() {
        System.setOut(stdout);
    }

    @Test
    public void validateCommandParams_WhenIncorrectNumberOfArgumentsSupplied() {
        String[] args = {"-f", "abc.text", "-d"};
        assertThrows(RuntimeException.class, () -> ActiveCookie.findActiveCookie(args));
    }

    @Test
    public void validateCommandParams_WhenIncorrectParamSequenceSupplied() {
        String[] args = {"-d", "2021-01-27", "-f", "log.csv"};
        assertThrows(RuntimeException.class, () -> ActiveCookie.findActiveCookie(args));
    }

    @Test
    public void validateCommandParams_WhenInvalidDateIsProvided() {
        String[] args = {"-f", "log.csv", "-d", "27-01-2020"};
        assertThrows(IllegalArgumentException.class, () -> ActiveCookie.findActiveCookie(args));
    }

    @Test
    public void findActiveCookie_WhenTwoCookiesAreExpectedAsResult() throws IOException {
        String[] args = {"-f", "test2.csv", "-d", "2018-12-09"};
        ActiveCookie.findActiveCookie(args);
        await().until(() -> "AtY0laUfhglK3lC7\nSAZuXPGUrfbcn5UA".equals(output.toString().trim())
        );
    }
}