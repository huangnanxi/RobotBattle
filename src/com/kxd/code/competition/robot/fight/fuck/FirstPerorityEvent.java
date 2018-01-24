package com.kxd.code.competition.robot.fight.fuck;

import java.util.List;

import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;
/**
 * 第一优先级：可以移动的四个方向有机器人
 * @author miaoqingquan
 *
 */
public class FirstPerorityEvent extends AbstractEvent{

	@Override
	public MoveActionCommandEnum getNextStep(MapContext mapContext) {
		Location location = mapContext.getOnselfLoction();
		FightRobotBaseInfo onselfRobotBaseInfo = mapContext.getOnselfRobot();
		List<FightRobotBaseInfo> fightRobotBaseInfos = mapContext.getRobots();
		for (int i = 0; i < 4; i++) {
			int fx = location.x + dx[i];
			int fy = location.y + dy[i];
			for (FightRobotBaseInfo fightRobotBaseInfo : fightRobotBaseInfos) {
				if(fightRobotBaseInfo.currentLocation.x == fx
						&& fightRobotBaseInfo.currentLocation.y == fy 
						&& onselfRobotBaseInfo.bloodNum >= fightRobotBaseInfo.bloodNum && mapContext.isAttack()){
					System.out.println("oneseifBllod:" + onselfRobotBaseInfo.bloodNum +"robotBlood:" + fightRobotBaseInfo.bloodNum);
					return moveAction[i];
				}
			}
		}
		return null;
	}
}
