package com.kxd.code.competition.robot.fight.fuck;

import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;

public class MiaoRobot extends AbstractFightRobot{

	public MiaoRobot() {
		super("塞恩");
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public CommonMoveAction getNextAction() {

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        MapContext mapContext = null;
        MoveActionCommandEnum moveActionCommand = null;
        try {
        	mapContext = new MapContext(fightRobotSeeEntity);
        	
        	//开启第一优先级
        	FirstPerorityEvent firstPerorityEvent = new FirstPerorityEvent();
        	moveActionCommand = firstPerorityEvent.hasOneRobot(mapContext);
        	if (moveActionCommand != null) {
                return new CommonMoveAction(moveActionCommand);
            }
        	moveActionCommand = firstPerorityEvent.getNextStep(mapContext);
        	if (moveActionCommand != null) {
                return new CommonMoveAction(moveActionCommand);
            }
        	//开启第二优先级
        	SecondPerorityEvent secondPerorityEvent = new SecondPerorityEvent();
        	moveActionCommand = secondPerorityEvent.getNextStep(mapContext);
        	if (moveActionCommand != null) {
                return new CommonMoveAction(moveActionCommand);
            }
        	//开启第三优先级
        	ThirdPerorityEvent thirdPerorityEvent = new ThirdPerorityEvent();
        	moveActionCommand = thirdPerorityEvent.getNextStep(mapContext);
        	if (moveActionCommand != null) {
                return new CommonMoveAction(moveActionCommand);
            }
        	return null;
		} catch (Exception e) {
			// TODO: handle exception
			SaienLoction location = mapContext.getOnselfLoction();
			if(location.x > 0){
				moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
			}else if(location.x < mapContext.getElementLists().length - 1){
				moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
			}else if(location.y > 0){
				moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
			}else if(location.y > mapContext.getElementLists().length - 1){
				moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
			}
			
			return new CommonMoveAction(moveActionCommand);
		}
	}
	public CommonMoveAction attackBobot(MapContext mapContext){
		FirstPerorityEvent firstPerorityEvent = new FirstPerorityEvent();
		MoveActionCommandEnum moveActionCommand = firstPerorityEvent.getNextStep(mapContext);
    	if (moveActionCommand == null) {
    		return searchBlood(mapContext);
        }
		return new CommonMoveAction(moveActionCommand);
	}
	
	public CommonMoveAction searchBlood(MapContext mapContext){
		SecondPerorityEvent secondPerorityEvent = new SecondPerorityEvent();
    	MoveActionCommandEnum moveActionCommand = secondPerorityEvent.getNextStep(mapContext);
    	if(moveActionCommand == null){
    		return searchRobot(mapContext);
    	}
		return new CommonMoveAction(moveActionCommand);
	}
	
	public CommonMoveAction searchRobot(MapContext mapContext){
		ThirdPerorityEvent thirdPerorityEvent = new ThirdPerorityEvent();
		MoveActionCommandEnum moveActionCommand = thirdPerorityEvent.getNextStep(mapContext);
		return new CommonMoveAction(moveActionCommand);
	}
}
