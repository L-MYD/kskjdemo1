package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStationResqon {
    private int returnCode;
    private String returnMsg;
    private String returnUserMsg;
    private String data;
}
