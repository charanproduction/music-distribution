package com.digital.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.QuarterlyReport;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.QuarterlyReportRepository.QuarterlyAggregation;

import jakarta.transaction.Transactional;

public interface QuarterlyReportRepository extends JpaRepository<QuarterlyReport, Long> {
    @Query("""
        SELECT r.labelName as label, r.reportingMonth as reportingMonth,
               r.salesMonth as salesMonth, SUM(r.netRevenue) as totalNet
          FROM QuarterlyReport r
         GROUP BY r.labelName, r.reportingMonth, r.salesMonth
    """)
    List<QuarterlyAggregation> aggregateByLabelAndMonth();

    interface QuarterlyAggregation {
        String getLabel();
        String getReportingMonth();
        String getSalesMonth();
        BigDecimal getTotalNet();
    }
    
    @Modifying
    @Transactional
    @Query("DELETE FROM QuarterlyReport r WHERE r.reportingMonth BETWEEN :start AND :end")
    void deleteByYearAndQuarter(String start, String end);
    List<QuarterlyReport> findByReportId(Long reportId);

    List<QuarterlyReport> findDistinctByReportIdAndLabelName(Long reportId, String channelName);

    List<QuarterlyReport> findByReportIdAndLabelName(Long reportId, String channel);

    boolean existsByReportIdAndLabelName(Long reportId, String channel);
    @Query("SELECT DISTINCT q.labelName FROM QuarterlyReport q WHERE q.reportId = :reportId")
    List<String> findDistinctLabelNameByReportId(@Param("reportId") Long reportId);
    
//    @Query("SELECT DISTINCT r.labelName FROM QuarterlyReport r WHERE r.upload.id = :uploadId")
//    List<String> findChannelsByUploadId(@Param("uploadId") Long uploadId);
    
    @Query("""
    	    SELECT DISTINCT r.labelName 
    	    FROM QuarterlyReport r 
    	    WHERE r.reportId = :uploadId
    	""")
    	List<String> findDistinctChannelsByReportId(@Param("uploadId") Long uploadId);

//    	@Modifying
//    	@Transactional
//    	@Query("""
//    	    UPDATE QuarterlyReport r 
//    	    SET r.published = true 
//    	    WHERE r.upload.id = :uploadId 
//    	    AND r.channelName IN :channels
//    	""")
//    	void publishRecordsForChannels(@Param("uploadId") Long uploadId,
//    	                               @Param("channels") List<String> channels);
    
    @Query("""
    	    SELECT DISTINCT q.labelName
    	    FROM QuarterlyReport q
    	    WHERE q.labelName IS NOT NULL 
    	      AND q.labelName <> ''
    	    ORDER BY q.labelName ASC
    	""")
        List<String> findDistinctLabelName();
    
    @Query("""
    	    SELECT DISTINCT q.labelName 
    	    FROM QuarterlyReport q 
    	    WHERE q.reportId = :uploadId
    	""")
    	List<String> findDistinctLabelsByUploadId(@Param("uploadId") Long uploadId);
    
    @Query("""
    	    SELECT COUNT(DISTINCT q.labelName)
    	    FROM QuarterlyReport q
    	    WHERE q.reportId = :uploadId
    	""")
    	long countDistinctLabelNamesByUploadId(@Param("uploadId") Long uploadId);
    
    @Query("""
            SELECT COUNT(DISTINCT q.releaseTitle)
            FROM QuarterlyReport q
            WHERE q.releaseTitle IS NOT NULL
            AND TRIM(q.releaseTitle) <> ''
        """)
        long countUniqueSongs();   
    

        @Query("""
            SELECT COUNT(DISTINCT LOWER(TRIM(q.artistName)))
            FROM QuarterlyReport q
            WHERE q.artistName IS NOT NULL
            AND TRIM(q.artistName) <> ''
        """)
        long countUniqueArtists();

        @Query("""
            SELECT COUNT(DISTINCT LOWER(TRIM(q.labelName)))
            FROM QuarterlyReport q
            WHERE q.labelName IS NOT NULL
            AND TRIM(q.labelName) <> ''
        """)
        long countUniqueLabels();

        @Query("""
            SELECT COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
        """)
        BigDecimal totalNetRevenue();

        @Query("""
            SELECT COALESCE(SUM(q.grossRevenue), 0)
            FROM QuarterlyReport q
        """)
        BigDecimal totalGrossRevenue();

        @Query("""
            SELECT q.platform, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.platform IS NOT NULL
            GROUP BY q.platform
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> revenueByPlatform();

        @Query("""
            SELECT q.countryRegion, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.countryRegion IS NOT NULL
            GROUP BY q.countryRegion
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> revenueByCountry();

        @Query("""
            SELECT q.releaseTitle, q.artistName, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.releaseTitle IS NOT NULL
            GROUP BY q.releaseTitle, q.artistName
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> topSongsByRevenue();

        @Query("""
            SELECT q.artistName, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.artistName IS NOT NULL
            GROUP BY q.artistName
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> topArtistsByRevenue();

        @Query("""
            SELECT q.labelName, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.labelName IS NOT NULL
            GROUP BY q.labelName
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> topLabelsByRevenue();

        @Query("""
            SELECT q.salesType, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.salesType IS NOT NULL
            GROUP BY q.salesType
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> revenueBySalesType();

        @Query("""
            SELECT q.subscriptionType, COALESCE(SUM(q.netRevenue), 0)
            FROM QuarterlyReport q
            WHERE q.subscriptionType IS NOT NULL
            GROUP BY q.subscriptionType
            ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
        """)
        List<Object[]> revenueBySubscriptionType();
        
        @Query("""
        	    SELECT COUNT(DISTINCT LOWER(TRIM(q.releaseTitle)))
        	    FROM QuarterlyReport q
        	    WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
        	      AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
        	      AND q.releaseTitle IS NOT NULL
        	      AND TRIM(q.releaseTitle) <> ''
        	""")
        	long countUniqueSongs(@Param("year") String year,
        	                      @Param("label") String label);

            @Query("""
                SELECT COUNT(DISTINCT LOWER(TRIM(q.artistName)))
                FROM QuarterlyReport q
                WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
                  AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
                  AND q.artistName IS NOT NULL
                  AND TRIM(q.artistName) <> ''
            """)
            long countUniqueArtists(@Param("year") String year,
                                    @Param("label") String label);

            @Query("""
                SELECT COALESCE(SUM(q.netRevenue), 0)
                FROM QuarterlyReport q
                WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
                  AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
            """)
            BigDecimal totalNetRevenue(@Param("year") String year,
                                       @Param("label") String label);

            @Query("""
                SELECT q.platform, COALESCE(SUM(q.netRevenue), 0)
                FROM QuarterlyReport q
                WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
                  AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
                  AND q.platform IS NOT NULL
                GROUP BY q.platform
                ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
            """)
            List<Object[]> revenueByPlatform(@Param("year") String year,
                                             @Param("label") String label);

            @Query("""
                SELECT q.countryRegion, COALESCE(SUM(q.netRevenue), 0)
                FROM QuarterlyReport q
                WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
                  AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
                  AND q.countryRegion IS NOT NULL
                GROUP BY q.countryRegion
                ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
            """)
            List<Object[]> revenueByCountry(@Param("year") String year,
                                            @Param("label") String label);

            @Query("""
                SELECT q.releaseTitle, q.artistName, COALESCE(SUM(q.netRevenue), 0)
                FROM QuarterlyReport q
                WHERE (:year IS NULL OR :year = '' OR q.reportingMonth LIKE CONCAT(:year, '%'))
                  AND (:label IS NULL OR :label = 'ALL' OR q.labelName = :label)
                  AND q.releaseTitle IS NOT NULL
                GROUP BY q.releaseTitle, q.artistName
                ORDER BY COALESCE(SUM(q.netRevenue), 0) DESC
            """)
            List<Object[]> topSongs(@Param("year") String year,
                                    @Param("label") String label);

            @Query("""
                SELECT DISTINCT q.labelName
                FROM QuarterlyReport q
                WHERE q.labelName IS NOT NULL
                  AND TRIM(q.labelName) <> ''
                ORDER BY q.labelName
            """)
            List<String> findDistinctLabels();

            @Query("""
                SELECT DISTINCT SUBSTRING(q.reportingMonth, 1, 4)
                FROM QuarterlyReport q
                WHERE q.reportingMonth IS NOT NULL
                ORDER BY SUBSTRING(q.reportingMonth, 1, 4) DESC
            """)
            List<String> findDistinctYears();
}