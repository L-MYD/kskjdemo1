package com.kskj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.kskj.domian.BackupQuery;
import com.kskj.pojo.DbBackup;
import com.kskj.pojo.Menu;
import com.kskj.until.R;


import java.util.List;

public interface IDbBackupService extends IService<Menu> {
    List<DbBackup> getDbBackupAll(BackupQuery aoData);

    DbBackup findOne(Long id);
    void delet(Long id);
    void upDate(DbBackup article);
    R insertDataBack();
    PageInfo<DbBackup> queryPage(BackupQuery articleQuery);
//    R pathDele(Long[] id);
//Page<DbBackup> getUserPage(int pageNum, int pageSize);
Page<DbBackup> fuzzy(BackupQuery articleQuery);
//    PageInfo<DbBackup> queryPage(ArticleQuery articleQuery);

}
