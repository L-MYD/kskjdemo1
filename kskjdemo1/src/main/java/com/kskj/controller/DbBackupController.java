package com.kskj.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.kskj.domian.BackupQuery;
import com.kskj.domian.Massage;
import com.kskj.pojo.DbBackup;
import com.kskj.service.DbBackupService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sqlBackup")
public class DbBackupController {
    //给数据库手动备份的接口
    @Autowired
    private DbBackupService dbBackupService;

//查询备份信息的接口
    @PostMapping("/find")
    public List<DbBackup> getDbBackupAll(@RequestBody BackupQuery aoData) {
        List<DbBackup> dbBackupAll = dbBackupService.getDbBackupAll(aoData);
        return dbBackupAll;
    }
//    @PostMapping("/pagefind")
//    PageInfo<DbBackup> queryPage(@RequestBody BackupQuery articleTypeQuery){
//        System.out.println(articleTypeQuery);
//        PageInfo<DbBackup> PageInfo = dbBackupService.queryPage(articleTypeQuery);
//        return PageInfo;
//    }
@PostMapping("/pagefind")
public Page<DbBackup> page(@RequestBody BackupQuery articleTypeQuery) {
    System.out.println(articleTypeQuery);
    Page<DbBackup> userPage = dbBackupService.fuzzy(articleTypeQuery);
    return userPage;
}
    @PostMapping
    public R dataBack() {
        R r = dbBackupService.insertDataBack();
        return r;
    }
//    "http://localhost:8084/rcms/services/rest/hikRpcService/genAgvSchedulingTask"
@PostMapping("/rcms/services/rest/hikRpcService/genAgvSchedulingTask")
public Massage test(@RequestBody Massage massage) {

    return massage;
}
}
