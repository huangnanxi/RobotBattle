package com.kxd.code.competition.robot.fight;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.mapinfo.FightMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

public class WaliFightRobot4 extends AbstractFightRobot {
	public WaliFightRobot4() {
        super("瓦力");
    }
	
	//地图横纵坐标
	class P{
		public int a, b;
		public P(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}
	
	//根据下一步坐标判断移动方向
	class Q{
		public Location location;
		public MoveActionCommandEnum moveActionCommandEnum;
		public Q(Location location, MoveActionCommandEnum moveActionCommandEnum) {
			this.location = location;
			this.moveActionCommandEnum = moveActionCommandEnum;
		}
	}
	
	//四个方向对应横坐标变化
	public int dx[]= {-1,0,1,0};
	
	//四个方向对应纵坐标变化
	public int dy[]= {0,1,0,-1};
	
	//地图
	public int[][] maze;
	
	public FightRobotSeeEntity fightRobotSeeEntity;
	
	public ElementList[][] elementLists;
	
	public Location local;
	
	public FightMapInfo mapInfo;
	
	public FightRobotBaseInfo baseInfo;
	
	//禁止的方向集合
	public List<MoveActionCommandEnum> forbidDirection;
	
	//移动方向
	public CommonMoveAction actionEntity;
	 
	@Override
    public CommonMoveAction getNextAction() {
        fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        elementLists = fightRobotSeeEntity.robotGetElementList;
        
        local = currentSeeMazeSituation.robotBaseInfo.currentLocation;
        
        mapInfo = (FightMapInfo)currentSeeMazeSituation.mapInfo;
        
        baseInfo = (FightRobotBaseInfo)currentSeeMazeSituation.robotBaseInfo;
        
        maze = new int[mapInfo.size][mapInfo.size];
        
        forbidDirection = new ArrayList<MoveActionCommandEnum>();
        
        //地图初始化
        for(int i=0;i<mapInfo.size;i++) {
        	for(int j=0;j<mapInfo.size;j++) {
        		maze[i][j]=0;
        	}
        }
        
        // 当前所见的敌方机器人、血包存到list，血包、地雷放入自定义地图中
        List<FightRobotBaseInfo> enemyRobots = new ArrayList<>();
        List<BloodBag> bloodBags = new ArrayList<>();
        List<Landmine> landmines = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
            	//不在自己的坐标时
            	if(i!=local.x||j!=local.y){
            		ElementList elementList = elementLists[i][j];
            		for (AbstractElement element : elementList.elements) {
            			//血包
            			if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
            				BloodBag bloodBag = (BloodBag) element;
            				bloodBags.add(bloodBag);
            				maze[i][j]=2;
            			}
            			//地雷
            			if (ElementTypeEnum.LANDMINE == element.elementType) {
            				Landmine landmine = (Landmine) element;
            				landmines.add(landmine);
            				maze[i][j]=-2;
            			}
            			//敌方机器人
                        if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        	FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        	if (fightRobotBaseInfo.bloodNum > 0) {
                        		enemyRobots.add(fightRobotBaseInfo);
                        	}
                        }
                    }
            	}else{//在自己的坐标时
            		ElementList elementList = elementLists[i][j];
            		for (AbstractElement element : elementList.elements) {
                        if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        	FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        	//敌方机器人
                        	if (fightRobotBaseInfo.bloodNum > 0&&!fightRobotBaseInfo.name.equals(baseInfo.name)) {
                        		enemyRobots.add(fightRobotBaseInfo);
                        	}
                        }
                    }
            	}
            }
        }
        //敌方机器人
        sortRobotsByDistance(enemyRobots, local, 0, enemyRobots.size()-1);
		sortBloodBags(bloodBags, local, landmines, 0, bloodBags.size()-1);
		boolean chase = false;
        for(FightRobotBaseInfo enemyRobot:enemyRobots){
	        //距离为1
	        if(arrivalSafePlace(local, enemyRobot.currentLocation, new ArrayList<MoveActionCommandEnum>(), false)==1) {
	        	if(baseInfo.bloodNum>=enemyRobot.bloodNum||(baseInfo.bloodNum%2==1&&baseInfo.bloodNum>=enemyRobot.bloodNum-1)){
	        		arrivalSafePlace(local, enemyRobot.currentLocation, new ArrayList<MoveActionCommandEnum>(), true);
	        		return actionEntity;
	        	}else {
	        		List<Q> nextLocationList = nextActionLocation(local);
	        		for(int i=0;i<nextLocationList.size();i++) {
	        			if(nextLocationList.get(i).location.x==enemyRobot.currentLocation.x&&nextLocationList.get(i).location.y==enemyRobot.currentLocation.y) {
	        				forbidDirection.add(nextLocationList.get(i).moveActionCommandEnum);
	        			}
	        		}
	        	}
	        }
	        //距离为2，且血量小于等于敌方血量加一
	        if(arrivalSafePlace(local, enemyRobot.currentLocation, new ArrayList<MoveActionCommandEnum>(), false)==2&&(baseInfo.bloodNum<enemyRobot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum==enemyRobot.bloodNum+1))) {
	    		List<Q> nextLocationList = nextActionLocation(local);
	    		for(int i=0;i<nextLocationList.size();i++) {
	    			if(arrivalSafePlace(nextLocationList.get(i).location, enemyRobot.currentLocation, new ArrayList<MoveActionCommandEnum>(), false)==1) {
	    				forbidDirection.add(nextLocationList.get(i).moveActionCommandEnum);
	    			}
	    		}
	        }
	        if(!bloodBags.isEmpty()){
	        	int a = arrivalSafePlace(local, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(), false);
	        	int b = arrivalSafePlace(enemyRobot.currentLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(), false);
	        	if(b<a||(b==a&&(baseInfo.bloodNum<=enemyRobot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum<=enemyRobot.bloodNum)))){
	        		chase = true;
	        	}
	        }
        }
        for(FightRobotBaseInfo enemyRobot:enemyRobots){
	        //距离为3（斜对角），地图上不存在血包，且血量大于敌方减一
	        if(arrivalSafePlace(local, enemyRobot.currentLocation, forbidDirection, false)==3&&((Math.abs(local.x-enemyRobot.currentLocation.x)==2&&Math.abs(local.y-enemyRobot.currentLocation.y)==1)||(Math.abs(local.x-enemyRobot.currentLocation.x)==1&&Math.abs(local.y-enemyRobot.currentLocation.y)==2))&&(baseInfo.bloodNum>=enemyRobot.bloodNum||(baseInfo.bloodNum%2==1&&baseInfo.bloodNum>=enemyRobot.bloodNum-1))&&bloodBags.isEmpty()&&chase==true){
	        	List<Q> nextLocationList = nextActionLocation(local);
	        	for(int i=0;i<nextLocationList.size();i++) {
	        		if(arrivalSafePlace(nextLocationList.get(i).location, enemyRobot.currentLocation, forbidDirection,false)==2&&Math.abs(nextLocationList.get(i).location.x-enemyRobot.currentLocation.x)==1&&Math.abs(nextLocationList.get(i).location.y-enemyRobot.currentLocation.y)==1&&maze[nextLocationList.get(i).location.x][nextLocationList.get(i).location.y]!=-2) {
	        			arrivalSafePlace(local, nextLocationList.get(i).location, forbidDirection, true);
	        			return actionEntity;
	        		}
	        	}
	        }
	        //距离为3（斜对角），地图上存在血包，且血量大于敌方
	        if(arrivalSafePlace(local, enemyRobot.currentLocation, forbidDirection, false)==3&&((Math.abs(local.x-enemyRobot.currentLocation.x)==2&&Math.abs(local.y-enemyRobot.currentLocation.y)==1)||(Math.abs(local.x-enemyRobot.currentLocation.x)==1&&Math.abs(local.y-enemyRobot.currentLocation.y)==2))&&(baseInfo.bloodNum>=enemyRobot.bloodNum+1||(baseInfo.bloodNum%2==1&&baseInfo.bloodNum>=enemyRobot.bloodNum))&&!bloodBags.isEmpty()&&chase==true){
	        	List<Q> nextLocationList = nextActionLocation(local);
	        	for(int i=0;i<nextLocationList.size();i++) {
	        		if(arrivalSafePlace(nextLocationList.get(i).location, enemyRobot.currentLocation, new ArrayList<MoveActionCommandEnum>(),false)==2&&Math.abs(nextLocationList.get(i).location.x-enemyRobot.currentLocation.x)==1&&Math.abs(nextLocationList.get(i).location.y-enemyRobot.currentLocation.y)==1&&maze[nextLocationList.get(i).location.x][nextLocationList.get(i).location.y]!=-2) {
	        			arrivalSafePlace(local, nextLocationList.get(i).location, forbidDirection,true);
	        			return actionEntity;
	        		}
	        	}
	        }
        }
        FightRobotBaseInfo nearEnemyRobot = enemyRobots.get(0);
        for(int i=0;i<enemyRobots.size();i++){
        	if(enemyRobots.get(i).bloodNum>baseInfo.bloodNum-1){
        		maze[enemyRobots.get(i).currentLocation.x][enemyRobots.get(i).currentLocation.y]=-3;
        	}
        }
        //地图上存在两个血包
        if(bloodBags.size()>=2){
        	if(enemyRobots.size()>1){
        		return getNextActionByTwoBloodBagsManyRobots(bloodBags,local,landmines,nearEnemyRobot);
        	}else{
        		return getNextActionByTwoBloodBags(bloodBags,local,landmines,nearEnemyRobot);
        	}
        }
        else if(bloodBags.size()==1){
        	if(enemyRobots.size()>1){
        		return getNextActionByOneBloodBagManyRobots(bloodBags,local,landmines,nearEnemyRobot);
        	}else{
        		return getNextActionByOneBloodBag(bloodBags,local,landmines,nearEnemyRobot);
        	}
        }else{
        	if(baseInfo.bloodNum>nearEnemyRobot.bloodNum){
        		return MoveToChase(nearEnemyRobot);
        	}else{
        		return MoveToSafePlace();
        	}
        }
    }
	
	/**
	 * 将机器人按距离排序
	 * @param list
	 * @param location
	 * @param left
	 * @param right
	 */
	public void sortRobotsByDistance(List<FightRobotBaseInfo> list, Location location, int left, int right){
		if(left>=right) {
			return;
		}
		int index = partition1(list,location,left,right);
		sortRobotsByDistance(list,location,left,index-1);
		sortRobotsByDistance(list,location,index+1,right);
	}
	
	public int partition1(List<FightRobotBaseInfo> list, Location location, int a, int b){
		int pivot = Math.abs(list.get(a).currentLocation.x-location.x)+Math.abs(list.get(a).currentLocation.y-location.y);
		FightRobotBaseInfo c = list.get(a);
		while(a<b) {
			while(Math.abs(list.get(b).currentLocation.x-location.x)+Math.abs(list.get(b).currentLocation.y-location.y)>=pivot&&b>a) {
				b--;
			}
			list.set(a, list.get(b));
			while(Math.abs(list.get(a).currentLocation.x-location.x)+Math.abs(list.get(a).currentLocation.y-location.y)<=pivot&&b>a) {
				a++;
			}
			list.set(b, list.get(a));
		}
		list.set(b, c);
		return b;
	}
	
	
	/**
	 * 当地图存在两个血包时的策略
	 * @param bloodBags
	 * @param location
	 * @param landmines
	 * @param robot
	 * @return
	 */
	public CommonMoveAction getNextActionByTwoBloodBagsManyRobots(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot) {
		int a1 = arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false);
		int a2 = arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false);
		int a3 = arrivalSafePlace(bloodBags.get(0).location, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false);
		int b1 = arrivalSafePlace(robot.currentLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false);
		int b2 = arrivalSafePlace(robot.currentLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false);
		if(a1==-1&&a2!=-1) {
			if(a2<b2||(a2==b2&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		if(a1!=-1&&a2==-1) {
			if(a1<b1||(a1==b1&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		if(a1!=-1&&a2!=-1) {
			return getNextActionByTwoBloodBags(bloodBags,location,landmines,robot,a1,a2,a3,b1,b2,0,0);
		}else {
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
	}
	
	public CommonMoveAction getNextActionByTwoBloodBagsManyRobots(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot,int a1,int a2,int a3,int b1,int b2) {
		if(a3!=-1) {
			if(b1!=-1&&b2!=-1) {
				if(a1<=b1&&a2<=b2) {
					if(a1+a3<b2||(a1+a3==b2&&(baseInfo.bloodNum>=robot.bloodNum||(baseInfo.bloodNum%2==1&&baseInfo.bloodNum>=robot.bloodNum-1)))) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
						return actionEntity;
					}
					if(a2+a3<b1||(a2+a3==b2&&(baseInfo.bloodNum>=robot.bloodNum||(baseInfo.bloodNum%2==1&&baseInfo.bloodNum>=robot.bloodNum-1)))) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
						return actionEntity;
					}else {
						if(b1<=b2) {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
							return actionEntity;
						}else {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
							return actionEntity;
						}
					}
				}
				
				if(a1<=b1&&a2>=b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
					return actionEntity;
				}
				
				if(a1>=b1&&a2<=b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
					return actionEntity;
				}
				
				if(a1>b1&&a2>b2) {
					if(b1+a3<=a2&&b2+a3>=a1) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
						return actionEntity;
					}
					if(b2+a3<=a1&&b1+a3>=a2) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
						return actionEntity;
					}
					if(b1+a3>a2&&b2+a3>a1) {
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_LEFT)&&location.y-1>=0&&maze[location.x][location.y-1]!=-2) {
							Location nextLocation = location;
							nextLocation.y = location.y - 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_DOWN)&&location.x+1<=mapInfo.size&&maze[location.x+1][location.y]!=-2) {
							Location nextLocation = location;
							nextLocation.x = location.x + 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_RIGHT)&&location.y+1<=mapInfo.size&&maze[location.x][location.y+1]!=-2) {
							Location nextLocation = location;
							nextLocation.y = location.y + 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_TOP)&&local.x-1>=0&&maze[location.x-1][location.y]!=-2) {
							Location nextLocation = location;
							nextLocation.x = location.x - 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
								return actionEntity;
							}
						}
						if(a1<=a2) {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
							return actionEntity;
						}else {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
							return actionEntity;
						}
					}
					if(b1+a3<a2&&b2+a3<a1) {
//						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_LEFT)&&location.y-1>=0&&maze[location.x][location.y-1]!=-2) {
//							Location nextLocation = location;
//							nextLocation.y = location.y - 1;
//							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
//								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
//								return actionEntity;
//							}
//						}
//						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_DOWN)&&location.x+1<=mapInfo.size&&maze[location.x+1][location.y]!=-2) {
//							Location nextLocation = location;
//							nextLocation.x = location.x + 1;
//							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
//								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
//								return actionEntity;
//							}
//						}
//						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_RIGHT)&&location.y+1<=mapInfo.size&&maze[location.x][location.y+1]!=-2) {
//							Location nextLocation = location;
//							nextLocation.y = location.y + 1;
//							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
//								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
//								return actionEntity;
//							}
//						}
//						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_TOP)&&local.x-1>=0&&maze[location.x-1][location.y]!=-2) {
//							Location nextLocation = location;
//							nextLocation.x = location.x - 1;
//							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
//								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
//								return actionEntity;
//							}
//						}
						if(baseInfo.bloodNum>robot.bloodNum+2) {
							MoveToChase(robot);
							return actionEntity;
						}else {
							MoveToSafePlace();
							return actionEntity;
						}
					}			
				}
			}						
			if(b1==-1&&b2!=-1) {
				if(a2<b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
					return actionEntity;
				}
				if(a2==b2&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum))) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
					return actionEntity;
				}
			}
			
			if(b2==-1&&b1!=-1) {
				if(a1<b1) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
					return actionEntity;
				}
				if(a1==b1&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum))) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
					return actionEntity;
				}
			}
			
			if(b2==-1&&b1==-1) {
				if(a1<=a2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
					return actionEntity;
				}
			}
			
			arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
			return actionEntity;
			
		}else {
			if(a1<b1||(a1==b1&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
				return actionEntity;
			}
			if(a2<b2||(a2==b2&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, 0);
				return actionEntity;
			}
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
	}
	
	/**
	 * 一个血包时的策略
	 * @param bloodBags
	 * @param location
	 * @param landmines
	 * @param robot
	 * @return
	 */
	public CommonMoveAction getNextActionByOneBloodBagManyRobots(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot) {
		int a = arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false);
		int b = arrivalSafePlace(robot.currentLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false);
		if(a==-1) {			
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
		if(a!=-1&&b!=-1) {
			if(a<b||(a==b&&(baseInfo.bloodNum>=robot.bloodNum+1||(baseInfo.bloodNum%2==0&&baseInfo.bloodNum>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		else {
			arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, 0);
			return actionEntity;
		}
	}	
	
	
	/**
	 * 当地图存在两个血包时的策略
	 * @param bloodBags
	 * @param location
	 * @param landmines
	 * @param robot
	 * @return
	 */
	public CommonMoveAction getNextActionByTwoBloodBags(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot) {
		int a1 = arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false);
		int a2 = arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false);
		int a3 = arrivalSafePlace(bloodBags.get(0).location, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false);
		int d1 = 0;
		int d2 = 0;
		if(a3==-1) {
			a3= arrivalByOneLandmine(bloodBags.get(0).location, bloodBags.get(1).location, landmines);
		}
		if(a1==-1) {
			a1 = arrivalByOneLandmine(location, bloodBags.get(0).location, landmines);
			d1 = 1;
		}
		if(a2==-1) {
			a2 = arrivalByOneLandmine(location, bloodBags.get(1).location, landmines);
			d2 = 1;
		}
		int b1 = arrivalSafePlace(robot.currentLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false);
		int b2 = arrivalSafePlace(robot.currentLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false);
		if(a1==-1&&a2!=-1) {
			if(a2<b2||(a2==b2&&(baseInfo.bloodNum>=robot.bloodNum+1+d2||(baseInfo.bloodNum%2==d2&&baseInfo.bloodNum>=robot.bloodNum+d2)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		if(a1!=-1&&a2==-1) {
			if(a1<b1||(a1==b1&&(baseInfo.bloodNum>=robot.bloodNum+1+d1||(baseInfo.bloodNum%2==d1&&baseInfo.bloodNum>=robot.bloodNum+d1)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		if(a1!=-1&&a2!=-1) {
			return getNextActionByTwoBloodBags(bloodBags,location,landmines,robot,a1,a2,a3,b1,b2,d1,d2);
		}else {
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
	}
	
	public void arrivalBySafeOrOneLandmine(Location location,Location bloodBagLocation,List<Landmine> landmines,int d) {
		if(d==0) {
			arrivalSafePlace(location, bloodBagLocation, forbidDirection,true);
		}else {
			arrivalByOneLandmine(location, bloodBagLocation, landmines);
		}
	}
	
	public CommonMoveAction getNextActionByTwoBloodBags(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot,int a1,int a2,int a3,int b1,int b2,int d1,int d2) {
		if(a3!=-1) {
			if(b1!=-1&&b2!=-1) {
				if(a1<=b1&&a2<=b2) {
					if(a1+a3<b2||(a1+a3==b2&&(baseInfo.bloodNum>=robot.bloodNum+d1+d2||((baseInfo.bloodNum+2-d1-d2)%2==1&&baseInfo.bloodNum>=robot.bloodNum+d1+d2-1)))) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
						return actionEntity;
					}
					if(a2+a3<b1||(a2+a3==b2&&(baseInfo.bloodNum>=robot.bloodNum+d1+d2||((baseInfo.bloodNum+2-d1-d2)%2==1&&baseInfo.bloodNum>=robot.bloodNum+d1+d2-1)))) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
						return actionEntity;
					}else {
						if(b1<=b2) {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
							return actionEntity;
						}else {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
							return actionEntity;
						}
					}
				}
				
				if(a1<=b1&&a2>=b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
					return actionEntity;
				}
				
				if(a1>=b1&&a2<=b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
					return actionEntity;
				}
				
				if(a1>b1&&a2>b2) {
					if(b1+a3<=a2&&b2+a3>=a1) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
						return actionEntity;
					}
					if(b2+a3<=a1&&b1+a3>=a2) {
						arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
						return actionEntity;
					}
					if(b1+a3>a2&&b2+a3>a1) {
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_LEFT)&&location.y-1>=0&&maze[location.x][location.y-1]!=-2) {
							Location nextLocation = location;
							nextLocation.y = location.y - 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_DOWN)&&location.x+1<=mapInfo.size&&maze[location.x+1][location.y]!=-2) {
							Location nextLocation = location;
							nextLocation.x = location.x + 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_RIGHT)&&location.y+1<=mapInfo.size&&maze[location.x][location.y+1]!=-2) {
							Location nextLocation = location;
							nextLocation.y = location.y + 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
								return actionEntity;
							}
						}
						if(!forbidDirection.contains(MoveActionCommandEnum.MOVE_TOP)&&local.x-1>=0&&maze[location.x-1][location.y]!=-2) {
							Location nextLocation = location;
							nextLocation.x = location.x - 1;
							if(arrivalSafePlace(nextLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false)&&arrivalSafePlace(nextLocation, bloodBags.get(1).location, new ArrayList<MoveActionCommandEnum>(),false)<arrivalSafePlace(location, bloodBags.get(1).location, forbidDirection,false)) {
								actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
								return actionEntity;
							}
						}
						if(a1<=a2) {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
							return actionEntity;
						}else {
							arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
							return actionEntity;
						}
					}
					if(b1+a3<a2&&b2+a3<a1) {
						if(d1==1&&d2==1) {
							int c1 = arrivalByOneLandmine(location, bloodBags.get(0).location, landmines);
							int c2 = arrivalByOneLandmine(location, bloodBags.get(1).location, landmines);
							if(c1!=-1&&c1<b1) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
								return actionEntity;
							}
							if(c2!=-1&&c2<b2) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
								return actionEntity;
							}
							if(c1!=-1&&c1==b1&&(baseInfo.bloodNum>=robot.bloodNum+2||((baseInfo.bloodNum+1)%2==1&&baseInfo.bloodNum>=robot.bloodNum+1))) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
								return actionEntity;
							}
							if(c2!=-1&&c2==b2&&(baseInfo.bloodNum>=robot.bloodNum+2||((baseInfo.bloodNum+1)%2==1&&baseInfo.bloodNum>=robot.bloodNum+1))) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
								return actionEntity;
							}
							if(baseInfo.bloodNum>robot.bloodNum+2) {
								return MoveToChase(robot);
							}else {
								return MoveToSafePlace();
							}
						}
						if(d1==2&&d2==2) {
							if(baseInfo.bloodNum>robot.bloodNum+2) {
								return MoveToChase(robot);
							}else {
								return MoveToSafePlace();
							}
						}
						if(d1==1&&d2==2) {
							int c1 = arrivalByOneLandmine(location, bloodBags.get(0).location, landmines);
							if(c1!=-1&&c1<b1) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
								return actionEntity;
							}
							if(c1!=-1&&c1==b1&&(baseInfo.bloodNum>=robot.bloodNum+2||((baseInfo.bloodNum+1)%2==1&&baseInfo.bloodNum>=robot.bloodNum+1))) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
								return actionEntity;
							}
							if(baseInfo.bloodNum>robot.bloodNum+2) {
								return MoveToChase(robot);
							}else {
								return MoveToSafePlace();
							}
						}
						if(d2==1&&d1==2) {
							int c2 = arrivalByOneLandmine(location, bloodBags.get(1).location, landmines);
							if(c2!=-1&&c2<b2) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
								return actionEntity;
							}
							if(c2!=-1&&c2==b2&&(baseInfo.bloodNum>=robot.bloodNum+2||((baseInfo.bloodNum+1)%2==1&&baseInfo.bloodNum>=robot.bloodNum+1))) {
								arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
								return actionEntity;
							}
							if(baseInfo.bloodNum>robot.bloodNum+2) {
								return MoveToChase(robot);
							}else {
								return MoveToSafePlace();
							}
						}
					}			
				}
			}
						
			if(b1==-1&&b2!=-1) {
				if(a2<b2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
					return actionEntity;
				}
				if(a2==b2&&(baseInfo.bloodNum-d2>=robot.bloodNum+1||((baseInfo.bloodNum+1-d2)%2==1&&baseInfo.bloodNum-d2>=robot.bloodNum))) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
					return actionEntity;
				}
			}
			
			if(b2==-1&&b1!=-1) {
				if(a1<b1) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
					return actionEntity;
				}
				if(a1==b1&&(baseInfo.bloodNum-d2>=robot.bloodNum+1||((baseInfo.bloodNum+1-d2)%2==1&&baseInfo.bloodNum-d2>=robot.bloodNum))) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
					return actionEntity;
				}
			}
			
			if(b2==-1&&b1==-1) {
				if(a1<=a2) {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
					return actionEntity;
				}
				else {
					arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
					return actionEntity;
				}
			}
			
			arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
			return actionEntity;
			
		}else {
			if(a1<b1||(a1==b1&&(baseInfo.bloodNum-d1>=robot.bloodNum+1||((baseInfo.bloodNum+1-d1)%2==1&&baseInfo.bloodNum-d1>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d1);
				return actionEntity;
			}
			if(a2<b2||(a2==b2&&(baseInfo.bloodNum-d2>=robot.bloodNum+1||((baseInfo.bloodNum+1-d2)%2==1&&baseInfo.bloodNum-d2>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(1).location, landmines, d2);
				return actionEntity;
			}
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
	}
	
	/**
	 * 将血包按距离排序
	 * @param list
	 * @param location
	 * @param left
	 * @param right
	 */
	public void sortBloodBags(List<BloodBag> list, Location location, List<Landmine> landmines, int left, int right){
		if(left>=right) {
			return;
		}
		int index = partition(list,location,landmines,left,right);
		sortBloodBags(list,location,landmines,left,index-1);
		sortBloodBags(list,location,landmines,index+1,right);
	}
	
	public int partition(List<BloodBag> list, Location location, List<Landmine> landmines, int a, int b){
		int pivot = getDistance(list.get(a),location,landmines);
		BloodBag c = list.get(a);
		while(a<b) {
			while(getDistance(list.get(b),location,landmines)>=pivot&&b>a) {
				b--;
			}
			list.set(a, list.get(b));
			while(getDistance(list.get(a),location,landmines)<=pivot&&b>a) {
				a++;
			}
			list.set(b, list.get(a));
		}
		list.set(b, c);
		return b;
	}
	
	public int getDistance(BloodBag bloodBag,Location location, List<Landmine> landmines){
		int pivot = arrivalSafePlace(location, bloodBag.location, forbidDirection,false);
		if(pivot==-1){
			if(arrivalByOneLandmine(location, bloodBag.location, landmines)==-1){
				pivot = 2*mapInfo.size*mapInfo.size;
			}else{
				pivot = mapInfo.size*mapInfo.size+arrivalByOneLandmine(location, bloodBag.location, landmines);
			}			
		}
		return pivot;
	}
	
	/**
	 * 一个血包时的策略
	 * @param bloodBags
	 * @param location
	 * @param landmines
	 * @param robot
	 * @return
	 */
	public CommonMoveAction getNextActionByOneBloodBag(List<BloodBag> bloodBags,Location location,List<Landmine> landmines,FightRobotBaseInfo robot) {
		int a = arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,false);
		int b = arrivalSafePlace(robot.currentLocation, bloodBags.get(0).location, new ArrayList<MoveActionCommandEnum>(),false);
		int d = 0;
		if(a==-1) {
			a = arrivalByOneLandmine(location, bloodBags.get(0).location, landmines);
			d = 1;
		}
		if(a==-1) {			
			if(baseInfo.bloodNum>robot.bloodNum+2) {
				return MoveToChase(robot);
			}else {
				return MoveToSafePlace();
			}
		}
		if(a!=-1&&b!=-1) {
			if(a<b||(a==b&&(baseInfo.bloodNum-d>=robot.bloodNum+1||((baseInfo.bloodNum+1-d)%2==1&&baseInfo.bloodNum-d>=robot.bloodNum)))) {
				arrivalBySafeOrOneLandmine(location, bloodBags.get(0).location, landmines, d);
				return actionEntity;
			}else {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}
		}
		else {
			b = arrivalByOneLandmine(robot.currentLocation, bloodBags.get(0).location, landmines);
			d = d-1;
			if(b==-1) {
				if(baseInfo.bloodNum>robot.bloodNum+2) {
					return MoveToChase(robot);
				}else {
					return MoveToSafePlace();
				}
			}else {
				if(a<b||(a==b&&(baseInfo.bloodNum-d>=robot.bloodNum+1||((baseInfo.bloodNum+1-d)%2==1&&baseInfo.bloodNum-d>=robot.bloodNum)))) {
					if(d==-1) {
						arrivalSafePlace(location, bloodBags.get(0).location, forbidDirection,true);
					}else {
						arrivalByOneLandmine(location, bloodBags.get(0).location, landmines);
					}
					return actionEntity;
				}else {
					if(baseInfo.bloodNum>robot.bloodNum+1) {
						return MoveToChase(robot);
					}else {
						return MoveToSafePlace();
					}
				}
			}
		}
	}
		
	/**
	 * 追击的方法
	 * @param robot
	 * @return
	 */
	public CommonMoveAction MoveToChase(FightRobotBaseInfo robot) {
		if(arrivalSafePlace(local, robot.currentLocation, forbidDirection,true)!=-1) {
			return actionEntity;
		}else {
			List<MoveActionCommandEnum> sortDirection = sortMoveActionCommandEnum(robot.currentLocation,local);
			getNextActionByNoLandmine(local, sortDirection);
			if(actionEntity!=null)
				return actionEntity;
//			getNextActionByOneLandmine(local, robot.currentLocation, sortDirection);
//			if(actionEntity!=null){
//				return actionEntity;
//			}
			getNextActionByLessLandmine(sortDirection, local);
			return actionEntity;
		}
	}

	/**
	 * 向安全区移动方法
	 * @return
	 */
	public CommonMoveAction MoveToSafePlace(){
		List<Location> safePlaces = findSafePlaces();
		//寻找能安全到达的安全区
		for(int i=0;i<safePlaces.size();i++){
            arrivalSafePlace(local, safePlaces.get(i), forbidDirection,true);
			if(actionEntity!=null){
				return actionEntity;
			}
		}
		//将安全区按距离排序
		sortSafePlacesByDistance(safePlaces, local, 0, safePlaces.size()-1);
		//取最近的安全区并对四个方向排序
		List<MoveActionCommandEnum> sortDirection = sortMoveActionCommandEnum(safePlaces.get(0),local);
		//若不存在直接可到达的安全区，则寻找四个方向中下一步为安全的方向
		getNextActionByNoLandmine(local, sortDirection);
		if(actionEntity!=null)
			return actionEntity;
//		//寻找下一步踩中一雷后可以安全到达安全区的方向
//		for(int i=0;i<safePlaces.size();i++){
//			List<MoveActionCommandEnum> list = sortMoveActionCommandEnum(safePlaces.get(i),local);
//			getNextActionByOneLandmine(local, safePlaces.get(i), list);
//			if(actionEntity!=null){
//				return actionEntity;
//			}
//		}
		getNextActionByLessLandmine(sortDirection, local);
		return actionEntity;
	}
	
	/**
	 * 寻找安全区方法，返回按优先级从高到低的排序后的位置链表
	 * @return
	 */
	public List<Location> findSafePlaces(){
		List<Location> list = new ArrayList<>();
		Location first;
		for(int i=0;i<3;i++){
			first = findSafePlace();
			list.add(first);
			maze[first.x][first.y] = -2;
		}
		for(int i=0;i<3;i++) {
			maze[list.get(i).x][list.get(i).y] = 0;
		}
		return list;
	}
	
	public Location findSafePlace(){
		int i,j,la,ra;
		Location location = new Location(local.x,local.y);
		int ans=0;
		int[] c = new int[mapInfo.size];
		int[] r = new int[mapInfo.size];
		int[] l = new int[mapInfo.size];
		int[] h = new int[mapInfo.size]; 
	    for (i=0;i<mapInfo.size;i++) {
	    	h[i]=0;
	    	l[i]=0;
	    	r[i]=mapInfo.size-1;
	    }  
	    for (i=0;i<mapInfo.size;i++) {  
	        la=-1; ra=mapInfo.size;//la表示左边第一个障碍点的位置，ra表示右边第一个障碍点的位置  
	        for (j=0;j<mapInfo.size;j++) {
	        	if (maze[i][j]==-2||maze[i][j]==-3){//如果当前点为1，那么他就是一条悬线的起点  
	        	  c[j]=i;
	  	          h[j]=0;
	  	          la=j;
	  	          l[j]=1; 
	        	}else{
	        		h[j]++;
	        		if(l[j]>=la+1){
	        			l[j]=l[j];
	        		}else{
	        			l[j]=la+1;
	        		}
	        	}
	        }	         
	        for (j=mapInfo.size-1;j>=0;j--) { 
	        	if(maze[i][j]==-2||maze[i][j]==-3){
	        		r[j]=mapInfo.size;
	        		ra=j;
	        	}
	        	else{
	        		if(r[j]<=ra-1){
	        			r[j]=r[j];
	        		}else{
	        			r[j]=ra-1;
	        		}
	        		if((r[j]-l[j]+1)*h[j]>ans){
	        			ans=(r[j]-l[j]+1)*h[j];
	        			location.y=(r[j]+l[j])/2;
	        			location.x=c[j]+h[j]/2;
	        		}
	        	}
	        }  
	     }  		
		return location;
	}
	
	/**
	 * 四个方向移动后位置
	 * @param location
	 * @return
	 */
	public List<Q> nextActionLocation(Location location){
		List<Q> nextLocationList = new ArrayList<Q>();
		if(location.y-1>=0){
			Location nextLocation = new Location(location.x,location.y-1);
			nextLocationList.add(new Q(nextLocation,MoveActionCommandEnum.MOVE_LEFT));
		}
		if(location.x+1<mapInfo.size){
			Location nextLocation = new Location(location.x+1,location.y);
			nextLocationList.add(new Q(nextLocation,MoveActionCommandEnum.MOVE_DOWN));
		}

		if(location.y+1<mapInfo.size){
			Location nextLocation = new Location(location.x,location.y+1);
			nextLocationList.add(new Q(nextLocation,MoveActionCommandEnum.MOVE_RIGHT));
		}
		if(location.x-1>=0){
			Location nextLocation = new Location(location.x-1,location.y);
			nextLocationList.add(new Q(nextLocation,MoveActionCommandEnum.MOVE_TOP));
		}
		return nextLocationList;
	}

	/**
	 * 返回当前位置到目标点的最短距离，若不能到达，返回-1
	 * @param location
	 * @param safePlace
	 * @return
	 */
	public int arrivalSafePlace(Location location, Location safePlace, List<MoveActionCommandEnum> list, boolean need) {
		if(maze[safePlace.x][safePlace.y]==-2) {
			maze[safePlace.x][safePlace.y]=-1;
		}
		Queue<P> que = new LinkedList<P>();
		int[][] d = new int[mapInfo.size][mapInfo.size];
		for(int i=0;i<mapInfo.size;i++) {
			for(int j=0;j<mapInfo.size;j++) {
				d[i][j] = -1;
			}
		}
		que.offer(new P(location.x,location.y));
		d[location.x][location.y]=0;
		boolean isArrival = false;
		while(que.size()>0) {
			P p = que.poll();
			int i;
			for(i=0;i<4;i++) {
				if(p.a!=location.x||p.b!=location.y||list.isEmpty()||(i==0&&!list.contains(MoveActionCommandEnum.MOVE_TOP))||(i==1&&!list.contains(MoveActionCommandEnum.MOVE_RIGHT))||(i==2&&!list.contains(MoveActionCommandEnum.MOVE_DOWN))||(i==3&&!list.contains(MoveActionCommandEnum.MOVE_LEFT))) {
					int nx = p.a + dx[i];
					int ny = p.b + dy[i];
					if(0<=nx&&nx<mapInfo.size&&0<=ny&&ny<mapInfo.size&&maze[nx][ny]!=-2&&d[nx][ny]==-1) {
						que.offer(new P(nx,ny));
						d[nx][ny]=d[p.a][p.b]+1;
						if(nx==safePlace.x&&ny==safePlace.y) {
							isArrival=true;
							break;
						}
					}
				}
			}
			if(i!=4)
				break;
		}
		if(isArrival==false){
			actionEntity = null;
			return -1;
		}
		if(need==true){
			P nextp = null;
			if(d[safePlace.x][safePlace.y]==1) {
				nextp = new P(safePlace.x,safePlace.y);
			}else {
				Queue<P> fque = new LinkedList<P>();
				fque.offer(new P(safePlace.x,safePlace.y));
				while(fque.size()>0) {
					P p = fque.poll();
					int i;
					for(i=0;i<4;i++) {
						int nx = p.a + dx[i];
						int ny = p.b + dy[i];
						if(0<=nx&&nx<mapInfo.size&&0<=ny&&ny<mapInfo.size&&d[nx][ny]==d[p.a][p.b]-1&&d[nx][ny]>=1) {
							fque.offer(new P(nx,ny));
							if(d[nx][ny]==1) {
								nextp=new P(nx,ny);
								break;
							}
						}
					}
					if(i!=4)
						break;
				}
			}
			if(nextp.a>location.x) {
				actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
			}
			if(nextp.a<location.x) {
				actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
			}
			if(nextp.b>location.y) {
				actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
			}
			if(nextp.b<location.y) {
				actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
			}
			if(maze[safePlace.x][safePlace.y]==-1) {
				maze[safePlace.x][safePlace.y]=-2;
			}
		}
		return d[safePlace.x][safePlace.y];
	}
	
	/**
	 * 两点之间踩中一雷的最短距离，不能到达返回-1
	 * @param location
	 * @param safePlace
	 * @param landmines
	 * @return
	 */
	public int arrivalByOneLandmine(Location location, Location safePlace, List<Landmine> landmines) {
		if(landmines.isEmpty()||landmines.size()>2*mapInfo.size) {
			return -1;
		}
		int distance = 100;
		Location theLandmine = null;
		for(int i=0;i<landmines.size();i++) {
			if(arrivalSafePlace(location, landmines.get(i).location, forbidDirection,false)>0&&arrivalSafePlace(safePlace, landmines.get(i).location, new ArrayList<MoveActionCommandEnum>(),false)>0&&arrivalSafePlace(location, landmines.get(i).location, forbidDirection,false)+arrivalSafePlace(safePlace, landmines.get(i).location, new ArrayList<MoveActionCommandEnum>(),false)<distance) {				
				distance = arrivalSafePlace(location, landmines.get(i).location, forbidDirection,false)+arrivalSafePlace(safePlace, landmines.get(i).location, new ArrayList<MoveActionCommandEnum>(),false);
				theLandmine = landmines.get(i).location;
			}
		}
		if(distance!=100) {
			arrivalSafePlace(location, theLandmine, forbidDirection,true);
			return distance;
		}else {
			return -1;
		}
	}
	
	/**
	 * 将安全区序列按距离排序
	 * @param list
	 * @param location
	 * @param left
	 * @param right
	 */
	public void sortSafePlacesByDistance(List<Location> list, Location location, int left, int right){
		if(left>=right) {
			return;
		}
		int index = partition(list,location,left,right);
		sortSafePlacesByDistance(list,location,left,index-1);
		sortSafePlacesByDistance(list,location,index+1,right);
	}
	
	public int partition(List<Location> list, Location location, int a, int b){
		int pivot = Math.abs(list.get(a).x-location.x)+Math.abs(list.get(a).y-location.y);
		Location c = list.get(a);
		while(a<b) {
			while(Math.abs(list.get(b).x-location.x)+Math.abs(list.get(b).y-location.y)>=pivot&&b>a) {
				b--;
			}
			list.set(a, list.get(b));
			while(Math.abs(list.get(a).x-location.x)+Math.abs(list.get(a).y-location.y)<=pivot&&b>a) {
				a++;
			}
			list.set(b, list.get(a));
		}
		list.set(b, c);
		return b;
	}
	
	/**
	 * 获取已排序的方向链表
	 * @param safePlace
	 * @param location
	 * @return
	 */
	public List<MoveActionCommandEnum> sortMoveActionCommandEnum(Location safePlace, Location location){
		List<MoveActionCommandEnum> list = new ArrayList<>();
		if(safePlace.y==location.y&&safePlace.x==location.x) {
			list.add(MoveActionCommandEnum.MOVE_LEFT);
			list.add(MoveActionCommandEnum.MOVE_DOWN);
			list.add(MoveActionCommandEnum.MOVE_RIGHT);
			list.add(MoveActionCommandEnum.MOVE_TOP);
		}
		if(safePlace.y<location.y&&safePlace.x>=location.x){
			list.add(MoveActionCommandEnum.MOVE_TOP);
			list.add(MoveActionCommandEnum.MOVE_RIGHT);
			list.add(MoveActionCommandEnum.MOVE_DOWN);
			list.add(MoveActionCommandEnum.MOVE_LEFT);
		}
		if(safePlace.y<=location.y&&safePlace.x<location.x){
			list.add(MoveActionCommandEnum.MOVE_RIGHT);
			list.add(MoveActionCommandEnum.MOVE_DOWN);
			list.add(MoveActionCommandEnum.MOVE_LEFT);
			list.add(MoveActionCommandEnum.MOVE_TOP);
		}
		if(safePlace.y>location.y&&safePlace.x<=location.x){
			list.add(MoveActionCommandEnum.MOVE_DOWN);
			list.add(MoveActionCommandEnum.MOVE_LEFT);
			list.add(MoveActionCommandEnum.MOVE_TOP);
			list.add(MoveActionCommandEnum.MOVE_RIGHT);
		}
		if(safePlace.y>=location.y&&safePlace.x>location.x){
			list.add(MoveActionCommandEnum.MOVE_LEFT);
			list.add(MoveActionCommandEnum.MOVE_TOP);
			list.add(MoveActionCommandEnum.MOVE_RIGHT);
			list.add(MoveActionCommandEnum.MOVE_DOWN);
		}
		return list;
	}

	/**
	 * 寻找四个方向中可行的不踩雷的方向
	 * @param location
	 */
	public void getNextActionByNoLandmine(Location location, List<MoveActionCommandEnum> list){
		for(int i=0;i<list.size();i++){
			isSafeByNoLandmine(list.get(i), location);
		}
	}
	
	/**
	 * 判断某个方向的下一步是否不踩雷
	 * @param moveActionCommandEnum
	 */
	public boolean isSafeByNoLandmine(MoveActionCommandEnum moveActionCommandEnum, Location location){
		if(forbidDirection.contains(moveActionCommandEnum)){
			return false;
		}
		boolean safe = true;
		Location nextLocation = location;
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_LEFT){
			if(location.y-1<0){
				return false;
			}
			nextLocation.y = location.y-1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_DOWN){
			if(location.x+1>=mapInfo.size){
				return false;
			}
			nextLocation.x = location.x+1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_RIGHT){
			if(location.y+1>=mapInfo.size){
				return false;
			}
			nextLocation.y = location.y+1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_TOP){
			if(location.x-1<0){
				return false;
			}
			nextLocation.x = location.x-1;
		}		
		if(maze[nextLocation.x][nextLocation.y]==-2){
			safe = false;
		}
		if(safe==true){
			actionEntity = new CommonMoveAction(moveActionCommandEnum);
			return true;
		}else{
			return false;
		}
	}
	
//	/**
//	 * 寻找四个方向中踩中一雷后可以到达安全区的方向
//	 * @param location
//	 */
//	public void getNextActionByOneLandmine(Location location, Location safePlace, List<MoveActionCommandEnum> list){
//		for(int i=0;i<list.size();i++){
//			isSafeByOneLandmine(location, safePlace, list.get(i));
//		}
//	}
//	
//	/**
//	 * 判断某一方向在踩中一雷后是否可以到达安全区
//	 * @param safePlace
//	 * @param moveActionCommandEnum
//	 * @return
//	 */
//	public boolean isSafeByOneLandmine(Location location, Location safePlace, MoveActionCommandEnum moveActionCommandEnum){
//		if(forbidDirection.contains(moveActionCommandEnum)){
//			return false;
//		}
//		Location nextLocation = new Location(location.x,location.y);
//		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_LEFT){
//			if(location.y-1<0){
//				return false;
//			}
//			nextLocation.y = location.y-1;
//		}
//		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_DOWN){
//			if(location.x+1>=mapInfo.size){
//				return false;
//			}
//			nextLocation.x = location.x+1;
//		}
//		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_RIGHT){
//			if(location.y+1>=mapInfo.size){
//				return false;
//			}
//			nextLocation.y = location.y+1;
//		}
//		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_TOP){
//			if(location.x-1<0){
//				return false;
//			}
//			nextLocation.x = location.x-1;
//		}
//		if(arrivalSafePlace(nextLocation,safePlace,new ArrayList<MoveActionCommandEnum>(),false)>0) {
//			actionEntity = new CommonMoveAction(moveActionCommandEnum);
//			return true;
//		}else {
//			return false;
//		}
//	}
	
	/**
	 * 获取四个方向中踩中一雷后下一步安全值最大的方向
	 * @param safePlace
	 * @param location
	 */
	public void getNextActionByLessLandmine(List<MoveActionCommandEnum> list, Location location){
		int maxSafe=getSafeByLessLandmine(location, list.get(0));
		actionEntity = new CommonMoveAction(list.get(0));
		for(int i=1;i<list.size();i++){
			if(maxSafe<=getSafeByLessLandmine(location, list.get(i))){
				maxSafe = getSafeByLessLandmine(location, list.get(i));
				actionEntity = new CommonMoveAction(list.get(i));
			}
		}
	}
	
	/**
	 * 获取某一方向踩中一雷后下一步安全值
	 * @param safePlace
	 * @param location
	 */
	public int getSafeByLessLandmine(Location location, MoveActionCommandEnum moveActionCommandEnum){
		if(forbidDirection.contains(moveActionCommandEnum)){
			return -1;
		}
		Location nextLocation = new Location(location.x,location.y);
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_LEFT&&location.y-1>=0){
			nextLocation.y = location.y-1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_DOWN&&location.x+1<mapInfo.size){
			nextLocation.x = location.x+1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_RIGHT&&location.y+1<mapInfo.size){
			nextLocation.y = location.y+1;
		}
		if(moveActionCommandEnum == MoveActionCommandEnum.MOVE_TOP&&location.x-1>=0){
			nextLocation.x = location.x-1;
		}
		if(nextLocation.equals(location)){
			return -2;
		}
		int totalSafe = 0;
		if(nextLocation.y-1>=0){
			ElementList elementList = elementLists[nextLocation.x][nextLocation.y-1];
			boolean isLandmine = false;
    		for (AbstractElement element : elementList.elements) {
                if (ElementTypeEnum.LANDMINE == element.elementType) {
                	isLandmine = true;
                }
            }
    		if(isLandmine==false){
    			totalSafe++;
    		}
		}
		if(nextLocation.x+1<mapInfo.size){
			ElementList elementList = elementLists[nextLocation.x+1][nextLocation.y];
			boolean isLandmine = false;
    		for (AbstractElement element : elementList.elements) {
                if (ElementTypeEnum.LANDMINE == element.elementType) {
                	isLandmine = true;
                }
            }
    		if(isLandmine==false){
    			totalSafe++;
    		}
		}
		if(nextLocation.y+1<mapInfo.size){
			ElementList elementList = elementLists[nextLocation.x][nextLocation.y+1];
			boolean isLandmine = false;
    		for (AbstractElement element : elementList.elements) {
                if (ElementTypeEnum.LANDMINE == element.elementType) {
                	isLandmine = true;
                }
            }
    		if(isLandmine==false){
    			totalSafe++;
    		}
		}
		if(nextLocation.x-1>=0){
			ElementList elementList = elementLists[nextLocation.x-1][nextLocation.y];
			boolean isLandmine = false;
    		for (AbstractElement element : elementList.elements) {
                if (ElementTypeEnum.LANDMINE == element.elementType) {
                	isLandmine = true;
                }
            }
    		if(isLandmine==false){
    			totalSafe++;
    		}
		}
		return totalSafe;
	}

}
