package com.kskj.domian;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultCode {
    private Integer id;
    private Integer faultNo;
    private String faultDesc;
    private DateTime createTime;
    private DateTime updateTime;
    private String remark;
}
