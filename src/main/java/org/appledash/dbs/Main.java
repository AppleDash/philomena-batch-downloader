package org.appledash.dbs;

import org.appledash.dbs.derpibooru.structs.PhilomenaId;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Main {
    private static final Path OUTPUT_DIRECTORY = Paths.get("derpibooru_images/");
    private static final Path SUCCESS_LOG = Paths.get("success.txt");
    private static final Path ERROR_LOG = Paths.get("error.txt");

    public static void main(String[] args) throws IOException, InterruptedException {
        PrintWriter successWriter = new PrintWriter(Files.newBufferedWriter(SUCCESS_LOG, StandardOpenOption.APPEND, StandardOpenOption.CREATE), true);
        PrintWriter errorWriter = new PrintWriter(Files.newBufferedWriter(ERROR_LOG, StandardOpenOption.APPEND, StandardOpenOption.CREATE), true);

        Files.createDirectories(OUTPUT_DIRECTORY);
        List<String> downloadables = new ArrayList<>(Files.readAllLines(Paths.get("/home/appledash/Desktop/without_loc_ids.txt")));
        downloadables.removeAll(Files.readAllLines(SUCCESS_LOG));

        PhilomenaBatchDownloader downloader = new PhilomenaBatchDownloader(downloadables.stream().map(PhilomenaId::of).collect(Collectors.toList()), OUTPUT_DIRECTORY);
        downloader.start();

        downloader.runUntilComplete(result -> {
            if (result.success()) {
                successWriter.println(result.id());
            } else {
                errorWriter.println(result.id());
            }
        });

        downloader.requestShutdown();
    }
}
