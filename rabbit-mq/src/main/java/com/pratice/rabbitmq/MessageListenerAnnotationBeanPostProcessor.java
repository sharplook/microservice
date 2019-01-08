package com.pratice.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MessageListenerAnnotationBeanPostProcessor
        implements BeanPostProcessor, BeanFactoryAware, Ordered {

    private BeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName)
            throws BeansException {
        if (!beanFactory.containsBean(beanName) || beanFactory.isPrototype(beanName)) {
            // 非单例对象排除
            return bean;
        }
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        // 处理类上带注解的
        {
            MessageListener messageListener = targetClass.getAnnotation(MessageListener.class);
            if (messageListener != null && MessageConsumer.class.isAssignableFrom(targetClass)) {
                addConsumerListener(messageListener, (MessageConsumer<?>) bean);
            }
        }
        // 处理方法上带注解的
        for (Method method : ReflectionUtils.getAllDeclaredMethods(targetClass)) {
            // 方法参数及返回值要保持一致
            if (method.getReturnType() != Action.class) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            MessageListener messageListener = method.getAnnotation(MessageListener.class);
            if (messageListener != null) {
                ReflectionUtils.makeAccessible(method);
                MessageConsumer<?> messageConsumer = (Object message) -> {
                    try {
                        return (Action) method.invoke(bean, message);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        log.error("message consumer error", e);
                        return Action.Reject;
                    }
                };
                addConsumerListener(messageListener, messageConsumer);
            }
        }
        return bean;
    }

    /**
     * 注册listener
     *
     * @param messageListener
     * @param bean
     */
    private void addConsumerListener(MessageListener messageListener,
                                     MessageConsumer<?> messageConsumer) {
        if (messageListener.queues() == null || messageListener.queues().length == 0) {
            log.error("the queues of cousumer is empty");
            return;
        }
        MqAccessBuilder mqAccessBuilder = null;
        if (StringUtils.isEmpty(messageListener.mqAccessBuilder())) {
            mqAccessBuilder = beanFactory.getBean(MqAccessBuilder.class);
        } else {
            mqAccessBuilder =
                    beanFactory.getBean(messageListener.mqAccessBuilder(), MqAccessBuilder.class);
        }

        String exchange = messageListener.exchange();
        if (!StringUtils.isEmpty(exchange)) {
            exchange = resolve(exchange);
        }
        for (String queue : messageListener.queues()) {
            queue = resolve(queue);
            String routingKey = "";
            try {
                mqAccessBuilder.addConsumerListener(exchange, routingKey, queue,
                        messageConsumer);
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
