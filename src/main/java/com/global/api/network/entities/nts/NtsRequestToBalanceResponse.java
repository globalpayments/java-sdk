package com.global.api.network.entities.nts;

import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsRequestToBalanceResponse implements INtsResponseMessage {
	@Getter
	@Setter
	private	int batchNumber;
	@Getter
	@Setter
	private	int totalTransaction;
	@Getter
	@Setter
	private	int totalSales;
	@Getter
	@Setter
	private	int totalReturns;
	@Getter
	@Setter
	private	int hostTransactionCount;
	@Getter
	@Setter
	private	int hostTotalSales;
	@Getter
	@Setter
	private	int hostTotalReturns;
	@Getter
	@Setter
	private NtsNetworkMessageHeader ntsResponse;

	@Override
	public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
		NtsRequestToBalanceResponse response = new NtsRequestToBalanceResponse();
		StringParser sp = new StringParser(buffer);

		response.setBatchNumber(sp.readInt(2));
		NtsUtils.log("Batch Number", response.getBatchNumber());

		response.setTotalTransaction(sp.readInt(3));
		NtsUtils.log("Total Transaction", response.getTotalTransaction());

		response.setTotalSales(sp.readInt(9));
		NtsUtils.log("Total Sales", response.getTotalSales());

		response.setTotalReturns(sp.readInt(9));
		NtsUtils.log("Total Return", response.getTotalReturns());

		response.setHostTransactionCount(sp.readInt(3));
		NtsUtils.log("Host Transaction Count", response.getHostTransactionCount());

		response.setHostTotalSales(sp.readInt(9));
		NtsUtils.log("Host Total Sales", response.getHostTotalSales());

		response.setHostTotalReturns(sp.readInt(9));
		NtsUtils.log("Host Total Return", response.getHostTotalReturns());

		return response;
	}
}
