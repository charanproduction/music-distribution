package com.digital.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report_channel_publish",
        uniqueConstraints = @UniqueConstraint(columnNames = {"upload_id", "channel_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportChannelPublish {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    private ReportUploadHistory upload;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "published", nullable = false)
    private boolean published = true;
}
