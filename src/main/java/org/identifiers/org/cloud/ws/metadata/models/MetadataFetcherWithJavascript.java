package org.identifiers.org.cloud.ws.metadata.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.models
 * Timestamp: 2018-02-12 14:31
 * ---
 */
@Component
@Scope("prototype")
public class MetadataFetcherWithJavascript implements MetadataFetcher {
    private Logger logger = LoggerFactory.getLogger(MetadataFetcherWithJavascript.class);

    @Override
    public String fetchMetadataFor(String url) throws MetadataFetcherException {
        // Fetch the URL content
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setUseInsecureSSL(true);
        HtmlPage page = null;
        try {
            page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(10000);
        } catch (IOException e) {
            throw new MetadataFetcherException(String.format("METADATA FETCH ERROR for URL '%s', there was a problem while fetching its content", url));
        }
        logger.debug("Retrieved content from URL '{}', titled '{}'", url, page.getTitleText());
        // Look for JSON-LD
        String jsonldSelector = "script[type='application/ld+json']";
        DomNodeList<DomNode> jsonldDomNodes = page.querySelectorAll(jsonldSelector);
        if (jsonldDomNodes.size() > 1) {
            String errorMessage = String.format("MULTIPLE JSON-LD entries found in the header of URL '%s', entries: %s", url, jsonldDomNodes.toString());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        if (jsonldDomNodes.isEmpty()) {
            String errorMessage = String.format("JSON-LD formatted METADATA NOT FOUND for URL '%s', content \n'%s'", url, page.getHead().toString());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
        // Check on used contexts
        String metadata = jsonldDomNodes.get(0).getFirstChild().getTextContent();
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
