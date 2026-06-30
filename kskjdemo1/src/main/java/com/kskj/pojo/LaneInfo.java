package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaneInfo {//巷道对应的类
    private  int id;
    private  String lanename;
    private  String lanenumber;
    private  String tunneicapacity;
    private  String state;
    private  String capacityavailable;
    private  String areaid;
    private  String areaname;
    private  String regionname;
}
