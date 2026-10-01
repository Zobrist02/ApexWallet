package com.app.apexwallet.kafka;

import com.app.apexwallet.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationConsumer {

    private final NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationConsumer.class);

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "apexwallet-notification-group"
    )
    public void consume(TransactionEvent event) {

        logger.info(
                "Transaction event received by notification consumer: transactionId={}, walletId={}, type={}",
                event.getTransactionId(),
                event.getWalletId(),
                event.getTransactionType()
        );

        notificationService.sendTransactionNotification(event);
    }
}