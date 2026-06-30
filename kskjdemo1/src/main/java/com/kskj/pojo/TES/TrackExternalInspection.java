package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackExternalInspection {
    //轨道外检信息类
    private long messageID;
    private int messageType;
    private String warehouseID;
    private String createTime;
    private Content content;
}
