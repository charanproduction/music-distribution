package com.digital.repository;

import com.digital.entity.SongRequest;
import com.digital.enums.SongRequestStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRequestRepository extends JpaRepository<SongRequest, Long> {

    Page<SongRequest> findByChannelNameOrderByCreatedDateDesc(String channelName, Pageable pageable);

    Page<SongRequest> findByChannelNameAndStatusOrderByCreatedDateDesc(
            String channelName, SongRequestStatus status, Pageable pageable);

    Page<SongRequest> findByStatusOrderByCreatedDateDesc(SongRequestStatus status, Pageable pageable);

    long countByChannelName(String channelName);

    long countByChannelNameAndCreatedDateAfter(String channelName, java.time.LocalDateTime after);
    
    ///Page<SongRequest> findByChannelNameOrderByCreatedAtDesc(String channelName, Pageable pageable);

   // List<SongRequest> findByChannelNameOrderByUploadedAtDesc(String channelName);

    Page<SongRequest> findByStatusInOrderByCreatedDateDesc(List<SongRequestStatus> statuses, Pageable pageable);

    Page<SongRequest> findByChannelNameAndStatusInOrderByCreatedDateDesc(
            String channelName, List<SongRequestStatus> statuses, Pageable pageable);
    List<SongRequest> findByChannelNameIgnoreCase(String channelName);
    List<SongRequest> findByChannelNameOrderByCreatedDateDesc(String channelName);
    @Query("""
    	    SELECT r FROM SongRequest r
WHERE r.channelName = :channel
ORDER BY 
    CASE r.status
        WHEN com.digital.enums.SongRequestStatus.ACTION_REQUIRED THEN 1
        WHEN com.digital.enums.SongRequestStatus.PENDING_REVIEW THEN 2
        WHEN com.digital.enums.SongRequestStatus.MARKED_FOR_REVIEW THEN 3
        WHEN com.digital.enums.SongRequestStatus.REVIEW_COMPLETED THEN 4
        WHEN com.digital.enums.SongRequestStatus.READY_FOR_PUBLISHING THEN 5
        WHEN com.digital.enums.SongRequestStatus.PUBLISHED THEN 6
        WHEN com.digital.enums.SongRequestStatus.CANCELLED THEN 7
        ELSE 7
    END,
    r.createdDate DESC
    	    """)
    	Page<SongRequest> findByChannelSorted(
    	        @Param("channel") String channel,
    	        Pageable pageable
    	);
    
    @Modifying
    @Query("UPDATE SongRequest r SET r.status = :status, r.comments = :comments WHERE r.id = :id")
    void updateStatusAndComment(@Param("id") Long id,
                                @Param("status") SongRequestStatus status,
                                @Param("comments") String comments);
    
    Optional<SongRequest> findByIdAndChannelName(Long id, String channel);

}
