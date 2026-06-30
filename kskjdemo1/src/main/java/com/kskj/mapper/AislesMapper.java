package com.kskj.mapper;

import com.kskj.pojo.Aisles;
import com.kskj.pojo.LocationInfo;
import com.kskj.pojo.Storagelocations;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AislesMapper {
    Aisles findLikeBywmsreference(String wmsreference);
    Aisles findOneByaislename(String aislename);
}
