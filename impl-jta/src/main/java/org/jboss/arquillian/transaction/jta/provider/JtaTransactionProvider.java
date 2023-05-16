/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.arquillian.transaction.jta.provider;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.transaction.spi.annotation.TransactionScope;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class JtaTransactionProvider implements TransactionProvider {

    private static final String DEFAULT_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

    @Inject
    private Instance<Context> jndiContextInstance;

    @Inject
    @TransactionScope
    private InstanceProducer<UserTransaction> userTransactionInstance;

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginTransaction(TransactionalTest transactionalTest) {
        try {
            final UserTransaction transaction = getUserTransaction(transactionalTest);
            userTransactionInstance.set(transaction);
            if (isTransactionNotActive(transaction)) {
                transaction.begin();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to start transaction", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitTransaction(TransactionalTest transactionalTest) {
        try {
            final UserTransaction transaction = userTransactionInstance.get();
            if (isTransactionMarkedToRollback(transaction)) {
                transaction.rollback();
            } else {
                transaction.commit();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to commit the transaction.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollbackTransaction(TransactionalTest transactionalTest) {
        try {
            final UserTransaction userTransaction = userTransactionInstance.get();

            if (userTransaction != null) {
                userTransaction.rollback();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not rollback the transaction.", e);
        }
    }

    /**
     * Retrieves the {@link UserTransaction} from JNDI context.
     *
     * @param transactionalTest
     *     the transaction test
     *
     * @return the {@link UserTransaction}
     */
    private UserTransaction getUserTransaction(TransactionalTest transactionalTest) {

        final String jndiName = getJtaTransactionJndiName(transactionalTest);

        try {
            final Context context = jndiContextInstance.get();

            if (context == null) {
                throw new RuntimeException("No Naming Context available.");
            }

            return (UserTransaction) context.lookup(jndiName);
        } catch (NamingException e) {

            throw new RuntimeException("Failed obtaining transaction using ["
                + jndiName
                + "]. Is it registered under this name in your container?", e);
        }
    }

    private String getJtaTransactionJndiName(TransactionalTest transactionalTest) {
        if (transactionalTest.getManager() != null) {
            return transactionalTest.getManager();
        }

        return DEFAULT_TRANSACTION_JNDI_NAME;
    }

    private boolean isTransactionNotActive(final UserTransaction transaction) throws SystemException {
        return Status.STATUS_NO_TRANSACTION == transaction.getStatus();
    }

    private boolean isTransactionMarkedToRollback(final UserTransaction transaction) throws SystemException {
        return Status.STATUS_MARKED_ROLLBACK == transaction.getStatus();
    }
}
