package org.identifiers.cloud.ws.metadata.retrivers.togoid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identifiers.cloud.libapi.models.resolver.ParsedCompactIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// TODO mock SPARQL response
class TogoIdMetadataRetrieverTest {
    static final int QUERY_LIMIT = 10;

    TogoIdMetadataRetriever retriever = new TogoIdMetadataRetriever(
            "https://sparql.api.identifiers.org/sparql",
            new String[0],
            new DefaultResourceLoader()
    );

    final ParsedCompactIdentifier parsedCompactIdentifier =
            new ParsedCompactIdentifier()
            .setNamespace("uniprot")
            .setLocalId("P12345")
            .setRawRequest("uniprot:P12345");

    @Test
    void getRawMetadata() throws JsonProcessingException {
        if (retriever.getRawMetaData(parsedCompactIdentifier) instanceof String strResult) {
            var objectmapper = new ObjectMapper();
            var typeReference = new TypeReference<Map<String, Object>>() {};
            var result = objectmapper.readValue(strResult, typeReference);
            assertTrue(result.containsKey("head"));
            assertTrue(result.containsKey("results"));
        } else {
            fail();
        }
    }

    @Test
    void getParsedMetadata() {
        var parsedMetaData = retriever.getParsedMetaData(parsedCompactIdentifier);
        assertFalse(parsedMetaData.isEmpty());
        assertTrue(parsedMetaData.size() <= QUERY_LIMIT);
    }
}