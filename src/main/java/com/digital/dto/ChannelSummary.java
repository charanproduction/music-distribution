package com.digital.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelSummary {
	public ChannelSummary(String ch, BigDecimal total) {
		// TODO Auto-generated constructor stub
	}
	private String channelName;
	private double clientShare;
	private double companyShare;
}
