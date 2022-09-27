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

    /*
    VISA
    */

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

    public static CreditCardData VisaManualEncrypted() {
        return VisaManualEncrypted(true);
    }
    public static CreditCardData VisaManualEncrypted(boolean cardPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("4012005997950016");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2020);
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(true);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0", "2"));
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

    public static CreditTrackData VisaSwipeEncryptedV2() {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("4012007060016=2512101eaN0ZqMIGA5/9Dpe");
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0", "2"));
        return rvalue;
    }

    public static CreditCardData VisaCorporateManual() {
        return VisaCorporateManual(false, false);
    }
    public static CreditCardData VisaCorporateManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("4013872718148777");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData VisaCorporateSwipe() {
        return VisaCorporateSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData VisaCorporateSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B4013872718148777^VISA TEST CARD/GOOD^2512101?;4013872718148777=1712101?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditCardData VisaPurchasingManual() {
        return VisaPurchasingManual(false, false);
    }
    public static CreditCardData VisaPurchasingManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("4484104292153662");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData VisaPurchasingSwipe() {
        return VisaPurchasingSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData VisaPurchasingSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B4484104292153662^POSINT TEST VISA P CARD^2512501032100321001000?;4484104292153662=18035010321?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditCardData VisaFleetManual() {
        return VisaFleetManual(false, false);
    }
    public static CreditCardData VisaFleetManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("4484630000000126");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData VisaFleetSwipe() {
        return VisaFleetSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData VisaFleetSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B4484630000000126^VISA TEST CARD/GOOD^25121019206100000001?;4484630000000126=16111019206100000001?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    /*
    MASTERCARD
    */

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

    public static CreditCardData MasterCardManualEncrypted() {
        return MasterCardManualEncrypted(true);
    }
    public static CreditCardData MasterCardManualEncrypted(boolean cardPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("5473500844750014");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2020);
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(true);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0"));
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

    public static CreditCardData MasterCardPurchasingManual() {
        return MasterCardPurchasingManual(false, false);
    }
    public static CreditCardData MasterCardPurchasingManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("5302490000004066");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2025);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData MasterCardPurchasingSwipe() {
        return MasterCardPurchasingSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCardPurchasingSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B5302490000004066^MASTERCARD TEST^25121011234567890123?;5302490000004066=18121011234567890123?");
        rvalue.setEntryMethod(entryMethod);
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

    public static CreditTrackData MasterCard24Swipe() {
        return MasterCard24Swipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCard24Swipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B2223000010005780^TEST CARD/EMV BIN-2^19121010000000009210?;2223000010005780=19121010000000009210?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    public static CreditTrackData MasterCardSeries2SwipeEncryptedv2() {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("2223005065780=19121016wImiKusZcUl9y0M");
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wEJTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0pg982v/qeMw/IXYS8nlEM8CHRh/MnT3lRDC3VRkU2+F25m8icv5Whf4eMUVATDPF+dwiOoMbnOXpXuYh5awgKiwlqNIfLE2VeegqCzdbj7gzUE6GZTLmmxrd9/BPTEh3DTlVQ8igbZT8xHyzfwvsZ1ZQChcdqmHamuHBm7RJ4bIAUKetgNpAplB6GFxm+ynOOiNI/GxCB3Mre5vLqypCvoWWAnD0", "2"));
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

    public static CreditTrackData MasterCardSwipeEncryptedV2() {
        return MasterCardSwipeEncryptedV2(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCardSwipeEncryptedV2(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("5473507060014=2512101Bc3ZFrxvoqak");
        rvalue.setEntryMethod(entryMethod);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0", "2"));
        return rvalue;
    }

    public static CreditCardData MasterCardFleetManual() {
        return MasterCardFleetManual(false, false);
    }
    public static CreditCardData MasterCardFleetManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData card = new CreditCardData();
        card.setNumber("5567300000000016");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(cardPresent);
        card.setReaderPresent(readerPresent);

        return card;
    }

    public static CreditTrackData MasterCardFleetSwipe() {
        return MasterCardFleetSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData MasterCardFleetSwipe(EntryMethod entryMethod) {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B5567300000000016^MASTERCARD FLEET          ^2512101777766665555444433332111?;5567300000000016=25121019999888877711?");
        track.setEntryMethod(entryMethod);

        return track;
    }

    /*
    DISCOVER
    */

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

    public static CreditCardData DiscoverManualEncrypted() {
        return DiscoverManualEncrypted(true);
    }
    public static CreditCardData DiscoverManualEncrypted(boolean cardPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("6011005612796527");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2019);
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(true);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0", "2"));
        return rvalue;

    }

    public static CreditTrackData DiscoverSwipe() {
        return DiscoverSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData DiscoverSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%B6011000990156527^DIS TEST CARD^25121011000062111401?");
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

    public static CreditTrackData DiscoverSwipeEncryptedV2() {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("B6011006066527^DIS TEST CARD^2512101+i2dm9dOIVKMmznP");
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4wELTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0g2G9fXumxd48J9FbkaXTE4xfW2I241KBjseL8SZDFNFeU4Cf5D3ucwDuQ6+bx3MlKi5wk3Tk68Va7O7t0CQNbH9Qvc+9yiUalQzOtQ+X5Fis/MkVYkBLZlxvXARnRhNCNedU9Cr1SDftK9G8n+0ZC7ZAcpTR/H6P9GJig5R+ZvwAgZ0t3bnLx0XZHT5ys1CwpjcBDRkDIdqY6tZ4ceUp7WvIuQq0", "1"));
        return rvalue;
    }

    /*
    AMERICAN EXPRESS
    */

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

    public static CreditCardData AmexManualEncrypted() {
        return AmexManualEncrypted(true);
    }
    public static CreditCardData AmexManualEncrypted(boolean cardPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("372700790311018");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2020);
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(true);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gwTTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0yp142cX/wGCVF/gVBOFEiFbZxWq0ZQeADdyMNKbOOzxu2MsHhZ+MkDQrz1KJKJVOHQyV3/mnHBWsQPdlGpVkxK0GxFrxbtIxOwViiBZb2ySajpUat6o+MunOrz7ZsYeurOJHtrpYrLEmPgVwxL3dn3Br+XS5sF2pqtG4lq5MsmgAzzKH9/llZ+FDb1e0NJX/8Nso784bBAr3dmUqagCaWSVb4fcg", "1"));
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

    public static CreditTrackData AmexSwipeEncrypted() {
        return AmexSwipeEncrypted(EntryMethod.Swipe);
    }
    public static CreditTrackData AmexSwipeEncrypted(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("B372700791018^AMEX TEST CARD^2512990ocSvC1w2YgC");
        rvalue.setEntryMethod(entryMethod);
        rvalue.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gwTTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0yp142cX/wGCVF/gVBOFEiFbZxWq0ZQeADdyMNKbOOzxu2MsHhZ+MkDQrz1KJKJVOHQyV3/mnHBWsQPdlGpVkxK0GxFrxbtIxOwViiBZb2ySajpUat6o+MunOrz7ZsYeurOJHtrpYrLEmPgVwxL3dn3Br+XS5sF2pqtG4lq5MsmgAzzKH9/llZ+FDb1e0NJX/8Nso784bBAr3dmUqagCaWSVb4fcg", "1"));
        return rvalue;
    }

    /*
    JCB
    */

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

    /*
    VOYAGER
    */

    public static CreditCardData VoyagerManual() {
        return VoyagerManual(false, false);
    }
    public static CreditCardData VoyagerManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("7088869008250005056");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2019);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData VoyagerSwipe() {
        return VoyagerSwipe(EntryMethod.Swipe);
    }
    public static CreditTrackData VoyagerSwipe(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("%07088869008250005056^VOYAGER TEST ACCT THREE  ^2212100000000000000?");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    /*
    Fuelman
    */

    public static CreditCardData FuelmanFleetManual() {
        return FuelmanFleetManual(false, false);
    }
    public static CreditCardData FuelmanFleetManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("70764912345100040");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2049);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData FuelmanFleet() {
        return FuelmanFleet(EntryMethod.Swipe);
    }
    public static CreditTrackData FuelmanFleet(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("70764912345100040=4912");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    /*
  Fleetwide
  */

    public static CreditCardData FleetWideManual() {
        return FleetWideManual(false, false);
    }
    public static CreditCardData FleetWideManual(boolean cardPresent, boolean readerPresent) {
        CreditCardData rvalue = new CreditCardData();
        rvalue.setNumber("70768512345200005");
        rvalue.setExpMonth(12);
        rvalue.setExpYear(2099);
        rvalue.setCvn("123");
        rvalue.setCardPresent(cardPresent);
        rvalue.setReaderPresent(readerPresent);
        return rvalue;
    }

    public static CreditTrackData FleetWide() {
        return FleetWide(EntryMethod.Swipe);
    }
    public static CreditTrackData FleetWide(EntryMethod entryMethod) {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("70768512345200005=99120");
        rvalue.setEntryMethod(entryMethod);
        return rvalue;
    }

    /*
    GIFT CARDS
    */

    public static GiftCard GiftCard1Swipe() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue("%B5022440000000000098^^391200081613?;5022440000000000098=391200081613?");
        return rvalue;
    }
    public static GiftCard GiftCard2Manual() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue("5022440000000000007");
        return rvalue;
    }

    /*
    VALUE LINK
    */
    public static GiftCard ValueLinkManual() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue("6010561234567890123");
        return rvalue;
    }
    public static GiftCard ValueLinkSwipe() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue("6010561234567890123=25010004000070779628");
        return rvalue;
    }

    /*
    SVS
    ;7083559900007000792=99990018010300000?
    ;7083559900007000776=99990013849500000?
    ;7083559900007000818=99990012504400000?
    */
    public static GiftCard SvsManual() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue("6394700000001113");
        rvalue.setPin("1234");
        return rvalue;
    }
    public static GiftCard SvsSwipe() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue(";7083559900007000818=99990012504400000?");
        return rvalue;
    }
}
