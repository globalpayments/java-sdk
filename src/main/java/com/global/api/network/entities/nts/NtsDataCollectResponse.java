package com.global.api.network.entities.nts;

import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class NtsDataCollectResponse implements INtsResponseMessage {
	@Getter
	@Setter
	private	int authorizationResponseCode;
	@Getter
	@Setter
	private	int originalTransactionDate;
	@Getter
	@Setter
	private	int originalTransactionTime;
	@Getter
	@Setter
	private NTSCardTypes cardType;
	@Getter
	@Setter
	private	String accountNumber;
	@Getter
	@Setter
	private	String approvalCode;
	@Getter
	@Setter
	private	int batchNumber;
	@Getter
	@Setter
	private	int sequenceNumber;

	@Override
	public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
		NtsDataCollectResponse ntsDataCollectResponse = new NtsDataCollectResponse();
		StringParser sp = new StringParser(buffer);

		ntsDataCollectResponse.setAuthorizationResponseCode(sp.readInt(2));
		NtsUtils.log("Authorization Response Code", ntsDataCollectResponse.getAuthorizationResponseCode());

		ntsDataCollectResponse.setOriginalTransactionTime(sp.readInt(4));
		NtsUtils.log("Original Transaction Time", ntsDataCollectResponse.getOriginalTransactionTime());

		ntsDataCollectResponse.setOriginalTransactionDate(sp.readInt(6));
		NtsUtils.log("Original Transaction Date", ntsDataCollectResponse.getOriginalTransactionDate());

		ntsDataCollectResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
		NtsUtils.log("Card Type", ntsDataCollectResponse.getCardType());

		ntsDataCollectResponse.setAccountNumber(sp.readString(19));
		if(ntsDataCollectResponse.getAccountNumber() != null) {
			String actNumber = ntsDataCollectResponse.getAccountNumber().trim();
			if (!actNumber.isEmpty()) {
				NtsUtils.log("Account Number", StringUtils.maskAccountNumber(ntsDataCollectResponse.getAccountNumber()));
			}
		}

		ntsDataCollectResponse.setApprovalCode(sp.readString(6)); // Data collect
		NtsUtils.log("Approval Code", ntsDataCollectResponse.getApprovalCode());

		ntsDataCollectResponse.setBatchNumber(sp.readInt(2));
		NtsUtils.log("Batch Number", ntsDataCollectResponse.getBatchNumber());

		ntsDataCollectResponse.setSequenceNumber(sp.readInt(3));
		NtsUtils.log("Sequence Number", ntsDataCollectResponse.getSequenceNumber());


		return ntsDataCollectResponse;
	}
}
