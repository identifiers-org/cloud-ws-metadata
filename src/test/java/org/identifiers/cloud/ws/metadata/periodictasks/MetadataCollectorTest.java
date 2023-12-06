package org.identifiers.cloud.ws.metadata.periodictasks;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MetadataCollectorTest {
    @Autowired
    MetadataCollector metadataCollector;

    @Test
    void testAttendMetadataExtractionRequestWithValidUrl() {
        MetadataExtractionRequest request = new MetadataExtractionRequest();
        request.setAccessUrl("http://localhost:8082/page_with_metadata.html");
        request.setResolutionPath("resolution path");
        request.setTimestamp(Timestamp.from(Instant.now()));
        request.setResourceId("resource id");
        MetadataExtractionResult res = metadataCollector.attendMetadataExtractionRequest(request);
        assertTrue(HttpStatus.valueOf(res.getHttpStatus()).is2xxSuccessful());
    }
}