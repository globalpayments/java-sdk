package com.global.api.network.entities.mpdl;

import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class MPDLTable40 implements IMPDLTable{
    @Getter @Setter
    private Integer noOfCardTypes;

    @Getter @Setter
    private List<MPDLTable40Card> cards;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setNoOfCardTypes(sp.readInt(2));

        this.cards = new ArrayList<>();
        for(int index = 0; index < this.noOfCardTypes; index++) {
            MPDLTable40Card card = new MPDLTable40Card();
            card.setCustomerCardType(sp.readString(2));
            card.setHostCardType(sp.readStringConstant(2, NTSCardTypes.class));
            card.setPaymentDescription(sp.readString(9));
            card.setCobrandMsgFlag(sp.readString(1));
            card.setTimeOut(sp.readInt(2));
            card.setAcceptFlag(sp.readString(1));
            card.setManualEntry(sp.readString(1));
            card.setAuthAmount(sp.readInt(4));
            card.setPreAuthControlFlag(sp.readString(1));
            card.setPostEntryFlag(sp.readString(1));
            card.setSplitPaymentFlag(sp.readString(1));
            card.setRefundFlag(sp.readString(1));
            card.setAvsConfigFlag(sp.readString(1));
            card.setCvnConfigFlag(sp.readString(1));
            card.setStandInLimit(sp.readInt(3));
            card.setPreset$ForPump(sp.readInt(4));
            card.setSignatureLimit(sp.readInt(2));
            card.setAppliedDiscountFlag(sp.readString(1));
            card.setAmountPerGallonDiscount(sp.readString(4));
            card.setPercentDiscount(sp.readString(4));
            card.setCustomerDefined(sp.readString(1));
            this.cards.add(card);
        }
        return new MPDLTable(this);
    }
}
