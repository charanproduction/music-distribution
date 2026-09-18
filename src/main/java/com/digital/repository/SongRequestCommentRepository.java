package com.digital.repository;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRequestCommentRepository extends JpaRepository<SongRequestComment, Long> {

    List<SongRequestComment> findBySongRequest_IdOrderByCreatedAtAsc(SongRequest request);
    //List<SongRequestComment> findBySongRequestOrderByCreatedAtAsc(SongRequest request);
    List<SongRequestComment> findBySongRequest_IdOrderByCreatedAtAsc(Long requestId);
}
