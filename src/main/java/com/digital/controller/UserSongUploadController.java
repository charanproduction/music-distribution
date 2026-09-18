package com.digital.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.SongRequest;
import com.digital.repository.UserRepository;
import com.digital.service.SongRequestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/upload")
@RequiredArgsConstructor
public class UserSongUploadController {

    private final SongRequestService songRequestService;
    private final UserRepository userRepo;

    @PostMapping
    public String upload(@RequestParam("title") String title,
                         @RequestParam("language") String language,
                         @RequestParam("genre") String genre,
                         @RequestParam("singer") String singer,
                         @RequestParam("lyricist") String lyricist,
                         @RequestParam("musicDirector") String musicDirector,
                         @RequestParam("releaseDate") LocalDate releaseDate,
                         @RequestParam("files") MultipartFile[] files,
                         Principal principal) throws Exception {

        String channel = userRepo.findByUsername(principal.getName())
                .get().getChannelName();

        String folder = channel + "_" + System.currentTimeMillis();
        Path uploadDir = Paths.get("uploads/" + folder);
        Files.createDirectories(uploadDir);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Files.copy(
                        file.getInputStream(),
                        uploadDir.resolve(file.getOriginalFilename()),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }

        SongRequest req = SongRequest.builder()
                .channelName(channel)
                .songName(title)
                //.language(language)
                //.genre(genre)
                .primaryArtist(singer)
                .secondaryArtist(singer)
                .lyricist(lyricist)
                .musicDirector(musicDirector)
                .releaseDate(releaseDate)
                .uploadedFolder(folder)
                .build();

        songRequestService.save(req);

        return "redirect:/user/requests?uploaded=true";
    }
}

