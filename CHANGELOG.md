# Changelog

## v10.2.3 (03/07/2023)

### Enhancements:

- GP-API: Open Banking
- GP-API: Fix MapTransactionSummaryTest()
- GP-API: Add Account ID
- GP-API: Rename Test Classes
- GP-API: Fix Access Token Set Manual On Header
- GP-ECOM/REALEX: Rename Test Classes
- Update GSON & HttpMime Dependencies

## v10.2.2 (02/28/2023)

### Enhancements:
- NTS Phase1 Issue:
  - Transcation Date and Transaction Time Issues(Issue-10123,10145,10148)
  - Reversal User data change for wex fleet (Issue-10152)
  - Approval code changes for reversal request (Issue- 10153)
  - Formatted tag 22 and 23(Pin Block and KSN) changes (Issue-10141)
  - GiftCard Entry Method changes Issues(Issues-10150,10155,10156,10157)
- GP-API: Misc Tests Updated
- REALEX: OpenBanking_GetBankPaymentById() test updated

## v10.2.1 (02/23/2023)

### Enhancements:
- NTS phase1 Issues: 
    - PAN With Expiry,Pin block and KSN Changes For Pin debit Without Track Format(Issue-10146)
- Portico: 
	- EMVDATA tag bug fixes  
	
## v10.2.0 (02/21/2023)

### Enhancements:
- NTS Phase1 Issue: 
    - PAN With Expiry changes for PIN Debit Request Format without Track Data (Issue-10146)    
- GP-API: add risk assessment feature

## v10.1.3 (02/14/2023)

### Enhancements:
- NTS Phase1 Issues: 
    - Approved Amount changes for Fuelman and Fleet wide(Issue-10140)
    - Fleet Data correction for Fuelman and FleetWide(Issue-10143)
- GP-API : Miscellaneous Tests Update
- GP-API: Click To Pay
- Add XGP Signature validation

## v10.1.2 (02/07/2023)

### Enhancements:
NTS Issue: Mastercard purchasing Conditions is added for MasterCardBanknetRefId & Settlement Date Changes

## v10.1.1 (01/31/2023)

### Enhancements:
NTS Issue: Approval Code Changes for Void Transaction
GP-API: Update Java Server Demo for a Client app to support decoupled authentication on 3DS flow

## v10.1.0 (01/19/2023)

### Enhancements:
- Transaction API Connector Implementation
- NTS: NTS phase 2 Implementation except Heartland gift card code changes
- NTS phase1 Issues: 
    - Date timestamp
    - Visa Fleet and Mastercard Fleet user data Data collect(Tag 09 changes)
    - Zip code changes for tag 07 and user data layout.
    -Timeout and Entry method Contact EMV Unattended AFD
- Add UPA StartCardTransaction command	
- GP-API: Exemption Fields
- GP-API: Misc Tests Updated
- Request Logger Refactor

## v10.0.6 (01/05/2023)

### Enhancements:
- GP-API: Buy Now and Pay Later

## v10.0.5 (12/13/2022)

### Enhancements:
- UPA Devices: Added initial support for SendSAF command
- Portico: SdkNameVersion: Name and Version of the SDK used for integration, where applicable
- Update device configuration to be generic
- NTS : Voyager Fleet odometer and driver Id padding changes
- GP-API: Add mapping for Card issuer result codes from /transaction endpoint
- GP-API: Decoupled Authentication
- GP-API: Onboarding Merchant

#### Bug Fixes:
- GP-API: Brand Reference missed on Recurring Transaction
- Portico : Pinblock tag issue changes for Credit Sale EMV transactions

## v10.0.4 (12/01/2022)

### Enhancements:
- VAPS : VAPS Spec update 19.1, 20.1, 21.1 and 21.2.
- VAPS : Added support for 3DES
- NTS issue : NTS Visa Fleet Odometer Changes
- Macroscope Security Issues

## v10.0.3 (11/17/2022)

### Enhancements:

- Align CountryData & CountryUtils to other SDKs 
- GP-API: Misc Test Fixed
- NTS: Added Changes for Wex Fleet Service prompt list & Voyager Fleet Driver ID Update

## v10.0.2 (11/08/2022)

### Enhancements:

- GP-API: Fraud Management
- GP-API: Misc Tests Updated

## v10.0.1 (11/03/2022)

### Enhancements:

- NTS: Added the Extended user data flag to Data collect.
- NTS: Updated the Header time format to 24 HR.
- NTS: Updated the STAN for Data collect request.
- NTS: Updated the `approvalCode` in response for Data collect.
- NTS: Added `bankcardData` variable to reference object.
- NTS: Log optimization.

## v10.0.0 (10/25/2022)

### Enhancements:

- GP-API, GP-ECOM/REALEX: Sunset 3DS1 
- Java Server Demo for a Client app
- GP-API: Update unit tests with new set of credentials for GP-API

## v9.0.3 (10/11/2022)

### Enhancements:

- NTS : Added Changes for DataCollect & Void transaction
- GP-API: PayLink enhancements

## v9.0.2 (09/29/2022)

### Enhancements:

- Genius/Merchantware: Added initial support

## v9.0.1 (09/27/2022)

### Enhancements:

- VAPS: Added support for FleetCor 
- VAPS: Added Support for DE49 and DE50 Currency Code
- NTS: Added Terminal Types N1 and N2

## v9.0.0 (09/13/2022)

### Enhancements:

- PORTICO: Added PorticoConfig for Portico Configuration instead of GatewayConfig 
- GP-ECOM/REALEX: Added GpEcomConfig for GP-ECOM/Realex Configuration instead of GatewayConfig 

## v8.0.7 (09/08/2022)

### Enhancements:

- VAPS: 20.1 specification update.
- Realex: Set CountryCode for country node
- Realex: Set ChallengeRequestIndicator value for HPP_CHALLENGE_REQUEST_INDICATOR property

## v8.0.6 (08/29/2022)

### Enhancements:

- VAPS: 19.1 specification update.
- PORTICO: 3DS and wallet data tags updated.
- VAPS: Batch close issue fixed.
- isNotNullInSubProperty validation on Builders

## v8.0.5 (08/09/2022)

### Enhancements:

- GP-ECOM/REALEX: Supported Stored Credential when creating a new card
- Security changes: Set TLS 1.2 as default for socket connections
- GP-API: Updated Misc Tests
- 

## v8.0.4 (08/02/2022)

### Enhancements:

- GP-API: Added PayLink API that allows you to generate single or multi-use unique payment links.

## v8.0.3 (07/14/2022)

### Enhancements:

- GP-ECOM/REALEX: Updated unit test for SupplementaryData
- GP-API: Added Incremental Auth

## v8.0.2 (07/05/2022)

### Enhancements:

- GP-ECOM/REALEX: Added Open Banking as new payment method
- GP-API: Added "acsReferenceNumber" mapping for 3DS Initiate Authentication flow
- PORTICO: Added EBT reversal support
- GP-API: Mapped providerServerTransRef field on ThreeDSecure class

## v8.0.1 (06/29/2022)

### Enhancements:

- GP-API Dynamic Descriptor 
- HPP EnableExemptionOptimization field
- Flag to do not insert "x-gp-sdk" header on GpApiConnector requests for Android SDK

## v8.0.0 (06/27/2022)

### Enhancements:

- GNAP Connector Implementation

## v7.0.5 (06/14/2022)

### Enhancements:

- Portico: Add support for AdditionalDuplicateData response element

## v7.0.4 (06/07/2022)

### Enhancements:

- Adjust Sale Transaction
- Adjust POST Search Payment Method
- Adjust GET Dispute Document
- Add New HPP Fields in the SDKs for HPP Capture Billing enhancement
- Update VerifyTokenizedPaymentMethodWithFingerprint() test

## v7.0.3 (05/16/2022)

### Enhancements:

- Update Payment Token
- Portico: added DebitAuth and DebitAddToBatch
- Portico: Updated the existing DebitReversal request with Track 2 Data

## v7.0.2 (04/21/2022)

### Enhancements:

- Added Fingerprint 
- Updated code for Secure3D and WalletData Element
- Added Challenge Indicator Nullity validation
- Fixed comments on verfifySignature methods
- Aligned HostedPaymentData constructor with other SDKs
- Updated Misc Tests

## v7.0.1 (04/05/2022)

### Enhancements:

- Added challenge request indicator on 3DS2 initiate step on Gp3DSProvider
- Aligned processAuthorization() logic of an APM to .NET & PHP SDKs.
- Updated Misc Tests
- Deprecate verifySignature() and VerifyEnrolled() from CreditCardData 
- Updated BaseGpApiTest Credentials
- Changed TimeCreated field on Stored Payments Method from Date to DateTime

#### Bug Fixes:

- Fixed Multicapture information on the response of Authorize transaction (GpApi)
- Fixed for card information not to be sent into request when using tokenized card (GpApi)

---

## v7.0.0 (03/24/2022)

### Enhancements:

- NTS Connector Phase 1

---

## v6.3.7 (03/22/2022)

### Enhancements:

- UPA devices: add support for batch summary report, batch detail report, and open tab details report
- UPA devices: various modifications to account for latest UPA version's changes

### Bug Fixes:

- UPA devices: fix potential error from disconnect attempt

---

## v6.3.6 (03/15/2022)

### Enhancements:

- MOBILE_SDK source in the 3DS flow (initiate step)
- AccessTokenInfo Refactor
- Misc Tests Updated

### Bug Fixes:

- Incorrect entry_mode set when doing a Contactless Swipe transaction
- ReadyLink card type issue
- On Stored payments method TimeCreated field should be DateTime instead of Date
- Card object sent into request when using tokenized card
- Multi capture information are not mapped on the response of Authorize transaction

---

## v6.3.5 (03/01/2022)

### Bug Fixes:

- Fix "AccountType" mapping with HPS/Portico integrations

---

## v6.3.4 (03/01/2022)

### Enhancements:

- Improved support for UPA Terminals/Devices
- Added logging functionality; supports easier output to file

### Bug Fixes:

- Fix reporting functionality bug for Heartland/Portico integrations

---

## v6.3.3 (02/17/2022)

### Enhancements:

- Add IN_APP value for entry_mode on GP-API
- Add LNK_ID in Create Transaction Request
- Update Misc Tests

---

## v6.3.2 (02/10/2022)

### Enhancements:

- Dynamic Currency Conversion on GP-API Connector
- Add Alternative Payment Methods (APMs) to HPP
- Misc tests updated

### Bug Fixes:

- Strip fleet from card type for Realex Connector and GP-API Connector
- Diners card type fix on Realex Connector

---

## v6.3.1 (12/16/2021)

### Enhancements:

- Add support for 'ManageTokens' transaction using Heartland/Portico gateway
- Add support for 'Reversal' transaction using UPA devices
- Add PAYPAL alternative payment method on GP-API Connector
- Update miscProductData structure
- Add configName to Recurring Services
- Update MasterCard regex

---

## v6.3.0 (11/18/2021)

### Enhancements:

- Add initial support for UPA Terminals/Devices

---

## v6.2.8 (11/16/2021)

### Enhancements:

- Add AVS missing mapping to response
- Availability of country lists for Android SDK
- Add tests for query param paymentMethod

### Bug Fixes:

- Transaction reporting not working due to new mandatory query parameter added

---

## v6.2.7 (11/11/2021)

### Enhancements:

- Add Mobile phone country code and subscriber number validation for 3DS2
- Add recurring payment with stored credentials functionality to GpApi
- Thread Safe Changes
- Tests Updates
- Vaps Connector refactor

---

## v6.2.6 (02/11/2021)

### Enhancements:

- Add ACH to GpApi

---

## v6.2.4 (28/10/2021)

### Enhancements:

- Add Digital Wallet to GpApi
- Add AvsResponse mappings to GpApi

---

## v6.2.3 (14/10/2021)

### Enhancements:

- Add Multiple merchants to GpApi

---

## v6.2.2 (08/09/2021)

### Enhancements:

- Add Multiple merchants to GpApi

---

## v6.2.2 (08/09/2021)

### Enhancements:

- Add Alternative payment method response mapping to GpEcom/Realex
- Add miscellaneous missing functionality to GpEcom/Realex Connector
- Many GpEcom/Realex tests fixed
- Send "x-gp-sdk" in the header with the SDK programming language and release version used
- Send headers to GP-API that are dynamically set through configuration, like:
  - x-gp-platform: "prestashop;version=1.7.2"
  - x-gp-extension: "coccinet;version=2.4.1"
- Add Fraud Dynamic Rules to GpEcom

---

## v6.1.94 (12/08/2021)

### Enhancements:

- Tests clean up

---

## v6.1.94 (12/08/2021)

### Enhancements:

- Tests clean up

---

## v6.1.93 (05/08/2021)

### Enhancements:

- Upgrade GP-API to 2021-03-22 version and fix tests to support it
- Add GP-API 3DS enhanced

### Bug Fixes:

- Set the GpApiConfig's proxy setting to the parent's Gateway class

---

## v6.1.90 (13/07/2021)

### Enhancements:

- Support findSettlementDisputes by Deposit ID
- Fix Failing Disputes Tests

### Bug Fixes:

- Exceptions unhidden on Credit.updateTokenExpiry() & Credit.deleteToken()

---

## v6.1.89 (01/07/2021)

### Enhancements:

- Support Netherlands Antilles Country

---

## v6.1.84 (17/06/2021)

### Enhancements:

- Message Extension List added to ThreeDSecure class

---

## v6.1.83 (08/06/2021)

### Enhancements:

- Update GP API access token not authenticated scenarios
- Add GP API 3DS builder stored credentials and properly map the data on each request
- Set global merchant country configuration where required
- Add enable exemption optimization on GP ECOM 3DS2 initiate authentication
- Add GP API close batch functionality
- Add GP API actions report
- Implement GP API transaction reauthorization
- Add Access Token permissions

### Bug Fixes:

- Move GP API 3DS tests service container to class initialize to make sure we reuse the same access token
- Fix GP API get settlement dispute detail with wrong id unit test
- Fix TokenManagement and Misc Tests
---

## v6.1.80 (04/13/2021)

### Enhancements:

- Update GP API tokenize payment method and verify flows
- Enable limit the specific permissions the GP API access token will have
- Update GP API 3DS authentication flows
- Add GP API 3DS check availability request body fields
- Add GP API 3DS initiate authentication request body fields
- Add additional GP API transaction summary mappings
- Remove GP API disputes and settled disputes filter by adjustment funding
- Remove GP API disputes and settled disputes filter by from adjustment time created and to adjustment time created
- Enhance GP API transactions reports and settled transactions reports
- Remove Detokenization endpoint
- Add overloaded tokenize method to support setting of TokenUsageMode(SINGLE or MULTIPLE)

### Bug Fixes:

- Check if GP API token is not set to create a tokenized payment method
- Clear GP API card token on detokenize to prevent error creating transactions from that card object
- Check GP API 3DS not enrolled response code and let the flow throw the exception in other case

---
