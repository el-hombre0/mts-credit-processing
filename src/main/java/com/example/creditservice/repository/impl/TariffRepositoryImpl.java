package com.example.creditservice.repository.impl;

import com.example.creditservice.model.tariff.Tariff;
import com.example.creditservice.repository.TariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TariffRepositoryImpl implements TariffRepository {
    private final JdbcTemplate jdbcTemplate;
    private final String SELECT_ALL_FROM_TABLE = "select * from TARIFF";
    private final String SELECT_EXISTS_BY_ID = "SELECT EXISTS (select * from TARIFF where ID = ?)";
    private final String INSERT_INTO_TABLE = "insert into TARIFF (TYPE, INTEREST_RATE) values (?, ?)";
    private final String DELETE_FROM_TABLE = "delete from TARIFF where ID = ?";

    @Autowired
    public TariffRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Optional<List<Tariff>> findAll() {
        return Optional.of(
                jdbcTemplate.query(
                        SELECT_ALL_FROM_TABLE,
                        new BeanPropertyRowMapper<>(Tariff.class))
        );
    }

    @Override
    public Boolean existsById(long tariffId) {
        return jdbcTemplate.queryForObject(SELECT_EXISTS_BY_ID, Boolean.class, tariffId);
    }

    @Override
    public int save(Tariff tariff) {
        return jdbcTemplate.update(
                INSERT_INTO_TABLE,
                tariff.getType(),
                tariff.getInterestRate());
    }

    @Override
    public int delete(long id) {
        return jdbcTemplate.update(
                DELETE_FROM_TABLE,
                id
        );
    }
}
