package com.app.apexwallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletCreateResponse {
    private Long id;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;

    public WalletCreateResponse(Long id, BigDecimal balance, String currency, LocalDateTime createdAt){
        this.id = id;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
