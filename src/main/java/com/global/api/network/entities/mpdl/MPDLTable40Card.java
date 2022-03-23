package com.global.api.network.entities.mpdl;

import com.global.api.network.enums.NTSCardTypes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class MPDLTable40Card {
    @Getter
    @Setter
    private String customerCardType;
    @Getter
    @Setter
    private NTSCardTypes hostCardType;
    @Getter
    @Setter
    private String paymentDescription;
    @Getter
    @Setter
    private String cobrandMsgFlag;
    @Getter
    @Setter
    private Integer timeOut;
    @Getter
    @Setter
    private String acceptFlag;
    @Getter
    @Setter
    private String manualEntry;
    @Getter
    @Setter
    private Integer authAmount;
    @Getter
    @Setter
    private String preAuthControlFlag;
    @Getter
    @Setter
    private String postEntryFlag;
    @Getter
    @Setter
    private String splitPaymentFlag;
    @Getter
    @Setter
    private String refundFlag;
    @Getter
    @Setter
    private String avsConfigFlag;
    @Getter
    @Setter
    private String cvnConfigFlag;
    @Getter
    @Setter
    private Integer standInLimit;
    @Getter
    @Setter
    private Integer preset$ForPump;
    @Getter
    @Setter
    private Integer signatureLimit;
    @Getter
    @Setter
    private String appliedDiscountFlag;
    @Getter
    @Setter
    private String amountPerGallonDiscount;
    @Getter
    @Setter
    private String percentDiscount;
    @Getter
    @Setter
    private String customerDefined;
}
