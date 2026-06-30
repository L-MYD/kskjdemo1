package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsStatusItem {
    @JsonProperty("WmsId")    // 明确指定JSON字段名为 "WmsId"
    private String wmsId;     // Java字段使用小写

    @JsonProperty("Palno")
    private String palno;

    @JsonProperty("Tkdat")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date tkdat;

    @JsonProperty("Statu")
    private String statu;

    @JsonProperty("WCsId")    // 注意这里的 "WCsId" 而不是 "WCSId"
    private String wcsId;

    @JsonProperty("Port")
    private String port;
}
