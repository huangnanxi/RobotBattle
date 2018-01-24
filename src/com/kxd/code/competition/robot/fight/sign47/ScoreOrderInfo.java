package com.kxd.code.competition.robot.fight.sign47;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mengqingyan 2018/1/19
 */
public class ScoreOrderInfo {

    private List<FightRobotScoreInfo> fightRobotScoreInfos;

    private int currentOrder;

    private int currentScore;

    private int maxScore;

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public List<FightRobotScoreInfo> getFightRobotScoreInfos() {
        return fightRobotScoreInfos;
    }

    public List<FightRobotScoreInfo> getTopNFightRobotScoreInfosExMe(int n) {
        List<FightRobotScoreInfo> topNFightRobotScoreInfos = new ArrayList<>();
        int tmpN = n;
        int size = this.getFightRobotScoreInfos().size();
        if(tmpN > size) {
            tmpN = size;
        }
        for (int i = 0; i < tmpN; i++) {
            if(i == currentOrder) {
                continue;
            }
            topNFightRobotScoreInfos.add(this.fightRobotScoreInfos.get(i));
        }
        return topNFightRobotScoreInfos;
    }



    public void setFightRobotScoreInfos(List<FightRobotScoreInfo> fightRobotScoreInfos) {
        this.fightRobotScoreInfos = fightRobotScoreInfos;
    }

    public List<FightRobotScoreInfo> getHigherFightRobotScoreInfos() {
        List<FightRobotScoreInfo> higherFightRobotScoreInfos = new ArrayList<>();
        for (int i = 0; i < currentOrder; i++) {
            higherFightRobotScoreInfos.add(this.fightRobotScoreInfos.get(i));
        }
        return higherFightRobotScoreInfos;
    }

    public int getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(int currentOrder) {
        this.currentOrder = currentOrder;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }
}
