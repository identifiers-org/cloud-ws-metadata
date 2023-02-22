package org.identifiers.cloud.ws.metadata.models;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-02-08 11:54
 * ---
 */
public interface MetadataFetcher {

    // TODO - Create an implementation based on a Chromium web driver
    Object fetchMetadataFor(String url) throws MetadataFetcherException;
}
