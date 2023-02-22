package org.identifiers.cloud.ws.metadata.data.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.data.models
 * Timestamp: 2018-09-16 14:25
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class MetadataExtractionRequest implements Serializable, Comparable<MetadataExtractionRequest> {
    private String resolutionPath;
    // When it has been checked (UTC)
    private Timestamp timestamp = new Timestamp(new Date().getTime());
    private String resourceId;
    private String accessUrl;

    public String getResolutionPath() {
        return resolutionPath;
    }

    public MetadataExtractionRequest setResolutionPath(String resolutionPath) {
        this.resolutionPath = resolutionPath;
        return this;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public MetadataExtractionRequest setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public MetadataExtractionRequest setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public MetadataExtractionRequest setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        return this;
    }

    @Override
    public int compareTo(MetadataExtractionRequest o) {
        return this.timestamp.compareTo(o.getTimestamp());
    }
}
