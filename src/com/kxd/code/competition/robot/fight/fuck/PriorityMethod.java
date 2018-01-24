package com.kxd.code.competition.robot.fight.fuck;

import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.Location;

public class PriorityMethod {

	private Location location;
	
	private int priority;
	
	private MoveActionCommandEnum moveActionCommandEnum;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public MoveActionCommandEnum getMoveActionCommandEnum() {
		return moveActionCommandEnum;
	}

	public void setMoveActionCommandEnum(MoveActionCommandEnum moveActionCommandEnum) {
		this.moveActionCommandEnum = moveActionCommandEnum;
	}
}
