package com.app.apexwallet.service;

import com.app.apexwallet.dto.TransactionReportSummary;
import com.app.apexwallet.entity.TransactionReport;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.enums.TransactionType;
import com.app.apexwallet.exception.UserNotFoundException;
import com.app.apexwallet.exception.WalletNotFoundException;
import com.app.apexwallet.kafka.TransactionEvent;
import com.app.apexwallet.repository.TransactionReportRepository;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportingService {

    private final TransactionReportRepository transactionReportRepository;

    private final WalletRepository walletRepository;

    private final UserRepository userRepository;

    public ReportingService(
            TransactionReportRepository transactionReportRepository, WalletRepository walletRepository, UserRepository userRepository) {

        this.transactionReportRepository = transactionReportRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public void recordTransaction(TransactionEvent event) {

        if (transactionReportRepository.existsById(event.getTransactionId())) {
            return;
        }

        TransactionReport report = new TransactionReport();

        report.setTransactionId(event.getTransactionId());
        report.setWalletId(event.getWalletId());
        report.setType(
                com.app.apexwallet.enums.TransactionType
                        .valueOf(event.getTransactionType())
        );
        report.setAmount(event.getAmount());
        report.setBalanceAfter(event.getBalanceAfterTransaction());
        report.setReceivedAt(LocalDateTime.now());

        transactionReportRepository.save(report);
    }

    public List<TransactionReport> getReports(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        Wallet wallet = walletRepository.findWalletByUser(user);
        if (wallet == null) {
            throw new WalletNotFoundException("Wallet does not exist for this user");
        }
        Long walletId = wallet.getId();

        return transactionReportRepository
                .findByWalletIdOrderByReceivedAtDesc(walletId);
    }

    public TransactionReportSummary getSummary(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        Wallet wallet = walletRepository.findWalletByUser(user);
        if (wallet == null) {
            throw new WalletNotFoundException("Wallet does not exist for this user");
        }
        Long walletId = wallet.getId();

        List<TransactionReport> reports =
                transactionReportRepository
                        .findByWalletIdOrderByReceivedAtDesc(walletId);

        BigDecimal totalDeposits = reports.stream()
                .filter(report -> report.getType() == TransactionType.DEPOSIT)
                .map(TransactionReport::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = reports.stream()
                .filter(report -> report.getType() == TransactionType.WITHDRAWAL)
                .map(TransactionReport::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionReportSummary(
                totalDeposits,
                totalWithdrawals,
                reports.size()
        );
    }
}