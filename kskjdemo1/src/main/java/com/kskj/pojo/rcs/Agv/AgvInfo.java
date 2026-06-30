package com.kskj.pojo.rcs.Agv;


public class AgvInfo {
    private int id;
    private String agvcode;
    private String agvname;
    private String ipaddress;
    private String status;
    private String batterylevel;
    private String currentposition;
    private String currenttaskId;
    private String isactive;
    private String speed;
    private String exceptionmessage;
    private String area;

    public AgvInfo() {
    }

    public AgvInfo(int id, String agvcode, String agvname, String ipaddress, String status, String batterylevel, String currentposition, String currenttaskId, String isactive, String speed, String exceptionmessage, String area) {
        this.id = id;
        this.agvcode = agvcode;
        this.agvname = agvname;
        this.ipaddress = ipaddress;
        this.status = status;
        this.batterylevel = batterylevel;
        this.currentposition = currentposition;
        this.currenttaskId = currenttaskId;
        this.isactive = isactive;
        this.speed = speed;
        this.exceptionmessage = exceptionmessage;
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAgvcode() {
        return agvcode;
    }

    public void setAgvcode(String agvcode) {
        this.agvcode = agvcode;
    }

    public String getAgvname() {
        return agvname;
    }

    public void setAgvname(String agvname) {
        this.agvname = agvname;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBatterylevel() {
        return batterylevel;
    }

    public void setBatterylevel(String batterylevel) {
        this.batterylevel = batterylevel;
    }

    public String getCurrentposition() {
        return currentposition;
    }

    public void setCurrentposition(String currentposition) {
        this.currentposition = currentposition;
    }

    public String getCurrenttaskId() {
        return currenttaskId;
    }

    public void setCurrenttaskId(String currenttaskId) {
        this.currenttaskId = currenttaskId;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getExceptionmessage() {
        return exceptionmessage;
    }

    public void setExceptionmessage(String exceptionmessage) {
        this.exceptionmessage = exceptionmessage;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
