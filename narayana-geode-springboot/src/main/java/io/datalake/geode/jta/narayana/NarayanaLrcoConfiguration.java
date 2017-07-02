/*
 * Copyright (c) 2017. The original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.datalake.geode.jta.narayana;

import org.jnp.server.NamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;
import java.util.Optional;

/**
 * The {@link NarayanaLrcoConfiguration} is a Spring {@link Configuration @Configuration}
 * annotated class used to configure the Geode's "Last Resource Commit Optimization" {@link org.aspectj.lang.annotation.Aspect Aspects}.
 * <p>
 * Note: The {@link org.springframework.core.annotation.Order} management implementation here is copied
 * from John Blum's SDG LR work.
 *
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
@Configuration
@SuppressWarnings("unused")
public class NarayanaLrcoConfiguration implements ImportAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Integer enableTransactionManagementOrder;

    /* (non-Javadoc) */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableTransactionManagementOrder = resolveEnableTransactionManagementOrder(importMetadata);
    }

    /* (non-Javadoc) */
    protected int resolveEnableTransactionManagementOrder(AnnotationMetadata importMetadata) {

        AnnotationAttributes enableTransactionManagementAttributes =
                resolveEnableTransactionManagementAttributes(importMetadata);

        Integer order = enableTransactionManagementAttributes.getNumber("order");

        return Optional.ofNullable(order)
                .filter(it -> !(it == Integer.MAX_VALUE || it == Integer.MIN_VALUE))
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "The @%1$s(order) attribute value [%2$s] must be explicitly set to a value"
                                + " other than Integer.MAX_VALUE or Integer.MIN_VALUE",
                        EnableTransactionManagement.class.getSimpleName(), String.valueOf(order))));
    }

    /* (non-Javadoc) */
    protected AnnotationAttributes resolveEnableTransactionManagementAttributes(
            AnnotationMetadata importMetadata) {

        Map<String, Object> enableTransactionManagementAttributes =
                importMetadata.getAnnotationAttributes(EnableTransactionManagement.class.getName());

        return Optional.ofNullable(enableTransactionManagementAttributes)
                .map(AnnotationAttributes::fromMap)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "The @%1$s annotation may only be used on a Spring application @%2$s class"
                                + " that is also annotated with @%3$s with an explicit [order] set",
                        EnableGeodeNarayanaJta.class.getSimpleName(), Configuration.class.getSimpleName(),
                        EnableTransactionManagement.class.getSimpleName())));
    }

    /* (non-Javadoc) */
    protected Integer getEnableTransactionManagementOrder() {

        return Optional.ofNullable(this.enableTransactionManagementOrder)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "The @%1$s(order) attribute [%2$s] was not properly specified; Also, please make your Spring application"
                                + " @%3$s annotated class is annotated with both @%4$s and @%1$s",
                        EnableTransactionManagement.class.getSimpleName(), String.valueOf(this.enableTransactionManagementOrder),
                        Configuration.class.getSimpleName(), EnableGeodeNarayanaJta.class.getSimpleName())));
    }

    /* (non-Javadoc) */
    @Bean
    public NarayanaLrcoAspect geodeLastResourceCommitAspect() {

        NarayanaLrcoAspect geodeLastResourceCommitAspect = new NarayanaLrcoAspect();

        // Ensure that NarayanaLrcoAspect has lower precedence (e.g. will be executed after) the @Transaction.
        int order = (getEnableTransactionManagementOrder() + 1);

        geodeLastResourceCommitAspect.setOrder(order);

        return geodeLastResourceCommitAspect;
    }

    @Bean
    public GeodeDependsOnBeanFactoryPostProcessor gemfireDependsOnBeanFactoryPostProcessor() {
        return new GeodeDependsOnBeanFactoryPostProcessor().add("NarayanaNamingServer");
    }

    // Starts standalone JNDI server used by Gemfire to lookup global transactions.
    // Gemfire uses JNDI java:/TransactionManager name to lookup the JTA transaction manager.
    // The narayanaNamingServer also pre-bind all narayana transaction managers.
    @Bean(name = "NarayanaNamingServer")
    @ConditionalOnMissingBean(NamingServer.class)
    public NarayanaNamingServerFactoryBean narayanaNamingServer() {
        logger.info("Start standalone JNDI and bind narayana TM");
        return new NarayanaNamingServerFactoryBean();
    }
}
