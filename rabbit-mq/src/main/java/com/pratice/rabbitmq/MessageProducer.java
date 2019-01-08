package com.pratice.rabbitmq;

/***
 * 消息生产者
 */
public interface MessageProducer {
    /**
     * 同步发送消息，只要不抛异常就表示成功
     *
     * @param message 要发送的消息
     * @return
     */
    void send(Object message);

    /**
     * 同步发送消息，只要不抛异常就表示成功
     *
     * @param routeKey
     * @param message
     */
    void send(String routeKey, Object message);

    /**
     * 发送消息，Oneway形式，服务器不应答，无法保证消息是否成功到达服务器
     *
     * @param message
     */
    void sendOneway(Object message);

    /**
     * 发送消息，异步Callback形式
     *
     * @param message 要发送的消息
     * @param confirmBack 发送完成要执行的回调函数
     */
    <T> void sendAsync(T message, final ConfirmBack<T> confirmBack);

    /**
     * 使用消息实现rpc调用
     *
     * @param message 要发送的消息
     * @return
     */
    Object sendAndReceive(Object message);

    /**
     * 使用消息实现rpc调用
     *
     * @param routeKey
     * @param message 要发送的消息
     * @return
     */
    Object sendAndReceive(String routeKey, Object message);

    /**
     * 使用消息实现rpc调用
     * @param routeKey 要发送的消息
     * @param message
     * @param messageId
     * @param builder
     * @param receiveTopic 返回topic
     * @return
     */
    Object sendAndReceive(String routeKey, Object message, String messageId,
                          MqAccessBuilder builder, String receiveTopic);


    /**
     * 同步发送消息，只要不抛异常就表示成功
     *
     * @param exchange
     * @param message 要发送的消息
     * @param messageId
     * @return
     */
    void sendWithMessageId(String exchange, Object message, String messageId);


}
