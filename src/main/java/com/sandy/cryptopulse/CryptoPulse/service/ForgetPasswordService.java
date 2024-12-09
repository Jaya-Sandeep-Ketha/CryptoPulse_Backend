package com.sandy.cryptopulse.CryptoPulse.service;

import com.sandy.cryptopulse.CryptoPulse.domain.VerificationType;
import com.sandy.cryptopulse.CryptoPulse.model.ForgetPasswordToken;
import com.sandy.cryptopulse.CryptoPulse.model.User;

public interface ForgetPasswordService {
    ForgetPasswordToken createToken(User user, String id, String otp, VerificationType verificationType, String sendTo);
    ForgetPasswordToken findById(String id);
    ForgetPasswordToken findByUser(Long userId);
    void deleteToken(ForgetPasswordToken token);
}
