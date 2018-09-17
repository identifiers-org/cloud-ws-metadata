package org.identifiers.org.cloud.ws.metadata.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.configuration
 * Timestamp: 2018-02-07 11:46
 * ---
 */
@Configuration
public class ApplicationConfig {
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;
    @Value("${org.identifiers.cloud.ws.metadata.backend.data.queue.key.metadataextractionrequest}")
    private String queueKeyMetadataExtractionRequest;
    @Value("${org.identifiers.cloud.ws.metadata.backend.data.channel.key.metadataextractionresult}")
    private String channelKeyMetadataExtractionResult;
}
