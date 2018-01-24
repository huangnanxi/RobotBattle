package com.kxd.code.competition.robot.fight.av;

import com.kxd.code.competition.entity.Location;

public class AVLocation extends Location {
    public AVLocation(Location location) {
        super(location.x, location.y);
    }

    public AVLocation(int x, int y) {
        super(x, y);
    }

    protected boolean willBeVisit = false;

    protected boolean hasBeenVisit = false;

    @Override
    public boolean equals(Object location) {
        return super.equals(location);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }
}
