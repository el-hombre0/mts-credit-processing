package com.example.creditservice.repository;

import com.example.creditservice.model.enums.OrderStatus;
import com.example.creditservice.model.order.LoanOrder;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanOrderRepository {
    Optional<List<LoanOrder>> findByUserId(long userId);
    Optional<LoanOrder> findByUserIdAndOrderId(long userId, UUID orderId);
    Optional<UUID> save(LoanOrder loanOrder);
    Optional<OrderStatus> getStatusByOrderId(UUID orderId);
    int deleteByUserIdAndOrderId(long userId, UUID orderId);
    Optional<List<LoanOrder>> findByStatus(OrderStatus status);
    void updateStatusByOrderId(OrderStatus statusToSet, Timestamp timeUpdate, long id);
}
