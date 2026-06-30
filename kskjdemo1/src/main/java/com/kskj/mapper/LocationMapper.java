package com.kskj.mapper;

import com.kskj.pojo.LocationInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface LocationMapper {
    LocationInfo findOneByBackup(String backup);
    LocationInfo findOneByLocationCode(String locationcode);
    LocationInfo findOneByLocationTypeAndLaneNumber(String locationtype,String lanenumber );
    ArrayList<LocationInfo>  findByStatusAndLaneNumber(String status, String lanenumber );
    void  updeteStatus(LocationInfo locationInfo);
    void  updeteStatu(LocationInfo locationInfo);
    ArrayList<LocationInfo> findOneByLocationType(String locationtype);
}
