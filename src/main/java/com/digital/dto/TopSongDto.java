// TopSongDto.java
package com.digital.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSongDto {
    private String trackTitle;
    private String artistName;
    private double revenueInr;
}

