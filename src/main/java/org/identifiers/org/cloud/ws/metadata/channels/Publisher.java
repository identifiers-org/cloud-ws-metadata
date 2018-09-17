package org.identifiers.org.cloud.ws.metadata.channels;

/**
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.channels
 * Timestamp: 2018-09-17 10:49
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface Listener<V> {
    void process(V value);
}