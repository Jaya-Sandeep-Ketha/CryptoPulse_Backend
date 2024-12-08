package com.sandy.cryptopulse.CryptoPulse.repository;

import com.sandy.cryptopulse.CryptoPulse.model.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorOtpRepository extends JpaRepository<TwoFactorOTP, String> {
    TwoFactorOTP findByUserId(String userId);
}
