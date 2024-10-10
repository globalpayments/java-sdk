package com.global.api.utils;

import com.global.api.entities.enums.TrackNumber;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.ITrackData;
import lombok.var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardUtils {
    private static final Pattern AmexRegex = Pattern.compile("^3[47]");
    private static final Pattern MasterCardRegex = Pattern.compile("^(?:5[1-8]|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)");
    private static final Pattern VisaRegex = Pattern.compile("^4");
    private static final Pattern DinersClubRegex = Pattern.compile("^3(?:0[0-5]|[68][0-9])");
    private static final Pattern RouteClubRegex = Pattern.compile("^(2014|2149)");
    private static final Pattern DiscoverRegex = Pattern.compile("^6(?:011|5[0-9]{2})");
    private static final Pattern JcbRegex = Pattern.compile("^(?:2131|1800|35\\d{3})");
    private static final Pattern VoyagerRegex = Pattern.compile("^70888[5-9]");
    private static final Pattern WexRegex = Pattern.compile("^(?:690046|707138)");
    private static final Pattern FuelmanRegex = Pattern.compile("^707649[0-9]");
    private static final Pattern FleetwideRegex = Pattern.compile("^707685[0-9]");
    private static final Pattern StoredValueRegex = Pattern.compile("^(?:600649|603261|603571|627600|639470)");
    private static final Pattern ValueLinkRegex = Pattern.compile("^(?:601056|603225)");
    private static final Pattern HeartlandGiftRegex = Pattern.compile("^(?:502244|627720|708355)");
    private static final Pattern UnionPayRegex = Pattern.compile("^(?:62[0-8]|81[0-8])");

    private static final Pattern trackOnePattern = Pattern.compile("%?[B0]?([\\d]+)\\^[^\\^]+\\^([\\d]{4})([^?]+)?/?");
    private static final Pattern trackTwoPattern = Pattern.compile(";?([\\d]+)[=|[dD]](\\d{4})([^?]+)?/?");

    private static Map<String, Map<String, String>> fleetBinMap;
    private static Map<String,Pattern> regexMap;
    private static ArrayList<String> readyLinkBinMap;

    static {
        regexMap = new HashMap<String, Pattern>();
        regexMap.put("Amex", AmexRegex);
        regexMap.put("MC", MasterCardRegex);
        regexMap.put("Visa", VisaRegex);
        regexMap.put("DinersClub", DinersClubRegex);
        regexMap.put("EnRoute", RouteClubRegex);
        regexMap.put("Discover", DiscoverRegex);
        regexMap.put("Jcb", JcbRegex);
        regexMap.put("Voyager", VoyagerRegex);
        regexMap.put("Wex", WexRegex);
        regexMap.put("Fuelman", FuelmanRegex);
        regexMap.put("FleetWide", FleetwideRegex);
        regexMap.put("StoredValue", StoredValueRegex);
        regexMap.put("ValueLink", ValueLinkRegex);
        regexMap.put("HeartlandGift", HeartlandGiftRegex);
        regexMap.put("UnionPay", UnionPayRegex);

        // ReadyLink
        readyLinkBinMap = new ArrayList<String>();
        readyLinkBinMap.add("462766");
        readyLinkBinMap.add("406498");
        readyLinkBinMap.add("440230");
        readyLinkBinMap.add("485932");
        readyLinkBinMap.add("434249");
        readyLinkBinMap.add("487093");
        readyLinkBinMap.add("411338");
        readyLinkBinMap.add("438968");
        readyLinkBinMap.add("444083");
        readyLinkBinMap.add("417021");
        readyLinkBinMap.add("400421");
        readyLinkBinMap.add("426938");
        readyLinkBinMap.add("478499");
        readyLinkBinMap.add("446053");
        readyLinkBinMap.add("459440");
        readyLinkBinMap.add("421783");
        readyLinkBinMap.add("422799");
        readyLinkBinMap.add("473517");
        readyLinkBinMap.add("493478");
        readyLinkBinMap.add("453037");
        readyLinkBinMap.add("443613");
        readyLinkBinMap.add("401658");
        readyLinkBinMap.add("439331");
        readyLinkBinMap.add("407216");
        readyLinkBinMap.add("400123");
        readyLinkBinMap.add("402407");
        readyLinkBinMap.add("405551");
        readyLinkBinMap.add("404206");
        readyLinkBinMap.add("422803");
        readyLinkBinMap.add("407635");
        readyLinkBinMap.add("447904");
        readyLinkBinMap.add("439461");

        // fleet bin ranges
        fleetBinMap = new HashMap<String, Map<String, String>>();

        // visa fleet mappings
        Map<String, String> visaFleetMap = new HashMap<String, String>();
        visaFleetMap.put("448460", "448611");
        visaFleetMap.put("448613", "448616");
        visaFleetMap.put("448617", "448674");
        visaFleetMap.put("448676", "448686");
        visaFleetMap.put("448688", "448699");
        visaFleetMap.put("461400", "461421");
        visaFleetMap.put("461423", "461499");
        visaFleetMap.put("448616", "448619");
        visaFleetMap.put("448628", "448629");
        visaFleetMap.put("448631", "448663");
        visaFleetMap.put("448665", "448673");
        visaFleetMap.put("480700", "480899");
        visaFleetMap.put("471562", "471562");
        fleetBinMap.put("Visa", visaFleetMap);

        // mastercard fleet mappings
        Map<String, String> mcFleetMap = new HashMap<String, String>();
        mcFleetMap.put("553231", "553380");
        mcFleetMap.put("556083", "556099");
        mcFleetMap.put("556100", "556599");
        mcFleetMap.put("556700", "556999");
        fleetBinMap.put("MC", mcFleetMap);

        // wright express fleet mappings
        Map<String, String> wexFleetMap = new HashMap<String, String>();
        wexFleetMap.put("690046", "690046");
        wexFleetMap.put("707138", "707138");
        fleetBinMap.put("Wex", wexFleetMap);

        // voyager fleet
        Map<String, String> voyagerFleetMap = new HashMap<String, String>();
        voyagerFleetMap.put("708885", "708889");
        fleetBinMap.put("Voyager", voyagerFleetMap);

        //Fuelman fleet
        Map<String, String> fuelmanFleetMap = new HashMap<String, String>();
        fuelmanFleetMap.put("707649", "707649");
        fleetBinMap.put("Fuelman", fuelmanFleetMap);

        //FleetWide
        Map<String, String> fleetWideMap = new HashMap<String, String>();
        fleetWideMap.put("707685", "707685");
        fleetBinMap.put("FleetWide", fleetWideMap);
    }
    public static boolean isFleet(String cardType, String pan) {
        if (!StringUtils.isNullOrEmpty(pan)) {
            int compareValue = Integer.parseInt(pan.substring(0, 6));
            String baseCardType = StringUtils.trimEnd(cardType, "Fleet");

            if (fleetBinMap.containsKey(baseCardType)) {
                Map<String, String> binRanges = fleetBinMap.get(baseCardType);
                for (String key : binRanges.keySet()) {
                    int lowerRange = Integer.parseInt(key);
                    int upperRange = Integer.parseInt(binRanges.get(key));

                    if (compareValue >= lowerRange && compareValue <= upperRange) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isReadyLink(String pan) {
        if(!StringUtils.isNullOrEmpty(pan)) {
            String compareValue = pan.substring(0, 6);
            return readyLinkBinMap.contains(compareValue);
        }
        return false;
    }

    public static String mapCardType(String pan) {
        String rvalue = "Unknown";
        if(!StringUtils.isNullOrEmpty(pan)) {
            pan = pan.replace(" ", "").replace("-", "");

            for (Map.Entry<String, Pattern> kvp : regexMap.entrySet()) {
                if (kvp.getValue().matcher(pan).find()) {
                    rvalue = kvp.getKey();
                }
            }

            // we have a card type, check if it's a fleet card
            if(!rvalue.equals("Unknown")) {
               // if(isFleet(rvalue, pan)){
                if(isFleet(rvalue, pan)&&(!rvalue.equals("FleetWide"))) {
                    rvalue += "Fleet";
                }
                else if(isReadyLink(pan)) {
                    rvalue += "ReadyLink";
                }
            }
        }
        return rvalue;
    }

    public static String getBaseCardType(String cardType) {
        var resultCardType = cardType;
        for (String cardTypeKey : regexMap.keySet()) {
            if (cardType.toUpperCase(Locale.ENGLISH).startsWith(cardTypeKey.toUpperCase(Locale.ENGLISH))) {
                return cardTypeKey;
            }
        }
        return resultCardType;
    }

    public static GiftCard parseTrackData(GiftCard paymentMethod) {
        String trackData = paymentMethod.getValue();
        Matcher matcher = trackTwoPattern.matcher(trackData);
        if(matcher.find()) {
            paymentMethod.setTrackNumber(TrackNumber.TrackTwo);
            paymentMethod.setPan(matcher.group(1));
            paymentMethod.setExpiry(matcher.group(2));
            paymentMethod.setTrackData(StringUtils.trimStart(matcher.group(), ";"));
        }
        else {
            matcher = trackOnePattern.matcher(trackData);
            if(matcher.find()) {
                paymentMethod.setTrackNumber(TrackNumber.TrackOne);
                paymentMethod.setPan(matcher.group(1));
                paymentMethod.setExpiry(matcher.group(2));
                paymentMethod.setTrackData(StringUtils.trimStart(matcher.group(), "%"));
            }
        }

        return paymentMethod;
    }
    public static <T extends ITrackData> T parseTrackData(T paymentMethod) {
        String trackData = paymentMethod.getValue();
        Matcher matcher = trackTwoPattern.matcher(trackData);
        if(matcher.find()) {
            String pan = matcher.group(1);
            String expiry = matcher.group(2);
            String discretionary = matcher.group(3);

            if(!StringUtils.isNullOrEmpty(discretionary)) {
                if(pan.concat(expiry).concat(discretionary).length() == 37 && discretionary.toLowerCase(Locale.ENGLISH).endsWith("f")) {
                    discretionary = discretionary.substring(0, discretionary.length() - 1);
                }
            }

            paymentMethod.setTrackNumber(TrackNumber.TrackTwo);
            paymentMethod.setPan(pan);
            paymentMethod.setExpiry(expiry);
            paymentMethod.setDiscretionaryData(discretionary);
            paymentMethod.setTrackData(String.format("%s=%s%s", pan, expiry, discretionary != null ? discretionary : ""));
        }
        else {
            matcher = trackOnePattern.matcher(trackData);
            if(matcher.find()) {
                paymentMethod.setTrackNumber(TrackNumber.TrackOne);
                paymentMethod.setPan(matcher.group(1));
                paymentMethod.setExpiry(matcher.group(2));
                paymentMethod.setDiscretionaryData(matcher.group(3));
                paymentMethod.setTrackData(StringUtils.trimStart(matcher.group(), "%"));
            }
        }

        return paymentMethod;
    }
}
