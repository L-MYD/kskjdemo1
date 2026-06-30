package com.kskj.pojo.rcs.reportertask;

public class RcsReporterTask {
    private  String robotTaskCode;
    private  String singleRobotCode;
    private  ReporterTaskExtra extra;

    public RcsReporterTask() {
    }

    public RcsReporterTask(String robotTaskCode, String singleRobotCode, ReporterTaskExtra extra) {
        this.robotTaskCode = robotTaskCode;
        this.singleRobotCode = singleRobotCode;
        this.extra = extra;
    }

    public String getRobotTaskCode() {
        return robotTaskCode;
    }

    public void setRobotTaskCode(String robotTaskCode) {
        this.robotTaskCode = robotTaskCode;
    }

    public String getSingleRobotCode() {
        return singleRobotCode;
    }

    public void setSingleRobotCode(String singleRobotCode) {
        this.singleRobotCode = singleRobotCode;
    }

    public ReporterTaskExtra getExtra() {
        return extra;
    }

    public void setExtra(ReporterTaskExtra extra) {
        this.extra = extra;
    }
}
