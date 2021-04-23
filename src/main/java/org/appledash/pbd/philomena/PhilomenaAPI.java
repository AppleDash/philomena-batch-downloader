package org.appledash.pbd.philomena;

import org.appledash.pbd.philomena.impl.PhilomenaAPIImpl;
import org.appledash.pbd.philomena.structs.ImageResponse;
import org.appledash.pbd.philomena.structs.PhilomenaId;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PhilomenaAPI {
    /**
     * Perform a search against this Philomena API.
     *
     * @param query Search query
     * @return List of image results
     * @throws PhilomenaAPIException If something bad happens
     */
    @NotNull List<ImageResponse> search(String query) throws PhilomenaAPIException;

    /**
     * Perform a batch query against this Philomena API.
     * This constitutes retrieving Image Results for all of the provided image IDs, subject to any
     * restrictions that the API in use may pose on maximum result set size.
     *
     * @param imageIds List of image IDs to request results for
     * @return List of image results
     * @throws PhilomenaAPIException If something bad happens
     */
    @NotNull List<ImageResponse> batchQuery(@NotNull Collection<PhilomenaId> imageIds) throws PhilomenaAPIException;

    /**
     * Request a single image response for an image with the given ID.
     *
     * @param id Image ID
     * @return ImageResult, or empty optional if not found.
     * @throws PhilomenaAPIException If something bad happens
     */
    @NotNull Optional<ImageResponse> getImage(@NotNull PhilomenaId id) throws PhilomenaAPIException;

    static PhilomenaAPI create(URI apiUri) {
        return new PhilomenaAPIImpl(apiUri, null, HttpClient.newHttpClient());
    }

    static PhilomenaAPI create(URI apiUri, HttpClient httpClient) {
        return new PhilomenaAPIImpl(apiUri, null, httpClient);
    }
}
