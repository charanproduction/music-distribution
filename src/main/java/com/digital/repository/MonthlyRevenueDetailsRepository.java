package com.digital.repository;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.digital.entity.MonthlyRevenueDetails;

import jakarta.transaction.Transactional;

public interface MonthlyRevenueDetailsRepository extends JpaRepository<MonthlyRevenueDetails, Long> {

    @Query("SELECT COALESCE(SUM(m.netRevenue),0) FROM MonthlyRevenueDetails m " +
           "WHERE m.reportingMonth BETWEEN :from AND :to")
    BigDecimal sumNetRevenueLastQuarter(@Param("from") String from, @Param("to") String to);

    @Query("SELECT COALESCE(SUM(m.clientShare),0) FROM MonthlyRevenueDetails m " +
           "WHERE m.reportingMonth BETWEEN :from AND :to")
    BigDecimal sumClientShareLastQuarter(@Param("from") String from, @Param("to") String to);

    // NEW: filter for analytics
    List<MonthlyRevenueDetails> findByChannelNameAndReportingMonthStartingWith(
            String channelName, String reportingMonthPrefix);
    
    List<MonthlyRevenueDetails> findByChannelNameAndYear(String channelName, Integer year);

    // Optional but useful
    List<MonthlyRevenueDetails> findByChannelName(String channelName);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MonthlyRevenueDetails r WHERE r.year = :year AND r.salesMonth BETWEEN :start AND :end")
    void deleteMonthly(Integer year, String start, String end);
}
