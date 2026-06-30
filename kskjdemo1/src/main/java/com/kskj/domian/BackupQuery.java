package com.kskj.domian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupQuery {
    private  Integer pageNum=1;
    //每页的显示的条数
    private Integer  pageSize=20;
//    public  Integer getStar(){
//        return ((this.pageNum-1)*pageSize);
//    }
//{"messageID":577134054730629145,"messageType":52,"warehouseID":"HETU","createTime":"2025-07-26T19:11:43.207110291+08:00","content":{"robotID":"6002","stationCode":"F1-CHECK-01","result":{"podID":"900017","signal":{"location":"6002","errorCode":0,"errorReason":[],"type":"sizeCheck","barCode":"900003","podType":0,"signalBody":{"rfid":null,"weight":0,"weightKG":0,"weightOutOfRange":0,"sizeCheck":{"length":0,"width":0,"height":100,"lengthOutOfRange":0,"widthOutOfRange":0,"heightOutOfRange":0},"electricalCheck":"","button":0}},"errorState":null,"errorMessage":null}}}
//    private Integer total;
//    private String keyword;
//    private String sex;
//    private String name;
//    private String address;
}
