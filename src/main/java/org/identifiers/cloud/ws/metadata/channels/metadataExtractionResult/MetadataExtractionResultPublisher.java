package org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.channels.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult
 * Timestamp: 2018-09-17 10:57
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataExtractionResultPublisher extends Publisher<String, MetadataExtractionResult> {
    @Autowired
    private RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate;
    @Autowired
    private ChannelTopic channelTopicMetadataExtractionResult;

    @Override
    protected ChannelTopic getChannelTopic() {
        return channelTopicMetadataExtractionResult;
    }

    @Override
    protected RedisTemplate<String, MetadataExtractionResult> getRedisTemplate() {
        return metadataExtractionResultRedisTemplate;
    }
}
