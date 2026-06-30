package com.kskj.mapper;

import com.kskj.pojo.PSInfo;
import com.kskj.pojo.ResendMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PsMapper {
    PSInfo findoneByRobotID(String robotID);
    void  updete(PSInfo psInfo);

}
