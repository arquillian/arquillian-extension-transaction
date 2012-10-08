package org.jboss.arquillian.transaction.impl.lifecycle;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;

public class DefaultTransactionEnabler implements TransactionEnabler {

    private final Deployment deployment;

    private final Container container;

    public DefaultTransactionEnabler(Deployment deployment, Container container) {
        this.deployment = deployment;
        this.container = container;
    }

    @Override
    public boolean isTransactionEnabled(TestEvent testEvent) {
        boolean runAsClient = RunModeUtils.isRunAsClient(deployment, testEvent.getTestClass().getJavaClass(), testEvent.getTestMethod());
        boolean isLocal = RunModeUtils.isLocalContainer(container);

        boolean transactionSupported = runAsClient || isLocal || (!runAsClient && isLocal);

        boolean transactionTest = testEvent.getTestMethod().isAnnotationPresent(Transactional.class)
                || testEvent.getTestClass().isAnnotationPresent(Transactional.class);

        return transactionSupported && transactionTest;
    }

}
