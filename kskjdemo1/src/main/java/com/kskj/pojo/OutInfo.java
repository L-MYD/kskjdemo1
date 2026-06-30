package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutInfo {
    private Integer materialCountOfPlan;
    private Integer TANUM;
    private Integer TAPOS;
    private String materialCodeFix;
    private String batchCode;
    private String factoryCode;
    private String materialDesc;
}
