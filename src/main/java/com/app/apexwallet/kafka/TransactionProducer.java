package com.app.apexwallet.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionProducer {

    private static final String TOPIC = "wallet-transactions";

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionProducer.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendTransactionEvent(TransactionEvent event){

        logger.info(
                "Publishing transaction event to Kafka: transactionId={}, walletId={}, type={}",
                event.getTransactionId(),
                event.getWalletId(),
                event.getTransactionType()
        );

        kafkaTemplate.send(
                TOPIC,
                event.getTransactionId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {

                logger.error(
                        "Failed to publish transaction event to Kafka: transactionId={}",
                        event.getTransactionId(),
                        exception
                );

                return;
            }

            logger.info(
                    "Transaction event published successfully to Kafka: transactionId={}, partition={}, offset={}",
                    event.getTransactionId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}

