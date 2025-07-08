package com.global.api.tests;

import com.global.api.network.abstractions.IStanProvider;

import java.io.*;

public class StanGenerator implements IStanProvider {
    private final Object objectLock = new Object();

    private static StanGenerator _instance;
    public static StanGenerator getInstance() {
        if(_instance == null) {
            _instance = new StanGenerator();
        }
        return _instance;
    }

    private int currNumber = 0;
    private String fileName = "C:\\temp\\stan.dat";

    private StanGenerator() {
        synchronized (objectLock) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(fileName));
                String savedValue = br.readLine();
                if (savedValue != null) {
                    currNumber = Integer.parseInt(savedValue);
                } else saveCurrNumber();
            } catch (IOException exc) {
                saveCurrNumber();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException exc) { /* NOM NOM */ }
                }
            }
        }
    }

    public int generateStan() {
        synchronized (objectLock) {
            if(currNumber == 9999) {
                currNumber = 1;
            }
            else currNumber += 1;
            saveCurrNumber();
        }
        return currNumber;
    }

    public void reset() {
        synchronized (objectLock) {
            currNumber = 0;
            saveCurrNumber();
        }
    }

    private void saveCurrNumber() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(currNumber + "");
        } catch (IOException exc) {
            /* NOM NOM */
        }
    }
}
