package org.identifiers.org.cloud.ws.metadata.data.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.data.models
 * Timestamp: 2018-09-16 14:43
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@RedisHash(value = "MetadataMetadataExtractionResult")
public class MetadataExtractionResult implements Serializable, Comparable<MetadataExtractionResult> {
    // TODO
    @Id
    private String id;
    @Indexed
    private String resourceId;
    @Indexed
    private String accessUrl;
    @Indexed
    private String timestamp = (new Timestamp(new Date().getTime())).toString();
    // When this check was requested (UTC)
    private String requestTimestamp;
    @Indexed
    private int httpStatus;
    private String metadataContent;
    private String errorMessage;

    public String getResourceId() {
        return resourceId;
    }

    public MetadataExtractionResult setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public MetadataExtractionResult setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        return this;
    }

    public Timestamp getTimestamp() {
        return Timestamp.valueOf(timestamp);
    }

    public MetadataExtractionResult setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp.toString();
        return this;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public MetadataExtractionResult setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
        return this;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public MetadataExtractionResult setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public String getMetadataContent() {
        return metadataContent;
    }

    public MetadataExtractionResult setMetadataContent(String metadataContent) {
        this.metadataContent = metadataContent;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public MetadataExtractionResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public int compareTo(MetadataExtractionResult o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }
}
