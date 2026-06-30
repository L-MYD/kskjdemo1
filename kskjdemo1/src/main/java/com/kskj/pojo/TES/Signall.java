package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signall {
    private String rfid; // 根据实际数据类型调整
    private int weight;//托盘重
    private int weightOutOfRange;//是否超重
    private SizeCheck sizeCheck;
    private int electricalCheck;//预留
    private int button;//预留
//    private String location;//设备工位号
//    private int errorCode;//检测失败错误码 0表示成功 非0表示有错误
//    private String[] errorReason; //
//    private String type;//站点类型
//    private String barCode;//托盘号
//    private int podType;//预留
//    private SignalBody signalBody;
}
