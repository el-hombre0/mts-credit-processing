package com.example.creditservice.service.impl;

import com.example.creditservice.exception.CustomException;
import com.example.creditservice.exception.TimeOutException;
import com.example.creditservice.model.enums.OrderStatus;
import com.example.creditservice.model.request.CreateOrder;
import com.example.creditservice.model.order.LoanOrder;
import com.example.creditservice.repository.LoanOrderRepository;
import com.example.creditservice.repository.TariffRepository;
import com.example.creditservice.repository.UserRepository;
import com.example.creditservice.service.LoanOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanOrderServiceImpl implements LoanOrderService {
    private final LoanOrderRepository loanOrderRepository;
    private final TariffRepository tariffRepository;
    private final UserRepository userRepository;

    @Override
    public List<LoanOrder> findByUserId(long userId) {
        return loanOrderRepository.findByUserId(userId).orElseThrow();
    }

    @Override
    public UUID save(CreateOrder order) {
        userRepository.findById(order.getUserId()).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "Пользователь не найден"));

        LoanOrder loanOrder = new LoanOrder();
        loanOrder.setUserId(order.getUserId());
        loanOrder.setTariffId(order.getTariffId());

        if (tariffRepository.existsById(loanOrder.getTariffId())) {
            List<LoanOrder> loanOrderList = loanOrderRepository.findByUserId(loanOrder.getUserId()).orElseThrow();

            for (int i = 0; i < loanOrderList.size(); i++) {
                if (loanOrderList.get(i).getTariffId() == loanOrder.getTariffId()) {
                    switch (loanOrderList.get(i).getStatus()) {
                        case IN_PROGRESS -> {
                            log.error("LOAN_CONSIDERATION");
                            throw new CustomException("LOAN_CONSIDERATION", "Заявка на рассмотрении");
                        }
                        case APPROVED -> {
                            log.error("LOAN_ALREADY_APPROVED");
                            throw new CustomException("LOAN_ALREADY_APPROVED", "Заявка уже одобрена");
                        }
                        case REFUSED -> {
                            long now = new Timestamp(System.currentTimeMillis()).getTime();
                            if (now - loanOrderList.get(i).getTimeUpdate().getTime() < 1_000 * 2 * 60) {
                                log.error("TRY_LATER");
                                throw new CustomException("TRY_LATER", "Попробуйте позже");
                            }
                        }
                    }
                }
            }

            loanOrder.setOrderId(UUID.randomUUID().toString());
            loanOrder.setCreditRating(0.1 + Math.random() * 0.8);
            loanOrder.setStatus(OrderStatus.IN_PROGRESS);
            loanOrder.setTimeInsert(new Timestamp(System.currentTimeMillis()));
            return loanOrderRepository.save(loanOrder).orElseThrow();
        } else {
            log.error("TARIFF_NOT_FOUND");
            throw new CustomException("TARIFF_NOT_FOUND", "Тариф не найден");
        }
    }

    @Override
    public OrderStatus getStatusByOrderId(UUID orderId) {
        return loanOrderRepository.getStatusByOrderId(orderId).orElseThrow(() -> new CustomException("ORDER_NOT_FOUND", "Заявка не найдена"));
    }

    @Override
    public int deleteByOrderIdAndUserId(long userId, UUID orderId) {
        LoanOrder loanOrder = loanOrderRepository.findByUserIdAndOrderId(userId, orderId).orElseThrow(() -> new CustomException("ORDER_NOT_FOUND", "Заявка не найдена"));
        if (loanOrder.getStatus() == OrderStatus.IN_PROGRESS) {
            return loanOrderRepository.deleteByUserIdAndOrderId(userId, orderId);
        }

        throw new CustomException("ORDER_IMPOSSIBLE_TO_DELETE", "Невозможно удалить заявку");
    }

    @Override
    public UUID saveFallback(final Throwable t) {
        throw new TimeOutException("REQUEST_TIME_OUT: SAVE_ORDER", "Не удалось сохранить заявку. Превышено время ожидания");
    }

    @Override
    public OrderStatus getStatusByOrderIdFallback(final Throwable t) {
        throw new TimeOutException("REQUEST_TIME_OUT: GET_STATUS", "Не удалось получить статус заявки. Превышено время ожидания");
    }

    @Override
    public int deleteByOrderIdAndUserIdFallback(final Throwable t) {
        throw new TimeOutException("REQUEST_TIME_OUT: DELETE_ORDER", "Не удалось удалить удалить заявку. Превышено время ожидания");
    }
}
