package org.identifiers.org.cloud.ws.metadata.data.services;

import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.org.cloud.ws.metadata.data.repositories.MetadataExtractionResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.data.services
 * Timestamp: 2018-09-16 20:33
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataExtractionResultService {
    @Autowired
    private MetadataExtractionResultRepository repository;

    public MetadataExtractionResult findByAccessUrl(String accessUrl) throws MetadataExtractionResultServiceException {
        try {
            return repository.findByAccessUrl(accessUrl);
        } catch (RuntimeException e) {
            throw new MetadataExtractionResultServiceException(String.format("Could not retrieve information on " +
                    "Access URL '%s' due to '%s'", accessUrl, e.getMessage()));
        }
    }

    public MetadataExtractionResult save(MetadataExtractionResult metadataExtractionResult) throws MetadataExtractionResultServiceException {
        try {
            repository.save(metadataExtractionResult);
        } catch (RuntimeException e) {
            throw new MetadataExtractionResultServiceException(String.format("Could not save metadata extraction " +
                    "result for Access URL '%s' due to '%s'", metadataExtractionResult.getAccessUrl(), e.getMessage()));
        }
        return metadataExtractionResult;
    }
}
