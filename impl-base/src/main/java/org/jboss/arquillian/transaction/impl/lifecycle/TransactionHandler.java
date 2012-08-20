/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.transaction.impl.lifecycle;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.test.impl.RunModeUtils;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.test.TransactionalTestImpl;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.event.*;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

/**
 * The transaction life cycle handler, which is responsible for initializing new transactions before execution of the
 * test method and compensating it afterwards, based on the strategy defined by {@link Transactional} annotation. <p/>
 * The implementation delegates to registered {@link TransactionProvider} in the current context to perform actual
 * operation. If no provider has been found or multiple classes has been registered then {@link
 * TransactionProviderNotFoundException} is being thrown.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 * @see Transactional
 * @see TransactionProvider
 */
public class TransactionHandler {

    /**
     * Instance of {@link ServiceLoader}, used for retrieving the {@link TransactionProvider} registered in the
     * context.
     */
    @Inject
    private Instance<ServiceLoader> serviceLoaderInstance;

    /**
     * The transaction configuration.
     */
    @Inject
    private Instance<TransactionConfiguration> configurationInstance;

    /**
     * Transaction lifecycle event.
     */
    @Inject
    Event<TransactionEvent> lifecycleEvent;

    /**
     * The context bound to the current transaction.
     */
    @Inject
    private Instance<TransactionContext> transactionContextInstance;

    /**
     * The instance of the test result.
     */
    @Inject
    private Instance<TestResult> testResultInstance;

    /**
     * Represents the deployment.
     */
    @Inject
    private Instance<Deployment> deploymentInstance;

    /**
     * Represents the container.
     */
    @Inject
    private Instance<Container> containerInstance;

    /**
     * Initializes a transaction before execution of the test.
     *
     * @param beforeTest the test event
     */
    public void startTransactionBeforeTest(@Observes(precedence = 10) Before beforeTest) {

        TransactionProvider transactionProvider;

        if (isTransactionEnabled(beforeTest)) {

            TransactionMode transactionMode = getTransactionMode(beforeTest);

            if (transactionMode != TransactionMode.DISABLED) {
                transactionProvider = getTransactionProvider();

                // creates the transaction context
                TransactionContext transactionContext = transactionContextInstance.get();
                transactionContext.activate();

                lifecycleEvent.fire(new BeforeTransactionStarted());

                transactionProvider.beginTransaction(new TransactionalTestImpl(getTransactionManager(beforeTest)));

                lifecycleEvent.fire(new AfterTransactionStarted());
            }
        }
    }

    /**
     * Compensates the transaction after execution of the test.
     *
     * @param afterTest the test event
     */
    public void endTransactionAfterTest(@Observes(precedence = 50) After afterTest) {

        TransactionProvider transactionProvider;

        if (isTransactionEnabled(afterTest)) {

            // retrieves the transaction mode, declared for the test method
            TransactionMode transactionMode = getTransactionMode(afterTest);

            if (transactionMode != TransactionMode.DISABLED) {
                try {
                    lifecycleEvent.fire(new BeforeTransactionEnded());

                    transactionProvider = getTransactionProvider();

                    TransactionalTest transactionalTest =
                            new TransactionalTestImpl(getTransactionManager(afterTest));

                    if (transactionMode == TransactionMode.ROLLBACK || isTestRequiresRollback()) {
                        // rollbacks the transaction
                        transactionProvider.rollbackTransaction(transactionalTest);
                    } else {
                        // commits the transaction
                        transactionProvider.commitTransaction(transactionalTest);
                    }

                } finally {
                    lifecycleEvent.fire(new AfterTransactionEnded());

                    // finally destroys the transaction context
                    TransactionContext transactionContext = transactionContextInstance.get();
                    transactionContext.destroy();
                }
            }
        }
    }

    /**
     * Returns whether the test requires to be rollback.
     * </p>
     * By default it will return true if the last executed test has failed.
     *
     * @return true if test requires rollback, false otherwise
     */
    private boolean isTestRequiresRollback() {

        return testResultInstance.get().getStatus() == TestResult.Status.FAILED;
    }

    /**
     * Returns whether the transaction is enabled for the current test.
     *
     * @param testEvent the test event
     *
     * @return true if the transaction support has been enabled, false otherwise
     */
    private boolean isTransactionEnabled(TestEvent testEvent) {

        boolean runAsClient = RunModeUtils.isRunAsClient(deploymentInstance.get(),
                testEvent.getTestClass().getJavaClass(), testEvent.getTestMethod());

        boolean transactionSupported = !runAsClient || runAsClient && RunModeUtils.isLocalContainer(
                containerInstance.get());

        boolean transactionTest =  testEvent.getTestMethod().isAnnotationPresent(Transactional.class)
                || testEvent.getTestClass().isAnnotationPresent(Transactional.class);

        return transactionSupported && transactionTest;
    }

    /**
     * Retrieves the transaction mode for the current test.
     *
     * @param testEvent the test event
     *
     * @return the transaction mode
     */
    private TransactionMode getTransactionMode(TestEvent testEvent) {

        // retrieves the transaction annotation
        Transactional transactional = getTransactionalAnnotation(testEvent);

        // returns the transaction mode
        return transactional.value();
    }

    /**
     * Retrieves the transaction manager. The default implementation tries to first retrieve then transaction manager
     * name from the annotation first on the method level then class. If non of above condition is meet then is used
     * the manager name provided through configuration.
     *
     * @param testEvent the test event
     *
     * @return the transaction manager name or null if one hasn't been set
     */
    private String getTransactionManager(TestEvent testEvent) {

        Transactional transactional;
        String transactionManager = "";

        // tries to retrieve the name of the manager from annotated test method
        transactional = testEvent.getTestMethod().getAnnotation(Transactional.class);
        if (transactional != null) {
            transactionManager = transactional.manager();
        }

        // if the transaction manager name hasn't been set then tries to retrieve it from class level annotation
        if (transactionManager.length() == 0) {
            transactional = testEvent.getTestClass().getAnnotation(Transactional.class);
            if (transactional != null) {
                transactionManager = transactional.manager();
            }
        }

        // if the transaction manager name still hasn't been set then tries to get the manager from the configuration
        if (transactionManager.length() == 0) {

            if (configurationInstance.get().getManager() != null) {
                transactionManager = configurationInstance.get().getManager();
            }
        }

        return transactionManager.length() != 0 ? transactionManager : null;
    }

    /**
     * Retrieves the {@link Transactional} from the test method or class that was used for configuring the transaction
     * support.
     *
     * @param testEvent the test event
     *
     * @return the {@link Transactional} annotation
     */
    private Transactional getTransactionalAnnotation(TestEvent testEvent) {

        if (testEvent.getTestMethod().isAnnotationPresent(Transactional.class)) {
            return testEvent.getTestMethod().getAnnotation(Transactional.class);
        } else {
            return testEvent.getTestClass().getAnnotation(Transactional.class);
        }
    }

    /**
     * Retrieves the {@link TransactionProvider} registered in current context.
     *
     * @return the transaction provider
     *
     * @throws TransactionProviderNotFoundException
     *          if no provider could be found or there are multiple providers registered.
     */
    private TransactionProvider getTransactionProvider() {

        try {
            ServiceLoader serviceLoader = serviceLoaderInstance.get();

            TransactionProvider transactionProvider = serviceLoader.onlyOne(TransactionProvider.class);

            if (transactionProvider == null) {
                throw new TransactionProviderNotFoundException(
                        "Transaction provider for given test case has not been found.");
            }

            return transactionProvider;
        } catch (IllegalStateException exc) {
            // thrown if there were multiple providers registered in the context
            throw new TransactionProviderNotFoundException(
                    "More then one transaction provider has been specified.", exc);
        }
    }
}