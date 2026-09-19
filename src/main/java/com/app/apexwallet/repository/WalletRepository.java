package com.app.apexwallet.repository;

import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Wallet findByUser(User user);
}
