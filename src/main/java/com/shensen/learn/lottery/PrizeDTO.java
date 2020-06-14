package com.shensen.learn.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeDTO {

    /**
     * 索引
     */
    private Integer index;


    /**
     * 奖品名称
     */
    private String name;

    /**
     * 奖品编号
     */
    private String no;


    /**
     * 中奖概率
     */
    private Double probability;

}
