package org.identifiers.cloud.ws.metadata.api.responses;

import java.io.Serializable;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models.api.responses
 * Timestamp: 2018-03-07 10:59
 * ---
 */
public class ResponseFetchMetadataPayload implements Serializable {
    Object metadata;

    public Object getMetadata() {
        return metadata;
    }

    public ResponseFetchMetadataPayload setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }
}
