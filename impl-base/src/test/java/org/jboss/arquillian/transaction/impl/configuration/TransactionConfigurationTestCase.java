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

package org.jboss.arquillian.transaction.impl.configuration;

import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.impl.test.TransactionalTestImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests {@link TransactionConfiguration} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationTestCase {

    /**
     * Represents the instance of tested class.
     */
    private TransactionConfiguration instance;

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        instance = new TransactionConfiguration();
    }

    /**
     * Tests {@link TransactionalTestImpl#getManager()} method.</p>
     */
    @Test
    public void shouldReturnManager() {

        String manager = "manager";

        assertNull("Invalid property default value.", instance.getManager());

        instance.setManager(manager);

        assertEquals("Invalid property value.", manager, instance.getManager());
    }

    @Test
    public void shouldHaveDefaultTransactionModeSetToCommitIfNotSpecifiedOtherwise() {
        assertEquals("Expecting COMMIT value.", TransactionMode.COMMIT, instance.getTransactionDefaultMode());
    }
}
