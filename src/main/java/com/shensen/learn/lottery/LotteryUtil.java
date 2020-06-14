package com.shensen.learn.lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 抽奖算法
 *
 * @author Alwyn
 * @date 2020-06-08 11:42
 */
public class LotteryUtil {

    /**
     * 抽奖方法
     *
     * @param orignalRates 商品中奖概率列表，保证顺序和实际物品对应
     * @return 中奖商品索引
     */
    public static int lottery(List<Double> orignalRates) {

        if (orignalRates == null || orignalRates.isEmpty()) {
            return -1;
        }

        int size = orignalRates.size();

        // 计算总概率，这样可以保证不一定总概率是1
        double sumRate = 0d;
        for (double rate : orignalRates) {
            sumRate += rate;
        }

        // 计算每个物品在总概率的基础下的概率情况
        List<Double> sortOrignalRates = new ArrayList<>(size);
        Double tempSumRate = 0d;
        for (double rate : orignalRates) {
            tempSumRate += rate;
            sortOrignalRates.add(tempSumRate / sumRate);
        }

        // 根据区块值来获取抽取到的物品索引
        double nextDouble = ThreadLocalRandom.current().nextDouble();
        sortOrignalRates.add(nextDouble);
        Collections.sort(sortOrignalRates);

        return sortOrignalRates.indexOf(nextDouble);
    }

    public static void main(String[] args) {
        List<PrizeDTO> prizeList = new ArrayList<>();
        prizeList.add(new PrizeDTO(1, "13英寸 MacBook Pro 2020", "P1", 0.05d));
        prizeList.add(new PrizeDTO(2, "iPhone 11 Pro Max", "P1", 0.1d));
        prizeList.add(new PrizeDTO(3, "iPhone 8 Plus", "P2", 0.2d));
        prizeList.add(new PrizeDTO(4, "iPhone SE", "P3", 0.15d));
        prizeList.add(new PrizeDTO(5, "谢谢参与", "P4", 0.5d));

        // 存储概率
        List<Double> orignalRates = new ArrayList<>(prizeList.size());
        for (PrizeDTO gift : prizeList) {
            double probability = gift.getProbability();
            if (probability < 0) {
                probability = 0;
            }
            orignalRates.add(probability);
        }

        // 统计
        Map<Integer, Integer> count = new HashMap<>();

        // 测试次数
        double num = 1000;
        for (int i = 0; i < num; i++) {
            int orignalIndex = lottery(orignalRates);
            Integer value = count.get(orignalIndex);
            count.put(orignalIndex, value == null ? 1 : value + 1);
        }

        for (Map.Entry<Integer, Integer> entry : count.entrySet()) {
            System.out.println(prizeList.get(entry.getKey()) + ", 命中次数=" + entry.getValue() + ", 实际概率="
                    + entry.getValue() / num);
        }
    }
}
