package com.global.api.tests;

import com.global.api.utils.StringParser;

public class ParseNTSDataCollectUserData {

    static void parseDataCollect(String data){
        StringParser sp = new StringParser(data);
        System.out.println("PUMP NUMBER: "+ sp.readString(2));
        System.out.println("WORK STATION ID: "+ sp.readString(2));
        System.out.println("ORIGINAL SALE DATE: "+ sp.readString(6));
        System.out.println("INVOICE: "+ sp.readString(6));
        System.out.println("SERVICE CODE: "+ sp.readString(1));
        System.out.println("SECURITY DATA ID: "+ sp.readString(1));
        System.out.println("ZIP: "+ sp.readString(9));
        System.out.println("CVN: "+ sp.readString(4));


        System.out.println("Product 1: "+ sp.readString(23));
        System.out.println("Product 2: "+ sp.readString(23));
        System.out.println("Product 3: "+ sp.readString(23));
        System.out.println("Product 4: "+ sp.readString(23));
        System.out.println("Product 5: "+ sp.readString(23));


        System.out.println("SALES TAX: "+ sp.readString(7));
        System.out.println("PDL FUEL DISCOUNT: "+ sp.readString(5));
        System.out.println("Filler: "+ sp.readString(12));

    }
    static void parseTag09(String data) {
        StringParser sp = new StringParser(data);
        System.out.println("Product 1: "+ sp.readString(23));
        System.out.println("Product 2: "+ sp.readString(23));
        System.out.println("Product 3: "+ sp.readString(23));
        System.out.println("Product 4: "+ sp.readString(23));
        System.out.println("Product 5: "+ sp.readString(23));


        System.out.println("SALES TAX: "+ sp.readString(7));
        System.out.println("PDL FUEL DISCOUNT: "+ sp.readString(5));
        System.out.println("Filler: "+ sp.readString(12));
    }

    static void parseVoygerData(String data){
        StringParser sp = new StringParser(data);
        System.out.println("ODOMETER: "+ sp.readString(7));
        System.out.println("ID NUMBER : "+ sp.readString(6));
        System.out.println("SERVICE TYPE: "+ sp.readString(1));

        System.out.println("Fuel 1: "+ sp.readString(12));
        System.out.println("Fuel 2: "+ sp.readString(12));
        System.out.println("Product 1: "+ sp.readString(9));
        System.out.println("Product 2: "+ sp.readString(9));
        System.out.println("Product 3: "+ sp.readString(9));
        System.out.println("Product 4: "+ sp.readString(9));


        System.out.println("TAX: "+ sp.readRemaining());

    }
    public static void main(String[] args) {
        // Credit Data
//        parseData("01011111211221441090210000000000990000000304800000383706910740000100000001074069107400002000000021480621074000010000000107400000000000000000000000000000001000000000000000");

        // Debit Data
//        parseDataCollect("01011111211245511090210000000000280125900102400000128906910740000100000001074069107400001000000010740691074000010000000107440000000000200000002148000000001000000000000000");


//        parseTag09("0990000000304800000383706910740000100000001074087107400001000000010740611074000010000000107400000000000000000000000000323300000000000000000");
        parseTag09("321 0010240125900000128919 0020240125900000254819 00202401259000002548F90010010749001001074900400429603233?");
       // parseVoygerData("48000121234562001010340117000201034011700030202148004020214800601010740060101074003233");
    }
}
