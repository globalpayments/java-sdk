package com.global.api.serviceConfigs;

import com.global.api.entities.Address;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.enums.*;
import com.global.api.network.enums.gnap.PINCapability;
import com.global.api.network.enums.nts.AvailableProductsCapability;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class AcceptorConfig {
    // DE22 - POS DATA CODE
    private CardDataInputCapability cardDataInputCapability = CardDataInputCapability.MagStripe_KeyEntry;
    private CardHolderAuthenticationCapability cardHolderAuthenticationCapability = CardHolderAuthenticationCapability.None;
    private boolean cardCaptureCapability = false;
    private OperatingEnvironment operatingEnvironment = OperatingEnvironment.OnPremises_CardAcceptor_Attended;
    private CardHolderAuthenticationEntity cardHolderAuthenticationEntity = CardHolderAuthenticationEntity.NotAuthenticated;
    private CardDataOutputCapability cardDataOutputCapability = CardDataOutputCapability.None;
    private TerminalOutputCapability terminalOutputCapability = TerminalOutputCapability.None;
    private PinCaptureCapability pinCaptureCapability = PinCaptureCapability.TwelveCharacters;
    @Setter
    @Getter
    private AvailableProductsCapability availableProductCapability=AvailableProductsCapability.DeviceIsAvailableProductsCapable;
    // DE32 - Acquiring Institution Identification Code
    private String acquiringInstitutionIdentificationCode;

    // DE48_2 - HARDWARE SOFTWARE CONFIG
    private String hardwareLevel;
    private String softwareLevel;
    private String operatingSystemLevel;

    // DE48_33 | DE62.NPC - POS CONFIGURATION
    private String timezone;
    private Boolean supportsPartialApproval;
    private Boolean supportsReturnBalance;
    private Boolean supportsCashOver;
    private Boolean mobileDevice;
    private Boolean supportsShutOffAmount;
    private Boolean supportsDiscoverNetworkReferenceId;
    private Boolean supportsAvsCnvVoidReferrals;
    private Boolean supportsEmvPin;
    private Boolean capableAmexRemainingBalance;
    private Boolean capableVoid;
    @Getter @Setter
    private Boolean supportWexAdditionalProducts;
    @Getter @Setter
    private Boolean capableVisaFleetTwoPointO;
    @Getter @Setter
    private Boolean accountFundingSourceOrTransactionLinkId;
//    @Getter @Setter
//    private Boolean supportBankcard;
    @Getter @Setter
    private PurchaseType supportVisaFleet2dot0;
    @Getter @Setter
    private PurchaseRestrictionCapability supportTerminalPurchaseRestriction;
    @Getter @Setter
    private Boolean visaFleet2;

    //NTS Message Header
    @Getter @Setter
    private Boolean posActionCode;
    private Boolean pinlessDebit;
    @Getter @Setter
    private PINCapability pinCapability;
    @Getter @Setter
    private boolean emvCapable;
    @Getter @Setter
    private String pinPadSerialNumber;
    @Getter @Setter
    private boolean supportsE2EEEncryption;
    @Getter @Setter
    private String deviceType="9.";

    //DE48_34
    private Boolean echoSettlementData;
    private Boolean includeLoyaltyData;
    private Boolean performDateCheck;
    @Getter @Setter
    private String transactionGroupId;
    @Getter @Setter
    private Boolean incrementalSupportIndicator;
    @Getter @Setter
    private String merchantId;
    @Getter @Setter
    private DE22_CardDataInputMode cardDataInputMode;
    // DE48_40 - DE48_49
    private Address address;

    // DE127 - FORWARDING DATA
    private EncryptionType supportedEncryptionType = EncryptionType.TEP2;
    @Getter @Setter
    private ServiceType serviceType;
    @Getter @Setter
    private OperationType operationType;
    @Getter@Setter
    private TokenizationOperationType tokenizationOperationType;
    @Getter @Setter
    private TokenizationType tokenizationType;

    public Boolean getEchoSettlementData() {
        return echoSettlementData;
    }
    public void setEchoSettlementData(Boolean echoSettlementData) {
        this.echoSettlementData = echoSettlementData;
    }
    public Boolean getIncludeLoyaltyData() {
        return includeLoyaltyData;
    }
    public void setIncludeLoyaltyData(Boolean includeLoyaltyData) {
        this.includeLoyaltyData = includeLoyaltyData;
    }
    public Boolean getPerformDateCheck() {
        return performDateCheck;
    }
    public void setPerformDateCheck(Boolean performDateCheck) {
        this.performDateCheck = performDateCheck;
    }

    // DE32 - Acquiring Institution Identification Code
    public String getAcquiringInstitutionIdentificationCode() {
        return acquiringInstitutionIdentificationCode;
    }
    public void setAcquiringInstitutionIdentificationCode(String acquiringInstitutionIdentificationCode) {
        this.acquiringInstitutionIdentificationCode = acquiringInstitutionIdentificationCode;
    }

    // DE48-40
    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }

    // DE22 - POS DATA CODE
    public CardDataInputCapability getCardDataInputCapability() {
        return cardDataInputCapability;
    }
    public void setCardDataInputCapability(CardDataInputCapability cardDataInputCapability) {
        this.cardDataInputCapability = cardDataInputCapability;
    }
    public CardHolderAuthenticationCapability getCardHolderAuthenticationCapability() {
        return cardHolderAuthenticationCapability;
    }
    public void setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability cardHolderAuthenticationCapability) {
        this.cardHolderAuthenticationCapability = cardHolderAuthenticationCapability;
    }
    public boolean isCardCaptureCapability() {
        return cardCaptureCapability;
    }
    public void setCardCaptureCapability(boolean cardCaptureCapability) {
        this.cardCaptureCapability = cardCaptureCapability;
    }
    public OperatingEnvironment getOperatingEnvironment() {
        return operatingEnvironment;
    }
    public void setOperatingEnvironment(OperatingEnvironment operatingEnvironment) {
        this.operatingEnvironment = operatingEnvironment;
    }
    public CardHolderAuthenticationEntity getCardHolderAuthenticationEntity() {
        return cardHolderAuthenticationEntity;
    }
    public void setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity cardHolderAuthenticationEntity) {
        this.cardHolderAuthenticationEntity = cardHolderAuthenticationEntity;
    }
    public CardDataOutputCapability getCardDataOutputCapability() {
        return cardDataOutputCapability;
    }
    public void setCardDataOutputCapability(CardDataOutputCapability cardDataOutputCapability) {
        this.cardDataOutputCapability = cardDataOutputCapability;
    }
    public TerminalOutputCapability getTerminalOutputCapability() {
        return terminalOutputCapability;
    }
    public void setTerminalOutputCapability(TerminalOutputCapability terminalOutputCapability) {
        this.terminalOutputCapability = terminalOutputCapability;
    }
    public PinCaptureCapability getPinCaptureCapability() {
        return pinCaptureCapability;
    }
    public void setPinCaptureCapability(PinCaptureCapability pinCaptureCapability) {
        this.pinCaptureCapability = pinCaptureCapability;
    }

    // DE48_2 - HARDWARE SOFTWARE CONFIG
    public String getHardwareLevel() {
        return hardwareLevel;
    }
    public void setHardwareLevel(String hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }
    public String getSoftwareLevel() {
        return softwareLevel;
    }
    public void setSoftwareLevel(String softwareLevel) {
        this.softwareLevel = softwareLevel;
    }
    public String getOperatingSystemLevel() {
        return operatingSystemLevel;
    }
    public void setOperatingSystemLevel(String operatingSystemLevel) {
        this.operatingSystemLevel = operatingSystemLevel;
    }

    // DE48_33 POS CONFIGURATION
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    public Boolean getSupportsPartialApproval() {
        return supportsPartialApproval;
    }
    public void setSupportsPartialApproval(Boolean supportsPartialApproval) {
        this.supportsPartialApproval = supportsPartialApproval;
    }
    public Boolean getSupportsShutOffAmount() {
        return supportsShutOffAmount;
    }
    public void setSupportsShutOffAmount(Boolean supportsShutOffAmount) {
        this.supportsShutOffAmount = supportsShutOffAmount;
    }
    public Boolean getSupportsReturnBalance() {
        return supportsReturnBalance;
    }
    public void setSupportsReturnBalance(Boolean supportsReturnBalance) {
        this.supportsReturnBalance = supportsReturnBalance;
    }
    public Boolean getSupportsCashOver() {
        return supportsCashOver;
    }
    public void setSupportsCashOver(Boolean supportsCashOver) {
        this.supportsCashOver = supportsCashOver;
    }
    public Boolean getSupportsDiscoverNetworkReferenceId() {
        return supportsDiscoverNetworkReferenceId;
    }
    public void setSupportsDiscoverNetworkReferenceId(Boolean supportsDiscoverNetworkReferenceId) {
        this.supportsDiscoverNetworkReferenceId = supportsDiscoverNetworkReferenceId;
    }
    public Boolean getSupportsAvsCnvVoidReferrals() {
        return supportsAvsCnvVoidReferrals;
    }
    public void setSupportsAvsCnvVoidReferrals(Boolean supportsAvsCnvVoidReferrals) {
        this.supportsAvsCnvVoidReferrals = supportsAvsCnvVoidReferrals;
    }
    public Boolean getSupportsEmvPin() {
        return supportsEmvPin;
    }
    public void setSupportsEmvPin(Boolean supportsEmvPin) {
        this.supportsEmvPin = supportsEmvPin;
    }
    public Boolean getMobileDevice() {
        return mobileDevice;
    }
    public void setMobileDevice(Boolean mobileDevice) {
        this.mobileDevice = mobileDevice;
    }
    public Boolean getPinlessDebit() {
        return pinlessDebit;
    }
    public void setPinlessDebit(Boolean pinlessDebit) {
        this.pinlessDebit = pinlessDebit;
    }

    public Boolean getCapableAmexRemainingBalance() {
        return capableAmexRemainingBalance;
    }

    public void setCapableAmexRemainingBalance(Boolean capableAmexRemainingBalance) {
        this.capableAmexRemainingBalance = capableAmexRemainingBalance;
    }

    public Boolean getCapableVoid() {
        return capableVoid;
    }

    public void setCapableVoid(Boolean capableVoid) {
        this.capableVoid = capableVoid;
    }

    public boolean hasPosConfiguration_MessageControl() {
        return (!StringUtils.isNullOrEmpty(timezone)
                || supportsPartialApproval != null
                || supportsReturnBalance != null
                || supportsCashOver != null
                || mobileDevice != null);
    }
    public boolean hasPosConfiguration_IssuerData() {
        return (supportsPartialApproval != null
                || supportsShutOffAmount != null
                || supportsReturnBalance != null
                || supportsDiscoverNetworkReferenceId != null
                || supportsAvsCnvVoidReferrals != null
                || supportsEmvPin != null
                || mobileDevice != null
                || pinlessDebit != null
                || supportWexAdditionalProducts != null);
    }
    public boolean hasPosConfiguration_MessageData(){
        return performDateCheck != null
                || echoSettlementData != null
                || includeLoyaltyData != null
                || transactionGroupId !=null
                || incrementalSupportIndicator !=null;
    }

    public boolean hasPosConfiguration_BankcardData() {
        return (supportsPartialApproval != null
                || supportsShutOffAmount != null
                || capableAmexRemainingBalance != null
                || supportsDiscoverNetworkReferenceId != null
                || capableVoid != null
                || supportsEmvPin != null
                || mobileDevice != null);
    }
    public String getPosConfigForIssuerData() {
        String rvalue = supportsPartialApproval != null ? supportsPartialApproval ? "Y" : "N" : "N";
        rvalue = rvalue.concat(supportsShutOffAmount != null ? supportsShutOffAmount ? "Y" : "N" : "N")
                .concat("N")
                .concat(supportsReturnBalance != null ? supportsReturnBalance ? "Y" : "N" : "N")
                .concat(supportsDiscoverNetworkReferenceId != null ? supportsDiscoverNetworkReferenceId ? "Y" : "N" : "N")
                .concat(supportsAvsCnvVoidReferrals != null ? supportsAvsCnvVoidReferrals ? "Y" : "N" : "N")
                .concat(supportsEmvPin != null ? supportsEmvPin ? "Y" : "N" : "N")
                .concat(mobileDevice != null ? mobileDevice ? "Y" : "N" : "N")
                .concat(pinlessDebit != null ? pinlessDebit ? "Y" : "N" : "N")
                .concat(supportWexAdditionalProducts != null ? supportWexAdditionalProducts ? "Y" : "N" : "N")
                .concat(supportTerminalPurchaseRestriction != null ? "Y" : "N");
        return rvalue;
    }

    public String getTerminalCapabilityForBankcard() {
        String rvalue = supportsPartialApproval != null ? supportsPartialApproval ? "Y" : "N" : "N";
        rvalue = rvalue.concat(supportsShutOffAmount != null ? supportsShutOffAmount ? "Y" : "N" : "N")
                .concat("N")
                .concat(capableAmexRemainingBalance != null ? capableAmexRemainingBalance ? "Y" : "N" : "N")
                .concat(supportsDiscoverNetworkReferenceId != null ? supportsDiscoverNetworkReferenceId ? "Y" : "N" : "N")
                .concat(capableVoid != null ? capableVoid ? "Y" : "N" : "N")
                .concat(supportsEmvPin != null ? supportsEmvPin ? "Y" : "N" : "N")
                .concat(mobileDevice != null ? mobileDevice ? "Y" : "N" : "N")
                .concat("N")
                .concat(posActionCode != null? posActionCode? "Y": "N": "N")
                .concat(capableVisaFleetTwoPointO != null ? capableVisaFleetTwoPointO ? "Y" : "N" : "N")
                .concat(accountFundingSourceOrTransactionLinkId != null ? accountFundingSourceOrTransactionLinkId ? "Y" : "N" : "N");
        return rvalue;
    }

    // DE127 FORWARDING DATA
    public EncryptionType getSupportedEncryptionType() {
        return supportedEncryptionType;
    }
    public void setSupportedEncryptionType(EncryptionType supportedEncryptionType) {
        this.supportedEncryptionType = supportedEncryptionType;
    }

    public boolean isAttended() {
        return operatingEnvironment.equals(OperatingEnvironment.OnPremises_CardAcceptor_Attended);
    }

    public void validate() throws ConfigurationException {
        String hardwareLevel = StringUtils.isNullOrEmpty(getHardwareLevel()) ? "    " : StringUtils.padRight(getHardwareLevel(), 4, ' ');
        String softwareLevel = StringUtils.isNullOrEmpty(getSoftwareLevel()) ? "        " : StringUtils.padRight(getSoftwareLevel(), 8, ' ');
        String operatingSystemLevel = StringUtils.isNullOrEmpty(getOperatingSystemLevel()) ? "        " : StringUtils.padRight(getOperatingSystemLevel(), 8, ' ');
        if(hardwareLevel.concat(softwareLevel).concat(operatingSystemLevel).length() > 20) {
            throw new ConfigurationException("The values for Hardware, Software and Operating System Level cannot exceed a combined length of 20 characters.");
        }

        if(address != null) {
            if(address.getName() == null || address.getStreetAddress1() == null || address.getCity() == null) {
                throw new ConfigurationException("Missing Acceptor Address Field: Name, Street1 or City.");
            }

            if(address.getPostalCode() == null || address.getState() == null || address.getCountry() == null) {
                throw new ConfigurationException("Missing Acceptor Address Field: PostalCode, State/Region or Country.");
            }
        }
    }
}
