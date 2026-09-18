//package com.digital.controller;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.digital.entity.Song;
//import com.digital.repository.SongRepository;
//
//@Controller
//@RequestMapping("/admin/songs")
//public class SongUploadController {
//
//    private final SongRepository songRepository;
//
//    public SongUploadController(SongRepository songRepository) {
//        this.songRepository = songRepository;
//    }
//
//    @PostMapping("/upload")
//    public String uploadSongs(@RequestParam("file") MultipartFile file,
//                              RedirectAttributes redirect) throws IOException {
//
//        try (InputStream is = file.getInputStream();
//             Workbook workbook = WorkbookFactory.create(is)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean first = true;
//            for (Row row : sheet) {
//                if (first) { // skip header
//                    first = false;
//                    continue;
//                }
//                if (row.getCell(0) == null) continue;
//
//                Song s = new Song();
//                s.setSongName(row.getCell(0).getStringCellValue());
//                s.setPrimaryArtist(row.getCell(1).getStringCellValue());
//                s.setSecondaryArtist(row.getCell(2).getStringCellValue());
//                s.setChannelName(row.getCell(3).getStringCellValue());
//                s.setLyricist(row.getCell(4).getStringCellValue());
//                s.setUploadDate(row.getCell(5).getLocalDateTimeCellValue().toLocalDate());
//                s.setReleaseDate(row.getCell(6).getLocalDateTimeCellValue().toLocalDate());
//                s.setReleaseQuarter(row.getCell(7).getStringCellValue());
//                songRepository.save(s);
//            }
//        }
//        redirect.addFlashAttribute("message", "Songs uploaded successfully");
//        return "redirect:/admin/songs/upload";
//    }
//}
