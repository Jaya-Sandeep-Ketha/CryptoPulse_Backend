package com.sandy.cryptopulse.CryptoPulse.repository;

import com.sandy.cryptopulse.CryptoPulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
