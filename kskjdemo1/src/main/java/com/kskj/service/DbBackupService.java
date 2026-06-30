package com.kskj.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kskj.domian.BackupQuery;
import com.kskj.mapper.DbBackupMapper;
import com.kskj.mapper.MenuMapper;
import com.kskj.pojo.DbBackup;
import com.kskj.pojo.Menu;
import com.kskj.service.impl.IDbBackupService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.IdUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.io.File;

@Service
public class DbBackupService extends ServiceImpl<MenuMapper, Menu> implements IDbBackupService {
    @Autowired
    private DbBackupMapper dbBackupMapper;

    @Override
    public List<DbBackup> getDbBackupAll(BackupQuery aoData) {
        //直接弄一个分页查询；
        List<DbBackup> dbBackups = dbBackupMapper.pageFind(aoData);
        return dbBackups;

    }
    @Override
    public PageInfo<DbBackup> queryPage(BackupQuery articleQuery) {
        PageHelper.startPage(articleQuery.getPageNum(), articleQuery.getPageSize());
        List<DbBackup> articles = dbBackupMapper.queryPage(articleQuery);
        PageInfo<DbBackup> info = new PageInfo<DbBackup>(articles);
        return  info;
    }

    @Override
    public Page<DbBackup> fuzzy(BackupQuery articleQuery) {
        LambdaQueryWrapper<DbBackup> queryWrapper = new LambdaQueryWrapper();
        Page<DbBackup> zktPage = dbBackupMapper.selectPage(new Page<>(articleQuery.getPageNum(), articleQuery.getPageSize()),queryWrapper );
        return zktPage;
    }

    //    @Override
//    public Page<DbBackup> getUserPage(int pageNum, int pageSize) {
//        Page<DbBackup> page = new Page<>(pageNum, pageSize);
//        return dbBackupMapper.selectPage(page, null);
//    }
    @Override
    public R insertDataBack() {

        String databaseName = "kswms";
        String databaseUser = "sa";
        String databasePassword = "123456";

        String databaseBackupUrl = "D:\\back_path";
        String serverIp = "localhost ";
        //判断存储路径文件夹是否存在，不存在则创建
        File file = new File(databaseBackupUrl);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //拼接数据库连接配置
            String url = "jdbc:sqlserver://" + serverIp + ":1433;" + "databaseName=" + databaseName + ";user=" + databaseUser + ";password=" + databasePassword + ";" + "trustServerCertificate=true";
            Connection conn = DriverManager.getConnection(url);
//            Connection conn = DriverManager.getConnection("jdbc:sqlserver://ip:1433;"+
//                    "databaseName=数据库名;user=数据库登录账号;password=数据库登录密码");
            Statement stmt = conn.createStatement();

//            BACKUP DATABASE wmsdb TO DISK = 'D:\back_path\wmsdb_full.bak'
            //获取当前时间 yyyy-MM-dd HH:mm:ss
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTime = df.format(date);
            //拼接备份数据库的命令  BACKUP DATABASE primaryedu TO DISK =  'D:\Backup\备份文件.bak'
            String backupCmd = "BACKUP DATABASE " + databaseName + " TO DISK =  \'" + databaseBackupUrl + "/" + databaseName + currentTime + ".bak\'";
            stmt.execute(backupCmd);
            String name = databaseName + currentTime + ".bak";
            stmt.close();
            conn.close();
            DbBackup dbBackup = new DbBackup();
            LocalDateTime localDateTime = LocalDateTime.now();
            long l = IdUtil.getSnowflake().nextId();
            String s = String.valueOf(l);
//            String result = s.substring(s.lastIndexOf("\\") + 1);
//            int i = Integer.parseInt(result);
            dbBackup.setId(s);
            dbBackup.setFileName(name);
            dbBackup.setCreateTime(localDateTime);
            dbBackup.setUpdateTime(localDateTime);
            dbBackup.setRemark("无语了");
//                int newId = this.dbBackupDAO.save(this.dbBackup);
            dbBackupMapper.add(dbBackup);
            //写一个根据文件名来查询我们的添加有没有成功
            DbBackup dbBackup1 = dbBackupMapper.FindByName(name);
            if (dbBackup1 != null) {
                return R.ok("SUCCESS");
            }
            System.out.println("Backup successful");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.fail("数据备份失败");
    }


    @Override
    public DbBackup findOne(Long id) {
        return null;
    }

    @Override
    public void delet(Long id) {

    }

    @Override
    public void upDate(DbBackup article) {

    }


}
