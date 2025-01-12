package com.example.creditservice.model.response;

import com.example.creditservice.model.tariff.Tariff;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataResponseTariff {
    private List<Tariff> tariffs;
}
