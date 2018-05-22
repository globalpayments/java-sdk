package com.global.api.tests;

import com.global.api.entities.Address;
import com.global.api.utils.CountryUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
        assertEquals("United States", result);
    }

    @Test
    public void getCountryByCodeNullDoesNotError() {
        CountryUtils.getCountryCodeByCountry(null);
    }

    @Test
    public void checkAddressCodeFromCountryExact() {
        Address address = new Address();
        address.setCountry("United States");
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
        address.setCountry("United States");
        assertTrue(address.isCountry("US"));
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
}