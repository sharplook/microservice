package com.pratice.rabbitmq;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageListener {

    /**
     * exchange名称
     */
    String exchange() default "";

    /**
     * 队列列表
     *
     * @return
     */
    String[] queues() default {};

    /**
     * MqAccessBuilder实例id，针对多MqAccessBuilder场景才需要
     *
     * @return
     */
    String mqAccessBuilder() default "";

}