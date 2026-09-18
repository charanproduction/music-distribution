package com.digital.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.entity.Song;
import com.digital.repository.SongRepository;
import com.digital.service.DashboardChartsService;
import com.digital.service.MonthlyRevenueService;
import com.digital.service.RevenueService;
import com.digital.service.SongService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final SongRepository songRepository;
    private final DashboardChartsService dashboardChartsService;
    private final SongService songService;
    private final RevenueService revenueService;

    public AdminDashboardController(SongRepository songRepository,
                                    DashboardChartsService dashboardChartsService, SongService songService, RevenueService revenueService) {
        this.songRepository = songRepository;
        this.dashboardChartsService = dashboardChartsService;
		this.songService = songService;
		this.revenueService = revenueService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "songName") String sortField,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            Model model) {

        // KPIs
    	long totalSongs = 0 ;
    	BigDecimal totalRevenue   = dashboardChartsService.getTotalRevenue();
        BigDecimal distributed    = dashboardChartsService.getTotalDistributed();
        BigDecimal profit         = dashboardChartsService.getProfit();
        if(dashboardChartsService.getTotalUniqueSongs() > songService.countAll()) {
        totalSongs =  dashboardChartsService.getTotalUniqueSongs();
        }else {
        totalSongs = songService.countAll();
        }
       

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("distributed", distributed);
        model.addAttribute("profit", profit);
        model.addAttribute("totalSongs", totalSongs);

        // Pagination + sorting
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, 10, sort);
        Page<Song> songPage = songRepository.findAll(pageable);

        model.addAttribute("songPage", songPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // Chart data: adds monthlyLabels, monthlyInrValues, quarterlyLabels, quarterlyInrValues,
        // channelLabels, channelClientShares
        dashboardChartsService.populateChartModel(model);

        // IMPORTANT: no leading slash
        return "admin/dashboard";
    }
    
    @GetMapping
    public String dashboard(Model model) {

        BigDecimal totalRevenue   = dashboardChartsService.getTotalRevenue();
        BigDecimal distributed    = dashboardChartsService.getTotalDistributed();
        BigDecimal profit         = dashboardChartsService.getProfit();

        long totalSongs           = songService.countAll();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("distributed", distributed);
        model.addAttribute("profit", profit);
        model.addAttribute("totalSongs", totalSongs);

        return "admin/dashboard";
    }
    
//    @GetMapping("/revenue")
//    public String revenuePage(
//            @RequestParam(required = false) String channel,
//            @RequestParam(required = false) Integer year,
//            @RequestParam(required = false) String quarter,
//            @RequestParam(required = false) String month,
//            @RequestParam(defaultValue = "0") int page,
//            Model model) {
//
//        // 🔥 Convert ALL → null
//    	String ch = (channel == null || channel.equals("") || channel.equalsIgnoreCase("ALL")) ? null : channel;
//    	Integer yr = (year == null || year == 0) ? null : year;
//    	String q = (quarter == null || quarter.equals("") || quarter.equalsIgnoreCase("ALL")) ? null : quarter;
//    	String m = (month == null || month.equals("") || month.equalsIgnoreCase("ALL")) ? null : month;
//
//        // 🔥 Fetch paginated results correctly
//        Page<QuarterlyRevenueDetails> revenuePage =
//                revenueService.searchRevenue(ch, yr, q, m, page);
//
//        model.addAttribute("revenueList", revenuePage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", revenuePage.getTotalPages());
//
//        // populate filters
//        model.addAttribute("channels", revenueService.allChannels());
//        model.addAttribute("years", revenueService.allYears());
//
//        return "admin/revenue";
//    }

}
