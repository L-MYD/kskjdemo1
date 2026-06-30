package com.kskj.pojo.rcs.Task;


public class TaskResonse {
    private String code;
    private String message;
    private ResonseData data;

    public TaskResonse() {
    }

    public TaskResonse(String code, String message, ResonseData data) {
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

    public ResonseData getData() {
        return data;
    }

    public void setData(ResonseData data) {
        this.data = data;
    }
}
