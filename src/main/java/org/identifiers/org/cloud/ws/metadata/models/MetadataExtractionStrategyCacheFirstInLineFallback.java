package org.identifiers.org.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionRequestFactory;
import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionResultBuilder;
import org.identifiers.org.cloud.ws.metadata.data.services.MetadataExtractionResultService;
import org.identifiers.org.cloud.ws.metadata.data.services.MetadataExtractionResultServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.models
 * Timestamp: 2018-09-17 18:27
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This strategy will try to get metadata from the cache first, falling back to metadata inline extraction if there is
 * nothing on the cache for the given resolved resources.
 * <p>
 * Let's explain this with a little bit more details.
 * <p>
 * When a list of resolved resources is presented to this metadata extraction strategy, it will iterate through that
 * list from the most recommenden to the least recommended.
 * <p>
 * For every resource:
 * - if the resolved resource has not been seen before (no metadata extraction result in the cache), a metadata
 * extraction request is queued. Continue.
 * - if the resolved resource has been seen before, but it has no metadata, log the absence of metadata and keep
 * looking. Continue.
 * - if the resolved resource has been seen before, and it had metadata, set this metadata as the result and log its
 * provenance, as it may not come from the best scoring resolved resource.
 * - Upon loop exit, if we have no metadata, do an in-line metadata collection of the most recommended resource and send
 * whatever results back to the client, including all possible logs of the process to get here.
 * Else, if we have metadata, send it back to the client including all possible logs of the process to get here.
 */
@Component
public class MetadataExtractionStrategyCacheFirstInLineFallback implements MetadataExtractionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(MetadataExtractionStrategyCacheFirstInLineFallback
            .class);

    @Autowired
    private MetadataExtractionResultService metadataExtractionResultService;
    @Autowired
    private MetadataFetcher metadataFetcher;
    @Autowired
    private MetadataExtractionResultBuilder metadataExtractionResultBuilder;
    @Autowired
    private BlockingDeque<MetadataExtractionRequest> metadataExtractionRequestQueue;

    // Helpers
    private MetadataExtractionResult getCachedMetadataExtractionResult(ResolvedResource resolvedResource) {
        try {
            return metadataExtractionResultService.findByAccessUrl(resolvedResource.getAccessUrl());
        } catch (MetadataExtractionResultServiceException e) {
            logger.error("Could not locate metadata extraction result cache entry for access URL '{}' due to '{}'",
                    resolvedResource.getAccessUrl(), e.getMessage());
        }
        return null;
    }

    @Override
    public MetadataExtractionResult extractMetadata(List<ResolvedResource> resolvedResources) throws MetadataExtractionStrategyException {
        resolvedResources.sort((r1, r2) -> {
            if (r1.getRecommendation().getRecommendationIndex() == r2.getRecommendation().getRecommendationIndex()) {
                return 0;
            }
            if (r1.getRecommendation().getRecommendationIndex() > r2.getRecommendation().getRecommendationIndex()) {
                return -1;
            }
            return 1;
        });
        // Prepare result
        MetadataExtractionResult metadataExtractionResult = null;
        for (ResolvedResource resolvedResource :
                resolvedResources) {
            logger.info("Processing access URL '{}' with score '{}'", resolvedResource.getAccessUrl(),
                    resolvedResource.getRecommendation().getRecommendationIndex());
            // Get metadata from cache
            MetadataExtractionResult cachedMetadataExtractionResult = getCachedMetadataExtractionResult(resolvedResource);
            if ((cachedMetadataExtractionResult == null) || (cachedMetadataExtractionResult.getHttpStatus() != 200)) {
                // queue a metadata extraction request
                logger.info("Queuing metadata extraction request for access URL '{}' score '{}'",
                        resolvedResource.getAccessUrl(),
                        resolvedResource.getRecommendation().getRecommendationIndex());
                metadataExtractionRequestQueue
                        .add(MetadataExtractionRequestFactory.getMetadataExtractionRequest(resolvedResource));
                // Keep looking
                continue;
            }
            // If we get here it means we got valid metadata, so keep it if, and only if, we didn't keep metadata from
            // previous iterations
            if (metadataExtractionResult == null) {
                metadataExtractionResult = cachedMetadataExtractionResult;
            }
            // We explore all the given resolved resources
        }
        if (metadataExtractionResult == null) {
            // TODO - Do in-line metadata extraction
        }
        return metadataExtractionResult;
    }
}
