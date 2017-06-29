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
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.distributed.ServerLauncher;
import org.apache.geode.internal.cache.TXManagerImpl;
import org.jnp.server.SingletonNamingServer;

import javax.transaction.UserTransaction;

import static org.apache.geode.cache.DataPolicy.PARTITION;

/**
 * Example uses the plain Geode API to create Global JTA transaction, enlisting Geode as Last Resource Commit.
 * <p>
 * 1. Use the OOTB standalone JNDI server: SingletonNamingServer.
 * 2. Use plain Geode API to start Server and create Region.
 * 3. Use the Narayana API to start and commit JTA transaction.
 * 4. Use NarayanaGeodeSupport.enlistGeodeAsLastCommitResource() to enlist Geode as LRCO.
 *
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
public class SimpleApplication {

    private static int printLineCounter = 1;

    // 1. Create standalone JNDI server
    private static SingletonNamingServer jndiServer;

    public static void main(String[] args) throws Exception {

        // 1. Bootstrap a standalone JNDI server
        jndiServer = new SingletonNamingServer();

        // 1.1. Bind JTA TM implementations with default names. Concerning Geode, this bind will register the
        // Narayana Transaction Manager under name "java:/TransactionManager"
        JNDIManager.bindJTAImplementation();

        // 2 Create standalone Geode server
        ServerLauncher serverLauncher = new ServerLauncher.Builder()
                .set("log-level", "info")
                .set("jmx-manager", "false")
                .set("jmx-manager-start", "false")
                .setWorkingDirectory("target")
                .setMemberName("server1")
                //.setSpringXmlLocation("src/test/resources/cache.xml")
                .setServerPort(40406)
                .build();

        // 2.1 Start Geode server
        serverLauncher.start();

        // 2.2 Create Geode Cache
        Cache cache = new CacheFactory().create();

        // 2.3 Create Geode region
        Region<String, Object> region = cache.<String, Object>createRegionFactory()
                .setDataPolicy(PARTITION)
                .create("testRegion");

        // 3. Start Narayana JTA transaction
        UserTransaction jta = com.arjuna.ats.jta.UserTransaction.userTransaction();
        jta.begin();

        geodeTxState("Before Enlisting");

        // 4. Enlist Geode as Last Resource Commit Resource
        NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();

        geodeTxState("After Enlisting");

        // 5. Perform Geode operations
        region.put("666", 666);

        geodeTxState("After PUT");

        // 6. Commit the Narayana JTA transaction
        jta.commit();

        geodeTxState("After JTA Commit");

        // 7. Stop the Geode server
        serverLauncher.stop();

        // 8. Stop the JNDI server
        jndiServer.destroy();
    }

    private static void geodeTxState(String description) {
        System.out.printf("%s. TXState %s: %s \n", (printLineCounter++), description, TXManagerImpl.getCurrentTXState());
    }
}
