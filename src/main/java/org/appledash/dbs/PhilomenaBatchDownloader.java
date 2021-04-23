package org.appledash.dbs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.appledash.dbs.derpibooru.PhilomenaAPI;
import org.appledash.dbs.derpibooru.structs.ImageResponse;
import org.appledash.dbs.derpibooru.structs.PhilomenaId;
import org.appledash.dbs.derpibooru.util.StandardPhilomenaBoorus;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class PhilomenaBatchDownloader {
    private static final int NUM_IMAGE_DOWNLOADERS = 3; /* This seems a good balance between angering the API and speed. */

    private final Logger logger = LogManager.getLogger(PhilomenaBatchDownloader.class);
    private final ExecutorService executorService;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final PhilomenaAPI philomenaAPI = StandardPhilomenaBoorus.buildDerpibooruApi()
            .httpClient(this.httpClient)
            .apiKey(System.getenv("PHILOMENA_API_KEY"))
            .build();

    private final Queue<PhilomenaId> remainingIds;
    private final BlockingQueue<ImageResponse> imageResponses = new LinkedBlockingQueue<>(49); // Chunk size minus one
    private final BlockingQueue<ImageDownloader.Result> downloadResults = new LinkedBlockingQueue<>();

    private final ChunkQuerier chunkQuerier;
    private final Set<ImageDownloader> downloaders = new HashSet<>();

    public PhilomenaBatchDownloader(Collection<PhilomenaId> inputList, Path outputDirectory) {
        this.executorService = Executors.newFixedThreadPool(NUM_IMAGE_DOWNLOADERS + 1);
        this.remainingIds = new ConcurrentLinkedQueue<>(inputList);

        this.chunkQuerier = new ChunkQuerier(this.philomenaAPI, this.remainingIds, this.imageResponses);

        for (int i = 0; i < NUM_IMAGE_DOWNLOADERS; i++) {
            this.downloaders.add(
                    new ImageDownloader(this.httpClient, outputDirectory, this.imageResponses, this.downloadResults)
            );
        }
    }

    public void start() {
        this.logger.info("Will attempt to download {} images", this.remainingIds.size());

        this.executorService.submit(this.chunkQuerier);

        for (ImageDownloader imageDownloader : this.downloaders) {
            this.executorService.submit(imageDownloader);
        }
    }

    public void runUntilComplete(Consumer<ImageDownloader.Result> resultCallback) throws InterruptedException {
        while (!this.remainingIds.isEmpty() || !this.imageResponses.isEmpty() || !this.downloadResults.isEmpty()) {
            resultCallback.accept(this.downloadResults.take());
        }
    }

    public void requestShutdown() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
