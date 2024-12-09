package com.sandy.cryptopulse.CryptoPulse.controller;

import com.sandy.cryptopulse.CryptoPulse.domain.VerificationType;
import com.sandy.cryptopulse.CryptoPulse.model.ForgetPasswordToken;
import com.sandy.cryptopulse.CryptoPulse.model.User;
import com.sandy.cryptopulse.CryptoPulse.model.VerificationCode;
import com.sandy.cryptopulse.CryptoPulse.response.ApiResponse;
import com.sandy.cryptopulse.CryptoPulse.response.AuthResponse;
import com.sandy.cryptopulse.CryptoPulse.response.ForgetPasswordTokenRequest;
import com.sandy.cryptopulse.CryptoPulse.response.ResetPasswordRequest;
import com.sandy.cryptopulse.CryptoPulse.service.EmailService;
import com.sandy.cryptopulse.CryptoPulse.service.ForgetPasswordService;
import com.sandy.cryptopulse.CryptoPulse.service.UserService;
import com.sandy.cryptopulse.CryptoPulse.service.VerificationCodeService;
import com.sandy.cryptopulse.CryptoPulse.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private ForgetPasswordService forgetPasswordService;

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

    @PostMapping("/auth/users/reset-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgetPasswordOtp(@RequestBody ForgetPasswordTokenRequest req) throws Exception {
        User user = userService.findUserByEmail(req.getSendTo());
        String otp = OtpUtils.generateOtp();
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        ForgetPasswordToken token = forgetPasswordService.findByUser(user.getId());

        if(token==null){
            token= forgetPasswordService.createToken(user, id, otp, req.getVerificationType(), req.getSendTo());
        }
        if(req.getVerificationType()==VerificationType.EMAIL){
            emailService.sendVerificationOtpEmail(user.getEmail(), token.getOtp());
        }
        AuthResponse response = new AuthResponse();
        response.setSession(token.getId());
        response.setMessage("Password reset otp sent successfully");

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/auth/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String id, @RequestBody ResetPasswordRequest req, @RequestHeader("Authorization") String jwt) throws Exception {
        ForgetPasswordToken forgetPasswordToken = forgetPasswordService.findById(id);
        boolean isVerified = forgetPasswordToken.getOtp().equals(req.getOtp());
        if(isVerified){
            userService.updatePassword(forgetPasswordToken.getUser(), req.getPassword());
            ApiResponse res = new ApiResponse();
            res.setMessage("Password Updated Successfully");
            return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
        }
        throw new Exception("Wrong OTP");
    }
}
