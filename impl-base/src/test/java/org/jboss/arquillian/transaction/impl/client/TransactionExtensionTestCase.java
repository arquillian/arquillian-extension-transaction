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

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link TransactionExtension} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionExtensionTestCase {

    /**
     * Represents the instance of tested class.
     */
    private TransactionExtension instance;

    /**
     * Extension builder.
     */
    @Mock
    private LoadableExtension.ExtensionBuilder mockExtensionBuilder;

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        instance = new TransactionExtension();
    }

    /**
     * Tests the {@link TransactionExtension#register(LoadableExtension.ExtensionBuilder)} method.
     */
    @Test
    public void shouldRegisterExtensionServices() {

        when(mockExtensionBuilder.service(any(Class.class), any(Class.class))).thenReturn(mockExtensionBuilder);

        instance.register(mockExtensionBuilder);

        verify(mockExtensionBuilder).observer(TransactionConfigurationProducer.class);
        verify(mockExtensionBuilder).service(AuxiliaryArchiveAppender.class, TransactionArchiveAppender.class);
        verifyNoMoreInteractions(mockExtensionBuilder);
    }
}
