package org.identifiers.cloud.ws.metadata.configuration;

import org.identifiers.cloud.libapi.services.ApiServicesFactory;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionRequest;
import org.identifiers.cloud.ws.metadata.data.models.MetadataExtractionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.support.collections.DefaultRedisList;

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
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}") String redisHost,
            @Value("${spring.redis.port}") int redisPort
    ) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    // Redis Serialization templates
    // Generic
    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    // Metadata Extraction Request
    @Bean
    public RedisTemplate<String, MetadataExtractionRequest> metadataExtractionRequestRedisTemplate(
            RedisConnectionFactory redisConnectionFactory
    ) {
        RedisTemplate<String, MetadataExtractionRequest> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // Metadata Extraction Result
    @Bean
    public RedisTemplate<String, MetadataExtractionResult> metadataExtractionResultRedisTemplate(
            RedisConnectionFactory redisConnectionFactory
    ) {
        RedisTemplate<String, MetadataExtractionResult> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // Queues
    @Bean
    public BlockingDeque<MetadataExtractionRequest> metadataExtractionRequestQueue(
            @Value("${org.identifiers.cloud.ws.metadata.backend.data.queue.key.metadataextractionrequest}")
            String queueKeyMetadataExtractionRequest,
            RedisTemplate<String, MetadataExtractionRequest> metadataExtractionRequestRedisTemplate
    ) {
        return new DefaultRedisList<>(
                queueKeyMetadataExtractionRequest,
                metadataExtractionRequestRedisTemplate);
    }

    ////// Publisher - Subscriber
    // Containers
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.start();
        return container;
    }
    // Channels
    @Bean
    public ChannelTopic channelTopicMetadataExtractionResult(
            @Value("${org.identifiers.cloud.ws.metadata.backend.data.channel.key.metadataextractionresult}")
            String channelKeyMetadataExtractionResult
    ) {
        return new ChannelTopic(channelKeyMetadataExtractionResult);
    }

    @Bean
    public ResolverService resolverService(
            @Value("${org.identifiers.cloud.ws.metadata.resolver.host}") String wsResolverHost,
            @Value("${org.identifiers.cloud.ws.metadata.resolver.port}") String wsResolverPort
    ) {
        return ApiServicesFactory.getResolverService(wsResolverHost, wsResolverPort);
    }
}
