package com.global.api.network.entities.nts;

import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.NtsMessageCode;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
public class NtsDataCollectRequest {
    @Getter
    @Setter
    private EntryMethod entryMthod;
    @Getter
    @Setter
    private NTSCardTypes cardType;
    @Getter
    @Setter
    private String debitAuthorizer;
    @Getter
    @Setter
    private String accountNumber;
    @Getter
    @Setter
    private String expirationDate;
    @Getter
    @Setter
    private String approvalCode;
    @Getter
    @Setter
    private AuthorizerCode authorizer;
    @Getter
    @Setter
    private BigDecimal amount;
    @Getter
    @Setter
    private NtsMessageCode messageCode;
    @Getter
    @Setter
    private String authorizationResponseCode;
    @Getter
    @Setter
    private String originalTransactionDate;
    @Getter
    @Setter
    private String originalTransactionTime;
    @Getter
    @Setter
    private int batchNumber;
    @Getter
    @Setter
    private int sequenceNumber;

    /**
     * This constructor prepares the remaining part of data-collect message based on the response
     *
     * @param messageCode : Message code [ data-collect, credit_adjustment]
     * @param response    : Nts response received form the transaction
     * @param amount      : approved amount for data-collect request
     */
    public NtsDataCollectRequest(NtsMessageCode messageCode, Transaction response, BigDecimal amount) {
        this.messageCode = messageCode;
        this.amount = amount;
        this.authorizationResponseCode = response.getResponseCode();
        this.setAuthorizerAndApprovalCode(response);
    }

    private void setAuthorizerAndApprovalCode(Transaction response) {
        INtsResponseMessage ntsResponse = response.getNtsResponse().getNtsResponseMessage();
        if (ntsResponse instanceof NtsSaleCreditResponseMapper) {
            this.authorizer = ((NtsSaleCreditResponseMapper) ntsResponse).getCreditMapper().getAuthorizer();
            this.approvalCode = ((NtsSaleCreditResponseMapper) ntsResponse).getCreditMapper().getApprovalCode();
            this.debitAuthorizer = response.getResponseCode();
        } else if (ntsResponse instanceof NtsAuthCreditResponseMapper) {
            this.authorizer = ((NtsAuthCreditResponseMapper) ntsResponse).getCreditMapper().getAuthorizer();
            this.approvalCode = ((NtsAuthCreditResponseMapper) ntsResponse).getCreditMapper().getApprovalCode();
            this.debitAuthorizer = response.getResponseCode();
        } else if (ntsResponse instanceof NtsDebitResponse) {
            this.authorizer = AuthorizerCode.Terminal_Authorized;
            this.approvalCode = ((NtsDebitResponse) ntsResponse).getCode();
            this.debitAuthorizer = ((NtsDebitResponse) ntsResponse).getAuthorizerCode().getValue();
        } else if (ntsResponse instanceof NtsEbtResponse) {
            this.authorizer = AuthorizerCode.Terminal_Authorized;
            this.approvalCode = ((NtsEbtResponse) ntsResponse).getApprovalCode();
            this.debitAuthorizer = ((NtsEbtResponse) ntsResponse).getAuthorizerCode().getValue();

        }
    }
}
