package com.digital.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelName;
    private String songName;
    private String language;
    private String genre;
    private String releaseQuarter;
    private LocalDate releaseDate;

    private String primarySinger;
    private String secondarySinger;
    private String lyricist;        // NEW
    private String musicDirector;   // NEW

    private String filePath;        // actual song file location

    @Column(updatable = false)
    private LocalDate uploadDate;   // NEW

    @PrePersist
    public void onCreate() {
        this.uploadDate = LocalDate.now();
    }
}

