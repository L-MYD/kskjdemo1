package com.kskj.pojo.rcs.reportertask;



public class RcsReporterTaskResonseData {
    private  String robotTaskCode;

    public RcsReporterTaskResonseData() {
    }

    public RcsReporterTaskResonseData(String robotTaskcode) {
        this.robotTaskCode = robotTaskcode;
    }

    public String getRobotTaskcode() {
        return robotTaskCode;
    }

    public void setRobotTaskcode(String robotTaskcode) {
        this.robotTaskCode = robotTaskcode;
    }
}
