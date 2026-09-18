package com.digital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySeriesPoint {
	private String month;
	private double inrValue;
	private double clientShare;
	private double companyShare;
}
