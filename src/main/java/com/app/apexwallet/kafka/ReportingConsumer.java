package com.app.apexwallet.kafka;

import com.app.apexwallet.service.ReportingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportingConsumer {

    private final ReportingService reportingService;

    public ReportingConsumer(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @KafkaListener(
            topics = "wallet-transactions",
            groupId = "apexwallet-reporting-group"
    )
    public void consume(TransactionEvent event) {

        reportingService.recordTransaction(event);
    }
}