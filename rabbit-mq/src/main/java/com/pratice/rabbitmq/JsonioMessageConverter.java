package com.pratice.rabbitmq;

import com.cedarsoftware.util.io.JsonIoException;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class JsonioMessageConverter extends AbstractJsonMessageConverter {

    /**
     * welab event 定义为persistent
     *
     * @param object
     * @param messageProperties
     * @return
     */
    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] bytes;
        try {
            String jsonString = JsonWriter.objectToJson(object);
            bytes = jsonString.getBytes(getDefaultCharset());
        } catch (IOException e) {
            throw new MessageConversionException("Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(getDefaultCharset());
        if (bytes != null) {
            messageProperties.setContentLength(bytes.length);
        }
        return new Message(bytes, messageProperties);

    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.contains("json")) {
                String encoding = properties.getContentEncoding();
                if (encoding == null) {
                    encoding = getDefaultCharset();
                }
                try {
                    String json = new String(message.getBody(), encoding);
                    content = JsonReader.jsonToJava(json);
                } catch (IOException | JsonIoException | IllegalArgumentException e) {
                    log.error("Failed to convert Message[{}], error:",
                            new String(message.getBody(), Charset.defaultCharset()), e);
                }
            } else {
                log.warn("Could not convert incoming message [{}] with content-type [{}]",
                        new String(message.getBody(), Charset.defaultCharset()), contentType);
            }
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }
}
