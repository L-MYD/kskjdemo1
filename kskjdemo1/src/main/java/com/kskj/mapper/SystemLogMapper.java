package com.kskj.mapper;


import com.kskj.pojo.SystemLog;
import com.kskj.pojo.WmsWcsTaskInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemLogMapper  {
    void  add(SystemLog systemLog);
}
