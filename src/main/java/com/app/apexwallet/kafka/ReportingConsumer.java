package com.app.apexwallet.kafka;

import com.app.apexwallet.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportingConsumer {

    private final ReportingService reportingService;

    private static final Logger logger =
            LoggerFactory.getLogger(ReportingConsumer.class);

    public ReportingConsumer(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "apexwallet-reporting-group"
    )
    public void consume(TransactionEvent event) {

        logger.info(
                "Transaction event received by reporting consumer: transactionId={}, walletId={}, type={}",
                event.getTransactionId(),
                event.getWalletId(),
                event.getTransactionType()
        );

        reportingService.recordTransaction(event);
    }
}