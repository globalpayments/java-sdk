package com.global.api.terminals.abstractions;

import java.math.BigDecimal;
import java.util.Map;

import com.global.api.entities.enums.SummaryType;
import com.global.api.terminals.SummaryResponse;

public interface ISAFResponse extends IDeviceResponse {
	Integer getTotalCount();
	BigDecimal getTotalAmount();
	Map<SummaryType, SummaryResponse> getApproved();
	Map<SummaryType, SummaryResponse> getPending();
	Map<SummaryType, SummaryResponse> getDeclined();

}
