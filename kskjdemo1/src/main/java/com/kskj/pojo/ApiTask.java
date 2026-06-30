package com.kskj.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiTask {
    private int id;

    @JsonProperty("WCSTaskId")
    private String wcsTaskId;

    @JsonProperty("WMSTaskId")
    private String wmsTaskId;

    @JsonProperty("ThirdPartyTaskId")
    private String thirdPartyTaskId;

    @JsonProperty("TaskType")
    private String taskType;

    @JsonProperty("ContainerCode")
    private String containerCode;

    @JsonProperty("AGVCode")
    private String agvCode;

    @JsonProperty("StartPosition")
    private String startPosition;

    @JsonProperty("TargetPosition")
    private String targetPosition;

    @JsonProperty("TaskStatus")
    private String taskStatus;

    @JsonProperty("Progress")
    private String progress;

    @JsonProperty("RcsOrTes")
    private String rcsOrTes;

    @JsonProperty("TaskCreaTime")
    private String taskCreaTime;

    @JsonProperty("TaskCompletionTime")
    private String taskCompletionTime;
}
