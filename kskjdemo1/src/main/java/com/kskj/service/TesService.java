package com.kskj.service;

import cn.hutool.core.util.IdUtil;

import com.kskj.HttpService.TesHttpService;
import com.kskj.HttpService.WMSHttpService;
import com.kskj.mapper.*;
import com.kskj.pojo.*;
import com.kskj.pojo.PLC.PlcSendMes;
import com.kskj.pojo.TES.*;
import com.kskj.pojo.WMS.WmsLocation;
import com.kskj.pojo.WMS.WmsLocationResqon;
import com.kskj.pojo.WMS.WmsStatusItem;
import com.kskj.pojo.WMS.WmsStatusResponse;
import com.kskj.service.impl.ITesService;
import com.kskj.until.ExceptionLogger;
import com.kskj.until.SqlLogger;
import com.kskj.until.TaskConfig;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TesService implements ITesService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private RetryMechanismMapper retryMechanismMapper;
    @Autowired
    private TesHttpService tesHttpService;
    @Autowired
    private WMSHttpService wmsHttpService;
    @Autowired
    private PsMapper psMapper;
    @Autowired
    private TaskConfig taskConfig;
    @Autowired
    private PlcSendMesMapper plcSendMesMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private AislesMapper aislesMapper;
    @Autowired
    private StoragelocationsMapper storagelocationsMapper;

    @Override
    //当前方法为获取外检信息之后，申请库位方法
    public TrackExternallnspectionResqon WarehouseLocation(TrackExternalInspection trackExternalInspection) {
        // 记录方法开始
        SqlLogger.logSqlStart("WarehouseLocation", "处理外检信息", trackExternalInspection);
        TrackExternallnspectionResqon tenll = new TrackExternallnspectionResqon();
        tenll.setReturnCode(0);
        tenll.setReturnMsg("succ");
        try {
            int errorCode = trackExternalInspection.getContent().getSignal().getErrorCode();
            //----------这里为外检成功
            if (errorCode == 0) {
                //TODO:此为外检成功----组装向wms申请库位的参数。
                WmsLocation wmsLocation = new WmsLocation();
                // 获取站点号//这个申请点还需要去根据站点编号映射一下
                String stationCode = trackExternalInspection.getContent().getStationCode();
                System.out.println(stationCode);
                String sc;
                //TODO:根据TES下发的站点号去查询表locationMapper，并设置给wms申请库位的申请点
                switch (stationCode) {
                    case "3003":
                        sc = "F1-RETURN-02";
                        LocationInfo oneByLocationCode = locationMapper.findOneByLocationCode(sc);
                        String backup = oneByLocationCode.getBackup();
                        wmsLocation.setApplyLocation(backup);
                        break;
                    case "3005":
                        sc = "F1-RETURN-03";
                        LocationInfo oneByLocationCode2 = locationMapper.findOneByLocationCode(sc);
                        String backup2 = oneByLocationCode2.getBackup();
                        wmsLocation.setApplyLocation(backup2);
                        break;
                      case "2005":
                          sc = "F4-RETURN-01";
                          LocationInfo oneByLocationCode3 = locationMapper.findOneByLocationCode(sc);
                          String backup3 = oneByLocationCode3.getBackup();
                          wmsLocation.setApplyLocation(backup3);
                         break;
                    default:
                        wmsLocation.setApplyLocation(stationCode);
                        break;
                }
                // 获取容器号
                String barCode = trackExternalInspection.getContent().getSignal().getBarCode();
                wmsLocation.setPalno(barCode);
                // 生成唯一ID
                String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                wmsLocation.setWcsId("WCS" + substring);
                wmsLocation.setHeight("M");
                //TODO:
                // 记录申请库位开始
                WmsLocationResqon wmsLocationResqon;
                SqlLogger.logSqlStart("申请库位", "WMS申请库位", wmsLocation);
                if(stationCode.equals("2005")||trackExternalInspection.getContent().getRobotID().equals("2005"))
                {
                    System.out.println("解析入库申请库位");
                    WmsLocationResqon wmsLocationResqon1 = wmsHttpService.WcapplyRcsLocation(wmsLocation);
                    if (wmsLocationResqon1.getMsgType().equals("S")) {
                        SqlLogger.logSqlSuccess("申请库位", System.currentTimeMillis());
                        // TODO:组装TES任务创建参数
                        TesTask task = new TesTask();
                        task.setWarehouseID("HETU");
                        task.setClientCode("WCS");
                        task.setRequestID(substring);
                        DesExt creaTaskDE = new DesExt();
                        creaTaskDE.setUnload(0);
//                      creaTaskDE.setPodFace(1.57F);
                        task.setDesExt(creaTaskDE);
                        String podID = trackExternalInspection.getContent().getSignal().getBarCode();
                        task.setPodID(podID);
                        //这里的目标点位还需要去映射，这个是巷道区域，所以需要去映射表位wcs_aisles
                        String destination2 = wmsLocationResqon1.getToLocation();
                        //判断申请站点是否为出口，如果是OUT01，则直接出库
                        if (destination2 != null && destination2.equals("OUT01")) {
                            task.setDestination(destination2);
                        }else {
                        System.out.println("申请巷道区域：" + destination2);
                        Aisles likeBywmsreference = aislesMapper.findLikeBywmsreference(destination2);
                        if (likeBywmsreference != null) {
                            task.setDestination(likeBywmsreference.getAislename());
                        }
                        else {
                            System.out.println("申请入库时，wms下发编号未查询到" + destination2);
                            //此为查询到wms下发的目的地查询不到的时候
                            WmsLocation wmsLocation3 = new WmsLocation();
                            wmsLocation3.setPalno(podID);
                            wmsLocation3.setWcsId("WCS" + substring);
                            wmsHttpService.RedoInMstore(wmsLocation3);
                            return tenll;
                        }}
                        task.setSrcType(1);
                        // 记录创建TES任务开始
                        // TODO:组装TES任务创建参数完成
                        SqlLogger.logSqlStart("创建TES任务", "发送任务到TES", task);
                        TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                        //----------此为创建TES任务成功，并且组装任务表参数写入数据库
                        if (taskResqon.getReturnUserMsg().equals("成功")) {
                            // 记录创建TES任务成功
                            SqlLogger.logSqlSuccess("创建TES任务", System.currentTimeMillis());
                            // TODO:组装任务数据表参数预写入数据库
                            WmsWcsTaskInfo taskModel = new WmsWcsTaskInfo();
                            String s = String.valueOf(taskResqon.getData().getTaskID());
                            String wmsId = wmsLocationResqon1.getWmsId();
                            taskModel.setThirdPartyTaskId(s);
                            taskModel.setWCSTaskId("WCS" + substring);
                            taskModel.setWMSTaskId(wmsId);
                            taskModel.setTaskStatus("assigned");//为待执行
                            String destination1 = wmsLocationResqon1.getToLocation();
                            Aisles likeBywmsreference1 = aislesMapper.findLikeBywmsreference(destination1);
                            String aislename = likeBywmsreference1.getAislename();
                            String stationCode1 = wmsLocation.getApplyLocation();//这里获取的是给wms的申请点，还需要去转化一次
                            LocationInfo oneByBackup = locationMapper.findOneByBackup(stationCode1);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            taskModel.setTaskCreaTime(currentTime);
                            taskModel.setTargetPosition(aislename);
                            taskModel.setStartPosition(oneByBackup.getLocationcode());
                            taskModel.setTaskType("成品库入库任务");
                            taskModel.setContainerCode(podID);
                            taskModel.setRcsOrTes("TES");
                            // TODO:组装任务数据表参数完成写入数据库
                            // 记录数据库操作
                            SqlLogger.logSqlStart("插入任务记录", "插入任务到数据库", taskModel);
                            taskMapper.add(taskModel);
                            SqlLogger.logSqlSuccess("插入任务记录", System.currentTimeMillis(), 1);
                            return tenll;
                        }
                        //----------这里就是需要将入库创建任务失败消息，原因发送通知与wms。
                        else {
                            // 记录创建TES任务失败
                            System.out.println(taskResqon.getReturnUserMsg());
                            SqlLogger.logSqlError("创建TES任务", "TES返回失败: " + taskResqon.getReturnUserMsg());
                            ExceptionLogger.logApplicationError("创建TES任务失败", "WarehouseLocation");
                            WmsLocation wmsLocation3 = new WmsLocation();
                            wmsLocation3.setPalno(podID);
                            wmsLocation3.setWcsId("WCS" + substring);
                            wmsHttpService.RedoInMstore(wmsLocation3);
                            return tenll;
                        }
                    }

                    //----------此为入库向wms申请库位失败.
                    else {
                        // 记录申请库位失败
                        SqlLogger.logSqlError("申请库位", "WMS返回失败: " + wmsLocationResqon1.getMsgType());
                        ExceptionLogger.logApplicationError("申请库位失败", "WarehouseLocation");
                        return tenll;
                    }
                }
                else {
                    WmsLocationResqon wmsLocationResqon3 = wmsHttpService.applyLocation(wmsLocation);
                    System.out.println("申请库位:" + wmsLocationResqon3);                            
                    // ----------此为入库向wms申请库位成功.
                    if (wmsLocationResqon3.getMsgType().equals("S")) {
                        SqlLogger.logSqlSuccess("申请库位", System.currentTimeMillis());
                        // TODO:组装TES任务创建参数
                        TesTask task = new TesTask();
                        task.setWarehouseID("HETU");
                        task.setClientCode("WCS");
                        task.setRequestID(substring);
                        DesExt creaTaskDE = new DesExt();
                        creaTaskDE.setUnload(0);
//                  creaTaskDE.setPodFace(1.57F);
                        task.setDesExt(creaTaskDE);
                        String podID = trackExternalInspection.getContent().getSignal().getBarCode();//托盘号
                        task.setPodID(podID);
                        //这里的目标点位还需要去映射，这个是巷道区域，所以需要去映射表位wcs_aisles
                        String destination2 = wmsLocationResqon3.getToLocation();
                        System.out.println("申请巷道区域：" + destination2);
                        Aisles likeBywmsreference = aislesMapper.findLikeBywmsreference(destination2);
                        if (likeBywmsreference != null) {
                            task.setDestination(likeBywmsreference.getAislename());
                        } else {
                            System.out.println("申请入库时，wms下发编号未查询到" + destination2);
                            //此为查询到wms下发的目的地查询不到的时候
                            WmsLocation wmsLocation3 = new WmsLocation();
                            wmsLocation3.setPalno(podID);
                            wmsLocation3.setWcsId("WCS" + substring);
                            wmsHttpService.RedoInMstore(wmsLocation3);
                            return tenll;
                        }
                        task.setSrcType(1);
                        // 记录创建TES任务开始
                        // TODO:组装TES任务创建参数完成
                        SqlLogger.logSqlStart("创建TES任务", "发送任务到TES", task);
                        TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                        //----------此为创建TES任务成功，并且组装任务表参数写入数据库
                        if (taskResqon.getReturnUserMsg().equals("成功")) {
                            // 记录创建TES任务成功
                            SqlLogger.logSqlSuccess("创建TES任务", System.currentTimeMillis());
                            // TODO:组装任务数据表参数预写入数据库
                            WmsWcsTaskInfo taskModel = new WmsWcsTaskInfo();
                            String s = String.valueOf(taskResqon.getData().getTaskID());
                            String wmsId = wmsLocationResqon3.getWmsId();
                            taskModel.setThirdPartyTaskId(s);
                            taskModel.setWCSTaskId("WCS" + substring);
                            taskModel.setWMSTaskId(wmsId);
                            taskModel.setTaskStatus("assigned");//为待执行
                            String destination1 = wmsLocationResqon3.getToLocation();
                            Aisles likeBywmsreference1 = aislesMapper.findLikeBywmsreference(destination1);
                            String aislename = likeBywmsreference1.getAislename();
                            String stationCode1 = wmsLocation.getApplyLocation();//这里获取的是给wms的申请点，还需要去转化一次
                            LocationInfo oneByBackup = locationMapper.findOneByBackup(stationCode1);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String currentTime = sdf.format(new Date());
                            taskModel.setTaskCreaTime(currentTime);
                            taskModel.setTargetPosition(aislename);
                            taskModel.setStartPosition(oneByBackup.getLocationcode());
                            taskModel.setTaskType("成品库入库任务");
                            taskModel.setContainerCode(podID);
                            taskModel.setRcsOrTes("TES");
                            // TODO:组装任务数据表参数完成写入数据库
                            // 记录数据库操作
                            SqlLogger.logSqlStart("插入任务记录", "插入任务到数据库", taskModel);
                            taskMapper.add(taskModel);
                            SqlLogger.logSqlSuccess("插入任务记录", System.currentTimeMillis(), 1);
                            return tenll;
                        }
                        //----------这里就是需要将入库创建任务失败消息，原因发送通知与wms。
                        else {
                            // 记录创建TES任务失败
                            System.out.println(taskResqon.getReturnUserMsg());
                            SqlLogger.logSqlError("创建TES任务", "TES返回失败: " + taskResqon.getReturnUserMsg());
                            ExceptionLogger.logApplicationError("创建TES任务失败", "WarehouseLocation");
                            WmsLocation wmsLocation3 = new WmsLocation();
                            wmsLocation3.setPalno(podID);
                            wmsLocation3.setWcsId("WCS" + substring);
                            wmsHttpService.RedoInMstore(wmsLocation3);
                            return tenll;
                        }
                    }

                    //----------此为入库向wms申请库位失败.
                    else {
                        // 记录申请库位失败
                        SqlLogger.logSqlError("申请库位", "WMS返回失败: " + wmsLocationResqon3.getMsgType());
                        ExceptionLogger.logApplicationError("申请库位失败", "WarehouseLocation");
                        return tenll;
                    }
                }
//                System.out.println("申请库位:" + wmsLocationResqon3);
                // ----------此为入库向wms申请库位成功.

            }
            //----------这里为外检失败
            else {

                return tenll;
            }
        }
        catch (Exception ex) {
            // 记录异常
            SqlLogger.logSqlException("WarehouseLocation", ex, "处理外检信息异常");
            ExceptionLogger.logException(ex, "外检信息处理方法执行异常");
            // 设置错误返回信息
            tenll.setReturnCode(1);
            tenll.setReturnMsg("处理失败: " + ex.getMessage());
            return tenll;
        }
    }

    @Override
    //此方法为接收到TES任务消息之后，向wms回传任务的状态
    public TrackExternallnspectionResqon ToWmsTaskInfo(TaskStatusMessage taskStatusMessage) {
        TrackExternallnspectionResqon tenll = new TrackExternallnspectionResqon();
        tenll.setReturnCode(0);
        tenll.setReturnMsg("succ");
        try {
            SqlLogger.logSqlStart("ToWmsTaskInfo", "开始处理TES任务消息");
            //当接收到任务的状态之后，需要拿到当前任务的id,当前状态。
            //拿到tes的id之后，去数据库查询当前对应的数据，拿到wms任务id,再去组装参数，回传给wms.
            int taskID = taskStatusMessage.getContent().getTaskID();
            String s = String.valueOf(taskID);//当前任务的id
            int status = taskStatusMessage.getContent().getStatus();
            String robotID = taskStatusMessage.getContent().getRobotID();
            SqlLogger.logSqlStart("查询任务信息", "SELECT * FROM wms_wcs_task_info WHERE id = ?", s);
            WmsWcsTaskInfo one = taskMapper.findOne(s);// 去数据库查询数据
            //这里需要去判断当前数据库对应任务是否为子任务，如果为子任务，就需要去看他的是否为最后一个任务，
            //如果没有不是子任务，说明这条任务是单条任务，直接给WMS推送即可。
            if (one.getWMSTaskId() != null && one.getProgress() != null) {
                //此任务为子任务
                //这里还需要去判断当前的子任务是否为最后一个任务。
                int maxSubTasksByType = taskConfig.getMaxSubTasksByType(one.getTaskType());
//                int maxSubTasksByTaskType = getMaxSubTasksByTaskType(one.getTaskType());
                int i = extractTaskNumber(one.getTaskType());
                if (maxSubTasksByType == i) {//如果相同，代表是最后一个任务，也需要去给wms发送任务完成信号
                    WmsStatusItem wmsStatusItem = AddToWmsTaskStuas(one, status, robotID, taskStatusMessage.getContent().getDesNodeID());
                    SqlLogger.logSqlStart("调用WMS接口", "开始向WMS发送任务状态");
                    SendWmsTaskInfo(wmsStatusItem, one);//调用WMS任务状态接口
                    return tenll;
                }
                else {
                    //如果不是最后一个任务，我们只需要去将对应任务的数据库数据的状态修改一下即可。
                    switch (status) {
                        case 2://任务开始
                            one.setAGVCode(robotID);
                            one.setTaskStatus("executing");//0为创建成功-待执行，1为执行中，2为执行完成
                            //这里还需要去根据robotID去查询我们的PS数据，把这个任务id分配给他
                            taskMapper.updete(one);
                            PSInfo psInfo = psMapper.findoneByRobotID(robotID);
                            psInfo.setCurrenttaskid(one.getWCSTaskId());
                            psMapper.updete(psInfo);
                            taskMapper.updete(one);
                            break;
                        case 4://任务完成
                            one.setAGVCode(null);
                            one.setTaskStatus("completed");//0为创建成功-待执行，1为执行中，2为执行完成
                            PSInfo psInfo2 = psMapper.findoneByRobotID(robotID);
                            psInfo2.setCurrenttaskid(null);
                            psMapper.updete(psInfo2);
                            taskMapper.updete(one);
                            break;
                        case 5://任务失败
                            one.setTaskStatus("failed");//0为创建成功-待执行，1为执行中，2为执行完成
                            taskMapper.updete(one);
                            SqlLogger.logSqlError("任务失败", "TES任务执行失败，状态码: EE");
                            break;
                        default:
                            SqlLogger.logSqlError("未知状态", "接收到未知的任务状态: " + status);
                            break;
                    }
                }

            }
            else {
               WmsStatusItem wmsStatusItem = AddToWmsTaskStuas(one, status, robotID, taskStatusMessage.getContent().getDesNodeID());
                SqlLogger.logSqlStart("调用WMS接口", "开始向WMS发送任务状态");
                //LJW
                SendWmsTaskInfo(wmsStatusItem, one);//调用WMS任务状态接口
                return tenll;
            }
            SqlLogger.logSqlSuccess("ToWmsTaskInfo", 0);
        } catch (Exception e) {
//            SqlLogger.logSqlException("ToWmsTaskInfo", e);
//            ExceptionLogger.logException(e, "ToWmsTaskInfo方法执行异常");
            return tenll;
        }
        SqlLogger.logSqlStart("返回给TES的参数", "" + tenll);
        return tenll;
    }

    /**
     * 此方法为调用WMS任务状态接口
     */
    private void SendWmsTaskInfo(WmsStatusItem wmsStatusItem, WmsWcsTaskInfo one) {
    try {
    System.out.println("发送给wms的任务状态参数跟状态" + wmsStatusItem + wmsStatusItem.getStatu());
    WmsStatusResponse wmsStatusResponse1 = wmsHttpService.sendTaskStatus(wmsStatusItem);
    System.out.println(wmsStatusResponse1);
    String msgType = wmsStatusResponse1.getMsgType();
    SqlLogger.logSqlResult("WMS响应", 1); // 假设返回1条结果

     //--------------这个是向wms发送状态失败的情况
     if (msgType.equals("E") || msgType.equals("F")) {//这里如果接收成功，那就不需要管他了。所以这里只有失败的情况
        ResendMessage resendMessage = new ResendMessage();
        String wmsTaskid1 = one.getWMSTaskId();
        String wcsTaskid1 = one.getWCSTaskId();
        String podid1 = one.getContainerCode();
        String statu = wmsStatusItem.getStatu();
        resendMessage.setWmsTaskid(wmsTaskid1);
        resendMessage.setWcsTaskid(wcsTaskid1);
        resendMessage.setTesTaskid(one.getThirdPartyTaskId());
        resendMessage.setPodid(podid1);
        resendMessage.setStatu(statu);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);
        resendMessage.setTkdat(formattedDate);
        resendMessage.setIsSuccessfully("0");
        SqlLogger.logSqlStart("保存重试消息", "INSERT INTO resend_message", resendMessage);
        retryMechanismMapper.add(resendMessage);
        SqlLogger.logSqlSuccess("保存重试消息", 0, 1);
        SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + one.getWMSTaskId());
    }
     //--------------这个是向wms发送状态成功的情况
     else {
        //如果成功了的话，还需要去看WMS回传的参数有无深度
        //如果有深度的话，就需要去再次创建任务，如果无，就不管
        if (wmsStatusResponse1.getAddre() != null) {
            String addre = wmsStatusResponse1.getAddre();
            //需要去根据深度查询对应的信息
            Storagelocations oneBywmsreference = storagelocationsMapper.findOneBywmsreference(addre);
            if (oneBywmsreference != null) {
                String locationname = oneBywmsreference.getLocationname();//获取到对应TES的点位，目标点位
                String wcsId = wmsStatusResponse1.getWcsId();//获取到wcs的上个任务的iD，去获取到任务参数，如托盘号，目的地
                WmsWcsTaskInfo oneByWcstaskId = taskMapper.findOneByWcstaskId(wcsId);
                String containerCode = oneByWcstaskId.getContainerCode();//托盘号
                String targetPosition = oneByWcstaskId.getTargetPosition();//上个任务的目标点，这个任务起始点
                String wmsTaskId = oneByWcstaskId.getWMSTaskId();//获取到这个任务的wmsid,新任务也是这个wmsid
                //组装参数，给TES发送搬运任务
                TesTask task = new TesTask();
                task.setWarehouseID("HETU");
                task.setClientCode("WCS");
                task.setSrcType(1);
                String substring =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                task.setRequestID(substring);
                DesExt creaTaskDE = new DesExt();
                creaTaskDE.setUnload(1);
//                        creaTaskDE.setPodFace(1.57F);
                task.setDesExt(creaTaskDE);
                String podID = containerCode;
                task.setPodID(podID);
                task.setDestination(locationname);
                System.out.println(task);
                TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                if (taskResqon.getReturnUserMsg().equals("成功")) {
                    //成功的话就把数据放入数据库中
                    WmsWcsTaskInfo taskModel = new WmsWcsTaskInfo();
                    String s = String.valueOf(taskResqon.getData().getTaskID());
                    taskModel.setThirdPartyTaskId(s);
                    taskModel.setWMSTaskId(wmsTaskId);
                    taskModel.setWCSTaskId("WCS" + substring);
                    taskModel.setTaskStatus("assigned");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    taskModel.setTaskCreaTime(currentTime);
                    taskModel.setTargetPosition(locationname);
                    taskModel.setStartPosition(targetPosition);
                    taskModel.setTaskType("05");
                    taskModel.setContainerCode(podID);
                    taskModel.setRcsOrTes("TES");
                    // 记录数据库操作
                    SqlLogger.logSqlStart("插入任务记录", "插入任务到数据库", taskModel);
                    taskMapper.add(taskModel);
                }
                else {
                    //TODO:如果创建小车任务失败的情况下，就将此任务放入我们的数据库中，让自动创建任务工具去创建任务即可
                    WmsWcsTaskInfo taskModel = new WmsWcsTaskInfo();
//                  String s = String.valueOf(taskResqon.getData().getTaskID());
//                  taskModel.setThirdPartyTaskId(s);
                    taskModel.setWMSTaskId(wmsTaskId);
                    taskModel.setWCSTaskId("WCS" + substring);
                    taskModel.setTaskStatus("pending");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    taskModel.setTaskCreaTime(currentTime);
                    taskModel.setTargetPosition(locationname);
                    taskModel.setStartPosition(targetPosition);
                    taskModel.setTaskType("05");
                    taskModel.setContainerCode(podID);
                    taskModel.setRcsOrTes("TES");
                    // 记录数据库操作
                    SqlLogger.logSqlStart("插入任务记录", "插入任务到数据库", taskModel);
                    taskMapper.add(taskModel);
                    System.out.println("入库位任务创建失败" + taskResqon.getReturnUserMsg());
                }
            }
            else {
                System.out.println("wms下发深度未查询到对应点位");
            }
        } else {
            System.out.println("任务完成wms回调成功");
        }
    }
     }
    catch (Exception e){
     //这里面应该去判断这个异常是不是超时，或者是其他
        // 判断是否是网络波动异常
        if (e instanceof org.springframework.web.client.ResourceAccessException ||
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.ConnectException) {
            // 网络波动：将当前的数据写入数据库中，等待下次发送，避免了这个消息丢失
            ResendMessage resendMessage = new ResendMessage();
            String wmsTaskid1 = one.getWMSTaskId();
            String wcsTaskid1 = one.getWCSTaskId();
            String podid1 = one.getContainerCode();
            String statu = wmsStatusItem.getStatu();
            resendMessage.setWmsTaskid(wmsTaskid1);
            resendMessage.setWcsTaskid(wcsTaskid1);
            resendMessage.setTesTaskid(one.getThirdPartyTaskId());
            resendMessage.setPodid(podid1);
            resendMessage.setStatu(statu);
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = now.format(formatter);
            resendMessage.setTkdat(formattedDate);
            resendMessage.setIsSuccessfully("0");
            SqlLogger.logSqlStart("由于网络原因保存重试消息", "INSERT INTO resend_message", resendMessage);
            retryMechanismMapper.add(resendMessage);
            SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + one.getWMSTaskId());
            return; // 结束方法，不继续执行
        }
      }
     }

    /**
     * 此方法为创建向wms发送任务状态的方法
     */
    private WmsStatusItem AddToWmsTaskStuas(WmsWcsTaskInfo one, int status, String robotID, String toLaction) {
        if (one != null) {//查询到任务，
            SqlLogger.logSqlSuccess("查询任务信息", 0, 1);
            String wmsTaskid = one.getWMSTaskId();//wms任务id
            String wcsTaskid = one.getWCSTaskId();//wcs任务id
            String podid = one.getContainerCode();//容器号
            //创建回传给wms的任务类型。
            WmsStatusItem wmsStatusItem = new WmsStatusItem();
            wmsStatusItem.setWmsId(wmsTaskid);
            wmsStatusItem.setWcsId(wcsTaskid);
            wmsStatusItem.setPalno(podid);//
            String taskType = one.getTaskType();//（0=入库，1为出库，2为移库）
            int i = wmsTaskid.indexOf("|");
            String substring = wmsTaskid.substring(0, i);
            System.out.println("wmsiD的前缀为:" + substring);
            String startPosition2 = one.getStartPosition();
            Aisles oneByaislename1 = aislesMapper.findOneByaislename(startPosition2);
            Aisles oneByaislename2 = aislesMapper.findOneByaislename(one.getTargetPosition());
            switch (status) {
                case 2://任务开始
                    if (substring.equals("O")) {//代表出库
                        if(oneByaislename1==null){
                            wmsStatusItem.setStatu("SO");
                        }
                        if(oneByaislename2!=null){
                            wmsStatusItem.setStatu("IS");
                        }
                    }
                    else {//代表入库
                        wmsStatusItem.setStatu("IS");
                        //这里还需要去判断一下这个点位是哪个站点，需要将对应站点的清除消息发送给plc
                        String startPosition = one.getStartPosition();//此为起始位置，如果起始位等于几个入库点的话，就让向对应的plc写入对应的数据
                        LocationInfo oneByLocationCode = locationMapper.findOneByLocationCode(startPosition);//查询到对应的数据
                        System.out.println(oneByLocationCode);
                        String startPosition1 = one.getStartPosition();
                        if (oneByLocationCode != null && oneByLocationCode.getLocationtype().equals("5")) {//去查看这个是否为空并且看是否为入库点
                            PlcSendMes plcSendMes = new PlcSendMes();
                            if (oneByLocationCode.getLocationcode().equals("F1-RETURN-02")) {//第三个入库点
                                plcSendMes.setPlcIp("192.168.30.104");
                                plcSendMes.setDbData("100");
                                plcSendMes.setMessType("CL");
                                plcSendMes.setUnitID(one.getContainerCode() + "************");
                                plcSendMes.setFromLocation("3003");
                                plcSendMes.setToLocation("3003");
                                plcSendMes.setCanWrite("01");
                                plcSendMes.setUnitHigh("0000");
                                plcSendMes.setUnitWeigh("000000");
                                plcSendMes.setReasonCode("00000000");
                                plcSendMesMapper.add(plcSendMes);
                            }
                            else if (oneByLocationCode.getLocationcode().equals("F1-RETURN-03")) {//第二个入库点
                                plcSendMes.setPlcIp("192.168.30.104");
                                plcSendMes.setDbData("100");
                                plcSendMes.setMessType("CL");
                                plcSendMes.setUnitID(one.getContainerCode() + "************");
                                plcSendMes.setFromLocation("3005");
                                plcSendMes.setToLocation("3005");
                                plcSendMes.setCanWrite("01");
                                plcSendMes.setUnitHigh("0000");
                                plcSendMes.setUnitWeigh("000000");
                                plcSendMes.setReasonCode("00000000");
                                plcSendMesMapper.add(plcSendMes);
                            }
                            else if (oneByLocationCode.getLocationcode().equals("F4-RETURN-01")) {//二楼解析区入库
                                plcSendMes.setPlcIp("192.168.30.100");
                                plcSendMes.setDbData("100");
                                plcSendMes.setMessType("CL");
                                plcSendMes.setUnitID(one.getContainerCode() + "************");
                                plcSendMes.setFromLocation("2005");
                                plcSendMes.setToLocation("2005");
                                plcSendMes.setCanWrite("01");
                                plcSendMes.setUnitHigh("0000");
                                plcSendMes.setUnitWeigh("000000");
                                plcSendMes.setReasonCode("00000000");
                                plcSendMesMapper.add(plcSendMes);
                                //这里只要进来了，需要去把对应的点位给清除掉 "jx-3"清除之后才能去创建下一个解析区入库任务
                                oneByLocationCode.setLocationcode("JX-3");
                                oneByLocationCode.setStatus("available");
                                locationMapper.updeteStatus(oneByLocationCode);
                            }

                        } else if (oneByLocationCode != null && ("PS-IN&OUT1".equals(startPosition1) || "PS-IN&OUT2".equals(startPosition1) || "PS-IN&OUT3".equals(startPosition1))) {
                            //就代表着这个为拣选入库任务已经开始了，需要去把这个点位释放掉
                            oneByLocationCode.setStatus("available");
                            locationMapper.updeteStatus(oneByLocationCode);
                        }
                    }
                    one.setAGVCode(robotID);
                    one.setTaskStatus("executing");//0为创建成功-待执行，1为执行中，2为执行完成
                    taskMapper.updete(one);
                    break;
                case 4://任务完成
                    //TODO:这个任务完成这里还需要去看是不是需要申请深度，如果要申请深度的话，还需要向wms发送IF,如果不需要的话就直接走下面操作
                    Aisles oneByaislename = aislesMapper.findOneByaislename(toLaction);
                    if (oneByaislename != null) {
                        //不等于null说明需要去请求深度
                        wmsStatusItem.setStatu("IF");
                        one.setTaskStatus("completed");//0为创建成功-待执行，1为执行中，2为执行完成
                        one.setAGVCode(null);
                        one.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        taskMapper.updete(one);
                    }
                    else {
                        if (substring.equals("O")) {//代表出库
                            //出库这里还需要去看看任务的目的地是否是拣选口，
                            String target = one.getTargetPosition();
                            if ("PS-IN&OUT1".equals(target) || "PS-IN&OUT2".equals(target) || "PS-IN&OUT3".equals(target)) {
                                wmsStatusItem.setStatu("FJ");
                                LocationInfo oneByLocationCode = locationMapper.findOneByLocationCode(target);
                                String backup = oneByLocationCode.getBackup();
                                wmsStatusItem.setPort(backup);
                            } else {
                                if(oneByaislename1!=null) {
                                    wmsStatusItem.setStatu("PF");
                                }
                                else {
                                    wmsStatusItem.setStatu("FO");
                                }
                                //如果不是就是正常的出库

                            }
                        }
                        else {//代表入库
                            wmsStatusItem.setStatu("PF");
                        }
                        one.setTaskStatus("completed");//0为创建成功-待执行，1为执行中，2为执行完成
                        one.setAGVCode(null);
                        one.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        PSInfo psInfo2 = psMapper.findoneByRobotID(robotID);
                        psInfo2.setCurrenttaskid(null);
                        psMapper.updete(psInfo2);
                        taskMapper.updete(one);
                        //任务完成之后，还需要去给plc发送消息，先要去判断是否为两个出库口，如果是那么就需要去写入plc消息表中
                        String targetPosition = one.getTargetPosition();
                        String containerCode = one.getContainerCode();
                        ChoosePlcSendMes(targetPosition, containerCode);
                    }
                    break;
                case 5://任务失败
//                    wmsStatusItem.setStatu("EE");//任务失败
//                    one.setTaskStatus("failed");//0为创建成功-待执行，1为执行中，2为执行完成
//                    taskMapper.updete(one);
//                    SqlLogger.logSqlError("任务失败", "TES任务执行失败，状态码: EE");
                    break;
                default:
                    SqlLogger.logSqlError("未知状态", "接收到未知的任务状态: " + status);
                    break;
            }

            wmsStatusItem.setTkdat(new Date());//到这里参数组装完毕，需要调用我们的wms消息接收接口。
            SqlLogger.logSqlStart("调用WMS接口", "开始向WMS发送任务状态");
            return wmsStatusItem;//返回给调用方法
        } else {//如果没有查询到，就写到sql日志里,没有当前任务，说明可能是数据库损坏，或者是数据库连接出现问题
            return new WmsStatusItem();
        }

    }

    //此方法为根据目标位置来给不同plc发送消息的方法
    private void ChoosePlcSendMes(String toLocation, String UnitID) {
        PlcSendMes plcSendMes = new PlcSendMes();
        switch (toLocation) {
            case "F1-RETURN-01"://当任务完成后且目标位置为当前这个点位的话，就需要向对应的plc发送消息，
                plcSendMes.setPlcIp("192.168.30.104");
                plcSendMes.setDbData("100");
                plcSendMes.setMessType("TO");
                plcSendMes.setUnitID(UnitID);
                plcSendMes.setFromLocation("3001");
                plcSendMes.setToLocation("3002");
                plcSendMes.setCanWrite("01");
                plcSendMes.setUnitHigh("0000");
                plcSendMes.setUnitWeigh("000000");
                plcSendMes.setReasonCode("00000000");
                plcSendMesMapper.add(plcSendMes);
                break;
            case "F1-RETURN-02":
                plcSendMes.setPlcIp("192.168.30.104");
                plcSendMes.setDbData("100");
                plcSendMes.setMessType("TO");
                plcSendMes.setUnitID(UnitID);
                plcSendMes.setFromLocation("3003");
                plcSendMes.setToLocation("3004");
                plcSendMes.setCanWrite("01");
                plcSendMes.setUnitHigh("0000");
                plcSendMes.setUnitWeigh("000000");
                plcSendMes.setReasonCode("00000000");
                plcSendMesMapper.add(plcSendMes);
                break;
            case "F1-RETURN-03":
                plcSendMes.setPlcIp("192.168.30.104");
                plcSendMes.setDbData("100");
                plcSendMes.setMessType("TO");
                plcSendMes.setUnitID(UnitID);
                plcSendMes.setFromLocation("3005");
                plcSendMes.setToLocation("3006");
                plcSendMes.setCanWrite("01");
                plcSendMes.setUnitHigh("0000");
                plcSendMes.setUnitWeigh("000000");
                plcSendMes.setReasonCode("00000000");
                plcSendMesMapper.add(plcSendMes);
                break;
            default:
                break;
        }

    }

    /**
     * 从任务类型中提取数字
     */
    private int extractTaskNumber(String taskType) {
        try {
            if (taskType == null) return 0;

            Pattern pattern = Pattern.compile(".*?(\\d+)");
            Matcher matcher = pattern.matcher(taskType);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    //此方法为接收到TES站点容器被清除之后，向wms回传任务的状态
    public TrackExternallnspectionResqon getSiteMessage(StationStatusMessage stationStatusMessage) {
        TrackExternallnspectionResqon trackExternallnspectionResqon = new TrackExternallnspectionResqon();
        trackExternallnspectionResqon.setReturnCode(1);
        trackExternallnspectionResqon.setReturnMsg("succ");
        if (stationStatusMessage.getContent().getOccupyStatus() == 3) {//等于3的时候说明，现在这里已经没有容器了
            //这里需要通知wms
            return trackExternallnspectionResqon;
        } else {//不等于3的时候说明，现在这里有容器了，或则有容器预占用
            return trackExternallnspectionResqon;
        }
    }
}


