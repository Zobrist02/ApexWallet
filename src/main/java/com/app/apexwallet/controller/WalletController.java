package com.app.apexwallet.controller;

import com.app.apexwallet.dto.*;
import com.app.apexwallet.service.TransactionService;
import com.app.apexwallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class WalletController {
    private final WalletService walletService;

    private final TransactionService transactionService;

    public WalletController(WalletService walletService, TransactionService transactionService){
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @PostMapping("/{id}/wallet")
    public WalletCreateResponse createWallet(@RequestBody WalletCreateRequest request, @PathVariable Long id){
        return walletService.createWallet(request, id);
    }

    @GetMapping("/{id}/wallet")
    public WalletResponse displayWallet(@PathVariable Long id){
        return walletService.getWallet(id);
    }

    @PostMapping("/{id}/wallet/deposit")
    public WalletResponse deposit(
            @PathVariable Long id,
            @Valid @RequestBody WalletTransactionRequest request) {

        return walletService.deposit(id, request);
    }

    @PostMapping("/{id}/wallet/withdraw")
    public WalletResponse withdraw(
            @PathVariable Long id,
            @Valid @RequestBody WalletTransactionRequest request) {

        return walletService.withdraw(id, request);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long id){
        return transactionService.getTransactionsForUser(id);
    }
}
