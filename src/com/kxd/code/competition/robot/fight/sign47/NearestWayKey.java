package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;

/**
 * @author mengqingyan 2018/1/10
 */
public class NearestWayKey {
    private final Location startLocation;
    private final Location targetLocation ;

    public NearestWayKey(Location startLocation, Location targetLocation) {
        this.startLocation = startLocation;
        this.targetLocation = targetLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NearestWayKey that = (NearestWayKey) o;

        if (startLocation != null ? !startLocation.equals(that.startLocation) : that.startLocation != null)
            return false;
        return targetLocation != null ? targetLocation.equals(that.targetLocation) : that.targetLocation == null;
    }

    @Override
    public int hashCode() {
        int result = startLocation != null ? startLocation.hashCode() : 0;
        result = 31 * result + (targetLocation != null ? targetLocation.hashCode() : 0);
        return result;
    }
}
