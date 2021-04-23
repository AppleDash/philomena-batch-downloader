package org.appledash.pbd.workers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.appledash.pbd.philomena.PhilomenaAPI;
import org.appledash.pbd.philomena.PhilomenaAPIException;
import org.appledash.pbd.philomena.structs.ImageResponse;
import org.appledash.pbd.philomena.structs.PhilomenaId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public final class ChunkQuerier implements Runnable {
    private static final int CHUNK_SIZE = 50; /* Number of images to grab API responses for at once. */
    private static final int BACK_OFF_TIME = 15; /* Fifteen seconds */

    private final Logger logger = LogManager.getLogger(ChunkQuerier.class);
    private final PhilomenaAPI api;
    private final Queue<PhilomenaId> inputQueue;
    private final BlockingQueue<ImageResponse> outputQueue;

    /* Assignment of mutable queues is intentional :) */
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public ChunkQuerier(PhilomenaAPI api, Queue<PhilomenaId> inputQueue, BlockingQueue<ImageResponse> outputQueue) {
        this.api = api;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while (!this.inputQueue.isEmpty()) {
            List<PhilomenaId> chunk = new ArrayList<>(CHUNK_SIZE);

            for (int i = 0; i < CHUNK_SIZE && !this.inputQueue.isEmpty(); i++) {
                chunk.add(this.inputQueue.remove());
            }

            this.logger.info("Requesting new chunk ({} IDs)...", chunk.size());
            List<ImageResponse> results =  this.queryChunk(chunk);
            this.logger.info("Got {} results.", results.size());

            for (ImageResponse result : results) {
                try {
                    /* This might block, which is cool - means we're not ready for more yet.. */
                    this.outputQueue.put(result);
                } catch (InterruptedException e) {
                    this.logger.error(e);
                    return;
                }
            }
        }
    }

    private List<ImageResponse> queryChunk(List<PhilomenaId> chunk) {
        List<ImageResponse> results = Collections.emptyList();
        int tryCount = 5;

        for (int i = 1; i <= tryCount; i++) {
            try {
                results = this.api.batchQuery(chunk);
                break;
            } catch (PhilomenaAPIException e) {
                int sleepSeconds = BACK_OFF_TIME * i;

                this.logger.error("Philomena API returned an error, sleeping for {} seconds... (Try #{}/{})", sleepSeconds, i, tryCount, e);

                try {
                    Thread.sleep(sleepSeconds * 1000L);
                } catch (InterruptedException interruptedException) {
                    this.logger.error(interruptedException);
                    Thread.currentThread().interrupt();
                }
            }
        }

        return Collections.unmodifiableList(results);
    }
}
