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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.internal.cache.TXManagerImpl;
import com.gemstone.gemfire.internal.cache.TXStateProxy;
import org.jnp.server.SingletonNamingServer;
import org.junit.*;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Field;
import java.util.Map;

import static com.gemstone.gemfire.cache.DataPolicy.PARTITION;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
public class TestCase {

    private static SingletonNamingServer jndiServer;
    private ServerLauncher serverLauncher;
    private Region<String, Object> region;
    private TransactionManager transactionManager;

    @BeforeClass
    public static void beforeClass() throws Exception {
        jndiServer = new SingletonNamingServer();
        // Bind JTA implementation with default names
        JNDIManager.bindJTAImplementation();
    }

    @Before
    public void before() throws SystemException, NotSupportedException {

        serverLauncher = new ServerLauncher.Builder()
                .set("log-level", "error")
                .set("jmx-manager", "false")
                .set("jmx-manager-start", "true")
                .set("cache-xml-file", "src/test/resources/cache.xml")
                .setWorkingDirectory("target")
                .setMemberName("server1")
                .setServerPort(40406)
                .build();

        serverLauncher.start();

        Cache cache = new CacheFactory().create();

        region = cache.<String, Object>createRegionFactory()
                .setDataPolicy(PARTITION)
                .create("testRegion");

        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();
    }

    @After
    public void after() throws Exception {
        transactionManager.commit();
        serverLauncher.stop();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        jndiServer.destroy();
    }

    @Test
    public void lastResourceCommitOptimization() throws Exception {
        TransactionImple tx = (TransactionImple) transactionManager.getTransaction();

        assertThat("At this point NarayanaGeodeLastCommitResource MUST NOT be enlisted as LRCO Resource!",
                tx.getResources().size(), is(0));


        assertNull("For Non LRCO mode, the Geode's TXState must be set before the first PUT/GET operations",
                getCacheTransactionManagerCurrentTXState());

        assertNull(getCacheTransactionManagerCurrentTXState());

        // Enlist Geode as LRCO resource
        NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();

        assertThat("NarayanaGeodeLastCommitResource should be enlisted as LRCO!",
                tx.getResources().size(), is(1));

        // Internally Gemfire treats the LRCO mode as JCA use case.
        assertThat(getCacheTransactionManagerCurrentTXState().isJCATransaction(), is(true));

        // Geode transactional operation
        region.put("666", 666);

        AtomicAction atomicAction = extractTheAtomicAction(tx);

        assertThat("In LRCO mode, after the first Geode operation inside the transaction, Geode's TxManager" +
                        " should NOT register a Geode TXStateProxy (as Synchronization) in JTA's transaction!",
                atomicAction.getSynchronizations().size(), is(0));

        // Geode enlisted as LRCO should behave as JCA and not JTA sync
        assertThat(getCacheTransactionManagerCurrentTXState().isJCATransaction(), is(true));
        assertThat(isJTA(getCacheTransactionManagerCurrentTXState()), is(false));
    }

    @Test
    public void nonLastResourceCommitOptimization() throws Exception {

        Transaction tx = (Transaction) transactionManager.getTransaction();

        assertThat("NarayanaGeodeLastCommitResource MUST NOT be registered as transaction Resource!",
                tx.getResources().size(), is(0));

        assertNull("Geode TXStateProxy mustn't be created", TXManagerImpl.getCurrentTXState());

        assertNull("For Non LRCO mode, the Geode's TXState must be set before the first PUT/GET operations",
                getCacheTransactionManagerCurrentTXState());

        AtomicAction atomicAction = extractTheAtomicAction(tx);

        // For non-LRCO mode Geode will be registered as Synchronization Resource in the running transaction during
        // the first Geode operation in the JTA transaction. Check the LocalRegion#getJTAEnlistedTX()
        assertThat("In non-LRCO mode, Geode should not be registered as Transaction Synch. " +
                        "Registered before the first Geode operation",
                atomicAction.getSynchronizations().size(), is(0));


        // Perform Geode Operation inside the transaction.
        region.put("666", 666);

        Map<Uid, String> synchonizations = atomicAction.getSynchronizations();
        assertThat("In Non-LRCO mode, after the first Geode operation inside the transaction, Geode's TxManager" +
                        " should have registered a Geode TXStateProxy (as Synchronization) in JTA's transaction!"
                , synchonizations.size(), is(1));
        assertThat("The Geode Synchronization implementation (TXStateProxy) is missing from the " +
                        "registered JTA Synchronizations",
                synchonizations.values().iterator().next(), containsString("TXStateProxy"));

        // Geode NOT enlisted as LRCO should behave as JTA sync resource and NOT as JCA resource.
        assertThat(getCacheTransactionManagerCurrentTXState().isJCATransaction(), is(false));
        assertThat(isJTA(getCacheTransactionManagerCurrentTXState()), is(true));
    }

    public static AtomicAction extractTheAtomicAction(Object aaObject) throws Exception {
        Field f = aaObject.getClass().getDeclaredField("_theTransaction");
        f.setAccessible(true);
        return (AtomicAction) f.get(aaObject);
    }

    public static boolean isJTA(Object aaObject) throws Exception {
        Field f = aaObject.getClass().getDeclaredField("isJTA");
        f.setAccessible(true);
        return (Boolean) f.get(aaObject);
    }

    public static TXStateProxy getCacheTransactionManagerCurrentTXState() {
        return TXManagerImpl.getCurrentTXState();
    }
}
