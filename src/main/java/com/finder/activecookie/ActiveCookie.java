package com.finder.activecookie;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ActiveCookie {

    // Extending the solution
    //1. Read from multiple files
    //2. Read from a variety of sources
    //3. Improve performance

    private static final String CRITERIA_MATCHING_DATE_FORMAT = "yyyy-MM-dd";
    private static final String CSV_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final Map<String, Integer> cookieCounterRegistry = new HashMap<>();
    private static ExecutorService service = Executors.newFixedThreadPool(5);

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
     */
    public static void findActiveCookie(String[] args) throws IOException {
        validateInput(args);
        LocalDate dateCriteria = getMatchingDateCriteria(args);
        List<InputStream> fileStreams = getFileStreams(args);
        fileStreams.forEach(fileStream -> {
            try {
                filterRecords(dateCriteria, fileStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
    private static void filterRecords(LocalDate dateCriteria, InputStream fileStream) throws IOException {
        DateTimeFormatter csvDateFormatter = DateTimeFormat.forPattern(CSV_DATE_FORMAT);
        try (Reader reader = new InputStreamReader(fileStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {
            csvParser.getRecords().stream().parallel().forEach(csvRecord -> {
                String cookie = csvRecord.get(0);
                LocalDate logDate = csvDateFormatter.parseLocalDate(csvRecord.get(1));
                if (logDate.equals(dateCriteria)) {
                    updateRegistry(cookie);
                }
                if (logDate.isBefore(dateCriteria)) {
                    // As we have the logs sorted in desc order, when getting to the point where log date goes beyond criteria date
                    // break the iteration
                    //break;
                }
            });
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
    private static List<InputStream> getFileStreams(String[] args) throws IOException {
        String filesArg = args[InputCommandParam.FILE.getIndex() + 1];
        // test.csv, test2.csv
        ClassLoader classLoader = ActiveCookie.class.getClassLoader();
        String[] files = filesArg.split(",");

        List<String> filePats = new ArrayList<>();
        for (int index = 0; index < files.length; index++) {
            filePats.add(files[index]);
        }
        List<InputStream> inputFileStreams = new ArrayList<>();
        filePats.forEach(filePath -> {
            service.submit(() -> {
                if (filePath.startsWith("http")) {
                    // Remote file
                    URI uri = URI.create(filePath);
                    InputStream inputRemoteStream = null;
                    try {
                        inputRemoteStream = uri.toURL().openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    inputFileStreams.add(inputRemoteStream);
                } else {
                    File file = new File(Objects.requireNonNull(classLoader.getResource(filePath)).getFile());
                    try {
                        inputFileStreams.add(new FileInputStream(file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

        });
        return inputFileStreams;
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
