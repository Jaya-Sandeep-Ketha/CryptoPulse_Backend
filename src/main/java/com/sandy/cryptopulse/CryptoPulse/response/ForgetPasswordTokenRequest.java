package com.sandy.cryptopulse.CryptoPulse.response;

import com.sandy.cryptopulse.CryptoPulse.domain.VerificationType;

public class ForgetPasswordTokenRequest {
    private String sendTo;
    private VerificationType verificationType;

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }
}
