package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskContent {
    private int taskID;
    private String bizID;
    private String clientCode;
    private String deviceType;
    private String robotID;
    private String podID;
    private PodInfo podInfo;
    private int priority;
    private int taskType;
    private int status;
    private int errorCode;
    private String errorReason;
    private int desType;
    private String desNodeID;
    private String desStationCodes;
    private String desStorageID;
    private String desZoneCode;
    private String result;
    private String createTime;
    private String finishTime;
}
