package org.jboss.arquillian.transaction.impl.lifecycle;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;

public class TransactionProviderProducer {

    @Inject
    private Instance<ServiceLoader> serviceLoaderInstance;

    @Inject
    private Instance<Deployment> deploymentInstance;

    @Inject
    private Instance<Container> containerInstance;

    @Inject
    @TestScoped
    private InstanceProducer<TransactionProvider> transactionProviderProducer;

    /**
     * Retrieves the {@link TransactionProvider} registered in current context.
     *
     * @return the transaction provider
     * @throws TransactionProviderNotFoundException if no provider could be found or there are multiple providers registered.
     */

    public void registerTransactionProvider(@Observes(precedence = 100) Before beforeTest) {
        if (!isTransactionSupported(beforeTest)) {
            return;
        }
        try {
            final TransactionProvider transactionProvider = serviceLoaderInstance.get().onlyOne(TransactionProvider.class);
            if (transactionProvider == null) {
                throw new TransactionProviderNotFoundException("Transaction provider for given test case has not been found.");
            }
            transactionProviderProducer.set(transactionProvider);
        } catch (IllegalStateException e) {
            throw new TransactionProviderNotFoundException("More then one transaction provider has been specified.", e);
        }
    }

    private boolean isTransactionSupported(TestEvent testEvent) {
        return new ModeChecker(deploymentInstance.get(), containerInstance.get()).isClientMode(testEvent);
    }
}
