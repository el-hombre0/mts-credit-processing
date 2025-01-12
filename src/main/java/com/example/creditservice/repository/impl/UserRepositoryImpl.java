package com.example.creditservice.repository.impl;

import com.example.creditservice.model.user.User;
import com.example.creditservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String SELECT_BY_EMAIL = "SELECT * FROM USERS WHERE email = ?";
    private final String SELECT_BY_ID = "SELECT * FROM USERS WHERE ID = ?";
    private final String INSERT_INTO_TABLE = "INSERT INTO USERS (firstname, lastname, email, password, role) VALUES (?, ?, ?, ?, ?)";
    private final String DELETE_BY_ID = "DELETE FROM USERS WHERE ID = ?";

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            SELECT_BY_EMAIL,
                            new BeanPropertyRowMapper<>(User.class),
                            email
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            SELECT_BY_ID,
                            new BeanPropertyRowMapper<>(User.class),
                            id
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public int save(User user) {
        return jdbcTemplate.update(
            INSERT_INTO_TABLE,
            user.getFirstname(),
            user.getLastname(),
            user.getEmail(),
            user.getPassword(),
            user.getRole().toString()
        );
    }

    @Override
    public int delete(long id) {
        return jdbcTemplate.update(
                DELETE_BY_ID,
                id
        );
    }
}
