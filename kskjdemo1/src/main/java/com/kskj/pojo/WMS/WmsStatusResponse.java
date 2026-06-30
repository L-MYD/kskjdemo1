package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsStatusResponse {
    @JsonProperty("msgType")
    private String msgType;        // 消息类型：S-成功，E-错误

    @JsonProperty("msgText")
    private String msgText;        // 消息文本

    @JsonProperty("WcsId")
    private String wcsId;          // WCS ID

    @JsonProperty("Addre")
    private String Addre;       // 新地址
}
