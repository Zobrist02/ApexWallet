package com.app.apexwallet.dto;

import java.math.BigDecimal;

public class WalletTransactionRequest {

    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}