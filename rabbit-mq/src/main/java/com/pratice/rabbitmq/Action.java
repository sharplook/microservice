package com.pratice.rabbitmq;

public enum Action {
    /**
     * 消费成功
     */
    CommitMessage,
    /**
     * 消费是失败,告知broker稍后重试,继续消费其他消息
     */
    ReconsumeLater,
    /**
     * 无需重试的错误,拒绝
     */
    Reject,

}
