package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TesTask {
 private  String warehouseID;
 private  String requestID;
 private  String clientCode;
 private  int desType;
 private  int srcType;
 private  String podID;
 private  int priority;
 private  String bizID;
 private  int replacePodTask;
 private  DesExt desExt;
 private TaskExt taskExt;
 private String destination;
}
