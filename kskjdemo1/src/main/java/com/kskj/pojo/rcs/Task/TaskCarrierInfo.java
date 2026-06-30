package com.kskj.pojo.rcs.Task;



public class TaskCarrierInfo {
    private String carrierType;
    private String carrierCode;
    private int layer;

    public TaskCarrierInfo() {
    }

    public TaskCarrierInfo(String carrierType, String carrierCode, int layer) {
        this.carrierType = carrierType;
        this.carrierCode = carrierCode;
        this.layer = layer;
    }

    public String getCarrierType() {
        return carrierType;
    }

    public void setCarrierType(String carrierType) {
        this.carrierType = carrierType;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}
