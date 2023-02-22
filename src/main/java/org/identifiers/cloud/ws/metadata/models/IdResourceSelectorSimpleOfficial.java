package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-12 11:21
 * ---
 */
@Component
@Profile("disabled")
public class IdResourceSelectorSimpleOfficial implements IdResourceSelector {
    @Override
    public ResolvedResource selectResource(List<ResolvedResource> resources) throws IdResourceSelectorException {
        List<ResolvedResource> selected = resources
                .parallelStream()
                .filter(resolverApiResponseResource -> resolverApiResponseResource.isOfficial())
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            throw new IdResourceSelectorException("NO ID RESOURCE could be selected for mining metadata from (select official resource selector)");
        }
        if (selected.size() > 1) {
            throw new IdResourceSelectorException("THERE IS MORE THAN ONE official resource for the given list of ID resources");
        }
        return selected.get(0);
    }
}
