package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.element.child.BloodBag;

/**
 * Created by Administrator on 2018/1/22.
 */
public class BloodTrace {
    private BloodBag bloodBag;
    private int minStep = Integer.MAX_VALUE;
    private int robotNum = 0;


    public BloodBag getBloodBag() {
        return bloodBag;
    }

    public void setBloodBag(BloodBag bloodBag) {
        this.bloodBag = bloodBag;
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public int getRobotNum() {
        return robotNum;
    }

    public void addRobotNum() {
        this.robotNum++;
    }
}
