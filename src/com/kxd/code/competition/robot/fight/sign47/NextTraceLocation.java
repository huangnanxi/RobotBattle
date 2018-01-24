package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;

/**
 * Created by Administrator on 2018/1/17.
 */
public class NextTraceLocation extends Location {

    public NextTraceLocation(int x, int y) {
        super(x, y);
    }

    public NextTraceLocation(Location nextLocation) {
        super(nextLocation.x, nextLocation.y);
    }


    private Location targetLocation;

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }
}
