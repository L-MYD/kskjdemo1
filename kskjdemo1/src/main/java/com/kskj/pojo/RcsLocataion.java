package com.kskj.pojo;

public class RcsLocataion {
    private  int id;
    private  String locataion;
    private  String lanenumber;
    private  String backup;

    public RcsLocataion() {
    }

    public RcsLocataion(int id, String locataion, String lanenumber, String backup) {
        this.id = id;
        this.locataion = locataion;
        this.lanenumber = lanenumber;
        this.backup = backup;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocataion() {
        return locataion;
    }

    public void setLocataion(String locataion) {
        this.locataion = locataion;
    }

    public String getLanenumber() {
        return lanenumber;
    }

    public void setLanenumber(String lanenumber) {
        this.lanenumber = lanenumber;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }
}
