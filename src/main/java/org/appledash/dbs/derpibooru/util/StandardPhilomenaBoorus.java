package org.appledash.dbs.derpibooru.util;

import org.appledash.dbs.derpibooru.PhilomenaAPIBuilder;
import org.jetbrains.annotations.Contract;

import java.net.URI;

public final class StandardPhilomenaBoorus {
    public static final URI DERPIBOORU_API_URI = URI.create("https://derpibooru.org/api/v1/json/");
    public static final URI PONYBOORU_API_URI = URI.create("https://ponybooru.org/api/v1/json/");
    public static final URI PONERPICS_API_URI = URI.create("https://ponerpics.org/api/v1/json/");
    public static final URI MANEBOORU_API_URI = URI.create("https://manebooru.art/api/v1/json/");

    private StandardPhilomenaBoorus() {
    }

    @Contract(" -> new")
    public static PhilomenaAPIBuilder buildDerpibooruApi() {
        return new PhilomenaAPIBuilder().apiUri(DERPIBOORU_API_URI);
    }

    @Contract(" -> new")
    public static PhilomenaAPIBuilder buildPonybooruApi() {
        return new PhilomenaAPIBuilder().apiUri(PONYBOORU_API_URI);
    }

    @Contract(" -> new")
    public static PhilomenaAPIBuilder buildPonerpicsApi() {
        return new PhilomenaAPIBuilder().apiUri(PONERPICS_API_URI);
    }

    @Contract(" -> new")
    public static PhilomenaAPIBuilder buildManebooruApi() {
        return new PhilomenaAPIBuilder().apiUri(MANEBOORU_API_URI);
    }
}
