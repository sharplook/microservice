package com.pratice.rabbitmq.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
public class PojoJsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String SEPARATOR = ":";
    public static final String TYPE_FIELD = "@type";

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 类似xpath获取json下面的字段值
     *
     * @param json
     * @param path
     * @return
     */
    public static String fetchValue(String json, String path) {
        JsonNode tempNode = null;
        try {
            JsonNode jsonNode = MAPPER.readTree(json);
            tempNode = jsonNode;
            String[] paths = path.split(SEPARATOR);
            for (String fieldName : paths) {
                if (tempNode.isArray()) {
                    tempNode = fetchValueFromArray(tempNode, fieldName);
                } else {
                    tempNode = fetchValueFromObject(tempNode, fieldName);
                }
            }
        } catch (Exception e) {
            log.warn("", e);
            return null;
        }
        if (tempNode != null) {
            String value = tempNode.asText();
            if (value == null || value.isEmpty()) {
                value = tempNode.toString();
            }
            return value;
        }
        return null;
    }

    private static JsonNode fetchValueFromObject(JsonNode jsonNode, String fieldName) {
        return jsonNode.get(fieldName);
    }

    private static JsonNode fetchValueFromArray(JsonNode jsonNode, String index) {
        return jsonNode.get(Integer.parseInt(index));
    }

    public static final Object parseObject(String text) throws Exception {
        String className = fetchValue(text, TYPE_FIELD);
        if (className == null) {
            return null;
        }
        Class<?> clazz = Class.forName(className);
        try {
            // 没有无参的构建方式直接返回null
            clazz.getConstructor();
        } catch (Exception e) {
            return null;
        }
        return removeType(MAPPER.readValue(text, clazz));
    }

    /**
     * 针对map反序列化时会生成@type属性，注意移除
     *
     * @param ob
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected static Object removeType(Object ob)
            throws IllegalArgumentException, IllegalAccessException {
        if (ob == null) {
            return ob;
        }
        Class<?> clazz = ob.getClass();
        // 采用递归会增加极大代码复杂度，需要解决循环引用问题，在此简化处理了
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isPrimitive()) {
                continue;
            }
            field.setAccessible(true);
            Object v = field.get(ob);
            if (v == null) {
                continue;
            }
            if (v instanceof Map<?, ?>) {
                ((Map<?, ?>) v).remove(TYPE_FIELD);
            }
        }
        return ob;
    }

}
