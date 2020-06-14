package com.shensen.learn.dto;

import java.io.Serializable;

/**
 * 抽奖奖项
 *
 * @author Alwyn
 * @date 2020-06-12 16:07
 */
public class LotteryAwards implements Serializable {

    private static final long serialVersionUID = -351546212930060051L;
    private Long awardsId;
    private String commodityCode;
    private Integer awardsStatus;
    private Integer awardCount;
    private Integer winningCount;

    public Long getAwardsId() {
        return awardsId;
    }

    public void setAwardsId(Long awardsId) {
        this.awardsId = awardsId;
    }

    public String getCommodityCode() {
        return commodityCode;
    }

    public void setCommodityCode(String commodityCode) {
        this.commodityCode = commodityCode;
    }

    public Integer getAwardsStatus() {
        return awardsStatus;
    }

    public void setAwardsStatus(Integer awardsStatus) {
        this.awardsStatus = awardsStatus;
    }

    public Integer getAwardCount() {
        return awardCount;
    }

    public void setAwardCount(Integer awardCount) {
        this.awardCount = awardCount;
    }

    public Integer getWinningCount() {
        return winningCount;
    }

    public void setWinningCount(Integer winningCount) {
        this.winningCount = winningCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteryAwards{");
        sb.append("awardsId=").append(awardsId);
        sb.append(", commodityCode='").append(commodityCode).append('\'');
        sb.append(", awardsStatus=").append(awardsStatus);
        sb.append(", awardCount=").append(awardCount);
        sb.append(", winningCount=").append(winningCount);
        sb.append('}');
        return sb.toString();
    }

}
