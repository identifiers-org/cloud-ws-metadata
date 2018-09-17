package org.identifiers.org.cloud.ws.metadata.channels.metadataExtractionResult;

import org.identifiers.org.cloud.ws.metadata.channels.Publisher;
import org.identifiers.org.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

/**
 * Project: metadata
 * Package: org.identifiers.org.cloud.ws.metadata.channels.metadataExtractionResult
 * Timestamp: 2018-09-17 10:57
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class MetadataExtractionResultPublisher extends Publisher<String, MetadataExtractionResult> {
    @Autowired
    private RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate;
    @Autowired
    private ChannelTopic channelTopicMetadataExtractionResult;
}
