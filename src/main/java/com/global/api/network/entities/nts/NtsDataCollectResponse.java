package com.global.api.network.entities.nts;

import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
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
		ntsDataCollectResponse.setOriginalTransactionTime(sp.readInt(4));
		ntsDataCollectResponse.setOriginalTransactionDate(sp.readInt(6));
		ntsDataCollectResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
		ntsDataCollectResponse.setAccountNumber(sp.readString(19));
		ntsDataCollectResponse.setApprovalCode(sp.readString(6)); // Data collect
		ntsDataCollectResponse.setBatchNumber(sp.readInt(2));
		ntsDataCollectResponse.setSequenceNumber(sp.readInt(3));

		return ntsDataCollectResponse;
	}
}
