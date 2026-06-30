package com.kskj.mapper;

import com.kskj.domian.BackupQuery;
import com.kskj.domian.DeviceState;
import com.kskj.pojo.DbBackup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeviceStateMapper {

    List<DeviceState> findDevice();


    List<DeviceState> findByProperty(int stackerNo);
}
