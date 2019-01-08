package com.pratice.rabbitmq;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Data
public class DataResult {
    /**
     * ack缓存列表，超时临界极端情况该map有些键值没清除，仅多占点内存
     */
    private Map<String, Object> resutMap = new ConcurrentHashMap<>();
    /**
     * 锁列表，用于控制消息同步返回
     */
    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();

    public Object get(String messageId) throws InterruptedException {
        try {
            latchMap.get(messageId).await(Constants.RPC_VALID_TIME, TimeUnit.SECONDS);
        } finally {
            latchMap.remove(messageId);
        }
        return resutMap.remove(messageId);
    }

    public void put(String messageId) {
        latchMap.put(messageId, new CountDownLatch(1));
    }
}
