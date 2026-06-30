package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultInfo {
    private int id;
    private String faulttype;
    private String faultmessage;
    private String isshow;
}
