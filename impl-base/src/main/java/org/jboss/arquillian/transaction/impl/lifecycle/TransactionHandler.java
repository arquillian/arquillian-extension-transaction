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

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.test.DefaultTransactionalTest;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.event.*;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

/**
 * The transaction life cycle handler, which is responsible for initializing new
 * transactions before execution of the test method and compensating it
 * afterwards, based on the strategy defined by {@link Transactional}
 * annotation.
 * <br />
 * The implementation delegates to registered {@link TransactionProvider} in the
 * current context to perform actual operation. If no provider has been found or
 * multiple classes has been registered then
 * {@link TransactionProviderNotFoundException} is being thrown.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 * @see Transactional
 * @see TransactionProvider
 */
public abstract class TransactionHandler {

    /**
     * Instance of {@link ServiceLoader}, used for retrieving
     * required SPIs registered in the context.
     */
    @Inject
    private Instance<ServiceLoader> serviceLoaderInstance;

    @Inject
    private Instance<TransactionConfiguration> configurationInstance;

    @Inject
    private Event<TransactionEvent> lifecycleEvent;

    @Inject
    private Instance<TransactionContext> transactionContextInstance;

    @Inject
    private Instance<TestResult> testResultInstance;

    @Inject
    private Instance<TransactionProvider> transactionProviderInstance;

    public abstract boolean isTransactionSupported(TestEvent testEvent);

    public void startTransactionBeforeTest(@Observes(precedence = 10) Before beforeTest) {

        if (isTransactionEnabled(beforeTest)) {

            TransactionContext transactionContext = transactionContextInstance.get();
            transactionContext.activate();

            lifecycleEvent.fire(new BeforeTransactionStarted());

            transactionProviderInstance.get()
                .beginTransaction(new DefaultTransactionalTest(getTransactionManager(beforeTest)));

            lifecycleEvent.fire(new AfterTransactionStarted());
        }
    }

    public void endTransactionAfterTest(@Observes(precedence = -1) EventContext<After> afterTestContext) {
        try {
            afterTestContext.proceed();
        } finally {
            endTransaction(afterTestContext.getEvent());
        }
    }

    // -- Private methods

    private void endTransaction(After afterTest) {
        if (isTransactionEnabled(afterTest)) {
            try {
                lifecycleEvent.fire(new BeforeTransactionEnded());

                final TransactionProvider transactionProvider = transactionProviderInstance.get();
                final TransactionalTest transactionalTest =
                    new DefaultTransactionalTest(getTransactionManager(afterTest));

                if (rollbackRequired(afterTest)) {
                    transactionProvider.rollbackTransaction(transactionalTest);
                } else {
                    transactionProvider.commitTransaction(transactionalTest);
                }
            } finally {
                lifecycleEvent.fire(new AfterTransactionEnded());
                transactionContextInstance.get().destroy();
            }
        }
    }

    /**
     * Returns whether the transaction is enabled for the current test.
     *
     * @param testEvent
     *     the test event
     *
     * @return true if the transaction support has been enabled, false otherwise
     */
    private boolean isTransactionEnabled(TestEvent testEvent) {
        if (!isTransactionSupported(testEvent)) {
            return false;
        }

        final TransactionMode transactionMode = getTransactionMode(testEvent);
        return transactionMode != null && !TransactionMode.DISABLED.equals(transactionMode);
    }

    private boolean rollbackRequired(TestEvent testEvent) {
        return testRequiresRollbackDueToFailure() || TransactionMode.ROLLBACK.equals(getTransactionMode(testEvent));
    }

    /**
     * Returns whether the test requires to be rolled back. </p>
     * By default it will return true if the last executed test has failed.
     *
     * @return true if test requires rollback, false otherwise
     */
    private boolean testRequiresRollbackDueToFailure() {
        if (testResultInstance.get() != null) {
            final Status actualStatus = testResultInstance.get().getStatus();
            return TestResult.Status.FAILED.equals(actualStatus);
        }
        return true;
    }

    /**
     * Retrieves the transaction mode for the current test.
     *
     * @param testEvent
     *     the test event
     *
     * @return the transaction mode
     */
    private TransactionMode getTransactionMode(TestEvent testEvent) {
        TransactionMode result = extractFromTestEvent(testEvent);
        if (TransactionMode.DEFAULT.equals(result)) {
            result = configurationInstance.get().getTransactionDefaultMode();
        }
        return result;
    }

    private TransactionMode extractFromTestEvent(TestEvent testEvent) {
        TransactionMode methodLevel = null;
        TransactionMode classLevel = null;
        final TransactionEnablerLoader transactionEnablerLoader =
            new TransactionEnablerLoader(serviceLoaderInstance.get());
        for (TransactionEnabler enabler : transactionEnablerLoader.getTransactionEnablers()) {
            if (methodLevel == null && enabler.isTransactionHandlingDefinedOnMethodLevel(testEvent)) {
                methodLevel = enabler.getTransactionModeFromMethodLevel(testEvent);
            }

            if (classLevel == null && enabler.isTransactionHandlingDefinedOnClassLevel(testEvent)) {
                classLevel = enabler.getTransactionModeFromClassLevel(testEvent);
            }
        }
        if (methodLevel != null) {
            return methodLevel;
        }

        return classLevel;
    }

    /**
     * Retrieves the transaction manager. The default implementation tries to
     * first retrieve then transaction manager name from the annotation first on
     * the method level then class. If non of above condition is meet then is
     * used the manager name provided through configuration.
     *
     * @param testEvent
     *     the test event
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

        // if the transaction manager name hasn't been set then tries to
        // retrieve it from class level annotation
        if (transactionManager.length() == 0) {
            transactional = testEvent.getTestClass().getAnnotation(Transactional.class);
            if (transactional != null) {
                transactionManager = transactional.manager();
            }
        }

        if (transactionManager.length() == 0) {
            transactionManager = obtainTranscationManagerFromConfiguration(transactionManager);
        }

        return transactionManager.length() != 0 ? transactionManager : null;
    }

    private String obtainTranscationManagerFromConfiguration(String transactionManager) {
        if (configurationInstance.get().getManager() != null) {
            transactionManager = configurationInstance.get().getManager();
        }
        return transactionManager;
    }
}