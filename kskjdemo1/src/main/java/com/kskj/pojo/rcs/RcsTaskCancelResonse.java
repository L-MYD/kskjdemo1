package com.kskj.pojo.rcs;



public class RcsTaskCancelResonse {
private  String code;
private  String message;
private  ReturnData data;

    public RcsTaskCancelResonse() {
    }

    public RcsTaskCancelResonse(String code, String message, ReturnData data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ReturnData getData() {
        return data;
    }

    public void setData(ReturnData data) {
        this.data = data;
    }
}
