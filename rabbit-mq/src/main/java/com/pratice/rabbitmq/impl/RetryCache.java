package com.pratice.rabbitmq.impl;

import com.pratice.rabbitmq.CommunicationMode;
import com.pratice.rabbitmq.ConfirmBack;
import com.pratice.rabbitmq.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RetryCache implements RabbitTemplate.ConfirmCallback {

    /**
     * 消息列表
     */
    private Map<String, MessageBean<?>> messageMap = new ConcurrentHashMap<>();
    /**
     * ack缓存列表，超时临界极端情况该map有些键值没清除，仅多占点内存
     */
    private Map<String, Boolean> ackMap = new ConcurrentHashMap<>();
    /**
     * 锁列表，用于控制消息同步返回
     */
    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();
    private AtomicLong id = new AtomicLong();

    @Data
    @NoArgsConstructor
    private static class MessageBean<T> {
        long time;
        T message;
        CommunicationMode mode;
        ConfirmBack<T> confirmBack;
    }

    /**
     * 生成消息唯一id
     *
     * @return
     */
    private String generateId() {
        return "" + id.incrementAndGet();
    }

    public <T> String add(T message, CommunicationMode mode, ConfirmBack<T> confirmBack) {
        String messageId = generateId();
        MessageBean<T> bean = new MessageBean();
        bean.setTime(System.currentTimeMillis());
        bean.setMessage(message);
        bean.setMode(mode);
        bean.setConfirmBack(confirmBack);
        if (mode == CommunicationMode.SYNC) {
            // 同步发送模式，需创建CountDownLatch，用于异步转同步
            latchMap.put(messageId, new CountDownLatch(1));
        }
        messageMap.put(messageId, bean);
        return messageId;
    }

    /**
     * 该方法非幂等
     *
     * @param id
     * @return
     * @throws InterruptedException
     */
    public Boolean removeAndGet(String id) throws InterruptedException {
        try {
            latchMap.get(id).await(Constants.CONFIRM_VALID_TIME, TimeUnit.SECONDS);
        } finally {
            latchMap.remove(id);
        }
        return ackMap.remove(id);
    }

    /**
     * borker确认收到消息后会触发该方法
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("receive confirm message {},{}",
                correlationData != null ? correlationData.getId() : null, ack);
        if (correlationData == null || StringUtils.isEmpty(correlationData.getId())) {
            log.error("correlation messageId is empty");
            return;
        }
        String messageId = correlationData.getId();
        MessageBean messageBean = messageMap.remove(messageId);
        if (messageBean == null) {
            log.error("message not found,messageId:{}", messageId);
            return;
        }
        if (messageBean.getMode() == CommunicationMode.SYNC) {
            // 同步返回ack，且未超时才有必要处理
            CountDownLatch latch = latchMap.get(messageId);
            if (latch != null) {
                ackMap.put(messageId, ack);
                latch.countDown();
            }
        } else if (messageBean.confirmBack != null) {
            // 异步回调
            messageBean.confirmBack.confirm(messageBean.getMessage(), ack);
        }
    }

    @Override
    public String toString() {
        return "RetryCache [messageMap=" + messageMap.keySet() + ", ackMap=" + ackMap.keySet()
                + ", latchMap=" + latchMap.keySet() + "]";
    }

}