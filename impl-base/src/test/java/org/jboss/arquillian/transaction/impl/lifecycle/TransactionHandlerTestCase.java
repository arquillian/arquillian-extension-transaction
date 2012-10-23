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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.event.BeforeTransactionStarted;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link TransactionHandler} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionHandlerTestCase extends AbstractTestTestBase {

    /**
     * Transaction provider.
     */
    @Mock
    private TransactionProvider mockTransactionProvider;

    /**
     * Transaction provider.
     */
    @Mock
    private TransactionContext mockTransactionContext;

    /**
     * Service loader.
     */
    @Mock
    private ServiceLoader mockServiceLoader;

    /**
     * Deployment.
     */
    @Mock
    private Deployment mockDeployment;

    /**
     * Deployment descriptor.
     */
    @Mock
    private DeploymentDescription mockDeploymentDescriptor;

    /**
     * The configuration.
     */
    private TransactionConfiguration transactionConfiguration;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TransactionHandler.class);
    }

    /**
     * Sets up the test environment.
     *
     * @throws Exception if any error occurs
     */
    @Before
    public void setUp() throws Exception {

        transactionConfiguration = new TransactionConfiguration();
        transactionConfiguration.setManager("configurationManager");

        bind(ApplicationScoped.class, ServiceLoader.class, mockServiceLoader);
        bind(ApplicationScoped.class, TransactionContext.class, mockTransactionContext);
        bind(ApplicationScoped.class, TransactionConfiguration.class, transactionConfiguration);
        bind(ApplicationScoped.class, Deployment.class, mockDeployment);

        when(mockServiceLoader.onlyOne(TransactionProvider.class)).thenReturn(mockTransactionProvider);
        when(mockDeployment.getDescription()).thenReturn(mockDeploymentDescriptor);
        when(mockDeployment.isDeployed()).thenReturn(true);
        when(mockDeploymentDescriptor.testable()).thenReturn(false);
    }

    /**
     * Tests the {@link TransactionHandler#startTransactionBeforeTest(org.jboss.arquillian.test.spi.event.suite.Before)}
     * method.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldStartTransaction() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("defaultTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // checks if the transaction context has been created
        verify(mockTransactionContext).activate();

        // verifies that the transaction has been started
        verify(mockTransactionProvider).beginTransaction(any(TransactionalTest.class));

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#startTransactionBeforeTest(org.jboss.arquillian.test.spi.event.suite.Before)}
     * method when the test method declares a transaction manager.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldStartTransactionWithTestMethodManager() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestManagerClass();
        Method testMethod = instance.getClass().getMethod("testWithManager");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // checks if the transaction context has been created
        verify(mockTransactionContext).activate();

        ArgumentCaptor<TransactionalTest> argumentCaptor = ArgumentCaptor.forClass(TransactionalTest.class);

        // verifies that the transaction has been started
        verify(mockTransactionProvider).beginTransaction(argumentCaptor.capture());

        // checks if the manager name has been correctly retrieved
        assertEquals("The manager name is invalid.", "testMethodManager", argumentCaptor.getValue().getManager());

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#startTransactionBeforeTest(org.jboss.arquillian.test.spi.event.suite.Before)}
     * method when the test case declares a transaction manager.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldStartTransactionWithTestCaseManager() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestManagerClass();
        Method testMethod = instance.getClass().getMethod("testWithoutManager");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // checks if the transaction context has been created
        verify(mockTransactionContext).activate();

        ArgumentCaptor<TransactionalTest> argumentCaptor = ArgumentCaptor.forClass(TransactionalTest.class);

        // verifies that the transaction has been started
        verify(mockTransactionProvider).beginTransaction(argumentCaptor.capture());

        // checks if the manager name has been correctly retrieved
        assertEquals("The manager name is invalid.", "testCaseManager", argumentCaptor.getValue().getManager());

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#startTransactionBeforeTest(org.jboss.arquillian.test.spi.event.suite.Before)}
     * method when manager should be read from the configuration.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldStartTransactionWithConfigurationManager() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("defaultTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // checks if the transaction context has been created
        verify(mockTransactionContext).activate();

        ArgumentCaptor<TransactionalTest> argumentCaptor = ArgumentCaptor.forClass(TransactionalTest.class);

        // verifies that the transaction has been started
        verify(mockTransactionProvider).beginTransaction(argumentCaptor.capture());

        // checks if the manager name has been correctly retrieved
        assertEquals("The manager name is invalid.", "configurationManager", argumentCaptor.getValue().getManager());

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#startTransactionBeforeTest(org.jboss.arquillian.test.spi.event.suite.Before)}
     * method.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldNotStartTransaction() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("disabledTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // checks if the transaction context hasn't been created
        verifyZeroInteractions(mockTransactionContext);

        // verifies that the transaction hasn't been started
        verifyZeroInteractions(mockTransactionProvider);

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#endTransactionAfterTest(org.jboss.arquillian.test.spi.event.suite.After)}
     * method.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldRollbackTransaction() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("rollbackTest");

        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // checks if the transaction context has been disposed
        verify(mockTransactionContext).destroy();

        // verifies that the transaction has been rollback
        verify(mockTransactionProvider).rollbackTransaction(any(TransactionalTest.class));

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#endTransactionAfterTest(org.jboss.arquillian.test.spi.event.suite.After)}
     * method.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldRollbackTransactionOnFail() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("failTest");

        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.FAILED));

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // checks if the transaction context has been disposed
        verify(mockTransactionContext).destroy();

        // verifies that the transaction has been rollback
        verify(mockTransactionProvider).rollbackTransaction(any(TransactionalTest.class));

        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link TransactionHandler#endTransactionAfterTest(org.jboss.arquillian.test.spi.event.suite.After)}
     * method.
     *
     * @throws Exception if any error occurs
     */
    @Test
    public void shouldCommitTransaction() throws Exception {

        getManager().getContext(ClassContext.class).activate(TestClass.class);

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("commitTest");

        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // checks if the transaction context has been disposed
        verify(mockTransactionContext).destroy();

        // verifies that the transaction has been committed
        verify(mockTransactionProvider).commitTransaction(any(TransactionalTest.class));

        getManager().getContext(ClassContext.class).deactivate();
    }

    @Test
    public void shouldActivateTransactionWhenRunAsClient() throws Exception {
        when(mockDeploymentDescriptor.testable()).thenReturn(false);
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("commitTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        assertEventFiredInContext(BeforeTransactionStarted.class, ApplicationContext.class);
    }

    @Test
    public void shouldActivateTransactionWhenLocalProtocol() throws Exception {
        when(mockDeploymentDescriptor.testable()).thenReturn(true);
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        Container container = Mockito.mock(Container.class);
        DeployableContainer deployableContainer = Mockito.mock(DeployableContainer.class);
        when(container.getDeployableContainer()).thenReturn(deployableContainer);
        when(deployableContainer.getDefaultProtocol()).thenReturn(new ProtocolDescription("Local"));

        bind(ApplicationScoped.class, Container.class, container);
        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("commitTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        assertEventFiredInContext(BeforeTransactionStarted.class, ApplicationContext.class);
    }

    @Test
    public void shouldActivateTransactionWhenRunAsClientAndLocalProtocol() throws Exception {
        when(mockDeploymentDescriptor.testable()).thenReturn(false);
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        Container container = Mockito.mock(Container.class);
        DeployableContainer deployableContainer = Mockito.mock(DeployableContainer.class);
        when(container.getDeployableContainer()).thenReturn(deployableContainer);
        when(deployableContainer.getDefaultProtocol()).thenReturn(new ProtocolDescription("Local"));

        bind(ApplicationScoped.class, Container.class, container);
        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("commitTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        assertEventFiredInContext(BeforeTransactionStarted.class, ApplicationContext.class);
    }

    @Test
    public void shouldNotActivateTransactionWhenNotRunAsClientOnClientSide() throws Exception {
        when(mockDeploymentDescriptor.testable()).thenReturn(true);
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));

        Object instance = new TestClass();
        Method testMethod = instance.getClass().getMethod("commitTest");

        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        assertEventNotFiredInContext(BeforeTransactionStarted.class, ApplicationContext.class);
    }

    /**
     * Imitates a test case. Used for testing different conditions.
     *
     * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
     */
    @Transactional
    @SuppressWarnings("unused")
    private static class TestClass {

        public void defaultTest() throws Exception {
            // empty test
        }

        @Transactional(value = TransactionMode.COMMIT)
        public void commitTest() throws Exception {
            // empty test
        }

        @Transactional(value = TransactionMode.ROLLBACK)
        public void rollbackTest() throws Exception {
            // empty test
        }

        public void failTest() throws Exception {
            // empty test
        }

        @Transactional(value = TransactionMode.DISABLED)
        public void disabledTest() throws Exception {
            // empty test
        }
    }

    /**
     * Imitated a test case. Used for testing different conditions.
     *
     * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
     */
    @Transactional(manager = "testCaseManager")
    @SuppressWarnings("unused")
    private static class TestManagerClass {

        @Transactional(manager = "testMethodManager")
        public void testWithManager() throws Exception {
            // empty test
        }

        @Transactional
        public void testWithoutManager() throws Exception {
            // empty test
        }
    }
}
