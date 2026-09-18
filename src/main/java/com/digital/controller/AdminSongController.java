package com.digital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.digital.service.SongUploadService;

@Controller
@RequestMapping("/admin/songs")
public class AdminSongController {

    private final SongUploadService songUploadService;

    public AdminSongController(SongUploadService songUploadService) {
        this.songUploadService = songUploadService;
    }

    @GetMapping("/upload")
    public String uploadSongsPage() {
        return "admin/upload-songs";
    }

    @PostMapping("/upload")
    public String handleSongUpload(@RequestParam("file") MultipartFile file,
                                   Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please upload a valid Excel file (.xlsx).");
            return "admin/upload-songs";
        }

        try {
            int count = songUploadService.importSongsFromExcel(file);
            model.addAttribute("success", count + " songs imported successfully.");
            model.addAttribute("songs", songUploadService.fetchAllSongs());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import songs: " + e.getMessage());
        }

        return "admin/upload-songs";
    }
}
