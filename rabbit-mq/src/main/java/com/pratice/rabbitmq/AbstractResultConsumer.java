package com.pratice.rabbitmq;

import lombok.AllArgsConstructor;

import java.util.concurrent.CountDownLatch;

@AllArgsConstructor
public class AbstractResultConsumer<T> implements MessageConsumer<T> {

    private DataResult dataResult;

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
    public void handle(String messageId, T message) {
        CountDownLatch countDownLatch = dataResult.getLatchMap().get(messageId);
        if (countDownLatch != null) {
            dataResult.getResutMap().put(messageId, message);
            countDownLatch.countDown();
        }
    }

}

