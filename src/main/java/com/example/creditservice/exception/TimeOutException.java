package com.example.creditservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeOutException extends RuntimeException{
    private String code;
    private String message;
}
