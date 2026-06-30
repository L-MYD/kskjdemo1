package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsTask {
    //出库任务模版
    @JsonProperty("WmsId")
    private String wmsId;//wms发送任务的id
    @JsonProperty("Palno")
    private String palno;//wms发送任务的托盘号
    @JsonProperty("FromLocation")
    private String fromLocation;//wms发送任务的起始地
    @JsonProperty("ToLocation")
    private String toLocation;//wms发送任务的目的地
    @JsonProperty("IfPicking")
    private String ifPicking;//当前任务是否拣选，0：整盘 1：拣选
    @JsonProperty("Num")
    private String num;//数字越大越优先，整数
    @JsonProperty("TaskType")
    private String TaskType;//根据项目定制
    @JsonProperty("Track")
    private String Track;//唯一单号（用于解决先建后出问题）
}
