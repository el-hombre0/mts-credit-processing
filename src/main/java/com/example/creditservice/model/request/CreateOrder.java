package com.example.creditservice.model.request;

import lombok.Data;

@Data
public class CreateOrder {
    private long userId;
    private long tariffId;
}
