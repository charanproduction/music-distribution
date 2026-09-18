package com.digital.service;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

import com.digital.repository.MonthlyRevenueDetailsRepository;

@Service
public class MonthlyRevenueService {

    private final MonthlyRevenueDetailsRepository repo;

    public MonthlyRevenueService(MonthlyRevenueDetailsRepository repo) {
        this.repo = repo;
    }

    public BigDecimal totalRevenueCurrentQuarter() {
        YearMonth now = YearMonth.now();
        return sumNetRevenueForQuarter(now.getYear(), quarterOf(now.getMonthValue()));
    }

    public BigDecimal totalClientShareCurrentQuarter() {
        YearMonth now = YearMonth.now();
        return sumClientShareForQuarter(now.getYear(), quarterOf(now.getMonthValue()));
    }

    private int quarterOf(int month) {
        return (month - 1) / 3 + 1;
    }

    public BigDecimal sumNetRevenueForQuarter(int year, int quarter) {
        YearMonth from = YearMonth.of(year, (quarter - 1) * 3 + 1);  // Jan, Apr, Jul, Oct
        YearMonth to   = YearMonth.of(year, (quarter - 1) * 3 + 3);  // Mar, Jun, Sep, Dec
        return repo.sumNetRevenueLastQuarter(from.toString(), to.toString());
    }

    public BigDecimal sumClientShareForQuarter(int year, int quarter) {
        YearMonth from = YearMonth.of(year, (quarter - 1) * 3 + 1);
        YearMonth to   = YearMonth.of(year, (quarter - 1) * 3 + 3);
        return repo.sumClientShareLastQuarter(from.toString(), to.toString());
    }
}
