package com.global.api.terminals.upa.subgroups;

import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;

public class RequestLodgingFields {
    private String folioNumber;
    private Integer stayDuration;
    private String checkInDate;
    private String checkOutDate;
    private BigDecimal dailyRate;
    private Integer preferredCustomer;
    private int[] extraChargeTypes;
    private BigDecimal extraChargeTotal;

    private static final String FOLIO_NUMBER = "folioNumber";
    private static final String STAY_DURATION = "stayDuration";
    private static final String CHECK_IN_DATE = "checkInDate";
    private static final String CHECK_OUT_DATE = "checkOutDate";
    private static final String DAILY_RATE = "dailyRate";
    private static final String PREFERRED_CUSTOMER = "preferredCustomer";
    private static final String EXTRA_CHARGE_TYPES = "extraChargeTypes";
    private static final String EXTRA_CHARGE_TOTAL = "extraChargeTotal";

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getLodging() != null) {
            if (builder.getLodging().getFolioNumber() != null) {
                this.folioNumber = builder.getLodging().getFolioNumber().toString();
            }

            if (builder.getLodging().getStayDuration() != null) {
                this.stayDuration = builder.getLodging().getStayDuration();
            }

            if (builder.getLodging().getCheckInDate() != null) {
                this.checkInDate =builder.getLodging().getCheckInDate();
            }

            if (builder.getLodging().getCheckOutDate() != null) {
                this.checkOutDate = builder.getLodging().getCheckOutDate();
            }

            if (builder.getLodging().getDailyRate() != null) {
                this.dailyRate = builder.getLodging().getDailyRate();
            }

            if (builder.getLodging().getPreferredCustomer() != null) {
                this.preferredCustomer = builder.getLodging().getPreferredCustomer();
            }

            if (builder.getLodging().getExtraChargeTypes() != null) {
                this.extraChargeTypes = builder.getLodging().getExtraChargeTypes();
            }

            if (builder.getLodging().getExtraChargeTotal() != null) {
                this.extraChargeTotal = builder.getLodging().getExtraChargeTotal();
            }

        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc lodging = new JsonDoc();
        boolean hasContents = false;

        if (folioNumber != null) {
            lodging.set(FOLIO_NUMBER, folioNumber);
            hasContents = true;
        }

        if (stayDuration != null) {
            lodging.set(STAY_DURATION, stayDuration);
            hasContents = true;
        }

        if (checkInDate != null) {
            lodging.set(CHECK_IN_DATE, checkInDate);
            hasContents = true;
        }

        if (checkOutDate != null) {
            lodging.set(CHECK_OUT_DATE, checkOutDate);
            hasContents = true;
        }

        if (dailyRate != null) {
            lodging.set(DAILY_RATE, StringUtils.toCurrencyString(dailyRate));
            hasContents = true;
        }

        if (preferredCustomer != null) {
            lodging.set(PREFERRED_CUSTOMER, preferredCustomer);
            hasContents = true;
        }

        if (extraChargeTypes != null) {
            lodging.set(EXTRA_CHARGE_TYPES, extraChargeTypes);
            hasContents = true;
        }

        if (extraChargeTotal != null){
            lodging.set(EXTRA_CHARGE_TOTAL,extraChargeTotal.toString());
            hasContents = true;
        }

        return hasContents ? lodging : null;
    }

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getLodging() != null) {
            if (builder.getLodging().getFolioNumber() != null) {
                this.folioNumber = builder.getLodging().getFolioNumber().toString();
            }

            if (builder.getLodging().getStayDuration() != null) {
                this.stayDuration = builder.getLodging().getStayDuration();
            }

            if (builder.getLodging().getCheckInDate() != null) {
                this.checkInDate =builder.getLodging().getCheckInDate();
            }

            if (builder.getLodging().getCheckOutDate() != null) {
                this.checkOutDate = builder.getLodging().getCheckOutDate();
            }

            if (builder.getLodging().getDailyRate() != null) {
                this.dailyRate = builder.getLodging().getDailyRate();
            }

            if (builder.getLodging().getPreferredCustomer() != null) {
                this.preferredCustomer = builder.getLodging().getPreferredCustomer();
            }

            if (builder.getLodging().getExtraChargeTypes() != null) {
                this.extraChargeTypes = builder.getLodging().getExtraChargeTypes();
            }

            if (builder.getLodging().getExtraChargeTotal() != null) {
                this.extraChargeTotal = builder.getLodging().getExtraChargeTotal();
            }

        }
    }
}
