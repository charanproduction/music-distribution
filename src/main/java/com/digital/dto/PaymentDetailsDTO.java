package com.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.digital.entity.PaymentDetails;
import com.digital.entity.PaymentStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentDetailsDTO {

    private Long id;
    private String channelName;
    private Integer year;
    private String quarter;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentReference;
    private LocalDate paymentDate;
    private PaymentStatus status;

    public PaymentDetailsDTO(PaymentDetails p) {
        this.id = p.getId();
        this.channelName = p.getChannelName();
        this.year = p.getYear();
        this.quarter = p.getQuarter();
        this.amount = p.getAmount();
        this.paymentMethod = p.getPaymentMethod();
        this.paymentReference = p.getPaymentReference();
        this.paymentDate = p.getPaymentDate();
        this.status = p.getStatus();
    }
}
