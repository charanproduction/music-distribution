package com.digital.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.digital.entity.User;
import com.digital.repository.UserRepository;
import com.digital.service.QuarterlyRevenueService;
import com.digital.service.QuarterlyRevenueService.QuarterRevenuePoint;
import com.digital.service.SongService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserRepository userRepo;
    private final SongService songService;
    private final QuarterlyRevenueService quarterlyRevenueService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        User user = userRepo.findByUsername(principal.getName())
                .orElseThrow();

        String channel = user.getChannelName();

        // Channel display
        model.addAttribute("channelName", channel);

        // KPI metrics
        long totalSongs = songService.countByChannel(channel);
        long songsLastWeek = songService.countUploadedLastWeek(channel);

        BigDecimal totalClientIncome =
                quarterlyRevenueService.totalClientShareForChannel(channel);

        List<QuarterRevenuePoint> last5 =
                quarterlyRevenueService.last5QuartersForChannel(channel);

        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("songsLastWeek", songsLastWeek);
        model.addAttribute("totalClientIncome", totalClientIncome);
        model.addAttribute("last5Quarters", last5);

//        // Chart labels/values
//        model.addAttribute(
//                "last5Labels",
//                last5.stream().map(QuarterRevenuePoint::label).toList()
//        );
//        model.addAttribute(
//                "last5Values",
//                last5.stream()
//                     .map(p -> p.clientShare() == null ? 0.0 : p.clientShare().doubleValue())
//                     .toList()
//        );

        return "user/dashboard";
    }
}
