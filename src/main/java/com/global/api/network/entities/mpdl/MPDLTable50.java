package com.global.api.network.entities.mpdl;

import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class MPDLTable50 implements IMPDLTable {
    @Getter
    @Setter
    private Integer noOfCardTypes;

    @Getter
    @Setter
    private List<Card> cards;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setNoOfCardTypes(sp.readInt(2));

        this.cards = new ArrayList<>();
        for (int index = 0; index < this.noOfCardTypes; index++) {
            Card card = new Card();
            card.setCustomerCardType(sp.readString(2));
            card.setHostCardType(sp.readStringConstant(2, NTSCardTypes.class));
            card.setBinRangeCount(sp.readInt(3));
            List<BinRanges> binRanges = new ArrayList<>();
            for (int binIndex = 0; binIndex < card.getBinRangeCount(); binIndex++) {
                BinRanges range = new BinRanges();
                range.setBinStart(sp.readString(19));
                range.setDebitCapable(sp.readString(1));
                range.setBinEnd(sp.readString(19));
                binRanges.add(range);
            }
            card.setBinRanges(binRanges);
            this.cards.add(card);
        }
        return new MPDLTable(this);
    }

    @ToString
    public class Card {
        @Getter
        @Setter
        private String customerCardType;
        @Getter
        @Setter
        private NTSCardTypes hostCardType;
        @Getter
        @Setter
        private List<BinRanges> binRanges;
        @Getter
        @Setter
        private Integer binRangeCount;
    }

    @ToString
    public class BinRanges {
        @Getter
        @Setter
        private String binStart;
        @Getter
        @Setter
        private String debitCapable;
        @Getter
        @Setter
        private String binEnd;
    }
}
