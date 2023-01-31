package com.example.restservice;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.restservice.entities.accessToken.AccessTokenInput;
import com.example.restservice.entities.accessToken.AccessTokenOutput;
import com.example.restservice.entities.authorizationData.AuthorizationDataInput;
import com.example.restservice.entities.authorizationData.AuthorizationDataOutput;
import com.example.restservice.entities.checkEnrollment.CheckEnrollmentInput;
import com.example.restservice.entities.checkEnrollment.CheckEnrollmentOutput;
import com.example.restservice.entities.getAuthenticationData.GetAuthenticationDataInput;
import com.example.restservice.entities.getAuthenticationData.GetAuthenticationDataOutput;
import com.example.restservice.entities.initiateAuthentication.InitiateAuthenticationInput;
import com.example.restservice.entities.initiateAuthentication.InitiateAuthenticationOutput;
import com.global.api.ServicesContainer;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.MobileData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AuthenticationSource;
import com.global.api.entities.enums.SdkInterface;
import com.global.api.entities.enums.SdkUiType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.GpApiService;
import com.global.api.services.Secure3dService;
import com.global.api.utils.JsonDoc;

@RestController
public class EndpointsController {

	private static final String template = "Hello, %s!";
	private static final String GP_API_CONFIG_NAME = "GP_API_CONFIG_FOR_IOS_APP_SERVER_SIDE";
	private final AtomicLong counter = new AtomicLong();

	@PostMapping("/accessToken")
	@ResponseBody
	public AccessTokenOutput getAccessToken(@RequestBody AccessTokenInput input) throws GatewayException, ConfigurationException {

        GpApiConfig gpApiConfig = new GpApiConfig();

        gpApiConfig.setAppId(input.getAppId());
        gpApiConfig.setAppKey(input.getAppKey());
        
        if( input.getPermissions().length > 0 ) {
        	gpApiConfig.setPermissions(input.getPermissions());
        }

        // These properties could be populated from the input if needed
        gpApiConfig.setCountry("GB");
        gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");
        
        gpApiConfig.setEnableLogging(true);
		
        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(gpApiConfig);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);
        
		return
				new AccessTokenOutput()
						.setAccessToken(accessTokenInfo.getAccessToken());
	}
	
	@PostMapping("/checkEnrollment")
	@ResponseBody
	public CheckEnrollmentOutput checkEnrollment(@RequestBody CheckEnrollmentInput input) throws ApiException {

		CreditCardData tokenizedCard = new CreditCardData();
		tokenizedCard.setToken(input.getCardToken());

		Secure3dBuilder checkEnrollment =
				Secure3dService
						.checkEnrollment(tokenizedCard)
						.withCurrency(input.getCurrency())
						.withAmount(new BigDecimal(input.getAmount()));

		if (input.getPreferredDecoupledAuth()) {
			checkEnrollment
					.withDecoupledNotificationUrl("https://www.example.com/decoupledNotification");
		}

		ThreeDSecure threeDSecure =
				checkEnrollment
						.execute(GP_API_CONFIG_NAME);

		return
				new CheckEnrollmentOutput()
						.setEnrolled(threeDSecure.getEnrolledStatus())
						.setVersion(threeDSecure.getVersion().getValue().toUpperCase())
						.setMessageVersion(threeDSecure.getMessageVersion())
						.setStatus(threeDSecure.getStatus())
						.setLiabilityShift(threeDSecure.getLiabilityShift())
						.setServerTransactionId(threeDSecure.getServerTransactionId())
						.setSessionDataFieldName(threeDSecure.getSessionDataFieldName())
						.setMethodUrl(threeDSecure.getIssuerAcsUrl())
						.setMethodData(threeDSecure.getPayerAuthenticationRequest())
						.setMessageType(threeDSecure.getMessageType())
						.setAcsInfoIndicator(threeDSecure.getAcsInfoIndicator().toString());
	}

	@PostMapping("/initiateAuthentication")
	@ResponseBody
	public InitiateAuthenticationOutput initiateAuthentication(@RequestBody InitiateAuthenticationInput input) throws ApiException {

		MobileData mobileData = new MobileData();

		mobileData.setApplicationReference(input.getMobileData().getApplicationReference());
		mobileData.setSdkTransReference(input.getMobileData().getSdkTransReference());
		mobileData.setReferenceNumber(input.getMobileData().getReferenceNumber());

    	// --------------------------------------------------------------------------------
		SdkInterface sdkInterface = null;
    	
    	for(SdkInterface value : SdkInterface.values()) {
    		if(value.getValue().equals(input.getMobileData().getSdkInterface())) {
    			sdkInterface = value;
    		}
    	}

		mobileData.setSdkInterface(sdkInterface);
    	// --------------------------------------------------------------------------------

    	mobileData.setEncodedData(input.getMobileData().getEncodedData());
    	mobileData.setMaximumTimeout(input.getMobileData().getMaximumTimeout());

    	String ephemeralPublicKeyX = input.getMobileData().getEphemeralPublicKeyX();
    	String ephemeralPublicKeyY = input.getMobileData().getEphemeralPublicKeyY();

    	// --------------------------------------------------------------------------------
    	JsonDoc ephemeralPublicKey =
    			JsonDoc.parse("{"
		    					+ "\"crv\": \"P-256\","
		    					+ "\"kty\": \"EC\","
		    					+ "\"x\"  : \"" + ephemeralPublicKeyX + "\","
		    					+ "\"y\"  : \"" + ephemeralPublicKeyY + "\""
    					+ "}");

    	mobileData.setEphemeralPublicKey(ephemeralPublicKey);
    	// --------------------------------------------------------------------------------    	
    	SdkUiType[] sdkUiTypes = new SdkUiType[] { 
    			SdkUiType.Text, SdkUiType.SingleSelect, SdkUiType.MultiSelect, SdkUiType.OOB, SdkUiType.HTML_Other };
    	
    	mobileData.setSdkUiTypes(sdkUiTypes);

		CreditCardData tokenizedCard = new CreditCardData();
		tokenizedCard.setToken(input.getCardToken());

		Secure3dBuilder initiateAuthentication =
				Secure3dService
						.initiateAuthentication(tokenizedCard, input.getThreeDsecure())
						.withAuthenticationSource(AuthenticationSource.MobileSDK)
						.withAmount(new BigDecimal(input.getAmount()))
						.withCurrency(input.getCurrency())
						.withOrderCreateDate(DateTime.now())
						.withCustomerEmail(input.getCustomerEmail())
						// Enable if needed
						//	        	.withAddress(input.getBillingAddress(), AddressType.Billing)
						//	        	.withAddress(input.getShippingAddress(), AddressType.Shipping)
						//	        	.withAddressMatchIndicator(input.getAddressMatchIndicator())
						//	        	.withAuthenticationSource(input.getAuthenticationSource())
						//	        	.withAuthenticationRequestType(input.getAuthenticationRequestType())
						//	        	.withMessageCategory(input.getMessageCategory())
						//	        	.withChallengeRequestIndicator(input.getChallengeRequestIndicator())
						//	        	.withMethodUrlCompletion(input.getMethodUrlCompletion())
						.withBrowserData(input.getBrowserData())
						.withMobileData(mobileData);

		if (input.getPreferredDecoupledAuth()) {
			initiateAuthentication.withDecoupledFlowRequest(true)
					.withDecoupledFlowTimeout(input.getDecoupledFlowTimeout())
					.withDecoupledNotificationUrl("https://www.example.com/decoupledNotification");
		}

		ThreeDSecure threeDSecure =
				initiateAuthentication
						.execute(GP_API_CONFIG_NAME);

		return
				new InitiateAuthenticationOutput()
						.setEnrolled(threeDSecure.getEnrolledStatus())
						.setVersion(threeDSecure.getVersion().getValue().toUpperCase())
						.setStatus(threeDSecure.getStatus())
						.setLiabilityShift(threeDSecure.getLiabilityShift())
						.setServerTransactionId(threeDSecure.getProviderServerTransRef())
						.setEci(threeDSecure.getEci())
						.setSessionDataFieldName(threeDSecure.getSessionDataFieldName())
						.setSessionDataFieldName(threeDSecure.getSessionDataFieldName())
						.setMethodUrl(threeDSecure.getIssuerAcsUrl())
						.setMessageVersion(threeDSecure.getMessageVersion())
						.setDsTransferReference(threeDSecure.getDirectoryServerTransactionId())
						.setLiabilityShift(threeDSecure.getLiabilityShift())
						.setAcsTransactionId(threeDSecure.getAcsTransactionId())
						.setAcsReferenceNumber(threeDSecure.getAcsReferenceNumber())
						.setPayerAuthenticationRequest(threeDSecure.getPayerAuthenticationRequest())
						.setAuthenticationValue(threeDSecure.getAuthenticationValue());
	}

	@PostMapping("/getAuthenticationData")
	@ResponseBody
	public GetAuthenticationDataOutput getAuthenticationData(@RequestBody GetAuthenticationDataInput input) throws ApiException {

		ThreeDSecure threeDSecure =
	    	Secure3dService
	        	.getAuthenticationData()
	        	.withServerTransactionId(input.getServerTransactionId())
	        	.execute(GP_API_CONFIG_NAME);

		return
				new GetAuthenticationDataOutput()
					.setStatus(threeDSecure.getStatus())
					.setLiabilityShift(threeDSecure.getLiabilityShift())
					.setServerTransactionId(threeDSecure.getServerTransactionId());
	}

	@PostMapping("/authorizationData")
	@ResponseBody
	public AuthorizationDataOutput authorizationData(@RequestBody AuthorizationDataInput input) throws ApiException {

		CreditCardData tokenizedCard = new CreditCardData();
		tokenizedCard.setToken(input.getCardToken());
		
		Transaction transaction =
						tokenizedCard
							.charge(new BigDecimal(input.getAmount()))
							.withCurrency(input.getCurrency())
							.execute(GP_API_CONFIG_NAME);

		return
				new AuthorizationDataOutput()
					.setTransactionId(transaction.getTransactionId())
					.setStatus(transaction.getResponseMessage())
					.setAmount(transaction.getBalanceAmount().toString())
					.setDate(transaction.getTimestamp())
					.setReference(transaction.getReferenceNumber())
					.setBatchId(transaction.getBatchSummary().getBatchReference())
					.setResponseCode(transaction.getResponseCode())
					.setResponseMessage(transaction.getResponseMessage());
	}

}