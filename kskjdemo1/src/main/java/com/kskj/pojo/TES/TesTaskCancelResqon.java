package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//此类为TES任务取消的接收类
public class TesTaskCancelResqon {
    private  String returnCode;
    private  String returnMsg;
    private  String returnUserMsg;
    private  CallData data;
}
