package com.app.apexwallet.controller;

import com.app.apexwallet.dto.*;
import com.app.apexwallet.entity.TransactionReport;
import com.app.apexwallet.service.ReportingService;
import com.app.apexwallet.service.TransactionService;
import com.app.apexwallet.service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class WalletController {
    private final WalletService walletService;

    private final TransactionService transactionService;
    private final ReportingService reportingService;

    public WalletController(WalletService walletService, TransactionService transactionService, ReportingService reportingService){
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.reportingService = reportingService;
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
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WalletTransactionRequest request) {

        return walletService.deposit(id, request, idempotencyKey);
    }

    @PostMapping("/{id}/wallet/withdraw")
    public WalletResponse withdraw(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WalletTransactionRequest request) {

        return walletService.withdraw(id, request, idempotencyKey);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long id){
        return transactionService.getTransactionsForUser(id);
    }

    @GetMapping("/{id}/transaction-reports")
    public List<TransactionReport> getTransactionReports(
            @PathVariable Long id) {

        return reportingService.getReports(id);
    }

    @GetMapping("/{id}/transaction-reports/summary")
    public TransactionReportSummary getTransactionReportSummary(
            @PathVariable Long id) {

        return reportingService.getSummary(id);
    }
}
