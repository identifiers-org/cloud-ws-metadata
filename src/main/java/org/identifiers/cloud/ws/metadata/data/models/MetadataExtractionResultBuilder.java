package org.identifiers.cloud.ws.metadata.data.models;

import org.identifiers.cloud.ws.metadata.models.MetadataFetcher;
import org.identifiers.cloud.ws.metadata.models.MetadataFetcherException;
import org.springframework.http.HttpStatus;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.data.models
 * Timestamp: 2018-09-17 23:35
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface MetadataExtractionResultBuilder {

    default MetadataExtractionResult attendMetadataExtractionRequest(MetadataFetcher metadataFetcher, MetadataExtractionRequest request) {
        Object metadata = null;
        MetadataExtractionResult metadataExtractionResult = null;
        try {
            metadata = metadataFetcher.fetchMetadataFor(request.getAccessUrl());
            metadataExtractionResult = MetadataExtractionResultFactory.createResultWithMetadata()
                    .setHttpStatus(200)
                    .setMetadataContent(metadata);
        } catch (MetadataFetcherException e) {
            metadataExtractionResult = MetadataExtractionResultFactory.createResultWithoutMetadata();
            metadataExtractionResult.setErrorMessage(String.format("FAILED to fetch metadata for resolution path '%s'," +
                            " because '%s'",
                    request.getResolutionPath(),
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.INTERNAL_ERROR.getValue()) {
                metadataExtractionResult.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            } else if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.METADATA_NOT_FOUND.getValue()) {
                metadataExtractionResult.setHttpStatus(HttpStatus.NOT_FOUND.value());
            } else if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.METADATA_INVALID.getValue()) {
                metadataExtractionResult.setHttpStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            } else {
                metadataExtractionResult.setHttpStatus(HttpStatus.BAD_REQUEST.value());
            }
        }
        metadataExtractionResult
                .setAccessUrl(request.getAccessUrl())
                .setRequestTimestamp(request.getTimestamp().toString())
                .setResourceId(request.getResourceId())
                .setResolutionPath(request.getResolutionPath());
        return metadataExtractionResult;
    }
}
