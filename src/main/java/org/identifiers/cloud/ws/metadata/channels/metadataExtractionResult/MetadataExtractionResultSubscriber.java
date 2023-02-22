package org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.identifiers.cloud.ws.metadata.channels.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.channels.metadataExtractionResult
 * Timestamp: 2018-09-17 11:01
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataExtractionResultSubscriber extends Subscriber<String, MetadataExtractionResult> {
    @Autowired
    private RedisMessageListenerContainer redisContainer;

    @Autowired
    private ChannelTopic channelTopicMetadataExtractionResult;

    @Autowired
    private RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate;

    @PostConstruct
    public void registerSubscriber() {
        doRegisterSubscriber();
    }

    @Override
    protected RedisMessageListenerContainer getRedisContainer() {
        return redisContainer;
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
