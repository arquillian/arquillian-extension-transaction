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

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;

import java.util.Collections;
import java.util.Map;

/**
 * The configuration producer.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationProducer {

    public static final String TRANSACTION_EXTENSION = "transaction";

    public static final String MANAGER_PROPERTY_NAME = "manager";

    public static final String DEFAULT_TRANSACTION_MODE_PROPERTY_NAME = "transactionDefaultMode";

    @Inject @ApplicationScoped
    private InstanceProducer<TransactionConfiguration> configurationInstance;

    /**
     * Loads the extension configuration as soon as possible, i.e. in the load configuration bootstrapping phase.
     *
     * @param descriptor
     *     the event fired during load configuration (before the test suite events are fired)
     */
    public void loadConfiguration(@Observes ArquillianDescriptor descriptor) {

        TransactionConfiguration config = getConfiguration(descriptor);
        configurationInstance.set(config);
    }

    /**
     * Creates the configuration from the passed arquillian descriptor.
     *
     * @param arquillianDescriptor
     *     the arquillian descriptor
     *
     * @return the created configuration
     */
    private TransactionConfiguration getConfiguration(ArquillianDescriptor arquillianDescriptor) {

        final Map<String, String> extensionProperties = getExtensionProperties(arquillianDescriptor);

        final TransactionConfiguration configuration = new TransactionConfiguration();
        configuration.setManager(extensionProperties.get(MANAGER_PROPERTY_NAME));
        final String transactionDefaultMode = extensionProperties.get(DEFAULT_TRANSACTION_MODE_PROPERTY_NAME);
        if (transactionDefaultMode != null && transactionDefaultMode.length() > 0) {
            configuration.setTransactionDefaultMode(TransactionMode.valueOf(transactionDefaultMode));
        }

        return configuration;
    }

    /**
     * Retrieves the extension properties
     *
     * @param arquillianDescriptor
     *     the arquillian descriptor
     *
     * @return the extension properties
     */
    private Map<String, String> getExtensionProperties(ArquillianDescriptor arquillianDescriptor) {
        for (ExtensionDef extensionDef : arquillianDescriptor.getExtensions()) {
            if (TRANSACTION_EXTENSION.equals(extensionDef.getExtensionName())) {
                return extensionDef.getExtensionProperties();
            }
        }

        return Collections.emptyMap();
    }
}
