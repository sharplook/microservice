package com.pratice.plulgins;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.util.Properties;

@Intercepts({@Signature(type = StatementHandler.class, method = "parameterize", args = java.sql.Statement.class) })
public class MyFirstPlugins implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("MyFirstPlugin... intercept before:" + invocation.getMethod());
        // 执行目标方法
        Object proceed = invocation.proceed();
        System.out.println("MyFirstPlugin... intercept after:" + invocation.getMethod());

        return proceed;
    }

    @Override
    public Object plugin(Object target) {
        System.out.println("MyFirstPlugin... plugin:mybatis将要包装的对象" + target);
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件配置的信息:" + properties);
    }
}
