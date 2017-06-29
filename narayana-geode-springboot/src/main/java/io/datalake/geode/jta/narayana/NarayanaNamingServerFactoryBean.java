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

import com.arjuna.ats.jta.utils.JNDIManager;
import org.jnp.server.NamingServer;
import org.jnp.server.SingletonNamingServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
public class NarayanaNamingServerFactoryBean implements FactoryBean<NamingServer>,
        InitializingBean, DisposableBean {

    private SingletonNamingServer singletonNamingServer;

    @Override
    public void destroy() throws Exception {
        singletonNamingServer.destroy();
    }

    @Override
    public NamingServer getObject() throws Exception {
        return singletonNamingServer;
    }

    @Override
    public Class<?> getObjectType() {
        return (singletonNamingServer != null ? singletonNamingServer.getClass() : NamingServer.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        singletonNamingServer = new SingletonNamingServer();
        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();
    }
}
