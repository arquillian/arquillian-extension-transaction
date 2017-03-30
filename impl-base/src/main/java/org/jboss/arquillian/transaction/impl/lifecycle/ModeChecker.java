package org.jboss.arquillian.transaction.impl.lifecycle;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

public class ModeChecker {
    private final Deployment deployment;
    private final Container container;

    public ModeChecker(Deployment deployment, Container container) {
        this.deployment = deployment;
        this.container = container;
    }

    public boolean isClientMode(TestEvent testEvent) {
        boolean runAsClient =
            RunModeUtils.isRunAsClient(deployment, testEvent.getTestClass().getJavaClass(), testEvent.getTestMethod());
        boolean isLocal = RunModeUtils.isLocalContainer(container);
        return runAsClient || isLocal;
    }
}
