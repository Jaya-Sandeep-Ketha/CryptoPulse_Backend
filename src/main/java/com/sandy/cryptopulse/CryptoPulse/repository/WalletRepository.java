package com.sandy.cryptopulse.CryptoPulse.repository;

import com.sandy.cryptopulse.CryptoPulse.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUserId(Long userId);
}
