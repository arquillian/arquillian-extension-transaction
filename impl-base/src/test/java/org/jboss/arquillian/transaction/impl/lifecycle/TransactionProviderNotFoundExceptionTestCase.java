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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link TransactionProviderNotFoundException} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionProviderNotFoundExceptionTestCase {

    private static final String MESSAGE = "MESSAGE";

    private static final Throwable CAUSE = new Throwable();

    private TransactionProviderNotFoundException instance;

    @Test
    public void shouldExtendRuntimeException() {

        assertEquals("Class does not extend the proper base class.", RuntimeException.class,
            TransactionProviderNotFoundException.class.getSuperclass());
    }

    @Test
    public void shouldCreateExceptionInstance() {

        instance = new TransactionProviderNotFoundException();
    }

    @Test
    public void shouldCreateExceptionInstanceWithDetailedErrorMessage() {

        instance = new TransactionProviderNotFoundException(MESSAGE);

        assertEquals("The exception has invalid error message.", MESSAGE, instance.getMessage());
    }

    @Test
    public void shouldCreateExceptionInstanceWithDetailedErrorMessageAndInnerCause() {

        instance = new TransactionProviderNotFoundException(MESSAGE, CAUSE);

        assertEquals("The exception has invalid error message.", MESSAGE, instance.getMessage());
        assertEquals("The exception has invalid inner cause.", CAUSE, instance.getCause());
    }
}
