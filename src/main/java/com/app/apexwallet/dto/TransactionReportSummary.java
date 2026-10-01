package com.app.apexwallet.dto;

import java.math.BigDecimal;

public class TransactionReportSummary {

    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private long transactionCount;

    public TransactionReportSummary(
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            long transactionCount) {

        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public long getTransactionCount() {
        return transactionCount;
    }
}