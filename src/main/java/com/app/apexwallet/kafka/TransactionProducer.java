package com.app.apexwallet.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {

    private static final String TOPIC = "wallet-transactions";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendTransactionEvent(TransactionEvent event){
        kafkaTemplate.send(TOPIC,
                event.getTransactionId().toString(),
                event);
    }
}

