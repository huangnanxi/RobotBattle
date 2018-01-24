package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

/**
 * @author mengqingyan 2018/1/19
 */
public class FightRobotScoreInfo {

    private FightRobotBaseInfo fightRobotBaseInfo;
    private int score;

    public FightRobotBaseInfo getFightRobotBaseInfo() {
        return fightRobotBaseInfo;
    }

    public void setFightRobotBaseInfo(FightRobotBaseInfo fightRobotBaseInfo) {
        this.fightRobotBaseInfo = fightRobotBaseInfo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
