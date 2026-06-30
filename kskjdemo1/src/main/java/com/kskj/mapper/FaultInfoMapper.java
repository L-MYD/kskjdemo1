package com.kskj.mapper;

import com.kskj.pojo.FaultInfo;
import com.kskj.pojo.WmsWcsTaskInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FaultInfoMapper {
    void  add(FaultInfo faultInfo);
}
