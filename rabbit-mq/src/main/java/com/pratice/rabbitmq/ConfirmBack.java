package com.pratice.rabbitmq;

/***
 * 生产者信道confirm模式, broker确认收到消息异步回调confirm方法
 * channel 的confirm模式的回调方法
 */
public interface ConfirmBack<T> {

    /***
     *
     * @param message
     * @param ack
     */
    void confirm(T message,boolean ack);

}
