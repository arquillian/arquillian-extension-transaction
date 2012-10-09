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

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import junit.framework.Assert;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
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

    private static final String TRANSACTION_ENABLER_SPI_RESOURCE = TEST_SPI_FOLDER + "org.jboss.arquillian.transaction.spi.provider.TransactionEnabler";

    @Mock
    private ServiceLoader mockServiceLoader;

    @Mock
    private Deployment mockDeployment;

    @Mock
    private Container mockContainer;

    TransactionEnablerLoader transactionEnablerLoader;

    @Before
    public void initializeLoader() {
        when(mockServiceLoader.onlyOne(TransactionEnabler.class)).thenAnswer(new Answer<TransactionEnabler>() {
            @Override
            public TransactionEnabler answer(InvocationOnMock invocation) throws Throwable {
                final File spiFile = new File(TRANSACTION_ENABLER_SPI_RESOURCE);
                if (!spiFile.exists()) {
                    return null;
                }
                final String spiImplementation = new Scanner(spiFile).useDelimiter("\\A").next();
                return (TransactionEnabler) Class.forName(spiImplementation).newInstance();
            }
        });
        transactionEnablerLoader = new TransactionEnablerLoader(mockServiceLoader, mockDeployment, mockContainer);
    }

    @Test
    public void shouldUseDefaultImplementationWhenNoAlternateDefinedThroughSpi() throws Exception {
        // when
        TransactionEnabler transactionEnabler = transactionEnablerLoader.getTransactionEnabler();

        // then
        Assert.assertTrue(transactionEnabler instanceof AnnotationBasedTransactionEnabler);

    }

    @Test
    public void shouldResolveCustomTransactionEnablerWhenDefinedThroughSpi() throws Exception {
        // given
        final File spiEntry = createSPIEntry("org.jboss.arquillian.transaction.impl.lifecycle.CustomTransactionEnabler");

        try {
            // when
            TransactionEnabler transactionEnabler = transactionEnablerLoader.getTransactionEnabler();

            // then
            Assert.assertTrue(transactionEnabler instanceof CustomTransactionEnabler);
        } finally {
            spiEntry.delete();
        }

    }

    private File createSPIEntry(final String customImplementation) throws IOException {
        final File spiDirectory = new File(TEST_SPI_FOLDER);
        spiDirectory.mkdirs();

        final File file = new File(TRANSACTION_ENABLER_SPI_RESOURCE);
        FileWriter spiEntry = new FileWriter(file);
        spiEntry.write(customImplementation);
        spiEntry.close();

        return file;
    }

}