package com.app.apexwallet.service;

import com.app.apexwallet.dto.WalletCreateRequest;
import com.app.apexwallet.dto.WalletCreateResponse;
import com.app.apexwallet.dto.WalletResponse;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.exception.UserNotFoundException;
import com.app.apexwallet.exception.WalletAlreadyExistsException;
import com.app.apexwallet.exception.WalletNotFoundException;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public WalletService(UserRepository userRepository, WalletRepository walletRepository){
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public WalletCreateResponse createWallet(WalletCreateRequest request, Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        if(walletRepository.existsByUser(user)){
            throw new WalletAlreadyExistsException("Wallet for your user already exists");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(request.getCurrency());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletCreateResponse(
                    savedWallet.getId(),
                    savedWallet.getBalance(),
                    savedWallet.getCurrency(),
                    savedWallet.getCreatedAt()
            );
    }

    public WalletResponse getWallet(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        Wallet wallet = walletRepository.findByUser(user);
        if (wallet == null){
            throw new WalletNotFoundException("User does not have a wallet");
        }
        return new WalletResponse(wallet.getId(), wallet.getBalance(), wallet.getCurrency(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }
}
