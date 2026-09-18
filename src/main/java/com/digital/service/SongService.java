package com.digital.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.digital.repository.SongRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    public long countAll() {
        return songRepository.count();
    }

    public long countByChannel(String channelName) {
        if (channelName == null || channelName.isBlank()) {
            return 0L;
        }
        return songRepository.countByChannelNameIgnoreCase(channelName);
    }

    /**
     * Songs uploaded in the last 7 days for a channel.
     * Assumes Song has a LocalDate field "uploadDate".
     */
    public long countUploadedLastWeek(String channelName) {
        if (channelName == null || channelName.isBlank()) {
            return 0L;
        }
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return songRepository
                .countByChannelNameIgnoreCaseAndUploadDateGreaterThanEqual(channelName, sevenDaysAgo);
    }
}
