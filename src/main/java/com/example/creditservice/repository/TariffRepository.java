package com.example.creditservice.repository;

import com.example.creditservice.model.tariff.Tariff;

import java.util.List;
import java.util.Optional;

public interface TariffRepository {
    Optional<List<Tariff>> findAll();
    Boolean existsById(long tariffId);
    int save(Tariff tariff);
    int delete(long id);
}
