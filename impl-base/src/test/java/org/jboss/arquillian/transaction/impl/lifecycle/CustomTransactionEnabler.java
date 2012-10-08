package org.jboss.arquillian.transaction.impl.lifecycle;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;
import org.jboss.arquillian.transaction.spi.provider.TransactionEnabler;

public class CustomTransactionEnabler implements TransactionEnabler {

    @Override
    public boolean isTransactionEnabled(TestEvent testEvent) {
        return false;
    }

}
