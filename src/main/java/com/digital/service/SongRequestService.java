package com.digital.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import com.digital.entity.User;
import com.digital.enums.SongRequestStatus;
import com.digital.repository.SongRequestCommentRepository;
import com.digital.repository.SongRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongRequestService {

	private final SongRequestRepository requestRepo;
	private final SongRequestCommentRepository commentRepo;

	@Value("${app.song-upload-dir:uploads/songs}")
	private String uploadRootDir;

	public Page<SongRequest> findUserRequests(String channelName, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		return requestRepo.findByChannelNameOrderByCreatedDateDesc(channelName, pageable);
	}

	public SongRequest createRequest(User user, String songName, String primaryArtist, String secondaryArtist,
			String lyricist, String musicDirector, String albumName, String genre, String songLanguage,
			LocalDate releaseDate, MultipartFile[] files) throws IOException {

		String channelName = user.getChannelName();
		LocalDate now = LocalDate.now();

		String folderName = channelName.replaceAll("[^a-zA-Z0-9_\\- ]", "_") + "_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

		Path root = Paths.get(uploadRootDir);
		Files.createDirectories(root);

		Path targetFolder = root.resolve(folderName);
		Files.createDirectories(targetFolder);

		if (files != null) {
			for (MultipartFile f : files) {
				if (!f.isEmpty()) {
					Files.copy(f.getInputStream(), targetFolder.resolve(f.getOriginalFilename()),
							StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}

		SongRequest req = SongRequest.builder().channelName(channelName).songName(songName).albumName(albumName)
				.primaryArtist(primaryArtist).secondaryArtist(secondaryArtist).lyricist(lyricist)
				.musicDirector(musicDirector).songLanguage(songLanguage).genre(genre).releaseDate(releaseDate)
				.createdDate(now).uploadedFolder(targetFolder.toString()).status(SongRequestStatus.PENDING_REVIEW)
				.build();

		return requestRepo.save(req);
	}

	public SongRequest getRequest(Long id) {
		return requestRepo.findById(id).orElseThrow();
	}

	public void userCancel(Long id, String reason, String username) {
		SongRequest req = getRequest(id);
		req.setStatus(SongRequestStatus.CANCELLED);
		requestRepo.save(req);

		addComment(req, "USER", username, "Cancelled: " + reason);
	}

	public void markForReview(Long id, String comment, String username) {
		SongRequest request = getRequest(id);

		if (!(request.getStatus() == SongRequestStatus.PENDING_REVIEW
				|| request.getStatus() == SongRequestStatus.ACTION_REQUIRED)) {
			throw new IllegalStateException("Not allowed to mark this request for review");
		}

		request.setStatus(SongRequestStatus.MARKED_FOR_REVIEW);
		requestRepo.save(request);

		if (comment != null && !comment.trim().isEmpty()) {
			addComment(request, "USER", username, comment);
		}
	}

	public List<SongRequestComment> getComments(Long requestId) {
		return commentRepo.findBySongRequest_IdOrderByCreatedAtAsc(requestId);
	}

	public SongRequestComment addComment(SongRequest req, String authorRole, String authorName, String message) {
		SongRequestComment c = SongRequestComment.builder().songRequest(req).authorRole(authorRole)
				.authorName(authorName).comment(message).build();
		return commentRepo.save(c);
	}

	public SongRequestComment addComment(Long requestId, String authorRole, String authorName, String message) {
		return addComment(getRequest(requestId), authorRole, authorName, message);
	}

	public Page<SongRequest> findAdminQueue(List<SongRequestStatus> statuses, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
		return requestRepo.findByStatusInOrderByCreatedDateDesc(statuses, pageable);
	}

	public void adminSetStatus(Long id, SongRequestStatus newStatus, String comment, String adminName) {

		SongRequest req = getRequest(id);
		req.setStatus(newStatus);
		requestRepo.save(req);

		if (comment != null && !comment.isBlank()) {
			addComment(req, "ADMIN", adminName, comment);
		}
	}

	public SongRequest getSongRequestForUser(Long id, String channel) {
		SongRequest req = requestRepo.findByIdAndChannelName(id, channel)
				.orElseThrow(() -> new RuntimeException("Unauthorized Request"));

		req.getComments().size(); // force load comments
		return req;
	}

	public void updateStatus(Long id, SongRequestStatus status) {
		SongRequest req = getRequest(id);
		req.setStatus(status);
		requestRepo.save(req);
	}

	public SongRequest get(Long id) {
		return requestRepo.findById(id).orElseThrow();
	}

	public SongRequest save(SongRequest req) {
		return requestRepo.save(req);
	}

	public void delete(Long id) {
		requestRepo.deleteById(id);
	}

	public SongRequest submitRequest(SongRequest req, MultipartFile[] files) throws IOException {

		req.setStatus(SongRequestStatus.PENDING_REVIEW);
		req.setCreatedDate(LocalDate.now());

		String folderName = req.getChannelName().replace(" ", "_") + "_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

		String uploadPath = "C:/Users/omm50/DigitalUploads/" + folderName;
		req.setUploadedFolder(uploadPath);

		File dir = new File(uploadPath);
		if (!dir.exists())
			dir.mkdirs();

		if (files != null) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					File dest = new File(dir, file.getOriginalFilename());
					file.transferTo(dest);
				}
			}
		}

		return requestRepo.save(req);
	}

	public void updateStatus(Long id, SongRequestStatus newStatus, String comment, String username, boolean isAdmin) {

		SongRequest req = requestRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

		req.setStatus(newStatus);
		requestRepo.save(req);

		if (comment != null && !comment.isBlank()) {
			addComment(req, isAdmin ? "ADMIN" : "USER", username, comment);
		}
	}
	
	public void updateSongRequest(Long id, SongRequest updated, String username) {
        SongRequest db = getSongRequestForUser(id, username);

        db.setSongName(updated.getSongName());
        db.setPrimaryArtist(updated.getPrimaryArtist());
        db.setSecondaryArtist(updated.getSecondaryArtist());
        db.setLyricist(updated.getLyricist());
        db.setMusicDirector(updated.getMusicDirector());
        db.setSongLanguage(updated.getSongLanguage());
        db.setGenre(updated.getGenre());
        db.setAlbumName(updated.getAlbumName());
        db.setReleaseDate(updated.getReleaseDate());

        requestRepo.save(db);
    }
}
