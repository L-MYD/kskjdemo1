package com.kskj.service;

import cn.hutool.core.util.IdUtil;
import com.kskj.HttpService.RcsHttpService;
import com.kskj.HttpService.WMSHttpService;
import com.kskj.HttpService.WcsHttpService;
import com.kskj.mapper.*;
import com.kskj.pojo.LocationInfo;
import com.kskj.pojo.PLC.PlcSendMes;
import com.kskj.pojo.rcs.Agv.AgvInfo;
import com.kskj.pojo.rcs.Sation.UnBind;
import com.kskj.pojo.rcs.reportertask.RcsReporterTask;
import com.kskj.pojo.rcs.reportertask.RcsReporterTaskResonse;
import com.kskj.pojo.rcs.reportertask.RcsReporterTaskResonseData;
import com.kskj.pojo.ResendMessage;
import com.kskj.pojo.WcsResonse;
import com.kskj.pojo.WMS.WmsStatusItem;
import com.kskj.pojo.WMS.WmsStatusResponse;
import com.kskj.pojo.WmsWcsTaskInfo;
import com.kskj.service.impl.IRcsService;
import com.kskj.until.SqlLogger;
import com.kskj.until.TaskConfig;

import org.luaj.vm2.ast.Stat.Break;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RcsService  implements IRcsService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskConfig taskConfig;
    @Autowired
    private WMSHttpService wmsHttpService;
    @Autowired
    private WcsHttpService wcsHttpService;
    @Autowired
    private RcsHttpService rcsHttpService;
    @Autowired
    private AgvMapper agvMapper;
    @Autowired
    private RetryMechanismMapper retryMechanismMapper;
    @Autowired
    private PlcSendMesMapper plcSendMesMapper;
    @Autowired
    private LocationMapper locationMapper;
    /*
    此方法接收RCS的任务消息并处理
     */
    @Override
    public RcsReporterTaskResonse RcsTaskToWms(RcsReporterTask rcsReporterTask) {
        RcsReporterTaskResonse rcsReporterTaskResonse = new RcsReporterTaskResonse();
        rcsReporterTaskResonse.setCode("SUCCESS");
        rcsReporterTaskResonse.setMessage("成功");
        RcsReporterTaskResonseData rcsReporterTaskResonseData1 = new RcsReporterTaskResonseData();
        rcsReporterTaskResonseData1.setRobotTaskcode(rcsReporterTask.getRobotTaskCode());
        rcsReporterTaskResonse.setData(rcsReporterTaskResonseData1);
        //接收到RCS的信息后，我们需要去任务表查询我们的当前任务
        //根据RCS的任务号来查询对应的数据和rcs任务。查询到任务看是否有wms任务id，如果没有代表是我们wcs自操作。
        //自操作就需要去管理库位。非自操作就需要去通知wms任务执行完成。
        String robotTaskCode = rcsReporterTask.getRobotTaskCode();//获取RCS的任务号
        String status = rcsReporterTask.getExtra().getValues().getMethod();//获取任务的状态
        //查询数据库任务表中对应任务数据
        WmsWcsTaskInfo rcs = taskMapper.findOneByThirdPartyTaskIdAndRcsOrTes(robotTaskCode, "RCS");
//        String agvCode = rcs.getAGVCode();
//        AgvInfo agvInfo = agvMapper.findoneByRobotID(agvCode);
        if(rcs==null){
            return rcsReporterTaskResonse;
        }
        else{
            String progress = rcs.getProgress();//此为当前子任务的父任务
            WmsWcsTaskInfo oneByWcstaskId = taskMapper.findOneByWcstaskId(progress);
            //如果查询到有的话，就需要去修改我们表中的数据了，并且需要给wms返回任务执行情况
            /*TODO: 现在查询到的信息，WCS这边只去管开始执行，和完成，
             *  拿到任务号之后，也需要去判断当前任务是否为子任务，如果为子任务那么，就需要去看这个子任务是否为最后一个
             *  不为子任务：
             *  如果不为子任务，直接通知WMS
             *  为子任务：
             *  如果为最后一个就去将父，子任务类型都变为已完成，然后再去通知WMS,
             *  如果不为最后一个：
             * 还需要去看这个是1还是2，
             * 如果是1：就直接去改变我们对应的任务信息，载具信息即可。
             * 如果是2：除了需要修改任务信息，载具信息，
             */
            //TODO:此为创建 ，设置发送WMS任务状态对象参数
            WmsStatusItem wmsStatusItem = new WmsStatusItem();//创建发送实体对象
            wmsStatusItem.setPalno(rcs.getContainerCode());//传入容器号
            wmsStatusItem.setTkdat(new Date());//设置时间
            wmsStatusItem.setWcsId(rcs.getWCSTaskId());//设置wcs的任务id
            if(rcs.getWMSTaskId()==null&&rcs.getProgress()!=null){
                //这里为子任务
                String taskType = rcs.getTaskType();
                int i = extractTaskNumber(taskType);
                String parentTaskType = taskType;
                int index = taskType.indexOf("子任务");
                if (index != -1) {
                    parentTaskType = taskType.substring(0, index) + "任务";
                }
                int maxSubTasksByType = taskConfig.getMaxSubTasksByType(parentTaskType);
                if(maxSubTasksByType==i){
                    //此条件为，是最后一个任务，通知wms即可，通知wms需要组装参数了
                    //还需要设置任务的状态
                    //TODO:此为修改数据库任务信息等
                    switch(status) {
                        case "start":
                            //此为任务开始
                            rcs.setTaskStatus("executing");
                            wmsStatusItem.setWmsId(oneByWcstaskId.getWMSTaskId());//传入wms的任务号
                            String wmsTaskId = oneByWcstaskId.getWMSTaskId();
                            int a = wmsTaskId.indexOf("|");
                            String substring = wmsTaskId.substring(0, a);
                            if (substring.equals("O")) {//代表出库
                                wmsStatusItem.setStatu("SO");
                            }
                            else {
                                wmsStatusItem.setStatu("IS");
                            }
                            oneByWcstaskId.setTaskStatus("executing");
                            taskMapper.updeteStaus(rcs);//修改子任务任务状态
                            taskMapper.updeteStaus(oneByWcstaskId);//修改父任务的状态
                            System.out.println("RCS最后任务开始");
                            WmsStatusResponse wmsStatusRespons2 = wmsHttpService.sendTaskStatus(wmsStatusItem);
                            String msgType2 = wmsStatusRespons2.getMsgType();
                            if (msgType2.equals("E") || msgType2.equals("F")) {//这里如果接收成功，那就不需要管他了。所以这里只有失败的情况
                                ResendMessage resendMessage = new ResendMessage();
                                String wmsTaskid1 = rcs.getWMSTaskId();
                                String wcsTaskid1 = rcs.getWCSTaskId();
                                String podid1 = rcs.getContainerCode();
                                String statu = wmsStatusItem.getStatu();
                                resendMessage.setWmsTaskid(wmsTaskid1);
                                resendMessage.setWcsTaskid(wcsTaskid1);
                                resendMessage.setTesTaskid(rcs.getThirdPartyTaskId());
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
                                SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + rcs.getWMSTaskId());

                            }
//                        agvMapper.updete(agvInfo);//修改小车任务id
                            break;
                        case "outbin":
                            //带容器出库
                   if(rcs.getStartPosition().equals("JX-T2")){
                       PlcSendMes plcSendMes4 = new PlcSendMes();
                       plcSendMes4.setPlcIp("192.168.30.80");
                       plcSendMes4.setDbData("4");
                       plcSendMes4.setMessType("CL");
                       plcSendMes4.setUnitID(rcs.getContainerCode() + "************");
                       plcSendMes4.setFromLocation("1304");
                       plcSendMes4.setToLocation("1304");
                       plcSendMes4.setCanWrite("01");
                       plcSendMes4.setUnitHigh("0000");
                       plcSendMes4.setUnitWeigh("000000");
                       plcSendMes4.setReasonCode("00000000");
                       plcSendMesMapper.add(plcSendMes4);
                       //这个地方还需要去清理一下这个JX-T2的占位
                       LocationInfo locationInfo = new LocationInfo();
                       locationInfo.setStatus("available");
                       locationInfo.setLocationcode("JX-T2");
                       locationMapper.updeteStatus(locationInfo);
                       System.out.println("RCS最后任务托盘举升");
                   }else if(rcs.getStartPosition().equals("YR-T1")){
                        PlcSendMes plcSendMes5 = new PlcSendMes();
                       plcSendMes5.setPlcIp("192.168.30.80");
                       plcSendMes5.setDbData("4");
                       plcSendMes5.setMessType("CL");
                       plcSendMes5.setUnitID(rcs.getContainerCode() + "************");
                       plcSendMes5.setFromLocation("1601");
                       plcSendMes5.setToLocation("1601");
                       plcSendMes5.setCanWrite("01");
                       plcSendMes5.setUnitHigh("0000");
                       plcSendMes5.setUnitWeigh("000000");
                       plcSendMes5.setReasonCode("00000000");
                       plcSendMesMapper.add(plcSendMes5);
                       //这个地方还需要去清理一下这个YR-T1的占位
                       LocationInfo locationInfo = new LocationInfo();
                       locationInfo.setStatus("available");
                       locationInfo.setLocationcode("YR-T1");
                       locationMapper.updeteStatus(locationInfo);
                       System.out.println("RCS最后任务托盘举升");
                    }
                            break;
                        case "end":
                            //任务完成
                            rcs.setTaskStatus("completed");
                            //任务完成后，我们也需要去将任务执行的AGV给清掉
                            rcs.setAGVCode(null);
                            rcs.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            // 也需要去将AGV的任务清除
                            oneByWcstaskId.setTaskStatus("completed");//修改父任务的状态
//                        agvInfo.setCurrenttaskId(null);//清除任务
                            wmsStatusItem.setWmsId(oneByWcstaskId.getWMSTaskId());//传入wms的任务号
                            oneByWcstaskId.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            String wmsTaskId2 = oneByWcstaskId.getWMSTaskId();
                            int a2 = wmsTaskId2.indexOf("|");
                            String substring2 = wmsTaskId2.substring(0, a2);
                            if (substring2.equals("O")) {//代表出库
                                wmsStatusItem.setStatu("FO");
                            }else {
                                wmsStatusItem.setStatu("PF");
                            }
                            taskMapper.updeteStaus(rcs);//修改子任务任务状态
                            taskMapper.updeteStaus(oneByWcstaskId);//修改父任务的状态
                            System.out.println("RCS最后任务完成");
//                        agvMapper.updete(agvInfo);//修改小车任务id
//                        List<WmsStatusResponse> wmsStatusResponses = wmsHttpService.sendTaskStatus(wmsStatusItem);//向wms发送任务状态
                            WmsStatusResponse wmsStatusRespons = wmsHttpService.sendTaskStatus(wmsStatusItem);
                            String msgType = wmsStatusRespons.getMsgType();
                            if (msgType.equals("E") || msgType.equals("F")) {//这里如果接收成功，那就不需要管他了。所以这里只有失败的情况
                                ResendMessage resendMessage = new ResendMessage();
                                String wmsTaskid1 = rcs.getWMSTaskId();
                                String wcsTaskid1 = rcs.getWCSTaskId();
                                String podid1 = rcs.getContainerCode();
                                String statu = wmsStatusItem.getStatu();
                                resendMessage.setWmsTaskid(wmsTaskid1);
                                resendMessage.setWcsTaskid(wcsTaskid1);
                                resendMessage.setTesTaskid(rcs.getThirdPartyTaskId());
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
                                SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + rcs.getWMSTaskId());

                            }
                            break;
                        default:
                            break;
                    }
                }
                else {
                    //此为不是最后一个任务，不需要通知wms.
                    /*TODO:还需要去修改库位的信息。
                     *  库位信息根据什么拿到呢？根据rcs数据中的起始点，去查询库位信息
                     *  查询到库位信息后，如果是开始执行，就不去管他
                     */
                    switch(status) {
                        case "start":
                            //需要将这个任务的状态变为开始执行
                            rcs.setTaskStatus("executing");
                            taskMapper.updeteStaus(rcs);
                            break;
                        case "outbin":

                            //在这里需要将对应的plc数据给清除掉，新增MJ-4,MJ-5
                            String startPosition = rcs.getStartPosition();
                            PlcSendMes plcSendMes = new PlcSendMes();
                            switch(startPosition) {
                                case "MJ-1":
                                    plcSendMes.setPlcIp("192.168.30.85");
                                    plcSendMes.setDbData("4");
                                    plcSendMes.setMessType("CL");
                                    plcSendMes.setUnitID(rcs.getContainerCode() + "************");
                                    plcSendMes.setFromLocation("1014");
                                    plcSendMes.setToLocation("1014");
                                    plcSendMes.setCanWrite("01");
                                    plcSendMes.setUnitHigh("0000");
                                    plcSendMes.setUnitWeigh("000000");
                                    plcSendMes.setReasonCode("00000000");
                                    plcSendMesMapper.add(plcSendMes);
                                    break;
                                case "MJ-2":
                                    plcSendMes.setPlcIp("192.168.30.85");
                                    plcSendMes.setDbData("4");
                                    plcSendMes.setMessType("CL");
                                    plcSendMes.setUnitID(rcs.getContainerCode() + "************");
                                    plcSendMes.setFromLocation("1018");
                                    plcSendMes.setToLocation("1018");
                                    plcSendMes.setCanWrite("01");
                                    plcSendMes.setUnitHigh("0000");
                                    plcSendMes.setUnitWeigh("000000");
                                    plcSendMes.setReasonCode("00000000");
                                    plcSendMesMapper.add(plcSendMes);
                                    break;
                                case "MJ-3":
                                    plcSendMes.setPlcIp("192.168.30.85");
                                    plcSendMes.setDbData("4");
                                    plcSendMes.setMessType("CL");
                                    plcSendMes.setUnitID(rcs.getContainerCode() + "************");
                                    plcSendMes.setFromLocation("1020");
                                    plcSendMes.setToLocation("1020");
                                    plcSendMes.setCanWrite("01");
                                    plcSendMes.setUnitHigh("0000");
                                    plcSendMes.setUnitWeigh("000000");
                                    plcSendMes.setReasonCode("00000000");
                                    plcSendMesMapper.add(plcSendMes);
                                    break;
                                case"MJ-4":
                                    plcSendMes.setPlcIp("192.168.30.85");
                                    plcSendMes.setDbData("4");
                                    plcSendMes.setMessType("CL");
                                    plcSendMes.setUnitID(rcs.getContainerCode() + "************");
                                    plcSendMes.setFromLocation("1022");
                                    plcSendMes.setToLocation("1022");
                                    plcSendMes.setCanWrite("01");
                                    plcSendMes.setUnitHigh("0000");
                                    plcSendMes.setUnitWeigh("000000");
                                    plcSendMes.setReasonCode("00000000");
                                    plcSendMesMapper.add(plcSendMes);
                                    break;
                                case"MJ-5":
                                    plcSendMes.setPlcIp("192.168.30.85");
                                    plcSendMes.setDbData("4");
                                    plcSendMes.setMessType("CL");
                                    plcSendMes.setUnitID(rcs.getContainerCode() + "************");
                                    plcSendMes.setFromLocation("1024");
                                    plcSendMes.setToLocation("1024");
                                    plcSendMes.setCanWrite("01");
                                    plcSendMes.setUnitHigh("0000");
                                    plcSendMes.setUnitWeigh("000000");
                                    plcSendMes.setReasonCode("00000000");
                                    plcSendMesMapper.add(plcSendMes);
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case "end":
                            //需要将这个任务状态变为已完成，还需要去清除AGV的数据
                            rcs.setTaskStatus("completed");
                            rcs.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            taskMapper.updeteStaus(rcs);
                            String targetPosition = rcs.getTargetPosition();                            
                            PlcSendMes plcSendMes6 = new PlcSendMes();
                            //如果目的地为,这里还需要去看是不是站点
                            switch(targetPosition){
                               case"MJ_T1":
                                   plcSendMes6.setPlcIp("192.168.30.80");
                                   plcSendMes6.setDbData("4");
                                   plcSendMes6.setMessType("TO");
                                   plcSendMes6.setUnitID(rcs.getContainerCode() + "************");
                                   plcSendMes6.setFromLocation("1301");
                                   plcSendMes6.setToLocation("1304");
                                   plcSendMes6.setCanWrite("01");
                                   plcSendMes6.setUnitHigh("0000");
                                   plcSendMes6.setUnitWeigh("000000");
                                   plcSendMes6.setReasonCode("00000000");
                                   plcSendMesMapper.add(plcSendMes6);
                                   break;
                               case"FH-T1":
                                    //目的地是发货区提升机，给PLC下发TO指令从一楼搬运到二楼
                                   plcSendMes6.setPlcIp("192.168.30.80");
                                   plcSendMes6.setDbData("4");
                                   plcSendMes6.setMessType("TO");
                                   plcSendMes6.setUnitID(rcs.getContainerCode() + "************");
                                   plcSendMes6.setFromLocation("1301");
                                   plcSendMes6.setToLocation("1304");
                                   plcSendMes6.setCanWrite("01");
                                   plcSendMes6.setUnitHigh("0000");
                                   plcSendMes6.setUnitWeigh("000000");
                                   plcSendMes6.setReasonCode("00000000");
                                   plcSendMesMapper.add(plcSendMes6);
                                   break;
                                case"YR-T1":
                                    //如果重点是灭菌区的提升机，调用PLC接口下发指令
                                    WcsResonse wcsreturn=wcsHttpService.sendPLCTask(rcs.getContainerCode());
                                    if (wcsreturn.getCode().equals("成功")) {
                                        //这个地方还需要去清理一下这个YR-T1的占位
                                        LocationInfo locationInfo = new LocationInfo();
                                        locationInfo.setStatus("available");
                                        locationInfo.setLocationcode("YR-T1");
                                        locationMapper.updeteStatus(locationInfo);
                                    }
                                    break;
                                default:
                                    break;
                               }
                            break;
                        default:
                            break;
                    }
                }
            }
            else {
                //这里为父任务
                //直接组装参数
                switch(status) {
                    case "start":
                        rcs.setTaskStatus("executing");
                        rcs.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        taskMapper.updeteStaus(rcs);
                        String wmsTaskId = rcs.getWMSTaskId();
                        int a = wmsTaskId.indexOf("|");
                        String substring = wmsTaskId.substring(0, a);
                        if (substring.equals("O")) {//代表出库
                             wmsStatusItem.setStatu("SO");
                        }else {
                            wmsStatusItem.setStatu("IS");
                        }
                        wmsStatusItem.setWmsId(rcs.getWMSTaskId());
                        WmsStatusResponse wmsStatusResponse = wmsHttpService.sendTaskStatus(wmsStatusItem);
                         //
                        String msgType = wmsStatusResponse.getMsgType();
                        if (msgType.equals("E") || msgType.equals("F")) {//这里如果接收成功，那就不需要管他了。所以这里只有失败的情况
                            ResendMessage resendMessage = new ResendMessage();
                            String wmsTaskid1 = rcs.getWMSTaskId();
                            String wcsTaskid1 = rcs.getWCSTaskId();
                            String podid1 = rcs.getContainerCode();
                            String statu = wmsStatusItem.getStatu();
                            resendMessage.setWmsTaskid(wmsTaskid1);
                            resendMessage.setWcsTaskid(wcsTaskid1);
                            resendMessage.setTesTaskid(rcs.getThirdPartyTaskId());
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

                            SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + rcs.getWMSTaskId());
                        }
                        //如果没发成功

                        break;
                    case "outbin":
                        break;
                    case "end":
                        rcs.setTaskStatus("completed");
                        rcs.setTaskCompletionTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        wmsStatusItem.setWmsId(rcs.getWMSTaskId());
                        taskMapper.updeteStaus(rcs);
                        //这里还需要去向plc发送TO指令
                        PlcSendMes plcSendMes = new PlcSendMes();
                        if(rcs.getTargetPosition().equals("JX-3")){
                            //等于这个的话说明是要解析区入库
                            plcSendMes.setPlcIp("192.168.30.100");
                            plcSendMes.setDbData("100");
                            plcSendMes.setMessType("TO");
                            plcSendMes.setUnitID("**********************"+rcs.getContainerCode());
                            plcSendMes.setFromLocation("2001");
                            plcSendMes.setToLocation("2005");
                            plcSendMes.setCanWrite("01");
                            plcSendMes.setUnitHigh("0000");
                            plcSendMes.setUnitWeigh("000000");
                            plcSendMes.setReasonCode("00000000");
                            plcSendMesMapper.add(plcSendMes);
                            UnBind unBind = new UnBind();
                            unBind.setCarrierCode(rcs.getContainerCode());
                            unBind.setSiteCode("JX-3");
                            rcsHttpService.SationUnBind(unBind);
                        }
                        String wmsTaskId3 = rcs.getWMSTaskId();
                        int a3 = wmsTaskId3.indexOf("|");
                        String substring3 = wmsTaskId3.substring(0, a3);
                        if (substring3.equals("O")) {//代表出库
                            wmsStatusItem.setStatu("FO");
                        }else {
                            wmsStatusItem.setStatu("PF");
                        }
                        //还需要去修改AGV的任务号

                        WmsStatusResponse wmsStatusResponse1 = wmsHttpService.sendTaskStatus(wmsStatusItem);
                        String msgType2 = wmsStatusResponse1.getMsgType();
                        if (msgType2.equals("E") || msgType2.equals("F")) {//这里如果接收成功，那就不需要管他了。所以这里只有失败的情况
                            ResendMessage resendMessage = new ResendMessage();
                            String wmsTaskid1 = rcs.getWMSTaskId();
                            String wcsTaskid1 = rcs.getWCSTaskId();
                            String podid1 = rcs.getContainerCode();
                            String statu = wmsStatusItem.getStatu();
                            resendMessage.setWmsTaskid(wmsTaskid1);
                            resendMessage.setWcsTaskid(wcsTaskid1);
                            resendMessage.setTesTaskid(rcs.getThirdPartyTaskId());
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

                            SqlLogger.logSqlError("WMS发送失败", "消息发送失败，已存入重试表，WMS任务ID: " + rcs.getWMSTaskId());
                        }

                        break;
                    default:

                        break;
                }
            }
            return rcsReporterTaskResonse;
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
}
