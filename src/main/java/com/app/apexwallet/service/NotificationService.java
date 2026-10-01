package com.app.apexwallet.service;

import com.app.apexwallet.kafka.TransactionEvent;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationService.class);

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

        logger.info(
                "Transaction notification generated: transactionId={}, walletId={}, message={}",
                event.getTransactionId(),
                event.getWalletId(),
                message
        );
    }
}