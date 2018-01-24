package com.kxd.code.competition.robot.fight.mvp;

import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;

public class MvpElement {
    public int minStep;
    public Location targetlocation;
    private Location mylocation;
    public CommonMoveAction action;

    public MvpElement(Location mylocation, Location targetlocation) {
        this.mylocation = mylocation;
        this.targetlocation = targetlocation;
        this.minStep = getMinStep();
    }

    private int getMinStep() {
        return Math.abs(mylocation.x - targetlocation.x) + Math.abs(mylocation.y - targetlocation.y);
    }
}
