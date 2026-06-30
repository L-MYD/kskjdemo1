package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sun.misc.Signal;


public class Result {
    private String location;//虚拟托盘号
    private int errorCode;//虚拟托盘号
    private String[] errorReason;
    private String type;//虚拟托盘号
    private String barCode;//虚拟托盘号
    private int podType;//虚拟托盘号
    private Signall signalBody;

    public Result() {
    }

    public Result(String location, int errorCode, String[] errorReason, String type, String barCode, int podType, Signall signalBody) {
        this.location = location;
        this.errorCode = errorCode;
        this.errorReason = errorReason;
        this.type = type;
        this.barCode = barCode;
        this.podType = podType;
        this.signalBody = signalBody;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String[] getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String[] errorReason) {
        this.errorReason = errorReason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public int getPodType() {
        return podType;
    }

    public void setPodType(int podType) {
        this.podType = podType;
    }

    public Signall getSignalBody() {
        return signalBody;
    }

    public void setSignalBody(Signall signalBody) {
        this.signalBody = signalBody;
    }
}
