package com.app.apexwallet.controller;

import com.app.apexwallet.dto.WalletCreateRequest;
import com.app.apexwallet.dto.WalletCreateResponse;
import com.app.apexwallet.dto.WalletResponse;
import com.app.apexwallet.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class WalletController {
    public WalletService walletService;

    public WalletController(WalletService walletService){
        this.walletService = walletService;
    }

    @PostMapping("/{id}/wallet")
    public WalletCreateResponse createWallet(@RequestBody WalletCreateRequest request, @PathVariable Long id){
        return walletService.createWallet(request, id);
    }

    @GetMapping("/{id}/wallet")
    public WalletResponse displayWallet(@PathVariable Long id){
        return walletService.getWallet(id);
    }
}
