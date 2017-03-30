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

/**
 * The transaction extension configuration.
 *
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class TransactionConfiguration {

    /**
     * Represents the name of the manager which should be used for handling the transactions.
     */
    private String manager;

    /**
     * Default mode used (COMMIT) for tests if not specified otherwise using annotation.
     */
    private TransactionMode transactionDefaultMode = TransactionMode.COMMIT;

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public TransactionMode getTransactionDefaultMode() {
        return transactionDefaultMode;
    }

    public void setTransactionDefaultMode(TransactionMode transactionDefaultMode) {
        this.transactionDefaultMode = transactionDefaultMode;
    }
}
