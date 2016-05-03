package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.Ticket;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * CasServer集群环境下使用Redis缓存Ticket
 * Created by fengjing on 2016/4/27.
 */

public class TicketRedisTemplate extends RedisTemplate<String, Ticket> {

    public TicketRedisTemplate() {
        RedisSerializer<String> string = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        setKeySerializer(string);
        setValueSerializer(jdk);
        setHashKeySerializer(string);
        setHashValueSerializer(jdk);
    }

    public TicketRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }
}