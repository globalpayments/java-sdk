package com.global.api.tests.testdata;

import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.CheckType;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.SecCode;
import com.global.api.paymentMethods.eCheck;

public class TestChecks {
    public static eCheck certification() {
        return certification(SecCode.Ppd, CheckType.Personal, AccountType.Checking, null);
    }
    public static eCheck certification(SecCode secCode, CheckType checkType, AccountType accountType) {
        return certification(secCode, checkType, accountType, null);
    }
    public static eCheck certification(SecCode secCode, CheckType checkType, AccountType accountType, String checkName) {
        eCheck rvalue = new eCheck();
        rvalue.setAccountNumber("24413815");
        rvalue.setRoutingNumber("490000018");
        rvalue.setCheckType(checkType);
        rvalue.setSecCode(secCode);
        rvalue.setAccountType(accountType);
        rvalue.setEntryMode(EntryMethod.Manual);
        rvalue.setCheckHolderName("John Doe");
        rvalue.setDriversLicenseNumber("09876543210");
        rvalue.setDriversLicenseState("TX");
        rvalue.setPhoneNumber("8003214567");
        rvalue.setBirthYear(1997);
        rvalue.setSsnLast4("4321");
        rvalue.setCheckName(checkName);
        
        return rvalue;
    }
}
