package com.example.creditservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataResponse<T> {
    private T data;
}
