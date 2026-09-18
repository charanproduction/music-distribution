package com.digital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.entity.ReportPublishMapping;

public interface ReportPublishMappingRepository extends JpaRepository<ReportPublishMapping, Long> {

    List<ReportPublishMapping> findByChannelNameAndIsPublishedToClientTrue(String channel);

    Optional<ReportPublishMapping> findByReportId(Long reportId);
    List<ReportPublishMapping>findByIsPublishedToClientTrue();
}
