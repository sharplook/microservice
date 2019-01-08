package com.pratice.rabbitmq;

/***
 * 消息消费者
 */
public interface MessageConsumer<T> {


    Action process(T message);
}

