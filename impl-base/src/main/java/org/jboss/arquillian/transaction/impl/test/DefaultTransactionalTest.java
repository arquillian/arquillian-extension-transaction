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
package org.jboss.arquillian.transaction.impl.test;

import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

/**
 * The default implementation of {@link TransactionalTest}.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class DefaultTransactionalTest implements TransactionalTest
{

   /**
    * Represents the name of the manager.
    */
   private String manager;

   /**
    * Creates new instance of {@link DefaultTransactionalTest} class.
    *
    * @param manager the manger
    */
   public DefaultTransactionalTest(String manager)
   {

      this.manager = manager;
   }

   /**
    * Retrieves the manager
    *
    * @return the manager
    */
   @Override
   public String getManager()
   {

      return manager;
   }
}
