package com.global.api.utils;

import com.global.api.entities.Address;

import java.util.*;

public class CountryUtils {
    private static Map<String, String> countryCodeMapByCountry;
    private static Map<String, String> countryMapByCountryCode;
    private static Map<String, String> countryCodeMapByNumericCode;
    private static Map<String, String> numericCodeMapByCountryCode;
    private static final int significantCountryMatch = 6;
    private static final int significantCodeMatch = 3;

    static {
        // build country code map
        countryCodeMapByCountry = new HashMap<>();
        countryCodeMapByCountry.put("Afghanistan", "AF");
        countryCodeMapByCountry.put("Åland Islands", "AX");
        countryCodeMapByCountry.put("Albania", "AL");
        countryCodeMapByCountry.put("Algeria", "DZ");
        countryCodeMapByCountry.put("American Samoa", "AS");
        countryCodeMapByCountry.put("Andorra", "AD");
        countryCodeMapByCountry.put("Angola", "AO");
        countryCodeMapByCountry.put("Anguilla", "AI");
        countryCodeMapByCountry.put("Antarctica", "AQ");
        countryCodeMapByCountry.put("Antigua and Barbuda", "AG");
        countryCodeMapByCountry.put("Argentina", "AR");
        countryCodeMapByCountry.put("Armenia", "AM");
        countryCodeMapByCountry.put("Aruba", "AW");
        countryCodeMapByCountry.put("Australia", "AU");
        countryCodeMapByCountry.put("Austria", "AT");
        countryCodeMapByCountry.put("Azerbaijan", "AZ");
        countryCodeMapByCountry.put("Bahamas", "BS");
        countryCodeMapByCountry.put("Bahrain", "BH");
        countryCodeMapByCountry.put("Bangladesh", "BD");
        countryCodeMapByCountry.put("Barbados", "BB");
        countryCodeMapByCountry.put("Belarus", "BY");
        countryCodeMapByCountry.put("Belgium", "BE");
        countryCodeMapByCountry.put("Belize", "BZ");
        countryCodeMapByCountry.put("Benin", "BJ");
        countryCodeMapByCountry.put("Bermuda", "BM");
        countryCodeMapByCountry.put("Bhutan", "BT");
        countryCodeMapByCountry.put("Bolivia (Plurinational State of)", "BO");
        countryCodeMapByCountry.put("Bonaire, Sint Eustatius and Saba", "BQ");
        countryCodeMapByCountry.put("Bosnia and Herzegovina", "BA");
        countryCodeMapByCountry.put("Botswana", "BW");
        countryCodeMapByCountry.put("Bouvet Island", "BV");
        countryCodeMapByCountry.put("Brazil", "BR");
        countryCodeMapByCountry.put("British Indian Ocean Territory", "IO");
        countryCodeMapByCountry.put("Brunei Darussalam", "BN");
        countryCodeMapByCountry.put("Bulgaria", "BG");
        countryCodeMapByCountry.put("Burkina Faso", "BF");
        countryCodeMapByCountry.put("Burundi", "BI");
        countryCodeMapByCountry.put("Cambodia", "KH");
        countryCodeMapByCountry.put("Cameroon", "CM");
        countryCodeMapByCountry.put("Canada", "CA");
        countryCodeMapByCountry.put("Cabo Verde", "CV");
        countryCodeMapByCountry.put("Cayman Islands", "KY");
        countryCodeMapByCountry.put("Central African Republic", "CF");
        countryCodeMapByCountry.put("Chad", "TD");
        countryCodeMapByCountry.put("Chile", "CL");
        countryCodeMapByCountry.put("China", "CN");
        countryCodeMapByCountry.put("Christmas Island", "CX");
        countryCodeMapByCountry.put("Cocos (Keeling) Islands", "CC");
        countryCodeMapByCountry.put("Colombia", "CO");
        countryCodeMapByCountry.put("Comoros", "KM");
        countryCodeMapByCountry.put("Congo", "CG");
        countryCodeMapByCountry.put("Congo (Democratic Republic of the)", "CD");
        countryCodeMapByCountry.put("Cook Islands", "CK");
        countryCodeMapByCountry.put("Costa Rica", "CR");
        countryCodeMapByCountry.put("Côte d'Ivoire", "CI");
        countryCodeMapByCountry.put("Croatia", "HR");
        countryCodeMapByCountry.put("Cuba", "CU");
        countryCodeMapByCountry.put("Curaçao", "CW");
        countryCodeMapByCountry.put("Cyprus", "CY");
        countryCodeMapByCountry.put("Czechia", "CZ");
        countryCodeMapByCountry.put("Denmark", "DK");
        countryCodeMapByCountry.put("Djibouti", "DJ");
        countryCodeMapByCountry.put("Dominica", "DM");
        countryCodeMapByCountry.put("Dominican Republic", "DO");
        countryCodeMapByCountry.put("Ecuador", "EC");
        countryCodeMapByCountry.put("Egypt", "EG");
        countryCodeMapByCountry.put("El Salvador", "SV");
        countryCodeMapByCountry.put("Equatorial Guinea", "GQ");
        countryCodeMapByCountry.put("Eritrea", "ER");
        countryCodeMapByCountry.put("Estonia", "EE");
        countryCodeMapByCountry.put("Ethiopia", "ET");
        countryCodeMapByCountry.put("Falkland Islands (Malvinas)", "FK");
        countryCodeMapByCountry.put("Faroe Islands", "FO");
        countryCodeMapByCountry.put("Fiji", "FJ");
        countryCodeMapByCountry.put("Finland", "FI");
        countryCodeMapByCountry.put("France", "FR");
        countryCodeMapByCountry.put("French Guiana", "GF");
        countryCodeMapByCountry.put("French Polynesia", "PF");
        countryCodeMapByCountry.put("French Southern Territories", "TF");
        countryCodeMapByCountry.put("Gabon", "GA");
        countryCodeMapByCountry.put("Gambia", "GM");
        countryCodeMapByCountry.put("Georgia", "GE");
        countryCodeMapByCountry.put("Germany", "DE");
        countryCodeMapByCountry.put("Ghana", "GH");
        countryCodeMapByCountry.put("Gibraltar", "GI");
        countryCodeMapByCountry.put("Greece", "GR");
        countryCodeMapByCountry.put("Greenland", "GL");
        countryCodeMapByCountry.put("Grenada", "GD");
        countryCodeMapByCountry.put("Guadeloupe", "GP");
        countryCodeMapByCountry.put("Guam", "GU");
        countryCodeMapByCountry.put("Guatemala", "GT");
        countryCodeMapByCountry.put("Guernsey", "GG");
        countryCodeMapByCountry.put("Guinea", "GN");
        countryCodeMapByCountry.put("Guinea-Bissau", "GW");
        countryCodeMapByCountry.put("Guyana", "GY");
        countryCodeMapByCountry.put("Haiti", "HT");
        countryCodeMapByCountry.put("Heard Island and McDonald Islands", "HM");
        countryCodeMapByCountry.put("Holy See", "VA");
        countryCodeMapByCountry.put("Honduras", "HN");
        countryCodeMapByCountry.put("Hong Kong", "HK");
        countryCodeMapByCountry.put("Hungary", "HU");
        countryCodeMapByCountry.put("Iceland", "IS");
        countryCodeMapByCountry.put("India", "IN");
        countryCodeMapByCountry.put("Indonesia", "ID");
        countryCodeMapByCountry.put("Iran (Islamic Republic of)", "IR");
        countryCodeMapByCountry.put("Iraq", "IQ");
        countryCodeMapByCountry.put("Ireland", "IE");
        countryCodeMapByCountry.put("Isle of Man", "IM");
        countryCodeMapByCountry.put("Israel", "IL");
        countryCodeMapByCountry.put("Italy", "IT");
        countryCodeMapByCountry.put("Jamaica", "JM");
        countryCodeMapByCountry.put("Japan", "JP");
        countryCodeMapByCountry.put("Jersey", "JE");
        countryCodeMapByCountry.put("Jordan", "JO");
        countryCodeMapByCountry.put("Kazakhstan", "KZ");
        countryCodeMapByCountry.put("Kenya", "KE");
        countryCodeMapByCountry.put("Kiribati", "KI");
        countryCodeMapByCountry.put("Korea (Democratic People's Republic of)", "KP");
        countryCodeMapByCountry.put("Korea (Republic of)", "KR");
        countryCodeMapByCountry.put("Kuwait", "KW");
        countryCodeMapByCountry.put("Kyrgyzstan", "KG");
        countryCodeMapByCountry.put("Lao People's Democratic Republic", "LA");
        countryCodeMapByCountry.put("Latvia", "LV");
        countryCodeMapByCountry.put("Lebanon", "LB");
        countryCodeMapByCountry.put("Lesotho", "LS");
        countryCodeMapByCountry.put("Liberia", "LR");
        countryCodeMapByCountry.put("Libya", "LY");
        countryCodeMapByCountry.put("Liechtenstein", "LI");
        countryCodeMapByCountry.put("Lithuania", "LT");
        countryCodeMapByCountry.put("Luxembourg", "LU");
        countryCodeMapByCountry.put("Macao", "MO");
        countryCodeMapByCountry.put("North Macedonia", "MK");
        countryCodeMapByCountry.put("Madagascar", "MG");
        countryCodeMapByCountry.put("Malawi", "MW");
        countryCodeMapByCountry.put("Malaysia", "MY");
        countryCodeMapByCountry.put("Maldives", "MV");
        countryCodeMapByCountry.put("Mali", "ML");
        countryCodeMapByCountry.put("Malta", "MT");
        countryCodeMapByCountry.put("Marshall Islands", "MH");
        countryCodeMapByCountry.put("Martinique", "MQ");
        countryCodeMapByCountry.put("Mauritania", "MR");
        countryCodeMapByCountry.put("Mauritius", "MU");
        countryCodeMapByCountry.put("Mayotte", "YT");
        countryCodeMapByCountry.put("Mexico", "MX");
        countryCodeMapByCountry.put("Micronesia (Federated States of)", "FM");
        countryCodeMapByCountry.put("Moldova (Republic of)", "MD");
        countryCodeMapByCountry.put("Monaco", "MC");
        countryCodeMapByCountry.put("Mongolia", "MN");
        countryCodeMapByCountry.put("Montenegro", "ME");
        countryCodeMapByCountry.put("Montserrat", "MS");
        countryCodeMapByCountry.put("Morocco", "MA");
        countryCodeMapByCountry.put("Mozambique", "MZ");
        countryCodeMapByCountry.put("Myanmar", "MM");
        countryCodeMapByCountry.put("Namibia", "NA");
        countryCodeMapByCountry.put("Nauru", "NR");
        countryCodeMapByCountry.put("Nepal", "NP");
        countryCodeMapByCountry.put("Netherlands", "NL");
        countryCodeMapByCountry.put("New Caledonia", "NC");
        countryCodeMapByCountry.put("New Zealand", "NZ");
        countryCodeMapByCountry.put("Nicaragua", "NI");
        countryCodeMapByCountry.put("Niger", "NE");
        countryCodeMapByCountry.put("Nigeria", "NG");
        countryCodeMapByCountry.put("Niue", "NU");
        countryCodeMapByCountry.put("Norfolk Island", "NF");
        countryCodeMapByCountry.put("Northern Mariana Islands", "MP");
        countryCodeMapByCountry.put("Norway", "NO");
        countryCodeMapByCountry.put("Oman", "OM");
        countryCodeMapByCountry.put("Pakistan", "PK");
        countryCodeMapByCountry.put("Palau", "PW");
        countryCodeMapByCountry.put("Palestine, State of", "PS");
        countryCodeMapByCountry.put("Panama", "PA");
        countryCodeMapByCountry.put("Papua New Guinea", "PG");
        countryCodeMapByCountry.put("Paraguay", "PY");
        countryCodeMapByCountry.put("Peru", "PE");
        countryCodeMapByCountry.put("Philippines", "PH");
        countryCodeMapByCountry.put("Pitcairn", "PN");
        countryCodeMapByCountry.put("Poland", "PL");
        countryCodeMapByCountry.put("Portugal", "PT");
        countryCodeMapByCountry.put("Puerto Rico", "PR");
        countryCodeMapByCountry.put("Qatar", "QA");
        countryCodeMapByCountry.put("Réunion", "RE");
        countryCodeMapByCountry.put("Romania", "RO");
        countryCodeMapByCountry.put("Russian Federation", "RU");
        countryCodeMapByCountry.put("Rwanda", "RW");
        countryCodeMapByCountry.put("Saint Barthélemy", "BL");
        countryCodeMapByCountry.put("Saint Helena, Ascension and Tristan da Cunha", "SH");
        countryCodeMapByCountry.put("Saint Kitts and Nevis", "KN");
        countryCodeMapByCountry.put("Saint Lucia", "LC");
        countryCodeMapByCountry.put("Saint Martin (French part)", "MF");
        countryCodeMapByCountry.put("Saint Pierre and Miquelon", "PM");
        countryCodeMapByCountry.put("Saint Vincent and the Grenadines", "VC");
        countryCodeMapByCountry.put("Samoa", "WS");
        countryCodeMapByCountry.put("San Marino", "SM");
        countryCodeMapByCountry.put("Sao Tome and Principe", "ST");
        countryCodeMapByCountry.put("Saudi Arabia", "SA");
        countryCodeMapByCountry.put("Senegal", "SN");
        countryCodeMapByCountry.put("Serbia", "RS");
        countryCodeMapByCountry.put("Seychelles", "SC");
        countryCodeMapByCountry.put("Sierra Leone", "SL");
        countryCodeMapByCountry.put("Singapore", "SG");
        countryCodeMapByCountry.put("Sint Maarten (Dutch part)", "SX");
        countryCodeMapByCountry.put("Slovakia", "SK");
        countryCodeMapByCountry.put("Slovenia", "SI");
        countryCodeMapByCountry.put("Solomon Islands", "SB");
        countryCodeMapByCountry.put("Somalia", "SO");
        countryCodeMapByCountry.put("South Africa", "ZA");
        countryCodeMapByCountry.put("South Georgia and the South Sandwich Islands", "GS");
        countryCodeMapByCountry.put("South Sudan", "SS");
        countryCodeMapByCountry.put("Spain", "ES");
        countryCodeMapByCountry.put("Sri Lanka", "LK");
        countryCodeMapByCountry.put("Sudan", "SD");
        countryCodeMapByCountry.put("Suriname", "SR");
        countryCodeMapByCountry.put("Svalbard and Jan Mayen", "SJ");
        countryCodeMapByCountry.put("Eswatini", "SZ");
        countryCodeMapByCountry.put("Sweden", "SE");
        countryCodeMapByCountry.put("Switzerland", "CH");
        countryCodeMapByCountry.put("Syrian Arab Republic", "SY");
        countryCodeMapByCountry.put("Taiwan, Province of China", "TW");
        countryCodeMapByCountry.put("Tajikistan", "TJ");
        countryCodeMapByCountry.put("Tanzania, United Republic of", "TZ");
        countryCodeMapByCountry.put("Thailand", "TH");
        countryCodeMapByCountry.put("Timor-Leste", "TL");
        countryCodeMapByCountry.put("Togo", "TG");
        countryCodeMapByCountry.put("Tokelau", "TK");
        countryCodeMapByCountry.put("Tonga", "TO");
        countryCodeMapByCountry.put("Trinidad and Tobago", "TT");
        countryCodeMapByCountry.put("Tunisia", "TN");
        countryCodeMapByCountry.put("Turkey", "TR");
        countryCodeMapByCountry.put("Turkmenistan", "TM");
        countryCodeMapByCountry.put("Turks and Caicos Islands", "TC");
        countryCodeMapByCountry.put("Tuvalu", "TV");
        countryCodeMapByCountry.put("Uganda", "UG");
        countryCodeMapByCountry.put("Ukraine", "UA");
        countryCodeMapByCountry.put("United Arab Emirates", "AE");
        countryCodeMapByCountry.put("United Kingdom of Great Britain and Northern Ireland", "GB");
        countryCodeMapByCountry.put("United States of America", "US");
        countryCodeMapByCountry.put("United States Minor Outlying Islands", "UM");
        countryCodeMapByCountry.put("Uruguay", "UY");
        countryCodeMapByCountry.put("Uzbekistan", "UZ");
        countryCodeMapByCountry.put("Vanuatu", "VU");
        countryCodeMapByCountry.put("Venezuela (Bolivarian Republic of)", "VE");
        countryCodeMapByCountry.put("Viet Nam", "VN");
        countryCodeMapByCountry.put("Virgin Islands (British)", "VG");
        countryCodeMapByCountry.put("Virgin Islands (U.S.)", "VI");
        countryCodeMapByCountry.put("Wallis and Futuna", "WF");
        countryCodeMapByCountry.put("Western Sahara", "EH");
        countryCodeMapByCountry.put("Yemen", "YE");
        countryCodeMapByCountry.put("Zambia", "ZM");
        countryCodeMapByCountry.put("Zimbabwe", "ZW");

        // build the inverse
        countryMapByCountryCode = new HashMap<>();
        for(String country : countryCodeMapByCountry.keySet()) {
            countryMapByCountryCode.put(countryCodeMapByCountry.get(country), country);
        }

        countryCodeMapByNumericCode = new HashMap<>();
        countryCodeMapByNumericCode.put("004", "AF");
        countryCodeMapByNumericCode.put("008", "AL");
        countryCodeMapByNumericCode.put("010", "AQ");
        countryCodeMapByNumericCode.put("012", "DZ");
        countryCodeMapByNumericCode.put("016", "AS");
        countryCodeMapByNumericCode.put("020", "AD");
        countryCodeMapByNumericCode.put("024", "AO");
        countryCodeMapByNumericCode.put("028", "AG");
        countryCodeMapByNumericCode.put("031", "AZ");
        countryCodeMapByNumericCode.put("032", "AR");
        countryCodeMapByNumericCode.put("036", "AU");
        countryCodeMapByNumericCode.put("040", "AT");
        countryCodeMapByNumericCode.put("044", "BS");
        countryCodeMapByNumericCode.put("048", "BH");
        countryCodeMapByNumericCode.put("050", "BD");
        countryCodeMapByNumericCode.put("051", "AM");
        countryCodeMapByNumericCode.put("052", "BB");
        countryCodeMapByNumericCode.put("056", "BE");
        countryCodeMapByNumericCode.put("060", "BM");
        countryCodeMapByNumericCode.put("064", "BT");
        countryCodeMapByNumericCode.put("068", "BO");
        countryCodeMapByNumericCode.put("070", "BA");
        countryCodeMapByNumericCode.put("072", "BW");
        countryCodeMapByNumericCode.put("074", "BV");
        countryCodeMapByNumericCode.put("076", "BR");
        countryCodeMapByNumericCode.put("084", "BZ");
        countryCodeMapByNumericCode.put("086", "IO");
        countryCodeMapByNumericCode.put("090", "SB");
        countryCodeMapByNumericCode.put("092", "VG");
        countryCodeMapByNumericCode.put("096", "BN");
        countryCodeMapByNumericCode.put("100", "BG");
        countryCodeMapByNumericCode.put("104", "MM");
        countryCodeMapByNumericCode.put("108", "BI");
        countryCodeMapByNumericCode.put("112", "BY");
        countryCodeMapByNumericCode.put("116", "KH");
        countryCodeMapByNumericCode.put("120", "CM");
        countryCodeMapByNumericCode.put("124", "CA");
        countryCodeMapByNumericCode.put("132", "CV");
        countryCodeMapByNumericCode.put("136", "KY");
        countryCodeMapByNumericCode.put("140", "CF");
        countryCodeMapByNumericCode.put("144", "LK");
        countryCodeMapByNumericCode.put("148", "TD");
        countryCodeMapByNumericCode.put("152", "CL");
        countryCodeMapByNumericCode.put("156", "CN");
        countryCodeMapByNumericCode.put("158", "TW");
        countryCodeMapByNumericCode.put("162", "CX");
        countryCodeMapByNumericCode.put("166", "CC");
        countryCodeMapByNumericCode.put("170", "CO");
        countryCodeMapByNumericCode.put("174", "KM");
        countryCodeMapByNumericCode.put("175", "YT");
        countryCodeMapByNumericCode.put("178", "CG");
        countryCodeMapByNumericCode.put("180", "CD");
        countryCodeMapByNumericCode.put("184", "CK");
        countryCodeMapByNumericCode.put("188", "CR");
        countryCodeMapByNumericCode.put("191", "HR");
        countryCodeMapByNumericCode.put("192", "CU");
        countryCodeMapByNumericCode.put("196", "CY");
        countryCodeMapByNumericCode.put("203", "CZ");
        countryCodeMapByNumericCode.put("204", "BJ");
        countryCodeMapByNumericCode.put("208", "DK");
        countryCodeMapByNumericCode.put("212", "DM");
        countryCodeMapByNumericCode.put("214", "DO");
        countryCodeMapByNumericCode.put("218", "EC");
        countryCodeMapByNumericCode.put("222", "SV");
        countryCodeMapByNumericCode.put("226", "GQ");
        countryCodeMapByNumericCode.put("231", "ET");
        countryCodeMapByNumericCode.put("232", "ER");
        countryCodeMapByNumericCode.put("233", "EE");
        countryCodeMapByNumericCode.put("234", "FO");
        countryCodeMapByNumericCode.put("238", "FK");
        countryCodeMapByNumericCode.put("239", "GS");
        countryCodeMapByNumericCode.put("242", "FJ");
        countryCodeMapByNumericCode.put("246", "FI");
        countryCodeMapByNumericCode.put("248", "AX");
        countryCodeMapByNumericCode.put("250", "FR");
        countryCodeMapByNumericCode.put("254", "GF");
        countryCodeMapByNumericCode.put("258", "PF");
        countryCodeMapByNumericCode.put("260", "TF");
        countryCodeMapByNumericCode.put("262", "DJ");
        countryCodeMapByNumericCode.put("266", "GA");
        countryCodeMapByNumericCode.put("268", "GE");
        countryCodeMapByNumericCode.put("270", "GM");
        countryCodeMapByNumericCode.put("275", "PS");
        countryCodeMapByNumericCode.put("276", "DE");
        countryCodeMapByNumericCode.put("288", "GH");
        countryCodeMapByNumericCode.put("292", "GI");
        countryCodeMapByNumericCode.put("296", "KI");
        countryCodeMapByNumericCode.put("300", "GR");
        countryCodeMapByNumericCode.put("304", "GL");
        countryCodeMapByNumericCode.put("308", "GD");
        countryCodeMapByNumericCode.put("312", "GP");
        countryCodeMapByNumericCode.put("316", "GU");
        countryCodeMapByNumericCode.put("320", "GT");
        countryCodeMapByNumericCode.put("324", "GN");
        countryCodeMapByNumericCode.put("328", "GY");
        countryCodeMapByNumericCode.put("332", "HT");
        countryCodeMapByNumericCode.put("334", "HM");
        countryCodeMapByNumericCode.put("336", "VA");
        countryCodeMapByNumericCode.put("340", "HN");
        countryCodeMapByNumericCode.put("344", "HK");
        countryCodeMapByNumericCode.put("348", "HU");
        countryCodeMapByNumericCode.put("352", "IS");
        countryCodeMapByNumericCode.put("356", "IN");
        countryCodeMapByNumericCode.put("360", "ID");
        countryCodeMapByNumericCode.put("364", "IR");
        countryCodeMapByNumericCode.put("368", "IQ");
        countryCodeMapByNumericCode.put("372", "IE");
        countryCodeMapByNumericCode.put("376", "IL");
        countryCodeMapByNumericCode.put("380", "IT");
        countryCodeMapByNumericCode.put("384", "CI");
        countryCodeMapByNumericCode.put("388", "JM");
        countryCodeMapByNumericCode.put("392", "JP");
        countryCodeMapByNumericCode.put("398", "KZ");
        countryCodeMapByNumericCode.put("400", "JO");
        countryCodeMapByNumericCode.put("404", "KE");
        countryCodeMapByNumericCode.put("408", "KP");
        countryCodeMapByNumericCode.put("410", "KR");
        countryCodeMapByNumericCode.put("414", "KW");
        countryCodeMapByNumericCode.put("417", "KG");
        countryCodeMapByNumericCode.put("418", "LA");
        countryCodeMapByNumericCode.put("422", "LB");
        countryCodeMapByNumericCode.put("426", "LS");
        countryCodeMapByNumericCode.put("428", "LV");
        countryCodeMapByNumericCode.put("430", "LR");
        countryCodeMapByNumericCode.put("434", "LY");
        countryCodeMapByNumericCode.put("438", "LI");
        countryCodeMapByNumericCode.put("440", "LT");
        countryCodeMapByNumericCode.put("442", "LU");
        countryCodeMapByNumericCode.put("446", "MO");
        countryCodeMapByNumericCode.put("450", "MG");
        countryCodeMapByNumericCode.put("454", "MW");
        countryCodeMapByNumericCode.put("458", "MY");
        countryCodeMapByNumericCode.put("462", "MV");
        countryCodeMapByNumericCode.put("466", "ML");
        countryCodeMapByNumericCode.put("470", "MT");
        countryCodeMapByNumericCode.put("474", "MQ");
        countryCodeMapByNumericCode.put("478", "MR");
        countryCodeMapByNumericCode.put("480", "MU");
        countryCodeMapByNumericCode.put("484", "MX");
        countryCodeMapByNumericCode.put("492", "MC");
        countryCodeMapByNumericCode.put("496", "MN");
        countryCodeMapByNumericCode.put("498", "MD");
        countryCodeMapByNumericCode.put("499", "ME");
        countryCodeMapByNumericCode.put("500", "MS");
        countryCodeMapByNumericCode.put("504", "MA");
        countryCodeMapByNumericCode.put("508", "MZ");
        countryCodeMapByNumericCode.put("512", "OM");
        countryCodeMapByNumericCode.put("516", "NA");
        countryCodeMapByNumericCode.put("520", "NR");
        countryCodeMapByNumericCode.put("524", "NP");
        countryCodeMapByNumericCode.put("528", "NL");
        countryCodeMapByNumericCode.put("531", "CW");
        countryCodeMapByNumericCode.put("533", "AW");
        countryCodeMapByNumericCode.put("534", "SX");
        countryCodeMapByNumericCode.put("535", "BQ");
        countryCodeMapByNumericCode.put("540", "NC");
        countryCodeMapByNumericCode.put("548", "VU");
        countryCodeMapByNumericCode.put("554", "NZ");
        countryCodeMapByNumericCode.put("558", "NI");
        countryCodeMapByNumericCode.put("562", "NE");
        countryCodeMapByNumericCode.put("566", "NG");
        countryCodeMapByNumericCode.put("570", "NU");
        countryCodeMapByNumericCode.put("574", "NF");
        countryCodeMapByNumericCode.put("578", "NO");
        countryCodeMapByNumericCode.put("580", "MP");
        countryCodeMapByNumericCode.put("581", "UM");
        countryCodeMapByNumericCode.put("583", "FM");
        countryCodeMapByNumericCode.put("584", "MH");
        countryCodeMapByNumericCode.put("585", "PW");
        countryCodeMapByNumericCode.put("586", "PK");
        countryCodeMapByNumericCode.put("591", "PA");
        countryCodeMapByNumericCode.put("598", "PG");
        countryCodeMapByNumericCode.put("600", "PY");
        countryCodeMapByNumericCode.put("604", "PE");
        countryCodeMapByNumericCode.put("608", "PH");
        countryCodeMapByNumericCode.put("612", "PN");
        countryCodeMapByNumericCode.put("616", "PL");
        countryCodeMapByNumericCode.put("620", "PT");
        countryCodeMapByNumericCode.put("624", "GW");
        countryCodeMapByNumericCode.put("626", "TL");
        countryCodeMapByNumericCode.put("630", "PR");
        countryCodeMapByNumericCode.put("634", "QA");
        countryCodeMapByNumericCode.put("638", "RE");
        countryCodeMapByNumericCode.put("642", "RO");
        countryCodeMapByNumericCode.put("643", "RU");
        countryCodeMapByNumericCode.put("646", "RW");
        countryCodeMapByNumericCode.put("652", "BL");
        countryCodeMapByNumericCode.put("654", "SH");
        countryCodeMapByNumericCode.put("659", "KN");
        countryCodeMapByNumericCode.put("660", "AI");
        countryCodeMapByNumericCode.put("662", "LC");
        countryCodeMapByNumericCode.put("663", "MF");
        countryCodeMapByNumericCode.put("666", "PM");
        countryCodeMapByNumericCode.put("670", "VC");
        countryCodeMapByNumericCode.put("674", "SM");
        countryCodeMapByNumericCode.put("678", "ST");
        countryCodeMapByNumericCode.put("682", "SA");
        countryCodeMapByNumericCode.put("686", "SN");
        countryCodeMapByNumericCode.put("688", "RS");
        countryCodeMapByNumericCode.put("690", "SC");
        countryCodeMapByNumericCode.put("694", "SL");
        countryCodeMapByNumericCode.put("702", "SG");
        countryCodeMapByNumericCode.put("703", "SK");
        countryCodeMapByNumericCode.put("704", "VN");
        countryCodeMapByNumericCode.put("705", "SI");
        countryCodeMapByNumericCode.put("706", "SO");
        countryCodeMapByNumericCode.put("710", "ZA");
        countryCodeMapByNumericCode.put("716", "ZW");
        countryCodeMapByNumericCode.put("724", "ES");
        countryCodeMapByNumericCode.put("728", "SS");
        countryCodeMapByNumericCode.put("729", "SD");
        countryCodeMapByNumericCode.put("732", "EH");
        countryCodeMapByNumericCode.put("740", "SR");
        countryCodeMapByNumericCode.put("744", "SJ");
        countryCodeMapByNumericCode.put("748", "SZ");
        countryCodeMapByNumericCode.put("752", "SE");
        countryCodeMapByNumericCode.put("756", "CH");
        countryCodeMapByNumericCode.put("760", "SY");
        countryCodeMapByNumericCode.put("762", "TJ");
        countryCodeMapByNumericCode.put("764", "TH");
        countryCodeMapByNumericCode.put("768", "TG");
        countryCodeMapByNumericCode.put("772", "TK");
        countryCodeMapByNumericCode.put("776", "TO");
        countryCodeMapByNumericCode.put("780", "TT");
        countryCodeMapByNumericCode.put("784", "AE");
        countryCodeMapByNumericCode.put("788", "TN");
        countryCodeMapByNumericCode.put("792", "TR");
        countryCodeMapByNumericCode.put("795", "TM");
        countryCodeMapByNumericCode.put("796", "TC");
        countryCodeMapByNumericCode.put("798", "TV");
        countryCodeMapByNumericCode.put("800", "UG");
        countryCodeMapByNumericCode.put("804", "UA");
        countryCodeMapByNumericCode.put("807", "MK");
        countryCodeMapByNumericCode.put("818", "EG");
        countryCodeMapByNumericCode.put("826", "GB");
        countryCodeMapByNumericCode.put("831", "GG");
        countryCodeMapByNumericCode.put("832", "JE");
        countryCodeMapByNumericCode.put("833", "IM");
        countryCodeMapByNumericCode.put("834", "TZ");
        countryCodeMapByNumericCode.put("840", "US");
        countryCodeMapByNumericCode.put("850", "VI");
        countryCodeMapByNumericCode.put("854", "BF");
        countryCodeMapByNumericCode.put("858", "UY");
        countryCodeMapByNumericCode.put("860", "UZ");
        countryCodeMapByNumericCode.put("862", "VE");
        countryCodeMapByNumericCode.put("876", "WF");
        countryCodeMapByNumericCode.put("882", "WS");
        countryCodeMapByNumericCode.put("887", "YE");
        countryCodeMapByNumericCode.put("894", "ZM");

        // build the inverse
        numericCodeMapByCountryCode = new HashMap<>();
        for(String numericCode : countryCodeMapByNumericCode.keySet()) {
            numericCodeMapByCountryCode.put(countryCodeMapByNumericCode.get(numericCode), numericCode);
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
        if(countryMapByCountryCode.containsKey(countryCode))
            return countryMapByCountryCode.get(countryCode);
        else {
            if(countryCode.length() > 3)
                return null;

            return fuzzyMatch(countryMapByCountryCode, countryCode, significantCodeMatch);
        }
    }

    public static String getCountryCodeByCountry(String country) {
        if(country == null)
            return null;

        // These can be tricky... first check for direct match
        if(countryCodeMapByCountry.containsKey(country)) {
            return countryCodeMapByCountry.get(country);
        }
        else {
            // check the inverse, in case we have a countryCode in the country field
            if (countryMapByCountryCode.containsKey(country)) {
                return country;
            }
            else {
                // check for codes in case we have a numericCode in the country field
                if (countryCodeMapByNumericCode.containsKey(country)) {
                    return countryCodeMapByNumericCode.get(country);
                }
                else {
                    // it's not a country match or a countryCode match so let's get fuzzy
                    String fuzzyCountryMatch = fuzzyMatch(countryCodeMapByCountry, country, significantCountryMatch);
                    if (fuzzyCountryMatch != null)
                        return fuzzyCountryMatch;
                    else {
                        // assume if it's > 3 it's not a code and do not do fuzzy code matching
                        if (country.length() > 3)
                            return null;

                        // 3 or less, let's fuzzy match
                        String fuzzyCodeMatch = fuzzyMatch(countryMapByCountryCode, country, significantCodeMatch);
                        if (fuzzyCodeMatch != null)
                            return countryCodeMapByCountry.get(fuzzyCodeMatch);
                        return null;
                    }
                }
            }
        }
    }

    private static String fuzzyMatch(Map<String, String> dict, String query, int significantMatch) {
        String rvalue = null;
        Map<String, String> matches = new HashMap<>();

        // now we can loop
        int highScore = -1;
        for(String key : dict.keySet()) {
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

    public static String getNumericCodeByCountry(String country) {
        if (countryCodeMapByNumericCode.containsKey(country)) {
            return country;
        }
        else {
            String countryCode = getCountryCodeByCountry(country);
            if (countryCode != null && numericCodeMapByCountryCode.containsKey(countryCode)) {
                return numericCodeMapByCountryCode.get(countryCode);
            }
            return null;
        }
    }

}