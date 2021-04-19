package org.appledash.dbs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.appledash.dbs.derpibooru.PhilomenaAPI;
import org.appledash.dbs.derpibooru.PhilomenaAPIException;
import org.appledash.dbs.derpibooru.structs.ImageResult;
import org.appledash.dbs.derpibooru.structs.PhilomenaId;
import org.appledash.dbs.derpibooru.util.StandardPhilomenaBoorus;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class DerpibooruBatchDownloader {
    private static final String USER_AGENT = "derpibooru-batch-downloader/0.1.0";
    private static final int NUM_THREADS = 3; /* This seems a good balance between angering the API and speed. */
    private static final int CHUNK_SIZE = 50; /* Number of images to grab API responses for at once. */
    private static final Path OUTPUT_DIRECTORY = Paths.get("derpibooru_images/");
    private static final Path SUCCESS_LOG = Paths.get("success.txt");
    private static final Path ERROR_LOG = Paths.get("error.txt");

    private final Logger logger = LogManager.getLogger(DerpibooruBatchDownloader.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final PhilomenaAPI philomenaAPI = StandardPhilomenaBoorus.buildDerpibooruApi()
            .httpClient(this.httpClient)
            .apiKey(System.getenv("PHILOMENA_API_KEY"))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Queue<PhilomenaId> remainingIds;
    private final Path outputDirectory;
    private final PrintWriter successWriter;
    private final PrintWriter errorWriter;

    public static void main(String[] args) throws IOException, InterruptedException {
        Files.createDirectories(OUTPUT_DIRECTORY);
        List<String> downloadables = new ArrayList<>(Files.readAllLines(Paths.get("/home/appledash/Desktop/without_loc_ids.txt")));
        downloadables.removeAll(Files.readAllLines(SUCCESS_LOG));

        DerpibooruBatchDownloader downloader = new DerpibooruBatchDownloader(downloadables.stream().map(PhilomenaId::of).collect(Collectors.toList()), OUTPUT_DIRECTORY);
        downloader.run();
    }

    public DerpibooruBatchDownloader(Collection<PhilomenaId> idsToDownload, Path outputDirectory) throws IOException {
        this.remainingIds = new ConcurrentLinkedQueue<>(idsToDownload);
        this.outputDirectory = outputDirectory;
        this.successWriter = new PrintWriter(Files.newBufferedWriter(SUCCESS_LOG, StandardOpenOption.APPEND), true);
        this.errorWriter = new PrintWriter(Files.newBufferedWriter(ERROR_LOG, StandardOpenOption.APPEND), true);
    }

    public void run() throws InterruptedException {
        this.logger.info("Will attempt to download {} images", this.remainingIds.size());
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < Math.ceil((double) this.remainingIds.size() / NUM_THREADS); i++) {
            executorService.submit(this::buildAndProcessChunk);
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    private void buildAndProcessChunk() {
        List<PhilomenaId> chunk = new ArrayList<>(CHUNK_SIZE);

        for (int i = 0; i < CHUNK_SIZE && !this.remainingIds.isEmpty(); i++) {
            chunk.add(this.remainingIds.remove());
        }

        List<ImageResult> results = Collections.emptyList();

        for (int i = 0; i < 5; i++) {
            try {
                results = this.philomenaAPI.batchQuery(chunk);
                break;
            } catch (PhilomenaAPIException e) {
                this.logger.error("Philomena API returned an error, sleeping for 30 seconds... (Try #{}/5)", i + 1, e);

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException interruptedException) {
                    this.logger.error(interruptedException);
                    Thread.currentThread().interrupt();
                }
            }
        }

        /* At this point, if we failed 5 times, results will still be empty and this loop will not loop. */

        for (ImageResult result : results) {
            this.processResult(result);
        }
    }

    private void processResult(ImageResult result) {
        int imageId = result.id();
        this.logger.info("Downloading {}", imageId);

        try (Writer jsonWriter = Files.newBufferedWriter(this.outputDirectory.resolve(imageId + ".json"));
             OutputStream imageWriter = Files.newOutputStream(this.outputDirectory.resolve(imageId + "." + result.format()))) {

            this.objectMapper.writeValue(jsonWriter, result);
            this.downloadImage(result, imageWriter);

            this.logger.info("Downloaded {}", imageId);

            synchronized (this.successWriter) {
                this.successWriter.println(imageId);
            }
        } catch (IOException e) {
            this.logger.error(e);

            synchronized (this.errorWriter) {
                this.errorWriter.println(imageId);
            }
        }
    }

    private void downloadImage(ImageResult imageResult, OutputStream outputStream) throws IOException {
        String viewUrl = imageResult.viewUrl();
        HttpRequest request = HttpRequest.newBuilder(URI.create(viewUrl))
                .header("User-Agent", USER_AGENT).GET().build();
        try {
            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            response.body().transferTo(outputStream);
            outputStream.flush();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
