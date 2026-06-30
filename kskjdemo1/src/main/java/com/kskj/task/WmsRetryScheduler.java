package com.kskj.task;

import cn.hutool.core.util.IdUtil;
import com.kskj.HttpService.TesHttpService;
import com.kskj.HttpService.WMSHttpService;
import com.kskj.mapper.RetryMechanismMapper;
import com.kskj.mapper.StoragelocationsMapper;
import com.kskj.mapper.TaskMapper;
import com.kskj.pojo.ResendMessage;
import com.kskj.pojo.Storagelocations;
import com.kskj.pojo.TES.DesExt;
import com.kskj.pojo.TES.TaskResqon;
import com.kskj.pojo.TES.TesTask;
import com.kskj.pojo.WMS.WmsStatusItem;
import com.kskj.pojo.WMS.WmsStatusResponse;
import com.kskj.pojo.WmsWcsTaskInfo;
import com.kskj.until.SqlLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
public class WmsRetryScheduler {
    @Autowired
    private RetryMechanismMapper retryMechanismMapper;
    @Autowired
    private WMSHttpService wMSHttpService;
    @Autowired
    private StoragelocationsMapper storagelocationsMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TesHttpService tesHttpService;
    private static final String WMS_IP = "192.168.30.129"; // wmsip
    // 每5秒执行一次
    @Scheduled(fixedRate = 50000)
    public void retryFailedMessages() {
        // 1. 首先ping WMS IP检查网络连通性
        if (!pingWmsIp()) {
            System.out.println("WMS网络不通，暂不重试");
            return; // 网络不通，直接返回
        }
        System.out.println("WMS网络通畅，开始重试失败消息");
        // 2. 网络通畅，查询所有待重试的消息（状态为0）
        List<ResendMessage> pendingMessages = retryMechanismMapper.findAll("0");

        if (pendingMessages.isEmpty()) {
            System.out.println("无待重试消息");
            return;
        }else {
            System.out.println("发现" + pendingMessages.size() + "条待重试消息");
        }

        // 3. 遍历并重试发送

        for (ResendMessage message : pendingMessages) {
            if(message.getStatu()==null){
                System.out.println("无待重试消息");
            }
            else {
                try {
                    // 组装发送参数
                    WmsStatusItem wmsStatusItem = new WmsStatusItem();
                    wmsStatusItem.setWmsId(message.getWmsTaskid());
                    wmsStatusItem.setWcsId(message.getWcsTaskid());
                    wmsStatusItem.setPalno(message.getPodid());
                    wmsStatusItem.setStatu(message.getStatu());
                    String tkdat = message.getTkdat();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(tkdat);
                    wmsStatusItem.setTkdat(date);
                    System.out.println("重试发送消息: " + message.getWmsTaskid());
                    // 发送到WMS
//                List<WmsStatusResponse> wmsStatusResponses = wmsHttpService.sendTaskStatus(wmsStatusItem);
                    WmsStatusResponse response = wMSHttpService.sendTaskStatus(wmsStatusItem);
                    System.out.println("重试消息发送成功");
                    message.setIsSuccessfully("1");
                    retryMechanismMapper.updete(message);
//                for (WmsStatusResponse response : wmsStatusResponses) {
                    if ("S".equals(response.getMsgType())) { // 假设"S"表示成功
                        if (response.getAddre() != null) {
                            String addre = response.getAddre();
                            //需要去根据深度查询对应的信息
                            Storagelocations oneBywmsreference = storagelocationsMapper.findOneBywmsreference(addre);
                            if (oneBywmsreference != null) {
                                String locationname = oneBywmsreference.getLocationname();//获取到对应TES的点位，目标点位
                                String wcsId = response.getWcsId();//获取到wcs的上个任务的iD，去获取到任务参数，如托盘号，目的地
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
                                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String currentTime = sdf2.format(new Date());
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
//                        String s = String.valueOf(taskResqon.getData().getTaskID());
//                        taskModel.setThirdPartyTaskId(s);
                                    taskModel.setWMSTaskId(wmsTaskId);
                                    taskModel.setWCSTaskId("WCS" + substring);
                                    taskModel.setTaskStatus("pending");
                                    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String currentTime = sdf3.format(new Date());
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
                        }
                    }
                    else {
                    }
                } catch (Exception e) {
                    System.out.println("发送异常，下次继续重试: " + message.getWmsTaskid() + ", 异常: " + e.getMessage());
                    // 发生异常，保持状态为0，下次继续重试
//                message.setLastRetryTime(LocalDateTime.now());
//                retryMechanismMapper.updateLastRetryTime(message);
                }
            }

        }
    }

    /**
     * Ping WMS IP地址检查网络连通性
     */
    private boolean pingWmsIp() {
        try {
            InetAddress address = InetAddress.getByName(WMS_IP);
            return address.isReachable(3000); // 3秒超时
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析字符串时间为Date对象
     */
//    private Date parseDate(String dateStr) {
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
//            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
//        } catch (Exception e) {
//            return new Date();
//        }
//    }

}
