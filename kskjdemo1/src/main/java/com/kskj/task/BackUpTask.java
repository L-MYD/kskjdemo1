package com.kskj.task;

import cn.hutool.core.util.IdUtil;
import com.kskj.HttpService.RcsHttpService;
import com.kskj.HttpService.TesHttpService;
import com.kskj.mapper.AislesMapper;
import com.kskj.mapper.FaultInfoMapper;
import com.kskj.mapper.LocationMapper;
import com.kskj.mapper.TaskMapper;
import com.kskj.pojo.*;
import com.kskj.pojo.TES.DesExt;
import com.kskj.pojo.TES.TaskExt;
import com.kskj.pojo.TES.TaskResqon;
import com.kskj.pojo.TES.TesTask;
import com.kskj.pojo.WMS.WmsTaskReponse;
import com.kskj.pojo.rcs.Sation.Bind;
import com.kskj.pojo.rcs.Sation.BindResonse;
import com.kskj.pojo.rcs.Task.RcsCreaTask;
import com.kskj.pojo.rcs.Task.TaskResonse;
import com.kskj.pojo.rcs.Task.TaskTargetRoute;
import com.kskj.service.DbBackupService;
import com.kskj.until.TaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// 每60秒检查一次
@Component
public class BackUpTask {
    @Autowired
    private DbBackupService dbBackupService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private TesHttpService tesHttpService;
    @Autowired
    private RcsHttpService rcsHttpService;
    @Autowired
    private FaultInfoMapper faultInfoMapper;
    @Autowired
    private TaskConfig taskConfig;
    @Autowired
    private AislesMapper aislesMapper;

    //    @Scheduled(cron = "0 0/60 * * * ?")
//    cron = “秒 分 时 日 月 周 年0 21 17 17 4 ? 2024
    public void processScheduledStatusChanges() {
        dbBackupService.insertDataBack();
        System.out.println("备份成功");
//        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
//        Instant now = Instant.now();
////        System.out.println("++++++++++++++++"+now.toEpochMilli());
//        String valueOf = String.valueOf(now.toEpochMilli());
//        String nows = valueOf.substring(0, valueOf.length() - 3);
//        // 获取所有已到执行时间的任务
//        Set<String> tasksToExecute = zSetOps.rangeByScore("status_change_tasks", 0, now.toEpochMilli());
//        for (String taskJson : tasksToExecute) {
//            //将到了时间的对象拿出来
//            TNucleic task = deserializeTask(taskJson);
//            long time = task.getSatrTime().getTime();
//            String valueOfs = String.valueOf(time);
//            String startTime = valueOfs.substring(0, valueOfs.length() - 3);
//            long time1 = task.getEndTime().getTime();
//            String valueOfss = String.valueOf(time1);
//            String startTime1 = valueOfss.substring(0, valueOfss.length() - 3);
//            if (nows.equals(startTime)) {
//                // 修改数据到开始状态
//                System.out.println("我来了好厚米");
////                String s = redisTemplate.opsForZSet().randomMember("status_change_tasks");
//                //将字符串转化为TNucleic对象
//                Long id1 = task.getId();
////                TNucleic tnucleic = JSON.parseObject(s, TNucleic.class);
////                Long id = tnucleic.getId();
//                TNucleic tNucleic = new TNucleic();
//                tNucleic.setId(id1);
//                tNucleic.setState(1L);
//                // 数据库操作
//                tNucleicMapper.update(tNucleic);
//
//            }
//            else if (nows.equals(startTime1)) {
//                Long id1 = task.getId();
////                TNucleic tnucleic = JSON.parseObject(s, TNucleic.class);
////                Long id = tnucleic.getId();
//                TNucleic tNucleic = new TNucleic();
//                tNucleic.setId(id1);
//                tNucleic.setState(2L);
//                // 数据库操作
//                tNucleicMapper.update(tNucleic);
//            }
////             执行完毕后移除任务
//            zSetOps.remove("status_change_tasks", taskJson);
    }

    /**
     * 对任务列表进行排序（优先级降序，时间升序）并返回新集合
     *
     * @param tasks 待排序的任务列表
     * @return 排序后的新列表
     */
    public static List<WmsWcsTaskInfo> sortTasks(List<WmsWcsTaskInfo> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建新集合
        List<WmsWcsTaskInfo> sortedList = new ArrayList<>(tasks);

        sortedList.sort(new Comparator<WmsWcsTaskInfo>() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public int compare(WmsWcsTaskInfo t1, WmsWcsTaskInfo t2) {
                // 1. 按优先级降序
                int p1 = getPriorityValue(t1.getPriority());
                int p2 = getPriorityValue(t2.getPriority());

                if (p1 != p2) {
                    return Integer.compare(p2, p1); // 降序
                }

                // 2. 优先级相同，按创建时间升序
                try {
                    Date d1 = sdf.parse(t1.getTaskCreaTime());
                    Date d2 = sdf.parse(t2.getTaskCreaTime());
                    return d1.compareTo(d2); // 升序
                } catch (Exception e) {
                    return 0;
                }
            }

            private int getPriorityValue(String priority) {
                if (priority == null || priority.trim().isEmpty()) {
                    return 0;
                }
                try {
                    return Integer.parseInt(priority.trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        return sortedList;
    }

    /**
     * 对出库特殊规则任务列表进行排序（优先级降序，时间升序）并返回新集合
     *
     * @param tasks 待排序的任务列表
     * @return 排序后的新列表
     */
    public static List<WmsWcsTaskInfo> sortTaskst(List<WmsWcsTaskInfo> tasks) {
        //这里我们会传入所有为未创建的集合，但是我们需要先去按照Track进行分类
        //再按照优先级，创建时间进行排序
        //再去看当前任务中有没有出库任务为未完成的，并且有Track的，如果有就返回null,如果没有就返回排序后的第一个集合
     if(!tasks.isEmpty()){
         List<String> qc = new ArrayList<>();
         for (WmsWcsTaskInfo task : tasks) {
             String track = task.getTrack();
             qc.add(track);
         }
         List<String> distinctQc = new ArrayList<>(new HashSet<>(qc));//清除重复之后的数据
         Map<String, List<WmsWcsTaskInfo>> trackToTaskList = new HashMap<>();
         for (String track : distinctQc) {
             trackToTaskList.put(track, new ArrayList<>());
         }
         //现在创建了装任务的集合了，我需要去遍历拿到这个任务的数据
         for (WmsWcsTaskInfo task : tasks) {
             String track = task.getTrack();
             for (String s : distinctQc) {
                 if (track.equals(s)) {
                     List<WmsWcsTaskInfo> wmsWcsTaskInfos = trackToTaskList.get(track);
                     wmsWcsTaskInfos.add(task);
                 }
             }
         }
         //到这一步了，就已经将所有任务按照Track进行分类了，现在需要去拿到这些集合进行排序，
         // 遍历每个 track 的任务列表
         for (Map.Entry<String, List<WmsWcsTaskInfo>> entry : trackToTaskList.entrySet()) {
             List<WmsWcsTaskInfo> sortedList = sortTasks(entry.getValue()); // 排序后的新列表
             entry.setValue(sortedList); // 放回 Map
         }
         // 获取所有分组的任务列表（Collection<List<WmsWcsTaskInfo>>）
         Collection<List<WmsWcsTaskInfo>> allTaskLists = trackToTaskList.values();
         //再次创建一个集合
         List<WmsWcsTaskInfo> dr=new ArrayList<>();
         for (List<WmsWcsTaskInfo> allTaskList : allTaskLists) {
             WmsWcsTaskInfo wmsWcsTaskInfo = allTaskList.get(0);
             dr.add(wmsWcsTaskInfo);
         }
         List<WmsWcsTaskInfo> wmsWcsTaskInfos = sortTasks(dr);
         WmsWcsTaskInfo wmsWcsTaskInfo = wmsWcsTaskInfos.get(0);
         String track = wmsWcsTaskInfo.getTrack();
         return trackToTaskList.get(track);
     }
     else {
        return tasks;
     }
    }

    @Scheduled(cron = "*/25 * * * * ?")//每20秒查询并调度任务
    public void ScheduledDGCreaTask() {
        //此定时器为单个任务失败后，状态被修改为待创建的任务。
        // 成品库出库任务
        List<WmsWcsTaskInfo> pending1 = taskMapper.findByTaskStatusAndTaskType("pending", "成品库移库任务", "TES");
        // 成品库入库任务
        List<WmsWcsTaskInfo> pending2 = taskMapper.findByTaskStatusAndTaskType("pending", "成品库入库任务", "TES");
        // 成品库拣选任务
        List<WmsWcsTaskInfo> pending3 = taskMapper.findByTaskStatusAndTaskType("pending", "成品库拣选任务", "TES");
        List<WmsWcsTaskInfo> allPendingTasks = new ArrayList<>();//此为拣选任务、移库任务的集合
        List<WmsWcsTaskInfo> pending4 = taskMapper.findByTaskStatusAndTaskTypeA("pending", "成品库出库任务", "TES");
        List<WmsWcsTaskInfo> pending7 = taskMapper.findByTaskStatusAndTaskTypeAb("成品库出库任务", "TES");
        List<WmsWcsTaskInfo> wmsWcsTaskInfos2 = sortTaskst(pending4);
        allPendingTasks.addAll(pending1);
        allPendingTasks.addAll(pending2);
        allPendingTasks.addAll(pending3);
        System.out.println("下面为TES需要特殊处理的任务（出库）："+wmsWcsTaskInfos2);
        List<WmsWcsTaskInfo> wmsWcsTaskInfos = sortTasks(allPendingTasks);
        System.out.println("下面为TES不需要特殊处理的任务（拣选，移库，指定容器出库）：" + wmsWcsTaskInfos);
        List<WmsWcsTaskInfo> pending5 = taskMapper.findByTaskStatusAndTaskType("pending", "解析完成入库任务", "RCS");//此为RCS单个任务组
        List<WmsWcsTaskInfo> pending6 = taskMapper.findByTaskStatusAndTaskType("pending", "解析区移库任务", "RCS");//此为RCS单个任务组
        List<WmsWcsTaskInfo> pending8 = taskMapper.findByTaskStatusAndTaskType("pending", "灭菌区直接出库任务", "RCS");//此为RCS单个任务组
        List<WmsWcsTaskInfo> allPendingTasks2 = new ArrayList<>();//此为RCS的单个任务集合
        List<WmsWcsTaskInfo> allPendingTasks3 = new ArrayList<>();//此为RCS的单个任务集合
        allPendingTasks2.addAll(pending5);
        allPendingTasks2.addAll(pending6);
        allPendingTasks2.addAll(pending8);
        List<WmsWcsTaskInfo> wmsWcsTaskInfos1 = sortTasks(allPendingTasks2);//此为RCS的单个任务集合
        List<WmsWcsTaskInfo> wmsWcsTaskInfos3 = sortTasks(allPendingTasks3);//解析区直接出库任务
        System.out.println("下面为需创建RCS不需要特殊处理的任务：" + wmsWcsTaskInfos1);
        //在这之前还得去根据优先级来分配

        //灭菌区直接出库
        if (!wmsWcsTaskInfos3.isEmpty()) {
            System.out.println("下一个需要创建的出库任务组：" + wmsWcsTaskInfos3);
            //查询站点是否被占用，创建特殊任务出库任务组
            LocationInfo oneByLocationCode = locationMapper.findOneByLocationCode("AGV-OUT-01");
            for (WmsWcsTaskInfo wmsWcsTaskInfo3 : wmsWcsTaskInfos3) {
                if (wmsWcsTaskInfo3.getTargetPosition().equals("AGV-OUT-01")) {
                    //判断AGV-OUT-01站点是否为空闲，如果为空闲就去创建任务，如果不为空闲就不去创建任务
                     if (oneByLocationCode != null && oneByLocationCode.getStatus().equals("available")) {
                        RcsCreaTask rcsCreaTask = new RcsCreaTask();//创建任务对象
                        TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];//创建两个目标点
                        TaskTargetRoute taskTargetRoute = new TaskTargetRoute();//目标点位1
                        TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();//目标点位2
                        //起始位
                        taskTargetRoute.setType("STACK");
                        taskTargetRoute.setCode(wmsWcsTaskInfo3.getStartPosition());
                        taskTargetRoute.setSeq(0);
                        //目标位
                        taskTargetRoute2.setType("SITE");
                        taskTargetRoute2.setCode(wmsWcsTaskInfo3.getTargetPosition());
                        taskTargetRoute2.setSeq(0);
                        //任务模板
                        rcsCreaTask.setTaskType("A2");
                        taskTargetRoutes[0] = taskTargetRoute;
                        taskTargetRoutes[1] = taskTargetRoute2;
                        rcsCreaTask.setTargetRoute(taskTargetRoutes);
                        //优先级
                        int i1 = Integer.parseInt(wmsWcsTaskInfo3.getPriority());
                        rcsCreaTask.setInitPriority(i1);
                        //发送容器搬运任务
                        TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);
                        if (taskResonse.getMessage().equals("成功")) {
                            //如果下发成功后，需要去把这个JX-3位置占住
                            //还需要去将当前任务状态改为待执行，把RCS的任务写入
                            wmsWcsTaskInfo3.setTaskStatus("assigned");
                            wmsWcsTaskInfo3.setThirdPartyTaskId(taskResonse.getData().getRobotTaskCode());//设置RCS的任务号
                            taskMapper.updeteStaus(wmsWcsTaskInfo3);//更新任务状态
                            //任务成功之后将次占位变为占用
                            oneByLocationCode.setStatus("occupied");
                            oneByLocationCode.setContainercode(wmsWcsTaskInfo3.getContainerCode());
                            locationMapper.updeteStatus(oneByLocationCode);
                        } else {
                            System.out.println("下发直接出库任务失败，原因：" + taskResonse.getMessage());
                        }
                    } else {

                        System.out.println("AGV-OUT-01站点不为空闲，不下发灭菌区直接出库任务");
                    }
                }
            }
        }
        if(!wmsWcsTaskInfos2.isEmpty()){
            //创建之前还需要去看有没有出库任务带Track的字段的任务，没有完成的，如果有没有完成的就不下发
            //这里拿到的数据就是需要创建的，按照优先级，创建时间来排序了的、
            //直接调用tes任务创建接口，创建任务即可
            if(pending7.isEmpty()){//如果为空才去创建任务
                for (WmsWcsTaskInfo wmsWcsTaskInfo : wmsWcsTaskInfos2) {
                    String priority = wmsWcsTaskInfo.getPriority();
                    String containerCode = wmsWcsTaskInfo.getContainerCode();
                    String targetPosition = wmsWcsTaskInfo.getTargetPosition();
                    //TODO:创建任务模版，并赋值参数
                    TesTask task = new TesTask();
                    task.setWarehouseID("HETU");
                    task.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                    task.setClientCode("WCS");
                    task.setPriority(Integer.parseInt(priority));
                    task.setSrcType(1);
                    task.setPodID(containerCode);
                    TaskExt taskExt = new TaskExt();
                    taskExt.setAutoToRest(1);
                    DesExt desExt = new DesExt();
                    String toLocation1 = targetPosition;
                    Aisles oneByaislename = aislesMapper.findOneByaislename(toLocation1);
                    if(oneByaislename!=null){
                        desExt.setUnload(0);
                    }else {
                        desExt.setUnload(1);
                    }
                    task.setDesExt(desExt);
                    task.setTaskExt(taskExt);
                    task.setDestination(toLocation1);
                    //TODO:发送请求，拿取回馈的任务号
                    TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                    if (taskResqon.getReturnMsg().equals("succ")) {
                        //代表任务发送创建成功
                        //TODO:设置数据库主任务任务表的参数
                        int taskID = taskResqon.getData().getTaskID();
                        String Tesid = String.valueOf(taskID);
                        wmsWcsTaskInfo.setThirdPartyTaskId(Tesid);//设置Tes任务id
                        wmsWcsTaskInfo.setTaskStatus("assigned");//设置初始任务状态,为待执行
                        //TODO:添加到数据库
                        taskMapper.updetet(wmsWcsTaskInfo);
                    }
                    else {
                        //如果创建失败，这个时候就需要去给人工说创建任务失败了
                        FaultInfo faultInfo = new FaultInfo();//创建页面警告对象
                        faultInfo.setFaulttype("出库任务创建失败报警");
                        faultInfo.setFaultmessage("失败原因："+taskResqon.getReturnUserMsg());
                        faultInfo.setIsshow("1");
                    }
                }
            }
            else {//不为空就说明还有任务在执行
                System.out.println("当前有受管控的任务未结束");
                System.out.println("下一个需要创建的任务组："+wmsWcsTaskInfos2);
            }

        }
        if (!wmsWcsTaskInfos.isEmpty()) {//此为TES任务
            for (WmsWcsTaskInfo wmsWcsTaskInfo : wmsWcsTaskInfos) {
                //TODO:此为拣选任务
                if (wmsWcsTaskInfo.getTargetPosition().equals("分拣口地址")) {
                    //此刻为拣选任务，需要去查看3个拣选口是否有空位
                    ArrayList<LocationInfo> byStatusAndLaneNumber = locationMapper.findByStatusAndLaneNumber("available", "分拣口地址");
                    int size = byStatusAndLaneNumber.size();
                    System.out.println("当前拣选口空闲数量为：" + size);
                    //大于0，说明有空位
                    if (size > 0) {
                        LocationInfo locationInfo = byStatusAndLaneNumber.get(0);//直接取第一个拣选点
//                    wmsWcsTaskInfo   ----为当前任务
                        //创建TES任务参数
                        TesTask task = new TesTask();
                        task.setWarehouseID("HETU");
                        task.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                        task.setClientCode("WCS");
                        if (wmsWcsTaskInfo.getPriority() == null) {
                            task.setPriority(Integer.parseInt("7"));
                        } else {
                            task.setPriority(Integer.parseInt(wmsWcsTaskInfo.getPriority()));
                        }
//                        task.setPriority(Integer.parseInt(wmsWcsTaskInfo.getPriority()));
                        task.setSrcType(1);
                        task.setPodID(wmsWcsTaskInfo.getContainerCode());
                        DesExt desExt = new DesExt();
                        desExt.setUnload(1);
                        task.setDesExt(desExt);
                        task.setDestination(locationInfo.getLocationcode());
                        TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                        if (taskResqon != null && taskResqon.getReturnMsg().equals("succ")) {
                            //如果任务创建成功，就去把对应的任务的状态，创建时间，目的地，TES回传的任务号添加进去
                            String Tesid = String.valueOf(taskResqon.getData().getTaskID());
                            wmsWcsTaskInfo.setThirdPartyTaskId(Tesid);
                            wmsWcsTaskInfo.setTaskStatus("assigned");
                            wmsWcsTaskInfo.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            wmsWcsTaskInfo.setTargetPosition(locationInfo.getLocationcode());
                            taskMapper.updeteStaus(wmsWcsTaskInfo);
                            //任务成功之后将次占位变为占用
                            locationInfo.setStatus("occupied");
                            locationMapper.updeteStatu(locationInfo);
                        } else {
                            System.out.println("任务创建失败等待下一次创建" + wmsWcsTaskInfo.getWCSTaskId() + "--------失败原因：" + taskResqon.getReturnUserMsg());
                            System.out.println("创建任务参数为：" + task);
                        }
                    }
                }
                //TODO:此为出库任务
                else {
                    //此处为出库任务，直接下发
                    //创建TES任务参数
                    TesTask task = new TesTask();
                    task.setWarehouseID("HETU");
                    task.setRequestID(String.format("%08d", Math.abs(IdUtil.getSnowflake().nextId() % 100000000L)));
                    task.setClientCode("WCS");
                    task.setPriority(Integer.parseInt(wmsWcsTaskInfo.getPriority()));
                    task.setSrcType(1);
                    task.setPodID(wmsWcsTaskInfo.getContainerCode());
                    DesExt desExt = new DesExt();
                    desExt.setUnload(1);
                    task.setDesExt(desExt);
                    task.setDestination(wmsWcsTaskInfo.getTargetPosition());
                    TaskResqon taskResqon = tesHttpService.AddTesTask(task);
                    if (taskResqon != null && taskResqon.getReturnMsg().equals("succ")) {
                        //如果任务创建成功，就去把对应的任务的状态，创建时间，目的地，TES回传的任务号添加进去
                        String Tesid = String.valueOf(taskResqon.getData().getTaskID());
                        wmsWcsTaskInfo.setThirdPartyTaskId(Tesid);
                        wmsWcsTaskInfo.setTaskStatus("assigned");
                        wmsWcsTaskInfo.setTaskCreaTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                    wmsWcsTaskInfo.setTargetPosition(locationInfo.getLocationcode());
                        taskMapper.updeteStaus(wmsWcsTaskInfo);
                    } else {
                        System.out.println("任务创建失败等待下一次创建" + wmsWcsTaskInfo.getWCSTaskId() + "--------失败原因：" + taskResqon.getReturnUserMsg());
                        System.out.println("创建任务参数为：" + task);
                    }
                }
            }
        }

        if (!wmsWcsTaskInfos1.isEmpty()) {//此为RCS任务 
            for (WmsWcsTaskInfo wmsWcsTaskInfo : wmsWcsTaskInfos1) {
                if (wmsWcsTaskInfo.getTargetPosition().equals("JX-3")) {
                    //如果进入这，那么就是解析完成之后再去入成品库
                    //再创建任务之前需要去看一下，这个地方在数据库中的状态是否为空，如果为空，即可去创建任务，如果不为空，就不行。
                    //这个地方的状态需要由这个TES的小车来取货之后，把这个地方状态给清空
                    //任务模版用A2,这里就不需要去容器上架
                    LocationInfo oneByLocationCode = locationMapper.findOneByLocationCode("JX-3");
                    //先去查询这个JX-3有没有，再看状态是否为空闲
                    if (oneByLocationCode != null && oneByLocationCode.getStatus().equals("available")) {
                        RcsCreaTask rcsCreaTask = new RcsCreaTask();//任务对象
                        TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];
                        TaskTargetRoute taskTargetRoute = new TaskTargetRoute();
                        TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();
                        taskTargetRoute.setType("STACK");
                        taskTargetRoute.setCode(wmsWcsTaskInfo.getStartPosition());
                        taskTargetRoute.setSeq(0);
                        taskTargetRoute2.setType("SITE");
                        taskTargetRoute2.setCode(wmsWcsTaskInfo.getTargetPosition());
                        taskTargetRoute2.setSeq(0);
                        rcsCreaTask.setTaskType("A2");
                        taskTargetRoutes[0] = taskTargetRoute;
                        taskTargetRoutes[1] = taskTargetRoute2;
                        rcsCreaTask.setTargetRoute(taskTargetRoutes);
                        int i1 = Integer.parseInt(wmsWcsTaskInfo.getPriority());
                        rcsCreaTask.setInitPriority(i1);
                        TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);//发送容器搬运任务
                        if (taskResonse.getMessage().equals("成功")) {
                            //如果下发成功后，需要去把这个JX-3位置占住
                            //还需要去将当前任务状态改为待执行，把RCS的任务写入
                            wmsWcsTaskInfo.setTaskStatus("assigned");
                            wmsWcsTaskInfo.setThirdPartyTaskId(taskResonse.getData().getRobotTaskCode());
                            taskMapper.updeteStaus(wmsWcsTaskInfo);
                            oneByLocationCode.setStatus("occupied");
                            oneByLocationCode.setContainercode(wmsWcsTaskInfo.getContainerCode());
                            locationMapper.updeteStatus(oneByLocationCode);
                        } else {
                            System.out.println("下发入库任务失败，原因：" + taskResonse.getMessage());
                        }
                    } else {

                        System.out.println("JX-3站点不为空闲，不下发解析入成品区任务");
                    }
                } else {
                    //如果这个不为这个点的话，说明是移库任务
//                  移库直接下发任务即可
                    RcsCreaTask rcsCreaTask = new RcsCreaTask();//任务对象
                    TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];
                    TaskTargetRoute taskTargetRoute = new TaskTargetRoute();
                    TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();
                    taskTargetRoute.setType("STACK");
                    taskTargetRoute.setCode(wmsWcsTaskInfo.getStartPosition());
                    taskTargetRoute.setSeq(0);
                    taskTargetRoute2.setType("STACK");
                    taskTargetRoute2.setCode(wmsWcsTaskInfo.getTargetPosition());
                    taskTargetRoute2.setSeq(0);
                    rcsCreaTask.setTaskType("A3");
                    taskTargetRoutes[0] = taskTargetRoute;
                    taskTargetRoutes[1] = taskTargetRoute2;
                    rcsCreaTask.setTargetRoute(taskTargetRoutes);
                    int i1 = Integer.parseInt(wmsWcsTaskInfo.getPriority());
                    rcsCreaTask.setInitPriority(i1);
                    TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);//发送容器搬运任务
                    if (taskResonse.getMessage().equals("成功")) {

                    } else {
                        System.out.println("下发移库任务失败，原因：" + taskResonse.getMessage());
                    }

                }
            }
        }
    }

    @Scheduled(cron = "5/25 * * * * ?")// 第二个任务在 5, 30, 55 秒执行
    public void ScheduledCreaTask() {
        /*TODO:总体思路：满足条件的任务就去创建（前面有子任务完成的优先创建，前面没有子任务的就根据优先级与创建时间来筛选），
         * 需要达到的要求：
         * 假设现在有两个相同的任务任务一与任务二：任务一（子任务1，子任务2，子任务3 优先级：9 创建时间比较早）任务二（子任务1，子任务2，子任务3 优先级：9 创建时间比较晚）
         * 那现在要创建的是任务一的子任务1，再去判断我们能不能创建子任务一，如果可以，那就创建子任务一，如果不行，那就等待。
         * 如果子任务一创建成功后，任务完成之后，我们就需要去创建任务一的子任务二，再去判断创建子任务二需要的条件，任务二创建成功后。
         * 如果这个时候来了任务三，那么又需要去比较任务二与任务三的优先级，如果优先级相同的情况下，看创建时间，优先级为越大越快执行。
         * 假设现在是任务三的优先级大于任务二，那么就该创建任务三的子任务一，再去看创建任务三的子任务1条件是否满足，如果满足就创建
         * 当任务一的子任务二完成后，需要去创建任务3的子任务二，再去看满不满足子任务二的创建条件，如果满足就创建，如果不满足就等待下次定时任务判定。
         * 以此类推，其他任务也是一样。
         */
        try {
            // 获取完整的父子任务结构,以树的结构存放数据
            List<TaskDto> taskDtos = buildTaskDtosFromPendingTasks();
            if (taskDtos.isEmpty()) {
                System.out.println("无需创建的任务");
                return;
            }
            // 按任务类型分组，将相同类型的任务放到一起
            Map<String, List<TaskDto>> tasksByType = taskDtos.stream()
                    .collect(Collectors.groupingBy(TaskDto::getTaskType));
            List<WmsWcsTaskInfo> executableTasks = new ArrayList<>();//用于装载可创建的任务
            // 对每个任务类型独立处理
            for (Map.Entry<String, List<TaskDto>> entry : tasksByType.entrySet()) {
                String taskType = entry.getKey();
                List<TaskDto> tasksOfType = entry.getValue();
                // 按优先级和创建时间排序该类型的任务
                List<TaskDto> sortedTasks = sortTasksByPriorityAndTime(tasksOfType);
                // 获取可执行的子任务
                List<WmsWcsTaskInfo> tasksForType = getExecutableSubTasks(sortedTasks);
                executableTasks.addAll(tasksForType);
            }
            if (!executableTasks.isEmpty()) {
                System.out.println(executableTasks);//打印来看看
                //不为空，把集合元素拿到后，通过判断条件，再看要不要创建。
                // TODO:下面就是我们需要去做的判断这些任务需要创建的条件是否满足 集合为：executableTasks
                for (WmsWcsTaskInfo executableTask : executableTasks) {//再次循环
                    String taskType = executableTask.getTaskType();//拿到当前子任务的任务类型
                    if (taskType == null || taskType.trim().isEmpty()) {
                        // 处理空值情况，比如抛出异常或返回默认值
                        throw new IllegalArgumentException("任务类型不能为空");
                    }

                    String parentTaskType = taskType;
                    int index = taskType.indexOf("子任务");
                    if (index != -1) {
                        parentTaskType = taskType.substring(0, index) + "任务";
                    }
                    int maxSubTasksByTaskType = taskConfig.getMaxSubTasksByType(parentTaskType);
//                    int maxSubTasksByTaskType = getMaxSubTasksByTaskType(taskType);
                    int i = extractTaskNumber(executableTask.getTaskType());
                    if (i != maxSubTasksByTaskType) {
                        //拿到任务类型之后，我们需要根据任务类型去查询对应的库位信息：条件为locationtype=3，而且taskType=lanenumber的数据
                        LocationInfo oneByLocationTypeAndLaneNumber = locationMapper.findOneByLocationTypeAndLaneNumber("3", taskType);
                        //数据拿到之后，就需要去判断对应的，看这个库位是否是空闲的，如是空闲的，就去根据任务的RCS/TES去调用不同接口创建任务
                        if (oneByLocationTypeAndLaneNumber != null && oneByLocationTypeAndLaneNumber.getStatus().equals("available")) {
                            String rcsOrTes = executableTask.getRcsOrTes();
                            if (rcsOrTes.equals("RCS")) {
                                //在这之前需要去判断目标点是不是站点，如果不是就不用模版A2就用模版A1
                                String targetPosition = executableTask.getTargetPosition();//获取任务的目的地
                                //TODO:需要去调用RCS的任务创建，（创建任务之前你还需要去查询对应区域的AGV是否空闲）
                                //TODO:任务创建成功后还需要去修改创建成功后当前任务的信息。
                                //TODO：每一个创建任务的时候，都需要先去将发任务之前先去进行站点上架，不管成功或者失败，都继续发任务
                                Bind bind = new Bind();
                                String startPosition = executableTask.getStartPosition();
                                String containerCode = executableTask.getContainerCode();
                                bind.setCarrierCode(containerCode);//设置载具号
                                bind.setSiteCode(startPosition);//设置站点号
                                BindResonse bindResonse = rcsHttpService.SationBind(bind);//发送站点绑架请求
                                //不管成功。失败需要分情况
                                //然后开始创建任务
                                RcsCreaTask rcsCreaTask = new RcsCreaTask();//任务对象
//                                rcsCreaTask.setTaskType("A2");
//                                    ArrayList<TaskTargetRoute> objects = new ArrayList<TaskTargetRoute>();
                                TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];
                                TaskTargetRoute taskTargetRoute = new TaskTargetRoute();
                                TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();
                                LocationInfo oneByLocationCode1 = locationMapper.findOneByLocationCode(startPosition);
                                LocationInfo oneByLocationCode2 = locationMapper.findOneByLocationCode(executableTask.getTargetPosition());
                                if (oneByLocationCode1 != null) {
                                    taskTargetRoute.setType("SITE");
                                    taskTargetRoute.setCode(startPosition);
                                    taskTargetRoute.setSeq(0);
                                } else {
                                    taskTargetRoute.setType("STACK");
                                    taskTargetRoute.setCode(startPosition);
                                    taskTargetRoute.setSeq(0);
                                }
                                if (oneByLocationCode2 != null) {
                                    taskTargetRoute2.setType("SITE");
                                    taskTargetRoute2.setCode(executableTask.getTargetPosition());
                                    taskTargetRoute2.setSeq(0);
                                    rcsCreaTask.setTaskType("A2");
                                    if(oneByLocationCode2.equals("YR-T1")){                                        
                                        rcsCreaTask.setTaskType("A5");
                                    }
                                } else {
                                    taskTargetRoute2.setType("STACK");
                                    taskTargetRoute2.setCode(startPosition);
                                    taskTargetRoute2.setSeq(0);
                                    rcsCreaTask.setTaskType("A3");
                                }
                                taskTargetRoutes[0] = taskTargetRoute;
                                taskTargetRoutes[1] = taskTargetRoute2;
                                rcsCreaTask.setTargetRoute(taskTargetRoutes);
                                int i1 = Integer.parseInt(executableTask.getPriority());
                                rcsCreaTask.setInitPriority(i1);

                                TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);//发送容器搬运任务
                                System.out.println(taskResonse.getMessage());
                                System.out.println(taskResonse.getData().getRobotTaskCode());
                                if (taskResonse.getMessage().equals("成功")) {
                                    //这里成功之后，需要把任务状态改为assigned
                                    executableTask.setTaskStatus("assigned");
                                    executableTask.setThirdPartyTaskId(taskResonse.getData().getRobotTaskCode());
                                    taskMapper.updeteStaus(executableTask);
                                    //还需要把这个站点的状态改为占用 occupied,这个地方还需要把容器给放进去
                                    oneByLocationTypeAndLaneNumber.setStatus("occupied");
                                    oneByLocationTypeAndLaneNumber.setContainercode(containerCode);
                                    locationMapper.updeteStatus(oneByLocationTypeAndLaneNumber);
                                } else {
                                    //这里为创建任务失败。
                                    System.out.println("任务创建失败，任务id为：" + executableTask.getWCSTaskId() + "。失败原因为：" + taskResonse.getMessage());
                                }
                            }
                        } else {
                            System.out.println(executableTask.getWMSTaskId() + "任务创建失败，对应目标位置有占位");
                        }
                    }
                    else {
                        //TODO:如果是等于最后一个，就只需要去拿到RCS/TES 按照不同任务类型进行创建即可。目的地是WMS下发的
                        //TODO：这里还有个问题，就是说如果我的第一个任务结束后，直接发最后一个任务，我的托盘还没到点位，是不能下发的
                        //直接去查询这个
                        LocationInfo oneByLocationTypeAndLaneNumber = locationMapper.findOneByLocationTypeAndLaneNumber("3", taskType);
                        if (oneByLocationTypeAndLaneNumber != null) {
                            if (oneByLocationTypeAndLaneNumber.getStatus().equals("occupied")) {
                                //需要再去上一遍架
                                Bind bind = new Bind();
                                String startPosition = executableTask.getStartPosition();
                                if(startPosition.equals("YR-T2")){
                                    startPosition = "YR-T1";
                                }
                                String containerCode = executableTask.getContainerCode();
                                bind.setCarrierCode(containerCode);//设置载具号
                                bind.setSiteCode(startPosition);//设置站点号
                                BindResonse bindResonse = rcsHttpService.SationBind(bind);//发送站点绑架请求
                                //上架后，再去发送请求即可
                                RcsCreaTask rcsCreaTask = new RcsCreaTask();//任务对象
                                TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];
                                TaskTargetRoute taskTargetRoute = new TaskTargetRoute();
                                TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();
                                LocationInfo oneByLocationCode1 = locationMapper.findOneByLocationCode(startPosition);
                                LocationInfo oneByLocationCode2 = locationMapper.findOneByLocationCode(executableTask.getTargetPosition());
                                if (oneByLocationCode1 != null) {
                                    taskTargetRoute.setType("SITE");
                                    taskTargetRoute.setCode(startPosition);
                                    taskTargetRoute.setSeq(0);
                                } else {
                                    taskTargetRoute.setType("STACK");
                                    taskTargetRoute.setCode(startPosition);
                                    taskTargetRoute.setSeq(0);
                                }
                                if (oneByLocationCode2 != null) {
                                    taskTargetRoute2.setType("SITE");
                                    taskTargetRoute2.setCode(executableTask.getTargetPosition());
                                    taskTargetRoute2.setSeq(0);
                                    rcsCreaTask.setTaskType("A2");
                                } else {
                                    taskTargetRoute2.setType("STACK");
                                    taskTargetRoute2.setCode(executableTask.getTargetPosition());
                                    taskTargetRoute2.setSeq(0);
                                    rcsCreaTask.setTaskType("A3");
                                }
                                taskTargetRoutes[0] = taskTargetRoute;
                                taskTargetRoutes[1] = taskTargetRoute2;
                                rcsCreaTask.setTargetRoute(taskTargetRoutes);
                                int i1 = Integer.parseInt(executableTask.getPriority());
                                rcsCreaTask.setInitPriority(i1);

                                TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);//发送容器搬运任务
                                System.out.println(taskResonse.getMessage());
                                System.out.println(taskResonse.getData().getRobotTaskCode());
                                if (taskResonse.getMessage().equals("成功")) {
                                    //这里成功之后，需要把任务状态改为assigned
                                    executableTask.setTaskStatus("assigned");
                                    executableTask.setThirdPartyTaskId(taskResonse.getData().getRobotTaskCode());
                                    String progress = executableTask.getProgress();//父id
                                    taskMapper.updeteStaus(executableTask);
                                    //这里最后一个任务创建成功后，需要把主任务也给改为assigned
                                    if (progress.equals("")) {
                                        System.out.println("没有查询到当前任务父id");
                                    } else {
                                        WmsWcsTaskInfo byProgress = taskMapper.findOneByWcstaskId(progress);
                                        byProgress.setTaskStatus("assigned");
                                        taskMapper.updeteStaus(byProgress);
                                    }
                                }
                                else {
                                    //这里为创建任务失败。
                                    System.out.println("任务创建失败，任务id为：" + executableTask.getWCSTaskId() + "。失败原因为：" + taskResonse.getMessage());
                                }
                            }
                        }
                        else {
                            //需要再去上一遍架
                            Bind bind = new Bind();
                            String startPosition = executableTask.getStartPosition();
                            if(startPosition.equals("YR-T2")){
                                    startPosition = "YR-T1";
                            }
                            String containerCode = executableTask.getContainerCode();
                            bind.setCarrierCode(containerCode);//设置载具号
                            bind.setSiteCode(startPosition);//设置站点号
                            BindResonse bindResonse = rcsHttpService.SationBind(bind);//发送站点绑架请求
                            //上架后，再去发送请求即可
                            RcsCreaTask rcsCreaTask = new RcsCreaTask();//任务对象
                            TaskTargetRoute[] taskTargetRoutes = new TaskTargetRoute[2];
                            TaskTargetRoute taskTargetRoute = new TaskTargetRoute();
                            TaskTargetRoute taskTargetRoute2 = new TaskTargetRoute();
                            LocationInfo oneByLocationCode1 = locationMapper.findOneByLocationCode(startPosition);
                            LocationInfo oneByLocationCode2 = locationMapper.findOneByLocationCode(executableTask.getTargetPosition());
                            if (oneByLocationCode1 != null) {
                                taskTargetRoute.setType("SITE");
                                taskTargetRoute.setCode(startPosition);
                                taskTargetRoute.setSeq(0);
                            } else {
                                taskTargetRoute.setType("STACK");
                                taskTargetRoute.setCode(startPosition);
                                taskTargetRoute.setSeq(0);
                            }
                            if (oneByLocationCode2 != null) {
                                taskTargetRoute2.setType("SITE");
                                taskTargetRoute2.setCode(executableTask.getTargetPosition());
                                taskTargetRoute2.setSeq(0);
                                rcsCreaTask.setTaskType("A2");
                            } else {
                                taskTargetRoute2.setType("STACK");
                                taskTargetRoute2.setCode(startPosition);
                                taskTargetRoute2.setSeq(0);
                                rcsCreaTask.setTaskType("A3");
                            }
                            taskTargetRoutes[0] = taskTargetRoute;
                            taskTargetRoutes[1] = taskTargetRoute2;
                            rcsCreaTask.setTargetRoute(taskTargetRoutes);
                            int i1 = Integer.parseInt(executableTask.getPriority());
                            rcsCreaTask.setInitPriority(i1);

                            TaskResonse taskResonse = rcsHttpService.NewRcsTask(rcsCreaTask);//发送容器搬运任务
                            System.out.println(taskResonse.getMessage());
                            System.out.println(taskResonse.getData().getRobotTaskCode());
                            if (taskResonse.getMessage().equals("成功")) {
                                //这里成功之后，需要把任务状态改为assigned
                                executableTask.setTaskStatus("assigned");
                                executableTask.setThirdPartyTaskId(taskResonse.getData().getRobotTaskCode());
                                String progress = executableTask.getProgress();//父id
                                taskMapper.updeteStaus(executableTask);
                                //这里最后一个任务创建成功后，需要把主任务也给改为assigned
                                if (progress.equals("")) {
                                    System.out.println("没有查询到当前子任务的父id");
                                } else {

                                    WmsWcsTaskInfo byProgress = taskMapper.findByProgress(progress);
                                    byProgress.setTaskStatus("assigned");
                                    taskMapper.updeteStaus(byProgress);
                                }
                            } else {
                                //这里为创建任务失败。
                                System.out.println("任务创建失败，任务id为：" + executableTask.getWCSTaskId() + "。失败原因为：" + taskResonse.getMessage());
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从pending任务构建TaskDto集合
     */
    private List<TaskDto> buildTaskDtosFromPendingTasks() {
        // 1. 查询所有pending状态的子任务（用于找到有pending子任务的父任务）
        List<WmsWcsTaskInfo> pendingChildTasks = taskMapper.findByTaskStatus("PENDING");
        if (pendingChildTasks.isEmpty()) {
            return new ArrayList<>();
        }
        // 2. 提取父任务ID并去重
        List<String> parentIds = pendingChildTasks.stream()
                .map(WmsWcsTaskInfo::getProgress)
                .filter(progress -> progress != null && !progress.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (parentIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 3. 逐个查询父任务
        List<WmsWcsTaskInfo> parentTasks = new ArrayList<>();
        for (String parentId : parentIds) {
            try {
                WmsWcsTaskInfo parentTask = taskMapper.findOneByWcstaskId(parentId);
                if (parentTask != null) {
                    parentTasks.add(parentTask);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (parentTasks.isEmpty()) {
            return new ArrayList<>();
        }
        // 4. 获取所有状态的子任务（关键修改）
        List<WmsWcsTaskInfo> allChildTasks = getAllChildTasksByParentIds(parentIds);
        // 5. 按父任务ID分组子任务
        Map<String, List<WmsWcsTaskInfo>> childTasksByParent = allChildTasks.stream()
                .collect(Collectors.groupingBy(WmsWcsTaskInfo::getProgress));

        // 6. 组装成TaskDto列表
        List<TaskDto> taskDtos = new ArrayList<>();
        for (WmsWcsTaskInfo parentTask : parentTasks) {
            TaskDto taskDto = new TaskDto();
            // 设置父任务属性
            taskDto.setId(parentTask.getId());
            taskDto.setWCSTaskId(parentTask.getWCSTaskId());
            taskDto.setWMSTaskId(parentTask.getWMSTaskId());
            taskDto.setThirdPartyTaskId(parentTask.getThirdPartyTaskId());
            taskDto.setTaskType(parentTask.getTaskType());
            taskDto.setContainerCode(parentTask.getContainerCode());
            taskDto.setStartPosition(parentTask.getStartPosition());
            taskDto.setAGVCode(parentTask.getAGVCode());
            taskDto.setTargetPosition(parentTask.getTargetPosition());
            taskDto.setTaskStatus(parentTask.getTaskStatus());
            taskDto.setProgress(parentTask.getProgress());
            taskDto.setRcsOrTes(parentTask.getRcsOrTes());
            taskDto.setTaskCreaTime(parentTask.getTaskCreaTime());
            taskDto.setTaskCompletionTime(parentTask.getTaskCompletionTime());
            taskDto.setPriority(parentTask.getPriority());
            // 设置子任务列表（现在包含所有状态的子任务）
            List<WmsWcsTaskInfo> children = childTasksByParent.get(parentTask.getWCSTaskId());
            taskDto.setChildren(children != null ? children : new ArrayList<>());
            taskDtos.add(taskDto);
        }

        return taskDtos;
    }

    /**
     * 获取所有状态的子任务
     * 根据您现有的Mapper方法，选择最适合的实现方式
     */
    private List<WmsWcsTaskInfo> getAllChildTasksByParentIds(List<String> parentIds) {
        List<WmsWcsTaskInfo> allChildTasks = new ArrayList<>();

        // 定义可能的状态
        String[] possibleStatuses = {"PENDING", "EXECUTING", "COMPLETED", "FAILED"};

        for (String status : possibleStatuses) {
            try {
                List<WmsWcsTaskInfo> tasksWithStatus = taskMapper.findByTaskStatus(status);
                // 过滤出属于这些父任务的子任务
                List<WmsWcsTaskInfo> filteredTasks = tasksWithStatus.stream()
                        .filter(task -> parentIds.contains(task.getProgress()))
                        .collect(Collectors.toList());
                allChildTasks.addAll(filteredTasks);
            } catch (Exception e) {
                // 如果某个状态查询失败，继续处理其他状态
                System.out.println("查询状态 " + status + " 的任务失败: " + e.getMessage());
            }
        }

        return allChildTasks;
    }

    /**
     * 按优先级和创建时间排序任务
     */
    private List<TaskDto> sortTasksByPriorityAndTime(List<TaskDto> tasks) {
        return tasks.stream()
                .sorted((task1, task2) -> {
                    // 先按优先级排序（数字越大优先级越高）
                    int priority1 = parsePriority(task1.getPriority());
                    int priority2 = parsePriority(task2.getPriority());

                    if (priority1 != priority2) {
                        return Integer.compare(priority2, priority1); // 降序
                    }

                    // 优先级相同，按创建时间排序（创建时间早的优先）
                    return task1.getTaskCreaTime().compareTo(task2.getTaskCreaTime());
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析优先级:将String变为int
     */
    private int parsePriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return 0; // 默认优先级为0
        }
        try {
            return Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 获取可执行的子任务
     */
//    private List<WmsWcsTaskInfo> getExecutableSubTasks(List<TaskDto> sortedTasks) {
//        List<WmsWcsTaskInfo> executableTasks = new ArrayList<>();
//
//        // 获取该任务类型的最大子任务步骤数
//        int maxSteps = taskConfig.getMaxSubTasksByType(sortedTasks.get(0).getTaskType());
////        int maxSteps = getMaxSubTasksByTaskType(sortedTasks.get(0).getTaskType());
//
//        // 按步骤从小到大处理
//        for (int step = 1; step <= maxSteps; step++) {
//            // 检查当前步骤是否有任务正在执行
//            boolean stepBusy = isStepBusy(sortedTasks, step);
//
//            if (!stepBusy) {
//                // 找到可以执行该步骤的任务
//                for (TaskDto taskDto : sortedTasks) {
//                    WmsWcsTaskInfo subTaskForStep = getSubTaskForStep(taskDto, step);
//
//                    if (subTaskForStep != null && canExecuteStep(subTaskForStep, taskDto, step)) {
//                        executableTasks.add(subTaskForStep);
//                        break; // 每个步骤只创建一个任务，避免资源冲突
//                    }
//                }
//            }
//        }
//
//        return executableTasks;
//    }

    /**
     * 获取可执行的子任务
     */
    private List<WmsWcsTaskInfo> getExecutableSubTasks(List<TaskDto> sortedTasks) {
        List<WmsWcsTaskInfo> executableTasks = new ArrayList<>();

        // 获取该任务类型的最大子任务步骤数
        int maxSteps = taskConfig.getMaxSubTasksByType(sortedTasks.get(0).getTaskType());

        // 按步骤从小到大处理
        for (int step = 1; step <= maxSteps; step++) {
            // 检查当前步骤是否有任务正在执行
            boolean stepBusy = isStepBusy(sortedTasks, step);

            if (!stepBusy) {
                // 用于存储候选任务及其排序时间
                Map<WmsWcsTaskInfo, LocalDateTime> candidateTasks = new HashMap<>();

                for (TaskDto taskDto : sortedTasks) {
                    WmsWcsTaskInfo subTaskForStep = getSubTaskForStep(taskDto, step);

                    if (subTaskForStep != null && canExecuteStep(subTaskForStep, taskDto, step)) {
                        // 获取排序时间
                        LocalDateTime sortTime = getSortTimeForStep(subTaskForStep, taskDto, step);
                        candidateTasks.put(subTaskForStep, sortTime);
                    }
                }

                // 根据步骤选择任务
                if (!candidateTasks.isEmpty()) {
                    WmsWcsTaskInfo selectedTask = selectTaskForStep(candidateTasks, step);
                    if (selectedTask != null) {
                        executableTasks.add(selectedTask);
                        break; // 每个步骤只创建一个任务，避免资源冲突
                    }
                }
            }
        }

        return executableTasks;
    }

    /**
     * 根据步骤获取排序时间
     */
    private LocalDateTime getSortTimeForStep(WmsWcsTaskInfo subTask, TaskDto parentDto, int step) {
        if (step == 1) {
            // 步骤1：使用父任务的创建时间
            try {
                return LocalDateTime.parse(parentDto.getTaskCreaTime(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        } else {
            // 步骤2及以后：使用前一个步骤的完成时间
            WmsWcsTaskInfo previousSubTask = getSubTaskForStep(parentDto, step - 1);
            if (previousSubTask != null && previousSubTask.getTaskCompletionTime() != null) {
                try {
                    return LocalDateTime.parse(previousSubTask.getTaskCompletionTime(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    return LocalDateTime.now();
                }
            }
            return LocalDateTime.now(); // 默认当前时间
        }
    }

    /**
     * 根据步骤选择任务
     */
    private WmsWcsTaskInfo selectTaskForStep(Map<WmsWcsTaskInfo, LocalDateTime> candidateTasks, int step) {
        if (step == 1) {
            // 步骤1：按照优先级降序，创建时间升序排序
            // 先找到最高优先级的任务
            Optional<WmsWcsTaskInfo> maxPriorityTask = candidateTasks.keySet().stream()
                    .max((task1, task2) -> {
                        int priority1 = parsePriority(task1.getPriority());
                        int priority2 = parsePriority(task2.getPriority());
                        return Integer.compare(priority1, priority2);
                    });

            if (maxPriorityTask.isPresent()) {
                // 获取最高优先级的值
                int maxPriority = parsePriority(maxPriorityTask.get().getPriority());

                // 筛选出所有具有最高优先级的任务
                List<WmsWcsTaskInfo> highestPriorityTasks = candidateTasks.keySet().stream()
                        .filter(task -> parsePriority(task.getPriority()) == maxPriority)
                        .collect(Collectors.toList());

                // 在这些任务中，按创建时间升序排序（使用parentDto的创建时间）
                highestPriorityTasks.sort((task1, task2) -> {
                    LocalDateTime time1 = candidateTasks.get(task1);
                    LocalDateTime time2 = candidateTasks.get(task2);
                    return time1.compareTo(time2);
                });

                return highestPriorityTasks.get(0);
            }
        } else {
            // 步骤2及以后：按照前一个步骤的完成时间升序排序（先完成的优先）
            List<WmsWcsTaskInfo> sortedTasks = new ArrayList<>(candidateTasks.keySet());
            sortedTasks.sort((task1, task2) -> {
                LocalDateTime time1 = candidateTasks.get(task1);
                LocalDateTime time2 = candidateTasks.get(task2);
                return time1.compareTo(time2);
            });

            return sortedTasks.get(0);
        }
        return null;
    }

    /**
     * 检查某个步骤是否有任务正在执行
     * 只有pending和completed状态不占用资源，其他状态都占用资源
     */
    private boolean isStepBusy(List<TaskDto> tasks, int step) {
        for (TaskDto task : tasks) {
            if (task.getChildren() != null) {
                for (WmsWcsTaskInfo child : task.getChildren()) {
                    int childStep = extractTaskNumber(child.getTaskType());
                    if (childStep == step) {
                        String status = child.getTaskStatus().toLowerCase();
                        // 只有pending和completed不占用资源，其他状态都占用
                        if (!"pending".equals(status) && !"completed".equals(status)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取任务在指定步骤的子任务
     */
    private WmsWcsTaskInfo getSubTaskForStep(TaskDto taskDto, int step) {
        if (taskDto.getChildren() == null) {
            return null;
        }

        for (WmsWcsTaskInfo child : taskDto.getChildren()) {
            int childStep = extractTaskNumber(child.getTaskType());
            if (childStep == step) {
                return child;
            }
        }

        return null;
    }

    /**
     * 检查是否可以执行某个步骤
     */
    private boolean canExecuteStep(WmsWcsTaskInfo subTask, TaskDto parentDto, int step) {
        String status = subTask.getTaskStatus().toLowerCase();

        // 如果子任务不是pending状态，不能执行
        if (!"pending".equals(status)) {
            return false;
        }

        // 检查前序步骤是否都完成了
        if (step > 1) {
            boolean allPreStepsCompleted = areAllPreStepsCompleted(parentDto, step);
            if (!allPreStepsCompleted) {
                return false;
            }
        }

        // 检查业务条件 - 这里您可以根据实际业务实现
        return true;
    }

    /**
     * 检查任务的所有前序步骤是否都完成
     */
    private boolean areAllPreStepsCompleted(TaskDto taskDto, int currentStep) {
        if (taskDto.getChildren() == null) {
            return currentStep == 1; // 如果没有子任务，只有步骤1可以执行
        }

        for (int step = 1; step < currentStep; step++) {
            WmsWcsTaskInfo preStepTask = getSubTaskForStep(taskDto, step);
            if (preStepTask == null || !"completed".equals(preStepTask.getTaskStatus().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据任务类型获取最大子任务数
     */
//    private int getMaxSubTasksByTaskType(String taskType) {
//        switch (taskType) {
//            case "入成品库任务":
//                return 3;
//            case "入待灭菌区任务":
//                return 3;
//            case "入解析库任务":
//                return 3;
//            default:
//                return 1;
//        }
//    }

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

