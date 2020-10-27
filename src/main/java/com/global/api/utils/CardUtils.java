package com.global.api.utils;

import com.global.api.entities.enums.TrackNumber;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.ITrackData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardUtils {
    private static final Pattern AmexRegex = Pattern.compile("^3[47]");
    private static final Pattern MasterCardRegex = Pattern.compile("^(?:5[1-5]|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)");
    private static final Pattern VisaRegex = Pattern.compile("^4");
    private static final Pattern DinersClubRegex = Pattern.compile("^3(?:0[0-5]|[68][0-9])");
    private static final Pattern RouteClubRegex = Pattern.compile("^(2014|2149)");
    private static final Pattern DiscoverRegex = Pattern.compile("^6(?:011|5[0-9]{2})");
    private static final Pattern JcbRegex = Pattern.compile("^(?:2131|1800|35\\d{3})");
    private static final Pattern VoyagerRegex = Pattern.compile("^70888[5-9]");
    private static final Pattern WexRegex = Pattern.compile("^(?:690046|707138)");
    private static final Pattern StoredValueRegex = Pattern.compile("^(?:600649|603261|603571|627600|639470)");
    private static final Pattern ValueLinkRegex = Pattern.compile("^(?:601056|603225)");
    private static final Pattern HeartlandGiftRegex = Pattern.compile("^(?:502244|627720|708355)");

    private static final Pattern trackOnePattern = Pattern.compile("%?[B0]?([\\d]+)\\^[^\\^]+\\^([\\d]{4})([^?]+)?/?");
    private static final Pattern trackTwoPattern = Pattern.compile(";?([\\d]+)[=|[dD]](\\d{4})([^?]+)?/?");

    private static Map<String, Map<String, String>> fleetBinMap;
    private static Map<String,Pattern> regexMap;

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
        regexMap.put("StoredValue", StoredValueRegex);
        regexMap.put("ValueLink", ValueLinkRegex);
        regexMap.put("HeartlandGift", HeartlandGiftRegex);

        // fleet bin ranges
        fleetBinMap = new HashMap<String, Map<String, String>>();

        // visa fleet mappings
        Map<String, String> visaFleetMap = new HashMap<String, String>();
        visaFleetMap.put("448460", "448611");
        visaFleetMap.put("448613", "448615");
        visaFleetMap.put("448617", "448674");
        visaFleetMap.put("448676", "448686");
        visaFleetMap.put("448688", "448699");
        visaFleetMap.put("461400", "461421");
        visaFleetMap.put("461423", "461499");
        visaFleetMap.put("480700", "480899");
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
                if(isFleet(rvalue, pan)) {
                    rvalue += "Fleet";
                }
            }
        }
        return rvalue;
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
                if(pan.concat(expiry).concat(discretionary).length() == 37 && discretionary.toLowerCase().endsWith("f")) {
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
