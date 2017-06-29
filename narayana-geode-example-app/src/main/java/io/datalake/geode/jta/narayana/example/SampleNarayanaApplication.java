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

package io.datalake.geode.jta.narayana.example;

import io.datalake.geode.jta.narayana.NarayanaNamingServerFactoryBean;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.distributed.ServerLauncher;
import org.jnp.server.NamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.TransactionManager;

import static org.apache.geode.cache.DataPolicy.PARTITION;

/**
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
@SpringBootApplication
//@NarayanaLastResourceCommitOptimization
@EnableTransactionManagement(order = 1)
public class SampleNarayanaApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SampleNarayanaApplication.class);

    // Starts standalone JNDI server used by Gemfire to lookup global transactions.
    // Gemfire uses JNDI java:/TransactionManager name to lookup the JTA transaction manager.
    // The narayanaNamingServer also pre-bind all narayana transaction managers.
    @Bean(name = "NarayanaNamingServer")
    @ConditionalOnMissingBean(NamingServer.class)
//    @ConditionalOnBean(TransactionManager.class)
    public NarayanaNamingServerFactoryBean narayanaNamingServer(TransactionManager tm) {
        System.out.println(tm.getClass().getName());
        return new NarayanaNamingServerFactoryBean();
    }

    @Autowired
    private AccountService transactionalAccountService;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SampleNarayanaApplication.class, args).close();
    }

    @Override
    public void run(String... strings) throws Exception {

        ServerLauncher serverLauncher = new ServerLauncher.Builder()
                .set("log-level", "info")
                .set("jmx-manager", "false")
                .set("jmx-manager-start", "true")
                .setMemberName("server1")
                .setServerPort(40406)
                .build();

        serverLauncher.start();

        Cache cache = new CacheFactory().create();

        Region<String, Account> region = cache.<String, Account>createRegionFactory()
                .setDataPolicy(PARTITION)
                .create("testRegion");

        transactionalAccountService.createAccountAndNotify("tzolov", region);

        LOG.info("JPA entry count is " + transactionalAccountService.jpaEntryCount());

        try {
            // Using username "error" will cause transactionalAccountService to throw SampleRuntimeException
            transactionalAccountService.createAccountAndNotify("error", region);
        } catch (SampleRuntimeException ex) {
            // Log message to let test case know that exception was thrown
            LOG.error(ex.getMessage());
        }
        LOG.info("JAP entry count is still " + transactionalAccountService.jpaEntryCount());

        serverLauncher.stop();
    }
}
