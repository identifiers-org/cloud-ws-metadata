package org.identifiers.cloud.ws.metadata.channels;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.channels
 * Timestamp: 2018-09-17 10:51
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public abstract class Publisher<K, V> {
    protected abstract ChannelTopic getChannelTopic();
    protected abstract RedisTemplate<K, V> getRedisTemplate();

    public void publish(V value) throws PublisherException {
        try {
            getRedisTemplate().convertAndSend(getChannelTopic().getTopic(), value);
        } catch (RuntimeException e) {
            throw new PublisherException(e.getMessage());
        }
    }
}
