package org.identifiers.cloud.ws.metadata.api.models;

import org.identifiers.cloud.libapi.models.resolver.Recommendation;
import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.cloud.libapi.models.resolver.ResponseResolvePayload;
import org.identifiers.cloud.libapi.models.resolver.ServiceResponseResolve;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.identifiers.cloud.ws.metadata.TestRedisServer;
import org.identifiers.cloud.ws.metadata.api.requests.RequestFetchMetadataForUrlPayload;
import org.identifiers.cloud.ws.metadata.api.requests.ServiceRequestFetchMetadataForUrl;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadata;
import org.identifiers.cloud.ws.metadata.api.responses.ServiceResponseFetchMetadataForUrl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = { TestRedisServer.class })
class MetadataApiModelTest {
    public static final String PAGE_WITH_METADATA_URL = "http://localhost:8082/page_with_metadata.html";
    @Autowired
    MetadataApiModel model;

    @MockBean
    ResolverService resolverService;

    @Test
    void testGetMetadataFor() {
        ServiceResponseResolve response = getValidResolverResponse();
        Mockito.when(resolverService.requestCompactIdResolution(Mockito.anyString())).thenReturn(response);

        ServiceResponseFetchMetadata res = model.getMetadataFor("compact_id");
        assertTrue(res.getHttpStatus().is2xxSuccessful());
    }

    @Test
    void testGetMetadataForRawRequest() {
        ServiceResponseResolve response = getValidResolverResponse();
        Mockito.when(resolverService.requestResolutionRawRequest(Mockito.anyString())).thenReturn(response);

        ServiceResponseFetchMetadata res = model.getMetadataForRawRequest("raw request");
        assertTrue(res.getHttpStatus().is2xxSuccessful());
    }

    @Test
    void testGetMetadataForUrl() {
        ServiceRequestFetchMetadataForUrl request = new ServiceRequestFetchMetadataForUrl();
        request.setApiVersion("1.0");
        RequestFetchMetadataForUrlPayload payload = new RequestFetchMetadataForUrlPayload();
        payload.setUrl(PAGE_WITH_METADATA_URL);
        request.setPayload(payload);

        ServiceResponseFetchMetadataForUrl res = model.getMetadataForUrl(request);
        assertTrue(res.getHttpStatus().is2xxSuccessful() || res.getHttpStatus() == HttpStatus.NOT_FOUND);
    }


    ServiceResponseResolve getValidResolverResponse() {
        ResolvedResource resolvedResource = new ResolvedResource()
                .setRecommendation(new Recommendation().setRecommendationIndex(100))
                .setCompactIdentifierResolvedUrl(PAGE_WITH_METADATA_URL);
        ResponseResolvePayload payload = new ResponseResolvePayload()
                .setResolvedResources(Collections.singletonList(resolvedResource));
        ServiceResponseResolve response = new ServiceResponseResolve();
        response.setApiVersion("1.0");
        response.setPayload(payload);
        return response;
    }
}