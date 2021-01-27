package com.finder.activecookie;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActiveCookieTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
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
    public void findActiveCookie_WhenOnlyOneCookieIsExpectedAsResult() throws IOException {
        String[] args = {"-f", "test1.csv", "-d", "2018-12-09"};
        ActiveCookie.findActiveCookie(args);
        assertEquals("AtY0laUfhglK3lC7", outputStreamCaptor.toString().trim());
    }

    @Test
    public void findActiveCookie_WhenTwoCookiesAreExpectedAsResult() throws IOException {
        String[] args = {"-f", "test2.csv", "-d", "2018-12-09"};
        ActiveCookie.findActiveCookie(args);
        await().until(() -> "AtY0laUfhglK3lC7\nSAZuXPGUrfbcn5UA".equals(outputStreamCaptor.toString().trim())
        );
    }
}