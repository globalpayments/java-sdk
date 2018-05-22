package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.MerchantDataCollection;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.enums.DccProcessor;
import com.global.api.entities.enums.DccRateType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
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
    private String cardType = "Unknown";
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
    public String getCardType() { return cardType; }
    public void setCardType(String value) {
        cardType = value;
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

    public DccRateData getDccRate(DccRateType dccRateType, BigDecimal amount, String currency, DccProcessor ccp) throws ApiException {
		Transaction response = new AuthorizationBuilder(TransactionType.DccRateLookup, this)
				.withAmount(amount)
				.withCurrency(currency)
				.withDccRateType(dccRateType)
				.withDccProcessor(ccp)
				.withDccType("1")
				.execute();

		DccRateData dccValues = new DccRateData();
		dccValues.setOredrId(response.getOrderId());
		dccValues.setDccProcessor(ccp.getValue());
		dccValues.setDccType("1");
		dccValues.setDccRateType(dccRateType.getValue());
		dccValues.setDccRate(response.getDccResponseResult().getCardHolderRate());
		dccValues.setCurrency(response.getDccResponseResult().getCardHolderCurrency());
		dccValues.setAmount(response.getDccResponseResult().getCardHolderAmount());

		return dccValues;
	}

    public boolean verifyEnrolled(BigDecimal amount, String currency) throws ApiException {
        return verifyEnrolled(amount, currency, null, "default");
    }
    public boolean verifyEnrolled(BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifyEnrolled(amount, currency, orderId, "default");
    }
    public boolean verifyEnrolled(BigDecimal amount, String currency, String orderId, String configName) throws ApiException {
        Transaction response;
        try{
            response = new AuthorizationBuilder(TransactionType.VerifyEnrolled, this)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderId(orderId)
                    .execute(configName);

        }
        catch (GatewayException exc) {
            if(exc.getResponseCode().equals("110"))
                return false;
            throw exc;
        }

        ThreeDSecure secureEcom = response.getThreeDsecure();
        if(secureEcom != null && secureEcom.isEnrolled()) {
            this.threeDSecure = secureEcom;
            this.threeDSecure.setAmount(amount);
            this.threeDSecure.setCurrency(currency);
            this.threeDSecure.setOrderId(response.getOrderId());
            return true;
        }
        return false;
    }

    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifySignature(authorizationResponse, amount, currency, orderId, "default");
    }
    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId, String configName) throws ApiException {
        // ensure we have an object
        if(this.threeDSecure == null)
            this.threeDSecure = new ThreeDSecure();

        this.threeDSecure.setAmount(amount);
        this.threeDSecure.setCurrency(currency);
        this.threeDSecure.setOrderId(orderId);

        return verifySignature(authorizationResponse, null, configName);
    }
    public boolean verifySignature(String authorizationResponse) throws ApiException {
        return verifySignature(authorizationResponse, null, "default");
    }
    public boolean verifySignature(String authorizationResponse, MerchantDataCollection merchantData) throws ApiException {
        return verifySignature(authorizationResponse, merchantData, "default");
    }
    public boolean verifySignature(String authorizationResponse, MerchantDataCollection merchantData, String configName) throws ApiException {
        // ensure we have an object
        if(this.threeDSecure == null)
            this.threeDSecure = new ThreeDSecure();

        // if we have merchant data use it
        if(merchantData != null)
            this.threeDSecure.setMerchantData(merchantData);

        TransactionReference ref = new TransactionReference();
        ref.setOrderId(threeDSecure.getOrderId());

        Transaction response = new ManagementBuilder(TransactionType.VerifySignature)
                .withAmount(threeDSecure.getAmount())
                .withCurrency(threeDSecure.getCurrency())
                .withPayerAuthenticationResponse(authorizationResponse)
                .withPaymentMethod(ref)
                .execute(configName);

        if(response.getResponseCode().equals("00")) {
            this.threeDSecure.setStatus(response.getThreeDsecure().getStatus());
            this.threeDSecure.setEci(response.getThreeDsecure().getEci());
            this.threeDSecure.setCavv(response.getThreeDsecure().getCavv());
            this.threeDSecure.setAlgorithm(response.getThreeDsecure().getAlgorithm());
            this.threeDSecure.setStatus(response.getThreeDsecure().getXid());
            return true;
        }
        return false;
    }
}
