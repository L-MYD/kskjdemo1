package com.kskj.pojo.rcs.Sation;

public class UnBind {
    private String carrierCode;//任务号，全局唯一
    private String siteCode;//任务取消类型 取消(原软取消) CANCEL   人工介入(原硬取消) DROP
    private String extra;//回库的载具编号

    public UnBind() {
    }

    public UnBind(String carrierCode, String siteCode, String extra) {
        this.carrierCode = carrierCode;
        this.siteCode = siteCode;
        this.extra = extra;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
