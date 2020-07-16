package com.shensen.learn.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.shensen.learn.fastjson.deserializer.SubjectListDeserializer;
import java.io.Serializable;
import java.util.List;

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
    @JSONField(deserializeUsing = SubjectListDeserializer.class)
    private List<Long> awardsList;

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

    public List<Long> getAwardsList() {
        return awardsList;
    }

    public void setAwardsList(List<Long> awardsList) {
        this.awardsList = awardsList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteryAwards{");
        sb.append("awardsId=").append(awardsId);
        sb.append(", commodityCode='").append(commodityCode).append('\'');
        sb.append(", awardsStatus=").append(awardsStatus);
        sb.append(", awardCount=").append(awardCount);
        sb.append(", winningCount=").append(winningCount);
        sb.append(", awardsList=").append(awardsList);
        sb.append('}');
        return sb.toString();
    }

}
