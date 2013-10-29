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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfigurationConverter;
import org.jboss.arquillian.transaction.impl.container.TransactionRemoteExtension;
import org.jboss.arquillian.transaction.impl.context.TransactionContextImpl;
import org.jboss.arquillian.transaction.impl.lifecycle.TransactionHandler;
import org.jboss.arquillian.transaction.impl.test.TransactionalTestImpl;
import org.jboss.arquillian.transaction.spi.annotation.TransactionScope;
import org.jboss.arquillian.transaction.spi.context.TransactionContext;
import org.jboss.arquillian.transaction.spi.event.BeforeTransactionStarted;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * An archive appender that packages all required classes for this extension.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionArchiveAppender implements AuxiliaryArchiveAppender
{

   /**
    * Represents the extension configuration.
    */
   @Inject
   private Instance<TransactionConfiguration> configurationInstance;

   /**
    * {@inheritDoc}
    */
   @Override
   public Archive<?> createAuxiliaryArchive()
   {

      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-transaction.jar");

      bundleApi(archive);

      bundleSpi(archive);

      bundleImplementation(archive);

      // adds the extension properties
      archive.addAsResource(new StringAsset(
            TransactionConfigurationConverter.exportToProperties(configurationInstance.get())),
            "arquillian-transaction-configuration.properties");

      // registers the remote extension
      archive.addAsServiceProvider(RemoteLoadableExtension.class, TransactionRemoteExtension.class);

      return archive;
   }

   private void bundleImplementation(JavaArchive archive)
   {
      archive.addPackage(TransactionRemoteExtension.class.getPackage());
      archive.addPackage(TransactionConfiguration.class.getPackage());
      archive.addPackage(TransactionContextImpl.class.getPackage());
      archive.addPackage(TransactionHandler.class.getPackage());
      archive.addPackage(TransactionalTestImpl.class.getPackage());
   }

   private void bundleSpi(JavaArchive archive)
   {
      archive.addPackage(TransactionScope.class.getPackage());
      archive.addPackage(TransactionContext.class.getPackage());
      archive.addPackage(BeforeTransactionStarted.class.getPackage());
      archive.addPackage(TransactionProvider.class.getPackage());
      archive.addPackage(TransactionalTest.class.getPackage());
   }

   private void bundleApi(JavaArchive archive)
   {
      archive.addPackage(Transactional.class.getPackage());
   }
}
