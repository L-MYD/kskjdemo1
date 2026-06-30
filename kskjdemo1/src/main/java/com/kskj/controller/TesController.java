package com.kskj.controller;

import com.kskj.pojo.TES.StationStatusMessage;
import com.kskj.pojo.TES.TaskStatusMessage;
import com.kskj.pojo.TES.TrackExternalInspection;
import com.kskj.pojo.TES.TrackExternallnspectionResqon;
import com.kskj.pojo.WMS.WmsTask;
import com.kskj.service.TesService;
import com.kskj.service.WmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Tes")
public class TesController {
@Autowired
private TesService  tesservice;
/*
 此接口为外检信息接收接口，接收外检信息。轨道站点外检消息messageType=52
 */
@PostMapping("/InspectionInfo")
public TrackExternallnspectionResqon getTesInspectionInfo(@RequestBody TrackExternalInspection teiTask){
    TrackExternallnspectionResqon trackExternallnspectionResqon = tesservice.WarehouseLocation(teiTask);
    return  trackExternallnspectionResqon;
}
 /*

 此接口为任务完成接口，接收完成消息的接口。任务状态更新消息通知messageType=10
 */
@PostMapping("/TaskInfo")
public TrackExternallnspectionResqon getTesTaskInfo(@RequestBody TaskStatusMessage taskStatusMessage){
    TrackExternallnspectionResqon trackExternallnspectionResqon = tesservice.ToWmsTaskInfo(taskStatusMessage);
    System.out.println("返回给TES的参数--------------"+trackExternallnspectionResqon);
    return  trackExternallnspectionResqon;
}
 /*
 此接口为AGV搬走容器之后，更新的的接口消息.站点容器消息messageType=60
 */
@PostMapping("/SiteMessage")
public TrackExternallnspectionResqon getSiteMessage(@RequestBody StationStatusMessage stationStatusMessage){
    TrackExternallnspectionResqon siteMessage = tesservice.getSiteMessage(stationStatusMessage);
    return siteMessage;
}
}
