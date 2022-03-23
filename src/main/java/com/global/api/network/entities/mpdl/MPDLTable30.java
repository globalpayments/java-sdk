package com.global.api.network.entities.mpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class MPDLTable30 implements IMPDLTable {

    @Getter
    @Setter
    private Integer batchSize;
    @Getter
    @Setter
    private Integer resendDelay;
    @Getter
    @Setter
    private Integer batchResendDelay;
    @Getter
    @Setter
    private Integer batchVelocityInsideStandIn;
    @Getter
    @Setter
    private Integer batchVelocityOutsideStandIn;
    @Getter
    @Setter
    private Integer batchVelocityOutsideOnLine;
    @Getter
    @Setter
    private Integer idleTime;
    @Getter
    @Setter
    private Integer pumpTimeout;
    @Getter
    @Setter
    private String siteStandIn;
    @Getter
    @Setter
    private Integer standInLimitCount;
    @Getter
    @Setter
    private String standInLimitHours;
    @Getter
    @Setter
    private Integer storageLimit;
    @Getter
    @Setter
    private Integer batchCloseTimer;
    @Getter
    @Setter
    private String avsFlag;
    @Getter
    @Setter
    private String cvnFlag;
    @Getter
    @Setter
    private String svActivation;
    @Getter
    @Setter
    private Integer svChargeLow;
    @Getter
    @Setter
    private Integer svChargeHigh;
    @Getter
    @Setter
    private Integer cobrandVariable;
    @Getter
    @Setter
    private String debitPromptFlag;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setBatchSize(sp.readInt(2));
        this.setResendDelay(sp.readInt(3));
        this.setBatchResendDelay(sp.readInt(2));
        this.setBatchVelocityInsideStandIn(sp.readInt(1));
        this.setBatchVelocityOutsideStandIn(sp.readInt(1));
        this.setBatchVelocityOutsideOnLine(sp.readInt(1));
        this.setIdleTime(sp.readInt(2));
        this.setPumpTimeout(sp.readInt(2));
        this.setSiteStandIn(sp.readString(1));
        this.setStandInLimitCount(sp.readInt(5));
        this.setStandInLimitHours(sp.readString(4));
        this.setStorageLimit(sp.readInt(2));
        this.setBatchCloseTimer(sp.readInt(2));
        this.setAvsFlag(sp.readString(1));
        this.setCvnFlag(sp.readString(1));
        this.setSvActivation(sp.readString(1));
        this.setSvChargeLow(sp.readInt(3));
        this.setSvChargeHigh(sp.readInt(3));
        this.setCobrandVariable(sp.readInt(3));
        this.setDebitPromptFlag(sp.readString(1));
        return new MPDLTable(this);
    }
}
