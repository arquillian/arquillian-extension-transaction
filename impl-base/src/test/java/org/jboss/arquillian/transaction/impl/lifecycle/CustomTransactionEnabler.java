/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CustomTransactionEnabler implements TransactionEnabler
{

   @Override
   public boolean isTransactionHandlingDefinedOnClassLevel(TestEvent testEvent)
   {
      return testEvent.getTestClass().isAnnotationPresent(CustomTransactional.class);
   }

   @Override
   public boolean isTransactionHandlingDefinedOnMethodLevel(TestEvent testEvent)
   {
      return testEvent.getTestMethod().isAnnotationPresent(CustomTransactional.class);
   }

   @Override
   public TransactionMode getTransactionModeFromClassLevel(TestEvent testEvent)
   {
      return testEvent.getTestClass().getAnnotation(CustomTransactional.class).value();
   }

   @Override
   public TransactionMode getTransactionModeFromMethodLevel(TestEvent testEvent)
   {
      return testEvent.getTestMethod().getAnnotation(CustomTransactional.class).value();
   }

   @Target(value = {TYPE, METHOD})
   @Retention(value = RUNTIME)
   @Inherited
   public static @interface CustomTransactional
   {
      TransactionMode value() default TransactionMode.COMMIT;
   }
}
