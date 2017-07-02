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


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link NarayanaLrcoAspect} is a Spring {@link Aspect}, that gets activated when the process
 * enters a running transaction.
 * <p>
 * When a Spring {@link org.springframework.stereotype.Service @Service component} class or method is annotated with a
 * {@link Transactional @Transactional annotation} then the {@link NarayanaLrcoAspect} enlists Geode as a
 * {@link com.arjuna.ats.jta.resources.LastResourceCommitOptimisation LastResourceCommitOptimisation} - non-XAResource
 * within the current Narayana {@link javax.transaction.Transaction Transaction}.
 *
 * You can Enable or Disable the LRCO behavior using the spring.jta.narayana.onePhaseCommit property. Later defaults to true.
 *
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
@Aspect
public class NarayanaLrcoAspect implements Ordered {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int order;

    @Value("${spring.jta.narayana.onePhaseCommit:false}")
    private boolean jtaNarayanaOnePhaseCommitEnabled;

    @Pointcut("@within(org.springframework.transaction.annotation.Transactional)")
    protected void atTransactionalType() {}

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    protected void atTransactionalMethod() {}

    /* (non-Javadoc) */
    @Before("atTransactionalType() || atTransactionalMethod()")
    public void doEnableGeodeNarayanaLastResourceCommitOptimization() {

        if (jtaNarayanaOnePhaseCommitEnabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Enlisting Geode as Last Resource Commit Optimization.");
            }
            NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();
        } else {
            logger.warn("The Last Resource Commit Optimization is Disabled!. " +
                    "To enable it set spring.jta.narayana.onePhaseCommit=true.");
        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
