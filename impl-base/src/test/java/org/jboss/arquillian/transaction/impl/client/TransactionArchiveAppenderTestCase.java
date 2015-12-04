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

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfigurationConverter;
import org.jboss.arquillian.transaction.impl.container.TransactionConfigurationRemoteProducer;
import org.jboss.arquillian.transaction.impl.container.TransactionRemoteExtension;
import org.jboss.arquillian.transaction.impl.context.TransactionContextImpl;
import org.jboss.arquillian.transaction.impl.lifecycle.TransactionHandler;
import org.jboss.arquillian.transaction.impl.lifecycle.TransactionProviderNotFoundException;
import org.jboss.arquillian.transaction.impl.test.DefaultTransactionalTest;
import org.jboss.arquillian.transaction.spi.annotation.TransactionScope;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.event.AfterTransactionEnded;
import org.jboss.arquillian.transaction.spi.event.AfterTransactionStarted;
import org.jboss.arquillian.transaction.spi.event.BeforeTransactionEnded;
import org.jboss.arquillian.transaction.spi.event.BeforeTransactionStarted;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link TransactionArchiveAppender} class.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionArchiveAppenderTestCase extends AbstractTestTestBase
{

   private TransactionArchiveAppender instance;
   private TransactionConfiguration transactionConfiguration;

   private final static List<Class<?>> REQUIRED_CLASSES = Arrays.asList(
         TransactionRemoteExtension.class, TransactionContextImpl.class, TransactionHandler.class,
         TransactionProviderNotFoundException.class, DefaultTransactionalTest.class, Transactional.class,
         TransactionMode.class, TransactionScope.class, TransactionContext.class,
         AfterTransactionEnded.class, AfterTransactionStarted.class, BeforeTransactionEnded.class,
         BeforeTransactionStarted.class, TransactionProvider.class, TransactionalTest.class,
         TransactionConfiguration.class, TransactionConfigurationConverter.class,
         TransactionConfigurationRemoteProducer.class);

   @Before
   public void setUp()
   {

      transactionConfiguration = new TransactionConfiguration();

      bind(ApplicationScoped.class, TransactionConfiguration.class, transactionConfiguration);

      instance = new TransactionArchiveAppender();
      getManager().inject(instance);
   }

   @Test
   public void shouldPackageAllExtensionClasses() throws Exception
   {

      Archive archive = instance.createAuxiliaryArchive();

      assertNotNull("Method returned null.", archive);
      assertTrue("The returned archive has incorrect type.", archive instanceof JavaArchive);

      for (Class c : REQUIRED_CLASSES)
      {

         assertTrue("The required type is missing: " + c.getName(),
               archive.contains(getClassResourcePath(c)));
      }
   }

   /**
    * Retrieves the resource name of the give class.
    *
    * @param c the class
    * @return the resource name for the class
    */
   public static ArchivePath getClassResourcePath(Class c)
   {

      StringBuilder sb = new StringBuilder();
      sb.append("/");
      sb.append(c.getName().replace(".", "/"));
      sb.append(".class");

      return ArchivePaths.create(sb.toString());
   }
}
