package com.digital.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.Song;
import com.digital.repository.SongRepository;

@Service
public class SongUploadService {

    private final SongRepository songRepository;

    public SongUploadService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public int importSongsFromExcel(MultipartFile file) throws Exception {
        List<Song> songs = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Assuming first row is header
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                String songName       = getStringCell(row, 0);
                String primaryArtist  = getStringCell(row, 1);
                String secondaryArtist= getStringCell(row, 2);
                String lyricist       = getStringCell(row, 3);
                String musicDirector  = getStringCell(row, 4);
                String language       = getStringCell(row, 5);
                String genre          = getStringCell(row, 6);
                String channelName    = getStringCell(row, 7);
                
                LocalDate uploadDate  = getDateCell(row, 8);
                LocalDate releaseDate = getDateCell(row, 9);
                String releaseQuarter = getStringCell(row, 10);

                Song s = Song.builder()
                        .songName(songName)
                        .primarySinger(primaryArtist)
                        .secondarySinger(secondaryArtist)
                        .lyricist(lyricist)
                        .musicDirector(musicDirector)
                        .language(language)
                        .genre(genre)
                        .channelName(channelName)                        
                        .uploadDate(uploadDate)
                        .releaseDate(releaseDate)
                        .releaseQuarter(releaseQuarter)
                        .build();

                songs.add(s);
            }
        }

        songRepository.saveAll(songs);
        return songs.size();
    }

    public List<Song> fetchAllSongs() {
        return songRepository.findAll();
    }

    // ---------------- helpers ----------------

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int cn = 0; cn < 8; cn++) {
            Cell cell = row.getCell(cn);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getStringCell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        return "";
    }

    private LocalDate getDateCell(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue().trim();
            if (v.isEmpty()) return null;
            // Try simple ISO format
            try {
                return LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
