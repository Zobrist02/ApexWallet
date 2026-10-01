package com.app.apexwallet.kafka;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionEventPublisher {

    private final TransactionProducer transactionProducer;

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionEventPublisher.class);

    public TransactionEventPublisher(TransactionProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionEvent event) {

        logger.info(
                "Transaction committed. Publishing event for transactionId={}, walletId={}, type={}",
                event.getTransactionId(),
                event.getWalletId(),
                event.getTransactionType()
        );

        transactionProducer.sendTransactionEvent(event);
    }
}