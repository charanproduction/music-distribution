package com.digital.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.MonthlyReport;

import jakarta.transaction.Transactional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    @Query("""
        SELECT r.labelName as label, r.reportingMonth as reportingMonth,
               r.salesMonth as salesMonth, SUM(r.netRevenue) as totalNet
          FROM MonthlyReport r
         GROUP BY r.labelName, r.reportingMonth, r.salesMonth
    """)
    List<MonthlyAggregation> aggregateByLabelAndMonth();

    interface MonthlyAggregation {
        String getLabel();
        String getReportingMonth();
        String getSalesMonth();
        BigDecimal getTotalNet();
    }
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MonthlyReport r WHERE r.reportingMonth BETWEEN :start AND :end")
    void deleteByYearAndQuarter( String start, String end);
    @Query("SELECT DISTINCT m.labelName FROM MonthlyReport m WHERE m.reportId = :reportId")
    List<String> findDistinctLabelNameByReportId(@Param("reportId") Long reportId);
}







