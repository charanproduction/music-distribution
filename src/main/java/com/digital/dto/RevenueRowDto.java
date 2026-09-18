package com.digital.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.entity.PaymentDetails;
import com.digital.entity.PaymentStatus;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class RevenueRowDto {

    // data coming from quarterly_revenue_details
    private Long id;
    private Integer year;
    private String quarter;
    private String salesMonth;
    private String channelName;
    private BigDecimal netRevenue;
    private BigDecimal inrValue;
    private BigDecimal clientShare;
    private BigDecimal companyShare;
    private BigDecimal payableAmount;

    // payment flags
    private boolean paid;              // true = already paid
    private Long paymentId;            // id from payment_details (if any)
    private BigDecimal paidAmount;
    // optional details for modal
    private String paymentMethod;
    private String paymentReference;
    private LocalDate paymentDate;        // you can keep it as String or LocalDate
    private PaymentStatus paymentStatus;

    /** Map from QuarterlyRevenueDetails to row DTO */
    public RevenueRowDto(QuarterlyRevenueDetails r) {
        this.id            = r.getId();
        this.year          = r.getYear();
        this.quarter       = r.getQuarter();
        this.salesMonth    = r.getSalesMonth();
        this.channelName   = r.getChannelName();
        this.inrValue      = r.getInrValue();
        this.clientShare   = r.getClientShare();
        this.companyShare  = r.getCompanyShare();
        this.payableAmount = r.getPayableAmount();
    }

    /** Convenience mapper for payment details (optional) */
    public void applyPayment(PaymentDetails p) {
        this.paid            = true;
        this.paymentId       = p.getId();
        this.paymentMethod   = p.getPaymentMethod();
        this.paymentReference= p.getPaymentReference();
        this.paymentStatus   = p.getStatus();
        this.paymentDate     = p.getPaymentDate() != null
                ? p.getPaymentDate()
                : null;
    }

	public RevenueRowDto(Long id, Integer year, String quarter, String salesMonth, String channelName,
			BigDecimal netRevenue, BigDecimal inrValue, BigDecimal clientShare, BigDecimal companyShare,
			BigDecimal payableAmount, boolean paid, Long paymentId, BigDecimal paidAmount, String paymentMethod,
			String paymentReference, LocalDate paymentDate, PaymentStatus paymentStatus) {
		super();
		this.id = id;
		this.year = year;
		this.quarter = quarter;
		this.salesMonth = salesMonth;
		this.channelName = channelName;
		this.netRevenue = netRevenue;
		this.inrValue = inrValue;
		this.clientShare = clientShare;
		this.companyShare = companyShare;
		this.payableAmount = payableAmount;
		this.paid = paid;
		this.paymentId = paymentId;
		this.paidAmount = paidAmount;
		this.paymentMethod = paymentMethod;
		this.paymentReference = paymentReference;
		this.paymentDate = paymentDate;
		this.paymentStatus = paymentStatus;
	}
}
