package com.example.creditservice.service;

import com.example.creditservice.model.tariff.Tariff;
import com.example.creditservice.model.request.TariffDTO;

import java.util.List;

public interface TariffService {
    List<Tariff> getTariffs();
    int save(TariffDTO tariffDTO);
    int deleteById(long id);

    List<Tariff> getTariffsFallback(final Throwable t);
}
