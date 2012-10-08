package org.jboss.arquillian.transaction.spi.provider;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;

/**
 * By implementing this interface one can alter strategy for determining
 * if transaction should be enabled for given test.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 *
 */
public interface TransactionEnabler {

    boolean isTransactionEnabled(TestEvent testEvent);

}
