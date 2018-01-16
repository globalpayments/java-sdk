package com.global.api.utils;

import com.global.api.entities.Address;

import java.util.*;

public class CountryUtils {
    private static Map<String, String> countryCodeMapByCountry;
    private static Map<String, String> countryMapByCode;
    private static final int significantCountryMatch = 6;
    private static final int significantCodeMatch = 3;

    static {
        // build country code map
        countryCodeMapByCountry = new HashMap<String, String>();
        for(String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            countryCodeMapByCountry.put(l.getDisplayCountry(), iso);
        }

        // build the inverse
        countryMapByCode = new HashMap<String, String>();
        for(String country : countryCodeMapByCountry.keySet()) {
            countryMapByCode.put(countryCodeMapByCountry.get(country), country);
        }
    }

    public static boolean isCountry(Address address, String countryCode) {
        if(address.getCountryCode() != null)
            return address.getCountryCode().equals(countryCode);
        else if(address.getCountry() != null) {
            String code = getCountryCodeByCountry(address.getCountry());
            if(code != null)
                return code.equals(countryCode);
            return false;
        }
        return false;
    }

    public static String getCountryByCode(String countryCode) {
        if(countryCode == null)
            return null;

        // These should be ISO so just check if it's there and return
        if(countryMapByCode.containsKey(countryCode))
            return countryMapByCode.get(countryCode);
        else {
            if(countryCode.length() > 3)
                return null;

            return fuzzyMatch(countryMapByCode, countryCode, significantCodeMatch);
        }
    }

    public static String getCountryCodeByCountry(String country) {
        if(country == null)
            return null;

        // These can be tricky... first check for direct match
        if(countryCodeMapByCountry.containsKey(country))
            return countryCodeMapByCountry.get(country);
        else {
            // check the inverse, in case we have a countryCode in the country field
            if(countryMapByCode.containsKey(country))
                return country;
            else {
                // it's not a country match or a countryCode match so let's get fuzzy
                String fuzzyCountryMatch = fuzzyMatch(countryCodeMapByCountry, country, significantCountryMatch);
                if(fuzzyCountryMatch != null)
                    return fuzzyCountryMatch;
                else {
                    // assume if it's > 3 it's not a code and do not do fuzzy code matching
                    if(country.length() > 3)
                        return null;

                    // 3 or less, let's fuzzy match
                    String fuzzyCodeMatch = fuzzyMatch(countryMapByCode, country, significantCodeMatch);
                    if(fuzzyCodeMatch != null)
                        return countryCodeMapByCountry.get(fuzzyCodeMatch);
                    return null;
                }
            }
        }
    }

    private static String fuzzyMatch(Map<String, String> dict, String query, int significantMatch) {
        String rvalue = null;
        Map<String, String> matches = new HashMap<String, String>();

        // now we can loop
        int highScore = -1;
        for(String key : dict.keySet()) {
            int score = fuzzyScore(key, query);
            if(score > significantMatch && score > highScore) {
                matches = new HashMap<String, String>();

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

    private static Integer fuzzyScore(final CharSequence term, final CharSequence query) {
        if(term == null || query == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        final String termLowerCase = term.toString().toLowerCase();
        final String queryLowerCase = query.toString().toLowerCase();

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
}