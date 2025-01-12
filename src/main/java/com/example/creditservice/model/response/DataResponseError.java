package com.example.creditservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataResponseError<T> {
    private T error;
}
