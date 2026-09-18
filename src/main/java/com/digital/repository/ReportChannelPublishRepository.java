package com.digital.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.ReportChannelPublish;
import com.digital.entity.ReportUploadHistory;

public interface ReportChannelPublishRepository extends JpaRepository<ReportChannelPublish, Long> {

    List<ReportChannelPublish> findByUploadId(Long uploadId);

    @Modifying
    @Query("""
        UPDATE ReportChannelPublish p
        SET p.published = true
        WHERE p.upload.id = :uploadId
        AND p.channelName IN :channels
    """)
    void publishForChannels(@Param("uploadId") Long uploadId,
                            @Param("channels") List<String> channels);
    
    @Query("SELECT r.channelName FROM ReportChannelPublish r WHERE r.upload.id = :uploadId")
    List<String> findChannelsByUpload_Id(@Param("uploadId") Long uploadId);

    @Query("SELECT r.channelName FROM ReportChannelPublish r WHERE r.upload.id = :uploadId")
    List<String> findPublishedChannels(@Param("uploadId") Long uploadId);
    
    boolean existsByUpload_IdAndChannelName(Long uploadId, String channel);

    @Query("""
        SELECT DISTINCT r.labelName
        FROM QuarterlyReport r
        WHERE r.reportId = :uploadId
    """)
    List<String> findDistinctLabelNameByUpload(@Param("uploadId") Long uploadId);

    List<ReportChannelPublish> findByUpload_Id(Long uploadId);
    void deleteByUploadId(Long uploadId);
    
    @Query("""
    		   SELECT r FROM ReportUploadHistory r
    		   JOIN ReportChannelPublish p ON p.upload.id = r.id
    		   WHERE p.channelName = :channel
    		   AND p.published = true
    		""")
    		List<ReportUploadHistory> findPublishedReportsForChannel(@Param("channel") String channel);
    
    @Query("""
 		   SELECT r FROM ReportChannelPublish  r
 		   JOIN ReportUploadHistory p ON p.id = r.upload.id
 		   WHERE r.channelName = :channel
 		   AND r.published = true
 		""")
 		List<ReportChannelPublish> findReportsForChannel(@Param("channel") String channel);
    
    @Query("""
    	    SELECT DISTINCT rcp.channelName 
    	    FROM ReportChannelPublish rcp 
    	    WHERE rcp.upload.id = :uploadId 
    	      AND rcp.published = true
    	""")
    	List<String> findPublishedLabelsForUpload(@Param("uploadId") Long uploadId);
    long countByUploadId(Long uploadId);

    Optional<ReportChannelPublish> findByUploadIdAndChannelName(Long uploadId, String channelName);
    
    @Query("""
    	    SELECT COUNT(DISTINCT rcp.channelName)
    	    FROM ReportChannelPublish rcp
    	    WHERE rcp.upload.id = :uploadId
    	""")
    	long countDistinctByUploadId(@Param("uploadId") Long uploadId);
}
