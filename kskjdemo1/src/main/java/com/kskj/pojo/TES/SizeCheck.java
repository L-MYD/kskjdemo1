package com.kskj.pojo.TES;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SizeCheck {
    private int length;//长
    private int width;//宽
    private int height;//高
    private int lengthOutOfRange;//超长
    private int widthOutOfRange;//超宽
    private int heightOutOfRange;//超高
}
