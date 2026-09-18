package com.digital.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "song_request_attachments", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongRequestAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_request_id")
    private SongRequest songRequest;

    private String originalFilename;
    private String storedFilename;
    private String relativePath;   // e.g. "uploads/song-requests/Charan_20251127_103000/file.mp3"

    private Long sizeBytes;
    private String contentType;
    private LocalDateTime uploadedAt;
}
