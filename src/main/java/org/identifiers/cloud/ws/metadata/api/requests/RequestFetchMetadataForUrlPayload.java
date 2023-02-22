package org.identifiers.cloud.ws.metadata.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-17 20:27
 * ---
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestFetchMetadataForUrlPayload {
    private String url;

    public String getUrl() {
        return url;
    }

    public RequestFetchMetadataForUrlPayload setUrl(String url) {
        this.url = url;
        return this;
    }
}
