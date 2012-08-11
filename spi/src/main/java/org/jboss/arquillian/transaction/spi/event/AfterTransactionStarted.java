package org.jboss.arquillian.transaction.spi.event;


/**
 * Event fired after new transaction is started.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="mailto:jmnarloch@gmail.com">Jakub Narloch</a>
 */
public class AfterTransactionStarted extends TransactionEvent {

    /**
     * Creates new instance of {@link AfterTransactionStarted} class.
     */
    public AfterTransactionStarted() {
        // empty constructor
    }
}