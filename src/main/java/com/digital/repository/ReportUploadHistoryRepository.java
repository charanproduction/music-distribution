package com.digital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.ReportUploadHistory;

public interface ReportUploadHistoryRepository extends JpaRepository<ReportUploadHistory, Long> {

    List<ReportUploadHistory> findAllByOrderByUploadedDateDesc();
    Optional<ReportUploadHistory> findByFileName(String fileName);
    Page<ReportUploadHistory> findByChannelName(String channelName, Pageable page);
    @Query("SELECT DISTINCT r.channelName FROM ReportUploadHistory r ORDER BY r.channelName ASC")
    List<String> findDistinctChannelNames();
    Page<ReportUploadHistory> findByFileNameContainingIgnoreCase(String filename, Pageable pageable);

    Page<ReportUploadHistory> findByChannelNameContainingIgnoreCase(String channel, Pageable pageable);

    Page<ReportUploadHistory> findByChannelNameContainingIgnoreCaseAndFileNameContainingIgnoreCase(
            String channel, String fileName, Pageable pageable);

    long countByFileNameContainingIgnoreCase(String filename);

    long countByChannelNameContainingIgnoreCase(String channel);

    long countByChannelNameContainingIgnoreCaseAndFileNameContainingIgnoreCase(
            String channel, String fileName);
    @Query("""
    	    SELECT r FROM ReportUploadHistory r
    	    WHERE LOWER(r.fileName) LIKE LOWER(CONCAT('%', :search, '%'))
    	""")
    	Page<ReportUploadHistory> searchByFile(@Param("search") String search,
    	                                       Pageable pageable);

    	@Query("""
    	    SELECT r FROM ReportUploadHistory r
    	    WHERE LOWER(r.fileName) LIKE LOWER(CONCAT('%', :search, '%'))
    	    AND r.channelName = :channel
    	""")
    	Page<ReportUploadHistory> searchByFileAndChannel(@Param("search") String search,
    	                                                 @Param("channel") String channel,
    	                                                 Pageable pageable);
    	@Query("""   			
    			select distinct qr.labelName from QuarterlyReport qr,
               ReportUploadHistory r where qr.reportId=r.quarterlyReportId""")
    	List<String> findAllChannels();
    	@Query("""   			
    			select distinct qr.labelName from QuarterlyReport qr,
               ReportUploadHistory r where qr.reportId=:reportId """)
    	List<String> findAllChannelsByReportId(Long reportId);
    

}
