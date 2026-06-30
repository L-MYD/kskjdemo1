package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveKu {
    private String trayCode;
    private String srcKuwei;
    private String dstKuwei;
}
