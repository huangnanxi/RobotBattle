package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;

import java.util.List;

/**
 * @author mengqingyan 2018/1/9
 */
public class NearestWayInfo {

    private List<Location> nextLocationsForTarget;

    private int            minStep;

    private Location targetLocation;

    public int getMinStep() {
        return minStep;
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
    }

    public List<Location> getNextLocationsForTarget() {
        return nextLocationsForTarget;
    }

    public void setNextLocationsForTarget(List<Location> nextLocationsForTarget) {
        this.nextLocationsForTarget = nextLocationsForTarget;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }
}
