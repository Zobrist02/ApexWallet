package com.app.apexwallet.repository;

import com.app.apexwallet.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {

    List<Transaction> findByWalletUserIdOrderByCreatedAtDesc(Long userId);

}
