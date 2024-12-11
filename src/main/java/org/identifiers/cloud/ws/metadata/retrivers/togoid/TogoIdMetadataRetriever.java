package org.identifiers.cloud.ws.metadata.retrivers.togoid;

import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.identifiers.cloud.libapi.models.resolver.ParsedCompactIdentifier;
import org.identifiers.cloud.ws.metadata.retrivers.SparqlBasedMetadataRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat.JSON;

@Component
public class TogoIdMetadataRetriever extends SparqlBasedMetadataRetriever {
    final Resource relatedTogoIdQueryFile;
    final Set<String> namespaceBlacklist;
    public TogoIdMetadataRetriever(@Value("${org.identifiers.cloud.ws.metadata.retrievers.togoid.sparqlendpoint}")
                                   String sparqlEndpoint,
                                   @Value("${org.identifiers.cloud.ws.metadata.retrievers.togoid.namespaceblacklist}")
                                   String[] namespaceBlacklist,
                                   ResourceLoader resourceLoader) {
        super(sparqlEndpoint);
        this.namespaceBlacklist = Set.of(namespaceBlacklist);
        this.relatedTogoIdQueryFile = resourceLoader.getResource("classpath:togoIdRelatedIdentifiers.sparql");
    }

    @Override
    public boolean isEnabled(ParsedCompactIdentifier compactIdentifier) {
        return !namespaceBlacklist.contains(compactIdentifier.getNamespace());
    }

    /**
     * Gets metadata by querying the togo ID sparql endpoint for
     *   related URIs and the label of their relationship.
     *   Answer is a JSON string in the SPARQL JSON response format.
     * @param compactIdentifier to get metadata on
     * @return SPARQL result as JSON string.
     */
    @Override
    public Object getRawMetaData(ParsedCompactIdentifier compactIdentifier) {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var resultHandler = QueryResultIO.createTupleWriter(JSON, byteArrayOutputStream);
        this.runTupleQuery(relatedTogoIdQueryFile, resultHandler,
                "curie", compactIdentifier.getRawRequest());
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    /**
     * Gets metadata by querying the togoid sparql endpoint for
     *   related URIs and the label of their relationship.
     * Then creates a map where entries are [ URI -> relationship label ] paris
     * @param compactIdentifier to get metadata on
     * @return Map relatedUri -> RelationLabel
     */
    @Override
    public Map<String, String> getParsedMetaData(ParsedCompactIdentifier compactIdentifier) {
        var result = new HashMap<String, String>();
        var queryResult = this.runTupleQuery(relatedTogoIdQueryFile,
                "curie", compactIdentifier.getRawRequest());
        if (queryResult == null) return Collections.emptyMap();

        queryResult.forEach(bs -> {
            String label = bs.getValue("label").stringValue();
            String relatedUri = bs.getValue("related").stringValue();

            result.put(relatedUri, label);
        });

        return result;
    }
}
