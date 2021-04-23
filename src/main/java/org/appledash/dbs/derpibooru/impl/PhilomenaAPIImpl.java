package org.appledash.dbs.derpibooru.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.appledash.dbs.derpibooru.PhilomenaAPI;
import org.appledash.dbs.derpibooru.PhilomenaAPIException;
import org.appledash.dbs.derpibooru.structs.ImageResponse;
import org.appledash.dbs.derpibooru.structs.PhilomenaId;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PhilomenaAPIImpl implements PhilomenaAPI {
    private static final int HTTP_200_OK = 200;
    private static final int HTTP_404_NOT_FOUND = 404;

    private final Logger logger = LogManager.getLogger(PhilomenaAPIImpl.class);

    private final URI rootUri;
    private final HttpClient httpClient;
    private final @Nullable String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS);

    public PhilomenaAPIImpl(@NotNull URI rootUri, @Nullable String apiKey, @NotNull HttpClient httpClient) {
        this.rootUri = rootUri;
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    @Override
    public @NotNull List<ImageResponse> search(String query) throws PhilomenaAPIException {
        return this.makeApiRequest("search/images?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8), MultipleImageResult.class).images();
    }

    @Override
    public @NotNull List<ImageResponse> batchQuery(@NotNull Collection<PhilomenaId> imageIds) throws PhilomenaAPIException {
        if (imageIds.isEmpty()) {
            throw new IllegalArgumentException("imageIds must not be empty :(");
        }

        return this.search(this.buildBatchQuery(imageIds));
    }

    @Override
    public @NotNull Optional<ImageResponse> getImage(@NotNull PhilomenaId id) throws PhilomenaAPIException {
        SingleImageResult result = this.makeApiRequest("images/" + id.rawId(), SingleImageResult.class, true);

        return result == null ? Optional.empty() : Optional.of(result.image());
    }

    private <T> @NotNull T makeApiRequest(String path, Class<T> expectedResponseType) throws PhilomenaAPIException {
        return this.makeApiRequest(path, expectedResponseType, false);
    }

    @Contract("_, _, true -> _; _, _, false -> !null")
    private <T> @Nullable T makeApiRequest(String path, Class<T> expectedResponseType, boolean returnNullForNotFound) throws PhilomenaAPIException {
        String fullPath;

        if (this.apiKey != null) {
            char separator = path.indexOf('?') == -1 ? '?' : '&';

            fullPath = path + separator + "key=" + URLEncoder.encode(this.apiKey, StandardCharsets.UTF_8);
        } else {
            fullPath = path;
        }

        URI uri = this.rootUri.resolve(fullPath);

        this.logger.debug("Making API request to \"{}\", expecting {}.", uri, expectedResponseType.getSimpleName());

        HttpRequest request = HttpRequest.newBuilder(uri).header("User-Agent", "derpibooru-batch-scraper/0.1.0").GET().build();

        try {
            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != HTTP_200_OK) {
                if (response.statusCode() == HTTP_404_NOT_FOUND && returnNullForNotFound) {
                    return null;
                }

                throw new PhilomenaAPIException("Bad status code: " + response.statusCode());
            }

            return this.objectMapper.readValue(response.body(), expectedResponseType);
        } catch (IOException | InterruptedException e) {
            throw new PhilomenaAPIException("I/O error", e);
        }
    }

    private String buildBatchQuery(Collection<PhilomenaId> imageIds) {
        StringJoiner joiner = new StringJoiner(" || ");

        for (PhilomenaId id : imageIds) {
            joiner.add("id:" + id.rawId());
        }

        return joiner.toString();
    }

    private static record SingleImageResult(ImageResponse image) { }

    private static record MultipleImageResult(List<ImageResponse> images) { }
}
