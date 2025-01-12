package com.example.creditservice.model.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomError {
    private String code;
    private String message;
}
