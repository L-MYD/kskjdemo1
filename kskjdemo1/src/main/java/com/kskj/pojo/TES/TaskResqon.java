package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResqon {
    private int returnCode;
    private String returnMsg;
    private String returnUserMsg;
    private  CallData data;
}
