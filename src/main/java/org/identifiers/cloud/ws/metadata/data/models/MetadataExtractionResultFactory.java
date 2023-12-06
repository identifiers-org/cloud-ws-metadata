package org.identifiers.cloud.ws.metadata.data.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.data.models
 * Timestamp: 2018-09-17 10:11
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataExtractionResultFactory {
    private static final Logger logger = LoggerFactory.getLogger(MetadataExtractionResultFactory.class);

    private static Long ttlResultWithMetadata = 10L;
    private static Long ttlResultWithoutMetadata = 10L;

    // Values from service configuration
    @Value("${org.identifiers.cloud.ws.metadata.backend.data.metadataextractionresults.with.content.ttl}")
    private Duration configTtlResultWithMetadata;
    @Value("${org.identifiers.cloud.ws.metadata.backend.data.metadataextractionresults.without.content.ttl}")
    private Duration configTtlResultWithoutMetadata;

    // Factory methods
    public static MetadataExtractionResult createResultWithMetadata() {
        return new MetadataExtractionResult().setTimeToLive(ttlResultWithMetadata);
    }

    public static MetadataExtractionResult createResultWithoutMetadata() {
        return new MetadataExtractionResult().setTimeToLive(ttlResultWithoutMetadata);
    }

    // Initialize parameters parameters from service configuration
    @PostConstruct
    private void init() {
        logger.info("Setting metadata extraction results TTL to '{}s' and '{}s' for results with and without content",
                configTtlResultWithMetadata, configTtlResultWithoutMetadata);
        ttlResultWithMetadata = configTtlResultWithMetadata.getSeconds();
        ttlResultWithoutMetadata = configTtlResultWithoutMetadata.getSeconds();
    }
}
