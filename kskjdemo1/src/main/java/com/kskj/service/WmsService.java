package com.kskj.service;

import cn.hutool.core.util.IdUtil;



import com.kskj.HttpService.RcsHttpService;
import com.kskj.HttpService.TesHttpService;
import com.kskj.HttpService.WMSHttpService;
import com.kskj.mapper.*;

import com.kskj.pojo.*;
import com.kskj.pojo.WMS.*;
import com.kskj.pojo.rcs.RcsTaskCancel;
import com.kskj.pojo.rcs.RcsTaskCancelResonse;
import com.kskj.pojo.rcs.Sation.UnBind;
import com.kskj.pojo.rcs.Sation.UnBindResonse;
import com.kskj.pojo.rcs.Task.RcsCreaTask;
import com.kskj.pojo.rcs.Task.TaskResonse;
import com.kskj.pojo.TES.*;
import com.kskj.service.impl.IWmsService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WmsService implements IWmsService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TesHttpService tesHttpService;
    @Autowired
    private WMSHttpService wmsHttpService;
    @Autowired
    private RcsHttpService rcsHttpService;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private RcsAgvMapper rcsAgvMapper;
    @Autowired
    private AislesMapper aislesMapper;
    @Autowired
    private StoragelocationsMapper storagelocationsMapper;
    @Autowired
    private RcsLocataionMapper rcsLocataionMapper;
    @Autowired
    private RetryMechanismMapper retryMechanismMapper;
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    /**
     *此方法为出库任务
     */
    @Override
    public WmsTaskReponse[] AddTask(WmsTask[] wmsTasklist) {
        if(wmsTasklist == null || wmsTasklist.length == 0){
            // 返回一个包含单个错误响应的数组
            return new WmsTaskReponse[]{new WmsTaskReponse("500", "未能成功接收到任务组", "0")};
        }
        List<WmsTaskReponse> responses = new ArrayList<>();
        // 遍历所有任务，为每个任务生成响应
        for (int i = 0; i < wmsTasklist.length; i++) {
            WmsTask wmsTask = wmsTasklist[i];
            String taskId = wmsTask.getWmsId() != null ? wmsTask.getWmsId() : "未知任务";

            try {
                // 验证任务属性
                String validationError = validateTask(wmsTask);
                if (validationError != null) {
                    // 验证失败，为当前任务创建错误响应
                    responses.add(new WmsTaskReponse("500",
                            "任务" + taskId + ":" + validationError, "0"));
                    continue; // 跳过验证失败的任务，继续下一个
                }

                // 处理任务并添加响应
                //这个之前我们需要根据wms下发的库位进行查询对比，把真实的库位编号查询出来
                String fromLocation = wmsTask.getFromLocation();//起始位置
                String toLocation = wmsTask.getToLocation();//目标位置
                //这两个位置都需要去查询库位信息，查询对应的数据，如果没有，就用wms下发的库位信息，如果有就直接拿对应的数据

                LocationInfo oneByBackup = locationMapper.findOneByBackup(fromLocation);
                if(oneByBackup!=null){//不等于null说明拿到了对应的库位信息
                    wmsTask.setFromLocation(oneByBackup.getLocationcode());//赋值起始位置
                }
                //这里还需要去拿到我们起始的点位，直接查询表
                Storagelocations oneBywmsreference = storagelocationsMapper.findOneBywmsreference(fromLocation);
                if(oneBywmsreference!=null){//不等于null说明拿到了对应的巷道区域信息
                    wmsTask.setFromLocation(oneBywmsreference.getLocationname());//赋值起始位置
                }
                //这里查看是否为出库或者是拣选
                LocationInfo oneByBackup1 = locationMapper.findOneByBackup(toLocation);
                if(oneByBackup1!=null){//不等于null说明拿到了对应的库位信息
                    wmsTask.setToLocation(oneByBackup1.getLocationcode());//赋值目标位置
                }
                //上面查看是否为出库，下面查看是否盘点或移库
                Aisles likeBywmsreference = aislesMapper.findLikeBywmsreference(toLocation);
                if(likeBywmsreference!=null){//不等于null说明拿到了对应的库位信息
                    String replace = likeBywmsreference.getAislename().replace("\r\n", "<br>");
                    wmsTask.setToLocation(replace);//赋值目标位置
                }
                //TODO:调用任务处理方法，参数为wmsTask（这里不只是为出库，还有可能为盘点，移库等操作）
                WmsTaskReponse taskResponse = processTaskByType(wmsTask);
                responses.add(taskResponse);
            } catch (Exception e) {
                System.out.println(e);
                // 处理异常，为当前任务创建错误响应
                responses.add(new WmsTaskReponse("500",
                        "任务" + taskId + "处理异常:" + e.getMessage(), "0"));
            }
        }

        // 将响应列表转换为数组返回
        return responses.toArray(new WmsTaskReponse[0]);
    }
    /**
     * 验证任务属性
     */
    private String validateTask(WmsTask wmsTask) {
        if (isNullOrEmpty(wmsTask.getWmsId())) {
            return "WMSId属性为空";
        }
        if (isNullOrEmpty(wmsTask.getPalno())) {
            return "Palno属性为空";
        }
        if (isNullOrEmpty(wmsTask.getFromLocation())) {
            return "FromLocation属性为空";
        }
        if (isNullOrEmpty(wmsTask.getToLocation())) {
            return "ToLocation属性为空";
        }
        if (isNullOrEmpty(wmsTask.getIfPicking())) {
            return "IfPicking属性为空";
        }
        if (isNullOrEmpty(wmsTask.getNum())) {
            return "Num属性为空";
        }
        if (isNullOrEmpty(wmsTask.getTaskType())) {
            return "TaskType属性为空";
        }
        return null;
    }

    /**
     * 根据任务类型处理任务----主要逻辑
     */
    private WmsTaskReponse processTaskByType(WmsTask wmsTask) {
        switch(wmsTask.getTaskType()) {
            case "01":
                //入成品库任务
                return executeInboundFinishedGoodsTask(wmsTask);
            case "02":
                // 入灭菌缓存任务，下发对应的子任务
                return executeSterilizationBufferTask(wmsTask);
            case "03":
                // 灭菌任务，下发对应的子任务
                return executeSterilizationTask(wmsTask);
            case "04":
                // 入解析区任务，下发对应的子任务
                return executeAnalysisAreaTask(wmsTask);
            case "05":
                // TES系统任务
                return executeTesSystemTask(wmsTask);
            case "06":
                // AGV系统任务
                return executeAgvSystemTask(wmsTask);
            case "07":
                // 带灭菌缓存区去灭菌区任务
                return executeSterilizationTask2(wmsTask);
            default:
                return new WmsTaskReponse("F", "未知的任务类型: " + wmsTask.getTaskType(), "0");
        }
    }
    /**
     * 此方法为入成品库的父子任务创建------01
     */
    private WmsTaskReponse executeInboundFinishedGoodsTask(WmsTask wmsTask) {
        try {
            // 入成品库任务，下发对应的子任务
            //这里的创建子任务，并不是创建真的任务，有一些还要根据条件判断看能不能直接创建搬运任务。
            //不管有没有子任务都需要去创建主任务
            //TODO:获取wms任务参数
            WmsWcsTaskInfo wmsWcsTaskInfo = new WmsWcsTaskInfo();//创建主任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKido = new WmsWcsTaskInfo();//创建子任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKidt = new WmsWcsTaskInfo();//创建子任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKidr = new WmsWcsTaskInfo();//创建子任务对象
            String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
            String palno = wmsTask.getPalno();//获取托盘号
            String fromLocation = wmsTask.getFromLocation();//获取起始地
            String toLocation = wmsTask.getToLocation();//获取目标地
            String num = wmsTask.getNum();//获取优先级
            String ifPicking = wmsTask.getIfPicking();//是否拣选
            //TODO:设置数据库主任务任务表的参数
            String wcsid= "WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfo.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfo.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfo.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfo.setStartPosition(fromLocation);//设置起始位
            wmsWcsTaskInfo.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfo.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfo.setTaskType("入成品库任务");//设置任务类型
            wmsWcsTaskInfo.setPriority(num);//设置优先级
            wmsWcsTaskInfo.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            //最后将这个主任务创建到数据库
            taskMapper.add(wmsWcsTaskInfo);
            //直接创建定时任务来查询这些任务是不是要好一些。
            //创建完成之后就需要去创建我们的子任务了
            //TODO:子任务1：
            wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            //子任务不需要wmsid
            wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
            //起始位需要为当前位
            wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
            //目标位为接驳口位
            wmsWcsTaskInfoKido.setTargetPosition("1062122AA555155");//设置目标地
            wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoKido.setTaskType("入成品库子任务1");//设置任务类型
            wmsWcsTaskInfoKido.setPriority(num);//设置优先级
            wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
            wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            taskMapper.add(wmsWcsTaskInfoKido);
            //TODO:子任务2：
            wmsWcsTaskInfoKidt.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoKidt.setContainerCode(palno);//设置容器号
            //起始位需要为当前位
            wmsWcsTaskInfoKidt.setStartPosition("1062122AA555155");//设置起始位
            //目标位为接驳口位
            wmsWcsTaskInfoKidt.setTargetPosition("1062122AA555166");//设置目标地
            wmsWcsTaskInfoKidt.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoKidt.setTaskType("入成品库子任务2");//设置任务类型
            wmsWcsTaskInfoKidt.setPriority(num);//设置优先级
            wmsWcsTaskInfoKidt.setProgress(wcsid);//设置父任务的wcsid
            wmsWcsTaskInfoKidt.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            taskMapper.add(wmsWcsTaskInfoKidt);
            //TODO:子任务3：
            wmsWcsTaskInfoKidr.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoKidr.setContainerCode(palno);//设置容器号
            //起始位需要为当前位
            wmsWcsTaskInfoKidr.setStartPosition("1062122AA555166");//设置起始位
            //目标位为接驳口位
            wmsWcsTaskInfoKidr.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfoKidr.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoKidr.setTaskType("入成品库子任务3");//设置任务类型
            wmsWcsTaskInfoKidr.setPriority(num);//设置优先级
            wmsWcsTaskInfoKidr.setProgress(wcsid);//设置父任务的wcsid
            wmsWcsTaskInfoKidr.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            taskMapper.add(wmsWcsTaskInfoKidr);

            return new WmsTaskReponse("S", "入成品库任务处理成功", wmsTask.getWmsId());
        } catch (Exception e) {

            return new WmsTaskReponse("E", "入成品库任务处理失败: " + e.getMessage(), "0");
        }
    }
    /**
     * 此方法为入待灭菌区任务创建------02
     */
    private WmsTaskReponse executeSterilizationBufferTask(WmsTask wmsTask) {
        try {
            WmsWcsTaskInfo wmsWcsTaskInfoFa = new WmsWcsTaskInfo();
            WmsWcsTaskInfo wmsWcsTaskInfoKido = new WmsWcsTaskInfo();//创建子任务对象
            // WmsWcsTaskInfo wmsWcsTaskInfoKidt = new WmsWcsTaskInfo();//创建子任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKidr = new WmsWcsTaskInfo();//创建子任务对象
            String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
            String palno = wmsTask.getPalno();//获取托盘号
            String fromLocation = wmsTask.getFromLocation();//获取起始地
            String toLocation = wmsTask.getToLocation();//获取目标地
            String num = wmsTask.getNum();//获取优先级
            String ifPicking = wmsTask.getIfPicking();//是否拣选
            //TODO:设置数据库主任务任务表的参数
            String wcsid= "WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfoFa.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoFa.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfoFa.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfoFa.setStartPosition(fromLocation);//设置起始位
            wmsWcsTaskInfoFa.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfoFa.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoFa.setTaskType("入待灭菌区任务");//设置任务类型
            wmsWcsTaskInfoFa.setPriority(num);//设置优先级
            wmsWcsTaskInfoFa.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            //最后将这个主任务创建到数据库
            taskMapper.add(wmsWcsTaskInfoFa);
            //直接创建定时任务来查询这些任务是不是要好一些。
            //创建完成之后就需要去创建我们的子任务了
            //TODO:子任务1：
            wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            //子任务不需要wmsid
            wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
            //起始位需要为当前位
            wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
            //目标位为接驳口位
            wmsWcsTaskInfoKido.setTargetPosition("FH-T1");//设置目标地
            wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoKido.setTaskType("入待灭菌区子任务1");//设置任务类型
            wmsWcsTaskInfoKido.setPriority(num);//设置优先级
            wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
            wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            taskMapper.add(wmsWcsTaskInfoKido);
            // //TODO:子任务2：
            // wmsWcsTaskInfoKidt.setWCSTaskId("WCS" +String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            // wmsWcsTaskInfoKidt.setContainerCode(palno);//设置容器号
            // //起始位需要为当前位
            // wmsWcsTaskInfoKidt.setStartPosition("1062122AA555155");//设置起始位
            // //目标位为接驳口位
            // wmsWcsTaskInfoKidt.setTargetPosition("1062122AA555166");//设置目标地
            // wmsWcsTaskInfoKidt.setTaskStatus("pending");//设置初始任务状态
            // wmsWcsTaskInfoKidt.setTaskType("入待灭菌区子任务2");//设置任务类型
            // wmsWcsTaskInfoKidt.setPriority(num);//设置优先级
            // wmsWcsTaskInfoKidt.setProgress(wcsid);//设置父任务的wcsid
            // wmsWcsTaskInfoKidt.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            // taskMapper.add(wmsWcsTaskInfoKidt);
            //目前中间子任务2，不需要，子任务1完成时给PLC下发TO指令从一楼到二楼站点，到了二楼站点过后给AGV下发搬运到待灭菌缓存区的任务
            //TODO:子任务2：
            wmsWcsTaskInfoKidr.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoKidr.setContainerCode(palno);//设置容器号
            //起始位需要为当前位
            wmsWcsTaskInfoKidr.setStartPosition("FH-T2");//设置起始位
            //目标位为接驳口位
            wmsWcsTaskInfoKidr.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfoKidr.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfoKidr.setTaskType("入待灭菌区子任务2");//设置任务类型
            wmsWcsTaskInfoKidr.setPriority(num);//设置优先级
            wmsWcsTaskInfoKidr.setProgress(wcsid);//设置父任务的wcsid
            wmsWcsTaskInfoKidr.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            taskMapper.add(wmsWcsTaskInfoKidr);

            return new WmsTaskReponse("S", "入待灭菌缓存任务处理成功", wmsTask.getWmsId());
        } catch (Exception e) {

            return new WmsTaskReponse("E", "入待灭菌缓存任务处理失败: " + e.getMessage(), "0");
        }
    }
    /**
     * 此方法为灭菌任务任务创建------03
     */
    private WmsTaskReponse executeSterilizationTask(WmsTask wmsTask) {
        try {
            // 入成品库任务，下发对应的子任务
            //这里的创建子任务，并不是创建真的任务，有一些还要根据条件判断看能不能直接创建搬运任务。
            //不管有没有子任务都需要去创建主任务
            //TODO:获取wms任务参数
            WmsWcsTaskInfo wmsWcsTaskInfo = new WmsWcsTaskInfo();//创建主任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKido = new WmsWcsTaskInfo();//创建子任务对象
            // WmsWcsTaskInfo wmsWcsTaskInfoKidt = new WmsWcsTaskInfo();//创建子任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKidr = new WmsWcsTaskInfo();//创建子任务对象
            String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
            String palno = wmsTask.getPalno();//获取托盘号
            String fromLocation = wmsTask.getFromLocation();//获取起始地
            String toLocation = wmsTask.getToLocation();//获取目标地
            String num = wmsTask.getNum();//获取优先级
            String ifPicking = wmsTask.getIfPicking();//是否拣选
            //TODO:设置数据库主任务任务表的参数
            String wcsid= "WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfo.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfo.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfo.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfo.setStartPosition(fromLocation);//设置起始位
            wmsWcsTaskInfo.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfo.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfo.setTaskType("灭菌任务");//设置任务类型
            wmsWcsTaskInfo.setPriority(num);//设置优先级
            wmsWcsTaskInfo.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            //最后将这个主任务创建到数据库
            taskMapper.add(wmsWcsTaskInfo);
            //直接创建定时任务来查询这些任务是不是要好一些。
            //创建完成之后就需要去创建我们的子任务了
            //这里需要判断起始目的地是待灭菌缓存区还是灭菌缓存区，待灭菌缓存区需要下发子任务去短程提升机，灭菌缓存区直接去灭菌柜
            if (wmsWcsTaskInfo.getStartPosition().equals("")) {
                //灭菌区入灭菌柜
                //TODO:子任务1：
                wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                //子任务不需要wmsid
                wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
                //起始位需要为当前位
                wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
                //目标位为接驳口位
                wmsWcsTaskInfoKido.setTargetPosition(toLocation);//设置目标地，直接下发目的地为WMS下发的灭菌柜
                wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
                wmsWcsTaskInfoKido.setTaskType("灭菌子任务1");//设置任务类型
                wmsWcsTaskInfoKido.setPriority(num);//设置优先级
                wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
                wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                taskMapper.add(wmsWcsTaskInfoKido);
            }
            else{
                //待灭菌区直接入灭菌柜，需要拆分两条任务，使用短程提升机            
                //TODO:子任务1：
                wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                //子任务不需要wmsid
                wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
                //起始位需要为当前位
                wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
                //目标位为接驳口位
                wmsWcsTaskInfoKido.setTargetPosition("YR-T1");//设置目标地
                wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
                wmsWcsTaskInfoKido.setTaskType("灭菌子任务1");//设置任务类型
                wmsWcsTaskInfoKido.setPriority(num);//设置优先级
                wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
                wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                taskMapper.add(wmsWcsTaskInfoKido);
                // //TODO:子任务2：
                // wmsWcsTaskInfoKidt.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                // wmsWcsTaskInfoKidt.setContainerCode(palno);//设置容器号
                // //起始位需要为当前位
                // wmsWcsTaskInfoKidt.setStartPosition("1062122AA555155");//设置起始位
                // //目标位为接驳口位
                // wmsWcsTaskInfoKidt.setTargetPosition("1062122AA555166");//设置目标地
                // wmsWcsTaskInfoKidt.setTaskStatus("pending");//设置初始任务状态
                // wmsWcsTaskInfoKidt.setTaskType("灭菌子任务2");//设置任务类型
                // wmsWcsTaskInfoKidt.setPriority(num);//设置优先级
                // wmsWcsTaskInfoKidt.setProgress(wcsid);//设置父任务的wcsid
                // wmsWcsTaskInfoKidt.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                // taskMapper.add(wmsWcsTaskInfoKidt);
                //TODO:子任务2：
                wmsWcsTaskInfoKidr.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                wmsWcsTaskInfoKidr.setContainerCode(palno);//设置容器号
                //起始位需要为当前位
                wmsWcsTaskInfoKidr.setStartPosition("YR-T2");//设置起始位，提升机
                //目标位为接驳口位
                wmsWcsTaskInfoKidr.setTargetPosition(toLocation);//设置目标地，灭菌柜
                wmsWcsTaskInfoKidr.setTaskStatus("pending");//设置初始任务状态
                wmsWcsTaskInfoKidr.setTaskType("灭菌子任务2");//设置任务类型
                wmsWcsTaskInfoKidr.setPriority(num);//设置优先级
                wmsWcsTaskInfoKidr.setProgress(wcsid);//设置父任务的wcsid
                wmsWcsTaskInfoKidr.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                taskMapper.add(wmsWcsTaskInfoKidr);
            }

            return new WmsTaskReponse("S", "灭菌任务处理成功", wmsTask.getWmsId());
        } catch (Exception e) {

            return new WmsTaskReponse("E", "灭菌任务处理失败: " + e.getMessage(), "0");
        }
    }
    /**
     * 此方法为入解析区任务任务创建------04
     */
    private WmsTaskReponse executeAnalysisAreaTask(WmsTask wmsTask) {

        return new WmsTaskReponse("S", "入解析区任务处理成功", wmsTask.getWmsId());
    }
    /**
     * 用于随机选择拣选口的小工具
     */
    public static int allocatePosition(int num) {
        // 获取当前时间的毫秒数
        long currentTimeMillis = System.currentTimeMillis();
        // 取毫秒数的最后两位
        int lastTwoDigits = (int) (currentTimeMillis % 100);
        // 对num取余
        return lastTwoDigits % num;
    }
    /**
     * 此方法为无任务组（PS任务）任务创建------05
     */
    private WmsTaskReponse executeTesSystemTask(WmsTask wmsTask) {
        WmsWcsTaskInfo wmsWcsTaskInfoFa2 = new WmsWcsTaskInfo();
        //此为拣选任务
        if(wmsTask.getIfPicking().equals("1")&&wmsTask.getToLocation().equals("分拣口地址")){
            /*TODO:
             * 这里为拣选任务创建，单个拣选任务创建，但是wms可能会下发多个拣选任务需要创建，所以不仅需要去判断当前拣选口空闲的数量
             * 还需要去将分配了的但是车还没到位的拣选口改为预占用状态。
             * 第一步先去查询（对应的三个拣选口空闲的有几个）
             * 情况一：有三个空位，那么该如何分配（随机分配？）
             * 情况二：有两个空位，那么该如何分配（也是随机分配？）
             * 情况三：有一个空位，那么直接分配给当前任务？
             * 情况四：一个空位都没有，那么是否将此任务先挂起，再根据任务创建服务去查看哪个位置空出来就把那个位置分配给这个任务。
             */
            ArrayList<LocationInfo> byStatusAndLaneNumber = locationMapper.findByStatusAndLaneNumber("available", "分拣口地址");
            int size = byStatusAndLaneNumber.size();
            System.out.println("当前拣选口空闲数量为："+size);
            String wmsId = wmsTask.getWmsId();//获取wms任务id
            String taskType = wmsTask.getTaskType();//获取任务类型
            String palno = wmsTask.getPalno();//获取容器号
            String fromLocation = wmsTask.getFromLocation();//获取起始位置
            String toLocation = wmsTask.getToLocation();//获取目的地
            TesTask task = new TesTask();
            task.setWarehouseID("HETU");
            task.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
            task.setClientCode("WCS");
            task.setPriority(Integer.parseInt(wmsTask.getNum()));
            task.setSrcType(1);
            task.setPodID(palno);
            DesExt desExt = new DesExt();
            desExt.setUnload(1);
            task.setDesExt(desExt);
            switch (size) {
            case 3://有三个空位，那么该如何分配（随机分配？）
                int i = allocatePosition(3);
                LocationInfo locationInfo = byStatusAndLaneNumber.get(i);
                // 现在需要直接下发小车搬运任务
                task.setDestination(locationInfo.getLocationcode());
                TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                if(taskResqon != null &&taskResqon.getReturnMsg().equals("succ")){//如果成功了的话，就需要去添加到数据库
                    String substring =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    int taskID = taskResqon.getData().getTaskID();
                    String Tesid = String.valueOf(taskID);
                    wmsWcsTaskInfoFa2.setThirdPartyTaskId(Tesid);//设置Tes任务id
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("assigned");//任务状态--重要(待执行)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(locationInfo.getLocationcode());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    //只要成功的了话，就需要去将对应的拣选库位的状态改为占用
                    locationInfo.setStatus("occupied");
                    locationMapper.updeteStatus(locationInfo);
                }
                else {//如果失败，需要去将任务变为创建
                    String substring =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("pending");//任务状态--重要(待创建)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    wmsWcsTaskInfoFa2.setPriority(wmsTask.getNum());
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    System.out.println("拣选任务创建失败任务号为："+wmsId+"----原因为："+taskResqon.getReturnUserMsg());
                }
                //赋值给任务表对象
                break;
            case 2://有两个空位，那么该如何分配（也是随机分配？）
                int i2 = allocatePosition(2);
                LocationInfo locationInfo2 = byStatusAndLaneNumber.get(i2);
                task.setDestination(locationInfo2.getLocationcode());
                System.out.println("2----创建TES任务参数为"+task);
                TaskResqon taskResqon2 = tesHttpService.AddTesTask(task);
                if(taskResqon2 != null &&taskResqon2.getReturnMsg().equals("succ")){//如果成功了的话，就需要去添加到数据库

                    String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    int taskID = taskResqon2.getData().getTaskID();
                    String Tesid = String.valueOf(taskID);
                    wmsWcsTaskInfoFa2.setThirdPartyTaskId(Tesid);//设置Tes任务id
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("assigned");//任务状态--重要(待执行)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(locationInfo2.getLocationcode());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    //只要成功的了话，就需要去将对应的拣选库位的状态改为占用
                    locationInfo2.setStatus("occupied");
                    locationMapper.updeteStatus(locationInfo2);
                }
                else {//如果失败，需要去将任务变为创建
                    String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("pending");//任务状态--重要(待创建)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    wmsWcsTaskInfoFa2.setPriority(wmsTask.getNum());
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    System.out.println("拣选任务创建失败任务号为："+wmsId+"----原因为："+taskResqon2.getReturnUserMsg());
                }
                    break;
            case 1://有一个空位，那么直接分配给当前任务？
                LocationInfo locationInfo3 = byStatusAndLaneNumber.get(0);
                task.setDestination(locationInfo3.getLocationcode());
                TaskResqon taskResqon3 = tesHttpService.AddTesTask(task);
                if(taskResqon3 != null &&taskResqon3.getReturnMsg().equals("succ")){//如果成功了的话，就需要去添加到数据库
                    String substring =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    int taskID = taskResqon3.getData().getTaskID();
                    String Tesid = String.valueOf(taskID);
                    wmsWcsTaskInfoFa2.setThirdPartyTaskId(Tesid);//设置Tes任务id
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("assigned");//任务状态--重要(待执行)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(locationInfo3.getLocationcode());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    //只要成功的了话，就需要去将对应的拣选库位的状态改为占用
                    locationInfo3.setStatus("occupied");
                    locationMapper.updeteStatus(locationInfo3);
                }
                else {//如果失败，需要去将任务变为创建

                    String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("pending");//任务状态--重要(待创建)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    wmsWcsTaskInfoFa2.setPriority(wmsTask.getNum());
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    System.out.println("拣选任务创建失败任务号为："+wmsId+"----原因为："+taskResqon3.getReturnUserMsg());
                }
                break;
                default://一个空位都没有，那么是否将此任务先挂起，再根据任务创建服务去查看哪个位置空出来就把那个位置分配给这个任务。
                    //赋值给任务表对象
                    String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+substring);
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");//
                    wmsWcsTaskInfoFa2.setTaskType("成品库拣选任务");//任务类型
                    wmsWcsTaskInfoFa2.setTaskStatus("pending");//任务状态--重要(待创建)
                    wmsWcsTaskInfoFa2.setContainerCode(palno);
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);
                    wmsWcsTaskInfoFa2.setPriority(wmsTask.getNum());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = sdf.format(new Date());
                    wmsWcsTaskInfoFa2.setTaskCreaTime(currentTime);
                    System.out.println("无空闲拣选位置，任务挂起");
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    break;
            }
            return new WmsTaskReponse("S", "拣选任务处理成功",wmsId);
        }
        //此为整托出库
        //这里整托任务还需要分两种情况，一种情况为移库任务（直接下发），第二种为出库任务（直接写入数据库，写入数据库之后去将这个按照时间，批次号按批次下发）

        else {
            String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
            String palno = wmsTask.getPalno();//获取托盘号
            String fromLocation = wmsTask.getFromLocation();//获取起始地
            String toLocation = wmsTask.getToLocation();//获取目标地
            String num = wmsTask.getNum();//获取优先级
            String ifPicking = wmsTask.getIfPicking();//是否拣选
            String track=wmsTask.getTrack();
            if(wmsTask.getTrack() != null && !wmsTask.getTrack().trim().isEmpty()){
                //这里不等于null说明是出库需要管控
                //直接放到数据库中
                String wcsid =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
                wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);//设置wms任务号
                //这里因为没有第一时间创建任务所以说没有tesid
                wmsWcsTaskInfoFa2.setContainerCode(palno);//设置容器号
                wmsWcsTaskInfoFa2.setStartPosition(fromLocation);//设置起始位
                wmsWcsTaskInfoFa2.setTargetPosition(toLocation);//设置目标地
                wmsWcsTaskInfoFa2.setPriority(num);//设置优先级
                wmsWcsTaskInfoFa2.setTaskStatus("pending");//设置初始任务状态,为待执行
                wmsWcsTaskInfoFa2.setTaskType("成品库出库任务");//设置任务类型
                wmsWcsTaskInfoFa2.setRcsOrTes("TES");
                wmsWcsTaskInfoFa2.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                wmsWcsTaskInfoFa2.setTrack(track);
                //TODO:添加到数据库
                taskMapper.add(wmsWcsTaskInfoFa2);
                return new WmsTaskReponse("S", "WCS系统任务处理成功",wmsId);
            }
            else {
                //这里说明是移库，直接下发就行
                //这里为单个任务，我们只需要创建任务，然后调用对应的http请求，即可。
                //TODO:创建任务模版，并赋值参数
                TesTask task = new TesTask();
                task.setWarehouseID("HETU");
                task.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                task.setClientCode("WCS");
                task.setPriority(Integer.parseInt(num));
                task.setSrcType(1);
                task.setPodID(palno);
                TaskExt taskExt = new TaskExt();
                taskExt.setAutoToRest(1);
                DesExt desExt = new DesExt();
                String toLocation1 = wmsTask.getToLocation();
                Aisles oneByaislename = aislesMapper.findOneByaislename(toLocation1);
                if(oneByaislename!=null){
                    desExt.setUnload(0);
                }else {
                    desExt.setUnload(1);
                }
                task.setDesExt(desExt);
                task.setTaskExt(taskExt);
                task.setDestination(wmsTask.getToLocation());
                //TODO:发送请求，拿取回馈的任务号
                TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                if (taskResqon.getReturnMsg().equals("succ")) {
                    //代表任务发送创建成功
                    //TODO:设置数据库主任务任务表的参数
                    String wcsid =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);//设置wms任务号
                    int taskID = taskResqon.getData().getTaskID();
                    String Tesid = String.valueOf(taskID);
                    wmsWcsTaskInfoFa2.setThirdPartyTaskId(Tesid);//设置Tes任务id
                    wmsWcsTaskInfoFa2.setContainerCode(palno);//设置容器号
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);//设置起始位
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);//设置目标地
                    wmsWcsTaskInfoFa2.setPriority(num);//设置优先级
                    wmsWcsTaskInfoFa2.setTaskStatus("assigned");//设置初始任务状态,为待执行
                    wmsWcsTaskInfoFa2.setTaskType("成品库移库任务");//设置任务类型
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");
                    wmsWcsTaskInfoFa2.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                    //TODO:添加到数据库
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    return new WmsTaskReponse("S", "TES系统任务处理成功",wmsId);

                }
                else {
                    String wcsid =String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                    wmsWcsTaskInfoFa2.setWCSTaskId("WCS"+wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
                    wmsWcsTaskInfoFa2.setWMSTaskId(wmsId);//设置wms任务号
                    wmsWcsTaskInfoFa2.setContainerCode(palno);//设置容器号
                    wmsWcsTaskInfoFa2.setStartPosition(fromLocation);//设置起始位
                    wmsWcsTaskInfoFa2.setTargetPosition(toLocation);//设置目标地
                    wmsWcsTaskInfoFa2.setPriority(num);//设置优先级
                    wmsWcsTaskInfoFa2.setTaskStatus("pending");//设置初始任务状态,为待执行
                    wmsWcsTaskInfoFa2.setTaskType("成品库移库任务");//设置任务类型
                    wmsWcsTaskInfoFa2.setRcsOrTes("TES");
                    wmsWcsTaskInfoFa2.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                    //TODO:添加到数据库
                    taskMapper.add(wmsWcsTaskInfoFa2);
                    return new WmsTaskReponse("S", wmsTask.getWmsId()+"任务创建失败，wcs准备重试", wcsid);
                }
            }

        }
    }
    /**
     * 此方法为无任务组（AGV任务）任务创建------06
     */
    private WmsTaskReponse executeAgvSystemTask(WmsTask wmsTask) {
        //这里是无任务组，我需要去
        //这里为单个任务，我们只需要创建任务，然后调用对应的http请求，即可。
        WmsWcsTaskInfo wmsWcsTaskInfoFa = new WmsWcsTaskInfo();
        String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
        String palno = wmsTask.getPalno();//获取托盘号
        String fromLocation = wmsTask.getFromLocation();//获取起始地，现在这个这些起始地都是库里
        String toLocation = wmsTask.getToLocation();//获取目标地，都已经转换过了，为库位信息了
        String num = wmsTask.getNum();//获取优先级
        String ifPicking = wmsTask.getIfPicking();//是否拣选
        //直接在这里去将起始地和目标地给映射出结果后，加入数据库
        RcsLocataion oneBylocataion = rcsLocataionMapper.findOneBylocataion(fromLocation);//根据wms下发的起始地，去查询对应的巷道号
        String lanenumber = oneBylocataion.getLanenumber();//起始地
        //这个目的地需要去判断一下，需要分两种情况，一种为移库，一种为出库
        // 移库就是直接去下发任务即可，出库就去直接写入数据库即可
        if(toLocation.equals("OUT05")){
            //如果目的站等于IN03的话，对于AGV来说的话，就是出库
            //TODO:设置数据库主任务任务表的参数
            String wcsid= String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfoFa.setWCSTaskId("WCS" +wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoFa.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfoFa.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfoFa.setStartPosition(lanenumber);//设置起始位
            wmsWcsTaskInfoFa.setTargetPosition("JX-3");//设置目标地
            wmsWcsTaskInfoFa.setTaskStatus("pending");//设置初始任务状态,为待执行
            wmsWcsTaskInfoFa.setTaskType("解析完成入库任务");//设置任务类型
            wmsWcsTaskInfoFa.setRcsOrTes("RCS");
            wmsWcsTaskInfoFa.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            if(num==null){
                wmsWcsTaskInfoFa.setPriority("6");
            }else {
                wmsWcsTaskInfoFa.setPriority(num);
            }
            //TODO:添加到数据库
            taskMapper.add(wmsWcsTaskInfoFa);
            return new WmsTaskReponse("S", "" +
                    "处理成功", wmsTask.getWmsId());
        }
        else {
            //这里为移库，可直接调用任务下发接口进行创建
            //如果目的站等于IN03的话，对于AGV来说的话，就是出库
            //TODO:设置数据库主任务任务表的参数
            String wcsid= String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfoFa.setWCSTaskId("WCS" +wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfoFa.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfoFa.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfoFa.setStartPosition(lanenumber);//设置起始位
            wmsWcsTaskInfoFa.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfoFa.setTaskStatus("pending");//设置初始任务状态,为待执行
            wmsWcsTaskInfoFa.setTaskType("解析区移库任务");//设置任务类型
            wmsWcsTaskInfoFa.setRcsOrTes("RCS");
            wmsWcsTaskInfoFa.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            //TODO:添加到数据库
            wmsWcsTaskInfoFa.setPriority(num);
            taskMapper.add(wmsWcsTaskInfoFa);
            return new WmsTaskReponse("S", "" +
                    "处理成功", wmsTask.getWmsId());
        }
    }

    /**
     * 此方法为待灭菌缓存区入灭菌缓存区任务任务创建------07
     */
    private WmsTaskReponse executeSterilizationTask2(WmsTask wmsTask) {
        try {
            // 入成品库任务，下发对应的子任务
            //这里的创建子任务，并不是创建真的任务，有一些还要根据条件判断看能不能直接创建搬运任务。
            //不管有没有子任务都需要去创建主任务
            //TODO:获取wms任务参数
            WmsWcsTaskInfo wmsWcsTaskInfo = new WmsWcsTaskInfo();//创建主任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKido = new WmsWcsTaskInfo();//创建子任务对象
            // WmsWcsTaskInfo wmsWcsTaskInfoKidt = new WmsWcsTaskInfo();//创建子任务对象
            WmsWcsTaskInfo wmsWcsTaskInfoKidr = new WmsWcsTaskInfo();//创建子任务对象
            String wmsId = wmsTask.getWmsId();//获取wms传入的任务号
            String palno = wmsTask.getPalno();//获取托盘号
            String fromLocation = wmsTask.getFromLocation();//获取起始地
            String toLocation = wmsTask.getToLocation();//获取目标地
            String num = wmsTask.getNum();//获取优先级
            String ifPicking = wmsTask.getIfPicking();//是否拣选
            //TODO:设置数据库主任务任务表的参数
            String wcsid= "WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
            wmsWcsTaskInfo.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
            wmsWcsTaskInfo.setWMSTaskId(wmsId);//设置wms任务号
            wmsWcsTaskInfo.setContainerCode(palno);//设置容器号
            wmsWcsTaskInfo.setStartPosition(fromLocation);//设置起始位
            wmsWcsTaskInfo.setTargetPosition(toLocation);//设置目标地
            wmsWcsTaskInfo.setTaskStatus("pending");//设置初始任务状态
            wmsWcsTaskInfo.setTaskType("灭菌任务");//设置任务类型
            wmsWcsTaskInfo.setPriority(num);//设置优先级
            wmsWcsTaskInfo.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
            //最后将这个主任务创建到数据库
            taskMapper.add(wmsWcsTaskInfo);
            //直接创建定时任务来查询这些任务是不是要好一些。
            //创建完成之后就需要去创建我们的子任务了
            //待灭菌缓存区需要下发子任务去短程提升机入灭菌缓存区
                //TODO:子任务1：
                wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                //子任务不需要wmsid
                wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
                //起始位需要为当前位
                wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
                //目标位为接驳口位
                wmsWcsTaskInfoKido.setTargetPosition("YR-T1");//设置目标地
                wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
                wmsWcsTaskInfoKido.setTaskType("入灭菌缓存区子任务1");//设置任务类型
                wmsWcsTaskInfoKido.setPriority(num);//设置优先级
                wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
                wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                taskMapper.add(wmsWcsTaskInfoKido);
                // //TODO:子任务2：
                // wmsWcsTaskInfoKidt.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                // wmsWcsTaskInfoKidt.setContainerCode(palno);//设置容器号
                // //起始位需要为当前位
                // wmsWcsTaskInfoKidt.setStartPosition("1062122AA555155");//设置起始位
                // //目标位为接驳口位
                // wmsWcsTaskInfoKidt.setTargetPosition("1062122AA555166");//设置目标地
                // wmsWcsTaskInfoKidt.setTaskStatus("pending");//设置初始任务状态
                // wmsWcsTaskInfoKidt.setTaskType("灭菌子任务2");//设置任务类型
                // wmsWcsTaskInfoKidt.setPriority(num);//设置优先级
                // wmsWcsTaskInfoKidt.setProgress(wcsid);//设置父任务的wcsid
                // wmsWcsTaskInfoKidt.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                // taskMapper.add(wmsWcsTaskInfoKidt);
                //TODO:子任务2：
                wmsWcsTaskInfoKidr.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                wmsWcsTaskInfoKidr.setContainerCode(palno);//设置容器号
                //起始位需要为当前位
                wmsWcsTaskInfoKidr.setStartPosition("YR-T2");//设置起始位，提升机高位
                //目标位为接驳口位
                wmsWcsTaskInfoKidr.setTargetPosition(toLocation);//设置目标地，灭菌缓存区储位
                wmsWcsTaskInfoKidr.setTaskStatus("pending");//设置初始任务状态
                wmsWcsTaskInfoKidr.setTaskType("入灭菌缓存区子任务2");//设置任务类型
                wmsWcsTaskInfoKidr.setPriority(num);//设置优先级
                wmsWcsTaskInfoKidr.setProgress(wcsid);//设置父任务的wcsid
                wmsWcsTaskInfoKidr.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                taskMapper.add(wmsWcsTaskInfoKidr);

            return new WmsTaskReponse("S", "灭菌任务处理成功", wmsTask.getWmsId());
        } catch (Exception e) {

            return new WmsTaskReponse("E", "灭菌任务处理失败: " + e.getMessage(), "0");
        }
    }

    /**
     * 此方法为取消任务的方法
     */
    @Override
    public R CancelTask(String WCSTaskId) {
        //这种又三方接口的需要判断参数是否有，如果没有直接返回错误
        //这里我们需要去根据wcsTaskId去任务表查询我们任务是rcs还是tes的任务，还需要拿到我们对应的第三方任务的id
        //查询到了之后，走对应的接口调用。
        //成功之后返回给我们的调用者成功的信息。
        WmsWcsTaskInfo oneByWcstaskId = taskMapper.findOneByWcstaskId(WCSTaskId);
        String thirdPartyTaskId = oneByWcstaskId.getThirdPartyTaskId();//获取需要取消的任务的iD
        String rcsOrTes = oneByWcstaskId.getRcsOrTes();//获取任务的类型，是RCS/TES
        if(rcsOrTes.equals("RCS")){
         //如果是RCS的任务，就需要去走RCS的任务取消接口
            RcsTaskCancel rcsTaskCancel = new RcsTaskCancel();
            rcsTaskCancel.setRobotlaskCode(thirdPartyTaskId);
            rcsTaskCancel.setCancelType("DROP");//人工介入
            RcsTaskCancelResonse rcsTaskCancelResonse = rcsHttpService.CancelTask(rcsTaskCancel);//发送请求
            String code = rcsTaskCancelResonse.getCode();
            switch(code) {
                case "SUCCESS":
                    // 如果返回成功的话，就去返回给我们的上游成功
                    R.ok("任务取消成功");
                    break;
                case "Err_TaskFinished":
                   R.fail("任务取消失败，任务已结束");
                    break;
                case "Err_TaskNotFound":
                    R.fail("任务取消失败，任务找不到");
                    break;
                case "Err_TaskModifyReject":
                    R.fail("任务取消失败，任务当前无法变更");
                    break;
                case "Err_TaskTypeNotSupport":
                    R.fail("任务取消失败，新任务任务类型不支持");
                    break;
                case "Err_RobotGroupsNotMatch":
                    R.fail("任务取消失败，机器人资源组编号与新任务不匹配，无法调度");
                    break;
                case "Err_RobotCodesNotMatch":
                    R.fail("任务取消失败，机器人编号与新任务不匹配，无法调度");
                    break;
                default:
                    R.fail("任务取消失败，请检查网络或服务");
                    break;
            }
        }
        else if (rcsOrTes.equals("TES")) {
            //如果是TES的任务，就需要去走TES的任务取消接口
            //这里的id是int类型的，还需要转化一下
            int tesId = Integer.parseInt(thirdPartyTaskId);
            TesTaskCancel task = new TesTaskCancel();
            task.setWarehouseID("HETU");
            long timestamp = System.currentTimeMillis();
            task.setRequestID(String.valueOf(timestamp));
            task.setClientCode("WCS");
            task.setTaskID(tesId);
            task.setForce(0);
            task.setWithoutRunning(1);
            TesTaskCancelResqon tesTaskCancelResqon = tesHttpService.CancelTask(task);
            String returnMsg = tesTaskCancelResqon.getReturnMsg();
            if(returnMsg.equals("succ")){
                return R.ok("任务取消成功");
            }else {
                return  R.fail("任务取消失败");
            }
        }
        return R.fail("取消任务失败，需要查看任务的信息是否正确");
    }
/**
 * 此方法为PLC通知wcs货物被叉下，调用接口去将对应的站点清理
 */
    @Override
    public R ReleaseStations(String station) {
        System.out.println(station);
        ReleaseStationsHttp releaseStationsHttp = new ReleaseStationsHttp();
        ReleaseStation releaseStation = new ReleaseStation();
        String wcsid= "WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
        switch (station) {
            case "3002":
                releaseStationsHttp.setWcsId(wcsid);
                releaseStationsHttp.setOutPort("OUT01");
                releaseStation.setWarehouseID("HETU");
                releaseStation.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                releaseStation.setClientCode("WCS");
                releaseStation.setStationCode("F1-RETURN-01");
                break;
            case "3004":
                releaseStationsHttp.setWcsId(wcsid);
                releaseStationsHttp.setOutPort("KBJB01");
                releaseStation.setWarehouseID("HETU");
                releaseStation.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                releaseStation.setClientCode("WCS");
                releaseStation.setStationCode("F1-RETURN-02");
                break;
            case "3006":
                releaseStationsHttp.setWcsId(wcsid);
                releaseStationsHttp.setOutPort("KBJB02");
                releaseStation.setWarehouseID("HETU");
                releaseStation.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                releaseStation.setClientCode("WCS");
                releaseStation.setStationCode("F1-RETURN-03");
                break;
            default:
                break;
        }
        ReleaseStationsHttpResqon releaseStationsHttpResqon = wmsHttpService.ReleaseStationsHttp(releaseStationsHttp);
        System.out.println("清除wms站点消息返回"+releaseStationsHttpResqon.getMesText());
        System.out.println("清除wms站点消息wcsid="+wcsid);
        ReleaseStationResqon releaseStationResqon = tesHttpService.ReleaseStationMet(releaseStation);
        System.out.println("TES站点清除状态："+releaseStationResqon.getReturnUserMsg()+"站点号为：F1-RETURN-01"+"plc传过来的参数为："+station);
        return null;
    }

    @Override
    public R ReleaseRcsStation(UnBind unBind) {
        UnBindResonse unBindResonse = rcsHttpService.SationUnBind(unBind);//有plc告诉我容器已经不在位置上了

        return null;
    }

    /**
     * 此方法为plc服务发消息，灭菌完成向wms申请库位，和创建任务组
     */
    @Override
    public R plcAddTask(WmsTask wmsTask) {

        //plc发送消息接收到了之后，把wmsTask中的起始地，托盘号，任务类型拿到
        //先给wms申请目的地，申请成功后，再去创建这个任务组，创建之后就不用管了

        WmsLocation wmsLocation = new WmsLocation();//创建一个申请库位的对象

        String substring = String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
        wmsLocation.setWcsId("WCS" + substring);//赋值wcsid
        wmsLocation.setPalno(wmsTask.getPalno());
        wmsLocation.setHeight("M");
        // TODO:这里还需要去跟wms商量这个申请点是什么。
        wmsLocation.setApplyLocation(wmsTask.getFromLocation());
        //WmsLocationResqon wmsLocationResqon = wmsHttpService.applyRcsLocation(wmsLocation);
        //模拟wms返回数据
        WmsLocationResqon wmsLocationResqon=new WmsLocationResqon();
        wmsLocationResqon.setMsgType("S");
        wmsLocationResqon.setMsgText("申请成功");
        wmsLocationResqon.setToLocation("AGV-OUT-01");
        String toLocation = wmsLocationResqon.getToLocation();
        //todo:这里有修改
        if(wmsLocationResqon.getMsgType().equals("S")){
            //拿到目的地后toLocation，还需要去判断这个地方是直接出库还是去我们的解析区
            if(toLocation!=null&&wmsTask.getTaskType().equals("04")){
                //这里判断这个地方不是解析区
                if(wmsLocationResqon.getToLocation().equals("AGV-OUT-01")){
                    //这里为直接出库的情况，直接去创建一个出库任务即可
                    WmsWcsTaskInfo wmsWcsTaskInfoFa = new WmsWcsTaskInfo();
                    String palno = wmsTask.getPalno();//获取托盘号
                    String fromLocation = wmsTask.getFromLocation();//获取起始地
                    String num = wmsTask.getNum();//获取优先级
                    String wcsid= "WCS" +String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                        wmsWcsTaskInfoFa.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
                        wmsWcsTaskInfoFa.setWMSTaskId(wmsLocationResqon.getWmsId());//设置wms任务号
                        wmsWcsTaskInfoFa.setContainerCode(palno);//设置容器号
                        wmsWcsTaskInfoFa.setStartPosition(fromLocation);//设置起始位
                        wmsWcsTaskInfoFa.setTargetPosition(toLocation);//设置目标地
                        wmsWcsTaskInfoFa.setTaskStatus("pending");//设置初始任务状态
                        wmsWcsTaskInfoFa.setTaskType("灭菌器直接出库任务");//设置任务类型
                        wmsWcsTaskInfoFa.setRcsOrTes("RCS");
                        wmsWcsTaskInfoFa.setPriority(num);//设置优先级
                        wmsWcsTaskInfoFa.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                        //最后将这个主任务创建到数据库
                        taskMapper.add(wmsWcsTaskInfoFa);
                }
                else {
                    try {
                        //这里为入解析区
                        //这里还需要去查询这个wms下发的目的地是否存在
                        RcsLocataion oneBylocataion = rcsLocataionMapper.findOneBylocataion(toLocation);
                        if(!oneBylocataion.getLanenumber().equals("")&&oneBylocataion!=null){
                            toLocation=oneBylocataion.getLanenumber();
                        }else {
                            //这个时候需要去调用这个wms清除接口，
                            WmsLocation wmsLocation3 = new WmsLocation();
                            wmsLocation3.setPalno(wmsTask.getPalno());
                            wmsLocation3.setWcsId("WCS" + substring);
                            wmsHttpService.RedoInMstore(wmsLocation3);
                            System.out.println("入解析区wms下发的目的地为："+oneBylocataion.getLanenumber()+"wcs查询不到对应点位");
                        }
                        WmsWcsTaskInfo wmsWcsTaskInfoFa = new WmsWcsTaskInfo();
                        WmsWcsTaskInfo wmsWcsTaskInfoKido = new WmsWcsTaskInfo();//创建子任务对象
                        WmsWcsTaskInfo wmsWcsTaskInfoKidt = new WmsWcsTaskInfo();//创建子任务对象
                        String palno = wmsTask.getPalno();//获取托盘号
                        String fromLocation = wmsTask.getFromLocation();//获取起始地
                        String num = wmsTask.getNum();//获取优先级
                        //TODO:设置数据库主任务任务表的参数
                        String wcsid= "WCS" +String.valueOf(IdUtil.createSnowflake(1, 1).nextId());
                        wmsWcsTaskInfoFa.setWCSTaskId(wcsid);//生成一个WCS开头的8位数字唯一id(雪花算法)
                        wmsWcsTaskInfoFa.setWMSTaskId(wmsLocationResqon.getWmsId());//设置wms任务号
                        wmsWcsTaskInfoFa.setContainerCode(palno);//设置容器号
                        wmsWcsTaskInfoFa.setStartPosition(fromLocation);//设置起始位
                        wmsWcsTaskInfoFa.setTargetPosition(toLocation);//设置目标地
                        wmsWcsTaskInfoFa.setTaskStatus("pending");//设置初始任务状态
                        wmsWcsTaskInfoFa.setTaskType("入解析区任务");//设置任务类型
                        wmsWcsTaskInfoFa.setRcsOrTes("RCS");
                        wmsWcsTaskInfoFa.setPriority(num);//设置优先级
                        wmsWcsTaskInfoFa.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                        //最后将这个主任务创建到数据库
                        taskMapper.add(wmsWcsTaskInfoFa);
                        //直接创建定时任务来查询这些任务是不是要好一些。
                        //创建完成之后就需要去创建我们的子任务了
                        //TODO:子任务1：
                        wmsWcsTaskInfoKido.setWCSTaskId("WCS" + String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                        //子任务不需要wmsid
                        wmsWcsTaskInfoKido.setContainerCode(palno);//设置容器号
                        //起始位需要为当前位
                        wmsWcsTaskInfoKido.setStartPosition(fromLocation);//设置起始位
                        //目标位为接驳口位
                        wmsWcsTaskInfoKido.setTargetPosition("MJ_T1");//设置目标地
                        wmsWcsTaskInfoKido.setTaskStatus("pending");//设置初始任务状态
                        wmsWcsTaskInfoKido.setTaskType("入解析区子任务1");//设置任务类型
                        wmsWcsTaskInfoKido.setPriority(num);//设置优先级
                        wmsWcsTaskInfoKido.setProgress(wcsid);//设置父任务的wcsid
                        wmsWcsTaskInfoKido.setRcsOrTes("RCS");
                        wmsWcsTaskInfoKido.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                        taskMapper.add(wmsWcsTaskInfoKido);
                        //TODO:子任务2：
                        wmsWcsTaskInfoKidt.setWCSTaskId("WCS" +String.valueOf(IdUtil.createSnowflake(1, 1).nextId()));//生成一个WCS开头的8位数字唯一id(雪花算法)
                        wmsWcsTaskInfoKidt.setContainerCode(palno);//设置容器号
                        //起始位需要为当前位
                        wmsWcsTaskInfoKidt.setStartPosition("JX-T2");//设置起始位
                        //目标位为接驳口位
                        wmsWcsTaskInfoKidt.setTargetPosition(toLocation);//设置目标地
                        wmsWcsTaskInfoKidt.setTaskStatus("pending");//设置初始任务状态
                        wmsWcsTaskInfoKidt.setTaskType("入解析区子任务2");//设置任务类型
                        wmsWcsTaskInfoKidt.setPriority(num);//设置优先级
                        wmsWcsTaskInfoKidt.setProgress(wcsid);//设置父任务的wcsid
                        wmsWcsTaskInfoKidt.setRcsOrTes("RCS");
                        wmsWcsTaskInfoKidt.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));//设置任务创建时间
                        taskMapper.add(wmsWcsTaskInfoKidt);
                    }catch (Exception e){
                        System.out.println("异常");
                    }

                }
            }

            return R.ok("成功下发入解析区任务组");
        }
        else {
            System.out.println("向WMS申请灭菌区目标点位失败，需要重新申请");
            return R.ok("成功下发入解析区任务组");
        }

    }

    @Override
    public List<String> OutboundQuery(String locationtype) {
        //查询站点为出库流向的所有站点，并且返回给wms
        ArrayList<LocationInfo> oneByLocationType = locationMapper.findOneByLocationType(locationtype);
        List<String> list=new ArrayList<String>();
        if(oneByLocationType!=null){
            for (LocationInfo locationInfo : oneByLocationType) {
                String backup = locationInfo.getBackup();
                list.add(backup);
            }
        }
        return list;
    }

    //此为向wms补发任务状态接口
    @Override
    public R WmsResendMessage(ResendMessage resendMessage) {
        retryMechanismMapper.add(resendMessage);
        return R.ok("成功");
    }
}
