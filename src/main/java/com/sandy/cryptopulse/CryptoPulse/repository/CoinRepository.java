package com.sandy.cryptopulse.CryptoPulse.repository;

import com.sandy.cryptopulse.CryptoPulse.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {
}
