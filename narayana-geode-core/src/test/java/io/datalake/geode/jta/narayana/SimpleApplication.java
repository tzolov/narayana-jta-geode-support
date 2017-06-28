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
import org.jnp.server.NamingBeanImpl;

import javax.transaction.UserTransaction;

import static org.apache.geode.cache.DataPolicy.PARTITION;

/**
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
public class SimpleApplication {

    private static NamingBeanImpl jndiServer = new NamingBeanImpl();

    static {
        try {
            jndiServer.start();
            // Bind JTA implementation with default names
            JNDIManager.bindJTAImplementation();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {

        ServerLauncher serverLauncher = new ServerLauncher.Builder()
                .set("log-level", "info")
                .set("jmx-manager", "false")
                .set("jmx-manager-start", "false")
                .setWorkingDirectory("target")
                .setMemberName("server1")
                .setSpringXmlLocation("src/test/resources/cache.xml")
                .setServerPort(40406)
                .build();

        serverLauncher.start();

        Cache cache = new CacheFactory().create();

        Region<String, Object> region = cache.<String, Object>createRegionFactory()
                .setDataPolicy(PARTITION)
                .create("testRegion");

        UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

        System.out.println("1 = " + TXManagerImpl.getCurrentTXState());

        ut.begin();

       // Thread.sleep(5000);
        System.out.println("2 = " + TXManagerImpl.getCurrentTXState());

        //NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();

        System.out.println("3 = " + TXManagerImpl.getCurrentTXState());


        region.put("666", 666);

        System.out.println("4 = " + TXManagerImpl.getCurrentTXState());

        ut.commit();

        System.out.println("5 = " + TXManagerImpl.getCurrentTXState());

        serverLauncher.stop();
        jndiServer.stop();
    }
}
