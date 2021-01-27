package com.finder.activecookie;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ActiveCookie {

    private static final String CRITERIA_MATCHING_DATE_FORMAT = "yyyy-MM-dd";
    private static final String CSV_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final Map<String, Integer> cookieCounterRegistry = new HashMap<>();

    public static void main(String[] args) throws IOException {
        findActiveCookie(args);
    }

    /**
     * A method which
     * <ul>
     *     <li>Validates the input parameters</li>
     *     <li>Filters the records that match the input date criteria</li>
     *     <li>Analyse the cookie count in registry and prints on console</li>
     * </ul>
     *
     * @param args
     * @throws IOException
     */
    public static void findActiveCookie(String[] args) throws IOException {
        validateInput(args);
        LocalDate dateCriteria = getMatchingDateCriteria(args);
        String absoluteFilePath = getAbsoluteFilePath(args);
        filterRecords(dateCriteria, absoluteFilePath);
        List<String> results = analyseRegistry();
        results.forEach(System.out::println);
    }

    /**
     * Scans through the csv file and increment occurrences for each cookie record
     *
     * @param dateCriteria
     * @param absoluteFilePath
     * @throws IOException
     */
    private static void filterRecords(LocalDate dateCriteria, String absoluteFilePath) throws IOException {
        DateTimeFormatter csvDateFormatter = DateTimeFormat.forPattern(CSV_DATE_FORMAT);
        try (Reader reader = new FileReader(absoluteFilePath, UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {
            for (CSVRecord record : csvParser) {
                String cookie = record.get(0);
                LocalDate logDate = csvDateFormatter.parseLocalDate(record.get(1));
                if (logDate.equals(dateCriteria)) {
                    updateRegistry(cookie);
                }
                if (logDate.isBefore(dateCriteria)) {
                    // As we have the logs sorted in desc order, when getting to the point where log date goes beyond criteria date
                    // break the iteration
                    break;
                }
            }
        }
    }

    /**
     * Validates input param and throws RuntimeException if invalid Accepted input param format : -f FILE_PATH -d DATE
     */
    private static void validateInput(String[] args) {
        if (args.length != 4) {
            throw new RuntimeException("Expected 4 input parameters");
        }
        if (!(InputCommandParam.FILE.getParam().equals(args[0]) && InputCommandParam.DATE.getParam().equals(args[2]))) {
            throw new RuntimeException(String.format("Invalid Command submitted %s %s %s %s", args[0], args[1], args[2], args[3]));
        }
    }

    /**
     * Formats and returns the input date param to acceptable date format. Throws IllegalArgumentException if incorrect format input param is supplied
     *
     * @param args
     * @return LocalDate
     */
    private static LocalDate getMatchingDateCriteria(String[] args) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(CRITERIA_MATCHING_DATE_FORMAT);
        return formatter.parseLocalDate(args[3]);
    }

    /**
     * Returns the absolute path of file specified in the input param, the next argument after -f index.
     *
     * @param args
     * @return String
     */
    private static String getAbsoluteFilePath(String[] args) {
        String fileName = args[InputCommandParam.FILE.getIndex() + 1];
        ClassLoader classLoader = ActiveCookie.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
        return file.getAbsolutePath();
    }

    /**
     * Updates the cookie registry with the cookie occurrence.
     */
    private static void updateRegistry(String cookie) {
        if (cookieCounterRegistry.containsKey(cookie)) {
            cookieCounterRegistry.put(cookie, cookieCounterRegistry.get(cookie) + 1);
        } else {
            cookieCounterRegistry.put(cookie, 1);
        }
    }

    /**
     * Returns the cookie with the maximum occurrence. If two or more cookies occur for the same time then return all cookies with same maximum count
     *
     * @return List
     */
    private static List<String> analyseRegistry() {
        if (cookieCounterRegistry.isEmpty()) {
            return List.of();
        }
        int max = cookieCounterRegistry.values().stream().max(Comparator.naturalOrder()).get();
        return cookieCounterRegistry.entrySet().stream()
                .filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
