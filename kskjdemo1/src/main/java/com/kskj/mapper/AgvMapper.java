package com.kskj.mapper;
import com.kskj.pojo.rcs.Agv.AgvInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgvMapper {
    AgvInfo findoneByRobotID(String agvname);
    void  updete(AgvInfo psInfo);
}
