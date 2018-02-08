package org.identifiers.org.cloud.ws.metadata.models;

import org.springframework.http.HttpStatus;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.models
 * Timestamp: 2018-02-07 11:36
 * ---
 */
public class MetadataApiResponse {

    private HttpStatus httpStatus = HttpStatus.OK;
    private String errorMessage;
    // Right now we focus on JSON-LD formatted metadata and we're not doing anything with it, so I keep it as a String
    private String metadata;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public MetadataApiResponse setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public MetadataApiResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public MetadataApiResponse setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }
}
