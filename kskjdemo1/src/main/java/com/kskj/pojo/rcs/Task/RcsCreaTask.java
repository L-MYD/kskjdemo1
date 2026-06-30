package com.kskj.pojo.rcs.Task;


public class RcsCreaTask {
    private  String taskType;
    private TaskTargetRoute[] targetRoute;
    private int initPriority;
    private String deadline;
    private String robotType;
    private String robotCode;
    private String interrupt;
    private String robotTaskCode;
    private String groupCode;
    private TaskExtra extra;

    public RcsCreaTask() {
    }

    public RcsCreaTask(String taskType, TaskTargetRoute[] targetRoute, int initPriority, String deadline, String robotType, String robotCode, String interrupt, String robotTaskCode, String groupCode, TaskExtra extra) {
        this.taskType = taskType;
        this.targetRoute = targetRoute;
        this.initPriority = initPriority;
        this.deadline = deadline;
        this.robotType = robotType;
        this.robotCode = robotCode;
        this.interrupt = interrupt;
        this.robotTaskCode = robotTaskCode;
        this.groupCode = groupCode;
        this.extra = extra;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskTargetRoute[] getTargetRoute() {
        return targetRoute;
    }

    public void setTargetRoute(TaskTargetRoute[] targetRoute) {
        this.targetRoute = targetRoute;
    }

    public int getInitPriority() {
        return initPriority;
    }

    public void setInitPriority(int initPriority) {
        this.initPriority = initPriority;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
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

    public String getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(String interrupt) {
        this.interrupt = interrupt;
    }

    public String getRobotTaskCode() {
        return robotTaskCode;
    }

    public void setRobotTaskCode(String robotTaskCode) {
        this.robotTaskCode = robotTaskCode;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public TaskExtra getExtra() {
        return extra;
    }

    public void setExtra(TaskExtra extra) {
        this.extra = extra;
    }
}
