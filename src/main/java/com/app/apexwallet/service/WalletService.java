package com.app.apexwallet.service;

import com.app.apexwallet.dto.WalletCreateRequest;
import com.app.apexwallet.dto.WalletCreateResponse;
import com.app.apexwallet.dto.WalletResponse;
import com.app.apexwallet.dto.WalletTransactionRequest;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.exception.*;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

    @Transactional
    public WalletResponse getWallet(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        Wallet wallet = walletRepository.findByUser(user);
        if (wallet == null){
            throw new WalletNotFoundException("User does not have a wallet");
        }
        return new WalletResponse(wallet.getId(), wallet.getBalance(), wallet.getCurrency(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    @Transactional
    public WalletResponse deposit(Long id, WalletTransactionRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User does not exist"));

        Wallet wallet = walletRepository.findByUser(user);

        if (wallet == null) {
            throw new WalletNotFoundException("User does not have a wallet");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getBalance(),
                savedWallet.getCurrency(),
                savedWallet.getCreatedAt(),
                savedWallet.getUpdatedAt()
        );
    }
    @Transactional
    public WalletResponse withdraw(Long id, WalletTransactionRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User does not exist"));

        Wallet wallet = walletRepository.findByUser(user);

        if (wallet == null) {
            throw new WalletNotFoundException("User does not have a wallet");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        if (wallet.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Insufficient balance in your account");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getBalance(),
                savedWallet.getCurrency(),
                savedWallet.getCreatedAt(),
                savedWallet.getUpdatedAt()
        );
    }
}
