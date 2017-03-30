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

import java.io.*;
import java.util.Properties;

/**
 * The configuration converter.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class TransactionConfigurationConverter {

    /**
     * Exports the configuration to properties.
     *
     * @param configuration
     *     the extension configuration
     *
     * @return the exported configuration
     */
    public static String exportToProperties(TransactionConfiguration configuration) {

        OutputStream outputStream = null;
        try {

            outputStream = new ByteArrayOutputStream();

            Properties properties = new Properties();
            setPropertyValue(properties, "manager", configuration.getManager());
            setPropertyValue(properties, "transactionDefaultMode", configuration.getTransactionDefaultMode().name());
            properties.store(outputStream, "arquillian-transaction-configuration");

            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not export the configuration..", e);
        } finally {
            close(outputStream);
        }
    }

    /**
     * Imports the configuration from properties.
     *
     * @param inputStream
     *     the input stream
     */
    public static TransactionConfiguration importFromProperties(final InputStream inputStream) {

        try {

            final Properties properties = new Properties();
            properties.load(inputStream);

            final TransactionConfiguration transactionConfiguration = new TransactionConfiguration();
            transactionConfiguration.setManager(getPropertyValue(properties, "manager"));
            final String transactionDefaultMode = getPropertyValue(properties, "transactionDefaultMode");
            if (transactionDefaultMode != null && transactionDefaultMode.length() > 0) {
                transactionConfiguration.setTransactionDefaultMode(TransactionMode.valueOf(transactionDefaultMode));
            }
            return transactionConfiguration;
        } catch (IOException e) {
            throw new RuntimeException("Could not import the configuration.", e);
        } finally {
            close(inputStream);
        }
    }

    /**
     * Retrieves the properties value.
     *
     * @param properties
     *     the properties to use
     * @param propertyName
     *     the property name
     *
     * @return the property value
     */
    private static String getPropertyValue(Properties properties, String propertyName) {

        final String value = properties.getProperty(propertyName);

        if (value == null || "".equals(value.trim())) {
            return null;
        }

        return value;
    }

    /**
     * Sets the property value.
     *
     * @param properties
     *     the properties to use
     * @param propertyName
     *     the property name
     * @param value
     *     the property value
     */
    private static void setPropertyValue(Properties properties, String propertyName, String value) {
        properties.setProperty(propertyName, getPropertyValueOrDefault(value));
    }

    /**
     * Retrieves the property value or empty string if the value is null.
     *
     * @param value
     *     the property value
     *
     * @return the property value
     */
    private static String getPropertyValueOrDefault(String value) {
        return value != null ? value : "";
    }

    private static void close(final Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close output stream.", e);
            }
        }
    }
}
