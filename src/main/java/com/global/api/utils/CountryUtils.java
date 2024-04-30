package com.global.api.utils;

import com.global.api.entities.Address;
import com.global.api.entities.CountryData;
import com.global.api.entities.enums.CountryCodeFormat;
import lombok.var;

import java.util.*;

public class CountryUtils {
    private static final int significantCountryMatch = 6;
    private static final int significantCodeMatch = 3;
    private static final CountryData countryData;

    static {
        countryData = new CountryData();
    }

    public static boolean isCountry(Address address,  String countryCode) {
        if(address.getCountryCode() != null)
            return address.getCountryCode().equals(countryCode);
        else if(address.getCountry() != null) {
             String code = getCountryCodeByCountry(address.getCountry(), null);
            if(code != null)
                return code.equals(countryCode);
            return false;
        }
        return false;
    }

    public static  String getCountryByCode( String countryCode){
        if (countryCode == null)
            return null;
         String output = "";

        if ( isAlpha2(countryCode))
        {
            output = convertFromAlpha2(countryCode, CountryCodeFormat.Name);
        }
        else if ( isAlpha3(countryCode))
        {
            output = ConvertFromAlpha3(countryCode, CountryCodeFormat.Name);
        }
        else if (isNumeric(countryCode))
        {
            output = ConvertFromNumeric(countryCode, CountryCodeFormat.Name);
        }
        if (! StringUtils.isNullOrEmpty(output))
        {
            return output;
        }
        else
        {
            if (countryCode.length() > 3)
                return null;
            return fuzzyMatch(CountryData.getCountryByAlpha2Code(), countryCode, significantCodeMatch);
        }
    }
    public static  String getCountryByCode( String countryCode,CountryCodeFormat countryCodeFormat){
        if (countryCode == null)
            return null;
        String output = "";

        switch (countryCodeFormat){
            case Alpha2: {
                output = convertFromAlpha2(countryCode, CountryCodeFormat.Name);
            }break;
            case Numeric: {
                output = ConvertFromAlpha3(countryCode, CountryCodeFormat.Name);
            }break;
            case Alpha3: {
                output = ConvertFromNumeric(countryCode, CountryCodeFormat.Name);
            }break;
        }
        if (! StringUtils.isNullOrEmpty(output))
        {
            return output;
        }
        else
        {
            if (countryCode.length() > 3)
                return null;
            return fuzzyMatch(CountryData.getCountryByAlpha2Code(), countryCode, significantCodeMatch);
        }
    }

    // Returns Country Code
    public static String getCountryCodeByCountry(String country) {
        return getCountryCodeByCountry(country, null);
    }

    public static String getCountryCodeByCountry(String country, CountryCodeFormat format) {
        if (format == null) {
            format = CountryCodeFormat.Alpha2;
        }

        String output = "";

        if (country == null)
            return null;

        if (isCountryName(country)) {
            output = convertFromName(country, format);
        } else if (isAlpha2(country)) {
            output = convertFromAlpha2(country, format);
        } else if (isAlpha3(country)) {
            output = ConvertFromAlpha3(country, format);
        } else if (isNumeric(country)) {
            output = ConvertFromNumeric(country, format);
        }

        if (!StringUtils.isNullOrEmpty(output)) {
            return output;
        } else {
            // it's not a country match or a countryCode match so let's get fuzzy
            return (fuzzyByFormat(format, country));
        }
    }

    private static String fuzzyByFormat(CountryCodeFormat format, String country) {
        String fuzzyCountryMatch = "";
        String output = "";

        if (format == CountryCodeFormat.Alpha2) {
            fuzzyCountryMatch = fuzzyMatch(CountryData.getAlpha2CodeByCountry(), country, significantCountryMatch);
        }
        else if (format == CountryCodeFormat.Alpha3)
        {
            fuzzyCountryMatch = fuzzyMatch(CountryData.getAlpha3CodeByCountry(), country, significantCountryMatch);
        }
        else if (format == CountryCodeFormat.Numeric)
        {
            fuzzyCountryMatch = fuzzyMatch(CountryData.getNumericCodeByCountry(), country, significantCountryMatch);
        }
        if (fuzzyCountryMatch != null)
            return fuzzyCountryMatch;
        else
        {
            // assume if it's > 3 it's not a code and do not do fuzzy code matching
            if (country.length() > 3)
                return null;
            // 3 or less, let's fuzzy match
            String fuzzyCodeMatch;
            if (format == CountryCodeFormat.Alpha2) {
                fuzzyCodeMatch = fuzzyMatch(CountryData.getCountryByAlpha2Code(), country, significantCodeMatch);
                if (fuzzyCodeMatch != null)
                    output = CountryData.getAlpha2CodeByCountry().get(fuzzyCodeMatch);
            }
            else if (format == CountryCodeFormat.Alpha3) {
                fuzzyCodeMatch = fuzzyMatch(CountryData.getCountryByAlpha3Code(), country, significantCodeMatch);
                if (fuzzyCodeMatch != null)
                    output = CountryData.getAlpha3CodeByCountry().get(fuzzyCodeMatch);
            }
            else if (format == CountryCodeFormat.Numeric) {
                fuzzyCodeMatch = fuzzyMatch(CountryData.getCountryByNumericCode(), country, significantCodeMatch);
                if (fuzzyCodeMatch != null)
                    output = CountryData.getNumericCodeByCountry().get(fuzzyCodeMatch);
            }
            return output;
        }
    }

    private static  String fuzzyMatch(Map< String,  String> dict,  String query, int significantMatch) {
         String rvalue = null;
        Map< String,  String> matches = new HashMap<>();

        // now we can loop
        int highScore = -1;
        for( String key : dict.keySet()) {
            int score = fuzzyScore(key, query);
            if(score > significantMatch && score > highScore) {
                matches = new HashMap<>();

                highScore = score;
                rvalue = dict.get(key);
                matches.put(key, rvalue);
            }
            else if(score == highScore) {
                matches.put(key, dict.get(key));
            }
        }

        if(matches.size() > 1)
            return null;
        return rvalue;
    }

    // Return Numeric Code for country
    public static String getNumericCodeByCountry(String country) {
        CountryData CountryData = new CountryData();

        if (isCountryName(country) && CountryData.getNumericCodeByCountry().containsKey(country)) {
            return CountryData.getNumericCodeByCountry().get(country);
        }
        if (isAlpha2(country) && CountryData.getNumericByAlpha2CountryCode().containsKey(country)) {
            return CountryData.getNumericByAlpha2CountryCode().get(country);
        }
        else if (isAlpha3(country) && CountryData.getNumericByAlpha3CountryCode().containsKey(country)) {
            return CountryData.getNumericByAlpha3CountryCode().get(country);
        }
        else if (isNumeric(country)) {
            return country;
        }

        return null;
    }

    private static Integer fuzzyScore(final CharSequence term, final CharSequence query) {
        if(term == null || query == null) {
            throw new IllegalArgumentException(" Strings must not be null");
        }

        final  String termLowerCase = term.toString().toLowerCase();
        final  String queryLowerCase = query.toString().toLowerCase();

        int score = 0;
        int termIndex = 0;
        int previousMatchingCharacterIndex = Integer.MIN_VALUE;

        for(int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
            final char queryChar = queryLowerCase.charAt(queryIndex);

            boolean termCharacterMatchFound = false;
            for(; termIndex < termLowerCase.length() && !termCharacterMatchFound; termIndex++) {
                final char termChar = termLowerCase.charAt(termIndex);

                if(queryChar == termChar) {
                    score++;

                    if(previousMatchingCharacterIndex + 1 == termIndex)
                        score += 2;

                    previousMatchingCharacterIndex = termIndex;
                    termCharacterMatchFound = true;
                }
            }
        }
        return score;
    }

    // Return Phone Code by country
    public static  String getPhoneCodesByCountry( String country) {

        if (isCountryName(country) && countryData.getPhoneCodeByCountry().containsKey(country)) {
            return countryData.getPhoneCodeByCountry().get(country);
        }
        else if (isNumeric(country) && CountryData.getCountryByNumericCode().containsKey(country)) {
            return countryData.getPhoneCodeByCountry().get(CountryData.getCountryByNumericCode().get(country));
        }
        else if ( isAlpha2(country) && CountryData.getNumericByAlpha2CountryCode().containsKey(country)) {
            if (CountryData.getCountryByNumericCode().containsKey(CountryData.getNumericByAlpha2CountryCode().get(country))) {
                var countryCode = CountryData.getCountryByNumericCode().get(CountryData.getNumericByAlpha2CountryCode().get(country));
                return countryData.getPhoneCodeByCountry().get(countryCode);
            }
        }
        else if ( isAlpha3(country) && CountryData.getNumericByAlpha3CountryCode().containsKey(country)) {
            if (CountryData.getCountryByNumericCode().containsKey(CountryData.getNumericByAlpha3CountryCode().get(country))) {
                var countryCode = CountryData.getCountryByNumericCode().get(CountryData.getNumericByAlpha3CountryCode().get(country));
                return countryData.getPhoneCodeByCountry().get(countryCode);
            }
        }
        return null;
    }

    // Converts from Name to requested format
    private static String convertFromName(String input, CountryCodeFormat countryCodeFormat){
        if (countryCodeFormat == CountryCodeFormat.Alpha2 && CountryData.getAlpha2CodeByCountry().containsKey(input)) {
            return CountryData.getAlpha2CodeByCountry().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Alpha3 && CountryData.getAlpha3CodeByCountry().containsKey(input)) {
            return CountryData.getAlpha3CodeByCountry().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Numeric && CountryData.getNumericCodeByCountry().containsKey(input)) {
            return CountryData.getNumericCodeByCountry().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Name) {
            return input;
        }
        return null;
    }

    // Converts from Alpha2 to requested format
    private static String convertFromAlpha2(String input, CountryCodeFormat countryCodeFormat) {
        if (countryCodeFormat == CountryCodeFormat.Numeric && CountryData.getNumericByAlpha2CountryCode().containsKey(input))  {
            return CountryData.getNumericByAlpha2CountryCode().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Alpha3 && CountryData.getAlpha3CodeByAlpha2Code().containsKey(input)) {
            return CountryData.getAlpha3CodeByAlpha2Code().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Alpha2) {
            return input;
        }
        else if (countryCodeFormat == CountryCodeFormat.Name && CountryData.getCountryByAlpha2Code().containsKey(input)) {
            return CountryData.getCountryByAlpha2Code().get(input);
        }
        return "";
    }

    // Converts from Alpha3 to requested format
    private static String ConvertFromAlpha3(String input, CountryCodeFormat countryCodeFormat) {
        if (countryCodeFormat == CountryCodeFormat.Alpha2 && CountryData.getAlpha2CodeByAlpha3Code().containsKey(input)) {
            return CountryData.getAlpha2CodeByAlpha3Code().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Numeric && CountryData.getNumericByAlpha3CountryCode().containsKey(input)) {
            return CountryData.getNumericByAlpha3CountryCode().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Alpha3) {
            return input;
        }
        else if (countryCodeFormat == CountryCodeFormat.Name && CountryData.getCountryByAlpha3Code().containsKey(input)) {
            return CountryData.getCountryByAlpha3Code().get(input);
        }
        return "";
    }

    // Converts from Numeric to requested format
    private static String ConvertFromNumeric(String input, CountryCodeFormat countryCodeFormat) {
        if (countryCodeFormat == CountryCodeFormat.Alpha2 && CountryData.getAlpha2CountryCodeByNumeric().containsKey(input)) {
            return CountryData.getAlpha2CountryCodeByNumeric().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Alpha3 && CountryData.getAlpha3CountryCodeByNumeric().containsKey(input)) {
            return CountryData.getAlpha3CountryCodeByNumeric().get(input);
        }
        else if (countryCodeFormat == CountryCodeFormat.Numeric) {
            return input;
        }
        else if (countryCodeFormat == CountryCodeFormat.Name && CountryData.getCountryByNumericCode().containsKey(input)) {
            return CountryData.getCountryByNumericCode().get(input);
        }
        return "";
    }

    private static boolean isCountryName( String input) {
        return CountryData.getAlpha2CodeByCountry().containsKey(input);
    }
    private static boolean  isAlpha2( String input) {
        return CountryData.getCountryByAlpha2Code().containsKey(input);
    }
    private static boolean  isAlpha3( String input) {
        return CountryData.getAlpha2CodeByAlpha3Code().containsKey(input);
    }
    private static boolean isNumeric( String input) {
        return CountryData.getAlpha2CountryCodeByNumeric().containsKey(input);
    }
}