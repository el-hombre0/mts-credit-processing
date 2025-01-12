package com.example.creditservice.model.order;

import com.example.creditservice.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanOrder {
    private long id;
    private String orderId;
    private long userId;
    private long tariffId;
    private double creditRating;
    private OrderStatus status;
    private Timestamp timeInsert;
    private Timestamp timeUpdate;
}
