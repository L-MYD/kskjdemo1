package com.kskj.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private int id;//主键id
    private String WCSTaskId;//wcs在接受到任务之后，自动生成的id
    private String WMSTaskId;//wms在发送任务之后传入的id
    private String ThirdPartyTaskId;//创建任务接口后，接收到的tes/haikio的id
    private String TaskType;//任务类型，由当前项目自己决定
    private String ContainerCode;//容器号
    private String StartPosition;//起始位置
    private String AGVCode;//执行当前任务的AGV
    private String TargetPosition;//目标位置
    private String TaskStatus;//任务状态
    private String Progress;//子任务的父任务id,父任务为空
    private String RcsOrTes;//RCS任务还是Tes任务
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String TaskCreaTime;//任务创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String TaskCompletionTime;//任务完成时间
    private String Priority;//优先级;//任务完成时间
    private List<WmsWcsTaskInfo> children;
 }
