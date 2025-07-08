package com.global.api.tests.network.gnap;

import com.global.api.network.entities.gnap.SequenceNumber;
import com.global.api.entities.Transaction;

import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUtil {
    private final String fileName = "C:\\temp\\batch.txt";
    private int dayCounter=1;
    private int shiftCounter=1;
    private int batchCounter=1;
    private int seqCounter=1;
    private int indicator=0;
    private static TestUtil _instance;
    public static TestUtil getInstance() {
        if(_instance == null) {
            _instance = new TestUtil();
        }
        return _instance;
    }

    protected int getTransmissionNo()  {
        String userName = System.getProperty("user.name");
        //byte b = 1;
        int i =0;
        int c = 0;
        try (FileInputStream fis = new FileInputStream("C://Users/" + userName + "/Documents/GnapTransCounter.txt")) {
            while ((i = fis.read()) != -1) {
                c = i;
            }
            try (FileOutputStream fos = new FileOutputStream("C://Users/" + userName + "/Documents/GnapTransCounter.txt")) {
                if (c == 99) {
                    fos.write(0);
                } else {
                    fos.write(++c);
                }
                fos.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("transmission Number :" + c);
        return c;
    }

    protected void assertSuccess(Transaction response) {
        String responseCodeValue = response.getGnapResponse().getGnapMessageHeader().getResponseCode();
        int responseCode = Integer.parseInt(responseCodeValue);
        if(responseCode==899)
        {
            save(response.getGnapResponse().getGnapResponseData().getSequenceNumber());
        }
        assertNotNull(response);
        assertTrue("Response code should be between 000 to 049 but actual value is " + responseCodeValue, 0 <= responseCode && responseCode <= 49);
    }

    protected SequenceNumber getSequenceNumber() {

        fetch();
        indicator=0;
        SequenceNumber sn=new SequenceNumber();
        sn.setDayCounter(dayCounter);
        sn.setShiftCounter(shiftCounter);
        sn.setBatchCounter(batchCounter);
        sn.setSequenceCounter(seqCounter);
        sn.setIndicator(indicator);
        seqCounter++;
        save();
        return sn;
    }

    protected SequenceNumber settlement()
    {
        shiftCounter=2;
        seqCounter=0;
        indicator=1;
        SequenceNumber sn=new SequenceNumber();
        sn.setDayCounter(dayCounter);
        sn.setShiftCounter(shiftCounter);
        sn.setBatchCounter(batchCounter);
        sn.setSequenceCounter(seqCounter);
        sn.setIndicator(indicator);
        dayCounter+=1;
        seqCounter+=1;
        save();
        return sn;
    }

    private void save()
    {
        save(null);
    }

    private void save(SequenceNumber sequence) {
        if (sequence != null) {
            dayCounter = sequence.getDayCounter();
            shiftCounter = sequence.getShiftCounter();
            batchCounter = sequence.getBatchCounter();
            seqCounter = sequence.getSequenceCounter();
            indicator = sequence.getIndicator();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("%s|%s|%s|%s|%s\r\n", dayCounter, shiftCounter, batchCounter, seqCounter, indicator));
            bw.flush();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void fetch() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String batchData = br.readLine();
            if (batchData != null) {
                String[] elements = batchData.split("\\|");
                dayCounter = Integer.parseInt(elements[0]);
                shiftCounter = Integer.parseInt(elements[1]);
                batchCounter = Integer.parseInt(elements[2]);
                seqCounter = Integer.parseInt(elements[3]);
                indicator = Integer.parseInt(elements[4]);
            }
        }catch (IOException exc) {
            exc.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException exc) { exc.printStackTrace(); }
            }
        }

        if(dayCounter==999)
            dayCounter=1;
        if(shiftCounter==2)
            shiftCounter=1;
        if(seqCounter==999)
            seqCounter=1;
        if(indicator==1)
            indicator=0;
    }

}
