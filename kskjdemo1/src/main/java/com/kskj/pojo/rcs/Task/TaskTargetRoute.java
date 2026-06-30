package com.kskj.pojo.rcs.Task;
public class TaskTargetRoute {
    private int seq;
    private String type;
    private String code;
    private String operation;
    private String robotType;
    private String robotCode;
    private TaskExtra extra;

    public TaskTargetRoute() {
    }

    public TaskTargetRoute(int seq, String type, String code, String operation, String robotType, String robotCode, TaskExtra extra) {
        this.seq = seq;
        this.type = type;
        this.code = code;
        this.operation = operation;
        this.robotType = robotType;
        this.robotCode = robotCode;
        this.extra = extra;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRobotType() {
        return robotType;
    }

    public void setRobotType(String robotType) {
        this.robotType = robotType;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public void setRobotCode(String robotCode) {
        this.robotCode = robotCode;
    }

    public TaskExtra getExtra() {
        return extra;
    }

    public void setExtra(TaskExtra extra) {
        this.extra = extra;
    }
}
