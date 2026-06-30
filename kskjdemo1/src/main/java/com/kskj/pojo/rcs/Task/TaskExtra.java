package com.kskj.pojo.rcs.Task;


public class TaskExtra {
    private  TaskAnglelnfo anglelnfo;
    private  TaskCarrierInfo[] carrierInfo;

    public TaskExtra() {
    }

    public TaskExtra(TaskAnglelnfo anglelnfo, TaskCarrierInfo[] carrierInfo) {
        this.anglelnfo = anglelnfo;
        this.carrierInfo = carrierInfo;
    }

    public TaskAnglelnfo getAnglelnfo() {
        return anglelnfo;
    }

    public void setAnglelnfo(TaskAnglelnfo anglelnfo) {
        this.anglelnfo = anglelnfo;
    }

    public TaskCarrierInfo[] getCarrierInfo() {
        return carrierInfo;
    }

    public void setCarrierInfo(TaskCarrierInfo[] carrierInfo) {
        this.carrierInfo = carrierInfo;
    }
}
