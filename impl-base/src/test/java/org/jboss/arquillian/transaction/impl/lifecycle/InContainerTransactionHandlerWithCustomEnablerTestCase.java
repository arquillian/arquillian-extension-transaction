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

import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.container.InContainerTransactionHandler;
import org.jboss.arquillian.transaction.impl.lifecycle.CustomTransactionEnabler.CustomTransactional;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InContainerTransactionHandlerWithCustomEnablerTestCase extends AbstractTestTestBase {

    @Mock
    private TransactionProvider mockTransactionProvider;

    @Mock
    private TransactionContext mockTransactionContext;

    @Mock
    private ServiceLoader mockServiceLoader;

    @Mock
    private Deployment mockDeployment;

    @Mock
    private DeploymentDescription mockDeploymentDescriptor;

    private TransactionConfiguration transactionConfiguration;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(InContainerTransactionHandler.class);
    }

    @Before
    public void setUp() throws Exception {
        when(mockServiceLoader.all(TransactionEnabler.class)).thenAnswer(new Answer<Collection<TransactionEnabler>>() {

            @Override
            public Collection<TransactionEnabler> answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<TransactionEnabler>() {{
                    add(new CustomTransactionEnabler());
                }};
            }
        });

        transactionConfiguration = new TransactionConfiguration();
        transactionConfiguration.setManager("configurationManager");

        bind(ApplicationScoped.class, ServiceLoader.class, mockServiceLoader);
        bind(ApplicationScoped.class, TransactionContext.class, mockTransactionContext);
        bind(ApplicationScoped.class, TransactionConfiguration.class, transactionConfiguration);
        bind(TestScoped.class, TransactionProvider.class, mockTransactionProvider);
        bind(ApplicationScoped.class, Deployment.class, mockDeployment);

        when(mockServiceLoader.onlyOne(TransactionProvider.class)).thenReturn(mockTransactionProvider);
        when(mockDeployment.getDescription()).thenReturn(mockDeploymentDescriptor);
        when(mockDeployment.isDeployed()).thenReturn(true);
        when(mockDeploymentDescriptor.testable()).thenReturn(false);
    }

    @After
    public void disableContexts() {
        getManager().getContext(ClassContext.class).deactivate();
    }

    @Test
    public void should_have_transaction_disabled_when_defined_on_class_level() throws Exception {
        // given
        Object instance = new ClassWithGloballyDisabledTransactionSupport();
        Method testMethod = instance.getClass().getMethod("shouldHaveTransactionDisabled");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));

        // then
        verifyZeroInteractions(mockTransactionContext);
    }

    @Test
    public void should_have_transaction_enabled_in_commit_when_defined_on_method_level_using_customized_approach()
        throws Exception {
        // given
        Object instance = new ClassWithGloballyDisabledTransactionSupport();
        Method testMethod = instance.getClass().getMethod("shouldHaveTransactionEnabledThroughCustomAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).commitTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_enabled_in_commit_when_defined_on_method_level_using_customized_approach_explicitly()
        throws Exception {
        // given
        Object instance = new ClassWithGloballyDisabledTransactionSupport();
        Method testMethod =
            instance.getClass().getMethod("shouldHaveTransactionInCommitModeEnabledThroughCustomAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).commitTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_rollback_when_test_failed() throws Exception {
        // given
        Object instance = new ClassWithGloballyDisabledTransactionSupport();
        Method testMethod =
            instance.getClass().getMethod("shouldHaveTransactionInCommitModeEnabledThroughCustomAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.FAILED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).rollbackTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_enabled_in_rollback_when_defined_on_method_level_using_customized_approach_explicitly()
        throws Exception {
        getManager().getContext(ClassContext.class).activate(ClassWithGloballyDisabledTransactionSupport.class);

        // given
        Object instance = new ClassWithGloballyDisabledTransactionSupport();
        Method testMethod =
            instance.getClass().getMethod("shouldHaveTransactionInRollbackModeEnabledThroughCustomAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).rollbackTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_enabled_in_rollback_when_defined_on_method_level_using_customized_approach_explicitly_and_core()
        throws Exception {
        // given
        Object instance = new OverlappingTransactionSettingsShouldRespectCore();
        Method testMethod = instance.getClass().getMethod("shouldRollbackBasedOnCoreAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).rollbackTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_disabled_when_defined_on_method_level_using_customized_approach_explicitly_and_core()
        throws Exception {
        // given
        Object instance = new OverlappingTransactionSettingsShouldRespectCore();
        Method testMethod = instance.getClass().getMethod("shouldBeDisabledBasedOnCoreAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider, never()).beginTransaction(any(TransactionalTest.class));
        verify(mockTransactionProvider, never()).rollbackTransaction(any(TransactionalTest.class));
        verify(mockTransactionProvider, never()).commitTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_commit_transaction_when_globally_defined_on_class_level_using_custom_annotation()
        throws Exception {
        // given
        Object instance = new TransactionalSupportEnabledOnClassLevelUsingAdditionalAnnotation();
        Method testMethod = instance.getClass().getMethod("shouldBeCommited");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider).commitTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_disabed_when_globally_enabled_on_class_level_using_custom_annotation_but_disabled_through_core_annotation()
        throws Exception {
        // given
        Object instance = new TransactionalSupportEnabledOnClassLevelUsingAdditionalAnnotation();
        Method testMethod = instance.getClass().getMethod("shouldBeDisabledBasedOnCoreAnnotation");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider, never()).beginTransaction(any(TransactionalTest.class));
    }

    @Test
    public void should_have_transaction_disabled_when_not_defined() throws Exception {
        // given
        Object instance = new TransactionSupportShouldBeDisabled();
        Method testMethod = instance.getClass().getMethod("shouldBeDisabled");

        // when
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.Before(instance, testMethod));
        bind(TestScoped.class, TestResult.class, new TestResult(TestResult.Status.PASSED));
        getManager().fire(new org.jboss.arquillian.test.spi.event.suite.After(instance, testMethod));

        // then
        verify(mockTransactionProvider, never()).beginTransaction(any(TransactionalTest.class));
        verify(mockTransactionProvider, never()).rollbackTransaction(any(TransactionalTest.class));
        verify(mockTransactionProvider, never()).commitTransaction(any(TransactionalTest.class));
    }

    // -- Test doubles

    @SuppressWarnings("unused")
    @Transactional(value = TransactionMode.DISABLED)
    private static class ClassWithGloballyDisabledTransactionSupport {

        public void shouldHaveTransactionDisabled() {
        }

        @CustomTransactional
        public void shouldHaveTransactionEnabledThroughCustomAnnotation() {
        }

        @CustomTransactional(TransactionMode.COMMIT)
        public void shouldHaveTransactionInCommitModeEnabledThroughCustomAnnotation() {
        }

        @CustomTransactional(TransactionMode.ROLLBACK)
        public void shouldHaveTransactionInRollbackModeEnabledThroughCustomAnnotation() {
        }
    }

    @SuppressWarnings("unused")
    private static class OverlappingTransactionSettingsShouldRespectCore {

        @Transactional(value = TransactionMode.ROLLBACK)
        @CustomTransactional(TransactionMode.COMMIT)
        public void shouldRollbackBasedOnCoreAnnotation() {
        }

        @Transactional(value = TransactionMode.DISABLED)
        @CustomTransactional(TransactionMode.ROLLBACK)
        public void shouldBeDisabledBasedOnCoreAnnotation() {
        }
    }

    @SuppressWarnings("unused")
    @CustomTransactional(TransactionMode.COMMIT)
    private static class TransactionalSupportEnabledOnClassLevelUsingAdditionalAnnotation {

        public void shouldBeCommited() {
        }

        @Transactional(value = TransactionMode.DISABLED)
        public void shouldBeDisabledBasedOnCoreAnnotation() {
        }
    }

    @SuppressWarnings("unused")
    private static class TransactionSupportShouldBeDisabled {

        public void shouldBeDisabled() {
        }
    }
}
