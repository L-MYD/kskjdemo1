package com.kskj.pojo.rcs.Task;


public class ResonseData {
private  String robotTaskCode;
private  Object extra;

    public ResonseData() {
    }

    public ResonseData(String robotTaskCode, Object extra) {
        this.robotTaskCode = robotTaskCode;
        this.extra = extra;
    }

    public String getRobotTaskCode() {
        return robotTaskCode;
    }

    public void setRobotTaskCode(String robotTaskCode) {
        this.robotTaskCode = robotTaskCode;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
