package com.app.apexwallet.service;

import com.app.apexwallet.dto.TransactionResponse;
import com.app.apexwallet.entity.Transaction;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.enums.TransactionStatus;
import com.app.apexwallet.enums.TransactionType;
import com.app.apexwallet.exception.UserNotFoundException;
import com.app.apexwallet.exception.WalletNotFoundException;
import com.app.apexwallet.repository.TransactionRepository;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransaction(
            Long walletId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException("Wallet does not exist"));

        Transaction transaction = new Transaction();

        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getTransactionsForUser(Long userId) {

        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User does not exist"));

        List<Transaction> transactions =
                transactionRepository.findByWalletUserIdOrderByCreatedAtDesc(userId);

        List<TransactionResponse> transactionInfo = new ArrayList<>();

        for (Transaction transaction : transactions) {

            TransactionResponse response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getBalanceAfter(),
                    transaction.getStatus(),
                    transaction.getCreatedAt()
            );

            transactionInfo.add(response);
        }

        return transactionInfo;
    }
}
