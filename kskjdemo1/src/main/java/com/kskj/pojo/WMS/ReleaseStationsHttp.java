package com.kskj.pojo.WMS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStationsHttp {
    @JsonProperty("WcsId")
    private String wcsId;
    @JsonProperty("OutPort")
    private String outPort;
}
