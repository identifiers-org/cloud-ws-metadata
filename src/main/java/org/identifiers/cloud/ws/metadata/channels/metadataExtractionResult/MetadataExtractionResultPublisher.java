package org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.channels.Publisher;
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
    private final RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate;
    private final ChannelTopic channelTopicMetadataExtractionResult;
    public MetadataExtractionResultPublisher(
            RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate,
            ChannelTopic channelTopicMetadataExtractionResult) {
        this.metadataExtractionResultRedisTemplate = metadataExtractionResultRedisTemplate;
        this.channelTopicMetadataExtractionResult = channelTopicMetadataExtractionResult;
    }

    @Override
    protected ChannelTopic getChannelTopic() {
        return channelTopicMetadataExtractionResult;
    }

    @Override
    protected RedisTemplate<String, MetadataExtractionResult> getRedisTemplate() {
        return metadataExtractionResultRedisTemplate;
    }
}
