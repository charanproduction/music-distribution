package com.digital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Existing User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "song_title", nullable = false)
    private String songTitle;

    private String language;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "production_label")
    private String productionLabel;

    @Column(length = 2000)
    private String prompt;

    /**
     * Absolute or relative path to generated poster file on disk
     */
    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "ai_generated")
    private Boolean aiGenerated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
