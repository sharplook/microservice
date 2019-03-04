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

/*

@Slf4j
@Intercepts({@Signature(type=StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Conditional(MybatisCondition.class)
@Component
public class PrivacyPrepareIntercept implements Interceptor {

    @Autowired
    private PrivacySpringManager privacySpringManager;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object realTarget = realTarget(invocation.getTarget());
        MetaObject metaStatementHandler = SystemMetaObject.forObject(realTarget);
        Connection connection = (Connection)invocation.getArgs()[0];
        BoundSql boundSql = (BoundSql)metaStatementHandler.getValue("delegate.boundSql");
        String database = ConnectUtils.getDatabaseNameByUrl(connection.getMetaData().getURL());
        PrivacyElementManager privacyElementManager = privacySpringManager.processPrepare(database, boundSql.getSql());
        if (isNeedIntercept(privacyElementManager)) {
            try {
                intercept(metaStatementHandler, privacyElementManager);
                log.debug("welab privacy intercept prepare sql: {}", privacyElementManager.getSql());
            }
            catch (Exception e) {
                log.warn("welab privacy intercept error prepare sql: {}", boundSql.getSql(), e);
                throw e;
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private void intercept(MetaObject metaStatementHandler, PrivacyElementManager privacyElementManager) {
        // 拦截sql编译
        if (!privacyElementManager.getElements().isEmpty()) {
            interceptPrepare(metaStatementHandler, privacyElementManager);
        }
        else if(null != privacyElementManager.getSql()){
            metaStatementHandler.setValue("delegate.boundSql.sql", privacyElementManager.getSql());
        }
    }

    private void interceptPrepare(MetaObject metaStatementHandler, PrivacyElementManager privacyElementManager) {
        Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
        BoundSql boundSql = (BoundSql)metaStatementHandler.getValue("delegate.boundSql");
        String sql = privacyElementManager.getSql();
        List<ParameterMapping> oldParameterMappings = boundSql.getParameterMappings();

        // 初始化 parameterMappings
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        parameterMappings.addAll(oldParameterMappings);

        // 组装 parameterMappings
        for (PrivacyElement privacyElement : privacyElementManager.getElements()) {
            int mappingIndex = privacyElement.getIndex() - 1;
            if (PrivacyColumn.ADD_COLUMN == privacyElement.getType()) {
                String additionalPropertyName = PrivacyElement.getJavaName("privacy_mybatis_mapping_" + mappingIndex);
                ParameterMapping mapping = new ParameterMapping.Builder(configuration, additionalPropertyName, String.class).build();
                parameterMappings.add(mappingIndex, mapping);
            }
            else {
                ParameterMapping oldMapping = parameterMappings.get(mappingIndex);
                ParameterMapping mapping = new ParameterMapping.Builder(configuration, oldMapping.getProperty(), String.class).build();
                parameterMappings.set(mappingIndex, mapping);
            }
        }
        MetaObject metaObjectBoundSql = SystemMetaObject.forObject(boundSql);
        PrivacyBoundSql newBoundSql = new PrivacyBoundSql(configuration, sql, parameterMappings, boundSql.getParameterObject());
        Map<String, Object> oldAdditionalParameters = (Map<String, Object>)metaObjectBoundSql.getValue("additionalParameters");
        Map<String, Object> additionalParameters = new HashMap<>();
        for (String key : oldAdditionalParameters.keySet()) {
            additionalParameters.put(key, oldAdditionalParameters.get(key));
        }

        for (PrivacyElement privacyElement : privacyElementManager.getElements()) {
            ParameterMapping parameterMapping = parameterMappings.get(privacyElement.getIndex() - 1);
            String property = parameterMapping.getProperty();

            Object originValue = getParameterValue(configuration, boundSql, parameterMappings.get(privacyElement.getOriginIndex() - 1));
            Object value = privacyElement.encrypt(originValue);
            additionalParameters.put(property, value);
        }

        MetaObject metaObjectNewBoundSql = SystemMetaObject.forObject(newBoundSql);
        for (String key : additionalParameters.keySet()) {
            newBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
        }
        metaObjectNewBoundSql.setValue("additionalParameters", additionalParameters);

        // 重新赋值mybaits参数
        setupStatementParams(metaStatementHandler, newBoundSql);
    }

    private boolean isNeedIntercept(PrivacyElementManager privacyElementManager) {
        return null != privacyElementManager && null != privacyElementManager.getSql();
    }

    private void setupStatementParams(MetaObject metaStatementHandler, BoundSql boundSql) {
        MetaObject metaParameterHandler = SystemMetaObject.forObject(metaStatementHandler.getValue("parameterHandler"));
        metaParameterHandler.setValue("boundSql", boundSql);

        metaStatementHandler.setValue("delegate.boundSql.sql", boundSql.getSql());
        metaStatementHandler.setValue("delegate.boundSql.parameterMappings", boundSql.getParameterMappings());
        metaStatementHandler.setValue("delegate.boundSql.parameterObject", boundSql.getParameterObject());
        MetaObject metaNewBoundSql = SystemMetaObject.forObject(boundSql);
        metaStatementHandler.setValue("delegate.boundSql.additionalParameters", metaNewBoundSql.getValue("additionalParameters"));
        metaStatementHandler.setValue("delegate.boundSql.metaParameters", metaNewBoundSql.getValue("metaParameters"));
    }

    private void setupStatementResutSet(MetaObject metaStatementHandler, List<ResultMap> resultMaps) {
        MappedStatement mappedStatement = (MappedStatement)metaStatementHandler.getValue("delegate.mappedStatement");
        if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
            metaStatementHandler.setValue("delegate.mappedStatement.resultMaps", resultMaps);
        }
    }

    private Object getParameterValue(Configuration configuration, BoundSql boundSql, ParameterMapping parameterMapping) {
        Object value = null;
        if (parameterMapping.getMode() != ParameterMode.OUT) {
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (boundSql.getParameterObject() == null) {
                value = null;
            } else if (configuration.getTypeHandlerRegistry().hasTypeHandler(boundSql.getParameterObject().getClass())) {
                value = boundSql.getParameterObject();
            } else {
                MetaObject metaObject = configuration.newMetaObject(boundSql.getParameterObject());
                value = metaObject.getValue(propertyName);
            }
        }
        return value;
    }

    private static Object realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        } else {
            return target;
        }
    }
}*/
