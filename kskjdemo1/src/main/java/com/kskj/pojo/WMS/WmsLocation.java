package com.kskj.pojo.WMS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocation {
    private String WcsId;//WCS 流水号
    private String ApplyLocation;//申请点
    private String Palno;//容器号
    private String Height;//容器高度,需要定义。
}
