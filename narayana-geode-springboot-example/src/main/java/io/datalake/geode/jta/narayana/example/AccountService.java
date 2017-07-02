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

import org.apache.geode.cache.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
@Service
public class AccountService {

    private final JmsTemplate jmsTemplate;

    private final AccountRepository jpaRepository;

    @Autowired
    public AccountService(JmsTemplate jmsTemplate, AccountRepository accountRepository) {
        this.jmsTemplate = jmsTemplate;
        this.jpaRepository = accountRepository;
    }

    @Transactional
    public void createAccountAndNotify(String username, Region<String, Account> region) {

        this.jmsTemplate.convertAndSend("accounts", username);

        Account account = new Account(username);

        this.jpaRepository.save(account);

        region.put(username, account);

        if ("error".equals(username)) {
            throw new SampleRuntimeException("Simulated error");
        }
    }

    public long jpaEntryCount() {
        return this.jpaRepository.count();
    }
}
