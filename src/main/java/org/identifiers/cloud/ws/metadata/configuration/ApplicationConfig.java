package org.identifiers.cloud.ws.metadata.configuration;

import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.RedisList;

import java.util.concurrent.BlockingDeque;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.configuration
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

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost,
                redisPort);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    // Redis Serialization templates
    // Generic
    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
    // Metadata Extraction Request
    @Bean
    public RedisTemplate<String, MetadataExtractionRequest> metadataExtractionRequestRedisTemplate() {
        RedisTemplate<String, MetadataExtractionRequest> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }

    // Metadata Extraction Result
    @Bean
    public RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate() {
        RedisTemplate<String, MetadataExtractionResult> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }

    // Queues
    @Bean
    public BlockingDeque<MetadataExtractionRequest> metadataExtractionRequestQueue() {
        RedisList<MetadataExtractionRequest> linkCheckRequests = new DefaultRedisList<>(queueKeyMetadataExtractionRequest,
                metadataExtractionRequestRedisTemplate());
        return linkCheckRequests;
    }

    // Publisher - Subscriber
    // Containers
    @Bean
    public RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.start();
        return container;
    }
    // Channels
    @Bean public ChannelTopic channelTopicMetadataExtractionResult() {
        return new ChannelTopic(channelKeyMetadataExtractionResult);
    }
}
