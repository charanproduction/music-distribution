package com.digital.controller;

import com.digital.entity.Poster;
import com.digital.entity.User;
import com.digital.repository.PosterRepository;
import com.digital.repository.UserRepository;
import com.digital.service.PosterService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/posters")
@RequiredArgsConstructor
public class PosterController {

    private final UserRepository userRepository;
    private final PosterRepository posterRepository;
    private final PosterService posterService;

    @GetMapping
    public String userPosters(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        List<Poster> posters = posterRepository.findByUserOrderByCreatedAtDesc(user);
        model.addAttribute("posters", posters);

        return "user/posters";
    }

    /**
     * Single form endpoint:
     *  - If user uploads images + prompt => assets + AI + composition
     *  - If user only provides prompt => pure AI poster
     */
    @PostMapping("/generate")
    public String generatePoster(@RequestParam("songTitle") String songTitle,
                                 @RequestParam(required = false) String language,
                                 @RequestParam(required = false) String artistName,
                                 @RequestParam(required = false) String channelName,
                                 @RequestParam(required = false) String productionLabel,
                                 @RequestParam(required = false, name = "prompt") String prompt,
                                 @RequestParam(required = false, name = "artistImage") MultipartFile artistImage,
                                 @RequestParam(required = false, name = "channelLogo") MultipartFile channelLogo,
                                 @RequestParam(required = false, name = "productionLogo") MultipartFile productionLogo,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow();

        posterService.generatePoster(
                user,
                songTitle,
                language,
                artistName,
                channelName,
                productionLabel,
                prompt,
                artistImage,
                channelLogo,
                productionLogo
        );

        redirectAttributes.addFlashAttribute("success", "Poster generated successfully!");
        return "redirect:/user/posters";
    }

    /**
     * Serve poster image from stored file path.
     */
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> servePosterImage(@PathVariable Long id,
                                                     Principal principal) throws Exception {

        Poster poster = posterRepository.findById(id)
                .orElseThrow();

        // Optional: enforce that only owner (or admin) can view
        if (!poster.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Path path = Paths.get(poster.getImagePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
