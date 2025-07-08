package com.global.api.tests;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.utils.IRequestEncoder;

import java.io.*;
import java.math.BigDecimal;
import java.util.LinkedList;

public class BatchProvider implements IBatchProvider {
    private final Object objectLock = new Object();
    private String fileName = "C:\\temp\\batch.dat";
    private int batchNumber = 1;
    private int sequenceNumber = 1;
    private int transactionCount = 0;
    private BigDecimal totalDebits = new BigDecimal(0);
    private BigDecimal totalCredits = new BigDecimal(0);
    private IRequestEncoder requestEncoder = null;
    private LinkedList<String> encodedRequests;
    private PriorMessageInformation priorMessageInformation;

    private static BatchProvider _instance;
    public static BatchProvider getInstance() {
        if(_instance == null) {
            _instance = new BatchProvider();
        }
        return _instance;
    }

    public int getBatchNumber() {
        return batchNumber;
    }
    public int getSequenceNumber() throws BatchFullException {
        if(sequenceNumber == 99) {
            throw new BatchFullException();
        }

        synchronized (objectLock) {
            sequenceNumber += 1;
            save();
        }
        return sequenceNumber;
    }
    public int getTransactionCount() {
        return transactionCount;
    }
    public BigDecimal getTotalCredits() {
        return totalCredits;
    }
    public BigDecimal getTotalDebits() {
        return totalDebits;
    }
    public IRequestEncoder getRequestEncoder() {
        return requestEncoder;
    }
    public LinkedList<String> getEncodedRequests() {
        return encodedRequests;
    }
    public PriorMessageInformation getPriorMessageData() {
        return priorMessageInformation;
    }
    public void setPriorMessageData(PriorMessageInformation priorMessageInformation) {
        this.priorMessageInformation = priorMessageInformation;
    }

    private BatchProvider() {
        encodedRequests = new LinkedList<String>();

        synchronized (objectLock) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(fileName));

                // read the batch, sequence and transaction data
                String batchData = br.readLine();
                if (batchData != null) {
                    String[] elements = batchData.split("\\|");

                    sequenceNumber = Integer.parseInt(elements[0]);
                    batchNumber = Integer.parseInt(elements[1]);
                    transactionCount = Integer.parseInt(elements[2]);
                    totalCredits = new BigDecimal(elements[3]);
                    totalDebits = new BigDecimal(elements[4]);

                    for(int i = 0; i < transactionCount; i++) {
                        String request = br.readLine();
                        if(request != null) {
                            encodedRequests.add(request);
                        }
                    }
                } else save();
            } catch (IOException exc) {
                save();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException exc) { /* NOM NOM */ }
                }
            }
        }
    }

    public void closeBatch(boolean inBalance) {
        synchronized (objectLock) {
            sequenceNumber = 1;
            if(batchNumber == 99) {
                batchNumber = 1;
            }
            else batchNumber += 1;
            transactionCount = 0;
            totalDebits = new BigDecimal(0);
            totalCredits = new BigDecimal(0);
            encodedRequests.clear();

            save();
        }
    }
    public void reportDataCollect(TransactionType transactionType, PaymentMethodType paymentMethodType, BigDecimal amount, String encodedRequest) {
        synchronized (objectLock) {
            transactionCount += 1;
            encodedRequests.add(encodedRequest);

            switch (transactionType) {
                case Capture:
                case Sale: {
                    totalDebits = totalDebits.add(amount);
                } break;
                case Refund: {
                    totalCredits = totalCredits.add(amount);
                }
                case Reversal: {
                    totalDebits = totalDebits.subtract(amount);
                } break;
            }
            save();
        }
    }

    private void save() {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.format("%s|%s|%s|%s|%s\r\n", sequenceNumber, batchNumber, transactionCount, totalCredits, totalDebits));

            for (String request : encodedRequests) {
                bw.write(request + "\r\n");
            }
        } catch (IOException exc) {
            // Handle exception if needed
        }
    }
}
