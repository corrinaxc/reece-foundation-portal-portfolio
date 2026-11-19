package com.reecefoundation.reecefoundation.repository;

import com.reecefoundation.reecefoundation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByEmail(String email);
}
