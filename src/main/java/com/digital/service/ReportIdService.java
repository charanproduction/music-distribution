package com.digital.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.digital.entity.ReportIdGenerator;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class ReportIdService {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Long getNextReportId() {
        ReportIdGenerator idGen = new ReportIdGenerator();
        entityManager.persist(idGen);
        entityManager.flush();
        return idGen.getId();
    }
}

