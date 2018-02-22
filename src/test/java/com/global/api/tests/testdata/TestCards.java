package com.global.api.tests.testdata;

import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.paymentMethods.*;

public class TestCards {
    public static DebitTrackData asDebit(CreditTrackData card, String pinBlock) {
        DebitTrackData rvalue = new DebitTrackData();

        rvalue.setValue(card.getValue());
        rvalue.setEncryptionData(card.getEncryptionData());
        rvalue.setPinBlock(pinBlock);

        return rvalue;
    }

    public static EBTTrackData asEBT(CreditTrackData card, String pinBlock) {
        EBTTrackData rvalue = new EBTTrackData();
        rvalue.setValue(card.getValue());
        rvalue.setEntryMethod(card.getEntryMethod());
        rvalue.setEncryptionData(card.getEncryptionData());
        rvalue.setPinBlock(pinBlock);
        return rvalue;
    }

    public static EBTCardData asEBT(CreditCardData card, String pinBlock) {
        EBTCardData rvalue = new EBTCardData();
        rvalue.setNumber(card.getNumber());
        rvalue.setExpMonth(card.getExpMonth());
        rvalue.setExpYear(card.getExpYear());
        rvalue.setPinBlock(pinBlock);

        return rvalue;
    }

    public static CreditCardData VisaManual() {
        return VisaManual(false, false);
    }
    public static CreditCardData VisaManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("4012002000060016");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData VisaSwipe() {
        return VisaSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData VisaSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData VisaSwipeEncrypted() {
        return VisaSwipeEncrypted(EntryMethod.Swipe);
    }
    public static CreditTrackData VisaSwipeEncrypted(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("<E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
        rvalue.setEntryMethod(entryMethod);
        rvalue.setEncryptionData(EncryptionData.version1());
        return rvalue;
    }

    public static CreditCardData MasterCardManual() {
        return MasterCardManual(false, false);
    }
    public static CreditCardData MasterCardManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("5473500000000014");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditCardData MasterCardSeries2Manual() {
        return MasterCardSeries2Manual(false, false);
    }
    public static CreditCardData MasterCardSeries2Manual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("2223000010005798");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2019);
        rvalue.setCvn("988");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData MasterCardSwipe() {
        return MasterCardSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCardSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B5473500000000014^MC TEST CARD^251210199998888777766665555444433332?;5473500000000014=25121019999888877776?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData MasterCard24Swipe() {
        return MasterCard24Swipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCard24Swipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B2223000010005780^TEST CARD/EMV BIN-2^19121010000000009210?;2223000010005780=19121010000000009210?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData MasterCard25Swipe() {
        return MasterCard25Swipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCard25Swipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B2223000010005798^TEST CARD/EMV BIN-2^19121010000000003840?;2223000010005798=19121010000000003840?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData MasterCardSwipeEncrypted() {
        return MasterCardSwipeEncrypted(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCardSwipeEncrypted(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("&lt;E1052711%B5473501000000014^MC TEST CARD^251200000000000000000000000000000000?|GVEY/MKaKXuqqjKRRueIdCHPPoj1gMccgNOtHC41ymz7bIvyJJVdD3LW8BbwvwoenI+|+++++++C4cI2zjMp|11;5473501000000014=25120000000000000000?|8XqYkQGMdGeiIsgM0pzdCbEGUDP|+++++++C4cI2zjMp|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        rvalue.setEntryMethod(entryMethod);
        rvalue.setEncryptionData(EncryptionData.version1());
        return rvalue;
    }

    public static CreditCardData DiscoverManual() {
        return DiscoverManual(false, false);
    }
    public static CreditCardData DiscoverManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("6011000990156527");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData DiscoverSwipe() {
        return DiscoverSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData DiscoverSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B6011000990156527^DIS TEST CARD^25121011000062111401?;6011000990156527=25121011000062111401?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData DiscoverSwipeEncrypted() {
        return DiscoverSwipeEncrypted(EntryMethod.Swipe);
    }
    public static CreditTrackData DiscoverSwipeEncrypted(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("&lt;E1049711%B6011000000006527^DIS TEST CARD^25120000000000000000?|nqtDvLuS4VHJd1FymxBxihO5g/ZDqlHyTf8fQpjBwkk95cc6PG9V|+++++++C+LdWXLpP|11;6011000000006527=25120000000000000000?|8VfZvczP6iBqRis2XFypmktaipa|+++++++C+LdWXLpP|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        rvalue.setEntryMethod(entryMethod);
        rvalue.setEncryptionData(EncryptionData.version1());
        return rvalue;
    }

    public static CreditCardData AmexManual() {
        return AmexManual(false, false);
    }
    public static CreditCardData AmexManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("372700699251018");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("1234");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData AmexSwipe() {
        return AmexSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData AmexSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B3727 006992 51018^AMEX TEST CARD^2512990502700?;372700699251018=2512990502700?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditCardData JcbManual() {
        return JcbManual(false, false);
    }
    public static CreditCardData JcbManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("3566007770007321");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData JcbSwipe() {
        return JcbSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData JcbSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B3566007770007321^JCB TEST CARD^2512101100000000000000000064300000?;3566007770007321=25121011000000076435?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static GiftCard GiftCard1Swipe() {
        GiftCard rvalue = new GiftCard();
        rvalue.setTrackData("%B5022440000000000098^^391200081613?;5022440000000000098=391200081613?");
        return rvalue;
    }

    public static GiftCard GiftCard2Manual() {
        GiftCard rvalue = new GiftCard();
        rvalue.setNumber("5022440000000000007");
        return rvalue;
    }
}
