package org.example.orderservice.mapper;


import org.example.orderservice.domain.payment.PaymentEntity;
import org.example.orderservice.dto.payment.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(PaymentEntity paymentEntity);
}
