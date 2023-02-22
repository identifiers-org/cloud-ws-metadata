package org.identifiers.cloud.ws.metadata.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-12 14:31
 * ---
 */
@Component
@Scope("prototype")
@Profile("disabled")
public class MetadataFetcherWithJavascript implements MetadataFetcher {
    private Logger logger = LoggerFactory.getLogger(MetadataFetcherWithJavascript.class);

    @Override
    public Object fetchMetadataFor(String url) throws MetadataFetcherException {
        // TODO - This is too slow, we need some kind of caching mechanism here
        // Fetch the URL content
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setUseInsecureSSL(true);
        HtmlPage page = null;
        try {
            page = webClient.getPage(url);
            // NOTE - Magic number here! OMG! Maybe change it in the future, if it makes sense
            webClient.waitForBackgroundJavaScript(10000);
        } catch (IOException e) {
            throw new MetadataFetcherException(String.format("METADATA FETCH ERROR for URL '%s', " +
                    "there was a problem while fetching its content", url),
                    MetadataFetcherException.ErrorCode.INTERNAL_ERROR);
        }
        logger.debug("Retrieved content from URL '{}', titled '{}'", url, page.getTitleText());
        logger.info("Webpage content for URL '{}', text content \n{}", url, page.getTextContent());
        logger.info("Webpage content for URL '{}', xml content \n{}", url, page.asXml());
        NodeList documentNodes = page.getOwnerDocument().getChildNodes();
        List<String> documentChildNodes = IntStream.range(0, documentNodes.getLength()).mapToObj(i -> String.format("\tChild node name '%s', value '%s'", documentNodes.item(i).getNodeName(), documentNodes.item(i).getNodeValue())).collect(Collectors.toList());
        logger.info("Parent document for URL '{}', xml content \n{}", url, String.join("\n", documentChildNodes));
        // Look for JSON-LD
        String jsonldSelector = "script[type='application/ld+json']";
        DomNodeList<DomNode> jsonldDomNodes = page.querySelectorAll(jsonldSelector);
        /*if (jsonldDomNodes.size() > 1) {
            String errorMessage = String.format("MULTIPLE JSON-LD entries found in the header of URL '%s', entries: %s", url, jsonldDomNodes.toString());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }*/
        if (jsonldDomNodes.isEmpty()) {
            String errorMessage = String.format("JSON-LD formatted METADATA NOT FOUND for URL '%s'", url);
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage, MetadataFetcherException.ErrorCode.METADATA_NOT_FOUND);
        }
        // Check on used contexts
        //String metadata = jsonldDomNodes.get(0).getFirstChild().getTextContent();
        /*String metadata = String.format("[%s]",
                String.join(",", jsonldDomNodes.stream()
                        .map(domNode -> domNode.getFirstChild().getTextContent()).collect(Collectors.toList())));*/
        ObjectMapper mapper = new ObjectMapper();
        List<Object> metadataObjects = jsonldDomNodes.stream().map(domNode -> {
            try {
                return mapper.readTree(domNode.getFirstChild().getTextContent());
            } catch (IOException e) {
                // TODO - Right now we ignore the errors, as it is not very clear how we're gonna deal with this kind
                // TODO - of data
            }
            return "ERROR IOException - Jackson JSON parser";
        }).collect(Collectors.toList());
        /*String metadata = "ERROR - Jackson mapper";
        try {
            metadata = mapper.writeValueAsString(metadataObjects);
        } catch (JsonProcessingException e) {
            // TODO
        }*/
        String metadata = "";
        try {
            metadata = mapper.writeValueAsString(metadataObjects);
        } catch (JsonProcessingException e) {
            // TODO - Not important right now as it is just for logging errors
        }
        logger.debug("Trying to process Metadata content '{}'", metadata);
        JsonNode metadataRootNode = null;
        metadataRootNode = new ObjectMapper().valueToTree(metadataObjects);
        List<JsonNode> contextParents = metadataRootNode.findParents("@context");
        if (contextParents.isEmpty()) {
            String errorMessage = String.format("JSON-LD PROCESSING ERROR for URL '%s', " +
                    "METADATA being parsed '%s', " +
                    "NO CONTEXT DEFINITION NODES FOUND! INVALID JSON+LD", url, metadata);
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage,
                    MetadataFetcherException.ErrorCode.METADATA_INVALID);
        }
        String contexts = String.join(",", contextParents.stream().map(jsonNode -> jsonNode.get("@context").asText()).collect(Collectors.toSet()));
        logger.info("SUCCESSFUL metadata extraction from URL '{}', METADATA '{}', found contexts '[{}]'",
                url, metadata, contexts);
        return metadataObjects;
    }
}
