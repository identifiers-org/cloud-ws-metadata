package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-03-01 1:41
 * ---
 */
@Component
public class IdResourceSelectorBasedOnRecommendation implements IdResourceSelector {
    @Override
    public ResolvedResource selectResource(List<ResolvedResource> resources) throws IdResourceSelectorException {
        resources.sort(
                Comparator
                        .comparing(resolverApiResponseResource ->
                                resolverApiResponseResource
                                        .getRecommendation()
                                        .getRecommendationIndex()));
        return resources.get(resources.size() - 1);
    }
}
