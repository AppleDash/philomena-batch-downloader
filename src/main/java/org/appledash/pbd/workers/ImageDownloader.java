package org.appledash.pbd.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.appledash.pbd.philomena.structs.ImageResponse;
import org.appledash.pbd.philomena.structs.PhilomenaId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public final class ImageDownloader implements Runnable {
    private static final String USER_AGENT = "philomena-batch-downloader/0.1.0";

    private final Logger logger = LogManager.getLogger(ImageDownloader.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final HttpClient httpClient;
    private final Path outputDirectory;
    private final BlockingQueue<ImageResponse> inputQueue;
    private final Queue<Result> outputQueue;

    /* Assignment of mutable queues is intentional :) */
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public ImageDownloader(HttpClient httpClient, Path outputDirectory, BlockingQueue<ImageResponse> inputQueue, Queue<Result> outputQueue) {
        this.httpClient = httpClient;
        this.outputDirectory = outputDirectory;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ImageResponse result;

            try {
                result = this.inputQueue.take();
            } catch (InterruptedException e) {
                break;
            }

            this.outputQueue.add(this.processImage(result));
        }
    }

    private Result processImage(ImageResponse result) {
        PhilomenaId imageId = result.id();
        this.logger.info("Downloading {}", imageId);

        try (Writer jsonWriter = Files.newBufferedWriter(this.outputDirectory.resolve(imageId + ".json"));
             OutputStream imageWriter = Files.newOutputStream(this.outputDirectory.resolve(imageId + "." + result.format()))) {

            this.objectMapper.writeValue(jsonWriter, result);
            this.downloadImage(result, imageWriter);

            this.logger.info("Downloaded {}", imageId);

            return Result.success(imageId);
        } catch (IOException e) {
            this.logger.error(e);

            return Result.failure(imageId);
        }
    }

    private void downloadImage(ImageResponse imageResponse, OutputStream outputStream) throws IOException {
        String viewUrl = imageResponse.viewUrl();
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


    public static final record Result(PhilomenaId id, boolean success) {
        public static Result success(PhilomenaId id) {
            return new Result(id, true);
        }

        public static Result failure(PhilomenaId id) {
            return new Result(id, false);
        }
    }
}
