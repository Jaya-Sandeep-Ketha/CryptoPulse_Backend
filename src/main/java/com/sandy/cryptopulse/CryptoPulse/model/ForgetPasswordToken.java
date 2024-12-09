package com.sandy.cryptopulse.CryptoPulse.model;

import com.sandy.cryptopulse.CryptoPulse.domain.VerificationType;
import jakarta.persistence.*;

@Entity
public class ForgetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @OneToOne
    private User user;

    private String otp;
    private VerificationType verificationType;
    private String sendTo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    @Override
    public String toString() {
        return "ForgetPasswordToken{" +
                "id=" + id +
                ", user=" + user +
                ", otp='" + otp + '\'' +
                ", verificationType=" + verificationType +
                ", sendTo='" + sendTo + '\'' +
                '}';
    }
}
