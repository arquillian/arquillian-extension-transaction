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

package org.jboss.arquillian.transaction.impl.container;

import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.transaction.impl.client.TransactionConfigurationProducer;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link TransactionConfigurationRemoteProducer} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationRemoteProducerTestCase extends AbstractTestTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TransactionConfigurationRemoteProducer.class);
    }

    /**
     * Tests the {@link TransactionConfigurationRemoteProducer#loadConfiguration(BeforeSuite)}
     * method.
     */
    @Test
    public void shouldCreateConfiguration() {

        getManager().getContext(ClassContext.class).activate(TestClass.class);
        getManager().fire(new BeforeSuite());

        TransactionConfiguration transactionConfiguration = getManager().resolve(TransactionConfiguration.class);
        assertEquals("Invalid transaction manager name.", "testManagerName", transactionConfiguration.getManager());

        getManager().getContext(ClassContext.class).deactivate();
    }
}
