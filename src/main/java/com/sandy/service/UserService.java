package com.sandy.service;


import com.sandy.domain.VerificationType;
import com.sandy.exception.UserException;
import com.sandy.model.User;


public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;
	
	public User findUserById(Long userId) throws UserException;

	public User verifyUser(User user) throws UserException;

	public User enabledTwoFactorAuthentication(VerificationType verificationType,
											   String sendTo, User user) throws UserException;

//	public List<User> getPenddingRestaurantOwner();

	User updatePassword(User user, String newPassword);

	void sendUpdatePasswordOtp(String email,String otp);

	User updateUserProfile(Long userId, User updatedUser) throws UserException;  // New method

//	void sendPasswordResetEmail(User user);
}
