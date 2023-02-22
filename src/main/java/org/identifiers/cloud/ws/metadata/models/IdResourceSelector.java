package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;

import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-12 11:18
 * ---
 */
public interface IdResourceSelector {
    ResolvedResource selectResource(List<ResolvedResource> resources) throws IdResourceSelectorException;
}
