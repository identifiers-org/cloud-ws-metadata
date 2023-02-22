package org.identifiers.cloud.ws.metadata.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-09 16:31
 * ---
 */
@Component
@Scope("prototype")
@Profile("disabled")
public class MetadataFetcherSimple implements MetadataFetcher {
    // TODO - Is it worth updating this one to use the new error codes in the exception?
    private static Logger logger = LoggerFactory.getLogger(MetadataFetcherSimple.class);

    @Override
    public String fetchMetadataFor(String url) throws MetadataFetcherException {
        // Fetch URL content
        Document document = null;
        // TODO   - First problem - Many pages produce the metadata content via Javascript
        // (SOLVED) Second problem - When a URL is HTTPS, very often the certificate is considered not valid
        try {
            document = Jsoup.connect(url).validateTLSCertificates(false).get();
        } catch (IOException e) {
            throw new MetadataFetcherException(String.format("METADATA FETCH ERROR for URL '%s', there was a problem while fetching its content", url));
        }
        logger.debug("Retrieved content from URL '{}', titled '{}'", url, document.title());
        // Look for JSON-LD
        String jsonldSelector = "script[type='application/ld+json']";
        Elements jsonldElements = document.head().select(jsonldSelector);
        if (jsonldElements.size() > 1) {
            String errorMessage = String.format("MULTIPLE JSON-LD entries found in the header of URL '%s', entries: %s", url, jsonldElements.toString());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        if (jsonldElements.isEmpty()) {
            String errorMessage = String.format("JSON-LD formatted METADATA NOT FOUND for URL '%s', content \n'%s'", url, document.head().toString());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        // Check on schema.org context
        String metadata = jsonldElements.get(0).data();
        logger.debug("Trying to process Metadata content '{}'", metadata);
        JsonNode metadataRootNode = null;
        try {
            metadataRootNode = new ObjectMapper().readTree(metadata);
        } catch (IOException e) {
            String errorMessage = String.format("JSON-LD PROCESSING ERROR for URL '%s', " +
                    "METADATA being parsed '%s', " +
                    "ERROR '%s'", url, metadata, e.getMessage());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        List<JsonNode> contextParents = metadataRootNode.findParents("@context");
        if (contextParents.isEmpty()) {
            String errorMessage = String.format("JSON-LD PROCESSING ERROR for URL '%s', " +
                    "METADATA being parsed '%s', " +
                    "NO CONTEXT DEFINITION NODES FOUND! INVALID JSON+LD", url, metadata);
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        String contexts = String.join(",", contextParents.stream().map(jsonNode -> jsonNode.get("@context").asText()).collect(Collectors.toSet()));
        logger.info("SUCCESSFUL metadata extraction from URL '{}', METADATA '{}', found contexts '[{}]'", url, metadata, contexts);
        return metadata;
    }
}
