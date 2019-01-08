package com.pratice.rabbitmq;

public class Constants {
    /**
     * MQ CONFIRM消息有效时间(秒)
     */
    public static final int CONFIRM_VALID_TIME = 5;
    /**
     * MQ RPC消息有效时间(秒)
     */
    public static final int RPC_VALID_TIME = 60;

    /**
     * 消息发送次数
     */
    public static final int MESSAGE_CONSUME_MAX_TIMES = 3;
    /**
     * 死信交换器
     */
    public static final String DEAD_LETTER_EXCHANGE_NAME = "welab.dead.letter.exchange";
    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE_NAME = "welab.dead.letter.queue";

}
