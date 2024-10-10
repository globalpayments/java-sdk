package com.global.api.tests.testdata;

import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.paymentMethods.*;

public class NtsTestCards {

    /**
     * SVS cards
     */

    public static GiftCard svsCardTrack1(EntryMethod entryMethod){
        GiftCard track = new GiftCard();
        track.setValue("%B6006491260550251158^SVSMC^7112110F88?");
        track.setEntryMethod(entryMethod);
        return track;
    }


    public static GiftCard svsCard(){
        GiftCard card = new GiftCard();
        card.setValue(";6006491286999911672=691211072913941?");
        return card;
    }

    public static GiftCard svsCard2(){
        GiftCard card = new GiftCard();
        card.setValue(";6006491260550253006=711111073762752?");
        return card;
    }

    /**
    GIFT CARDS
    */

    public static GiftCard GiftCardSwipe() {
        GiftCard rvalue = new GiftCard();
        rvalue.setValue(";5022440000000000098=391200081613?");
        return rvalue;
    }

    public static GiftCard GiftCardManual() {
        GiftCard rvalue = new GiftCard();
        rvalue.setNumber("5022440000000000007");
        rvalue.setExpiry("1239");
        return rvalue;
    }
    /**
     * Master cards
     */

    public static CreditTrackData MasterCardTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B5473500000000014^MASTERCARD TEST^12251041234567890123?9");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData MasterCardTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";5473500000000014=25121019999888877776?");
        track.setEntryMethod(entryMethod);
        return track;
    }
    public static CreditTrackData MasterCardPurchasingTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";5473500000000014=25121019999888877776?");
        track.setCardType("MastercardPurchasing");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData PropCardTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";6502702501812268=250650100200984?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    /**
     * Visa Cards
     */

    public static CreditTrackData VisaTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4012002000060016=12251011803939600000?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData VisaTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4012002000060016^VI TEST CREDIT^122510118039000000000396?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData Visa2Track1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4484104292153662^POSINT TEST VISA P CARD^1225501032100321001000?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData VisaFleet(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4484630000000126^VISA TEST CARD/GOOD^25121019206100000001?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    //Visa Fleet 2.0
    public static CreditTrackData VisaFleetTwoPointO(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4485580000080017=311220115886224023?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    /**
     * Amex Cards
     */
    public static CreditTrackData AmexTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";372700699251018=25121019999888877776?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData AmexTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    /**
     * Discover Cards
     */
    public static CreditTrackData DiscoverTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";6011000990156527=25121011000062111401?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData DiscoverTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B6011000990156527^DIS TEST CARD^2512990502700?");
        track.setEntryMethod(entryMethod);
        return track;
    }


    /**
     * EBT Cards
     */
    public static EBTTrackData EBTTrack2(EntryMethod entryMethod, EbtCardType ebtCardType){
        EBTTrackData cashTrack = new EBTTrackData(ebtCardType);
        cashTrack.setValue(";6004862001012758000=491200000000?");
        cashTrack.setPinBlock("1109D2058244FBC3A50401000440053F");
        cashTrack.setEntryMethod(entryMethod);
        EncryptionData data = EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2");
        data.setKsn("A50401000440053F    ");
       // cashTrack.setEncryptionData(data);
        return cashTrack;
    }

    public static EBTCardData getFoodCardManual() {
        EBTCardData card = new EBTCardData();
        card.setNumber("6004862001012758000");
        card.setPinBlock("142920FFFFFFFFFF");
        card.setExpYear(2049);
        card.setExpMonth(12);
        card.setEbtCardType(EbtCardType.FoodStamp);
        return card;
    }

    public static EBTCardData getCashCardManual() {
        EBTCardData card = new EBTCardData();
        card.setNumber("6004862001012758000");
        card.setPinBlock("142920FFFFFFFFFF");
        card.setExpYear(2049);
        card.setExpMonth(12);
        card.setEbtCardType(EbtCardType.CashBenefit);
        return card;
    }
    /**
     * Paypal cards
     */
    public static CreditTrackData PaypalTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";6506001000010029=25121010051012345678?");
        track.setEntryMethod(entryMethod);
        return track;
    }
    public static CreditTrackData PaypalTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData SynchronyDiscoverTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";6502702431894196=250660100200714?");
        //track.setValue(";6502702431894196=250660100200616?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData WexFleetTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";6900460430001234566=25121012202100000?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData MasterFleetTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData VoyagerFleetTrack2(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(entryMethod);
        return track;
    }

    public static CreditTrackData VisaFleetTrack1(EntryMethod entryMethod){
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4484630000000126^VISA TEST CARD/GOOD^25121019206100000001?");
        track.setEntryMethod(entryMethod);
        return track;
    }
}
