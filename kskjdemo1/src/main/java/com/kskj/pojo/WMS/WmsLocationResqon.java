package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocationResqon {
    private  String msgType;//消息类型:S:成功 E:程序异常 F:失败
    private  String msgText;//消息文本
    @JsonProperty("WcsId")
    private  String WcsId;//WCS 流水号
    @JsonProperty("WmsId")
    private  String WmsId;//WmS 流水号
    @JsonProperty("ToLocation")
    private  String ToLocation;//回执通道:通道号
}
