package org.identifiers.cloud.ws.metadata.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-09 13:42
 * ---
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class IdResolverTest {

    @Autowired
    private IdResolver idResolver;

    @Test
    public void testValidId() {
        String compactId = "CHEBI:36927";
        // Maybe I should not check that it comes back non-empty...
        assertThat(String.format("Valid Compact ID '%s' is resolved", compactId),
                idResolver.resolve(compactId).isEmpty(),
                is(false));
    }

    @Test(expected = IdResolverException.class)
    public void testInvalidId() {
        String compactId = "nowaythisprefixexists:36927";
        idResolver.resolve(compactId);
    }
}