package com.digital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.digital.enums.SongRequestStatus;

@Entity
@Table(name = "song_requests", schema = "digital_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String songName;
    private String albumName;

    private String primaryArtist;     // NEW
    private String secondaryArtist;   // NEW
    
    private String channelName;
    private String lyricist;
    private String musicDirector;
    private String songLanguage;
    private String genre;
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    private SongRequestStatus status;

    private String uploadedFolder;
    @Column(name = "created_date")
    private LocalDate createdDate;
    @OneToMany(mappedBy = "songRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<SongRequestComment> comments;
}
