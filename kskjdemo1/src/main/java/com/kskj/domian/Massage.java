package com.kskj.domian;

public class Massage {
    public Massage() {

    }

    private String reqCode;//请求编号
    private String reqTime;//时间
    private int cooX ; //地码 X 坐标(mm)：任务完成时有值
    public int cooY;//地码 Y 坐标(mm)：任务完成时有值
    public String currentPositionCode;//当前位置编号

    public String data;//自定义字段，不超过2000个字符
    public String mapCode;//地图编号
    public String mapDataCode;//地码编号：任务完成时有值
    public String stgBinCode;//仓位编号：叉车与CTU任务时有值
    public String method ;//方法名, 可使用任务类型做为方法名
    public String podCode ;//货架编号：背货架时有值
    public String podDir;//“180”,”0”,”90”,”-90” 分别对应地图的”左”,”右”,”上”,”下”：任务完成时有值
    public String materialLot ;//物料编号
    public String robotCode;//AGV编号（同 agvCode ）
    public String taskCode;//当前任务单号
    public String wbCode;//工作位，与RCS-2000端配置的位置名称一致。任务完成时有值，与生成任务单接口中的wbCode一致。
    public String ctnrCode;//容器编号
    public String ctnrType;//容器类型
    public String roadWayCode;//巷道编号
    public String seq;//巷道内顺序号 巷道尾是0，到巷道头依次递增1
    public String eqpCode;//设备编号

    public String getReqCode() {
        return reqCode;
    }

    public void setReqCode(String reqCode) {
        this.reqCode = reqCode;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    public int getCooX() {
        return cooX;
    }

    public void setCooX(int cooX) {
        this.cooX = cooX;
    }

    public int getCooY() {
        return cooY;
    }

    public void setCooY(int cooY) {
        this.cooY = cooY;
    }

    public String getCurrentPositionCode() {
        return currentPositionCode;
    }

    public void setCurrentPositionCode(String currentPositionCode) {
        this.currentPositionCode = currentPositionCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMapCode() {
        return mapCode;
    }

    public void setMapCode(String mapCode) {
        this.mapCode = mapCode;
    }

    public String getMapDataCode() {
        return mapDataCode;
    }

    public void setMapDataCode(String mapDataCode) {
        this.mapDataCode = mapDataCode;
    }

    public String getStgBinCode() {
        return stgBinCode;
    }

    public void setStgBinCode(String stgBinCode) {
        this.stgBinCode = stgBinCode;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPodCode() {
        return podCode;
    }

    public void setPodCode(String podCode) {
        this.podCode = podCode;
    }

    public String getPodDir() {
        return podDir;
    }

    public void setPodDir(String podDir) {
        this.podDir = podDir;
    }

    public String getMaterialLot() {
        return materialLot;
    }

    public void setMaterialLot(String materialLot) {
        this.materialLot = materialLot;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public void setRobotCode(String robotCode) {
        this.robotCode = robotCode;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getWbCode() {
        return wbCode;
    }

    public void setWbCode(String wbCode) {
        this.wbCode = wbCode;
    }

    public String getCtnrCode() {
        return ctnrCode;
    }

    public void setCtnrCode(String ctnrCode) {
        this.ctnrCode = ctnrCode;
    }

    public String getCtnrType() {
        return ctnrType;
    }

    public void setCtnrType(String ctnrType) {
        this.ctnrType = ctnrType;
    }

    public String getRoadWayCode() {
        return roadWayCode;
    }

    public void setRoadWayCode(String roadWayCode) {
        this.roadWayCode = roadWayCode;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getEqpCode() {
        return eqpCode;
    }

    public void setEqpCode(String eqpCode) {
        this.eqpCode = eqpCode;
    }
}
