package com.digital.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.PaymentDetails;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Long> {

    Optional<PaymentDetails> findByChannelNameIgnoreCaseAndYearAndQuarter(
            String channelName, Integer year, String quarter
    );
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentDetails p WHERE p.status = 'PAID'")
    BigDecimal findTotalPaidAmount();
    
    Optional<PaymentDetails> findByChannelNameAndYearAndQuarter(
            String channelName,
            Integer year,
            String quarter
    );

    List<PaymentDetails> findAllByChannelName(String channelName);

    void deleteByChannelNameAndYearAndQuarter(
            String channelName,
            Integer year,
            String quarter
    );
    
    @Query("""
    	    SELECT COALESCE(SUM(p.amount), 0)
    	    FROM PaymentDetails p
    	    WHERE LOWER(p.channelName) = LOWER(:channel)
    	      AND p.status = 'PAID'
    	""")
    	Double sumPaidAmountByChannel(@Param("channel") String channel);
    
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentDetails p
            WHERE LOWER(p.channelName) = LOWER(:channel)
            """)
     BigDecimal sumPaidAmountInrByChannel(String channel);
    
}
