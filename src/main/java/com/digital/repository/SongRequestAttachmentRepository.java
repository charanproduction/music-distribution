package com.digital.repository;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRequestAttachmentRepository extends JpaRepository<SongRequestAttachment, Long> {

    List<SongRequestAttachment> findBySongRequest(SongRequest request);

    void deleteBySongRequest(SongRequest request);
}
