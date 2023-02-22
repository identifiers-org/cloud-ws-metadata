package org.identifiers.cloud.ws.metadata.data.repositories;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.springframework.data.repository.CrudRepository;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.data.repositories
 * Timestamp: 2018-09-16 19:39
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface MetadataExtractionResultRepository extends CrudRepository<MetadataExtractionResult, String> {
    MetadataExtractionResult findByAccessUrl(String accessUrl);
}
