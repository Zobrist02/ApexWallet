package com.app.apexwallet.kafka;

import java.math.BigDecimal;

public class TransactionEvent {

    private Long transactionId;
    private Long walletId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;

    public TransactionEvent() {
    }

    public TransactionEvent(
            Long transactionId,
            Long walletId,
            String transactionType,
            BigDecimal amount,
            BigDecimal balanceAfterTransaction) {

        this.transactionId = transactionId;
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }
}