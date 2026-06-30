package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackExternallnspectionResqon {
    //返回的给tes的参数
    private  int returnCode;//状态码
    private  String returnMsg;//消息
}
