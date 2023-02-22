package org.identifiers.cloud.ws.metadata.data.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.data.models
 * Timestamp: 2018-09-17 23:26
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class MetadataExtractionRequestFactory {
    public static MetadataExtractionRequest getMetadataExtractionRequest() {
        return new MetadataExtractionRequest();
    }

    public static MetadataExtractionRequest getMetadataExtractionRequest(ResolvedResource resolvedResource) {
        return new MetadataExtractionRequest()
                .setAccessUrl(resolvedResource.getCompactIdentifierResolvedUrl())
                .setResourceId(Long.toString(resolvedResource.getId()));
    }
}
