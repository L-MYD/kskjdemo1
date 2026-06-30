package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TvShow {
    private String stackerState;
    private String stackerNo;
    private Integer noFinIshTaskNum;
    private String currentTask;
    private String trayCode;
    private OutInfo outInfo;
    private Fault fault;
    private MoveKu moveKu;
}
