package org.identifiers.org.cloud.ws.metadata.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 11:35
 * ---
 *
 * This may be the main Resolver API response object, let's see how it evolves over time, as the Resolver WS is being
 * build
 */
// TODO - REFACTOR OUT THIS ENTITY TO BE REUSED BY CLIENTS AS WELL
@JsonIgnoreProperties(value = {"httpStatus"}, ignoreUnknown = true)
public class ResolverApiResponse implements Serializable {
    private String errorMessage;
    private HttpStatus httpStatus = HttpStatus.OK;
    private List<ResolverApiResponseResource> resolvedResources = new ArrayList<>();

    public String getErrorMessage() {
        return errorMessage;
    }

    public ResolverApiResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ResolverApiResponse setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public List<ResolverApiResponseResource> getResolvedResources() {
        return resolvedResources;
    }

    public ResolverApiResponse setResolvedResources(List<ResolverApiResponseResource> resolvedResources) {
        this.resolvedResources = resolvedResources;
        return this;
    }
}
