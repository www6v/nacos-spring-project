/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.spring.beans.factory.annotation;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource;
import com.alibaba.nacos.spring.util.NacosUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Properties;

import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosServiceFactoryBean;
import static com.alibaba.spring.util.ClassUtils.resolveGenericType;
import static java.lang.String.format;

/**
 * Abstract Nacos Service Bean Builder
 *
 * @param <S> Nacos Service type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.1.0
 */
public abstract class AbstractNacosServiceBeanBuilder<S> implements BeanFactoryAware, EnvironmentAware {

    private BeanFactory beanFactory;

    private Environment environment;

    private final Class<?> type;

    private final GlobalNacosPropertiesSource source;

    protected AbstractNacosServiceBeanBuilder(GlobalNacosPropertiesSource source) {
        type = resolveGenericType(getClass());
        this.source = source;
    }

    public S build(NacosInjected nacosInjected) throws NacosException {

        NacosServiceFactory nacosServiceFactory = getNacosServiceFactoryBean(beanFactory);
        Properties properties = resolveProperties(nacosInjected);

        if (properties.isEmpty()) {
            throw new BeanCreationException(
                    format("The globalProperties() attribute that must be configured from @%s , @%s or @%s",
                            NacosInjected.class.getSimpleName(),
                            EnableNacosConfig.class.getSimpleName(),
                            EnableNacos.class.getSimpleName()));
        }

        return createService(nacosServiceFactory, properties);
    }

    /**
     * Subtype would implement this method to create target Nacos Service
     *
     * @param nacosServiceFactory {@link NacosServiceFactory}
     * @param properties          {@link Properties}
     * @return target Nacos Service instance
     * @throws NacosException When Nacos Service creation is failed
     */
    protected abstract S createService(NacosServiceFactory nacosServiceFactory, Properties properties) throws NacosException;

    final Properties resolveProperties(NacosInjected nacosInjected) {
        NacosProperties nacosProperties = nacosInjected.properties();
        Properties globalNacosProperties = resolveGlobalNacosProperties();
        return NacosUtils.resolveProperties(nacosProperties, environment, globalNacosProperties);
    }

    private Properties resolveGlobalNacosProperties() {
        return source.getMergedGlobalProperties(beanFactory);
    }


    final Class<?> getType() {
        return type;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
