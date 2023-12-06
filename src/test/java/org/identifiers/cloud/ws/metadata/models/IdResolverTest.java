package org.identifiers.cloud.ws.metadata.models;

import org.identifiers.cloud.libapi.models.resolver.ResolvedResource;
import org.identifiers.cloud.libapi.models.resolver.ResponseResolvePayload;
import org.identifiers.cloud.libapi.models.resolver.ServiceResponseResolve;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-09 13:42
 * ---
 */
@SpringBootTest()
class IdResolverTest {
    @Autowired
    IdResolver idResolver;

    @MockBean
    ResolverService resolverService;

    @Test
    void testValidId() {
        ResponseResolvePayload payload = new ResponseResolvePayload()
                .setResolvedResources(Collections.singletonList(new ResolvedResource()));
        ServiceResponseResolve response = (ServiceResponseResolve) new ServiceResponseResolve()
                .setApiVersion("1.0")
                .setPayload(payload);
        Mockito.when(resolverService.requestCompactIdResolution(Mockito.anyString()))
                                    .thenReturn(response);
        assertFalse(idResolver.resolve("valid_id").isEmpty());
    }

    @Test
    void testInvalidId() {
        ResponseResolvePayload payload = new ResponseResolvePayload()
                .setResolvedResources(Collections.emptyList());
        ServiceResponseResolve response = (ServiceResponseResolve) new ServiceResponseResolve()
                .setApiVersion("1.0")
                .setPayload(payload);
        Mockito.when(resolverService.requestCompactIdResolution(Mockito.anyString())).thenReturn(response);

        assertTrue(idResolver.resolve("invalid_id").isEmpty());
    }
}