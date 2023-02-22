package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;

import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-07 11:44
 * ---
 */
public interface IdResolver {
    // This is for you, intrepid developer, you may be wondering why I'm just reusing the Resolver model on providing
    // resources for a given Compact ID in an environment where I could have different strategies of collecting
    // resources for a given Compact ID. The developer's mind says there should be another model into which those
    // resources, from wherever they get collected, are translated into. The thing is, the resolver resource model does
    // the job perfectly, and this iteration of the metadata web service will not have another provider. Also, even in
    // the case of having another provider, the only burden in to the eyes, as this model will probably stay, and models
    // from other providers would be translated into this one... until a differentiation aspect comes up.
    List<ResolvedResource> resolve(String compactIdParameter) throws IdResolverException;

    List<ResolvedResource> resolve(String selector, String compactId) throws IdResolverException;

    List<ResolvedResource> resolveRawRequest(String rawRequest) throws IdResolverException;
}
