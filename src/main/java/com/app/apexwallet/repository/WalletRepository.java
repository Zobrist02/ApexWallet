package com.app.apexwallet.repository;

import com.app.apexwallet.entity.User;
import com.app.apexwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUser(User user);
    Wallet findByUser(User user);
}
