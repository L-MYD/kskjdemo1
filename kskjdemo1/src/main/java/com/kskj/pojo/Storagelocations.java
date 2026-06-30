package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Storagelocations {
    private int id;
    private String locationname;
    private String aislecode;
    private String wmsreference;
}
