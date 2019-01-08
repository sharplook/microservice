package com.pratice.rabbitmq;

import com.pratice.rabbitmq.impl.MessageListenerDefaultImpl;
import com.pratice.rabbitmq.impl.MessageProducerDefaultImpl;
import com.pratice.rabbitmq.impl.RetryCache;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
public class MqAccessBuilder {

    private ConnectionFactory connectionFactory;

    @PreDestroy
    public void destory() {
        // 主要是服务关闭确认能收到close报文返回(reply-code=200, reply-text=OK)
        // 如果不写这段代码也不影响，只会在error输出流打印日志
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 消费消息时才需要设置
     */
    private SimpleMessageListenerContainer container;

    /**
     * 允许修改messageConverter
     */
    private MessageConverter messageConverter;

    protected MqAccessBuilder buildConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    protected MqAccessBuilder buildListenerContainer(SimpleMessageListenerContainer container) {
        this.container = container;
        return this;
    }

    /**
     * topic消息sender
     *
     * @return
     * @throws IOException
     */
    public MessageProducer buildTopicSender() throws IOException {
        return this.buildMessageSender(ExchangeTypes.TOPIC, null, "");
    }

    /**
     * 点对点消息sender
     *
     * @param queue 队列名称
     * @return
     * @throws IOException
     */
    public MessageProducer buildDirectSender(String queue) throws IOException {
        return this.buildMessageSender(ExchangeTypes.DIRECT, null, queue, queue);
    }

    /**
     * 广播消息sender
     *
     * @param exchange 交换器名称
     * @param queues 队列名称列表
     * @return
     * @throws IOException
     */
    public MessageProducer buildFanoutSender(String exchange, String... queues) throws IOException {
        return this.buildMessageSender(ExchangeTypes.FANOUT, exchange, "", queues);
    }

    /**
     * <ol>
     * <li>构造template, exchange, routingkey等</li>
     * <li>设置message序列化方法</li>
     * <li>设置发送确认</li>
     * </ol>
     *
     * @param type 交换器类型，目前支持DIRECT、TOPIC和FANOUT
     * @param exchange 为空表明默认队列
     * @param routingKey
     * @param queue
     * @return
     * @throws IOException
     */
    protected MessageProducer buildMessageSender(String type, String exchange, String routingKey,
                                               String... queue) throws IOException {
        // 进行各种参数检查
        buildDeclaration(type, exchange, routingKey, queue);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(routingKey);
        if (this.messageConverter == null) {
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        } else {
            rabbitTemplate.setMessageConverter(this.messageConverter);
        }

        RetryCache retryCache = new RetryCache();
        rabbitTemplate.setConfirmCallback(retryCache);
        rabbitTemplate.setRetryTemplate(getRetryTemplate());

        MessageProducer messageProducer = new MessageProducerDefaultImpl(rabbitTemplate, retryCache);
        return messageProducer;
    }

    /**
     * 设置默认重试策略
     *
     * @return
     */
    private RetryTemplate getRetryTemplate() {
        RetryTemplate r = new RetryTemplate();
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(500);
        policy.setMaxInterval(10000);
        policy.setMultiplier(10.0);
        r.setBackOffPolicy(policy);
        return r;
    }

    /**
     * 判断exchange是否存在
     *
     * @param amqpAdmin
     * @param exchange
     * @return
     */
    private boolean existExchange(RabbitAdmin amqpAdmin, String exchange) {
        return amqpAdmin.getRabbitTemplate().execute(new ChannelCallback<Boolean>() {
            @Override
            public Boolean doInRabbit(Channel channel) throws Exception {
                try {
                    com.rabbitmq.client.AMQP.Exchange.DeclareOk declareOk =
                            channel.exchangeDeclarePassive(exchange);
                    return declareOk != null;
                } catch (IOException e) {
                    return false;
                }
            }
        });
    }

    /**
     * 判断queue是否存在
     *
     * @param amqpAdmin
     * @param queue
     * @return
     */
    private boolean existQueue(RabbitAdmin amqpAdmin, String queue) {
        return amqpAdmin.getRabbitTemplate().execute(new ChannelCallback<Boolean>() {
            @Override
            public Boolean doInRabbit(Channel channel) throws Exception {
                try {
                    com.rabbitmq.client.AMQP.Queue.DeclareOk declareOk =
                            channel.queueDeclarePassive(queue);
                    return declareOk != null;
                } catch (IOException e) {
                    return false;
                }
            }
        });
    }

    /**
     * 创建exchange、queue及对应的binding关系
     *
     * @param type
     * @param exchange
     * @param routingKey
     * @param queues
     * @throws IOException
     */
    public void buildDeclaration(String type, String exchange, String routingKey,
                                 final String... queues) throws IOException {
        RabbitAdmin amqpAdmin = new RabbitAdmin(connectionFactory);

        // type和exchange不为空才需要创建
        if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(exchange)) {
            Exchange ex = null;
            switch (type) {
                case ExchangeTypes.DIRECT:
                    ex = new DirectExchange(exchange, true, false);
                    break;
                case ExchangeTypes.TOPIC:
                    ex = new TopicExchange(exchange, true, false);
                    break;
                case ExchangeTypes.FANOUT:
                    ex = new FanoutExchange(exchange, true, false);
                    break;
                default:
                    throw new IllegalArgumentException("exchange type not allowed:" + type);
            }
            if (!existExchange(amqpAdmin, exchange)) {
                amqpAdmin.declareExchange(ex);
            }
        }
        if (queues == null || queues.length == 0) {
            return;
        }
        declareDeadletterExchange(amqpAdmin);
        Map<String, Object> args = new HashMap<>(2);
        args.put("x-dead-letter-exchange", Constants.DEAD_LETTER_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", Constants.DEAD_LETTER_QUEUE_NAME);
        for (String queue : queues) {
            Queue q = new Queue(queue, true, false, false, args);
            if (existQueue(amqpAdmin, queue)) {
                continue;
            }
            amqpAdmin.declareQueue(q);
            // 首先exchange名称不能为空，如果为默认exchange则不需要创建
            if (!StringUtils.isEmpty(exchange)) {
                // 1、生产者会指定exchange类型，则需建立绑定关系
                // 2、消费者启动时若判断exchange存在，则需建立绑定关系
                if (!StringUtils.isEmpty(type) || existExchange(amqpAdmin, exchange)) {
                    Binding binding =
                            new Binding(queue, Binding.DestinationType.QUEUE, exchange, routingKey, null);
                    amqpAdmin.declareBinding(binding);
                }
            }
        }
    }

    /**
     * 点对点消息listener
     *
     * @param queue 队列名
     * @param messageProcess 消息处理类
     * @throws IOException
     */
    public void addQueueListener(String queue, MessageConsumer<?> messageProcess)
            throws IOException {
        this.addConsumerListener(null, queue, queue, messageProcess);
    }

    /**
     * 广播消息listener
     *
     * @param exchange 交换器名称
     * @param queue 队列名
     * @param messageProcess 消息处理类
     * @throws IOException
     */
    public void addFanoutListener(String exchange, String queue, MessageConsumer<?> messageProcess)
            throws IOException {
        this.addConsumerListener(exchange, "", queue, messageProcess);
    }

    public void addFanoutListener(String exchange, MessageConsumer<?> messageProcess)
            throws IOException {
        RabbitAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
        Exchange ex = new FanoutExchange(exchange, true, false);
        amqpAdmin.declareExchange(ex);
        Queue queue = amqpAdmin.declareQueue();
        Binding binding = new Binding(queue.getName(), Binding.DestinationType.QUEUE, exchange, "", null);
        amqpAdmin.declareBinding(binding);

        MessageListenerDefaultImpl messageListener =
                (MessageListenerDefaultImpl) container.getMessageListener();
        messageListener.put(queue.getName(), messageProcess);
        String[] srcQueueNames = container.getQueueNames();
        String[] destQueueNames = new String[srcQueueNames.length + 1];
        System.arraycopy(srcQueueNames, 0, destQueueNames, 0, srcQueueNames.length);
        destQueueNames[destQueueNames.length - 1] = queue.getName();
        container.setQueueNames(destQueueNames);
    }

    protected void addConsumerListener(String exchange, String routingKey, final String queue,
                                       final MessageConsumer<?> messageProcess) throws IOException {
        // 先用最简单的规则，如果有指定exchange名称用fanout
        if (StringUtils.isEmpty(exchange)) {
            buildDeclaration(null, exchange, routingKey, queue);
        } else {
            buildDeclaration(ExchangeTypes.FANOUT, exchange, routingKey, queue);
        }

        MessageListenerDefaultImpl messageListener =
                (MessageListenerDefaultImpl) container.getMessageListener();
        messageListener.put(queue, messageProcess);
        String[] srcQueueNames = container.getQueueNames();
        String[] destQueueNames = new String[srcQueueNames.length + 1];
        System.arraycopy(srcQueueNames, 0, destQueueNames, 0, srcQueueNames.length);
        destQueueNames[destQueueNames.length - 1] = queue;
        container.setQueueNames(destQueueNames);
    }

    /**
     * 创建死信exchange和死信queue
     *
     * @param amqpAdmin
     */
    private void declareDeadletterExchange(AmqpAdmin amqpAdmin) {
        Properties properties = amqpAdmin.getQueueProperties(Constants.DEAD_LETTER_QUEUE_NAME);
        if (properties != null) {
            // 说明已经有死信队列
            return;
        }
        Exchange deadletterExchange = new DirectExchange(Constants.DEAD_LETTER_EXCHANGE_NAME);
        amqpAdmin.declareExchange(deadletterExchange);
        Queue deadletterQueue = new Queue(Constants.DEAD_LETTER_QUEUE_NAME);
        amqpAdmin.declareQueue(deadletterQueue);
        Binding binding = new Binding(deadletterQueue.getName(), Binding.DestinationType.QUEUE,
                deadletterExchange.getName(), deadletterQueue.getName(), null);
        amqpAdmin.declareBinding(binding);
    }

}
