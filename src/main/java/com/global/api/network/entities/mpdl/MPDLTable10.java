package com.global.api.network.entities.mpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class MPDLTable10 implements IMPDLTable {

    // START OF NETWORK DATA
    @Getter
    @Setter
    private String accessCode;
    @Getter
    @Setter
    private String primaryDialNumberOrPrimaryIpAddress;
    @Getter
    @Setter
    private String secondaryDialNumberOrSecondaryIpAddress;
    @Getter
    @Setter
    private String downloadNumberOrDownloadIpAddress;
    @Getter
    @Setter
    private String pollCode;
    @Getter
    @Setter
    private String unitOrLocationName;
    @Getter
    @Setter
    private String unitOrLocationAddress;
    @Getter
    @Setter
    private String unitOrLocationCity;
    @Getter
    @Setter
    private String unitOrLocationState;

    // START OF TABLE VERSIONS & FLAGS
    @Getter
    @Setter
    private String customerDiscretionaryTableVersion;
    @Getter
    @Setter
    private String customerDiscretionaryTableId30Flag;
    @Getter
    @Setter
    private String cardDataTableVersion;
    @Getter
    @Setter
    private String cardDataTableId40Flag;
    @Getter
    @Setter
    private String binRangeTableVersion;
    @Getter
    @Setter
    private String binRangeTableId50Flag;
    @Getter
    @Setter
    private String productDataTableVersion;
    @Getter
    @Setter
    private String productTableId60Flag;
    @Getter
    @Setter
    private String messageTableVersion;
    @Getter
    @Setter
    private String messageTableId70Flag;
    @Getter
    @Setter
    private String responseCodeTableVersion;
    @Getter
    @Setter
    private String responseCodeTableId80Flag;


    // START OF CUSTOMER LOCATION DISCRETIONARY DATA
    @Getter
    @Setter
    private Integer customerLocationDiscretionaryDataLength;
    @Getter
    @Setter
    private String zip;
    @Getter
    @Setter
    private String brand;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {

        // START OF NETWORK DATA
        this.setAccessCode(sp.readString(2));
        this.setPrimaryDialNumberOrPrimaryIpAddress(sp.readString(28));
        this.setSecondaryDialNumberOrSecondaryIpAddress(sp.readString(28));
        this.setDownloadNumberOrDownloadIpAddress(sp.readString(28));
        this.setPollCode(sp.readString(2));
        this.setUnitOrLocationName(sp.readString(20));
        this.setUnitOrLocationAddress(sp.readString(18));
        this.setUnitOrLocationCity(sp.readString(16));
        this.setUnitOrLocationState(sp.readString(2));

        // START OF TABLE VERSIONS & FLAGS
        this.setCustomerDiscretionaryTableVersion(sp.readString(3));
        this.setCustomerDiscretionaryTableId30Flag(sp.readString(1));
        this.setCardDataTableVersion(sp.readString(3));
        this.setCardDataTableId40Flag(sp.readString(1));
        this.setBinRangeTableVersion(sp.readString(3));
        this.setBinRangeTableId50Flag(sp.readString(1));
        this.setProductDataTableVersion(sp.readString(3));
        this.setProductTableId60Flag(sp.readString(1));
        this.setMessageTableVersion(sp.readString(3));
        this.setMessageTableId70Flag(sp.readString(1));
        this.setResponseCodeTableVersion(sp.readString(3));
        this.setResponseCodeTableId80Flag(sp.readString(1));

        // START OF CUSTOMER LOCATION DISCRETIONARY DATA
        this.setCustomerLocationDiscretionaryDataLength(sp.readInt(3));
        this.setZip(sp.readString(5));
        this.setBrand(sp.readString(1));
        return new MPDLTable(this);
    }
}
