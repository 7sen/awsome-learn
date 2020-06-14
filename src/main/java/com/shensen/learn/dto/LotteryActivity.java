package com.shensen.learn.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 抽奖活动
 *
 * @author Alwyn
 * @date 2020-06-12 16:08
 */
public class LotteryActivity implements Serializable {

    private static final long serialVersionUID = -2206476188333148865L;
    private List<Long> lotteryAwardsList;

    public List<Long> getLotteryAwardsList() {
        return lotteryAwardsList;
    }

    public void setLotteryAwardsList(List<Long> lotteryAwardsList) {
        this.lotteryAwardsList = lotteryAwardsList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LotteryActivity{");
        sb.append("lotteryAwardsList=").append(lotteryAwardsList);
        sb.append('}');
        return sb.toString();
    }
}
