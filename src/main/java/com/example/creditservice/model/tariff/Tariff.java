package com.example.creditservice.model.tariff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tariff {
    private long id;
    private String type;
    private String interestRate;

}
