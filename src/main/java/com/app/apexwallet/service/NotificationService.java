package com.app.apexwallet.service;

import com.app.apexwallet.kafka.TransactionEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendTransactionNotification(TransactionEvent event) {

        String message;

        if ("DEPOSIT".equals(event.getTransactionType())) {

            message = "Deposit of ₹"
                    + event.getAmount()
                    + " successful. "
                    + "New balance: ₹"
                    + event.getBalanceAfterTransaction();

        } else {

            message = "Withdrawal of ₹"
                    + event.getAmount()
                    + " successful. "
                    + "New balance: ₹"
                    + event.getBalanceAfterTransaction();
        }

        System.out.println("NOTIFICATION: " + message);
    }
}