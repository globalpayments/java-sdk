package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

import java.util.EnumMap;
import java.util.Map;

public enum NtsProductCode implements IStringConstant {
    Regular("001", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "01");
        put(NTSCardTypes.MastercardPurchasing, "001");
        put(NTSCardTypes.VisaFleet, "01");
        put(NTSCardTypes.VoyagerFleet, "01");
    }}), Plus("002", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "02");
        put(NTSCardTypes.MastercardPurchasing, "002");
        put(NTSCardTypes.VisaFleet, "02");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), Premium("003", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "03");
        put(NTSCardTypes.MastercardPurchasing, "004");
        put(NTSCardTypes.VisaFleet, "03");
        put(NTSCardTypes.VoyagerFleet, "04");
    }}), Plus2("004", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "02");
        put(NTSCardTypes.MastercardPurchasing, "002");
        put(NTSCardTypes.VisaFleet, "02");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), Plus3("005", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "02");
        put(NTSCardTypes.MastercardPurchasing, "002");
        put(NTSCardTypes.VisaFleet, "02");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), RegularM5("006", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "05");
        put(NTSCardTypes.MastercardPurchasing, "005");
        put(NTSCardTypes.VisaFleet, "06");
        put(NTSCardTypes.VoyagerFleet, "D1");
    }}), PlusM5("007", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "06");
        put(NTSCardTypes.MastercardPurchasing, "006");
        put(NTSCardTypes.VisaFleet, "07");
        put(NTSCardTypes.VoyagerFleet, "D4");
    }}), PremiumM5("008", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "08");
        put(NTSCardTypes.MastercardPurchasing, "008");
        put(NTSCardTypes.VisaFleet, "08");
        put(NTSCardTypes.VoyagerFleet, "D7");
    }}), RegularM7("009", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "05");
        put(NTSCardTypes.MastercardPurchasing, "005");
        put(NTSCardTypes.VisaFleet, "09");
        put(NTSCardTypes.VoyagerFleet, "D2");
    }}), PlusM7("010", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "06");
        put(NTSCardTypes.MastercardPurchasing, "006");
        put(NTSCardTypes.VisaFleet, "10");
        put(NTSCardTypes.VoyagerFleet, "D5");
    }}), RegularE5("011", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "19");
        put(NTSCardTypes.MastercardPurchasing, "019");
        put(NTSCardTypes.VisaFleet, "11");
        put(NTSCardTypes.VoyagerFleet, "D1");
    }}), PlusE5("012", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "20");
        put(NTSCardTypes.MastercardPurchasing, "020");
        put(NTSCardTypes.VisaFleet, "12");
        put(NTSCardTypes.VoyagerFleet, "D4");
    }}), PremiumE5("013", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "22");
        put(NTSCardTypes.MastercardPurchasing, "022");
        put(NTSCardTypes.VisaFleet, "13");
        put(NTSCardTypes.VoyagerFleet, "D7");
    }}), RegularE7("014", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "19");
        put(NTSCardTypes.MastercardPurchasing, "019");
        put(NTSCardTypes.VisaFleet, "14");
        put(NTSCardTypes.VoyagerFleet, "D2");
    }}), PlusE7("015", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "20");
        put(NTSCardTypes.MastercardPurchasing, "020");
        put(NTSCardTypes.VisaFleet, "15");
        put(NTSCardTypes.VoyagerFleet, "D5");
    }}), LeadedMeth("016", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "09");
        put(NTSCardTypes.MastercardPurchasing, "009");
        put(NTSCardTypes.VisaFleet, "16");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), LeadedEth("017", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "23");
        put(NTSCardTypes.MastercardPurchasing, "023");
        put(NTSCardTypes.VisaFleet, "17");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), Leaded("018", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "11");
        put(NTSCardTypes.MastercardPurchasing, "011");
        put(NTSCardTypes.VisaFleet, "18");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), Diesel2("019", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "012");
        put(NTSCardTypes.VisaFleet, "19");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), Dsl2Prem("020", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "13");
        put(NTSCardTypes.MastercardPurchasing, "013");
        put(NTSCardTypes.VisaFleet, "20");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), Diesel1("021", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "012");
        put(NTSCardTypes.VisaFleet, "21");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), Cng("022", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "16");
        put(NTSCardTypes.MastercardPurchasing, "016");
        put(NTSCardTypes.VisaFleet, "22");
        put(NTSCardTypes.VoyagerFleet, "59");
    }}), Lpg("023", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "15");
        put(NTSCardTypes.MastercardPurchasing, "015");
        put(NTSCardTypes.VisaFleet, "23");
        put(NTSCardTypes.VoyagerFleet, "62");
    }}), Lng("024", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "201");
        put(NTSCardTypes.VisaFleet, "24");
        put(NTSCardTypes.VoyagerFleet, "67");
    }}), M85("025", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "17");
        put(NTSCardTypes.MastercardPurchasing, "017");
        put(NTSCardTypes.VisaFleet, "25");
        put(NTSCardTypes.VoyagerFleet, "64");
    }}), E85("026", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "18");
        put(NTSCardTypes.MastercardPurchasing, "018");
        put(NTSCardTypes.VisaFleet, "26");
        put(NTSCardTypes.VoyagerFleet, "66");
    }}), Reg_Rfrm("027", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "24");
        put(NTSCardTypes.MastercardPurchasing, "024");
        put(NTSCardTypes.VisaFleet, "27");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), Plus_Rfrm("028", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "25");
        put(NTSCardTypes.MastercardPurchasing, "025");
        put(NTSCardTypes.VisaFleet, "28");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), Prem_Rfrm("029", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "25");
        put(NTSCardTypes.MastercardPurchasing, "025");
        put(NTSCardTypes.VisaFleet, "29");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), Plus_Rfrm2("030", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "25");
        put(NTSCardTypes.MastercardPurchasing, "025");
        put(NTSCardTypes.VisaFleet, "30");
        put(NTSCardTypes.VoyagerFleet, "02");
    }}), PlusRfrm3("031", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "25");
        put(NTSCardTypes.MastercardPurchasing, "025");
        put(NTSCardTypes.VisaFleet, "31");
        put(NTSCardTypes.VoyagerFleet, "04");
    }}), DslTaxEx("032", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "26");
        put(NTSCardTypes.MastercardPurchasing, "026");
        put(NTSCardTypes.VisaFleet, "32");
        put(NTSCardTypes.VoyagerFleet, "65");
    }}), UlsdNonTax("033", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "26");
        put(NTSCardTypes.MastercardPurchasing, "026");
        put(NTSCardTypes.VisaFleet, "33");
        put(NTSCardTypes.VoyagerFleet, "65");
    }}), BioDslOff("034", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "26");
        put(NTSCardTypes.MastercardPurchasing, "026");
        put(NTSCardTypes.VisaFleet, "34");
        put(NTSCardTypes.VoyagerFleet, "65");
    }}), BioUlsdOff("035", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "26");
        put(NTSCardTypes.MastercardPurchasing, "026");
        put(NTSCardTypes.VisaFleet, "35");
        put(NTSCardTypes.VoyagerFleet, "65");
    }}), Racing("036", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "203");
        put(NTSCardTypes.VisaFleet, "36");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), PremiumM7("037", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "08");
        put(NTSCardTypes.MastercardPurchasing, "008");
        put(NTSCardTypes.VisaFleet, "37");
        put(NTSCardTypes.VoyagerFleet, "D8");
    }}), RegM10("038", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "05");
        put(NTSCardTypes.MastercardPurchasing, "005");
        put(NTSCardTypes.VisaFleet, "38");
        put(NTSCardTypes.VoyagerFleet, "D3");
    }}), PlusM10("039", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "06");
        put(NTSCardTypes.MastercardPurchasing, "006");
        put(NTSCardTypes.VisaFleet, "39");
        put(NTSCardTypes.VoyagerFleet, "D6");
    }}), PremM10("040", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "08");
        put(NTSCardTypes.MastercardPurchasing, "008");
        put(NTSCardTypes.VisaFleet, "40");
        put(NTSCardTypes.VoyagerFleet, "D9");
    }}), PremE7("041", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "22");
        put(NTSCardTypes.MastercardPurchasing, "022");
        put(NTSCardTypes.VisaFleet, "41");
        put(NTSCardTypes.VoyagerFleet, "D8");
    }}), RegE10("042", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "19");
        put(NTSCardTypes.MastercardPurchasing, "019");
        put(NTSCardTypes.VisaFleet, "42");
        put(NTSCardTypes.VoyagerFleet, "D3");
    }}), PlusE10("043", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "20");
        put(NTSCardTypes.MastercardPurchasing, "020");
        put(NTSCardTypes.VisaFleet, "43");
        put(NTSCardTypes.VoyagerFleet, "D6");
    }}), PremE10("044", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "21");
        put(NTSCardTypes.MastercardPurchasing, "022");
        put(NTSCardTypes.VisaFleet, "44");
        put(NTSCardTypes.VoyagerFleet, "D9");
    }}), BioDsl2("045", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "45");
        put(NTSCardTypes.VoyagerFleet, "LB");
    }}), BioDsl5("046", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "46");
        put(NTSCardTypes.VoyagerFleet, "LB");
    }}), BioDsl10("047", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "47");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioDsl11("048", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "48");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioDsl15("049", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "49");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioDsl20("050", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "50");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioDsl100("051", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "51");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), UlsDsl1("052", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "29");
        put(NTSCardTypes.MastercardPurchasing, "029");
        put(NTSCardTypes.VisaFleet, "52");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), UlsDsl2("053", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "29");
        put(NTSCardTypes.MastercardPurchasing, "029");
        put(NTSCardTypes.VisaFleet, "53");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), Ulsd2Prem("054", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "29");
        put(NTSCardTypes.MastercardPurchasing, "029");
        put(NTSCardTypes.VisaFleet, "54");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), BioUlsd2("055", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "55");
        put(NTSCardTypes.VoyagerFleet, "LB");
    }}), BioUlsd5("056", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "56");
        put(NTSCardTypes.VoyagerFleet, "LB");
    }}), BioUlsd10("057", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "57");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioUlsd11("058", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "58");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioUlsd15("059", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "59");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioUlsd20("060", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "60");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), BioUlsd100("061", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "28");
        put(NTSCardTypes.MastercardPurchasing, "028");
        put(NTSCardTypes.VisaFleet, "61");
        put(NTSCardTypes.VoyagerFleet, "D0");
    }}), Def("062", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "47");
        put(NTSCardTypes.MastercardPurchasing, "047");
        put(NTSCardTypes.VisaFleet, "99");
        put(NTSCardTypes.VoyagerFleet, "DF");
    }}), RnwDsl99("071", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "012");
        put(NTSCardTypes.VisaFleet, "21");
        put(NTSCardTypes.VoyagerFleet, "05");
    }}), MiscFuel("099", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "200");
        put(NTSCardTypes.VisaFleet, "99");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), AutoMerch("100", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "40");
        put(NTSCardTypes.MastercardPurchasing, "040");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "19");
    }}), Oil("101", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "30");
        put(NTSCardTypes.MastercardPurchasing, "030");
        put(NTSCardTypes.VisaFleet, "30");
        put(NTSCardTypes.VoyagerFleet, "09");
    }}), CarWash("102", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "45");
        put(NTSCardTypes.MastercardPurchasing, "045");
        put(NTSCardTypes.VisaFleet, "45");
        put(NTSCardTypes.VoyagerFleet, "27");
    }}), OilChange("103", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "31");
        put(NTSCardTypes.MastercardPurchasing, "031");
        put(NTSCardTypes.VisaFleet, "31");
        put(NTSCardTypes.VoyagerFleet, "18");
    }}), OilFilter("104", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "19");
    }}), WorkOrder("105", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "21");
    }}), Fluids("106", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "13");
    }}), WashFluid("107", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "13");
    }}), BrakeFlui("108", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "36");
        put(NTSCardTypes.MastercardPurchasing, "036");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "13");
    }}), Tires("109", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "41");
        put(NTSCardTypes.MastercardPurchasing, "041");
        put(NTSCardTypes.VisaFleet, "41");
        put(NTSCardTypes.VoyagerFleet, "23");
    }}), FexTax("110", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "26");
    }}), Rotation("111", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "38");
        put(NTSCardTypes.MastercardPurchasing, "038");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "42");
    }}), Batteries("112", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "42");
        put(NTSCardTypes.MastercardPurchasing, "042");
        put(NTSCardTypes.VisaFleet, "42");
        put(NTSCardTypes.VoyagerFleet, "73");
    }}), Lube("113", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "31");
    }}), Inspection("114", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "38");
        put(NTSCardTypes.MastercardPurchasing, "038");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "32");
    }}), Labor("115", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "38");
        put(NTSCardTypes.MastercardPurchasing, "038");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "25");
    }}), Towing("116", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "46");
        put(NTSCardTypes.MastercardPurchasing, "046");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "70");
    }}), RoadSide("117", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "74");
    }}), AutoAcces("118", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "43");
        put(NTSCardTypes.MastercardPurchasing, "043");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "10");
    }}), Parts("119", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "19");
    }}), PrevMaint("120", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "U2");
    }}), AcService("121", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "39");
    }}), EngineSvc("122", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "32");
        put(NTSCardTypes.MastercardPurchasing, "032");
        put(NTSCardTypes.VisaFleet, "32");
        put(NTSCardTypes.VoyagerFleet, "O9");
    }}), TransSvc("123", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "33");
        put(NTSCardTypes.MastercardPurchasing, "033");
        put(NTSCardTypes.VisaFleet, "33");
        put(NTSCardTypes.VoyagerFleet, "38");
    }}), BrakeSvc("124", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "34");
        put(NTSCardTypes.MastercardPurchasing, "034");
        put(NTSCardTypes.VisaFleet, "34");
        put(NTSCardTypes.VoyagerFleet, "M8");
    }}), Exhaust("125", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "P1");
    }}), Body("126", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "Q9");
    }}), AutoGlass("127", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "44");
        put(NTSCardTypes.MastercardPurchasing, "044");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "48");
    }}), OilSynth("128", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "30");
        put(NTSCardTypes.MastercardPurchasing, "030");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "09");
    }}), Lamps("129", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "O7");
    }}), Wipers("130", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "43");
    }}), TubesHose("131", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "S7");
    }}), TireGen("132", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "40");
        put(NTSCardTypes.MastercardPurchasing, "040");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "15");
    }}), Repairs("133", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "039");
        put(NTSCardTypes.VisaFleet, "39");
        put(NTSCardTypes.VoyagerFleet, "20");
    }}), TankClean("136", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "38");
        put(NTSCardTypes.MastercardPurchasing, "038");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "21");
    }}), OthLubes("137", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "31");
    }}), FuelAdd("138", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "49");
    }}), MiscParts("149", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "37");
        put(NTSCardTypes.MastercardPurchasing, "037");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "19");
    }}), JetAdd("152", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "152");
        put(NTSCardTypes.VisaFleet, "A2");
        put(NTSCardTypes.VoyagerFleet, "07");
    }}), JetJp8("153", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "101");
        put(NTSCardTypes.VisaFleet, "A3");
        put(NTSCardTypes.VoyagerFleet, "07");
    }}), Jet2("154", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "04");
        put(NTSCardTypes.MastercardPurchasing, "102");
        put(NTSCardTypes.VisaFleet, "A4");
        put(NTSCardTypes.VoyagerFleet, "06");
    }}), Jet3("155", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "04");
        put(NTSCardTypes.MastercardPurchasing, "102");
        put(NTSCardTypes.VisaFleet, "A5");
        put(NTSCardTypes.VoyagerFleet, "06");
    }}), Storage("175", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "22");
    }}), CarRent("182", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "88");
    }}), Catering("185", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "303");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "83");
    }}), CallOut("188", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "307");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "87");
    }}), Charter("192", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "312");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "93");
    }}), CommFees("193", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "314");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "95");
    }}), CargoHand("195", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "316");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "97");
    }}), Marine1("225", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "M1");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), Marine2("226", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "M2");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), Marine3("227", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "M3");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), Marine4("228", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "M4");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), Marine5("229", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "12");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "M5");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), MarineOth("230", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MarFuel("249", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "150");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "08");
    }}), MarSvc("250", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "350");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "25");
    }}), MarLabor("251", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "38");
        put(NTSCardTypes.MastercardPurchasing, "350");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "25");
    }}), MarWo("252", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "39");
        put(NTSCardTypes.MastercardPurchasing, "350");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "25");
    }}), LaunchFee("253", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "350");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), SlipRent("254", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "350");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Kerosene("300", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "014");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "54");
    }}), WhiteGas("301", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "202");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "58");
    }}), HeatOil("302", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "200");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), PropaneBt("303", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "200");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "50");
    }}), FuelNontx("304", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "200");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), keroUls("305", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "014");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "54");
    }}), LskerNtx("306", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "014");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "54");
    }}), UlskerNtx("307", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "14");
        put(NTSCardTypes.MastercardPurchasing, "014");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "54");
    }}), FuelOther("399", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "200");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "63");
    }}), GenMerch("400", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), GenIce("401", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), GenTobaco("410", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "70");
        put(NTSCardTypes.MastercardPurchasing, "070");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), CigTobaco("411", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "70");
        put(NTSCardTypes.MastercardPurchasing, "070");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), TobacoOth("412", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "70");
        put(NTSCardTypes.MastercardPurchasing, "070");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), PkgBev("420", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), PkgBevNa("421", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Juice("422", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "82");
        put(NTSCardTypes.MastercardPurchasing, "082");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), PkgBevOth("423", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), DispBev("430", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), HotBev("431", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), ColdBev("432", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), FrozenBev("433", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), DisbevOth("434", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "89");
        put(NTSCardTypes.MastercardPurchasing, "089");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Snacks("440", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), SnackSalt("441", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), SnackAlt("442", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), SnkSweet("443", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Candy("450", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Dairy("460", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "82");
        put(NTSCardTypes.MastercardPurchasing, "082");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Milk("461", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "82");
        put(NTSCardTypes.MastercardPurchasing, "082");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), IceCream("462", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), DairyOth("463", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "82");
        put(NTSCardTypes.MastercardPurchasing, "082");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Grocery("470", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), GroEdible("471", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), GroNonEd("472", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), GroPerish("473", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Bread("474", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), FrozenFd("475", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Alcohol("480", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "81");
        put(NTSCardTypes.MastercardPurchasing, "081");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), BeerOrAlc("481", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "81");
        put(NTSCardTypes.MastercardPurchasing, "081");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), BeerNonal("482", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "81");
        put(NTSCardTypes.MastercardPurchasing, "081");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Wine("483", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "81");
        put(NTSCardTypes.MastercardPurchasing, "081");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Liquor("484", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "81");
        put(NTSCardTypes.MastercardPurchasing, "081");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Deli("490", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), SandwchPk("491", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Prpared_Fd("492", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), DeliItem("493", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), FoodSvc("500", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "79");
        put(NTSCardTypes.MastercardPurchasing, "079");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "14");
    }}), Lottery("510", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), LotInst("511", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), LotOnline("512", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), LotOth("513", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoneyOrd("520", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoVendor("521", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoPayrol("522", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoGift("523", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoRefund("524", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoCheck("525", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoRebate("526", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoDivide("527", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), MoUtility("528", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), HomeDeliv("531", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), SvPurchase("532", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), SvActivate("533", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Loyalty("534", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "36");
    }}), Hba("540", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "78");
        put(NTSCardTypes.MastercardPurchasing, "078");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), PublicAtio("550", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Discount1("900", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), DisNonFuel("901", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), Discount3("902", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), Coupon1("905", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), Coupon2("906", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), Coupon3("907", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), LotPayInst("910", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), LoyPayOnli("911", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), LotPayOth("912", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), TaxDisc("914", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), NegAdmin("949", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "98");
        put(NTSCardTypes.MastercardPurchasing, "098");
        put(NTSCardTypes.VisaFleet, "98");
        put(NTSCardTypes.VoyagerFleet, "35");
    }}), SalesTax("950", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "34");
    }}), Fet("951", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "34");
    }}), StateTax("952", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "34");
    }}), OtherTax("953", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "34");
    }}), MiscTax("954", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "34");
    }}), CashBack("955", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), CshbkFee("956", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Fee1("957", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Fee2("958", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Fee3("959", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Fee4("960", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }}), Fee5("961", new EnumMap<NTSCardTypes, String>(NTSCardTypes.class) {{
        put(NTSCardTypes.MastercardFleet, "99");
        put(NTSCardTypes.MastercardPurchasing, "099");
        put(NTSCardTypes.VisaFleet, "90");
        put(NTSCardTypes.VoyagerFleet, "33");
    }});

    private final String conexxusProductCodes;
    private final Map<NTSCardTypes, String> cards;

    NtsProductCode(String conexxusProductCodes, Map<NTSCardTypes, String> cards) {
        this.conexxusProductCodes = conexxusProductCodes;
        this.cards = cards;
    }

    @Override
    public byte[] getBytes() {
        return this.conexxusProductCodes.getBytes();
    }

    @Override
    public String getValue() {
        return this.conexxusProductCodes;
    }

    public String getProductCodeByCard(NTSCardTypes card) {
        if (cards.containsKey(card)) {
            return cards.get(card);
        }
        return null;
    }
}
