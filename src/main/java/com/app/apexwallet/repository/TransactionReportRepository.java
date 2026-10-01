package com.app.apexwallet.repository;

import com.app.apexwallet.entity.TransactionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionReportRepository
        extends JpaRepository<TransactionReport, Long> {

    List<TransactionReport> findByWalletIdOrderByReceivedAtDesc(Long walletId);
}