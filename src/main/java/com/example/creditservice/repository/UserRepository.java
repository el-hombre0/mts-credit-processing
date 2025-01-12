package com.example.creditservice.repository;

import com.example.creditservice.model.user.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);
    int save(User user);
    int delete(long id);
}
