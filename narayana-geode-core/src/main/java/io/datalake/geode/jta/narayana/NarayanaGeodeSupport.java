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


import org.apache.geode.LogWriter;
import org.apache.geode.cache.CacheFactory;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Helper class used to enlist Geode as one-phase, last resource in running Transactions.
 *
 * @author Christian Tzolov (christian.tzolov@gmail.com)
 */
public class NarayanaGeodeSupport {

    /**
     * Use this helper method to enlist Geode as a Last Resource Commit in current transaction.
     * <p>
     * Must be called within the transactional boundaries (e.g in a transactional method) but before any
     * Geode operation was used!
     * <p>
     * For example:
     * <pre>
     *      &#64;Transactional
     *     	public void myServiceMethod(Region region) {
     *
     * 			// Enable Geode Narayana LRCO
     * 			NarayanaGeodeSupport.enlistGeodeAsLastCommitResource();
     *          .....
     *
     *          region.put(KEY, VALUE);
     *          region.get(KEY);
     *          .....
     *      }
     * </pre>
     * <p>When enlisting the resource via method Transaction.enlistResource, Narayana
     * ensures that only a single instance of this type of participant is used within each transaction. Your resource
     * is driven last in the commit protocol, and no invocation of method prepare occurs.
     * <p>
     * By default an attempt to enlist more than one instance of a LastResourceCommitOptimisation class will fail and false
     * will be returned from Transaction.enlistResource. This behavior can be overridden by setting the
     * <b>com.arjuna.ats.jta.allowMultipleLastResources</b> to true.
     * <p>
     * <p>
     * Failure to enlist will throw an {@link NarayanaGeodeException} runtime exception, rolling back the transaction.
     */
    public static void enlistGeodeAsLastCommitResource() {

        try {
            TransactionManager txManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
            Transaction tx = txManager.getTransaction();
            tx.enlistResource(new NarayanaGeodeLastCommitResource());

            LogWriter logger = CacheFactory.getAnyInstance().getLogger();
            if (logger.fineEnabled()) {
                logger.fine("NarayanaGeodeLastCommitResource:Enlist into: " + tx.getClass());
            }
        } catch (SystemException e) {
            throw new NarayanaGeodeException("Failed to obtain the running transaction", e);
        } catch (RollbackException e) {
            throw new NarayanaGeodeException("Failed to enlist Geode as LRCO resource in the transaction!", e);
        }
    }
}
