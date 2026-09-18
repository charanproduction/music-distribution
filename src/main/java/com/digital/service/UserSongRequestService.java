package com.digital.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.digital.entity.SongRequest;
import com.digital.repository.SongRequestRepository;
@Service
public class UserSongRequestService {
	private final SongRequestRepository repository;
	
	
	public UserSongRequestService(SongRequestRepository repository) {
		super();
		this.repository = repository;
	}


	public Page<SongRequest> getRequests(String channel, int page) {
	    return repository.findByChannelSorted(channel, PageRequest.of(page, 10));
	}
}
