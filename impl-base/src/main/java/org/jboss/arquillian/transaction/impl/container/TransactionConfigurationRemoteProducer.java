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

package org.jboss.arquillian.transaction.impl.container;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfiguration;
import org.jboss.arquillian.transaction.impl.configuration.TransactionConfigurationConverter;

/**
 * The configuration remote producer.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationRemoteProducer
{

   /**
    * The extension configuration.
    */
   @Inject
   @ApplicationScoped
   private InstanceProducer<TransactionConfiguration> configurationInstance;

   /**
    * Loads the configuration from the properties file.
    *
    * @param beforeSuite the event fired before execution of test suite
    */
   public void loadConfiguration(@Observes BeforeSuite beforeSuite)
   {

      TransactionConfiguration configuration = TransactionConfigurationConverter.importFromProperties(
            SecurityActions.getResource("arquillian-transaction-configuration.properties"));

      configurationInstance.set(configuration);
   }
}
