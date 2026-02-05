package com.global.api.utils;

import java.util.HashMap;
import java.util.Map;

public class EmvUtils {
    private static final Map<String, String> knownTags;
    private static final Map<String, String> blackList;
    private static final Map<String, String> dataTypes;
    private static final Map<String, String> cardHolderLanguages;

    static {
        blackList = new HashMap<>();
        blackList.put("57", "Track 2 Equivalent Data");
        blackList.put("5A", "Application Primary Account Number (PAN)");
        blackList.put("99", "Transaction PIN Data");
        blackList.put("5F20", "Cardholder Name");
        blackList.put("5F24", "Application Expiration Date");
        blackList.put("9F0B", "Cardholder Name Extended");
        blackList.put("9F1F", "Track 1 Discretionary Data");
        blackList.put("9F20", "Track 2 Discretionary Data");

        knownTags = new HashMap<>();
        knownTags.put("4F", "Application Dedicated File (ADF) Name");
        knownTags.put("50", "Application Label");
        knownTags.put("6F", "File Control Information (FCI) Template");
        knownTags.put("71", "Issuer Script Template 1");
        knownTags.put("72", "Issuer Script Template 2");
        knownTags.put("82", "Application Interchange Profile");
        knownTags.put("84", "Dedicated File (DF) Name");
        knownTags.put("86", "Issuer Script Command");
        knownTags.put("87", "Application Priority Indicator");
        knownTags.put("88", "Short File Identifier (SFI)");
        knownTags.put("8A", "Authorization Response Code (ARC)");
        knownTags.put("8C", "Card Rick Management Data Object List 1 (CDOL1)");
        knownTags.put("8D", "Card Rick Management Data Object List 2 (CDOL2)");
        knownTags.put("8E", "Cardholder Verification Method (CVM) List");
        knownTags.put("8F", "Certification Authority Public Key Index");
        knownTags.put("90", "Issuer Public Key Certificate");
        knownTags.put("91", "Issuer Authentication Data");
        knownTags.put("92", "Issuer Public Key Remainder");
        knownTags.put("93", "Signed Static Application Data");
        knownTags.put("94", "Application File Locator (AFL)");
        knownTags.put("95", "Terminal Verification Results (TVR)");
        knownTags.put("97", "Transaction Certification Data Object List (TDOL)");
        knownTags.put("9A", "Transaction Date");
        knownTags.put("9B", "Transaction Status Indicator");
        knownTags.put("9C", "Transaction Type");
        knownTags.put("9D", "Directory Definition File (DDF) Name");

        knownTags.put("5F25", "Application Effective Date");
        knownTags.put("5F28", "Issuer Country Code");
        knownTags.put("5F2A", "Transaction Currency Code");
        knownTags.put("5F2D", "Language Preference");
        knownTags.put("5F30", "Service Code");
        knownTags.put("5F34", "Application Primary Account Number (PAN) Sequence Number");
        knownTags.put("5F36", "Transaction Currency Exponent");

        knownTags.put("9F0A", "Application Selection Registered Proprietary Data");
        knownTags.put("9F01", "Unknown");
        knownTags.put("9F02", "Amount, Authorized");
        knownTags.put("9F03", "Amount, Other");
        knownTags.put("9F05", "Application Discretionary Data");
        knownTags.put("9F06", "Application Identifier (AID)");
        knownTags.put("9F07", "Application Usage Control");
        knownTags.put("9F08", "Application Version Number");
        knownTags.put("9F09", "Application Version Number");
        knownTags.put("9F0D", "Issuer Action Code (IAC) - Default");
        knownTags.put("9F0E", "Issuer Action Code (IAC) - Denial");
        knownTags.put("9F0F", "Issuer Action Code (IAC) - Online");

        knownTags.put("9F10", "Issuer Application Data");
        knownTags.put("9F11", "Issuer Code Table Index");
        knownTags.put("9F12", "Application Preferred Name");
        knownTags.put("9F13", "Last Online Application Transaction Counter (ATC) Register");
        knownTags.put("9F14", "Lower Consecutive Offline Limit");
        knownTags.put("9F16", "Unknown");
        knownTags.put("9F17", "Personal Identification Number (PIN) Try Counter");
        knownTags.put("9F1A", "Terminal Country Code");
        knownTags.put("9F1B", "Terminal Floor Limit");
        knownTags.put("9F1C", "Unknown");
        knownTags.put("9F1D", "Terminal Risk Management Data");
        knownTags.put("9F1E", "Interface Device (IFD) Serial Number");

        knownTags.put("9F21", "Transaction Time");
        knownTags.put("9F22", "Certification Authority Public Key Modulus");
        knownTags.put("9F23", "Upper Consecutive Offline Limit");
        knownTags.put("9F26", "Application Cryptogram");
        knownTags.put("9F27", "Cryptogram Information Data");
        knownTags.put("9F2D", "Integrated Circuit Card (ICC) PIN Encipherment Public Key Certificate");
        knownTags.put("9F2E", "Integrated Circuit Card (ICC) PIN Encipherment Public Key Exponent");
        knownTags.put("9F2F", "Integrated Circuit Card (ICC) PIN Encipherment Public Key Remainder");

        knownTags.put("9F32", "Issuer Public Key Exponent");
        knownTags.put("9F33", "Terminal Capabilities");
        knownTags.put("9F34", "Cardholder Verification Method (CVM) Results");
        knownTags.put("9F35", "Terminal Type");
        knownTags.put("9F36", "Application Transaction Counter (ATC)");
        knownTags.put("9F37", "Unpredictable Number");
        knownTags.put("9F38", "Processing Options Data Object List (PDOL)");
        knownTags.put("9F39", "Point-Of-Service (POS) Entry Mode");
        knownTags.put("9F3B", "Application Reference Currency");
        knownTags.put("9F3C", "Transaction Reference Currency Code");
        knownTags.put("9F3D", "Transaction Reference Currency Conversion");

        knownTags.put("9F40", "Additional Terminal Capabilities");
        knownTags.put("9F41", "Transaction Sequence Counter");
        knownTags.put("9F42", "Application Currency Code");
        knownTags.put("9F43", "Application Reference Currency Exponent");
        knownTags.put("9F44", "Application Currency Exponent");
        knownTags.put("9F46", "Integrated Circuit Card (ICC) Public Key Certificate");
        knownTags.put("9F47", "Integrated Circuit Card (ICC) Public Key Exponent");
        knownTags.put("9F48", "Integrated Circuit Card (ICC) Public Key Remainder");
        knownTags.put("9F49", "Dynamic Data Authentication Data Object List (DDOL)");
        knownTags.put("9F4A", "Signed Data Authentication Tag List");
        knownTags.put("9F4B", "Signed Dynamic Application Data");
        knownTags.put("9F4C", "ICC Dynamic Number");
        knownTags.put("9F4E", "Unknown");

        knownTags.put("9F5B", "Issuer Script Results");
        knownTags.put("9F58", "Consecutive Transaction Counter Limit (CTCL)");
        knownTags.put("9F59", "Consecutive Transaction Counter Upper Limit (CTCUL)");
        knownTags.put("9F66", "Terminal Transaction Qualifiers");
        knownTags.put("9F68", "Card Additional Processes");
        knownTags.put("9F6C", "Card Transaction Qualifiers");

        knownTags.put("9F6E", "Form Factor Indicator/Third Party Data");
        knownTags.put("9F7C", "Customer Exclusive Data");
        knownTags.put("FFC6", "Terminal Action Code (TAC) Default");
        knownTags.put("FFC7", "Terminal Action Code (TAC) Denial");
        knownTags.put("FFC8", "Terminal Action Code (TAC) Online");

        //Adding Visa Fleet EMV
        knownTags.put("8201", "ICC Key CRT constant q-1 d mod (q-1)");
        knownTags.put("8202", "ICC Private Key CRT constant d mod (q-1)");
        knownTags.put("8203", "ICC Private Key CRT constant d mod (p-1)");
        knownTags.put("8204", "ICC Private Key CRT constant prime factor q");
        knownTags.put("8205", "ICC Private Key CRT constant prime factor p");

        // Adding the WEX EMV
        knownTags.put("42", "Issuer Identification Number (IIN or BIN)");
        knownTags.put("61", "Directory entry Template");
        knownTags.put("70", "Record Template");
        knownTags.put("73", "Directory Discretionary Template");
        knownTags.put("9F4D", "Log Entry");
        knownTags.put("9F4F", "Transaction Log Format");
        knownTags.put("9F52", "Card Verification Results (CVR)");
        knownTags.put("9F7E", "Issuer Life Cycle Data");
        knownTags.put("A5", "FCI Proprietary Template");
        knownTags.put("BF0C", "FCI Issuer Discretionary Data");
        knownTags.put("BF20", "PRO 00");
        knownTags.put("BF27", "PRO 07");
        knownTags.put("BF2E", "PRO 14");
        knownTags.put("C1", "Application Control");
        knownTags.put("C4", "Default Contact Profile31");
        knownTags.put("CA", "Previous Transaction History");
        knownTags.put("CB", "CRM Country Code");
        knownTags.put("CD", "CRM Currency Code");
        knownTags.put("D3", "PDOL Related data Length");
        knownTags.put("D8", "CAFL");
        knownTags.put("DF01", "Proprietary Data Element n°1");
        knownTags.put("DF02", "Proprietary Data Element n°2");
        knownTags.put("DF03", "Proprietary Data Element n°3");
        knownTags.put("DF04", "Proprietary Data Element n°4");
        knownTags.put("DF05", "Proprietary Data Element n°5 ");
        knownTags.put("DF06", "Proprietary Data Element n°6");
        knownTags.put("DF07", "Proprietary Data Element n°7 ");
        knownTags.put("DF08", "Proprietary Data Element n°8");
        knownTags.put("DF10", "Profile Selection Table");
        knownTags.put("DF11", "Currency Conversion Code 1");
        knownTags.put("DF12", "Currency Conversion Code 2");
        knownTags.put("DF13", "COTN counter");
        knownTags.put("DF14", "COTA accumulator");

        knownTags.put("DF15", "CIAC – Denial");
        knownTags.put("DF16", "CIAC – Default");
        knownTags.put("DF17", "CIAC – Online");
        knownTags.put("DF18", "LCOTA limit ");
        knownTags.put("DF19", "UCOTA limit");
        knownTags.put("DF1A", "MTAL limit ");
        knownTags.put("DF1B", "LCOL limit");

        knownTags.put("DF1C", "Upper Consecutive Offline Limit (UCOL)");
        knownTags.put("DF1D", "IADOL");

        knownTags.put("DF1E", "Derivation key Index");
        knownTags.put("DF30", "Fuel Card usage bitmap [Prompting], ATC Limit");
        knownTags.put("DF31", "Encrypted PIN cryptography failure limit");
        knownTags.put("DF32", "Purchase Restrictions (WEX refers to this as Chip Offline Purchase Restriction), Failed MAC limit");
        knownTags.put("DF33", "Lifetime MAC Limit");
        knownTags.put("DF34", "Chip Offline Purchase Restrictions Amount for Fuel*, Session MAC Limit");
        knownTags.put("DF35", "Chip Offline Purchase Restrictions Amount for non-Fuel*");

        knownTags.put("DF36", "Relationship Codes*");
        knownTags.put("DF37", "3rd Party Reference Data Generation 2*");
        knownTags.put("DF38", "Loyalty ID*");
        knownTags.put("DF39", "Purchase Device Sequence Number (with the suffix)* ");
        knownTags.put("DF40", "DDOL Related Data Length");
        knownTags.put("DF41", "CCDOL2 Related Data Length");
        knownTags.put("DF4D", "Transaction Log Setting parameter31");


        dataTypes = new HashMap<>();
        dataTypes.put("82", "b");
        dataTypes.put("8E", "b");
        dataTypes.put("95", "b");
        dataTypes.put("9B", "b");
        dataTypes.put("9F07", "b");
        dataTypes.put("9F33", "b");
        dataTypes.put("9F40", "b");
        dataTypes.put("9F5B", "b");

        cardHolderLanguages = new HashMap<>();
        cardHolderLanguages.put("656E", "en-US");
        cardHolderLanguages.put("6672", "fr-CA");
        cardHolderLanguages.put("6573", "es");
    }

    public static EmvData parseTagData(String tagData) {
        return parseTagData(tagData, false);
    }
    public static EmvData parseTagData(String tagData, boolean verbose) {
        if(StringUtils.isNullOrEmpty(tagData)) {
            return null;
        }

        tagData = tagData.toUpperCase();

        EmvData rvalue = new EmvData();

        for(int i = 0; i < tagData.length();) {
            try {
                String tagName = tagData.substring(i, i = i + 2);
                if((Integer.parseInt(tagName, 16) & 0x1F) == 0x1F) {
                    tagName += tagData.substring(i, i = i + 2);
                }

                String lengthStr = tagData.substring(i, i = i + 2);
                int length = Integer.parseInt(lengthStr, 16);
                if(length > 127) {
                    int bytesLength = length - 128;
                    lengthStr = tagData.substring(i, i = i + (bytesLength * 2));
                    length = Integer.parseInt(lengthStr, 16);
                }
                length *= 2;

                String value = tagData.substring(i, i = i + length);

                if(!blackList.containsKey(tagName)) {
                    TlvData approvedTag = new TlvData(tagName, lengthStr, value, knownTags.get(tagName));
                    if(tagName.equals("5F28") && !value.equals("840")) {
                        rvalue.setStandInStatus(false, "Card is not domestically issued");
                    }
                    else if(tagName.equals("95")) {
                        byte[] valueBuffer = StringUtils.bytesFromHex(value);
                        byte[] maskBuffer = StringUtils.bytesFromHex("FC50FC2000");

                        for(int idx = 0; idx < valueBuffer.length; idx++) {
                            if((valueBuffer[idx] & maskBuffer[idx]) != 0x00) {
                                rvalue.setStandInStatus(false, String.format("Invalid TVR status in byte %s of tag 95", idx + 1));
                            }
                        }
                    }
                    else if(tagName.equals("9B")) {
                        byte[] valueBuffer = StringUtils.bytesFromHex(value);
                        byte[] maskBuffer = StringUtils.bytesFromHex("E800");

                        for(int idx = 0; idx < valueBuffer.length; idx++) {
                            if((valueBuffer[idx] & maskBuffer[idx]) != maskBuffer[idx]) {
                                rvalue.setStandInStatus(false, String.format("Invalid TSI status in byte %s of tag 9B", idx + 1));
                            }
                        }
                    }

                    rvalue.addTag(approvedTag);
                }
                else {
                    rvalue.addRemovedTag(tagName, lengthStr, value, blackList.get(tagName));
                }
            }
            catch(NumberFormatException | IndexOutOfBoundsException exc) { /* NOM NOM*/ }
        }

        if(verbose) {
            System.out.println("Accepted Tags:");
            for(String tagName: rvalue.getAcceptedTags().keySet()) {
                TlvData tag = rvalue.getTag(tagName);
                boolean appendBinary = dataTypes.containsKey(tagName);

                System.out.printf("TAG: %s - %s%n", tagName, tag.getDescription());
                System.out.printf("%s: %s%s\r\n%n",tag.getLength(), tag.getValue(), appendBinary ? String.format(" [%s]", tag.getBinaryValue()) : "");
            }

            System.out.println("Removed Tags:");
            for(String tagName: rvalue.getRemovedTags().keySet()) {
                TlvData tag = rvalue.getRemovedTags().get(tagName);
                System.out.printf("TAG: %s - %s%n", tagName, tag.getDescription());
                if("57".equals(tagName)){
                    System.out.printf("%s: %s\r\n%n",tag.getLength(), StringUtils.maskTrackData(tag.getValue()));
                } else if ("5A".equals(tagName)) {
                    System.out.printf("%s: %s\r\n%n",tag.getLength(), StringUtils.maskAccountNumber(tag.getValue()));
                } else if ("5F24".equals(tagName)) {
                    System.out.printf("%s: %s\r\n%n",tag.getLength(), tag.getValue().replace(tag.getValue(),"******"));
                }else {
                    System.out.printf("%s: %s\r\n%n", tag.getLength(), tag.getValue());
                }
            }
        }

        return rvalue;
    }

    public static String mapCardHolderLanguage(String tagValue) {
        if(!StringUtils.isNullOrEmpty(tagValue)) {
            if(cardHolderLanguages.containsKey(tagValue)){
                return cardHolderLanguages.get(tagValue);
            }
            return tagValue;
        }
        return tagValue;
    }
}
