package com.digital.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import com.digital.repository.SongRequestCommentRepository;
import com.digital.repository.SongRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongRequestCommentService {

	private final SongRequestCommentRepository repo;
	private final SongRequestRepository songRequestRepository;

	public List<SongRequestComment> getComments(Long reqId) {
		return repo.findBySongRequest_IdOrderByCreatedAtAsc(reqId);
	}

	public SongRequestComment addComment(Long requestId, String message, String username, boolean isAdmin) {

		SongRequest request = songRequestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));

		SongRequestComment comment = SongRequestComment.builder().songRequest(request).authorName(username)
				.authorRole(isAdmin ? "ADMIN" : "USER").comment(message).build();

		return repo.save(comment);
	}
}
