# Changelog

## Latest Version - V14.2.13 (03/27/2025)
### Bug Fixes
- [Vaps] - Added a fix to set the original batchNumber into a datacollect transaction through TransactionReference object.
- [Vaps] - Updated DE 12 dateTime function for non-original transaction.
- [Vaps] - Updated the datetime retrieval method from datetime.now() to localdatetime.now() for internal data collect DE 12 tag (10349).

## V14.2.12 (02/27/2025)
- [NTS] -Fix issue 10348 : Updated the default value space to 0 for Voyager EMV  non-fuel product types.

## V14.2.11 (02/18/2025)
### Enhancements
- [Terminals] - Fix Large Amount Format.
- [GPApi] - Converted GPEcomm folder to JUnit 5 testing framework.

## V14.2.10 (02/11/2025)
### Enhancements
- [UPA] - Added withRequireSecurityCode to TerminalAuthBuilder to allow bypass of security code for manual entry gift card.
- [GPApi] - Converted GPAPI folder to JUnit 5 testing framework.

## V14.2.9 (02/03/2025)
### Bug Fixes
- [NTS] - Fix Issue 10347 : Updated p66 voyager auth emv request to send vehicle & id number instead of empty spaces.

##  V14.2.8 (01/28/2025)
### Bug Fixes
- [UPA] - Background SAFReport for android.
- [Portico] - Added pan length check to isFleet() to prevent a StringIndexOutOfBoundsException from occurring.

##  V14.2.7 (01/16/2025)
### Enhancements
- [Testing Framework] - Converted BillPay and ProPay test folders to JUnit-5.
- [NTS] - New test cases added for Code Coverage.

### Bug Fixes
- [Portico] - Fixed managed token XML request to update expiration date.
- [Vaps] - Debit & EBT DataCollect Generation by SDK based on STAN(10336).

##  V14.2.6 (01/06/2025)
### Enhancements
- [PAX] - Added logger to capture message received from device
- [GP-API] - Add mobile_phone and billing_address to 3DS initiate step request

### Bug Fixes
- [UPA] Put back removed fields on UpaTransactionResponse: IssuerResp, IsoRespCode, BankRespCode, PinVerified, AccountType,
  transactionType, PosSequenceNbr, appName
- [NTS] Fixed Voyager Fleet EMV DriverId formatting with left justified & space filled (10341)

## V14.2.5 (12/10/2024)
### Enhancements
- [GP-API] - Add cardholder name on 3DS check availability request
- [Portico] - Converted test files to JUnit 5 in Portico folder

## V14.2.4 (12/03/2024)
### Enhancements
- [GP-API] - Add new mappings on GET /transaction response: funding, tid, masked_number_first6last4, issuer, gratuity_amount, cashback_amount, authentication
- [GP-API] - Add new enum value for payByLinkType: HOSTED_PAYMENT_PAGE, THIRD_PARTY_PAGE

### Bug Fixes
- [NTS] - Updated DirverID format for Voyager Fleet EMV (10341)
- [NTS] - Fixed Null pointer exception for Batch close Transaction (10342)

## V14.2.3 (11/14/2024)
### Enhancements
- [GP-API] - Add new mapping fields on digital wallet transaction response: masked_number_last4, brand, brand_reference
- Added Support for JUnit 5, Mockito and Jacoco Reporting

### Bug Fixes
- [GP-ECOM] Fix request logger for recurring payments

## V14.2.2 (10/31/2024)
### Enhancements
- [Terminals] Add abstract class DeviceInterface
- [Terminals] Clean-up HPAInterface, UPAInterface, PaxInterface, GeniusInterface, DiamondInterface - remove all the 
              unused methods
- [DeviceInterface] Add new method: balance, authorize, sale, refund, Void, capture, startTransaction, getBatchReport
- [MITC UPA] Automatic access to UPA commands when new commands are added

### Deprecate
- [IDeviceInterface] Deprecate methods: - addLineItem, creditAuth, creditCapture, creditRefund, creditSale, creditVerify, 
                     creditVoid, debitSale, debitRefund, debitVoid, giftSale, giftSale, giftVoid, giftBalance,
                     ebtBalance, ebtRefund, ebtRefund, getBatchSummary

### Bug Fixes
- [UPA] Fixed issue for amount with values lower than 1
- [UPA] Fixed issue for invoiceNbr - change from Integer to String

## V14.2.1 (10/15/2024)
### Bug Fixes
- [NTS] - Setting PDL timeout values in Reversal & Completion requests. (rebasing the code with the hotfix branch- 10335) 
- [NTS] - Added Transaction Reference check for Visa ,Discover & Mastercard in the NTS User data.

## V14.2.0 (10/10/2024)
### Enhacements
- [NTS] - NTS 23.1 Spec Update includes General Information, EMV PDL, Bankcard Tag data, Pin debit Authorization, MasterCard Fleet, Voyager Fleet, Visa Fleet 2.0 & Wex Fleet changes.

## V14.1.0 (10/08/2024)
### Enhacements
- [NTS] - NTS Spec Update 22.1 includes General Information, EMV PDL, Bankcard Tag data, Pin debit Authorization, MasterCard Fleet, Voyager Fleet, Wex Fleet user data format, 3DES & Tokenization.
- [Portico] - Added ClientTxnId to ReportBatchDetail and ReportOpenAuths reports
- [Portico] - Reporting refactoring
- [Portico] - BatchId, StartDateUTC and EndDateUTC added to ReportBuilder
- 
### Bug Fixes
- [Portico] - Portico Activity Report mapping, plus correction of some failing tests

## V14.0.2 (10/01/2024)
### Bug Fixes
- [VAPS/NTS] - Fixed issue 10332 - Debit transactions were failing due to Communication exceptions.
- [GP-API] - Update 3DS Object fields in transaction endpoint ("server_trans_ref" and "ds_trans_ref").

## V14.0.1 (09/20/2024)
### Bug Fixes
- [VAPS/NTS] - Fixed Java Memory leak issue by implementing cache thread pool to optimize memory utilization(10331).
- [Portico] - Added mapping for 3DSecure (Version 2) to Transaction Summary Report.

## V14.0.0 (09/10/2024)
### Enhacements
- [UPA] Add new UPA commands
### Bug Fixes
- [VAPS] - Resolved 3DES storedValue RC-125 with the updated field matrix value for NonOriginal transaction (10330)

## V13.0.1 (08/29/2024)
### Enhacements
- [NTS] Implement masking of entire response after header for echoed request in response.(10317)

## Latest Version - V13.0.0 (08/27/2024)
### Enhacements
- [UPA MiC]: Add MiC connector for UPA via GP-API
- [MEET-IN-THE-CLOUD] [UPA] -  Add new mapping response fields for "/devices" endpoint
- [NTS] Created new method "withPDLTimeout" in builder to add PDL timeout value (10329).
- [GPAPI] Add MITC mapping enchacement
- [UPA]: Fix JSON reader in UPA
- [UPA]: Fix Auth Amount trimming leading zero.
- [UPA]: Implement GetParams
### Bug Fixes:
- [PAX] Correction to tip/gratuity handling in the request to device



## V12.0.2 (08/08/2024)
### Enhancements
- [PAX Devices] Added 'Sequence Number' to transaction response
- [DiamondCloud] Add logger for Diamond Cloud provider payment terminal.
- 10312 - Default timeout has been set to be 30 seconds in sdk and also provided way to override default value for NTS, Vaps 8583, NWS & GNAP.

### Fixes
- [VAPS] Fixed 10327 - prompts are getting appended with 00 hps wex fleet emv are removed.

## V12.0.1 (07/30/2024)
### Bug Fixes
- [VAPS]: VAPS 8583 Currency Code - Issue 10325
- [Portico] : Added capture condition for Debit & Credit EMV transaction - Issue 10326

## V12.0.0 (07/25/2024)
### Enhancements
- [DiamondCloud] Add support for Diamond Cloud provider payment terminals.
- [GP-API] Add "/payers" endpoint

### Bug Fixes
- [UPA]: Correction to 'Void' command SDK-level validations
- [UPA]: Correction to 'AuthCompletion' SDK-level validations
- [VAPS]: updated condition for Fleet Data prompt for issue 10321 & 10324. If there is no prompt data, then DE 48-8 should not be sent.
- [Portico]: Fixed the syntax for handling GatewayTimeOut Exception

## V11.2.25 (07/23/2024)
### Enhancements
- [NTS]: Added testcases for SDK capability matrix
- [NWS]: Code Coverage Improvement
- [Portico]: Added Test cases for code coverage
- [Bill Pay]: Added test cases for code coverage
- [GP-API] Code clean up and improvements
- [GP-ECOM] HPP Addres Capture

### Bug Fixes
- [GP-API]: Fixed "destination" field as optional for Open banking
- [UPA]: Fixed improper SAF transaction record response mapping
- [Portico]:Fixed conditional check for UniqueDeviceId and updated a test
- [VAPS]: Fixed issue 10321 Voyager EMV Prompts issue, updated Vehicle number to 3
- [VAPS]: Fixed issue 10324 Added field for IDNumber in FleetData.

## V11.2.24 (07/18/2024)
### Enhancements
UPA:
- Improved sendSAF command response handling

### Fixes
Portico:
- Fixed bug for report request formation

## V11.2.23 (07/16/2024)
### Enhancements
Portico:
- Add support for sending value for 'UniqueDeviceId' header element
- Fixed issue 10322 - All the CGP transactions that use Management Builder are failing with "length should not be more than 50 digits"

GP-API:
- Added suggested improvements into some requests and mappings

GP-ECOM:
- Added multi capture

### Fixes
NTS:
- Fixed issue 10317 - Added masking for EMV tag data logs for tags 57,5A & 5F24.

UPA:
- Fixed 'GetSAFReport' response handling

## V11.2.22 (07/02/2024)
  ### Fixes
  VAPS:
 - Added Tag DE_28 Date, Reconciliation to the Batch Close transaction Request.
 - Updated DE_62 tag IMC - Mastercard cit/mit Indicator value from hardCode Value to enums
 - Updated DE 62 tag NPC.11 value mapped from visaFleetCapable to TerminalPurchaseRestriction
		
  ### Enhancement
  Portico:
 - Updated Token Parameters element

### V11.2.21 (06/25/2024)
### Enhancement
NWS:
- NWS Spec Update 23.1 with Combined Tokenization and VisaFleet2.0 field update.

Portico:
- Category Indicator parameter addition to the Portico transaction.

PAX:
- Credential on File Datablock in Request/Response.

UPA:
- Added exception test cases for code coverage.

### Bug Fixes
NTS :
- Issue 10317 fixed - Track data is not being masked and it is in clear text.

## Version - V11.2.20 (06/20/2024)
### Enhancement
- UPA:
 Added support for message received logging.

## Version - V11.2.19 (06/18/2024)
### Enhancement
- UPA:
  Mapped service code,fallback & expiry date parameter to the startCard transaction response.
  Added support for Delete Preauth transaction.
  Added Cryptogram type to the transaction receipt.

- Portico:
  ClerkID parameter addition to the Portico transaction.

### Bug Fixes
- NTS:
  Java Nts 10318 updated response message "Denial Mastercard" to "Denial" for RC 10.

## V11.2.18 (06/06/2024)
### Enhancement -
- Bill Pay :
  Added Create Recurring Payment Request & Get Transaction By Order ID transaction.

### Bug Fixes
- NTS:
  Fixed issue 10316 - Removed Exception condition for Product Data Required.

- VAPS :
  Fixed field matrix value from 2 to 3 for debit force refund transaction for Tag DE 127.

## V11.2.17 (06/03/2024)
### Enhancement
- [Portico] Added GatewayTxn Id to GatewayException Object when Response Code is 30

##  V11.2.16 (05/30/2024)
### Enhancement
- [GP-ECOM] Added additional fee to a card transaction (surchargeamount).
- [GP-API] Add "payer->email" property on 3DS "/initiate" request
- [GPAPI] Code clean up
### Bug Fixes
- [Vaps] Retransmit Batch Close - 10315

##  V11.2.15 (05/23/2024)
### Bug Fixes
VAPS
- Fixed expiry date for force refund transactions(10309)

### V11.2.14 (05/21/2024)
### Bug Fixes
- Android Compatibility bug fix
- Code clean up

## v11.2.13 (5/16/2024)
### Bug Fixes
NTS
- Updated MessageException to ApiException (10301)


## v11.2.12 (5/14/2024)
### Enhancements
Portico
- Wrap the 'CheckQuery' request and response

[GP-API]
- Access token update
  -Fix mapping for "authCode"

### Bug Fixes
Vaps
- Updated debit refund DE 127 tag field matric for 3DES - 10308


## v11.2.11 (05/06/2024)
### Enhancements
NTS :
- Fixed userdata length of WEX EMV FALLBACK (10301)
- User Data Length Fixed for WEX NonEmv Datacollect

Portico Enhancement
- Added DebitReversal support using fromId.

## v11.2.10 (04/30/2024)
### Enhancements
Portico
- Expand the CountryUtils classes to include ISO 3166 values.

## v11.2.9 (04/23/2024)
### Enhancements
PAX
- Add Receipt & Surcharge.

NTS
- Fixed User Data Length for WEX EMV issue 10304.

### Bug Fixes
- Fixed partial auth response returning null properties (esp. authorizedAmount).

## v11.2.8 (04/18/2024)
### Enhancements
Bill Pay :
- Added support for MakeQuickPayBlindPayment & MakeQuickPayBlindPaymentReturnToken transaction.

## v11.2.7 (04/11/2024)
### Bug Fixes
NTS :
 - Resolve incorrect user data length for fuelman fleet card(Issue 10296).

VAPS :
 - Fixed the missing expiry date and updated the field matrix for force data collect of refund(Issue 10297).
 - Fixed the missing "NSI value" tag for retry data collect(Issue 10300).

## v11.2.6 (04/03/24)
### Bug Fixes
VAPS -

Issue 10295 - Fix RC-904 for 3DES encryption Debit and EBT transactions
Issue 10928 - Added support for EBTCardData 3DES Encryption

## v11.2.5 (03/28/24)
### Enhancements
  UPA Devices
- Added support for latest transaction response properties 

PAX 
- TipAdjust and TipRequest Flag Implementation

### Bug Fixes
VAPS
- Issue 10290 : Fixed Null Pointer Exception for Vaps 3DES ValueLink

## v11.2.4 (03/21/24)
### Enhancements
 VAPS
 - Issue 10294 - VisaFleet DE63 Format Update

## v11.2.3 (03/14/24)
### Enhancements
  VAPS
  - Remove enum validation for VAPS Authorizer Code
  - Visa Fleet2.0 Field Update

## v11.2.2 (03/05/2024)
### Enhancements
  - Updated config on FileProcessingTest
  - Code CleanUp


## v11.2.1 (02/27/2024)
### Enhancements
- [VAPS Enhancement]
  - 8583 VAPS 3DES ValueLink implementation.
  - Issue 10292- change MTI for resubmit force data collect.
  - Issue 10293 -Fixed null pointer exception for GatewayTimeoutException.


## v11.2.0 (02/22/2024)
### Enhancements
- [VAPS Enhancement]
 - Vaps Spec Update 23.1
 - Vaps Spec Update 21.2 with Tokenization, VoyagerEmv and VisaFleet2.0 field update.

## v11.1.33 (02/20/2024)
-NTS Phase1 Issue:
 - Fixed API exception issue for 10273 & 10274

## v11.1.32 (02/13/2024)
### Enhancements/
- [PAX Issue]
  - PAX_Add/Delete_image
  - PAX Local Details Report Implementation
- [VAPS Issue]
  - Fixed issue 10289 of partial approved amount for retransmit.

## v11.1.31 (02/01/24)
### Enhancements
- [VAPS Enhancement]
  - Added placeholder to set currency field in resubmit builder.
  
## v11.1.30 (01/30/24)
### Enhancements
- updated appsec.properties file
  
## v11.1.29 (01/25/24)
### Enhancements
- [NTS Phase1 Issue]
  - Fixed 10233 issue "Amount 6 field is getting sent with zero value".

- [VAPS Issue]
  - Fixed 953(Too many queued / no connection)error for Debit datacollect voltage encryption transaction

## v11.1.28 (01/17/24)
- [NTS Phase1 Issue]
- 10279: added fix for returning Non Approved and format error tokens together as part of BatchSummary object

## v11.1.27 (01/16/24)
- [NTS Phase1 Issue]
  - Fixed User Data empty issue for WEX Credit Adjustment Reversal - EMV(Issue 10270)
  - EMVData is sent empty in bankcard data for P66 Sale WEX issue has been fixed (Issue 10278)
- [VAPS Issue]
  - Fixed Null Pointer Exception for Debit completion message (Issue 10277)

##  v11.1.26 (01/11/24)
### Enhancements
- [NTS Phase1 Issue]
  - NTS_Issue_10233_wex_product_roll_up_for_quantity6.
- [VAPS Issue]
  - Issue_10277_8583_DUKPT_TDES_Encryption_error_for_Data_collects.
- Java code coverage enhancement.
- Microscopic Issue fixes

## v11.1.25 (01/04/24)
### Fix
[UPA]
- Fixed issue with null transactionAmount.

## v11.1.24 (12/12/23)
### Enhancements
[NTS Phase1 Issue]
- Fixed issue of getting extra RTB token in the nonApprovedDataCollectToken list(Issue 10275)


## v11.1.23 (12/07/23)
### Enhancements
- [GP-API] File Processing
- [GP-API] Add missing properties to authentication->three_ds (message_version, eci,server_trans_reference, ds_trans_reference,value)

## v11.1.22 (12/05/23)
### Enhancements
 [NTS Phase1 Issue]
- Fixed WEX Card EMV user data length(Issue- 10267).
- Updated condition for User data when emv data is not sent in Wex Pre-Auth reversal request(Issue-10268).
- Added missing required User data for WEX Sale reversal request to fix format exception (Issue-10269).

## v11.1.21 (11/30/23)
### Enhancements
 [NTS Phase1 Issue]

- rearranged product 6 & 7 when quantity length is more than 1 for WEX (Issue -10233)
- added new enum named NoPromptMCFleet for MasterCard Fleet when no prompt (Issue -10263)

[GpEcom]

- Update date format for connector.

[GpApi & GpEcom]

- Enhancement on fraud enum.
- Tests enhancements.


## v11.1.20 (11/16/23)
### Enhancements
[UPA]
- Add Signature Data
- GetSAF Report
- DeleteSAF
- Removed IS HSAFSA parameter
- Added Ref/Saf reference number
- Incremental Auth addition.

[NTS Phase1 Issue]
- rearranged product 6 & 7 when quantity length is more than 1 for WEX (Issue -10233).
-Portico:
-bug fixes for Batch close

[Portico]:
- Allow address info with digital wallet transactions.

## v11.1.19 (11/02/23)
### Enhancements:
[UPA]:
- StartCardTransaction.

[GP-ECOM]:
- Limit what card types to accept for payment or storage (HPP & API)
  * https://developer.globalpay.com/hpp/card-blocking
  * https://developer.globalpay.com/api/card-blocking

[GP-API]:
- Add a new alternative payment method, ALIPAY.
- Tests enhancement.

## v11.1.18 (10/31/23)
### Enhancements:
[GP-API]:
- Upload Merchant Documentst - https://developer.globalpay.com/api/merchants#/Upload%20Merchant%20Documentation/UploadMerchantDocumentation
- Improve unit tests.
- Replace Base64 from apache to the one from jdk.

## v11.1.17 (10/24/23)
### Enhancements:
[GP-API]:
- Map enrolled field on 3ds response.
- Improve unit tests on Merchants onboard.
### Bug Fixes:
[GP-API]:
- Fix mapping of accountNumber for Open Banking.
- Fix NullPointerException on request logger for open banking.

[Portico]:
- Fixed Null Pointer exception with customer data.

## v11.1.16 (10/23/23)

### Enhancements:
-NTS Phase1 Issue :
- Exception thrown for 02 RC for Regular Transactions(Issue -10260).

-VAPS:
- Approval Code changes for Refund Retransmit Transaction(Issue- 10258).

## v11.1.15 (10/19/23)

### Enhancements:
-NTS Phase1 Issue :
- Added BatchSummary Object when reSubmitting batchClose transaction(issue:10259)
- Fixed Incorrect amount and quantity for Gift Card & Fixed unit of measure value for WexFleet(Issue: 10233)

-[GP-API]
- Credit Or Debit a Funds Management Account (FMA) - https://developer.globalpay.com/api/funds
- Enhance logs based on environment (GP-API & GP-ECOM)

## v11.1.14 (10/16/23)

### Enhancements:
-NTS Phase1 Issue :
- Added missing condition of Mastercard Purchasing(Issue-10254)

-Portico:
- Added Cardholder email support property.

## v11.1.13 (10/12/23)

### Enhancements:
-NTS Phase1 Issue:
- reverted code of calculating userdata length excluding tagId and separators for logs(Issue-10250).


## v11.1.12 (10/10/23)

### Enhancements:
-NTS Phase1 Issue:
- Code added for 10213, 10214 & 10243 to handle RTB(16) returned 40/80/90 & DC (12) returned 40/80/90 and RTB(16) returned 01 scenario.
- MasterCard Purchasing UserData Format (10254)
- Fixes for debit  (10255)
- Added Authorizer Codes For ChaseNet & heartland Gift Card (10256)
- Return batch summary object instead of throwing an exception(10257)


## v11.1.11 (10/05/23)

### Enhancements:
-NTS Phase1 Issue:
- Issue 10233- Product roll-up data(combine product with similar product code)
- Issue 10253 - HPS-8583-Incorrect Batch Summary reported after retransmit RTB

## v11.1.10 (09/28/23)

### Enhancements:
-NTS Phase1 Issue :
- User Data length calculated excluding the slashes & tag Number & added into logging(Issue-10250)

-Portico:
- Added builder check condition for SAFData in buildEnvelope method.

## v11.1.9 (09/26/23)

### Enhancements:
-NTS Phase1 Issue :
- Updated code for BatchSummary transactionCount, creditAmount and debitAmount(Issue - 10251).

- GP-API: add mapping for batch_time_created on settlement transactions reporting.

## v11.1.8 (09/25/23)

### Enhancements:

-NTS Phase1 Issue :
- Masked request & response of retransmit & resubmit Transaction(Issue -10240)
- Aligned masked request & response with respective transaction itself(Issue-10249)

-Portico:
- Fixed typo CardHolderFirstName & BankingRoutingNbr

## v11.1.7 (09/20/23)

### Enhancements:

-NTS Phase1 Issue :
- Batch and Sequence Number aligned with Original Sale Transaction for WexFleet Credit Adjustment User Data (Issue 10248)

 -Verifone P400:
 - Added initial Meet-In-The-Cloud connectivity support for this device


## v11.1.6 (09/15/23)

### Enhancements:

-NTS Phase1 Issue :
- Handled exception while masking request and response(Issue 10240)

-Portico:
- Fixed SAFOrigDT hardcoded issue, It is now configurable.
- Fix description response from Portico to be consumed by schedule

## v11.1.5 (09/14/23)

### Enhancements:

-NTS Phase1 Issue:
- Issue 10233- Product roll-up data.
- Issue 10240: masked account Number, expiry Date and track Data in logs individually and also in request or response.
- Issue 10241: Fixed incorrect User Data Length and Error in Positions of several fields for Wex Fleet transactions with multiple products.
- Issue 10244 :Formatted the Price per unit correctly without rounding.

-Updated MC bin to 51-58 to address MC-regex internal query.

-Portico:
- Added SAF to transaction builder.

## v11.1.4 (09/12/23)

### Enhancements:

-NTS Phase1 Issue:
 - Updated the condition of Expanded User Data Indicator(E) for fallback transaction & resolved the Exception of Synchrony Card - Issue 10242


## v11.1.3 (09/07/23)

### Enhancements:

-NTS Phase1 Issue:
 - Updated the values of hostTransactionCount, Host Toital Sales,Host Total Returns, Response Code and Token from Batchsummary object at the time of retransmit(Issue -10239).

-[GP-ECOM]:
 - Support parseResponse for status_url on HostedService (HPP APMs)

## v11.1.2 (08/31/23)

### Enhancements:

-NTS Phase1 Issue: 
  - Updated right padding with left padding for DISPENSER QUANTITY and NUMBER OF SCANNERS / PERIPHERALS fields of POS Site Configuration Message Request Format (Issue-10238)
  - Fuel and non fuel product code will combined when two same product code is there for non fleet bankcard,Pin debit and stored value data collects (Issue-10233) 

## v11.1.1 (08/28/23)

### Enhancements:

-NTS Phase1 Issue:  
  -Added 2 byte decimal to creditAmount and debitAmount in BatchSummary Object(10237)

## v11.1.0 (08/24/23)

### Enhancements:

-NWS:
 - Debit Cards Implementation
 - Bankcards Implementation
 - EBT EWIC Implementation
 - EBT Implementation
 - Visa Ready Link Implementation
 - Fleet Cards Implementation
 - SVS Gift Cards Implementation
 - HMS Gift Cards Implementation
 - Card On File Implementation
 - Tokenization Implementation
 - Request to balance Implementation
 - EMV Bankcards Implementation
 - 3DES Implementation
 
-UPA:
 - Add sendReady message
 - Add registerPOS command

## v11.0.0 (08/22/23)

### Enhancements:
NTS Issue :
  - Added hostTransactionCount in batchSummary and mapped HostTotalSales with debitAmount and HostTotalReturns with creditAmount(10230)
  - Added missed open and closed braces to be in sync with previous 40/80/90 exception(10236).
 
GP-API :
  - Rename PayLink to PayByLink
  
Portico : 
  - Map Portico response tag "ClientTxnId" to transaction response
  - Commercial card values
  - Add Void-by-ClientTxnId for Heartland ACH
  - ADD SAF indicator and SAF data

## v10.3.14 (08/17/23)

### Enhancements:

-NTS Phase1 Issue:
- Added Null check for host response area Issue(10232)
- Resolved error in position of Several Fields in POS Site Configuration Message Request.

## v10.3.13 (08/14/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Transaction date and transaction time changes for credit adjustment scenario  - Issue(10229) 
  - Added the code for retransmit request to balance(16) to forced request to balance changes - Issue(10214)
 
## v10.3.12 (08/10/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Added Condition for Preauth Reversal Transaction for Batch and Sequence Number

-[GP-API]: 
  - Factorize date parsing logic.
  - Increase unit tests converage for date time parsing.

-[GP-ECOM]:
  - Improve customer number setting.
  - Add logic for Open Banking Refund.

## v10.3.11 (08/09/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Added code for 40/80/90 and 70/70/79 scenario while retransmitting (Issue-10213)

## v10.3.10 (08/03/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Handled NullPointer Exception For Sequence No and Batch No - Issue(10226) 
  - Updated duplicate logger  from INSIDE PED / MULTI-LANE DEVICE VENDOR to INSIDE PED / MULTI-LANE DEVICE PRODUCT NAME OR MODEL - Issue(10227)
 
-Replaced NotImplementedException with UnsupportedOperationException 

## v10.3.9 (08/01/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Minor code changes in emv data length pin debit request  (Issue-10220) 
  - Batch No and sequence No changes for reversal transaction (Issue-10226)
  
## v10.3.8 (07/25/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Removed unwanted checks from toNumeric method (Issue-10221)

## v10.3.7 (07/18/23)
 
### Enhancements:

-NTS Phase1 Issue:
  -  Added code for Card sequence Number, unique Device Id and OfflineDeclineIndicator are not required to be sent pin debit track 2 format  (Issue-10220) 
  -  corrected the formatting for product quantity and price for fleet cor (Issue-10221)

- [GP-API]: Added integration examples using Hosted Fields (GP JS library), 3DS library

## v10.3.6 (07/11/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Added code for reformatting sale request into datacollect request (Issue-10213) 
  
## v10.3.5 (07/06/23)
 
### Enhancements:

-NTS Phase1 Issue:
  -  Removed Sale conditions while sending value as 'D' as part of tag 01 - Function code(Issue-10217)
  -  Wex Fleet sale Emv Fallback changes(Issue -10211)
  -  Batch Summery null issue changes (Issue-10218)
  -  Updated nexus-staging-maven-plugin version to 1.6.13
  
- [GP-API] Unit tests improvements

## v10.3.4 (06/28/23)
 
### Enhancements:

-NTS Phase1 Issue:
  -  Handled 02 (Invalid Pin)exception same as 01 (Denial Request to Balance)(Issue-10218)
  -  Added EnteredData field to the list for counting number of prompt (Issue-10219)
  
### Bug Fixes:

[GP-ECOM]: Send the correct message_version in the initiate step on 3DS2
  
## v10.3.3 (06/27/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Wex Fleet emv fallback changes(Issue-10211)
  - Provided new method in batch summery with forceToHost parameter for Issue(10214)
  - Added code for MTI change for force data collect 3des(Issue-10215)
  
## v10.3.2 (06/20/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - fleetdata null handling for Wex fleet refunds(Issue-10210)

## v10.3.1 (06/15/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Wex fleet refunds changes (Issue-10210)
  - Set Emv Fallback changes (Issue-10211)

## v10.3.0 (06/13/23)
 
### Enhancements:

-Propay Transaction Implementation

-NTS Phase1 Issue:
  - Added setKsnAndEncryptedData method which is similar to setKtbAndKsn method
  
-GP-API:
  - Show Authorization header when logging
  - Improvements in the Request Builders and on GpApiRequest
  - Unit tests updates
  
#### Bug Fixes:
-GP-ECOM: 
  - Fix nullpointer for frictionless cards on getAuthenticationData for authentication source MOBILE_SDK

## v10.2.17 (06/06/23)
 
### Enhancements:

-NTS Phase1 Issue:
  - Changes of Transaction time for both PreAuthCompletion and DataCollect must match with that of Pre Auth transaction and user data changes for pre auth ,completion and datacollect for gifts card(Issue 10198)
  - EMV Issuer Response for Debit Emv transactions (Issue-10203)
  - Fleetcor product rollup: Non fuels product codes must be reported from most expensive to least expensive (Issue-10207)
  - Fleetcor product rollup for more than four non fuels products changes (Issue-10208)
  - Resolving API Exception for Magnum PDL (Issue- 10209)
  
-GP-API: String usage refactor.

## v10.2.16 (05/25/23)

### Enhancements:

-NTS Phase1 Issue:
- Correct the formatting for Wex Fleet Approved Amount field (Issue-10204)
- Rollback code of calling primary to secondary,now it will call only primary end point and return 40,80,90(Issue-10185)
- Changes of Transaction time for both PreAuthCompletion and DataCollect must match with that of Pre Auth transaction and user data changes for pre auth ,completion and datacollect(Issue 10198)

- GP-API: Refacto on the GpApiRequest class
- Unit tests updates on our utils classes

#### Bug Fixes:
- GP-ECOM: Fix serialization issue on PM_METHODS field to support both card and APMs

## v10.2.15 (05/16/23)

### Enhancements:

-NTS Phase1 Issue:
- Synchorny value S added in Authorizer code (Issue-10183)
- Primary and Secondary fallback scenario's((For host response code 40,80,90) changes (Issue-10185)
- Batch Summary Total credits and Total debits related changes (Issue-10188)
- Batch close Retransmits : Updating message code when response code is 01(out of balance)  (Issue-10194,10201)
- Estimated Purchase amount changes for WEX (Issue-10197)

- GP-API: Manage fund transfers, splits and reverse splits in your partner network.
  - https://developer.globalpay.com/api/transfers
  - https://developer.globalpay.com/api/transactions#/Split%20a%20Transaction%20Amount/splitTransaction
  - Exclude  vulnerable dependency on "commons-logging" suggested by Macroscope

## v10.2.14 (05/08/23)

### Enhancements:

-NTS Phase1 Issue:
- HOST RESPONSE CODE field changes Issue(10186,10196)
- Debit Pre-Auth Cancellation: Card Sequence Number ,Offline Decline Indicator and Unique Device Id fields changes Issues (10189,10190,10191)

GP-ECOM:
-Add to the mapping response fields: acs_reference_number, acs_signed_content,acs_interface and acs_ui_template for the authentication source MOBILE_SDK

Portico Gateway:
-Enable CreditAuth transaction type for Apple Pay & Google Pay

## v10.2.13 (05/02/23)

### Enhancements:

-NTS Phase1 Issue:
- Primary and secondary end point (10185)
- HOST RESPONSE CODE field Added(10186)
- Wex Prompting(10187)
- Visa Fleet Data Collect User data Length(10177)
- Batch Summary missing values(10188)
- GP-API:
  -  Manage merchant accounts for partner solution

## v10.2.12 (04/18/23)

### Enhancements:

-NTS Phase1 Issue:
-Tokenization (10121)
- Addition of HostResponseCode field (10186)


## v10.2.11 (04/10/23)

### Enhancements:

-NTS Phase1 Issue:
- Tokenization changes (10121)
- Batch Summary Changes (10180)- Addition to isNtsBalanced() method

## v10.2.10 (04/06/23)

### Enhancements:

-NTS Phase1 Issue:
- Prior message Information changes (10142)
- FleetCor Credit Adjustment User Data changes(10158)
- MasterCard Purchasing DataCollect changes(10171)
- customer code change for mastercard purchasing (10173)
- MasterCard Purchasing MasterCard code Changes (10172)
- Batch Summary changes (10176)
- User Data Expansion for Data Collect(10180)


## v10.2.9 (04/03/23)

### Enhancements:
-NTS Phase1 Issue:
-  Prior Message Information Issue updates(10142)
-  Batch Close Issue(10161)

## v10.2.8 (03/30/23)

### Enhancements:
- GP-API: Update /merchants create request
- GP-API: Add payer information on Transaction object
- GP-API: Avoid setting empty fields on GpApiAuthorizationRequestBuilder
- GP-ECOM/REALEX: Improvements for 3DS and Recurring unit test

## v10.2.7 (03/27/23)

### Enhancements:
-NTS Phase1 Issue:
- NTS Debit Partial Approval Issue(10166)
- Pin Block Format change(10169)
- Debit Offline Pin Verified Scenarios(10170)

## v10.2.6 (03/23/23)

### Enhancements:
- NTS Phase1 Issue:
  - User Data spaces for Data collect (10167)
  - Gift Card Track1 entry method Issues(10174)
  - Prior Message Information changes Issue(10142)

## v10.2.5 (03/16/23)

### Enhancements:
- GP-ECOM/REALEX: Add Payment Scheduler

## v10.2.4 (03/15/23)

### Enhancements:
- NTS Phase1 Issue:
  - Void Timestamp Issue for credit card(10123)
  - Gift Card Entry Method Issues(10150, 10155, 10156, 10157, 10162, 10163)
  - Authorizer Code for data collect Issue(10149)
  - Mastercard Purchasing Partial Approval mapping Issue(10151,10165)
  - Primary and Secondary Endpoints Issue (10159)
  - Tokenization POC for resubmit Transaction Issue(10121)

## v10.2.3 (03/07/23)

### Enhancements:

- GP-API: Open Banking
- GP-API: Fix MapTransactionSummaryTest()
- GP-API: Add Account ID
- GP-API: Rename Test Classes
- GP-API: Fix Access Token Set Manual On Header
- GP-ECOM/REALEX: Rename Test Classes
- Update GSON & HttpMime Dependencies

## v10.2.2 (02/28/23)

### Enhancements:
- NTS Phase1 Issue:
  - Transcation Date and Transaction Time Issues(Issue-10123,10145,10148)
  - Reversal User data change for wex fleet (Issue-10152)
  - Approval code changes for reversal request (Issue- 10153)
  - Formatted tag 22 and 23(Pin Block and KSN) changes (Issue-10141)
  - GiftCard Entry Method changes Issues(Issues-10150,10155,10156,10157)
- GP-API: Misc Tests Updated
- REALEX: OpenBanking_GetBankPaymentById() test updated

## v10.2.1 (02/23/23)

### Enhancements:
- NTS phase1 Issues:
  - PAN With Expiry,Pin block and KSN Changes For Pin debit Without Track Format(Issue-10146)
- Portico:
  - EMVDATA tag bug fixes

## v10.2.0 (02/21/23)

### Enhancements:
- NTS Phase1 Issue:
  - PAN With Expiry changes for PIN Debit Request Format without Track Data (Issue-10146)
- GP-API: add risk assessment feature

## v10.1.3 (02/14/23)

### Enhancements:
- NTS Phase1 Issues:
  - Approved Amount changes for Fuelman and Fleet wide(Issue-10140)
  - Fleet Data correction for Fuelman and FleetWide(Issue-10143)
- GP-API : Miscellaneous Tests Update
- GP-API: Click To Pay
- Add XGP Signature validation

## v10.1.2 (02/07/23)

### Enhancements:
NTS Issue: Mastercard purchasing Conditions is added for MasterCardBanknetRefId & Settlement Date Changes

## v10.1.1 (01/31/23)

### Enhancements:
NTS Issue: Approval Code Changes for Void Transaction
GP-API: Update Java Server Demo for a Client app to support decoupled authentication on 3DS flow

## v10.1.0 (01/19/23)

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

## v10.0.6 (01/05/23)

### Enhancements:
- GP-API: Buy Now and Pay Later

## v10.0.5 (12/13/22)

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

## v10.0.4 (12/01/22)

### Enhancements:
- VAPS : VAPS Spec update 19.1, 20.1, 21.1 and 21.2.
- VAPS : Added support for 3DES
- NTS issue : NTS Visa Fleet Odometer Changes
- Macroscope Security Issues

## v10.0.3 (11/17/22)

### Enhancements:

- Align CountryData & CountryUtils to other SDKs
- GP-API: Misc Test Fixed
- NTS: Added Changes for Wex Fleet Service prompt list & Voyager Fleet Driver ID Update

## v10.0.2 (11/08/22)

### Enhancements:

- GP-API: Fraud Management
- GP-API: Misc Tests Updated

## v10.0.1 (11/03/22)

### Enhancements:

- NTS: Added the Extended user data flag to Data collect.
- NTS: Updated the Header time format to 24 HR.
- NTS: Updated the STAN for Data collect request.
- NTS: Updated the `approvalCode` in response for Data collect.
- NTS: Added `bankcardData` variable to reference object.
- NTS: Log optimization.

## v10.0.0 (10/25/22)

### Enhancements:

- GP-API, GP-ECOM/REALEX: Sunset 3DS1
- Java Server Demo for a Client app
- GP-API: Update unit tests with new set of credentials for GP-API

## v9.0.3 (10/11/22)

### Enhancements:

- NTS : Added Changes for DataCollect & Void transaction
- GP-API: PayLink enhancements

## v9.0.2 (09/29/22)

### Enhancements:

- Genius/Merchantware: Added initial support

## v9.0.1 (09/27/22)

### Enhancements:

- VAPS: Added support for FleetCor
- VAPS: Added Support for DE49 and DE50 Currency Code
- NTS: Added Terminal Types N1 and N2

## v9.0.0 (09/13/22)

### Enhancements:

- PORTICO: Added PorticoConfig for Portico Configuration instead of GatewayConfig
- GP-ECOM/REALEX: Added GpEcomConfig for GP-ECOM/Realex Configuration instead of GatewayConfig

## v8.0.7 (09/08/22)

### Enhancements:

- VAPS: 20.1 specification update.
- Realex: Set CountryCode for country node
- Realex: Set ChallengeRequestIndicator value for HPP_CHALLENGE_REQUEST_INDICATOR property

## v8.0.6 (08/29/22)

### Enhancements:

- VAPS: 19.1 specification update.
- PORTICO: 3DS and wallet data tags updated.
- VAPS: Batch close issue fixed.
- isNotNullInSubProperty validation on Builders

## v8.0.5 (08/09/22)

### Enhancements:

- GP-ECOM/REALEX: Supported Stored Credential when creating a new card
- Security changes: Set TLS 1.2 as default for socket connections
- GP-API: Updated Misc Tests
-

## v8.0.4 (08/02/22)

### Enhancements:

- GP-API: Added PayLink API that allows you to generate single or multi-use unique payment links.

## v8.0.3 (07/14/22)

### Enhancements:

- GP-ECOM/REALEX: Updated unit test for SupplementaryData
- GP-API: Added Incremental Auth

## v8.0.2 (07/05/22)

### Enhancements:

- GP-ECOM/REALEX: Added Open Banking as new payment method
- GP-API: Added "acsReferenceNumber" mapping for 3DS Initiate Authentication flow
- PORTICO: Added EBT reversal support
- GP-API: Mapped providerServerTransRef field on ThreeDSecure class

## v8.0.1 (06/29/22)

### Enhancements:

- GP-API Dynamic Descriptor
- HPP EnableExemptionOptimization field
- Flag to do not insert "x-gp-sdk" header on GpApiConnector requests for Android SDK

## v8.0.0 (06/27/22)

### Enhancements:

- GNAP Connector Implementation

## v7.0.5 (06/14/22)

### Enhancements:

- Portico: Add support for AdditionalDuplicateData response element

## v7.0.4 (06/07/22)

### Enhancements:

- Adjust Sale Transaction
- Adjust POST Search Payment Method
- Adjust GET Dispute Document
- Add New HPP Fields in the SDKs for HPP Capture Billing enhancement
- Update VerifyTokenizedPaymentMethodWithFingerprint() test

## v7.0.3 (05/16/22)

### Enhancements:

- Update Payment Token
- Portico: added DebitAuth and DebitAddToBatch
- Portico: Updated the existing DebitReversal request with Track 2 Data

## v7.0.2 (04/21/22)

### Enhancements:

- Added Fingerprint
- Updated code for Secure3D and WalletData Element
- Added Challenge Indicator Nullity validation
- Fixed comments on verfifySignature methods
- Aligned HostedPaymentData constructor with other SDKs
- Updated Misc Tests

## v7.0.1 (04/05/22)

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

## v7.0.0 (03/24/22)

### Enhancements:

- NTS Connector Phase 1

---

## v6.3.7 (03/22/22)

### Enhancements:

- UPA devices: add support for batch summary report, batch detail report, and open tab details report
- UPA devices: various modifications to account for latest UPA version's changes

### Bug Fixes:

- UPA devices: fix potential error from disconnect attempt

---

## v6.3.6 (03/15/22)

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

## v6.3.5 (03/01/22)

### Bug Fixes:

- Fix "AccountType" mapping with HPS/Portico integrations

---

## v6.3.4 (03/01/22)

### Enhancements:

- Improved support for UPA Terminals/Devices
- Added logging functionality; supports easier output to file

### Bug Fixes:

- Fix reporting functionality bug for Heartland/Portico integrations

---

## v6.3.3 (02/17/22)

### Enhancements:

- Add IN_APP value for entry_mode on GP-API
- Add LNK_ID in Create Transaction Request
- Update Misc Tests

---

## v6.3.2 (02/10/22)

### Enhancements:

- Dynamic Currency Conversion on GP-API Connector
- Add Alternative Payment Methods (APMs) to HPP
- Misc tests updated

### Bug Fixes:

- Strip fleet from card type for Realex Connector and GP-API Connector
- Diners card type fix on Realex Connector

---

## v6.3.1 (12/16/21)

### Enhancements:

- Add support for 'ManageTokens' transaction using Heartland/Portico gateway
- Add support for 'Reversal' transaction using UPA devices
- Add PAYPAL alternative payment method on GP-API Connector
- Update miscProductData structure
- Add configName to Recurring Services
- Update MasterCard regex

---

## v6.3.0 (11/18/21)

### Enhancements:

- Add initial support for UPA Terminals/Devices

---

## v6.2.8 (11/16/21)

### Enhancements:

- Add AVS missing mapping to response
- Availability of country lists for Android SDK
- Add tests for query param paymentMethod

### Bug Fixes:

- Transaction reporting not working due to new mandatory query parameter added

---

## v6.2.7 (11/11/21)

### Enhancements:

- Add Mobile phone country code and subscriber number validation for 3DS2
- Add recurring payment with stored credentials functionality to GpApi
- Thread Safe Changes
- Tests Updates
- Vaps Connector refactor

---

## v6.2.6 (11/02/21)

### Enhancements:

- Add ACH to GpApi

---

## v6.2.4 (10/28/21)

### Enhancements:

- Add Digital Wallet to GpApi
- Add AvsResponse mappings to GpApi

---

## v6.2.3 (10/14/21)

### Enhancements:

- Add Multiple merchants to GpApi

---

## v6.2.2 (09/08/21)

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

## v6.1.94 (08/12/21)

### Enhancements:

- Tests clean up

---

## v6.1.93 (08/05/21)

### Enhancements:

- Upgrade GP-API to 2021-03-22 version and fix tests to support it
- Add GP-API 3DS enhanced

### Bug Fixes:

- Set the GpApiConfig's proxy setting to the parent's Gateway class

---

## v6.1.90 (07/13/21)

### Enhancements:

- Support findSettlementDisputes by Deposit ID
- Fix Failing Disputes Tests

### Bug Fixes:

- Exceptions unhidden on Credit.updateTokenExpiry() & Credit.deleteToken()

---

## v6.1.89 (07/01/21)

### Enhancements:

- Support Netherlands Antilles Country

---

## v6.1.84 (06/17/21)

### Enhancements:

- Message Extension List added to ThreeDSecure class

---

## v6.1.83 (06/08/21)

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

## v6.1.80 (04/13/21)

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
