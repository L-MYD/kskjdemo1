package com.kskj.mapper;

import com.kskj.pojo.RcsLocataion;
import com.kskj.pojo.Storagelocations;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RcsLocataionMapper {
    RcsLocataion findOneBylocataion(String locataion);
}
