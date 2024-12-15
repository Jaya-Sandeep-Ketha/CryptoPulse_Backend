package com.sandy.cryptopulse.CryptoPulse.service;

import com.sandy.cryptopulse.CryptoPulse.model.PaymentDetails;
import com.sandy.cryptopulse.CryptoPulse.model.User;

public interface PaymentDetailsService {
    public PaymentDetails addPaymentDetails(String accountNumber, String accountHolderName, String ifscCode, String bankName, User user);
    public PaymentDetails getUsersPAymentDetails(User user);
}
