package com.global.api.paymentMethods;

import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.enums.ManualEntryMethod;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EBTCardData extends EBT implements ICardData {
    private String approvalCode;
    private boolean cardPresent;
    private String cvn;
    private CvnPresenceIndicator cvnPresenceIndicator;
    private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent;
    private String serialNumber;
    public ManualEntryMethod entryMethod;

    @Override
    public String getCardType() {
        return ebtCardType.toString();
    }

    @Override
    public void setCardType(String cardType) {
        ebtCardType = EbtCardType.valueOf(cardType);
    }

    public String getShortExpiry() {
        return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
    }

    public EBTCardData() {}
    public EBTCardData(EbtCardType cardType) {
        ebtCardType = cardType;
    }
}
