package com.global.api.gateways;

import com.global.api.builders.ProPayBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.propay.*;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProPayConnector extends XmlGateway implements IProPayProvider{
    @Getter @Setter
    private String certStr;
    @Getter @Setter
    private String terminalID;
    @Getter @Setter
    private String x509CertPath;
    @Getter @Setter
    private String x509Base64String;

    @Override
    public Transaction processProPay(ProPayBuilder builder) throws ApiException {
        updateGatewaySettings(builder);

        ElementTree et = new ElementTree();
        Element request = et.element("XMLRequest");

        // Credentials
        et.subElement(request, "certStr", certStr);
        et.subElement(request, "termid", terminalID);
        et.subElement(request, "class", "partner");

        //Transaction
        Element xmlTrans = et.subElement(request, "XMLTrans");
        et.subElement(xmlTrans, "transType", mapRequestType(builder));

        // Account Details
        hydrateAccountDetails(et, xmlTrans, builder);
        String response = doTransaction(et.toString(request));
        return mapResponse(builder, response);
    }

    private void updateGatewaySettings(ProPayBuilder builder) throws GatewayException {
        List<TransactionType> certTransactions=new ArrayList<>();
        certTransactions.add(TransactionType.EditAccount);
        certTransactions.add(TransactionType.ObtainSSOKey);
        certTransactions.add(TransactionType.UpdateBankAccountOwnership);
        certTransactions.add(TransactionType.AddFunds);
        certTransactions.add(TransactionType.AddCardFlashFunds);

        if (certTransactions.contains(builder.getTransactionType())) {
            setHeaders(setX509Certificate());
        }
    }

    private HashMap<String,String> setX509Certificate() throws GatewayException {
        HashMap<String,String> headers=new HashMap<>();
        try {
            if(!StringUtils.isNullOrEmpty(x509CertPath)){
                Path path = Paths.get(x509CertPath);
                headers.put("X509Certificate",Base64.encodeBase64String(Files.readAllBytes(path)));
            } else if (!StringUtils.isNullOrEmpty(x509Base64String))
                headers.put("X509Certificate",x509Base64String);
            else
                throw new BuilderException("X509 Certificate was not provided.");
        }
        catch (Exception e) {
            throw new GatewayException("X509 Certificate Error", e);
        }
        return headers;
    }

    public String mapRequestType(ProPayBuilder builder) throws UnsupportedTransactionException {
        switch (builder.getTransactionType()) {
            case CreateAccount:
                return "01";
            case EditAccount:
                return "42";
            case ResetPassword:
                return "32";
            case RenewAccount:
                return "39";
            case UpdateBeneficialOwnership:
                return "44";
            case DisownAccount:
                return "41";
            case UploadDocumentChargeback:
                return "46";
            case UploadDocument:
                return "47";
            case ObtainSSOKey:
                return "300";
            case UpdateBankAccountOwnership:
                return "210";
            case AddFunds:
                return "37";
            case SweepFunds:
                return "38";
            case AddCardFlashFunds:
                return "209";
            case PushMoneyFlashFunds:
                return "45";
            case DisburseFunds:
                return "02";
            case SpendBack:
                return "11";
            case ReverseSplitPay:
                return "43";
            case SplitFunds:
                return "16";
            case GetAccountDetails:
                // We are using the Additional TransactionModifier to differentiate between GetAccountDetails and GetAccountDetailsEnhanced
                if (builder.getTransactionModifier() == TransactionModifier.Additional)
                    return "19";
                // If the TransactionModifier isn't "Additional" then it is either "None" or an unsupported value that should be treated as "None"
                return "13";
            case GetAccountBalance:
                return "14";
            case OrderDevice:
                return "430";
            default:
                throw new UnsupportedTransactionException();
        }
    }

    public Transaction mapResponse(ProPayBuilder builder, String rawResponse) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get("XMLResponse");
        String responseCode = root.getString("status");

        if (!responseCode.equals("00")) {
            throw new GatewayException(String.format("Unexpected Gateway Response: %s", responseCode));
        }

         ProPayResponseData proPayResponse = populateProPayResponse(builder, root);

        Transaction response=new Transaction();
        response.setProPayResponseData(proPayResponse);
        response.setResponseCode(responseCode);

        return response;
    }

    private ProPayResponseData populateProPayResponse(ProPayBuilder builder, Element root)
    {
        if (builder.getTransactionType() == TransactionType.GetAccountDetails && builder.getTransactionModifier() == TransactionModifier.Additional) {
            return populateResponseWithEnhancedAccountDetails(root);
        }
        else {
            ProPayResponseData responseData=new ProPayResponseData();
            responseData.setAccountNumber(getAccountNumberFromResponse(root));
            responseData.setRecAccountNum(root.getString("recAccntNum"));
            responseData.setPassword(root.getString("password"));
            responseData.setAmount(root.getString("amount"));
            responseData.setTransNum(root.getString("transNum"));
            responseData.setPending(root.getString("pending"));
            responseData.setSecondaryAmount(root.getString("secondaryAmount"));
            responseData.setSecondaryTransNum(root.getString("secondaryTransNum"));
            responseData.setSourceEmail(root.getString("sourceEmail"));
            responseData.setAuthToken(root.getString("AuthToken"));
            responseData.setBeneficialOwnerDataResults(getBeneficialOwnerDataResultsFromResponse(root));
            responseData.setAccountStatus(root.getString("accntStatus"));
            responseData.setPhysicalAddress(getPhysicalAddressFromResponse(root));
            responseData.setAffiliation(root.getString("affiliation"));
            responseData.setApiReady(root.getString("apiReady"));
            responseData.setCurrencyCode(root.getString("currencyCode"));
            responseData.setExpiration(root.getString("expiration"));
            responseData.setSignupDate(root.getString("signupDate"));
            responseData.setTier(root.getString("tier"));
            responseData.setVisaCheckoutMerchantID(root.getString("visaCheckoutMerchantId"));
            responseData.setCreditCardTransactionLimit(root.getString("CreditCardTransactionLimit"));
            responseData.setCreditCardMonthLimit(root.getString("CreditCardMonthLimit"));
            responseData.setAchPaymentPerTranLimit(root.getString("ACHPaymentPerTranLimit"));
            responseData.setAchPaymentMonthLimit(root.getString("ACHPaymentMonthLimit"));
            responseData.setCreditCardMonthlyVolume(root.getString("CreditCardMonthlyVolume"));
            responseData.setAchPaymentMonthlyVolume(root.getString("ACHPaymentMonthlyVolume"));
            responseData.setReserveBalance(root.getString("ReserveBalance"));
            responseData.setMasterPassCheckoutMerchantID(root.getString("MasterPassCheckoutMerchantId"));
            responseData.setPendingAmount(root.getString("pendingAmount"));
            responseData.setReserveAmount(root.getString("reserveAmount>"));
            responseData.setAchOut(getACHOutBalanceInfoFromResponse(root));
            responseData.setFlashFunds(getFlashFundsBalanceInfoFromResponse(root));
            return  responseData;
        }
    }

    private ProPayResponseData populateResponseWithEnhancedAccountDetails(Element root) {
        ProPayResponseData responseData=new ProPayResponseData();
        responseData.setAccountNumber(getAccountNumberFromResponse(root));

        UserPersonalData personalData=new UserPersonalData();
        personalData.setSourceEmail(root.getString("sourceEmail"));
        personalData.setFirstName(root.getString("firstName"));
        personalData.setMiddleInitial(root.getString("middleInitial"));
        personalData.setLastName(root.getString("lastName"));
        personalData.setDayPhone(root.getString("dayPhone"));
        personalData.setEveningPhone(root.getString("evenPhone"));
        personalData.setExternalID(root.getString("externalId"));
        personalData.setTier(root.getString("tier"));
        personalData.setCurrencyCode(root.getString("currencyCode"));
        personalData.setNotificationEmail(root.getString("notificationEmail"));

        Address homeAddress=new Address();
        homeAddress.setStreetAddress1(root.getString("addr"));
        homeAddress.setStreetAddress2(root.getString("aptNum"));
        homeAddress.setCity(root.getString("city"));
        homeAddress.setState(root.getString("state"));
        homeAddress.setPostalCode(root.getString("postalCode"));
        homeAddress.setCountry(root.getString("country"));
        personalData.setPersonalAddress(homeAddress);

        Address mailAddress=new Address();
        mailAddress.setStreetAddress1(root.getString("mailAddr"));
        mailAddress.setStreetAddress2(root.getString("mailApt"));
        mailAddress.setCity(root.getString("mailCity"));
        mailAddress.setState(root.getString("mailState"));
        mailAddress.setPostalCode(root.getString("mailPostalCode"));
        mailAddress.setCountry(root.getString("mailCountry"));
        personalData.setMailingAddress(mailAddress);

        BusinessData businessData=new BusinessData();
        businessData.setBusinessLegalName(root.getString("businessLegalName"));
        businessData.setDoingBusinessAs(root.getString("doingBusinessAs"));
        businessData.setEmployerIdentificationNumber(root.getString("ein"));


        Address businessAddress=new Address();
        businessAddress.setStreetAddress1(root.getString("businessAddress"));
        businessAddress.setStreetAddress2(root.getString("businessAddress2"));
        businessAddress.setCity(root.getString("businessCity"));
        businessAddress.setState(root.getString("businessState"));
        businessAddress.setPostalCode(root.getString("businessZip"));

        businessData.setWebsiteURL(root.getString("websiteURL"));
        businessData.setAverageTicket(root.getString("averageTicket"));
        businessData.setHighestTicket(root.getString("highestTicket"));
        businessData.setBusinessAddress(businessAddress);
        responseData.setBusinessData(businessData);

        AccountPermissions accountLimits=new AccountPermissions();
        accountLimits.setCreditCardTransactionLimit(root.getString("creditCardTransactionLimit"));
        accountLimits.setCreditCardMonthLimit(root.getString("creditCardMonthLimit"));
        accountLimits.setACHPaymentSoftLimitEnabled(root.getString("achPaymentSoftLimitEnabled").equalsIgnoreCase("Y"));
        accountLimits.setAchPaymentACHOffPercent(root.getString("achPaymentAchOffPercent"));
        accountLimits.setSoftLimitEnabled(root.getString("softLimitEnabled").equalsIgnoreCase("Y"));
        accountLimits.setSoftLimitACHOffPercent(root.getString("softLimitAchOffPercent"));
        responseData.setAccountLimits(accountLimits);

        responseData.setAchPaymentPerTranLimit(root.getString("achPaymentPerTranLimit"));
        responseData.setAchPaymentMonthLimit(root.getString("achPaymentMonthLimit"));
        responseData.setAchPaymentMonthlyVolume(root.getString("achPaymentMonthlyVolume"));
        responseData.setCreditCardMonthlyVolume(root.getString("creditCardMonthlyVolume"));
        responseData.setAvailableBalance(root.getString("availableBalance"));
        responseData.setPendingBalance(root.getString("pendingBalance"));
        responseData.setReserveBalance(root.getString("reserveBalance"));

        BankAccountData primaryBankAccountData=new BankAccountData();
        primaryBankAccountData.setAccountCountryCode(root.getString("primaryAccountCountryCode"));
        primaryBankAccountData.setAccountType(root.getString("primaryAccountType"));
        primaryBankAccountData.setAccountOwnershipType(root.getString("primaryAccountOwnershipType"));
        primaryBankAccountData.setBankName(root.getString("primaryBankName"));
        primaryBankAccountData.setAccountNumber(root.getString("primaryAccountNumberLast4"));
        primaryBankAccountData.setRoutingNumber(root.getString("primaryRoutingNumber"));
        responseData.setPrimaryBankAccountData(primaryBankAccountData);

        BankAccountData secondaryBankAccountData=new BankAccountData();
        secondaryBankAccountData.setAccountCountryCode(root.getString("secondaryAccountCountryCode"));
        secondaryBankAccountData.setAccountType(root.getString("secondaryAccountType"));
        secondaryBankAccountData.setAccountOwnershipType(root.getString("secondaryAccountOwnershipType"));
        secondaryBankAccountData.setBankName(root.getString("secondaryBankName"));
        secondaryBankAccountData.setAccountNumber(root.getString("secondaryAccountNumberLast4"));
        secondaryBankAccountData.setRoutingNumber(root.getString("secondaryRoutingNumber"));
        responseData.setSecondaryBankAccountData(secondaryBankAccountData);

        GrossBillingInformation grossBillingInformation= new GrossBillingInformation();
        BankAccountData grossSettleBankData = new BankAccountData();
        grossSettleBankData.setAccountName(root.getString("grossSettleAccountHolderName"));
        grossSettleBankData.setAccountNumber(root.getString("grossSettleAccountNumberLast4"));
        grossSettleBankData.setRoutingNumber(root.getString("grossSettleRoutingNumber"));
        grossSettleBankData.setAccountType(root.getString("grossSettleAccountType"));

        Address grossSettleAddress = new Address();
        grossSettleAddress.setStreetAddress1(root.getString("grossSettleAccountAddress"));
        grossSettleAddress.setCity(root.getString("grossSettleAccountCity"));
        grossSettleAddress.setState(root.getString("grossSettleAccountState"));
        grossSettleAddress.setCountryCode(root.getString("grossSettleAccountCountryCode"));
        grossSettleAddress.setPostalCode(root.getString("grossSettleAccountZipCode"));

        grossBillingInformation.setGrossSettleBankData(grossSettleBankData);
        grossBillingInformation.setGrossSettleAddress(grossSettleAddress);
        responseData.setGrossBillingInformation(grossBillingInformation);

        return responseData;
    }

    private String getAccountNumberFromResponse(Element root) {
        // ProPay API 4.1 (Create an account) has the account number specified in the response as "accntNum"
        // All other methods specify it as "accountNum" in the response
        if (root.has("accntNum")) {
            return root.getString("accntNum");
        }
        else {
            return root.getString("accountNum");
        }
    }

    private List<BeneficialOwnerDataResult> getBeneficialOwnerDataResultsFromResponse(Element root) {

        if (root.has("beneficialOwnerDataResult")) {
            List<BeneficialOwnerDataResult> beneficialOwnerDataResults = new ArrayList<>();
            for (Element owner : root.getAll("Owner"))
            {
                BeneficialOwnerDataResult beneficialOwnerData = new BeneficialOwnerDataResult();
                beneficialOwnerData.setFirstName(owner.getString("FirstName"));
                beneficialOwnerData.setLastName(owner.getString("LastName"));
                beneficialOwnerData.setStatus(owner.getString("Status"));
                 beneficialOwnerDataResults.add(beneficialOwnerData) ;
            }
            return beneficialOwnerDataResults;
        }
        return null;
    }

    private Address getPhysicalAddressFromResponse(Element root) {
        if (root.has("addr") ||
                root.has("city") ||
                root.has("state") ||
                root.has("zip"))
        {
            Address addr = new Address();
            addr.setStreetAddress1(root.getString("addr"));
            addr.setCity(root.getString("city"));
            addr.setState(root.getString("state"));
            addr.setPostalCode(root.getString("zip"));
            return addr;
        }
        return null;
    }

    private AccountBalanceResponseData getACHOutBalanceInfoFromResponse(Element root) {
        if (root.has("achOut")) {
            AccountBalanceResponseData  accountBalanceData = new AccountBalanceResponseData();
            accountBalanceData.setEnabled(root.getString("enabled"));
            accountBalanceData.setLimitRemaining(root.getString("limitRemaining"));
            accountBalanceData.setTransferFee(root.getString("transferFee"));
            accountBalanceData.setFeeType(root.getString("feeType"));
            accountBalanceData.setAccountLastFour(root.getString("accountLastFour"));

            return accountBalanceData;
        }
        return null;
    }

    private AccountBalanceResponseData getFlashFundsBalanceInfoFromResponse(Element root) {
        if (root.has("flashFunds")) {
            AccountBalanceResponseData  accountBalanceData = new AccountBalanceResponseData();
            accountBalanceData.setEnabled(root.getString("enabled"));
            accountBalanceData.setLimitRemaining(root.getString("limitRemaining"));
            accountBalanceData.setTransferFee(root.getString("transferFee"));
            accountBalanceData.setFeeType(root.getString("feeType"));
            accountBalanceData.setAccountLastFour(root.getString("accountLastFour"));

            return accountBalanceData;
        }
        return null;
    }

    private void hydrateAccountDetails(ElementTree xml, Element xmlTrans, ProPayBuilder builder) {
        xml.subElement(xmlTrans, "accountNum", builder.getAccountNumber());
        xml.subElement(xmlTrans, "sourceEmail", builder.getSourceEmail());
        xml.subElement(xmlTrans, "externalId", builder.getExternalID());
        xml.subElement(xmlTrans, "recAccntNum", builder.getReceivingAccountNumber());
        xml.subElement(xmlTrans, "amount", builder.getAmount());

        if(builder.getAllowPending() != null) {
            xml.subElement(xmlTrans, "allowPending", builder.getAllowPending() == true ? "Y" : "N");
        }
        xml.subElement(xmlTrans, "password", builder.getPassword());

        if (builder.getAccountPermissions() != null)

        {
            hydrateAccountPermissions(xml, xmlTrans, builder.getAccountPermissions());//doubt
        }

        if (builder.getUserPersonalData() != null) {
            hydrateUserPersonalData(xml, xmlTrans, builder.getUserPersonalData());
        }

        if (builder.getBusinessData() != null) {
            hydrateBusinessData(xml, xmlTrans, builder.getBusinessData());
        }

        hydrateBankDetails(xml, xmlTrans, builder);

        if (builder.getMailingAddressInofmation() != null) {
            hydrateMailAddress(xml, xmlTrans, builder.getMailingAddressInofmation());
        }

        if (builder.getThreatRiskData() != null) {
            hydrateThreatRiskData(xml, xmlTrans, builder.getThreatRiskData());
        }

        if (builder.getSignificantOwnerData() != null) {
            hydrateSignificantOwnerData(xml, xmlTrans, builder.getSignificantOwnerData());
        }

        if (!StringUtils.isNullOrEmpty(builder.getTimeZone())) {
            Element timezoneElement = xml.subElement(xmlTrans, "TimeZone", builder.getTimeZone());
        }

        if (builder.getDeviceData() != null) {
            hydrateDeviceData(xml, xmlTrans, builder.getDeviceData());
        }

        if (builder.getBeneficialOwnerData() != null) {
            hydrateBeneficialOwnerData(xml, xmlTrans, builder.getBeneficialOwnerData());
        }

        if (builder.getGrossBillingInformation() != null) {
            hydrateGrossBillingData(xml, xmlTrans, builder.getGrossBillingInformation());
        }

        if (builder.getRenewalAccountData() != null) {
            hydrateAccountRenewDetails(xml, xmlTrans, builder.getRenewalAccountData());
        }

        if (builder.getFlashFundsPaymentCardData() != null) {
            hydrateFlashFundsPaymentCardData(xml, xmlTrans, builder.getFlashFundsPaymentCardData());
        }

        if (builder.getDocumentUploadData() != null) {
            hydrateDocumentUploadData(xml, xmlTrans, builder.getTransactionType(), builder.getDocumentUploadData());
        }

        if (builder.getSSORequestData() != null) {
            hydrateSSORequestData(xml, xmlTrans, builder.getSSORequestData());
        }

        hydrateBankAccountOwnershipData(xml, xmlTrans, builder);

        xml.subElement(xmlTrans, "ccAmount", builder.getCCAmount());
       if (builder.getRequireCCRefund() != null) {
           xml.subElement(xmlTrans, "requireCCRefund", builder.getRequireCCRefund() == true ? "Y" : "N");
       }
        xml.subElement(xmlTrans, "transNum", builder.getTransNum());
        xml.subElement(xmlTrans, "gatewayTransactionId", builder.getGatewayTransactionId());
        xml.subElement(xmlTrans, "cardBrandTransactionId", builder.getCardBrandTransactionId());
        xml.subElement(xmlTrans, "globaltransId", builder.getGlobalTransId());
        xml.subElement(xmlTrans, "globalTransSource", builder.getGlobalTransSource());

        if (builder.getOrderDevice() != null){
            hydrateOrderDeviceData(xml,xmlTrans, builder.getOrderDevice(), builder);
        }
    }

    private void hydrateUserPersonalData(ElementTree xml, Element xmlTrans, UserPersonalData userPersonalData) {
        xml.subElement(xmlTrans, "firstName", userPersonalData.getFirstName());
        xml.subElement(xmlTrans, "mInitial", userPersonalData.getMiddleInitial());
        xml.subElement(xmlTrans, "lastName", userPersonalData.getLastName());
        xml.subElement(xmlTrans, "dob", userPersonalData.getDateOfBirth());
        xml.subElement(xmlTrans, "ssn", userPersonalData.getSsn());
        xml.subElement(xmlTrans, "sourceEmail", userPersonalData.getSourceEmail());
        xml.subElement(xmlTrans, "dayPhone", userPersonalData.getDayPhone());
        xml.subElement(xmlTrans, "evenPhone", userPersonalData.getEveningPhone());
        xml.subElement(xmlTrans, "NotificationEmail", userPersonalData.getNotificationEmail());
        xml.subElement(xmlTrans, "currencyCode", userPersonalData.getCurrencyCode());
        xml.subElement(xmlTrans, "tier", userPersonalData.getTier());
        xml.subElement(xmlTrans, "externalId", userPersonalData.getExternalID());
        xml.subElement(xmlTrans, "userId", userPersonalData.getUserID());
        xml.subElement(xmlTrans, "IpSignup", userPersonalData.getIpSignup());
        xml.subElement(xmlTrans, "USCitizen", userPersonalData.isUsCitizen()?"TRUE":"FALSE");
        xml.subElement(xmlTrans, "BOAttestation", userPersonalData.isBOAttestation()?"TRUE":"FALSE");
        xml.subElement(xmlTrans, "TermsAcceptanceIP", userPersonalData.getTermsAcceptanceIP());
        xml.subElement(xmlTrans, "TermsAcceptanceTimeStamp", DateTime.now().toString());
        xml.subElement(xmlTrans, "TermsVersion", userPersonalData.getTermsVersion()==null?null:userPersonalData.getTermsVersion().getValue());
        xml.subElement(xmlTrans, "nationality", userPersonalData.getNationality());
        xml.subElement(xmlTrans, "addr", userPersonalData.getPersonalAddress().getStreetAddress1());
        xml.subElement(xmlTrans, "aptNum", userPersonalData.getPersonalAddress().getStreetAddress2());
        xml.subElement(xmlTrans, "addr3", userPersonalData.getPersonalAddress().getStreetAddress3());
        xml.subElement(xmlTrans, "city", userPersonalData.getPersonalAddress().getCity());
        xml.subElement(xmlTrans, "state", userPersonalData.getPersonalAddress().getState());
        xml.subElement(xmlTrans, "zip", userPersonalData.getPersonalAddress().getPostalCode());
        xml.subElement(xmlTrans, "country", userPersonalData.getPersonalAddress().getCountryCode());

    }

    private void hydrateBusinessData(ElementTree xml, Element xmlTrans, BusinessData businessData) {
        xml.subElement(xmlTrans, "BusinessLegalName", businessData.getBusinessLegalName());
        xml.subElement(xmlTrans, "DoingBusinessAs", businessData.getDoingBusinessAs());
        xml.subElement(xmlTrans, "EIN", businessData.getEmployerIdentificationNumber());
        xml.subElement(xmlTrans, "MCCCode", businessData.getMerchantCategoryCode());
        xml.subElement(xmlTrans, "WebsiteURL", businessData.getWebsiteURL());
        xml.subElement(xmlTrans, "BusinessDesc", businessData.getBusinessDescription());
        xml.subElement(xmlTrans, "MonthlyBankCardVolume", businessData.getMonthlyBankCardVolume());
        xml.subElement(xmlTrans, "AverageTicket", businessData.getAverageTicket());
        xml.subElement(xmlTrans, "HighestTicket", businessData.getHighestTicket());
        xml.subElement(xmlTrans, "BusinessAddress", businessData.getBusinessAddress().getStreetAddress1());
        xml.subElement(xmlTrans, "BusinessAddress2", businessData.getBusinessAddress().getStreetAddress2());
        xml.subElement(xmlTrans, "BusinessCity", businessData.getBusinessAddress().getCity());
        xml.subElement(xmlTrans, "BusinessCountry", businessData.getBusinessAddress().getCountryCode());
        xml.subElement(xmlTrans, "BusinessState", businessData.getBusinessAddress().getState());
        xml.subElement(xmlTrans, "BusinessZip", businessData.getBusinessAddress().getPostalCode());
    }

    private void hydrateBankDetails(ElementTree xml, Element xmlTrans, ProPayBuilder builder) {
        if (builder.getCreditCardData() != null) {
            xml.subElement(xmlTrans, "NameOnCard", builder.getCreditCardData().getCardHolderName());
            xml.subElement(xmlTrans, "ccNum", builder.getCreditCardData().getNumber());
            xml.subElement(xmlTrans, "expDate", builder.getCreditCardData().getShortExpiry());
            xml.subElement(xmlTrans, "cvv2", builder.getCreditCardData().getCvn());
        }

        if (builder.getACHInofmation() != null) {
            xml.subElement(xmlTrans, "PaymentBankAccountNumber", builder.getACHInofmation().getAccountNumber());
            xml.subElement(xmlTrans, "PaymentBankRoutingNumber", builder.getACHInofmation().getRoutingNumber());
            xml.subElement(xmlTrans, "PaymentBankAccountType", builder.getACHInofmation().getAccountType());
        }

        if (builder.getBankAccountData() != null) {
            xml.subElement(xmlTrans, "AccountCountryCode", builder.getBankAccountData().getAccountCountryCode());
            xml.subElement(xmlTrans, "accountName", builder.getBankAccountData().getAccountName());
            xml.subElement(xmlTrans, "AccountNumber", builder.getBankAccountData().getAccountNumber());
            xml.subElement(xmlTrans, "AccountOwnershipType", builder.getBankAccountData().getAccountOwnershipType());
            xml.subElement(xmlTrans, "AccountType", builder.getBankAccountData().getAccountType());
            xml.subElement(xmlTrans, "BankName", builder.getBankAccountData().getBankName());
            xml.subElement(xmlTrans, "RoutingNumber", builder.getBankAccountData().getRoutingNumber());
        }

        if (builder.getSecondaryBankInofmation() != null) {
            xml.subElement(xmlTrans, "SecondaryAccountCountryCode", builder.getSecondaryBankInofmation().getAccountCountryCode());
            xml.subElement(xmlTrans, "SecondaryAccountName", builder.getSecondaryBankInofmation().getAccountName());
            xml.subElement(xmlTrans, "SecondaryAccountNumber", builder.getSecondaryBankInofmation().getAccountNumber());
            xml.subElement(xmlTrans, "SecondaryAccountOwnershipType", builder.getSecondaryBankInofmation().getAccountOwnershipType());
            xml.subElement(xmlTrans, "SecondaryAccountType", builder.getSecondaryBankInofmation().getAccountType());
            xml.subElement(xmlTrans, "SecondaryBankName", builder.getSecondaryBankInofmation().getBankName());
            xml.subElement(xmlTrans, "SecondaryRoutingNumber", builder.getSecondaryBankInofmation().getRoutingNumber());
        }

    }

    private void hydrateMailAddress(ElementTree xml, Element xmlTrans, Address mailingAddressInfo) {
        xml.subElement(xmlTrans, "mailAddr", mailingAddressInfo.getStreetAddress1());
        xml.subElement(xmlTrans, "mailApt", mailingAddressInfo.getStreetAddress2());
        xml.subElement(xmlTrans, "mailAddr3", mailingAddressInfo.getStreetAddress3());
        xml.subElement(xmlTrans, "mailCity", mailingAddressInfo.getCity());
        xml.subElement(xmlTrans, "mailCountry", mailingAddressInfo.getCountryCode());
        xml.subElement(xmlTrans, "mailState", mailingAddressInfo.getState());
        xml.subElement(xmlTrans, "mailZip", mailingAddressInfo.getPostalCode());
    }

    private void hydrateThreatRiskData(ElementTree xml, Element xmlTrans, ThreatRiskData threatRiskData) {
        xml.subElement(xmlTrans, "MerchantSourceip", threatRiskData.getMerchantSourceIP());
        xml.subElement(xmlTrans, "ThreatMetrixPolicy", threatRiskData.getThreatMetrixPolicy());
        xml.subElement(xmlTrans, "ThreatMetrixSessionid", threatRiskData.getThreatMetrixSessionID());
    }

    private void hydrateSignificantOwnerData(ElementTree xml, Element xmlTrans, SignificantOwnerData significantOwnerData) {
        xml.subElement(xmlTrans, "AuthorizedSignerFirstName", significantOwnerData.getAuthorizedSignerFirstName());
        xml.subElement(xmlTrans, "AuthorizedSignerLastName", significantOwnerData.getAuthorizedSignerLastName());
        xml.subElement(xmlTrans, "AuthorizedSignerTitle", significantOwnerData.getAuthorizedSignerTitle());
        xml.subElement(xmlTrans, "SignificantOwnerFirstName", significantOwnerData.getSignificantOwner().getFirstName());
        xml.subElement(xmlTrans, "SignificantOwnerLastName", significantOwnerData.getSignificantOwner().getLastName());
        xml.subElement(xmlTrans, "SignificantOwnerSSN", significantOwnerData.getSignificantOwner().getSsn());
        xml.subElement(xmlTrans, "SignificantOwnerDateOfBirth", significantOwnerData.getSignificantOwner().getDateOfBirth());
        xml.subElement(xmlTrans, "SignificantOwnerStreetAddress", significantOwnerData.getSignificantOwner().getOwnerAddress().getStreetAddress1());
        xml.subElement(xmlTrans, "SignificantOwnerCityName", significantOwnerData.getSignificantOwner().getOwnerAddress().getCity());
        xml.subElement(xmlTrans, "SignificantOwnerRegionCode", significantOwnerData.getSignificantOwner().getOwnerAddress().getState());
        xml.subElement(xmlTrans, "SignificantOwnerPostalCode", significantOwnerData.getSignificantOwner().getOwnerAddress().getPostalCode());
        xml.subElement(xmlTrans, "SignificantOwnerCountryCode", significantOwnerData.getSignificantOwner().getOwnerAddress().getCountryCode());
        xml.subElement(xmlTrans, "SignificantOwnerTitle", significantOwnerData.getSignificantOwner().getTitle());
        xml.subElement(xmlTrans, "SignificantOwnerPercentage", significantOwnerData.getSignificantOwner().getPercentage());
    }

    private void hydrateDeviceData(ElementTree xml, Element xmlTrans, DeviceData deviceData) {
        Element devices = xml.subElement(xmlTrans, "Devices");
        if (deviceData.getDevices().size() > 0) {
            for (DeviceInfo deviceInfo : deviceData.getDevices()) {
                Element device = xml.subElement(devices, "Device");
                xml.subElement(device, "Name", deviceInfo.getName());
                xml.subElement(device, "Quantity", deviceInfo.getQuantity());
                if (deviceInfo.getAttributes() != null && deviceInfo.getAttributes().size() > 0) {
                        Element attributes = xml.subElement(device, "Attributes");
                        for(DeviceAttributeInfo attributeInfo : deviceInfo.getAttributes()) {
                            Element item = xml.subElement(attributes, "Item");
                            item.set("Name", attributeInfo.getName());
                            item.set("Value", attributeInfo.getValue());
                    }
                }
            }
        }
    }

    private void hydrateBeneficialOwnerData(ElementTree xml, Element xmlTrans, BeneficialOwnerData beneficialOwnerData) {
        Element ownerDetails = xml.subElement(xmlTrans, "BeneficialOwnerData");
        xml.subElement(ownerDetails, "OwnerCount", beneficialOwnerData.getOwnersCount());

        if (beneficialOwnerData.getOwnersCount() > 0)
        {
            Element ownersList = xml.subElement(ownerDetails, "Owners");
            for(OwnersData ownerInfo : beneficialOwnerData.getOwnersList())
            {
                Element newOwner = xml.subElement(ownersList, "Owner");
                xml.subElement(newOwner, "FirstName", ownerInfo.getFirstName());
                xml.subElement(newOwner, "LastName", ownerInfo.getLastName());
                xml.subElement(newOwner, "Email", ownerInfo.getEmail());
                xml.subElement(newOwner, "SSN", ownerInfo.getSsn());
                xml.subElement(newOwner, "DateOfBirth", ownerInfo.getDateOfBirth());
                xml.subElement(newOwner, "Address", ownerInfo.getOwnerAddress().getStreetAddress1());
                xml.subElement(newOwner, "City", ownerInfo.getOwnerAddress().getCity());
                xml.subElement(newOwner, "State", ownerInfo.getOwnerAddress().getState());
                xml.subElement(newOwner, "Zip", ownerInfo.getOwnerAddress().getPostalCode());
                xml.subElement(newOwner, "Country", ownerInfo.getOwnerAddress().getCountryCode());
                xml.subElement(newOwner, "Title", ownerInfo.getTitle());
                xml.subElement(newOwner, "Percentage", ownerInfo.getPercentage());
            }
        }
    }

    private void hydrateGrossBillingData(ElementTree xml, Element xmlTrans, GrossBillingInformation grossBillingInformation) {
        xml.subElement(xmlTrans, "GrossSettleAddress", grossBillingInformation.getGrossSettleAddress().getStreetAddress1());
        xml.subElement(xmlTrans, "GrossSettleCity", grossBillingInformation.getGrossSettleAddress().getCity());
        xml.subElement(xmlTrans, "GrossSettleState", grossBillingInformation.getGrossSettleAddress().getState());
        xml.subElement(xmlTrans, "GrossSettleZipCode", grossBillingInformation.getGrossSettleAddress().getPostalCode());
        xml.subElement(xmlTrans, "GrossSettleCountry", grossBillingInformation.getGrossSettleAddress().getCountryCode());
        xml.subElement(xmlTrans, "GrossSettleCreditCardNumber", grossBillingInformation.getGrossSettleCreditCardData().getNumber());
        xml.subElement(xmlTrans, "GrossSettleNameOnCard", grossBillingInformation.getGrossSettleCreditCardData().getCardHolderName());
        xml.subElement(xmlTrans, "GrossSettleCreditCardExpDate", grossBillingInformation.getGrossSettleCreditCardData().getShortExpiry());
        xml.subElement(xmlTrans, "GrossSettleAccountCountryCode", grossBillingInformation.getGrossSettleBankData().getAccountCountryCode());
        xml.subElement(xmlTrans, "GrossSettleAccountHolderName", grossBillingInformation.getGrossSettleBankData().getAccountHolderName());
        xml.subElement(xmlTrans, "GrossSettleAccountNumber", grossBillingInformation.getGrossSettleBankData().getAccountNumber());
        xml.subElement(xmlTrans, "GrossSettleAccountType", grossBillingInformation.getGrossSettleBankData().getAccountType());
        xml.subElement(xmlTrans, "GrossSettleRoutingNumber", grossBillingInformation.getGrossSettleBankData().getRoutingNumber());
    }

    private void hydrateAccountPermissions(ElementTree xml, Element xmlTrans, AccountPermissions accountPermissions) {
        if (accountPermissions.isACHIn())
            xml.subElement(xmlTrans, "ACHIn", accountPermissions.isACHIn() ? "Y" : "N");
        if (accountPermissions.isACHOut())
            xml.subElement(xmlTrans, "ACHOut", accountPermissions.isACHOut() ? "Y" : "N");
        if (accountPermissions.isCCProcessing())
            xml.subElement(xmlTrans, "CCProcessing", accountPermissions.isCCProcessing() ? "Y" : "N");
        if (accountPermissions.isProPayIn())
            xml.subElement(xmlTrans, "ProPayIn", accountPermissions.isProPayIn() ? "Y" : "N");
        if (accountPermissions.isProPayOut())
            xml.subElement(xmlTrans, "ProPayOut", accountPermissions.isProPayOut() ? "Y" : "N");

        xml.subElement(xmlTrans, "CreditCardMonthLimit", accountPermissions.getCreditCardMonthLimit());
        xml.subElement(xmlTrans, "CreditCardTransactionLimit", accountPermissions.getCreditCardTransactionLimit());
        xml.subElement(xmlTrans, "MerchantOverallStatus", accountPermissions.getMerchantOverallStatus().getValue());

        if (accountPermissions.isSoftLimitEnabled())
            xml.subElement(xmlTrans, "SoftLimitEnabled", accountPermissions.isSoftLimitEnabled() ? "Y" : "N");
        if (accountPermissions.isACHPaymentSoftLimitEnabled())
            xml.subElement(xmlTrans, "AchPaymentSoftLimitEnabled", accountPermissions.isACHPaymentSoftLimitEnabled() ? "Y" : "N");

        xml.subElement(xmlTrans, "SoftLimitAchOffPercent", accountPermissions.getSoftLimitACHOffPercent());
        xml.subElement(xmlTrans, "AchPaymentAchOffPercent", accountPermissions.getAchPaymentACHOffPercent());
    }

    private void hydrateBankAccountOwnershipData(ElementTree xml, Element xmlTrans, ProPayBuilder builder) {
        if (builder.getPrimaryBankAccountOwner() != null || builder.getSecondaryBankAccountOwner() != null) {
            Element ownersDataTag = xml.subElement(xmlTrans, "BankAccountOwnerData");

            if (builder.getPrimaryBankAccountOwner() != null) {
                Element primaryOwnerTag = xml.subElement(ownersDataTag, "PrimaryBankAccountOwner");
                xml.subElement(primaryOwnerTag, "FirstName", builder.getPrimaryBankAccountOwner().getFirstName());
                xml.subElement(primaryOwnerTag, "LastName", builder.getPrimaryBankAccountOwner().getLastName());
                xml.subElement(primaryOwnerTag, "Address1", builder.getPrimaryBankAccountOwner().getOwnerAddress().getStreetAddress1());
                xml.subElement(primaryOwnerTag, "Address2", builder.getPrimaryBankAccountOwner().getOwnerAddress().getStreetAddress2());
                xml.subElement(primaryOwnerTag, "Address3", builder.getPrimaryBankAccountOwner().getOwnerAddress().getStreetAddress3());
                xml.subElement(primaryOwnerTag, "City", builder.getPrimaryBankAccountOwner().getOwnerAddress().getCity());
                xml.subElement(primaryOwnerTag, "StateProvince", builder.getPrimaryBankAccountOwner().getOwnerAddress().getState());
                xml.subElement(primaryOwnerTag, "PostalCode", builder.getPrimaryBankAccountOwner().getOwnerAddress().getPostalCode());
                xml.subElement(primaryOwnerTag, "Country", builder.getPrimaryBankAccountOwner().getOwnerAddress().getCountry());
                xml.subElement(primaryOwnerTag, "Phone", builder.getPrimaryBankAccountOwner().getPhoneNumber());
            }

            if (builder.getSecondaryBankAccountOwner() != null) {
                Element secondaryOwnerTag = xml.subElement(ownersDataTag, "SecondaryBankAccountOwner");
                xml.subElement(secondaryOwnerTag, "FirstName", builder.getSecondaryBankAccountOwner().getFirstName());
                xml.subElement(secondaryOwnerTag, "LastName", builder.getSecondaryBankAccountOwner().getLastName());
                xml.subElement(secondaryOwnerTag, "Address1", builder.getSecondaryBankAccountOwner().getOwnerAddress().getStreetAddress1());
                xml.subElement(secondaryOwnerTag, "Address2", builder.getSecondaryBankAccountOwner().getOwnerAddress().getStreetAddress2());
                xml.subElement(secondaryOwnerTag, "Address3", builder.getSecondaryBankAccountOwner().getOwnerAddress().getStreetAddress3());
                xml.subElement(secondaryOwnerTag, "City", builder.getSecondaryBankAccountOwner().getOwnerAddress().getCity());
                xml.subElement(secondaryOwnerTag, "StateProvince", builder.getSecondaryBankAccountOwner().getOwnerAddress().getState());
                xml.subElement(secondaryOwnerTag, "PostalCode", builder.getSecondaryBankAccountOwner().getOwnerAddress().getPostalCode());
                xml.subElement(secondaryOwnerTag, "Country", builder.getSecondaryBankAccountOwner().getOwnerAddress().getCountry());
                xml.subElement(secondaryOwnerTag, "Phone", builder.getSecondaryBankAccountOwner().getPhoneNumber());
            }
        }
    }

    private void hydrateDocumentUploadData(ElementTree xml, Element xmlTrans,TransactionType transType, DocumentUploadData docUploadData) {
        String docNameTag = transType == TransactionType.UploadDocumentChargeback ? "DocumentName" : "documentName";
        String docTypeTag = transType == TransactionType.UploadDocumentChargeback ? "DocType" : "docType";

        xml.subElement(xmlTrans, docNameTag, docUploadData.getDocumentName());
        xml.subElement(xmlTrans, "TransactionReference", docUploadData.getTransactionReference());
        xml.subElement(xmlTrans, "DocCategory", docUploadData.getDocCategory() == null ? null : docUploadData.getDocCategory().toString().toLowerCase());
        xml.subElement(xmlTrans, docTypeTag, docUploadData.getDocType() == null ? null : docUploadData.getDocType().toString().toLowerCase());
        xml.subElement(xmlTrans, "Document", docUploadData.getDocument());
    }

    private void hydrateSSORequestData(ElementTree xml, Element xmlTrans, SSORequestData ssoRequestData) {
        xml.subElement(xmlTrans, "ReferrerUrl", ssoRequestData.getReferrerURL());
        xml.subElement(xmlTrans, "IpAddress", ssoRequestData.getIpAddress());
        xml.subElement(xmlTrans, "IpSubnetMask", ssoRequestData.getIpSubnetMask());
    }

    private void hydrateAccountRenewDetails(ElementTree xml, Element xmlTrans, RenewAccountData renewalAccountData) {
        xml.subElement(xmlTrans, "tier", renewalAccountData.getTier());
        xml.subElement(xmlTrans, "CVV2", renewalAccountData.getCreditCard().getCvn());
        xml.subElement(xmlTrans, "ccNum", renewalAccountData.getCreditCard().getNumber());
        xml.subElement(xmlTrans, "expDate", renewalAccountData.getCreditCard().getShortExpiry());
        xml.subElement(xmlTrans, "zip", renewalAccountData.getZipCode());
        xml.subElement(xmlTrans, "PaymentBankAccountNumber", renewalAccountData.getPaymentBankAccountNumber());
        xml.subElement(xmlTrans, "PaymentBankRoutingNumber", renewalAccountData.getPaymentBankRoutingNumber());
        xml.subElement(xmlTrans, "PaymentBankAccountType", renewalAccountData.getPaymentBankAccountType());
    }

    private void hydrateFlashFundsPaymentCardData(ElementTree xml, Element xmlTrans, FlashFundsPaymentCardData cardData) {
        xml.subElement(xmlTrans, "ccNum", cardData.getCreditCard().getNumber());
        xml.subElement(xmlTrans, "expDate", cardData.getCreditCard().getShortExpiry());
        xml.subElement(xmlTrans, "CVV2", cardData.getCreditCard().getCvn());
        xml.subElement(xmlTrans, "cardholderName", cardData.getCreditCard().getCardHolderName());
        xml.subElement(xmlTrans, "addr", cardData.getCardholderAddress().getStreetAddress1());
        xml.subElement(xmlTrans, "city", cardData.getCardholderAddress().getCity());
        xml.subElement(xmlTrans, "state", cardData.getCardholderAddress().getState());
        xml.subElement(xmlTrans, "zip", cardData.getCardholderAddress().getPostalCode());
        xml.subElement(xmlTrans, "country", cardData.getCardholderAddress().getCountryCode());
    }

    private void hydrateOrderDeviceData(ElementTree xml, Element xmlTrans, OrderDevice orderDeviceData, ProPayBuilder builder) {
        xml.subElement(xmlTrans, "accntNum", builder.getAccountNumber());
        xml.subElement(xmlTrans, "shipTo", orderDeviceData.getShipTo());
        xml.subElement(xmlTrans, "shipToContact", orderDeviceData.getShipToContact());
        xml.subElement(xmlTrans, "shipToAddress", orderDeviceData.getShipToAddress());
        xml.subElement(xmlTrans, "shipToAddress2", orderDeviceData.getShipToAddress2());
        xml.subElement(xmlTrans, "shipToCity", orderDeviceData.getShipToCity());
        xml.subElement(xmlTrans, "shipToState", orderDeviceData.getShipToState());
        xml.subElement(xmlTrans, "shipToZip", orderDeviceData.getShipToZip());
        xml.subElement(xmlTrans, "shipToPhone", orderDeviceData.getShipToPhone());
        xml.subElement(xmlTrans, "cardholderName", orderDeviceData.getCardholderName());
        xml.subElement(xmlTrans, "ccNum", orderDeviceData.getCcNum());
        xml.subElement(xmlTrans, "expDate", orderDeviceData.getExpDate());
        xml.subElement(xmlTrans, "CVV2", orderDeviceData.getCvv2());
        xml.subElement(xmlTrans, "billingZip", orderDeviceData.getBillingZip());
    }


}
