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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionEnablerLoaderTestCase {

    private static final String TEST_SPI_FOLDER = "src/test/resources/META-INF/services/";

    private List<String> spiResources;

    @Mock
    private ServiceLoader mockServiceLoader;

    TransactionEnablerLoader transactionEnablerLoader;

    @Before
    public void initializeLoader() {
        spiResources = new ArrayList<String>();
        when(mockServiceLoader.all(TransactionEnabler.class)).thenAnswer(new Answer<Collection<TransactionEnabler>>() {

            @Override
            public Collection<TransactionEnabler> answer(InvocationOnMock invocation) throws Throwable {
                final Collection<TransactionEnabler> results = new ArrayList<TransactionEnabler>();
                for (String spiResource : spiResources) {
                    results.add(loadSpi(spiResource));
                }
                return results;
            }

            private TransactionEnabler loadSpi(final String transactionEnablerSpiResource)
                    throws FileNotFoundException, InstantiationException, IllegalAccessException,
                    ClassNotFoundException {
                final File spiFile = new File(transactionEnablerSpiResource);
                if (!spiFile.exists()) {
                    return null;
                }
                final String spiImplementation = new Scanner(spiFile).useDelimiter("\\A").next();
                TransactionEnabler transactionEnabler = (TransactionEnabler) Class.forName(spiImplementation).newInstance();
                return transactionEnabler;
            }
        });
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
        final File spiEntry = createSPIEntry("org.jboss.arquillian.transaction.impl.lifecycle.CustomTransactionEnabler");

        try {
            // when
            Collection<TransactionEnabler> transactionEnablers = transactionEnablerLoader.getTransactionEnablers();

            // then
            assertThat(transformToClasses(transactionEnablers)).containsExactly(AnnotationBasedTransactionEnabler.class, CustomTransactionEnabler.class);
        } finally {
            spiEntry.delete();
        }

    }

    // -- Test helpers

    private File createSPIEntry(final String customImplementation) throws IOException {

        final String resource = TEST_SPI_FOLDER + customImplementation;
        spiResources.add(resource);

        final File spiDirectory = new File(TEST_SPI_FOLDER);
        spiDirectory.mkdirs();
        final File file = new File(resource);
        FileWriter spiEntry = new FileWriter(file);
        spiEntry.write(customImplementation);
        spiEntry.close();

        return file;
    }

    // -- Testing helpers

    private List<Class<?>> transformToClasses(Collection<TransactionEnabler> transactionEnablers) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        for (TransactionEnabler enabler : transactionEnablers) {
            classes.add(enabler.getClass());
        }
        return classes;
    }

}