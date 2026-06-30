package com.kskj.controller;

import com.kskj.pojo.rcs.reportertask.RcsReporterTask;
import com.kskj.pojo.rcs.reportertask.RcsReporterTaskResonse;
import com.kskj.service.RcsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RcsController {
    @Autowired
    private RcsService rcsService;
/*
此接口为RCS任务完成情况的接口
*/
    @PostMapping("/robot/reporter/task")
    public RcsReporterTaskResonse getTesInspectionInfo(@RequestBody RcsReporterTask rcsReporterTask){
        System.out.println(rcsReporterTask.toString());
        RcsReporterTaskResonse rcsReporterTaskResonse = rcsService.RcsTaskToWms(rcsReporterTask);
        return rcsReporterTaskResonse;
    }
}
