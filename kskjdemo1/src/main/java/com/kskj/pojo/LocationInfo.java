package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfo {//库位信息类
    private  int id;
    private String locationcode;
    private String locationtype;
    private String status;
    private String containercode;
    private String storagetime;
    private String lanenumber;
    private int laneid;
    private int lanesequence;
    private String backup;
}
