package com.digital.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YearQuarterSum {
	private Integer year;
	private String quarter;
	private Double clientShare;	
	public String getLabel() {
        return year + "-Q" + quarter;
    }
	
}
