package com.digital.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.digital.entity.Song;

public interface SongRepository extends JpaRepository<Song, Long> {
    long countByChannelName(String channelName);

    long countByChannelNameAndUploadDateBetween(String channelName,
                                                  LocalDate from,
                                                  LocalDate to);

    Page<Song> findByChannelNameOrderByUploadDateDesc(String channelName, Pageable pageable);
    
    long countByChannelNameIgnoreCase(String channelName);

    long countByChannelNameIgnoreCaseAndUploadDateGreaterThanEqual(
            String channelName,
            LocalDate fromDate
    );
    Page<Song> findByChannelNameIgnoreCase(String channelName, Pageable pageable);
    Page<Song> findByChannelName(String channelName, Pageable pageable);

}
