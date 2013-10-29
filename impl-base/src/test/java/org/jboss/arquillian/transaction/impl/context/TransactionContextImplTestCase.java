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
package org.jboss.arquillian.transaction.impl.context;

import org.jboss.arquillian.transaction.spi.annotation.TransactionScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link TransactionContextImpl} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionContextImplTestCase
{

   /**
    * Represents the instance of tested class.
    */
   private TransactionContextImpl instance;

   /**
    * Sets up the test environment.
    *
    * @throws Exception if any error occurs
    */
   @Before
   public void setUp() throws Exception
   {

      instance = new TransactionContextImpl();
   }

   /**
    * Tests the {@link TransactionContextImpl#getScope()} method.
    */
   @Test
   public void shouldHandleTransactionScope()
   {

      assertEquals("Incorrect scope has been returned.", TransactionScope.class, instance.getScope());
   }

   /**
    * Tests the {@link TransactionContextImpl#createNewObjectStore()} method.
    */
   @Test
   public void shouldCreateObjectStore()
   {

      assertNotNull("Method returned null result.", instance.createNewObjectStore());
   }
}
