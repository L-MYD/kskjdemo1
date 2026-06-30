package com.kskj.pojo.PLC;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlcSendMes {
    private  int id;
    private String PlcIp;
    private String DbData;
    private String MessType;
    private String UnitID;
    private String FromLocation;
    private String ToLocation;
    private String UnitHigh;
    private String UnitWeigh;
    private String ReasonCode;
    private String CanWrite;
}
