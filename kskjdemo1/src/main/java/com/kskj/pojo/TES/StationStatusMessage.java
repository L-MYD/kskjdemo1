package com.kskj.pojo.TES;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationStatusMessage {
    @JsonProperty("messageID")
    private Long messageID;

    @JsonProperty("messageType")
    private Integer messageType;

    @JsonProperty("warehouseID")
    private String warehouseID;

    @JsonProperty("createTime")
    private String createTime;

    @JsonProperty("content")
    private StationStatusContent content;
}
