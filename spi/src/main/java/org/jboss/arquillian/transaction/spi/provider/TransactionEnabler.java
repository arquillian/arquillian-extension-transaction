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
package org.jboss.arquillian.transaction.spi.provider;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;

/**
 * Implementing this interface allows alter strategy for determining
 * if transaction should be enabled for given test.
 *
 * This mechanism is intended to handle multiple implementations (through the chain),
 * however it will always use the default one {@see org.jboss.arquillian.transaction.impl.lifecycle.AnnotationBasedTransactionEnabler}
 * as the precedent.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 *
 */
public interface TransactionEnabler {

    /**
     * Verifies if the test class contains metadata
     * which can be used by given extension point to determine
     * transaction configuration.
     *
     * @param testEvent
     * @return
     */
    boolean isTransactionHandlingDefinedOnClassLevel(TestEvent testEvent);

    /**
     * Verifies if the test method contains metadata
     * which can be used by given extension point to determine
     * transaction configuration.
     *
     * @param testEvent
     * @return
     */
    boolean isTransactionHandlingDefinedOnMethodLevel(TestEvent testEvent);

    /**
     * Determines transaction mode using custom logic
     * of the given SPI implementation on class level.
     *
     * @param testEvent
     * @return
     */
    TransactionMode getTransactionModeFromClassLevel(TestEvent testEvent);

    /**
     * Determines transaction mode using custom logic
     * of the given SPI implementation on test level.
     *
     * @param testEvent
     * @return
     */
    TransactionMode getTransactionModeFromMethodLevel(TestEvent testEvent);

}
