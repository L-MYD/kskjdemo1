package com.kskj.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendMessage {
    //此类为向wms发送任务消息失败的消息体类
    private  int id;
    private  String wmsTaskid;//wms任务id
    private  String wcsTaskid;//wcs任务id
    private  String podid;//容器号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private  String tkdat;//操作时间
    private  String statu;//任务状态
    private  String tesTaskid;//tes任务id
    private  String isSuccessfully;//是否重新发送成功
}
