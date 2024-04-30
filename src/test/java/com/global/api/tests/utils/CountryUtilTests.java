package com.global.api.tests.utils;

import com.global.api.entities.Address;
import com.global.api.entities.enums.CountryCodeFormat;
import com.global.api.utils.CountryUtils;
import com.global.api.utils.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class CountryUtilTests {
    @Test
    public void getCountryCodeExact() {
        String result = CountryUtils.getCountryCodeByCountry("Ireland");
        assertNotNull(result);
        assertEquals("IE", result);
    }

    @Test
    public void getCountryCodeMisspelled() {
        String result = CountryUtils.getCountryCodeByCountry("Afganistan");
        assertNotNull(result);
        assertEquals("AF", result);
    }

    @Test
    public void getCountryCodeFromPartial() {
        String result = CountryUtils.getCountryCodeByCountry("Republic of Congo");
        assertNotNull(result);
        assertEquals("CD", result);
    }

    @Test
    public void getCountryCodeByExactCode() {
        String result = CountryUtils.getCountryCodeByCountry("IE");
        assertNotNull(result);
        assertEquals("IE", result);
    }

    @Test
    public void getCountryCodeByPartialCode() {
        String result = CountryUtils.getCountryCodeByCountry("USA");
        assertNotNull(result);
        assertEquals("US", result);
    }

    @Test
    public void getCountryCodeNullDoesNotError() {
        CountryUtils.getCountryCodeByCountry(null);
    }

    @Test
    public void getCountryCodeFakeCountry() {
        String result = CountryUtils.getCountryCodeByCountry("FakeCountry");
        assertNull(result);
    }

    @Test
    public void getCountryCodeFakeCountry2() {
        String result = CountryUtils.getCountryCodeByCountry("Fakeistan");
        assertNull(result);
    }

    @Test
    public void getCountryCodeFakeCountry3() {
        String result = CountryUtils.getCountryCodeByCountry("MyRussia");
        assertNull(result);
    }

    @Test
    public void getCountryByCodeExact() {
        String result = CountryUtils.getCountryByCode("IE");
        assertNotNull(result);
        assertEquals("Ireland", result);
    }

    @Test
    public void getCountryByThreeDigitCode() {
        String result = CountryUtils.getCountryByCode("USA");
        assertNotNull(result);
        assertEquals("United States of America", result);
    }

    @Test
    public void getPhoneByCountry() {
        String result = CountryUtils.getPhoneCodesByCountry("United States of America");
        assertNotNull(result);
        assertEquals("1", result);

        result = CountryUtils.getPhoneCodesByCountry("840");
        assertNotNull(result);
        assertEquals("1", result);

        result = CountryUtils.getPhoneCodesByCountry("US");
        assertNotNull(result);
        assertEquals("1", result);

        result = CountryUtils.getPhoneCodesByCountry("USA");
        assertNotNull(result);
        assertEquals("1", result);
    }

    @Test
    public void getCountryCodeByExactNumericCode() {
        String result = CountryUtils.getCountryCodeByCountry("840");
        assertNotNull(result);
        assertEquals("US", result);
    }

    @Test
    public void getNumericCodeByTwoDigitCode() {
        String result = CountryUtils.getNumericCodeByCountry("US");
        assertNotNull(result);
        assertEquals("840", result);
    }

    @Test
    public void getNumericCodeByThreeDigitCode() {
        String result = CountryUtils.getNumericCodeByCountry("USA");
        assertNotNull(result);
        assertEquals("840", result);
    }

    @Test
    public void getNumericCodeByCountryName() {
        String result = CountryUtils.getNumericCodeByCountry("United States of America");
        assertNotNull(result);
        assertEquals("840", result);
    }

    @Test
    public void getNumericCodeByNumericCode() {
        String result = CountryUtils.getNumericCodeByCountry("840");
        assertNotNull(result);
        assertEquals("840", result);
    }

    @Test
    public void getNumericCodeByNonExistingCountryName() {
        String result = CountryUtils.getNumericCodeByCountry("Fake Country Name");
        assertNull(result);
    }

    @Test
    public void extractDigitsTest() {
        String result = StringUtils.extractDigits("12 MAIN ST STE 34");
        assertNotNull(result);
        assertEquals("1234", result);
    }

    @Test
    public void getCountryByCodeNullDoesNotError() {
        CountryUtils.getCountryCodeByCountry(null);
    }

    @Test
    public void checkAddressCodeFromCountryExact() {
        Address address = new Address();
        address.setCountry("United States of America");
        assertNotNull(address.getCountryCode());
        assertEquals("US", address.getCountryCode());
    }

    @Test
    public void checkAddressCountryFromCodeExact() {
        Address address = new Address();
        address.setCountryCode("US");
        assertNotNull(address.getCountry());
        assertNotNull("United States", address.getCountry());
    }

    @Test
    public void checkAddressCodeFromCountryFuzzy() {
        Address address = new Address();
        address.setCountry("Afganistan");
        assertNotNull(address.getCountryCode());
        assertEquals("AF", address.getCountryCode());
    }

    @Test
    public void checkAddressCountryFromCodeFuzzy() {
        Address address = new Address();
        address.setCountryCode("USA");
        assertNotNull(address.getCountry());
        assertNotNull("United States", address.getCountry());
    }

    @Test
    public void addressIsCountryExactMatch() {
        Address address = new Address();
        address.setCountry("United States of America");
        assertTrue(address.isCountry("US"));
    }

    @Test
    public void CheckAddressCodeFromNumericCodeExact() {
        Address address = new Address();
        address.setCountry("056");
        assertNotNull(address.getCountryCode());
        assertEquals("BE", address.getCountryCode());
    }

    @Test
    public void addressIsCountryExactMisMatch() {
        Address address = new Address();
        address.setCountry("United States");
        assertFalse(address.isCountry("GB"));
    }

    @Test
    public void addressIsCountryFuzzyMatch() {
        Address address = new Address();
        address.setCountry("Afganistan");
        assertTrue(address.isCountry("AF"));
    }

    @Test
    public void addressIsCountryFuzzyMisMatch() {
        Address address = new Address();
        address.setCountry("Afganistan");
        assertFalse(address.isCountry("GB"));
    }
    @Test
    public void getCountryByAlpha2Format(){
        String result = CountryUtils.getCountryCodeByCountry("AF", CountryCodeFormat.Alpha2);
        assertNotNull(result);
        assertEquals("AF", result);
    }
    @Test
    public void getCountryCodeByExactNumericValue() {
        String result = CountryUtils.getCountryCodeByCountry("840",CountryCodeFormat.Numeric);
        assertNotNull(result);
        assertEquals("840", result);
    }
    //negative scenario
    @Test
    public void getCountryCodeByMismatchCountryFormat() {
        String result = CountryUtils.getCountryCodeByCountry("840",CountryCodeFormat.Numeric);
        assertNotNull(result);
        assertEquals("US", result);
    }

}