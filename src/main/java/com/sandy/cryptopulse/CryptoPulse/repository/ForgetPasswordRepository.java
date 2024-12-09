package com.sandy.cryptopulse.CryptoPulse.repository;

import com.sandy.cryptopulse.CryptoPulse.model.ForgetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgetPasswordRepository extends JpaRepository<ForgetPasswordToken, String> {
    ForgetPasswordToken findByUserId(Long userId);



}
