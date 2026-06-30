package com.kskj.pojo.rcs.reportertask;

public class ReporterTaskExtra {
    private String async;
    private ReporterTaskValues values;

    public ReporterTaskExtra() {
    }

    public ReporterTaskExtra(String async, ReporterTaskValues values) {
        this.async = async;
        this.values = values;
    }

    public String getAsync() {
        return async;
    }

    public void setAsync(String async) {
        this.async = async;
    }

    public ReporterTaskValues getValues() {
        return values;
    }

    public void setValues(ReporterTaskValues values) {
        this.values = values;
    }
}
