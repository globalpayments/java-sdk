package com.global.api.paymentMethods;

import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CreditCardData extends Credit implements ICardData {
    private static final Pattern AmexRegex = Pattern.compile("^3[47][0-9]{13}$");
    private static final Pattern MasterCardRegex = Pattern.compile("^5[1-5][0-9]{14}$");
    private static final Pattern VisaRegex = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern DinersClubRegex = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]{11}$");
    private static final Pattern RouteClubRegex = Pattern.compile("^(2014|2149)");
    private static final Pattern DiscoverRegex = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");
    private static final Pattern JcbRegex = Pattern.compile("^(?:2131|1800|35\\d{3})\\d{11}$");

    private String cardHolderName;
    private boolean cardPresent = false;
    private String cvn;
    private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent = false;
    private Map<String,Pattern> regexMap;

    public String getCardHolderName() {
        return cardHolderName;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    public String getCardType() {
        String cardType = "Unknown";

        try {
            String cardNum = number.replace(" ", "").replace("-", "");
            for (Map.Entry<String, Pattern> kvp : regexMap.entrySet()) {
                if (kvp.getValue().matcher(cardNum).find()) {
                    cardType = kvp.getKey();
                    break;
                }
            }

        } catch (Exception e) {
            cardType = "Unknown";
        }

        return cardType;
    }
    public boolean isCardPresent() {
        return cardPresent;
    }
    public void setCardPresent(boolean cardPresent) {
        this.cardPresent = cardPresent;
    }
    public String getCvn() {
        return cvn;
    }
    public void setCvn(String cvn) {
        if(cvn != null && !cvn.equals("")) {
            this.cvn = cvn;
            this.cvnPresenceIndicator = CvnPresenceIndicator.Present;
        }
    }
    public CvnPresenceIndicator getCvnPresenceIndicator() {
        return cvnPresenceIndicator;
    }
    public void setCvnPresenceIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        this.cvnPresenceIndicator = cvnPresenceIndicator;
    }
    public Integer getExpMonth() {
        return expMonth;
    }
    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }
    public Integer getExpYear() {
        return expYear;
    }
    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getShortExpiry() {
        return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
    }
    public boolean isReaderPresent() {
        return readerPresent;
    }
    public void setReaderPresent(boolean readerPresent) {
        this.readerPresent = readerPresent;
    }

    public CreditCardData() {
        regexMap = new HashMap<String,Pattern>();
        regexMap.put("Amex", AmexRegex);
        regexMap.put("MC", MasterCardRegex);
        regexMap.put("Visa", VisaRegex);
        regexMap.put("DinersClub", DinersClubRegex);
        regexMap.put("EnRoute", RouteClubRegex);
        regexMap.put("Discover", DiscoverRegex);
        regexMap.put("Jcb", JcbRegex);
    }
    public CreditCardData(String token) {
        this();
        this.setToken(token);
    }
}
