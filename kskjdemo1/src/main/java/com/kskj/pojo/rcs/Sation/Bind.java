package com.kskj.pojo.rcs.Sation;

public class Bind {
    private String carrierCode;//任务号，全局唯一
    private String siteCode;//任务取消类型 取消(原软取消) CANCEL   人工介入(原硬取消) DROP
    private String carrierDir;//回库的载具编号
    private String extra;//回库的载具编号

    public Bind() {
    }

    public Bind(String carrierCode, String siteCode, String carrierDir, String extra) {
        this.carrierCode = carrierCode;
        this.siteCode = siteCode;
        this.carrierDir = carrierDir;
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

    public String getCarrierDir() {
        return carrierDir;
    }

    public void setCarrierDir(String carrierDir) {
        this.carrierDir = carrierDir;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
