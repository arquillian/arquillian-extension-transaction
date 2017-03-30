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

package org.jboss.arquillian.transaction.impl.client;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfigurationProducer;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link TransactionConfigurationProducer} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationProducerTestCase extends AbstractTestTestBase {
    private ArquillianDescriptor descriptor;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TransactionConfigurationProducer.class);
    }

    @Before
    public void load_arquillian_configuration() throws Exception {

        descriptor = Descriptors.importAs(ArquillianDescriptor.class).fromStream(
            new FileInputStream(new File("src/test/resources", "arquillian.xml")));
    }

    @Test
    public void shouldCreateConfiguration() {

        getManager().getContext(ClassContext.class).activate(TestClass.class);
        getManager().bindAndFire(ApplicationScoped.class, ArquillianDescriptor.class, descriptor);

        TransactionConfiguration transactionConfiguration = getManager().resolve(TransactionConfiguration.class);
        assertEquals("Wrongly mapped transaction manager name.", "testManagerName",
            transactionConfiguration.getManager());
        assertEquals("Wrongly mapped transaction default mode.", TransactionMode.DISABLED,
            transactionConfiguration.getTransactionDefaultMode());

        getManager().getContext(ClassContext.class).deactivate();
    }
}
