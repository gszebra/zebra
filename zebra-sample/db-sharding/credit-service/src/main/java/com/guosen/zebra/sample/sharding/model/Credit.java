package com.guosen.zebra.sample.sharding.model;

public class Credit {
    private String id;
    private Integer fundId;
    private Integer month;
    private Integer day;
    private String otherField1;
    private String otherField2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFundId() {
        return fundId;
    }

    public void setFundId(Integer fundId) {
        this.fundId = fundId;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getOtherField1() {
        return otherField1;
    }

    public void setOtherField1(String otherField1) {
        this.otherField1 = otherField1;
    }

    public String getOtherField2() {
        return otherField2;
    }

    public void setOtherField2(String otherField2) {
        this.otherField2 = otherField2;
    }
}
