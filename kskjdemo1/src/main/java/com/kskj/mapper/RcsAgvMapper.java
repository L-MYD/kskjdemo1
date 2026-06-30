package com.kskj.mapper;

import com.kskj.pojo.rcs.Agv.AgvInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RcsAgvMapper {
    //获取总条数

    List<AgvInfo> findOneByArea(String area);

}
