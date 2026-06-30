package com.kskj.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DbBackup {
    private String id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    private String fileName;



}
