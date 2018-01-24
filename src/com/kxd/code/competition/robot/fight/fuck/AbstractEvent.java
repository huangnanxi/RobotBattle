package com.kxd.code.competition.robot.fight.fuck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

public abstract class AbstractEvent {
	public final static int[] dx = { -1, 1, 0, 0 };
	public final static int[] dy = { 0, 0, -1, 1 };
	public final static MoveActionCommandEnum[] moveAction = { MoveActionCommandEnum.MOVE_TOP, MoveActionCommandEnum.MOVE_DOWN, MoveActionCommandEnum.MOVE_LEFT, MoveActionCommandEnum.MOVE_RIGHT };

	public abstract MoveActionCommandEnum getNextStep(MapContext mapContext);

	public static Stack<SaienLoction> printPath(SaienLoction start,
			SaienLoction end, MapContext mapContext,ElementTypeEnum elementTypeEnum) {

		/*
		 * 不用PriorityQueue是因为必须取出存在的元素
		 */
		ArrayList<SaienLoction> openTable = new ArrayList<SaienLoction>();
		ArrayList<SaienLoction> closeTable = new ArrayList<SaienLoction>();
		openTable.clear();
		closeTable.clear();
		Stack<SaienLoction> pathStack = new Stack<SaienLoction>();
		start.parent = null;
		// 该点起到转换作用，就是当前扩展点
		SaienLoction currentSaienLoction = new SaienLoction(start.x, start.y);
		// closeTable.add(currentSaienLoction);
		boolean flag = true;
		ElementList[][] elementLists = mapContext.getElementLists();
		int j = 0;
		while (flag) {
			for (int i = 0; i < 4; i++) {
				int fx = currentSaienLoction.x + dx[i];
				int fy = currentSaienLoction.y + dy[i];
				MoveActionCommandEnum moveActionCommandEnum = moveAction[i];
				SaienLoction tempSaienLoction = new SaienLoction(fx, fy,moveActionCommandEnum);
				if(j == 0 && (validateRobot(tempSaienLoction,mapContext) || validateLocRobot(tempSaienLoction,mapContext))){//位置上是不是有机器人，机器人血量是不是比自己高
					j++;
					continue;
				}
				if(0 > fx || fx > elementLists.length - 1 ||  0> fy || fy >elementLists.length - 1){
					continue;
				}
				ElementList elementList = elementLists[fx][fy];
				if ((elementList.elements.size() !=0 
						&& elementList.elements.get(0).elementType == ElementTypeEnum.LANDMINE)) {
					// 如果是地雷的话
					continue;
				} else {
					if (end.equals(tempSaienLoction)) {
						flag = false;
						end.parent = currentSaienLoction;
						end.moveActionCommandEnum = tempSaienLoction.moveActionCommandEnum;
						break;
					}
					if (i < 4) {
						tempSaienLoction.G = currentSaienLoction.G + 10;
					} else {
						tempSaienLoction.G = currentSaienLoction.G + 14;
					}
					tempSaienLoction.H = SaienLoction.getDis(tempSaienLoction,
							end);
					tempSaienLoction.F = tempSaienLoction.G
							+ tempSaienLoction.H;
					if (openTable.contains(tempSaienLoction)) {
						int pos = openTable.indexOf(tempSaienLoction);
						SaienLoction temp = openTable.get(pos);
						if (temp.F > tempSaienLoction.F) {
							openTable.remove(pos);
							openTable.add(tempSaienLoction);
							tempSaienLoction.parent = currentSaienLoction;
						}
					} else if (closeTable.contains(tempSaienLoction)) {
						int pos = closeTable.indexOf(tempSaienLoction);
						SaienLoction temp = closeTable.get(pos);
						if (temp.F > tempSaienLoction.F) {
							closeTable.remove(pos);
							openTable.add(tempSaienLoction);
							tempSaienLoction.parent = currentSaienLoction;
						}
					} else {
						openTable.add(tempSaienLoction);
						tempSaienLoction.parent = currentSaienLoction;
					}

				}
			}
			

			if (openTable.isEmpty()) {
				break;
			}// 无路径
			if (false == flag) {
				break;
			}// 找到路径
			openTable.remove(currentSaienLoction);
			if (openTable.isEmpty()) {
				break;
			}// 无路径
			closeTable.add(currentSaienLoction);
			Collections.sort(openTable);
			currentSaienLoction = openTable.get(0);

		}
		SaienLoction node = end;
		while (node.parent != null) {
			pathStack.push(node);
			node = node.parent;
		}
		return pathStack;
	}
	
	//匹配雷区
	public static boolean validateLand(SaienLoction location,MapContext mapContext){
    	List<Landmine> landmines = mapContext.getLands();
    	for (Landmine landmine : landmines) {
    		if(landmine.location.equals(location)){
    			return true;
    		}
		}
    	return false;
    }
    
    //匹配当前位置机器人
    public static boolean validateLocRobot(SaienLoction location,MapContext mapContext){
    	FightRobotBaseInfo onselfRobot = mapContext.getOnselfRobot();
    	List<FightRobotBaseInfo> robots = mapContext.getRobots();
    	for (FightRobotBaseInfo fightRobotBaseInfo : robots) {
    		if(location.equals(new SaienLoction(fightRobotBaseInfo.currentLocation.x, fightRobotBaseInfo.currentLocation.y))){
    			if(fightRobotBaseInfo.bloodNum >  onselfRobot.bloodNum){
    				return true;
    			}
    		}
		}
    	return false;
    }
    
    //匹配周边机器人
    public static boolean validateRobot(SaienLoction locationNextStep,MapContext mapContext){
        List<FightRobotBaseInfo> robots = mapContext.getRobots();
        for (FightRobotBaseInfo fightRobotBaseInfo : robots) {
			int x = Math.abs(fightRobotBaseInfo.currentLocation.x-locationNextStep.x);
			int y = Math.abs(fightRobotBaseInfo.currentLocation.y-locationNextStep.y);
			if(x + y == 1){
				return true;
			}
		}
        
    	return false;
    }
    
    public List<SaienLoction> getFieldMoveActions(SaienLoction saienLoction,MapContext mapContext){
    	List<SaienLoction> saienLoctions = new ArrayList<>();
    	for (int i = 0; i < 4; i++) {
			int fx = saienLoction.x + dx[i];
			int fy = saienLoction.y + dy[i];
			if(0 > fx || fx > mapContext.getSize() - 1 ||  0> fy || fy >mapContext.getSize() - 1){
				continue;
			}
			MoveActionCommandEnum moveActionCommandEnum = moveAction[i];
			
			SaienLoction tempSaienLoction = new SaienLoction(fx, fy,moveActionCommandEnum);
			if(!validateRobot(tempSaienLoction,mapContext) && !validateLand(tempSaienLoction,mapContext)){
				saienLoctions.add(tempSaienLoction);
			}
    	}
    	return saienLoctions;
    }
    
    //踩雷吃血的策略
    public MoveActionCommandEnum landmineIsFuck(SaienLoction saienLoction,MapContext mapContext){
    	List<Landmine> landmines = mapContext.getLands();
    	List<Landmine> landmineList = new ArrayList<>();
		for (Landmine landmine : landmines) {
			landmineList.add(landmine);
		}
		Stack<SaienLoction> stack = xXXX(saienLoction,landmineList,mapContext,0);
    	if(stack.size() > 0 ){
    		return stack.get(stack.size() - 1).getMoveActionCommandEnum();
    	}
    	return null;
    }
    
    public static Stack<SaienLoction> xXXX(SaienLoction start,List<Landmine> landmines, MapContext mapContext,int landminNum) {
    	List<BloodBag> bloodBags = mapContext.getBloodBags();
    	Iterator<Landmine> iterator =landmines.iterator();
    	while (iterator.hasNext()) {
    		Landmine landmine = iterator.next();
    		SaienLoction saienLoction = new SaienLoction(landmine.location.x,landmine.location.y);
    		Stack<SaienLoction> stack = landmineIsFuckFuck(start,saienLoction,landmines,mapContext);
    		if(stack.size() > 0){//可以到达的地雷有没有可以到达多个血点的
    			int i = 0;
    			for (BloodBag bloodBag : bloodBags) {
    				Stack<SaienLoction> stack2 = landmineIsFuckFuck(saienLoction,new SaienLoction(bloodBag.location.x, bloodBag.location.y),landmines,mapContext);
    				if(stack2.size() > 0){//可以到达的血包
    					i++;
    				}
				}
    			if(i > landminNum){
    				return stack;
    			}
    			iterator.remove();
    		}
    		
		}
    	landminNum ++;
    	
    	return xXXX(start,landmines,mapContext,landminNum);
    }
    
    
    public static Stack<SaienLoction> landmineIsFuckFuck(SaienLoction start,SaienLoction end,List<Landmine> landmines, MapContext mapContext) {

    	ArrayList<SaienLoction> openTable = new ArrayList<SaienLoction>();
		ArrayList<SaienLoction> closeTable = new ArrayList<SaienLoction>();
		openTable.clear();
		closeTable.clear();
		Stack<SaienLoction> pathStack = new Stack<SaienLoction>();
		start.parent = null;
		// 该点起到转换作用，就是当前扩展点
		SaienLoction currentSaienLoction = new SaienLoction(start.x, start.y);
		// closeTable.add(currentSaienLoction);
		boolean flag = true;
		ElementList[][] elementLists = mapContext.getElementLists();
		while (flag) {
			for (int i = 0; i < 4; i++) {
				Boolean hasLand = false;
				int fx = currentSaienLoction.x + dx[i];
				int fy = currentSaienLoction.y + dy[i];
				MoveActionCommandEnum moveActionCommandEnum = moveAction[i];
				SaienLoction tempSaienLoction = new SaienLoction(fx, fy,moveActionCommandEnum);
				if(0 > fx || fx > elementLists.length - 1 ||  0> fy || fy >elementLists.length - 1){
					continue;
				}
				if (!end.equals(tempSaienLoction)) {
					for (Landmine landmine : landmines) {
						if(tempSaienLoction.x == landmine.location.x && tempSaienLoction.y == landmine.location.y){
							hasLand = true;
							break;
						}
					}
				}
				if(hasLand){
					continue;
				}

				if (end.equals(tempSaienLoction)) {
					flag = false;
					end.parent = currentSaienLoction;
					end.moveActionCommandEnum = tempSaienLoction.moveActionCommandEnum;
					break;
				}
				if (i < 4) {
					tempSaienLoction.G = currentSaienLoction.G + 10;
				} else {
					tempSaienLoction.G = currentSaienLoction.G + 14;
				}
				tempSaienLoction.H = SaienLoction.getDis(tempSaienLoction,
						end);
				tempSaienLoction.F = tempSaienLoction.G
						+ tempSaienLoction.H;
				if (openTable.contains(tempSaienLoction)) {
					int pos = openTable.indexOf(tempSaienLoction);
					SaienLoction temp = openTable.get(pos);
					if (temp.F > tempSaienLoction.F) {
						openTable.remove(pos);
						openTable.add(tempSaienLoction);
						tempSaienLoction.parent = currentSaienLoction;
					}
				} else if (closeTable.contains(tempSaienLoction)) {
					int pos = closeTable.indexOf(tempSaienLoction);
					SaienLoction temp = closeTable.get(pos);
					if (temp.F > tempSaienLoction.F) {
						closeTable.remove(pos);
						openTable.add(tempSaienLoction);
						tempSaienLoction.parent = currentSaienLoction;
					}
				} else {
					openTable.add(tempSaienLoction);
					tempSaienLoction.parent = currentSaienLoction;
				}

			}
			

			if (openTable.isEmpty()) {
				break;
			}// 无路径
			if (false == flag) {
				break;
			}// 找到路径
			openTable.remove(currentSaienLoction);
			if (openTable.isEmpty()) {
				break;
			}// 无路径
			closeTable.add(currentSaienLoction);
			Collections.sort(openTable);
			currentSaienLoction = openTable.get(0);

		}
		SaienLoction node = end;
		while (node.parent != null) {
			pathStack.push(node);
			node = node.parent;
		}
		return pathStack;
	}
    
    public MoveActionCommandEnum hasOneRobot(MapContext mapContext){
    	SaienLoction location = mapContext.getOnselfLoction();
    	List<FightRobotBaseInfo> fightRobotBaseInfos = mapContext.getRobots();
    	int i =0;
    	FightRobotBaseInfo fightRobotBaseInfoLast = null;
    	for (FightRobotBaseInfo fightRobotBaseInfo : fightRobotBaseInfos) {
    		SaienLoction start = new SaienLoction(location.x, location.y);
	        SaienLoction end = new SaienLoction(fightRobotBaseInfo.currentLocation.x, fightRobotBaseInfo.currentLocation.y);
	        Stack<SaienLoction> stack= printPath(start, end, mapContext,ElementTypeEnum.BLOOD_BAG);
	        if(stack.size() > 0){
	        	fightRobotBaseInfoLast = fightRobotBaseInfo;
	        	i++;
	        }
		}
    	
    	if(i == 1 && fightRobotBaseInfos.size() != 1){//只能干一个机器人了，最高优先级
    		//找到了最优机器人，开始移动
        	List<SaienLoction> saienLoctions = getFieldMoveActions(new SaienLoction(location.x, location.y),mapContext);
        	int minDistance = 100000;
        	if(fightRobotBaseInfoLast != null ){
        		for (SaienLoction saienLoction : saienLoctions) {
            		int x = Math.abs(saienLoction.x - fightRobotBaseInfoLast.currentLocation.x);
            		int y = Math.abs(saienLoction.y - fightRobotBaseInfoLast.currentLocation.y);
            		int distance = x + y;
        			if(minDistance > distance && x != 0 && y != 0){
        				return saienLoction.getMoveActionCommandEnum();
        			}
        		}
        	}
    	}
    	
    	return null;
    }
}
