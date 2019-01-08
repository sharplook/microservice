package com.pratice.rabbitmq;

public abstract class AbstractRpcConsumer<T> implements MessageConsumer<T>{

    @Override
    public Action process(T message) {
        return Action.CommitMessage;
    }

    /**
     * 回复消息业务逻辑实现类
     *
     * @param message 要发送的消息
     * @return
     */
    public abstract Object handle(T message);

}
