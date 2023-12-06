package org.identifiers.cloud.ws.metadata.api.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.cloud.ws.metadata.api.requests.ServiceRequestFetchMetadataForUrl;
import org.identifiers.cloud.ws.metadata.api.responses.ResponseFetchMetadataForUrlPayload;
import org.identifiers.cloud.ws.metadata.api.responses.ResponseFetchMetadataPayload;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadata;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadataForUrl;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.api.ApiCentral;
import org.identifiers.cloud.ws.metadata.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-07 11:29
 * ---
 */
@Component
public class MetadataApiModel {
    private static final Logger logger = LoggerFactory.getLogger(MetadataApiModel.class);
    private final IdResolver idResolver;
    private final MetadataFetcher metadataFetcher;
    private final IdResourceSelector idResourceSelector;
    private final MetadataExtractionStrategy metadataExtractionStrategy;

    public MetadataApiModel(IdResolver idResolver,
                            MetadataFetcher metadataFetcher,
                            IdResourceSelector idResourceSelector,
                            MetadataExtractionStrategy metadataExtractionStrategy) {
        this.idResolver = idResolver;
        this.metadataFetcher = metadataFetcher;
        this.idResourceSelector = idResourceSelector;
        this.metadataExtractionStrategy = metadataExtractionStrategy;
    }

    private ServiceResponseFetchMetadata createDefaultResponseFetchMetadata(HttpStatus httpStatus, String errorMessage) {
        ServiceResponseFetchMetadata response = new ServiceResponseFetchMetadata();
        response.setApiVersion(ApiCentral.apiVersion)
                .setHttpStatus(httpStatus)
                .setErrorMessage(errorMessage);
        response.setPayload(new ResponseFetchMetadataPayload().setMetadata(""));
        return response;
    }

    private ServiceResponseFetchMetadataForUrl createDefaultResponseFetchMetadataForUrl(HttpStatus httpStatus, String errorMessage) {
        ServiceResponseFetchMetadataForUrl response = new ServiceResponseFetchMetadataForUrl();
        response.setApiVersion(ApiCentral.apiVersion)
                .setHttpStatus(httpStatus)
                .setErrorMessage(errorMessage);
        ResponseFetchMetadataForUrlPayload payload = new ResponseFetchMetadataForUrlPayload();
        payload.setMetadata("");
        response.setPayload(payload);
        return response;
    }

    private List<ResolvedResource> resolveCompactId(String compactId, ServiceResponseFetchMetadata response) {
        // Resolve the Compact ID
        List<ResolvedResource> resources = new ArrayList<>();
        try {
            resources = idResolver.resolve(compactId);
        } catch (IdResolverException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for Compact ID '%s', because '%s'",
                    compactId,
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
            return resources;
        }
        if (resources.isEmpty()) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for Compact ID '%s' " +
                    "because NO RESOURCES COULD BE FOUND", compactId));
            response.setHttpStatus(HttpStatus.NOT_FOUND);
        }
        return resources;
    }

    private List<ResolvedResource> resolveCompactId(String selector, String compactId, ServiceResponseFetchMetadata
            response) {
        List<ResolvedResource> resources = new ArrayList<>();
        try {
            resources = idResolver.resolve(selector, compactId);
            if (resources.isEmpty()) {
                response.setErrorMessage(String.format("FAILED to fetch metadata for Compact ID '%s', selector '%s'" +
                        "because NO RESOURCES COULD BE FOUND", selector, compactId));
                response.setHttpStatus(HttpStatus.NOT_FOUND);
            }
        } catch (IdResolverException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for Compact ID '%s', selector '%s', " +
                            "because '%s'",
                    selector,
                    compactId,
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }
        return resources;
    }

    private List<ResolvedResource> resolveRawRequest(String rawRequest, ServiceResponseFetchMetadata response) {
        List<ResolvedResource> resources = new ArrayList<>();
        try {
            resources = idResolver.resolveRawRequest(rawRequest);
            if (resources.isEmpty()) {
                response.setErrorMessage(String.format("FAILED to fetch metadata for request '%s', NO RESOURCES COULD" +
                        " BE FOUND", rawRequest));
                response.setHttpStatus(HttpStatus.NOT_FOUND);
            }
            // TODO
        } catch (IdResolverException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for requested '%s', due to '%s'",
                    rawRequest,
                    e.getMessage()));
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }
        return resources;
    }

    private ResolvedResource selectResource(String compactIdOrRequest,
                                            List<ResolvedResource> resources,
                                            ServiceResponseFetchMetadata response) {
        ResolvedResource selectedResource;
        try {
            selectedResource = idResourceSelector.selectResource(resources);
        } catch (IdResourceSelectorException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for '%s', " +
                            "because '%s'",
                    compactIdOrRequest,
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return null;
        }
        // Log selection
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Mining metadata for '{}' on selected resource '{}'",
                    compactIdOrRequest,
                    mapper.writeValueAsString(selectedResource));
        } catch (JsonProcessingException e) {
            // TODO will never happen ^_^
        }
        return selectedResource;
    }

    private void extractMetadata(ResolvedResource resolvedResource, ServiceResponseFetchMetadata response, String selector, String compactIdOrRequest) {
        // Extract the metadata
        try {
            response.getPayload().setMetadata(metadataFetcher.fetchMetadataFor(resolvedResource.getCompactIdentifierResolvedUrl()));
        } catch (MetadataFetcherException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for '%s', %s, " +
                            "because '%s'",
                    compactIdOrRequest,
                    (selector == null) ? "no selector information extracted" : String.format("selector '%s'", selector),
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.INTERNAL_ERROR.getValue()) {
                response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.METADATA_NOT_FOUND.getValue
                    ()) {
                response.setHttpStatus(HttpStatus.NOT_FOUND);
            } else if (e.getErrorCode().getValue() == MetadataFetcherException.ErrorCode.METADATA_INVALID.getValue()) {
                response.setHttpStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            } else {
                response.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void extractMetadata(List<ResolvedResource> resolvedResources, ServiceResponseFetchMetadata response, String rawRequest) {
        try {
            MetadataExtractionResult metadataExtractionResult =
                    metadataExtractionStrategy.extractMetadata(resolvedResources);
            response.getPayload().setMetadata(metadataExtractionResult.getMetadataContent());
            response.setErrorMessage(metadataExtractionResult.getErrorMessage());
            response.setHttpStatus(HttpStatus.valueOf(metadataExtractionResult.getHttpStatus()));
        } catch (MetadataExtractionStrategyException e) {
            String errorMessage = String.format("Error while trying to locate metadate for resolution request '%s', " +
                            "due to '%s'",
                    rawRequest,
                    e.getMessage());
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setErrorMessage(errorMessage);
            logger.error(errorMessage);
        }
    }

    // --- API Methods ---
    public ServiceResponseFetchMetadata getMetadataFor(String compactId) {
        ServiceResponseFetchMetadata response = createDefaultResponseFetchMetadata(HttpStatus.OK, "");
        List<ResolvedResource> resources = resolveCompactId(compactId, response);
        if (response.getHttpStatus() == HttpStatus.OK) {
            // Select the provider
            ResolvedResource selectedResource = selectResource(compactId, resources, response);
            if (response.getHttpStatus() == HttpStatus.OK && selectedResource != null) {
                extractMetadata(selectedResource, response, null, compactId);
            }
        }
        // return the response
        return response;
    }

    public ServiceResponseFetchMetadata getMetadataFor(String selector, String compactId) {
        ServiceResponseFetchMetadata response = createDefaultResponseFetchMetadata(HttpStatus.OK, "");
        List<ResolvedResource> resolvedResources = resolveCompactId(selector, compactId, response);
        if (response.getHttpStatus() == HttpStatus.OK) {
            // Log a warning if there's more than one resource
            if (resolvedResources.size() > 1) {
                logger.warn("Using selector '{}' for Compact ID '{}' returned #{} resources!",
                        selector, compactId, resolvedResources.size());
            }
            // Select the provider, note how we use the same selection method as in the case where there is no
            // 'provider code' supplied to force the Compact ID resolution to a particular provider. We can reuse it
            // because, even in the situation where, for some reason, we've got more than one provider when using a
            // provider code to resolve a Compact ID.
            ResolvedResource selectedResource = selectResource(compactId, resolvedResources, response);
            if (response.getHttpStatus() == HttpStatus.OK && selectedResource != null) {
                extractMetadata(selectedResource, response, selector, compactId);
            }
        }
        return response;
    }

    public ServiceResponseFetchMetadata getMetadataForRawRequest(String rawRequest) {
        ServiceResponseFetchMetadata response = createDefaultResponseFetchMetadata(HttpStatus.OK, "");
        logger.info("Getting metadata for RAW Request '{}'", rawRequest);
        List<ResolvedResource> resources = resolveRawRequest(rawRequest, response);
        if (response.getHttpStatus() == HttpStatus.OK) {
            extractMetadata(resources, response, rawRequest);
        }
        return response;
    }

    public ServiceResponseFetchMetadataForUrl getMetadataForUrl(ServiceRequestFetchMetadataForUrl request) {
        // TODO - Check API version?
        // TODO - This will need to be re-addressed, as it will still do in-line metadata extraction
        // TODO - Metadata collection for URLs is performed in-line, a different strategy could be useful in the future.
        String url = request.getPayload().getUrl();
        // Prepare default response
        ServiceResponseFetchMetadataForUrl response =
                createDefaultResponseFetchMetadataForUrl(HttpStatus.OK, "");
        // Extract the metadata
        try {
            response.getPayload().setMetadata(metadataFetcher.fetchMetadataFor(url));
            // Check if there's any metadata in the payload
            // TODO - I know, it's a quick hack, just to test it, I need to use an extraction strategy in the future, so
            // TODO - this way of getting metadata is scheduled to disappear
            if ((response.getPayload() == null) || (((List<Object>)response.getPayload().getMetadata()).isEmpty())) {
                response.setHttpStatus(HttpStatus.NOT_FOUND);
                response.setErrorMessage(String.format("No metadata found for URL '%s'", url));
            }
        } catch (MetadataFetcherException e) {
            response.setErrorMessage(String.format("FAILED to fetch metadata for URL '%s', " +
                            "because '%s'",
                    url,
                    e.getMessage()));
            // TODO I need to refine the error reporting here to correctly flag errors as client or server side
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }
        // return the response
        return response;
    }
}
