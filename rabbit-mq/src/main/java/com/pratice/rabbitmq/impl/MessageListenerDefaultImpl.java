package com.pratice.rabbitmq.impl;

import com.pratice.rabbitmq.*;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageListenerDefaultImpl extends AbstractAdaptableMessageListener {

    private MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();

    private JsonioMessageConverter oldMessageConverter = new JsonioMessageConverter();

    private String encoding = "UTF-8";
    /**
     * 保存queue和消费者的集合
     */
    private Map<String, MessageConsumer<?>> processMap = new ConcurrentHashMap<>();


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties props = message.getMessageProperties();
        String queue = props.getConsumerQueue();
        log.info("rabbitmq-consumer-message:{}", props);
        MessageConsumer cousumer = null;
        if (queue != null) {
            cousumer = processMap.get(queue);
        }
        if (cousumer == null) {
            log.error("consumer not found:" + queue);
        }

        Action action = Action.Reject;
        long deliveryTag = props.getDeliveryTag();
        try {
            if (cousumer != null) {
                Object event = this.extractMessage(message, cousumer);
                log.info("rabbitmq-consumer-message:{},{},{}", (props != null ? props.getMessageId() : ""),
                        queue, event);
                if (event != null) {
                    if (cousumer instanceof AbstractResultConsumer) {
                        ((AbstractResultConsumer) cousumer).handle(props.getMessageId(), event);;
                    } else if (cousumer instanceof AbstractRpcConsumer) {
                        Object reply = ((AbstractRpcConsumer) cousumer).handle(event);
                        Message replyMessage = this.getMessageConverter().toMessage(reply, props);
                        AMQP.BasicProperties convertedMessageProperties = messagePropertiesConverter
                                .fromMessageProperties(props, this.encoding);
                        channel.basicPublish("", props.getReplyTo(), convertedMessageProperties,
                                replyMessage.getBody());
                    }
                    action = cousumer.process(event);
                }else {
                    // 解析失败或者类似不匹配直接忽略消息
                    action = Action.CommitMessage;
                }
            }
        } catch (Exception e) {
            // 根据异常种类决定是否重新入队,目前全部拒绝
            log.error("consumer fail!", e);
            action = Action.Reject;
        } finally {
            // 通过finally块来保证Ack/Nack会且只会执行一次
            if (action == Action.CommitMessage) {
                channel.basicAck(deliveryTag, false);
            } else if (action == Action.ReconsumeLater) {
                channel.basicNack(deliveryTag, false, true);
            } else {
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

    /**
     * 保存队列与各消费者映射关系
     *
     * @param queue
     * @param messageCousumer
     */
    public void put(String queue, MessageConsumer<?> messageCousumer) {
        if (processMap.containsKey(queue)) {
            throw new AmqpException("this queue is already consume:" + queue);
        }
        processMap.put(queue, messageCousumer);
    }

    /**
     * 删除队列与各消费者映射关系
     *
     * @param queue
     * @return
     */
    public MessageConsumer<?> remove(String queue) {
        return processMap.remove(queue);
    }

    /**
     * 获取consumer实际的泛型类，注意lambda表达式为获取的类型为null
     *
     * @param consumer
     * @return
     */
    protected Class<?> getActualClass(MessageConsumer<?> consumer) {
        for (Type type : consumer.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (MessageConsumer.class.equals(pType.getRawType())) {
                    return (Class<?>) pType.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

    /**
     * 从mq header属性列表中获取headerName
     *
     * @param properties 属性列表
     * @param headerName
     * @return
     */
    protected String retrieveHeaderAsString(MessageProperties properties, String headerName) {
        Map<String, Object> headers = properties.getHeaders();
        Object classIdFieldNameValue = headers.get(headerName);
        String classId = null;
        if (classIdFieldNameValue != null) {
            classId = classIdFieldNameValue.toString();
        }
        return classId;
    }

    protected Object extractMessage(Message message, MessageConsumer<?> cousumer)
            throws UnsupportedEncodingException, Exception {
        // 快速返回，针对原生Message的情况
        Class<?> actualClass = getActualClass(cousumer);
        if (Message.class.equals(actualClass)) {
            return message;
        }
        MessageProperties props = message.getMessageProperties();
        String classidFieldName = this.retrieveHeaderAsString(props,
                AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME);
        if (StringUtils.isEmpty(classidFieldName)) {
            Object ob = oldMessageConverter.fromMessage(message);
            if(ob != null && actualClass.isInstance(ob)) {
                // 类型一致才返回
                return ob;
            }
            return null;
        } else {
            // 新的序列化协议
            return super.extractMessage(message);
        }
    }
}
