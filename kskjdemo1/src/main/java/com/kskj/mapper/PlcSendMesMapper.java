package com.kskj.mapper;

import com.kskj.pojo.PLC.PlcSendMes;
import com.kskj.pojo.ResendMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlcSendMesMapper {
    void  add(PlcSendMes PlcSendMes);
}
