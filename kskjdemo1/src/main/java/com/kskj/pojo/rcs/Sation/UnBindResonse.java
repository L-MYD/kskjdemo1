package com.kskj.pojo.rcs.Sation;

public class UnBindResonse {
    private String code;//任务号，全局唯一
    private String message;//任务取消类型 取消(原软取消) CANCEL   人工介入(原硬取消) DROP
    private String extra;//回库的载具编号

    public UnBindResonse() {
    }

    public UnBindResonse(String code, String message, String extra) {
        this.code = code;
        this.message = message;
        this.extra = extra;
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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
