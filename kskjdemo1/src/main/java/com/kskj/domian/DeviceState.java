package com.kskj.domian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceState {
    private String id;
    private int stackerNo;
    private int stackerState;
    private int faultNo;
    private int xposition;
    private int yposition;
    private int inKutaiNo;
    private String inKutaiTrayCode;
    private int inKutaiState;
    private int outKutaiNo;
    private String outKutaiTrayCode;
    private int outKutaiState;
    private String faultDesc;

}
