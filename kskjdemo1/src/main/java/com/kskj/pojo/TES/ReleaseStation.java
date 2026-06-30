package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStation {
    private  String warehouseID;
    private  String requestID;
    private  String clientCode;
    private  String stationCode;//站点号

}
