package com.pratice.rabbitmq.impl;

import com.pratice.rabbitmq.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MessageProducerDefaultImpl implements MessageProducer {

    private RabbitTemplate rabbitTemplate;

    private RetryCache retryCache;

    @Override
    public void sendOneway(Object message) {
        String messageId = retryCache.add(message, CommunicationMode.ONEWAY, null);
        rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(messageId));
    }

    @Override
    public void send(Object message) {
        this.send("", message);
    }

    @Override
    public void send(String routingKey, Object message) {
        String messageId = retryCache.add(message, CommunicationMode.SYNC, null);
        if (StringUtils.isEmpty(routingKey)) {
            routingKey = rabbitTemplate.getRoutingKey();
        }
        log.info("rabbitmq-producer-message:{},{}", routingKey, message);
        rabbitTemplate.convertAndSend(routingKey, message, new CorrelationData(messageId));
        Boolean ack = false;
        try {
            ack = retryCache.removeAndGet(messageId);
        } catch (InterruptedException e) {
            log.warn("", e);
        }
        if (ack == null) {
            throw new RuntimeException("broker confirm timeout");
        } else if (!ack) {
            throw new RuntimeException("broker ack false");
        }
    }

    @Override
    public <T> void sendAsync(T message, ConfirmBack<T> confirmback) {
        String messageId = retryCache.add(message, CommunicationMode.ASYNC, confirmback);
        rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(messageId));
    }

    @Override
    public Object sendAndReceive(Object message) {
        log.info("rabbitmq-producer-message:{}", message);
        return rabbitTemplate.convertSendAndReceive(message);
    }

    @Override
    public Object sendAndReceive(String routeKey, Object message) {
        return rabbitTemplate.convertSendAndReceive(routeKey, message);
    }

    final Map<String, DataResult> receiveTopicMap = new HashMap<>();

    @Override
    public Object sendAndReceive(String routeKey, Object message, String messageId,
                                 MqAccessBuilder builder, String receiveTopic) {
        try {
            synchronized (receiveTopicMap) {
                DataResult dataResult = receiveTopicMap.get(receiveTopic);
                if (dataResult == null) {
                    receiveTopicMap.put(receiveTopic, new DataResult());
                    final DataResult tempMap = receiveTopicMap.get(receiveTopic);
                    builder.addFanoutListener(receiveTopic,
                            new AbstractResultConsumer<Object>(tempMap));
                }
            }
            DataResult dataResult = receiveTopicMap.get(receiveTopic);
            dataResult.put(messageId);
            this.send(routeKey, message);
            return dataResult.get(messageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendWithMessageId(String exchange, Object message, String messageId) {
        String correlationId = retryCache.add(message, CommunicationMode.SYNC, null);
        log.info("rabbitmq-producer-message:{},{},{}", exchange, messageId, message);
        rabbitTemplate.convertAndSend(exchange, "", message, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties prop = message.getMessageProperties();
                prop.setMessageId(messageId);
                return message;
            }

        }, new CorrelationData(correlationId));
        Boolean ack = false;
        try {
            ack = retryCache.removeAndGet(correlationId);
        } catch (InterruptedException e) {
            log.warn("", e);
        }
        if (ack == null) {
            throw new RuntimeException("broker confirm timeout");
        } else if (!ack) {
            throw new RuntimeException("broker ack false");
        }
    }


}
