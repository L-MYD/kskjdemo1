package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WcsResonse {
    private  String code;
    private  String msg;
    private  String data;
}
