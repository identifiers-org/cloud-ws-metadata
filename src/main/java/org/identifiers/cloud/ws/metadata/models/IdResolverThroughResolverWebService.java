package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-08 12:02
 * ---
 */
@Component
@Scope("prototype")
public class IdResolverThroughResolverWebService implements IdResolver {
    private static final Logger logger = LoggerFactory.getLogger(IdResolverThroughResolverWebService.class);

    final ResolverService resolverService;

    public IdResolverThroughResolverWebService(@Autowired ResolverService resolverService) {
        this.resolverService = resolverService;
    }

    @Override
    public List<ResolvedResource> resolve(String compactIdParameter) throws IdResolverException {
        // The client will make sure that a default response is obtained in case any possible error
        return resolverService
                .requestCompactIdResolution(compactIdParameter)
                .getPayload()
                .getResolvedResources();
    }

    @Override
    public List<ResolvedResource> resolve(String selector, String compactId) throws IdResolverException {
        return resolverService
                .requestCompactIdResolution(compactId, selector)
                .getPayload()
                .getResolvedResources();

    }

    @Override
    public List<ResolvedResource> resolveRawRequest(String rawRequest) throws IdResolverException {
        return resolverService
                .requestResolutionRawRequest(rawRequest)
                .getPayload()
                .getResolvedResources();
    }
}
