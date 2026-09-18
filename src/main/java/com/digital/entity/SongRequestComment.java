package com.digital.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "song_request_comments", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongRequestComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_request_id")
    private SongRequest songRequest;

    private String authorName;   // username
    private String authorRole;   // "USER" / "ADMIN"

    @Column(length = 4000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

