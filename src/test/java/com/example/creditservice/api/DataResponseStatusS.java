package com.example.creditservice.api;

import com.example.creditservice.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataResponseStatusS {
    OrderStatus orderStatus;
}
