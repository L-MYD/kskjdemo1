package com.kskj.pojo.TES;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationStatusContent {
    @JsonProperty("stationCode")
    private String stationCode;

    @JsonProperty("podID")
    private String podID;

    @JsonProperty("occupyStatus")
    private int occupyStatus;
}
