package com.kskj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kskj.domian.BackupQuery;
import com.kskj.pojo.DbBackup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
@Mapper
public interface DbBackupMapper extends BaseMapper<DbBackup> {


    List<DbBackup> pageFind(BackupQuery backupQuery);
    void  add(DbBackup dbBackup);
    DbBackup FindByName(String name);
    List<DbBackup> queryPage(BackupQuery articleQuery);

//    Page<DbBackup> selectPage(Page<DbBackup> page, Object o);
}
