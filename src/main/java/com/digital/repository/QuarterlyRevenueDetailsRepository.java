package com.digital.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.model.CountryRevenueSum;
import com.digital.model.SongRevenueSum;
import com.digital.model.YearQuarterSum;

import jakarta.transaction.Transactional;

public interface QuarterlyRevenueDetailsRepository extends JpaRepository<QuarterlyRevenueDetails, Long> {
	List<QuarterlyRevenueDetails> findByChannelNameAndYear(String channelName, Integer year);
	
	//List<QuarterlyRevenueDetails> findByChannelAndYear(String channel, Integer year);
	
	@Query("""
	        SELECT q.year, q.quarter, SUM(q.inrValue)
	        FROM QuarterlyRevenueDetails q
	        GROUP BY q.year, q.quarter
	        ORDER BY q.year ASC, q.quarter ASC
	    """)
	    List<Object[]> fetchQuarterlyChartData();
	
	   

	        @Query("""
	            SELECT DISTINCT q.channelName FROM QuarterlyRevenueDetails q ORDER BY q.channelName ASC
	        """)
	        List<String> fetchAllChannels();

	        @Query("""
	            SELECT DISTINCT q.year FROM QuarterlyRevenueDetails q ORDER BY q.year ASC
	        """)
	        List<Integer> fetchAllYears();
	        
	     // ---- ANALYTICS: MONTHLY (client share per month) ----
	        @Query("""
	            SELECT q.monthName, SUM(q.clientShare)
	            FROM QuarterlyRevenueDetails q
	            WHERE (:channel IS NULL OR q.channelName = :channel)
	              AND (:year IS NULL OR q.year = :year)
	            GROUP BY q.year, q.monthName
	            ORDER BY q.year ASC, q.monthName ASC
	        """)
	        List<Object[]> analyticsMonthlyChart(@Param("channel") String channel,
	                                             @Param("year") Integer year);

	        // ---- ANALYTICS: QUARTERLY (client share per quarter) ----
	        @Query("""
	            SELECT q.year, q.quarter, SUM(q.clientShare)
	            FROM QuarterlyRevenueDetails q
	            WHERE (:channel IS NULL OR q.channelName = :channel)
	              AND (:year IS NULL OR q.year = :year)
	            GROUP BY q.year, q.quarter
	            ORDER BY q.year ASC, q.quarter ASC
	        """)
	        List<Object[]> analyticsQuarterlyChart(@Param("channel") String channel,
	                                               @Param("year") Integer year);

	        // ---- ANALYTICS: YEARLY (client share per year) ----
	        @Query("""
	            SELECT q.year, SUM(q.clientShare)
	            FROM QuarterlyRevenueDetails q
	            WHERE (:channel IS NULL OR q.channelName = :channel)
	            GROUP BY q.year
	            ORDER BY q.year ASC
	        """)
	        List<Object[]> analyticsYearlyChart(@Param("channel") String channel);

	        // ---- ANALYTICS: TOP CHANNELS (top 5 by client share, optional year) ----
	        @Query("""
	            SELECT q.channelName, SUM(q.clientShare)
	            FROM QuarterlyRevenueDetails q
	            WHERE (:year IS NULL OR q.year = :year)
	            GROUP BY q.channelName
	            ORDER BY SUM(q.clientShare) DESC
	        """)
	        List<Object[]> topChannelsByYear(@Param("year") Integer year);

	        // ---- DRILLDOWN: rows per quarter ----
	        @Query("""
	            SELECT q
	            FROM QuarterlyRevenueDetails q
	            WHERE (:channel IS NULL OR q.channelName = :channel)
	              AND q.year = :year
	              AND q.quarter = :quarter
	            ORDER BY q.monthName ASC
	        """)
	        List<QuarterlyRevenueDetails> findQuarterDetails(@Param("channel") String channel,
	                                                         @Param("year") Integer year,
	                                                         @Param("quarter") String quarter);
	        
	        @Query("SELECT COALESCE(SUM(q.inrValue), 0) FROM QuarterlyRevenueDetails q")
	        BigDecimal findTotalRevenue();
	        
	        Page<QuarterlyRevenueDetails> findAll(Pageable pageable);

	        Page<QuarterlyRevenueDetails> findByChannelNameContainingIgnoreCaseAndYearAndQuarter(
	                String channelName, Integer year, String quarter, Pageable pageable);
	        
	        @Query("""
	        	       SELECT r FROM QuarterlyRevenueDetails r
	        	       WHERE (:channel IS NULL OR r.channelName = :channel)
	        	         AND (:year IS NULL OR r.year = :year)
	        	         AND (:quarter IS NULL OR r.quarter = :quarter)
	        	         AND (:month IS NULL OR r.salesMonth = :month)
	        	       """)
	        	Page<QuarterlyRevenueDetails> findRevenue(
	        	        String channel,
	        	        Integer year,
	        	        String quarter,
	        	        String month,
	        	        Pageable pageable);
	        @Modifying
	        @Transactional
	        @Query("DELETE FROM QuarterlyRevenueDetails r WHERE r.year = :year AND r.quarter = :quarter")
	       void deleteByYearAndQuarter(Integer year, String quarter);
	       
	       List<QuarterlyRevenueDetails> findByYearAndQuarter(Integer year, String quarter);
	       
	       @Query("""
	    	        SELECT COALESCE(SUM(q.clientShare), 0)
	    	        FROM QuarterlyRevenueDetails q
	    	        WHERE q.channelName = :channel
	    	    """)
	    	    BigDecimal totalClientShareForChannel(@Param("channel") String channel);

	    	    @Query("""
	    	        SELECT q.year AS year,
	    	               q.quarter AS quarter,
	    	               SUM(q.clientShare) AS clientShare
	    	        FROM QuarterlyRevenueDetails q
	    	        WHERE q.channelName = :channel
	    	        GROUP BY q.year, q.quarter
	    	        ORDER BY q.year DESC, q.quarter DESC
	    	    """)
	    	    List<Map<String, Object>> last5QuartersForChannel(@Param("channel") String channel);

	    	    @Query("""
	    	        SELECT DISTINCT q.year
	    	        FROM QuarterlyRevenueDetails q
	    	        ORDER BY q.year DESC
	    	    """)
	    	    List<Integer> uniqueYears();

	    	    @Query("""
	    	        SELECT DISTINCT q.quarter
	    	        FROM QuarterlyRevenueDetails q
	    	        ORDER BY q.quarter DESC
	    	    """)
	    	    List<String> uniqueQuarters();
	    	    List<QuarterlyRevenueDetails> findByChannelNameIgnoreCase(String channelName);
	    	    
	    	    List<QuarterlyRevenueDetails> findByChannelNameOrderByYearDesc(String channels);
	    	    List<QuarterlyRevenueDetails> findByChannelNameAndYearOrderByYearDesc(String channels, Integer year);
	    	    List<QuarterlyRevenueDetails> findByChannelNameAndYearAndQuarterOrderByYearDesc(
	    	    		String channels, Integer year, String quarter);
	    	    
	    	    @Query("""
	    	            SELECT COALESCE(SUM(q.netRevenue),0) 
	    	            FROM QuarterlyRevenueDetails q 
	    	            WHERE LOWER(q.channelName) = LOWER(:channel)
	    	        """)
	    	        Double sumNetRevenueByChannel(@Param("channel") String channel);

	    	        @Query("""
	    	            SELECT COUNT(DISTINCT q.trackTitle) 
	    	            FROM QuarterlyReport q 
	    	            WHERE LOWER(q.labelName) = LOWER(:channel)
	    	        """)
	    	        long countDistinctTrackByChannel(@Param("channel") String channel);

	    	        // For trend
	    	        @Query("""
	    	        	    SELECT q.year, q.quarter, SUM(q.clientShare)
	    	        	    FROM QuarterlyRevenueDetails q
	    	        	    WHERE LOWER(q.channelName) = LOWER(:channel)
	    	        	    GROUP BY q.year, q.quarter
	    	        	    ORDER BY q.year ASC, q.quarter ASC
	    	        	""")
	    	        	List<Object[]> findRevenueGroupedByYearQuarter(@Param("channel") String channel);

	    	        // Top songs
	    	        @Query("""
	    	            SELECT q.trackTitle, q.artistName, SUM(q.netRevenue)
	    	            FROM QuarterlyReport q
	    	            WHERE LOWER(q.labelName) = LOWER(:channel)
	    	            GROUP BY q.trackTitle, q.artistName
	    	            ORDER BY SUM(q.netRevenue) DESC
	    	        """)
	    	        List<Object[]> findTopSongsByChannel(@Param("channel") String channel, Pageable pageable);

	    	        // Country wise
	    	        @Query("""
	    	            SELECT  q.countryRegion, SUM(q.netRevenue)
	    	            FROM QuarterlyReport q
	    	            WHERE LOWER(q.labelName) = LOWER(:channel)
	    	            GROUP BY q.countryRegion
	    	            ORDER BY SUM(q.netRevenue) DESC
	    	        """)
	    	        List<Object[]> sumRevenueByCountry(@Param("channel") String channel);
	    	        
	    	        @Query("""
	    	                SELECT COALESCE(SUM(q.clientShare), 0)
	    	                FROM QuarterlyRevenueDetails q
	    	                WHERE LOWER(q.channelName) = LOWER(:channel)
	    	                """)
	    	         BigDecimal sumClientShareInrByChannel(String channel);	

}
