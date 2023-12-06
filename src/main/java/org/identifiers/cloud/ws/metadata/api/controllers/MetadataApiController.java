package org.identifiers.cloud.ws.metadata.api.controllers;

import org.identifiers.cloud.ws.metadata.api.models.MetadataApiModel;
import org.identifiers.cloud.ws.metadata.api.requests.ServiceRequestFetchMetadataForUrl;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadata;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadataForUrl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.controllers
 * Timestamp: 2018-02-07 11:29
 * ---
 */
@RestController
public class MetadataApiController {

    private final MetadataApiModel model;
    public MetadataApiController(MetadataApiModel model) {
        this.model = model;
    }

    // Now this is always a RAW request
    @GetMapping(value = "/{identifierRequest}/**")
    public @ResponseBody
    ResponseEntity<?> getMetadataFor(@PathVariable String identifierRequest, HttpServletRequest request) {
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        ServiceResponseFetchMetadata response = model.getMetadataForRawRequest(path.replaceFirst("/", ""));
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping(value = "/getMetadataForUrl")
    public ResponseEntity<?> getMetadataForUrl(@RequestBody ServiceRequestFetchMetadataForUrl request) {
        ServiceResponseFetchMetadataForUrl response = model.getMetadataForUrl(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
