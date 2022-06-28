package com.global.api.tests.testdata;

import com.global.api.entities.enums.EntryMethod;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;

public class GnapTestCards {

    public static CreditCardData visaManualCard() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("4761739001010119");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditCardData MCManualCard() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089020029");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditCardData amexManualCard() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("374245001741007");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditCardData discoverManualCard() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("6510000000000018");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditCardData UnionPayManual() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("6210948000000029");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }

    public static CreditTrackData VisaTrack2() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";4761739001010119=22122011758909689?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditTrackData MCTrack2() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089020029=2512201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditTrackData DiscoverTrack2() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";6510000000000018=22122011758909689?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditTrackData UnionPayTrack2() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";6210948000000029=22122011758909689?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditTrackData AmexTrack2() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";374245001741007=22122011758909689?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }

    public static CreditTrackData MCEMVTrack() {
        CreditTrackData track=new CreditTrackData();
        track.setValue("5413330089604111D25122010123409172029F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static CreditTrackData visaEmvTrack() {
        CreditTrackData track=new CreditTrackData();
        track.setValue("4761739001010119D22122011143804400000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static CreditTrackData discoverEmvTrack() {
        CreditTrackData track=new CreditTrackData();
        track.setValue("6510000000000018D23122011000010600000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static DebitTrackData interacEMVTrack() {
        DebitTrackData track=new DebitTrackData();
        track.setValue("0012020000001D28122200012300000002000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }

    public static CreditCardData JCBManual()
    {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("3569990010082211");
        manual.setExpYear(2025);
        manual.setExpMonth(12);
        return manual;
    }
    public static CreditTrackData JCBTrack2()
    {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";3569990010082211=25122011758909689?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static  CreditTrackData JCBEmvTrack()
    {
        CreditTrackData track=new CreditTrackData();
        track.setValue("3569990010082211D25122010000000000000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static CreditTrackData amexEmvTrack() {
        CreditTrackData track=new CreditTrackData();
        track.setValue("374245001741007D241220117101234500000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static CreditTrackData unipayEmvTrack() {
        CreditTrackData track=new CreditTrackData();
        track.setValue("6210948000000029D30102200000000000000F");
        track.setEntryMethod(EntryMethod.EMVIntegratedChipCard);
        return track;
    }
    public static CreditTrackData testCard1_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";4761731000000043=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard1_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("4761731000000043");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard2_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";4761730000000011=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard2_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("4761730000000011");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard3_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";4761730000000243=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard3_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("4761730000000243");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard4_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";4761739001010267=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard4_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("4761739001010267");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard5_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089604111=2512201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard5_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089604111");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard6_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089604111=2512201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard6_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089604111");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard7_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089020011=2512201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard7_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089020011");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard8_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089020029=2512201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard8_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089020029");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard9_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";5413330089020011=2512601062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard9_MNL() {
        CreditCardData manual=new CreditCardData();
        manual.setNumber("5413330089020011");
        manual.setExpMonth(12);
        manual.setExpYear(2025);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    //need to create manual testCard for 10 & 11
    public static DebitTrackData testCard10_MSR(){
        DebitTrackData track=new DebitTrackData();
        track.setValue(";0012030000000003=2812220062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static DebitTrackData testCard11_MSR(){
        DebitTrackData track=new DebitTrackData();
        track.setValue(";0012030000000003=2812220062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditTrackData testCard12_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";374245001741007=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard12_MNL() {
        CreditCardData manual = new CreditCardData();
        manual.setNumber("374245001741007");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
        public static CreditTrackData testCard13_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";374245002771003=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard13_MNL() {
        CreditCardData manual = new CreditCardData();
        manual.setNumber("374245002771003");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard14_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";374245003731006=2412201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard14_MNL() {
        CreditCardData manual = new CreditCardData();
        manual.setNumber("374245003731006");
        manual.setExpMonth(12);
        manual.setExpYear(2024);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard15_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";6510000000000216=2312201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard15_MNL() {
        CreditCardData manual = new CreditCardData();
        manual.setNumber("6510000000000216");
        manual.setExpMonth(12);
        manual.setExpYear(2023);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }
    public static CreditTrackData testCard16_MSR() {
        CreditTrackData track=new CreditTrackData();
        track.setValue(";6510000000000331=2312201062980790?");
        track.setEntryMethod(EntryMethod.MagneticStripeAndMSRFallback);
        return track;
    }
    public static CreditCardData testCard16_MNL() {
        CreditCardData manual = new CreditCardData();
        manual.setNumber("6510000000000331");
        manual.setExpMonth(12);
        manual.setExpYear(2023);
        manual.setCardPresent(true);
        manual.setReaderPresent(true);
        return manual;
    }





}
