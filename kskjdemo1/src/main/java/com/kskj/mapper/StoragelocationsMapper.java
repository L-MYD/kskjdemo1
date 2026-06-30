package com.kskj.mapper;

import com.kskj.pojo.LocationInfo;
import com.kskj.pojo.Storagelocations;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoragelocationsMapper {
    Storagelocations findOneBywmsreference(String wmsreference);
}
