package com.kskj.controller;

import com.kskj.pojo.ResendMessage;
import com.kskj.pojo.WMS.WmsTask;
import com.kskj.pojo.WMS.WmsTaskReponse;
import com.kskj.pojo.rcs.Sation.UnBind;
import com.kskj.service.MenuService;
import com.kskj.service.WmsService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/WCS")
public class WmsController {
    @Autowired
    private WmsService wmsService;

    /*
     * 当前为wms下发出库任务
     */
    @PostMapping("/WMSTaskDis")
    public WmsTaskReponse[] WmsAddTask(@RequestBody WmsTask[] wmsTask) {
        WmsTaskReponse[] wmsTaskReponses = wmsService.AddTask(wmsTask);
        return wmsTaskReponses;
    }

    /*
    当前方法为任务取消接口此处
     */
    @PostMapping("/task/cancelTask")
    public R CancelTask(@RequestBody String WCSTaskId) {
        System.out.println(WCSTaskId);
        R r = wmsService.CancelTask(WCSTaskId);
        return r;
    }

    @PostMapping("/releaseStation")
    public void ReleaseStations(@RequestBody String station) {
        System.out.println(station);
        R r = wmsService.ReleaseStations(station);
    }
    //此接口为灭菌完成之后，plc给我们的信号，我们需要根据这个信号去向wms申请解析区的库位
    @PostMapping("/plcAddTask")
    public void PlcAddTask(@RequestBody WmsTask wmsTask ) {
        System.out.println("plc服务申请接口参数"+wmsTask);
        R r = wmsService.plcAddTask(wmsTask);
    }
    //plc发送mr信号到我这个里之后，需要去清理这个RCS的站点
    @PostMapping("/releaseRcsStation")
    public void ReleaseRcsStation(@RequestBody UnBind unBind) {
        R r = wmsService.ReleaseRcsStation(unBind);
    }
    //此处为WMS查询出库站点的接口，返回出口站点名称集合
    @PostMapping("/outboundQuery")
    public List<String> OutboundQuery(@RequestBody String locationtype) {
        List<String> list = wmsService.OutboundQuery(locationtype);
        return list;
    }
    @PostMapping("/resendMessage")
    public R WmsResendMessage(@RequestBody ResendMessage resendMessage) {
        wmsService.WmsResendMessage(resendMessage);
        return R.ok("成功");
    }

}
