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

import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionEnablerLoaderTestCase {

    @Mock
    private ServiceLoader mockServiceLoader;

    TransactionEnablerLoader transactionEnablerLoader;

    @Before
    public void initialize() {
        transactionEnablerLoader = new TransactionEnablerLoader(mockServiceLoader);
    }

    @Test
    public void shouldUseDefaultImplementationWhenNoAlternateDefinedThroughSpi() throws Exception {
        // when
        Collection<TransactionEnabler> transactionEnablers = transactionEnablerLoader.getTransactionEnablers();

        // then
        assertThat(transformToClasses(transactionEnablers)).containsOnly(AnnotationBasedTransactionEnabler.class);
    }

    @Test
    public void shouldResolveCustomTransactionEnablerWhenDefinedThroughSpi() throws Exception {
        // given
        when(mockServiceLoader.all(TransactionEnabler.class)).thenReturn(
            Arrays.<TransactionEnabler>asList(new CustomTransactionEnabler()));

        // when
        Collection<TransactionEnabler> transactionEnablers = transactionEnablerLoader.getTransactionEnablers();

        // then
        assertThat(transformToClasses(transactionEnablers)).containsExactly(AnnotationBasedTransactionEnabler.class,
            CustomTransactionEnabler.class);
    }

    // -- Test helpers

    private List<Class<?>> transformToClasses(Collection<TransactionEnabler> transactionEnablers) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        for (TransactionEnabler enabler : transactionEnablers) {
            classes.add(enabler.getClass());
        }
        return classes;
    }
}
