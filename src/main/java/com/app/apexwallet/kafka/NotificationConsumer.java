package com.app.apexwallet.kafka;

import com.app.apexwallet.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "apexwallet-notification-group"
    )
    public void consume(TransactionEvent event) {

        notificationService.sendTransactionNotification(event);
    }
}