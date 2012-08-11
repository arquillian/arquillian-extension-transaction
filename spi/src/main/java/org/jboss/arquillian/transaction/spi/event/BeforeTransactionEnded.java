package org.jboss.arquillian.transaction.spi.event;

import org.jboss.arquillian.test.spi.event.suite.TestEvent;

import java.lang.reflect.Method;

/**
 * Event fired before the transaction will be compensated.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class BeforeTransactionEnded extends TransactionEvent {

    /**
     * Creates new instance of {@link BeforeTransactionEnded} class.
     */
    public BeforeTransactionEnded() {
        // empty constructor
    }
}
