package com.digital.entity;

import jakarta.persistence.*;

@Entity
public class ReportIdGenerator {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_id_gen")
    @SequenceGenerator(name = "report_id_gen", sequenceName = "report_id_seq", allocationSize = 1)
    private Long id;

    public Long getId() {
        return id;
    }
}
