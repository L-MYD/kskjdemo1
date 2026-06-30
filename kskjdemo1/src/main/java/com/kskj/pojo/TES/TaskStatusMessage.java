package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusMessage {
    private Long messageID;
    private Integer messageType;
    private String warehouseID;
    private String createTime;
    private TaskContent content;
}
