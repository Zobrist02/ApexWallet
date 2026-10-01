package com.app.apexwallet.kafka;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class TransactionEventPublisher {

    private final TransactionProducer transactionProducer;

    public TransactionEventPublisher(TransactionProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionEvent event) {
        transactionProducer.sendTransactionEvent(event);
    }
}