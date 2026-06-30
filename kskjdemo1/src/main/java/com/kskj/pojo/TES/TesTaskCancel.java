package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//此类为TES任务取消的发送类
public class TesTaskCancel {
    private  String warehouseID;
    private  String requestID;
    private  String clientCode;
    private  int taskID;
    private  String reason;
    private  int force;
    private  int withoutRunning;
}
