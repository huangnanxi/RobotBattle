package com.kxd.code.competition.robot.fight.fuck;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

/**
 * 第三优先级：有空格
 * @author miaoqingquan
 *
 */
public class ThirdPerorityEvent extends AbstractEvent{
	
	private int priority = 1;

	@Override
	public MoveActionCommandEnum getNextStep(MapContext mapContext) {
		Location location = mapContext.getOnselfLoction();
		
		ElementList[][] elementLists = mapContext.getElementLists();
		List<FightRobotBaseInfo> robots = mapContext.getRobots();
		List<PriorityMethod> priorityMethods = new ArrayList<>();
		List<Location> emptyLoctions = mapContext.getEmptyLoctions();
		for (int i = 0; i < 4; i++) {
			int fx = location.x + dx[i];
			int fy = location.y + dy[i];
			if(0 > fx || fx > elementLists.length - 1 ||  0> fy || fy >elementLists.length - 1){
				continue;
			}
			MoveActionCommandEnum moveActionCommandEnum = moveAction[i];
			SaienLoction nextSaienLoction = new SaienLoction(fx, fy,moveActionCommandEnum);
			SaienLoction saienLoction = new SaienLoction(location.x, location.y);
			PriorityMethod priorityMethod = getPriorityStep(saienLoction,nextSaienLoction,mapContext);
			priorityMethods.add(priorityMethod);
		}
		
		MoveActionCommandEnum moveActionCommand = null;
		//比较完成后的优先级列表
        priority = 1;
        List<PriorityMethod> priorityMethodList = comparePriority(priorityMethods);
        PriorityMethod priorityMethodByOne = priorityMethodList.get(0);
    	priority = priorityMethodByOne.getPriority();
    	//分情况判断
    	if(priority == 1){
    		//因为沙盒规则变更，相应调整
    		//当自己血量足够时，至少是场上前1/5
    		if(mapContext.isAttack()){//寻找附近最近，并且血量比自己低的机器人开干
            	
    			moveActionCommand = getNextAttackRobot(priorityMethodByOne,location,robots,mapContext);
            }
    		if(moveActionCommand == null){
    			//去往人最少的地方
        		Random random = new Random(System.currentTimeMillis());
                Location fewerPeoperLocationLoction = emptyLoctions.get(Math.abs(random.nextInt()) % emptyLoctions.size());
                SaienLoction start = new SaienLoction(location.x, location.y);
                SaienLoction end = new SaienLoction(fewerPeoperLocationLoction.x, fewerPeoperLocationLoction.y);
                Stack<SaienLoction> stack= printPath(start, end, mapContext,ElementTypeEnum.BLOOD_BAG);
                moveActionCommand = stack.size() == 0 ? priorityMethodByOne.getMoveActionCommandEnum() : stack.get(stack.size()-1).getMoveActionCommandEnum();
    		}	
    	}else if(priority == 2){//如果都是有被机器人踩的可能，优先走血量低的一方
    		//先找到机器人最少的方位,并且血量最低
    		List<NextPointRobotInfo> nextPointRobotInfos = new ArrayList<>();
    		for (PriorityMethod priorityMethod : priorityMethodList) {
    			SaienLoction saienLoction = new SaienLoction(priorityMethod.getLocation().x, priorityMethod.getLocation().y);
    			NextPointRobotInfo nextPointRobotInfo = getNextPointRobotInfo(saienLoction,mapContext);
    			nextPointRobotInfos.add(nextPointRobotInfo);
			}
    		
    		NextPointRobotInfo leastnextPointRobotInfo = new NextPointRobotInfo();
			List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<>();
    		for (NextPointRobotInfo nextPointRobotInfo : nextPointRobotInfos) {
    			if(fightRobotBaseInfos.size() > nextPointRobotInfo.getFightRobotBaseInfos().size()
						|| nextPointRobotInfos.indexOf(nextPointRobotInfo)==0){
					fightRobotBaseInfos = nextPointRobotInfo.getFightRobotBaseInfos();
					leastnextPointRobotInfo = nextPointRobotInfo;
				}else if(fightRobotBaseInfos.size() == nextPointRobotInfo.getFightRobotBaseInfos().size()){//判断血量
					int bloodNum1 = 0,bloodNum2 = 0;
					for (FightRobotBaseInfo fightRobotBaseInfo2 : fightRobotBaseInfos) {
						if(fightRobotBaseInfo2.bloodNum < bloodNum1 || fightRobotBaseInfos.indexOf(fightRobotBaseInfo2) == 0){
							bloodNum1 = fightRobotBaseInfo2.getBloodBagNum;
						}
					}
					for (FightRobotBaseInfo fightRobotBaseInfo3 : fightRobotBaseInfos) {
						if(fightRobotBaseInfo3.bloodNum < bloodNum2 || fightRobotBaseInfos.indexOf(fightRobotBaseInfo3) == 0){
							bloodNum2 = fightRobotBaseInfo3.getBloodBagNum;
						}
					}
					if(bloodNum2 < bloodNum1){
						fightRobotBaseInfos = nextPointRobotInfo.getFightRobotBaseInfos();
    					leastnextPointRobotInfo = nextPointRobotInfo;
					}
				}
			}
    		
    		moveActionCommand = leastnextPointRobotInfo.getMoveActionCommandEnum();
    	}else if(priority == 3){//如果全是雷区
    		
    		moveActionCommand = priorityMethodByOne.getMoveActionCommandEnum();
    	}else if(priority == 4){//如果全是雷区
    		
    		moveActionCommand = priorityMethodByOne.getMoveActionCommandEnum();
    	}
    	return moveActionCommand;
	}
	
	
	private List<PriorityMethod> comparePriority(List<PriorityMethod> priorityMethods){
		List<PriorityMethod> priorityMethodList = new ArrayList<>();
		for (PriorityMethod priorityMethod : priorityMethods) {
			if(priority == priorityMethod.getPriority()){
				priorityMethodList.add(priorityMethod);
			}
		}
		//没有当前的优先级，递归
		if(priorityMethodList.size() == 0){
			priority = priority + 1;
			return comparePriority(priorityMethods);
		}else{
			priority = 1;
			return priorityMethodList;
		}
	}
	
	private PriorityMethod getPriorityStep(SaienLoction location,SaienLoction locationNextStep,MapContext mapContext){
		PriorityMethod priorityMethod = new PriorityMethod();
		priorityMethod.setMoveActionCommandEnum(locationNextStep.getMoveActionCommandEnum());
		priorityMethod.setLocation(locationNextStep);
		//没有机器人也没有炸弹，第一优先
		if(!validateRobot(locationNextStep,mapContext) && !validateLand(locationNextStep, mapContext) && !validateLocRobot(locationNextStep,mapContext)){//当前雷区可走的特殊情况，是其他三个方向存在机器人或者雷更加多
			priorityMethod.setPriority(1);
		//有机器人，是第二优先级
        }else if(validateRobot(locationNextStep,mapContext) && !validateLand(locationNextStep, mapContext)){
        	priorityMethod.setPriority(2);
        //有地雷是第三优先级
        }else if(!validateRobot(locationNextStep,mapContext) && validateLand(locationNextStep, mapContext)){
        	priorityMethod.setPriority(3);
        }else if(validateLocRobot(locationNextStep,mapContext)){//如果注定要死，不要勉强
        	priorityMethod.setPriority(4);
        }
		return priorityMethod;
	}

	public class NextPointRobotInfo{
    	//将要移动点的机器人的数量
    	private List<FightRobotBaseInfo> fightRobotBaseInfos =new ArrayList<>();
    	//移动的方向
    	private MoveActionCommandEnum moveActionCommandEnum;
		public List<FightRobotBaseInfo> getFightRobotBaseInfos() {
			return fightRobotBaseInfos;
		}
		public void setFightRobotBaseInfos(List<FightRobotBaseInfo> fightRobotBaseInfos) {
			this.fightRobotBaseInfos = fightRobotBaseInfos;
		}
		public MoveActionCommandEnum getMoveActionCommandEnum() {
			return moveActionCommandEnum;
		}
		public void setMoveActionCommandEnum(MoveActionCommandEnum moveActionCommandEnum) {
			this.moveActionCommandEnum = moveActionCommandEnum;
		}
    	
    }
	
	private NextPointRobotInfo getNextPointRobotInfo(SaienLoction location,MapContext mapContext){
		List<FightRobotBaseInfo> robots = mapContext.getRobots();
    	NextPointRobotInfo nextPointRobotInfo = new NextPointRobotInfo();
    	List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<>();
        ElementList[][] elementLists = mapContext.getElementLists();
        for (int i = 0; i < 4; i++) {
			int fx = location.x + dx[i];
			int fy = location.y + dy[i];
			if(0 > fx || fx > elementLists.length - 1 ||  0> fy || fy >elementLists.length - 1){
				continue;
			}
			MoveActionCommandEnum moveActionCommandEnum = moveAction[i];
			SaienLoction nextSaienLoction = new SaienLoction(fx, fy,moveActionCommandEnum);
			for (FightRobotBaseInfo fightRobotBaseInfo : robots) {
				//如果这3个位置都有机器人，不能走
	        	if(fightRobotBaseInfo.currentLocation.equals(nextSaienLoction)){
	        		fightRobotBaseInfos.add(fightRobotBaseInfo);
	        	}
			}
			nextPointRobotInfo.setFightRobotBaseInfos(fightRobotBaseInfos);
			nextPointRobotInfo.setMoveActionCommandEnum(moveActionCommandEnum);
        }
    	return nextPointRobotInfo;
    }
	
	private MoveActionCommandEnum getNextAttackRobot(PriorityMethod priorityMethodByOne,Location location,List<FightRobotBaseInfo> robots,MapContext mapContext){
    	FightRobotBaseInfo fightRobotBaseInfo = null;
    	int minDistance = 100000;
    	for (FightRobotBaseInfo robot : robots) {
    		int distance = Math.abs(robot.currentLocation.x - location.x) + Math.abs(robot.currentLocation.y - location.y);
    		if(distance % 2 == 1 && distance < minDistance && robot.bloodNum <= mapContext.getOnselfRobot().bloodNum){
    			fightRobotBaseInfo = robot;
    			minDistance = distance;
    		}
		}
    	
    	//找到了最优机器人，开始移动
    	List<SaienLoction> saienLoctions = getFieldMoveActions(new SaienLoction(location.x, location.y),mapContext);
    	
    	if(fightRobotBaseInfo != null ){
    		for (SaienLoction saienLoction : saienLoctions) {
        		int x = Math.abs(saienLoction.x - fightRobotBaseInfo.currentLocation.x);
        		int y = Math.abs(saienLoction.y - fightRobotBaseInfo.currentLocation.y);
        		int distance = x + y;
    			if(minDistance > distance && x != 0 && y != 0){
    				return saienLoction.getMoveActionCommandEnum();
    			}
    		}
    	}
    	
    	return null;
	}
}
