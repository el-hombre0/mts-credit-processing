package com.example.creditservice.service;

import com.example.creditservice.exception.CustomException;
import com.example.creditservice.model.enums.OrderStatus;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.order.LoanOrder;

import java.util.List;
import java.util.UUID;

public interface LoanOrderService {
    List<LoanOrder> findByUserId(long userId);
    UUID save(CreateOrder order) throws CustomException;
    OrderStatus getStatusByOrderId(UUID orderId);
    int deleteByOrderIdAndUserId(long userId, UUID orderId) throws CustomException;

    UUID saveFallback(final Throwable t);
    OrderStatus getStatusByOrderIdFallback(final Throwable t);
    int deleteByOrderIdAndUserIdFallback(final Throwable t);
}
