package com.global.api.terminals.upa.subgroups;

import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.upa.Entities.Enums.UpaAcquisitionType;
import com.global.api.utils.JsonDoc;

import java.sql.Array;
import java.util.ArrayList;

public class RequestParamFields implements IRequestSubGroup {
    ArrayList<UpaAcquisitionType> acquisitionTypes = new ArrayList<UpaAcquisitionType>();
    private String acquisitionTypesString = "";
    StoredCredentialInitiator cardBrandStorage;
    String cardBrandTransactionId;
    String clerkId;
    Integer tokenRequest;
    String tokenValue;
    String directMarketInvoiceNumber;
    Integer directMarketShipMonth;
    Integer directMarketShipDay;

    private static final String DIRECT_MARKET_INVOICE_NUMBER = "directMktInvoiceNbr";
    private static final String DIRECT_MARKET_SHIP_MONTH = "directMktShipMonth";
    private static final String DIRECT_MARKET_SHIP_DAY = "directMktShipDay";

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getClerkId() != null) {
            this.clerkId = builder.getClerkId().toString();
        }

        if (builder.isRequestMultiUseToken()) {
            this.tokenRequest = 1;
        }

        if (builder.getTokenValue() != null) {
            this.tokenValue = builder.getTokenValue();
        }

        if (builder.getCardBrandStorage() != null) {
            this.cardBrandStorage = builder.getCardBrandStorage();
        }

        if (builder.getCardBrandTransactionId() != null) {
            this.cardBrandTransactionId = builder.getCardBrandTransactionId();
        }

        if (builder.getTransactionType() == TransactionType.Activate) {
            if (this.acquisitionTypes.size() != 0) {
                // handle integration-supplied list
            } else {
                this.acquisitionTypes.add(UpaAcquisitionType.Contact);
                this.acquisitionTypes.add(UpaAcquisitionType.Contactless);
                this.acquisitionTypes.add(UpaAcquisitionType.Manual);
                this.acquisitionTypes.add(UpaAcquisitionType.Scan);
                this.acquisitionTypes.add(UpaAcquisitionType.Swipe);
            }
        }

        if (builder.getDirectMarketInvoiceNumber() != null){
            this.directMarketInvoiceNumber = builder.getDirectMarketInvoiceNumber();
        }

        if (builder.getDirectMarketShipMonth() != null){
            this.directMarketShipMonth = builder.getDirectMarketShipMonth();
        }

        if (builder.getDirectMarketShipDay() != null){
            this.directMarketShipDay = builder.getDirectMarketShipDay();
        }
    }

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getClerkId() != null) {
            this.clerkId = builder.getClerkId().toString();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

        if (cardBrandStorage != null) {
            // only two values supported per v1.30 integrator's guide
            if (cardBrandStorage == StoredCredentialInitiator.Merchant) {
                params.set("cardOnFileIndicator", "M");
            }
            if (cardBrandStorage == StoredCredentialInitiator.CardHolder) {
                params.set("cardOnFileIndicator", "C");
            }
            hasContents = true;
        }

        if (cardBrandTransactionId != null) {
            params.set("cardBrandTransId", cardBrandTransactionId);
            hasContents = true;
        }

        if (clerkId != null) {
            params.set("clerkId", clerkId);
            hasContents = true;
        }

        if (tokenRequest != null) {
            params.set("tokenRequest", tokenRequest);
            hasContents = true;
        }

        if (tokenValue != null) {
            params.set("tokenValue", tokenValue);
            hasContents = true;
        }

        if (acquisitionTypes != null && !acquisitionTypes.isEmpty()) {
            acquisitionTypesString = "";

            acquisitionTypes.forEach(
                (x) -> {
                    acquisitionTypesString += x.name();
                    acquisitionTypesString += "|";
                }
            );

            acquisitionTypesString = acquisitionTypesString.substring(0, acquisitionTypesString.length() - 1);

            params.set("acquisitionTypes", acquisitionTypesString);
            hasContents = true;
        }

        if (directMarketInvoiceNumber != null){
            params.set(DIRECT_MARKET_INVOICE_NUMBER,directMarketInvoiceNumber);
            hasContents = true;
        }

        if (directMarketShipMonth != null){
            params.set(DIRECT_MARKET_SHIP_MONTH,directMarketShipMonth);
            hasContents = true;
        }

        if (directMarketShipDay != null){
            params.set(DIRECT_MARKET_SHIP_DAY,directMarketShipDay);
            hasContents = true;
        }
        
        return hasContents ? params : null;
    }

    @Override
    public String getElementString() {
        return null;
    }
}
