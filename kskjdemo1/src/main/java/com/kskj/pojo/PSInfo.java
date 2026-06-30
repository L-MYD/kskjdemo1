package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PSInfo {
    private int id;
    private String pssncode;
    private String psname;
    private String ipaddress;
    private String pstype;
    private String status;
    private int batterylevel;
    private String currentposition;
    private String currenttaskid;
    private String isactive;
    private String exceptionmessage;

}
