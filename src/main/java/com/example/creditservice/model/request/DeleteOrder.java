package com.example.creditservice.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class DeleteOrder {
    private long userId;
    private UUID orderId;
}
