package com.app.apexwallet.service;

import com.app.apexwallet.dto.WalletCreateRequest;
import com.app.apexwallet.dto.WalletCreateResponse;
import com.app.apexwallet.dto.WalletResponse;
import com.app.apexwallet.dto.WalletTransactionRequest;
import com.app.apexwallet.entity.Transaction;
import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import com.app.apexwallet.enums.TransactionStatus;
import com.app.apexwallet.enums.TransactionType;
import com.app.apexwallet.exception.*;
import com.app.apexwallet.kafka.TransactionEvent;
import com.app.apexwallet.repository.TransactionRepository;
import com.app.apexwallet.repository.UserRepository;
import com.app.apexwallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger logger =
            LoggerFactory.getLogger(WalletService.class);

    public WalletService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            TransactionService transactionService,
            ApplicationEventPublisher eventPublisher) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
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

    public WalletResponse getWallet(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User does not exist"));
        Wallet wallet = walletRepository.findWalletByUser(user);
        if (wallet == null){
            throw new WalletNotFoundException("User does not have a wallet");
        }
        return new WalletResponse(wallet.getId(), wallet.getBalance(), wallet.getCurrency(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    @Transactional
    public WalletResponse deposit(Long id, WalletTransactionRequest request, String idempotencyKey) {

        logger.info(
                "Deposit requested for user {} with amount {}",
                id,
                request.getAmount()
        );

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();

            if (transaction.getAmount().compareTo(request.getAmount()) != 0
                    || transaction.getType() != TransactionType.DEPOSIT) {

                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different transaction"
                );
            }

            return new WalletResponse(
                    transaction.getWallet().getId(),
                    transaction.getBalanceAfter(),
                    transaction.getWallet().getCurrency(),
                    transaction.getWallet().getCreatedAt(),
                    transaction.getWallet().getUpdatedAt()
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User does not exist"));

        Wallet wallet = walletRepository.findByUser(user);

        if (wallet == null) {
            throw new WalletNotFoundException("User does not have a wallet");
        }

        BigDecimal amount = request.getAmount();

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();

        transaction.setWallet(savedWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setBalanceAfter(savedWallet.getBalance());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setIdempotencyKey(idempotencyKey);

        transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent(
                transaction.getId(),
                savedWallet.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter()
        );

        eventPublisher.publishEvent(event);

        logger.info(
                "Deposit completed successfully for user {} with amount {}",
                id,
                request.getAmount()
        );

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getBalance(),
                savedWallet.getCurrency(),
                savedWallet.getCreatedAt(),
                savedWallet.getUpdatedAt()
        );
    }
    @Transactional
    public WalletResponse withdraw(Long id, WalletTransactionRequest request, String idempotencyKey) {

        logger.info(
                "Withdrawal requested for user {} with amount {}",
                id,
                request.getAmount()
        );

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();

            if (transaction.getAmount().compareTo(request.getAmount()) != 0
                    || transaction.getType() != TransactionType.WITHDRAWAL) {

                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different transaction"
                );
            }

            if (transaction.getStatus() == TransactionStatus.FAILED) {
                throw new InsufficientBalanceException(
                        "This withdrawal was already processed and failed"
                );
            }

            return new WalletResponse(
                    transaction.getWallet().getId(),
                    transaction.getBalanceAfter(),
                    transaction.getWallet().getCurrency(),
                    transaction.getWallet().getCreatedAt(),
                    transaction.getWallet().getUpdatedAt()
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User does not exist"));

        Wallet wallet = walletRepository.findByUser(user);

        if (wallet == null) {
            throw new WalletNotFoundException("User does not have a wallet");
        }

        BigDecimal amount = request.getAmount();

        if (wallet.getBalance().compareTo(amount) < 0){

            transactionService.recordFailedTransaction(wallet.getId(), TransactionType.WITHDRAWAL, amount, wallet.getBalance(), idempotencyKey);

            logger.warn(
                    "Withdrawal rejected for user {} due to insufficient balance. Requested: {}, Available: {}",
                    id,
                    amount,
                    wallet.getBalance()
            );

            throw new InsufficientBalanceException("Insufficient balance in your account");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(savedWallet);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(savedWallet.getBalance());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setIdempotencyKey(idempotencyKey);

        transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent(
                transaction.getId(),
                savedWallet.getId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter()
        );

        eventPublisher.publishEvent(event);

        logger.info(
                "Withdrawal completed successfully for user {} with amount {}",
                id,
                amount
        );

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getBalance(),
                savedWallet.getCurrency(),
                savedWallet.getCreatedAt(),
                savedWallet.getUpdatedAt()
        );
    }
}
