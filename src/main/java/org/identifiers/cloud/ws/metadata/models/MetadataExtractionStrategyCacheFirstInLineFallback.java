package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequestFactory;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResultBuilder;
import org.identifiers.cloud.ws.metadata.data.services.MetadataExtractionResultService;
import org.identifiers.cloud.ws.metadata.data.services.MetadataExtractionResultServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
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
            return metadataExtractionResultService.findByAccessUrl(resolvedResource.getCompactIdentifierResolvedUrl());
        } catch (MetadataExtractionResultServiceException e) {
            logger.error("Could not locate metadata extraction result cache entry for access URL '{}' due to '{}'",
                    resolvedResource.getCompactIdentifierResolvedUrl(), e.getMessage());
        }
        return null;
    }

    @Override
    public MetadataExtractionResult extractMetadata(List<ResolvedResource> resolvedResources) throws
            MetadataExtractionStrategyException {
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
        List<String> reportMessages = new ArrayList<>();
        for (ResolvedResource resolvedResource :
                resolvedResources) {
            logger.info("Processing access URL '{}' with score '{}'", resolvedResource.getCompactIdentifierResolvedUrl(),
                    resolvedResource.getRecommendation().getRecommendationIndex());
            // Get metadata from cache
            MetadataExtractionResult cachedMetadataExtractionResult = getCachedMetadataExtractionResult(resolvedResource);
            if (cachedMetadataExtractionResult == null) {
                // queue a metadata extraction request
                logger.info("Queuing metadata extraction request for access URL '{}' score '{}'",
                        resolvedResource.getCompactIdentifierResolvedUrl(),
                        resolvedResource.getRecommendation().getRecommendationIndex());
                metadataExtractionRequestQueue
                        .add(MetadataExtractionRequestFactory.getMetadataExtractionRequest(resolvedResource));
                reportMessages.add(String.format("No cached metadata for access URL '%s', score '%s'",
                        resolvedResource.getCompactIdentifierResolvedUrl(),
                        resolvedResource.getRecommendation().getRecommendationIndex()));
                // Keep looking
                continue;
            }
            if (cachedMetadataExtractionResult.getHttpStatus() != 200) {
                if (metadataExtractionResult == null) {
                    String message = String.format("Temporarily using Metadata Extraction Result for access URL '%s'," +
                                    " " +
                                    "resource score '%s', " +
                                    "with HTTP Status '%s'",
                            resolvedResource.getCompactIdentifierResolvedUrl(),
                            resolvedResource.getRecommendation().getRecommendationIndex(),
                            HttpStatus.resolve(cachedMetadataExtractionResult.getHttpStatus()).toString());
                    logger.warn(message);
                    reportMessages.add(message);
                    metadataExtractionResult = cachedMetadataExtractionResult;
                    continue;
                } else {
                    logger.warn("Ignore cache entry for access URL '{}' as its HTTP Status is '{}' " +
                            "as I already have a candidate metadata extraction result for " +
                            "access URL '{}', HTTP Status '{}'",
                            cachedMetadataExtractionResult.getAccessUrl(),
                            cachedMetadataExtractionResult.getHttpStatus(),
                            metadataExtractionResult.getAccessUrl(),
                            metadataExtractionResult.getHttpStatus());
                }
            }
            // If we get here it means we got something, so keep it if, and only if, we didn't keep metadata from
            // previous iterations
            if (cachedMetadataExtractionResult.getHttpStatus() == 200) {
                if ((metadataExtractionResult == null) || (metadataExtractionResult.getHttpStatus() != 200)) {
                    String message = String.format("Valid Cached metadata for access URL '%s', score '%s', overwrites" +
                                    " previous selections",
                            resolvedResource.getCompactIdentifierResolvedUrl(),
                            resolvedResource.getRecommendation().getRecommendationIndex());
                    metadataExtractionResult = cachedMetadataExtractionResult;
                    logger.info(message);
                    reportMessages.add(message);
                } else {
                    String message = String.format("Ignoring valid Cached metadata for access URL '%s', score '%s' " +
                                    "as we have already found cached metadata for a higher scoring resource at access" +
                                    " URL '%s'",
                            resolvedResource.getCompactIdentifierResolvedUrl(),
                            resolvedResource.getRecommendation().getRecommendationIndex(),
                            metadataExtractionResult.getAccessUrl());
                    logger.warn(message);
                    reportMessages.add(message);
                }
                // We explore all the given resolved resources
            }
        }
        if (metadataExtractionResult == null) {
            // Do in-line metadata extraction
            String message = String.format("No Cached metadata found!, running in-line metadata extraction for " +
                            "access URL '%s', score '%s'",
                    resolvedResources.get(0).getCompactIdentifierResolvedUrl(),
                    resolvedResources.get(0).getRecommendation().getRecommendationIndex());
            logger.warn(message);
            reportMessages.add(message);
            metadataExtractionResult =
                    metadataExtractionResultBuilder
                            .attendMetadataExtractionRequest(metadataFetcher,
                                    MetadataExtractionRequestFactory
                                            .getMetadataExtractionRequest(resolvedResources.get(0)));
            if (metadataExtractionResult.getHttpStatus() != 200) {
                // NOTE - I may be duplicating error messages
                logger.error(metadataExtractionResult.getErrorMessage());
            }
            reportMessages.add(metadataExtractionResult.getErrorMessage());
        }
        // Finally, set the messages
        metadataExtractionResult.setErrorMessage(String.join("\n", reportMessages));
        return metadataExtractionResult;
    }
}