package com.kxd.code.competition.robot.fight.fuck;

import java.util.ArrayList;
import java.util.List;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;
/**
 * 地图的所有信息
 * @author 缪庆泉
 *
 */
public class MapContext {
	//自己的位置
	private boolean attack = false;
	//自己的位置
	private SaienLoction onselfLoction;
	
	private FightRobotBaseInfo onselfRobot;
	
	private ElementList[][] elementLists;
	
	private int size;
	
	//当前的空白点
    private List<Location> emptyLoctions = new ArrayList<>();
	// 当前所见的血包存到list
    private List<BloodBag> bloodBags = new ArrayList<>();
    // 当前所有的机器人List
    private List<FightRobotBaseInfo> robots = new ArrayList<>();
    // 当前所有的炸弹List
    private List<Landmine> lands = new ArrayList<>();
    

    public MapContext(FightRobotSeeEntity fightRobotSeeEntity) {
    	onselfRobot = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        onselfLoction = new SaienLoction( onselfRobot.currentLocation.x,onselfRobot.currentLocation.y);
        elementLists = fightRobotSeeEntity.robotGetElementList;	
        size = elementLists.length;
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                if(elementList.elements.size() == 0){
                	emptyLoctions.add(new Location(i, j));
                }else{
                	for (AbstractElement element : elementList.elements) {
                        if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        	bloodBags.add((BloodBag) element);
                        }else if(ElementTypeEnum.ROBOT_INFO == element.elementType){
                        	FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;//排除自己
                        	if(!onselfRobot.name.equals(fightRobotBaseInfo.name)){
                        		robots.add((FightRobotBaseInfo) element);
                        	}
                        	
                        }else if(ElementTypeEnum.LANDMINE == element.elementType){
                        	lands.add((Landmine) element);
                        }
                    }
                }
            }
        }
        int bloodNum = onselfRobot.bloodNum;//自己的血量
        int totalNum = 0, total = 0;
        for (FightRobotBaseInfo robot : robots) {
        	totalNum +=robot.bloodNum;
        	if(robot.bloodNum < bloodNum){
        		total ++;
        	}
		}
        int size = robots.size();
        if((bloodNum == 10 && totalNum/size == 10) || total >= (2*size)/3){
        	attack = true;
        }else{
        	attack = false;
        }
    }



	public SaienLoction getOnselfLoction() {
		return onselfLoction;
	}



	public void setOnselfLoction(SaienLoction onselfLoction) {
		this.onselfLoction = onselfLoction;
	}



	public List<BloodBag> getBloodBags() {
		return bloodBags;
	}


	public void setBloodBags(List<BloodBag> bloodBags) {
		this.bloodBags = bloodBags;
	}


	public List<FightRobotBaseInfo> getRobots() {
		return robots;
	}


	public void setRobots(List<FightRobotBaseInfo> robots) {
		this.robots = robots;
	}


	public List<Landmine> getLands() {
		return lands;
	}


	public void setLands(List<Landmine> lands) {
		this.lands = lands;
	}

	public ElementList[][] getElementLists() {
		return elementLists;
	}


	public void setElementLists(ElementList[][] elementLists) {
		this.elementLists = elementLists;
	}

	public FightRobotBaseInfo getOnselfRobot() {
		return onselfRobot;
	}


	public void setOnselfRobot(FightRobotBaseInfo onselfRobot) {
		this.onselfRobot = onselfRobot;
	}


	public List<Location> getEmptyLoctions() {
		return emptyLoctions;
	}


	public void setEmptyLoctions(List<Location> emptyLoctions) {
		this.emptyLoctions = emptyLoctions;
	}


	public boolean isAttack() {
		return attack;
	}


	public void setAttack(boolean attack) {
		this.attack = attack;
	}



	public int getSize() {
		return size;
	}



	public void setSize(int size) {
		this.size = size;
	}
	
}
