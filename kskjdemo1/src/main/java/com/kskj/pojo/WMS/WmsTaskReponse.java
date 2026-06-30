package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsTaskReponse {
    private String msgType;//S:成功 E:程序异常 F:失败
    private String msgText;//消息文本
    @JsonProperty("WcsId")
    private String wcsId;//wcsid
}
