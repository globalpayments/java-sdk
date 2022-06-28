package com.global.api.network.entities.gnap;

import com.global.api.network.enums.gnap.ISOResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor

 public abstract class GnapData {

        private GnapMessageHeader gnapMessageHeader;
        private GnapProdSubFids gnapProdSubFids;
        /** FID B - Transaction Amount */
        private BigDecimal transactionAmount;
        /** FID F - Approval Code */
        private String approvalCode;
        /** FID Q - Echo Data */
        private String echoData;
        /** FID h - Sequence Number */
        private SequenceNumber sequenceNumber;
        /** FID m - Day Totals */
        private GnapBatchTotal dayTotals;
       /** FID X - ISO Response Code */
       private ISOResponseCode isoResponseCode;
        /** FID 5 - Transaction Info */
        private String transactionInfo;

    }