package com.sandy.cryptopulse.CryptoPulse.controller;

import com.sandy.cryptopulse.CryptoPulse.domain.VerificationType;
import com.sandy.cryptopulse.CryptoPulse.model.User;
import com.sandy.cryptopulse.CryptoPulse.model.VerificationCode;
import com.sandy.cryptopulse.CryptoPulse.service.EmailService;
import com.sandy.cryptopulse.CryptoPulse.service.UserService;
import com.sandy.cryptopulse.CryptoPulse.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @GetMapping("/api/users/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<User>(user,HttpStatus.OK);
    }

    @PostMapping("/api/users/verification/{verificationType}/send-otp")
    public ResponseEntity<String> sendVerificationOtp(@RequestHeader("Authorization") String jwt, @PathVariable VerificationType verificationType) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        if(verificationCode == null) {
            verificationCode = verificationCodeService.sendVerificationCode(user, verificationType);
        }
        if(verificationType.equals(VerificationType.EMAIL)){
            emailService.sendVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
        }
        return new ResponseEntity<>("Verification Otp sent successfully",HttpStatus.OK);
    }

    @PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
    public ResponseEntity<User> enableTwoFactorAuthentication(@PathVariable String otp, @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        VerificationCode verificationCode = verificationCodeService.getVerificationCodeByUser(user.getId());
        String sendTo = verificationCode.getVerificationType().equals(VerificationType.EMAIL)?verificationCode.getEmail():verificationCode.getMobile();
        boolean isVerified = verificationCode.getOtp().equals(otp);
        if(isVerified){
            User updatedUser = userService.enableTwoFactorAuthentication(verificationCode.getVerificationType(), sendTo, user);
            verificationCodeService.deleteVerificationCodeById(verificationCode);
            return new ResponseEntity<>(updatedUser,HttpStatus.OK);
        }
        throw new Exception("Wrong OTP");
    }
}
