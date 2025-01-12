package com.example.creditservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DataResponseLoanOrder {
    UUID orderId;
}
