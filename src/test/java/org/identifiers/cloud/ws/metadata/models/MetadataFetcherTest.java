package org.identifiers.cloud.ws.metadata.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-12 13:28
 * ---
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MetadataFetcherTest {

    // TODO - Fix this unit test or kill it
    @Autowired
    private MetadataFetcher metadataFetcher;

    @Test
    public void testValidMetadata() {
        // NOTE - Possible future extension here to make it more exhaustive
        // This unit test is too dependant on that particular URL to be up, as soon as this is working, I may
        // choose to deactivate this test
        getUrlsWithValidMetadata().parallelStream().forEach(validUrl -> {
            Object metadata = metadataFetcher.fetchMetadataFor(validUrl);
            assertThat(String.format("URL '%s' contains VALID metadata", validUrl), metadata == null, is(false));
        });
    }

    private List<String> getUrlsWithValidMetadata() {
        return Arrays.asList("https://reactome.org/content/detail/R-HSA-177929",
                "https://www.omicsdi.org/dataset/arrayexpress-repository/E-GEOD-37196"
        );
    }
}