# Changelog

## Latest

### Enhancements:

- Upgrade GP-API to 2021-03-22 version and fix tests to support it
- Add GP-API 3DS enhanced
- 

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

### Bug Fixes:

-

---

## v6.1.84 (17/06/2021)

### Enhancements:

- Message Extension List added to ThreeDSecure class 

### Bug Fixes:

- 

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