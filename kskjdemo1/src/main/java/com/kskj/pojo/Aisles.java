package com.kskj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aisles {
    private int id;
    private String aislename;
    private String wmsreferenceone;
    private String wmsreferencetwo;
    private String floornum;
}
