package org.appledash.pbd.philomena;

import org.appledash.pbd.philomena.impl.PhilomenaAPIImpl;

import java.net.URI;
import java.net.http.HttpClient;

@SuppressWarnings("ReturnOfThis")
public final class PhilomenaAPIBuilder {
    private URI apiUri;
    private String apiKey;
    private HttpClient httpClient;

    public PhilomenaAPIBuilder apiUri(URI apiUri) {
        this.apiUri = apiUri;

        return this;
    }

    public PhilomenaAPIBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;

        return this;
    }

    public PhilomenaAPIBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;

        return this;
    }

    public PhilomenaAPI build() {
        return new PhilomenaAPIImpl(this.apiUri, this.apiKey, this.httpClient == null ? HttpClient.newHttpClient() : this.httpClient);
    }
}
