package com.kskj.pojo.rcs;




public class ReturnData {
    private  String robotTaskCode;
    private  Extra extra;

    public ReturnData() {
    }

    public ReturnData(String robotTaskCode, Extra extra) {
        this.robotTaskCode = robotTaskCode;
        this.extra = extra;
    }

    public String getRobotTaskCode() {
        return robotTaskCode;
    }

    public void setRobotTaskCode(String robotTaskCode) {
        this.robotTaskCode = robotTaskCode;
    }

    public Extra getExtra() {
        return extra;
    }

    public void setExtra(Extra extra) {
        this.extra = extra;
    }
}
