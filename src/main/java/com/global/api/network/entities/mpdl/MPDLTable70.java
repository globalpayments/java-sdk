package com.global.api.network.entities.mpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class MPDLTable70 implements IMPDLTable {
    @Getter
    @Setter
    private String inReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfInReceiptMessages;
    @Getter
    @Setter
    private List<String> inReceiptMessages;

    @Getter
    @Setter
    private String outReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfOutReceiptMessages;
    @Getter
    @Setter
    private List<String> outReceiptMessages;


    @Getter
    @Setter
    private String inPotentialDiscReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfInPotentialDiscReceiptMessages;
    @Getter
    @Setter
    private List<String> inPotentialDiscReceiptMessages;


    @Getter
    @Setter
    private String outPotentialDiscReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfOutPotentialDiscReceiptMessages;
    @Getter
    @Setter
    private List<String> outPotentialDiscReceiptMessages;


    @Getter
    @Setter
    private String inAppliedDiscReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfInAppliedDiscReceiptMessages;
    @Getter
    @Setter
    private List<String> inAppliedDiscReceiptMessages;


    @Getter
    @Setter
    private String outAppliedDiscReceiptMessageCode;
    @Getter
    @Setter
    private Integer noOfOutAppliedDiscReceiptMessages;
    @Getter
    @Setter
    private List<String> outAppliedDiscReceiptMessages;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setInReceiptMessageCode(sp.readString(2));
        this.setNoOfInReceiptMessages(sp.readInt(2));
        this.inReceiptMessages = new ArrayList<>();
        for (int inReceiptIndex = 0; inReceiptIndex < this.getNoOfInReceiptMessages(); inReceiptIndex++) {
            inReceiptMessages.add(sp.readString(38));
        }

        this.setOutReceiptMessageCode(sp.readString(2));
        this.setNoOfOutReceiptMessages(sp.readInt(2));
        this.outReceiptMessages = new ArrayList<>();
        for (int outReceiptIndex = 0; outReceiptIndex < this.getNoOfOutReceiptMessages(); outReceiptIndex++) {
            outReceiptMessages.add(sp.readString(18));
        }

        this.setInPotentialDiscReceiptMessageCode(sp.readString(2));
        this.setNoOfInPotentialDiscReceiptMessages(sp.readInt(2));
        this.inPotentialDiscReceiptMessages = new ArrayList<>();
        for (int inPotentialIndex = 0; inPotentialIndex < this.getNoOfInPotentialDiscReceiptMessages(); inPotentialIndex++) {
            inPotentialDiscReceiptMessages.add(sp.readString(38));
        }

        this.setOutPotentialDiscReceiptMessageCode(sp.readString(2));
        this.setNoOfOutPotentialDiscReceiptMessages(sp.readInt(2));
        this.outPotentialDiscReceiptMessages = new ArrayList<>();
        for (int outPotentialIndex = 0; outPotentialIndex < this.getNoOfOutPotentialDiscReceiptMessages(); outPotentialIndex++) {
            outPotentialDiscReceiptMessages.add(sp.readString(18));
        }

        this.setInAppliedDiscReceiptMessageCode(sp.readString(2));
        this.setNoOfInAppliedDiscReceiptMessages(sp.readInt(2));
        this.inAppliedDiscReceiptMessages = new ArrayList<>();
        for (int inAppliedIndex = 0; inAppliedIndex < this.getNoOfInAppliedDiscReceiptMessages(); inAppliedIndex++) {
            this.inAppliedDiscReceiptMessages.add(sp.readString(38));
        }

        this.setOutAppliedDiscReceiptMessageCode(sp.readString(2));
        this.setNoOfOutAppliedDiscReceiptMessages(sp.readInt(2));
        this.outAppliedDiscReceiptMessages = new ArrayList<>();
        for (int outAppliedIndex = 0; outAppliedIndex < this.getNoOfOutAppliedDiscReceiptMessages(); outAppliedIndex++) {
            outAppliedDiscReceiptMessages.add(sp.readString(18));
        }

        return new MPDLTable(this);
    }
}
