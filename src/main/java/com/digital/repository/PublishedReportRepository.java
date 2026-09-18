package com.digital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.entity.PublishedReport;

public interface PublishedReportRepository extends JpaRepository<PublishedReport, Long> {
    boolean existsByQuarterlyReportId(Long reportId);
    PublishedReport findByQuarterlyReportId(Long reportId);
}