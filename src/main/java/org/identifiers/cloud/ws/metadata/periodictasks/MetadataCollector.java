package org.identifiers.cloud.ws.metadata.periodictasks;

import org.identifiers.cloud.ws.metadata.channels.PublisherException;
import org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult.MetadataExtractionResultPublisher;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResultBuilder;
import org.identifiers.cloud.ws.metadata.data.services.MetadataExtractionResultService;
import org.identifiers.cloud.ws.metadata.data.services.MetadataExtractionResultServiceException;
import org.identifiers.cloud.ws.metadata.models.MetadataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.daemons
 * Timestamp: 2018-09-17 10:27
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataCollector implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MetadataCollector.class);

    @Value("${org.identifiers.cloud.ws.metadata.backend.data.metadatacollector.wait_time_poll_metadata_extraction_request_queue}")
    private Duration waitTimePollMetadataExtractionRequestQueue;

    private final BlockingDeque<MetadataExtractionRequest> metadataExtractionRequestQueue;
    private final MetadataExtractionResultPublisher metadataExtractionResultPublisher;
    private final MetadataExtractionResultService metadataExtractionResultService;
    private final MetadataExtractionResultBuilder metadataExtractionResultBuilder;
    private final MetadataFetcher metadataFetcher;
    public MetadataCollector(
            BlockingDeque<MetadataExtractionRequest> metadataExtractionRequestQueue,
            MetadataExtractionResultPublisher metadataExtractionResultPublisher,
            MetadataExtractionResultService metadataExtractionResultService,
            MetadataExtractionResultBuilder metadataExtractionResultBuilder,
            MetadataFetcher metadataFetcher) {
        this.metadataExtractionRequestQueue = metadataExtractionRequestQueue;
        this.metadataExtractionResultPublisher = metadataExtractionResultPublisher;
        this.metadataExtractionResultService = metadataExtractionResultService;
        this.metadataExtractionResultBuilder = metadataExtractionResultBuilder;
        this.metadataFetcher = metadataFetcher;
    }

    @Override
    public void run() {
        logger.info("--- [START] Metadata Collection Daemon ---");
        while (true) {
            try {
                logger.info("Polling metadata extraction request queue");
                MetadataExtractionRequest metadataExtractionRequest = nextMetadataExtractionRequest();
                if (metadataExtractionRequest == null) break;

                MetadataExtractionResult metadataExtractionResult = attendMetadataExtractionRequest(metadataExtractionRequest);
                if (metadataExtractionResult != null) {
                    persist(metadataExtractionResult);
                    announce(metadataExtractionResult);
                }
            } catch (RuntimeException e) {
                logger.error("An error has been stopped for preventing the thread from crashing", e);
            }
        }
        logger.info("--- [END] Metadata Collection Daemon ---");
    }

    private MetadataExtractionRequest nextMetadataExtractionRequest() {
        try {
            return metadataExtractionRequestQueue.pollFirst(waitTimePollMetadataExtractionRequestQueue.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while polling Metadata Extraction Request Queue", e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    MetadataExtractionResult attendMetadataExtractionRequest(MetadataExtractionRequest request) {
        logger.info("Attending metadata extraction request for Access URL '{}', resolution path '{}'",
                request.getAccessUrl(), request.getResolutionPath());
        return metadataExtractionResultBuilder.attendMetadataExtractionRequest(metadataFetcher, request);
    }

    private void persist(MetadataExtractionResult result) {
        try {
            metadataExtractionResultService.save(result);
        } catch (MetadataExtractionResultServiceException e) {
            logger.error("FAILED to persist metadata extraction result due to '{}'", e.getMessage());
        }
    }

    private void announce(MetadataExtractionResult result) {
        try {
            metadataExtractionResultPublisher.publish(result);
        } catch (PublisherException e) {
            logger.error("FAILED to announce metadata extraction result for Access URL '{}' due to '{}'",
                    result.getAccessUrl(), e.getMessage());
        }
    }


}
