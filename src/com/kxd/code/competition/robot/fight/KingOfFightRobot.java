package com.kxd.code.competition.robot.fight;

import java.util.ArrayList;
import java.util.List;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;
import com.kxd.code.competition.robot.fight.av.AVLocation;

/**
 * Created by qiaojs on 2017/12/21.
 */
public class KingOfFightRobot extends AbstractFightRobot {
    
    private static Integer  MAP_SIZE = 0;
    private static List<Location> BLOOD_BAG_LOCATIONS = null;
    private static List<Location> LANDMINE_LOCATIONS = null;
    private static List<Location> ENEMY_ROBOTS_LOCATIONS = null;
    private static List<FightRobotBaseInfo> ENEMY_ROBOTS = null;
    private static Integer SELF_BLOOD_NUM = 0;
//    private static FightRobotBaseInfo KING_OF_FIGHT_ROBOT = null;
    
    public KingOfFightRobot() {
        super("王的男人");
    }

    @Override
    public CommonMoveAction getNextAction() {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        initVariableParameter(fightRobotSeeEntity);
        resetMapInfo(fightRobotSeeEntity);
        CommonMoveAction actionEntity = calculateMoveAction(fightRobotSeeEntity);
        return actionEntity;
    }
    
    private CommonMoveAction calculateMoveAction(FightRobotSeeEntity fightRobotSeeEntity) {
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;
        Location nextBestLocation = getNextBestLocation(fightRobotSeeEntity);
        MoveActionCommandEnum moveActionCommand = moveActionCommandEnum(currentLocation, nextBestLocation);
       return new CommonMoveAction(moveActionCommand);
    }

    private Location getNextBestLocation(FightRobotSeeEntity fightRobotSeeEntity) {
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;
//        FightRobotBaseInfo robotInfo = (FightRobotBaseInfo)fightRobotSeeEntity.robotBaseInfo;
        Location moveLocation = aroundBestLocation(currentLocation);
        if(null == moveLocation) {
            moveLocation = nearestBestLocation(currentLocation);
        }
        return moveLocation;
    }
    
    private Location aroundBestLocation(Location currentLocation) {
        List<Location> nextLocations = getNextLocations(currentLocation);
        Location moveLocation = null;
        if(ENEMY_ROBOTS.size() == 1) {
            moveLocation = attackAroundEnemyRobot(nextLocations);
            if(null == moveLocation) {
                moveLocation  = getAroundBloodBags(nextLocations);
            }   
        }else {
            moveLocation = getAroundBloodBags(nextLocations);
            if(null == moveLocation) {
                moveLocation  = attackAroundEnemyRobot(nextLocations);
            }   
        }
        return moveLocation;
    }
    
    private Location attackAroundEnemyRobot(List<Location> nextLocations) {
        List<Location> robotLocations  = getCanAttackRobotLocations(nextLocations);
        if(isEmptyLoction(robotLocations)) {
            return null;
        }else if (robotLocations.size() == 1) {
            return robotLocations.get(0);
        }else {
            Location moveLocaiton = robotLocations.get(0);
            int num = 0;
            for(Location robot : robotLocations) {//算了，就样把
                List<Location> aroundLocation = getNextLocations(robot, 5);
                List<Location> otherBloogBags = this.hasBloodBags(aroundLocation);
                if(otherBloogBags.size() > num) {
                    num = otherBloogBags.size();
                    moveLocaiton = robot;
                }
            }
            return moveLocaiton;
        }
    }
    
    private Location getAroundBloodBags(List<Location> nextLocations) {
        List<Location> bloodBags  = getSafetyBloodBagsLocations(nextLocations);
        if(isEmptyLoction(bloodBags)) {
            return null;
        }else if (bloodBags.size() == 1) {
            return bloodBags.get(0);
        }else {
            Location moveLocaiton = bloodBags.get(0);
            int num = 0;
            for(Location bloodBag : bloodBags) {//算了，就样把
                List<Location> aroundLocation = getNextLocations(bloodBag, 5);
                List<Location> otherBloogBags = this.hasBloodBags(aroundLocation);
                if(otherBloogBags.size() > num) {
                    num = otherBloogBags.size();
                    moveLocaiton = bloodBag;
                }
            }
            return moveLocaiton;
        }
    }
    
    private Location nearestBestLocation(Location currentLocation) {
        Location moveLocation = null;
        moveLocation = getNearestBloodBagsMoveLocation(currentLocation);
        if(null == moveLocation) {
            moveLocation = attackNearestEnemyRobotMoveLocation(currentLocation);
        }

        return moveLocation;
    }

    

    private Location getNearestBloodBagsMoveLocation(Location currentLocation) {
        
        if(isEmptyLoction(BLOOD_BAG_LOCATIONS)) {
            return null;
        }
        
        List<Location> nextLocations = getNextLocations(currentLocation);
        List<Location> nextSafeLocations = getNextSafeLocations(nextLocations);
        if(isEmptyLoction(nextSafeLocations)) {
            nextSafeLocations = nextLocations;// 全是雷，找个雷最少的，或者靠近血包的  TODO
//            return nextLocations.get(0); 
        }
        
        Location nextMoveLocation = null;
        Location nextGoodMoveLocation = null;
        int shortMoveDistance = Integer.MAX_VALUE;
        int shortGoodMoveDistance = Integer.MAX_VALUE;
        
        for(Location location : nextSafeLocations) {
            //Location nearestLocation = null;   //TODO: 可能多人争抢一个血包
            int shortDistance = Integer.MAX_VALUE;
            int shortGoodDistance = Integer.MAX_VALUE;
            for(Location bloodBagLocations : BLOOD_BAG_LOCATIONS) {
                int d = calculateDistance(bloodBagLocations, location);
                if(d < shortDistance) {
                    shortDistance = d;
                    //nearestLocation = bloodBagLocations;
                }
                
                List<Location> aroundBags =  this.getNextLocations(bloodBagLocations, d-3);
                List<Location> enemyRobots = this.getEnemyRobotLocations(aroundBags);
                if(isEmptyLoction(enemyRobots) || enemyRobots.size() < 2 || ENEMY_ROBOTS.size() < 2) {
                    if(d < shortGoodDistance) {
                        shortGoodDistance = d;
                        //nearestLocation = bloodBagLocations;
                    }
                    //nearestLocation = bloodBagLocations;
                }
                
            }
            
            if(shortDistance < shortMoveDistance) {
                shortMoveDistance = shortDistance;
                nextMoveLocation = location;
            }
            
            if(shortGoodDistance < shortGoodMoveDistance) {
                shortGoodMoveDistance = shortGoodDistance;
                nextGoodMoveLocation = location;
            }
        }
        if(null != nextGoodMoveLocation) {
            return nextGoodMoveLocation;
        }else {
            return nextMoveLocation;
        }
    }
    
    private Location attackNearestEnemyRobotMoveLocation(Location currentLocation) {

        List<Location> nextLocations = getNextLocations(currentLocation);
        if(isEmptyLoction(ENEMY_ROBOTS_LOCATIONS)) {
            return nextLocations.get(0); //没有活着的机器人了，
        }
        
        List<Location> nextSafeLocations = getNextSafeLocations(nextLocations);
        if(isEmptyLoction(nextSafeLocations)) {
            nextSafeLocations = nextLocations;// 全是雷，找个雷最少的，或者靠近血包的  TODO
//          return nextLocations.get(0); 
        }
        
        Location nextMoveLocation = null;
        if(ENEMY_ROBOTS_LOCATIONS.size() == 1) { //只剩1个机器人
            FightRobotBaseInfo enemyRobot = ENEMY_ROBOTS.get(0);
            if(enemyRobot.bloodNum > SELF_BLOOD_NUM) {
                int d = calculateDistance(enemyRobot.currentLocation, currentLocation);
                if(d > 3) { 
                    nextMoveLocation = nearToEnemyRobot(nextSafeLocations, ENEMY_ROBOTS_LOCATIONS);
                }else { 
                    nextMoveLocation = runAwayEnemyRobot(nextSafeLocations, ENEMY_ROBOTS_LOCATIONS);
                }
            }else { 
                nextMoveLocation = nearToEnemyRobot(nextSafeLocations, ENEMY_ROBOTS_LOCATIONS);
            }
        }else {  
            List<Location> lowBloodEnemyLocation = new ArrayList<>();
            Location minBloodEnemyLocation = null;
            int minBlood = Integer.MAX_VALUE;
            for(FightRobotBaseInfo enemyRobot : ENEMY_ROBOTS) {
                if(enemyRobot.bloodNum < SELF_BLOOD_NUM) {
                    lowBloodEnemyLocation.add(enemyRobot.currentLocation);
                }
                if(enemyRobot.bloodNum < minBlood) {
                    minBlood = enemyRobot.bloodNum;
                    minBloodEnemyLocation = enemyRobot.currentLocation;
                }
            }
            
            if(!isEmptyLoction(lowBloodEnemyLocation)) {
                if(SELF_BLOOD_NUM < 3) {
                    nextMoveLocation = runAwayEnemyRobot(nextSafeLocations, lowBloodEnemyLocation);
                }else{
                    nextMoveLocation = nearToEnemyRobot(nextSafeLocations, lowBloodEnemyLocation);
                }
            }else {
                List<Location> minBloodEnemyLocations = new ArrayList<>();
                minBloodEnemyLocations.add(minBloodEnemyLocation);
                int d = calculateDistance(minBloodEnemyLocation, currentLocation); 
                
                if(d > 3) {
                    nextMoveLocation = nearToEnemyRobot(nextSafeLocations, minBloodEnemyLocations);
                }else {
                    nextMoveLocation = runAwayEnemyRobot(nextSafeLocations, minBloodEnemyLocations);
                }
            }
        }
        return nextMoveLocation;
    }
    
    private Location nearToEnemyRobot(List<Location> canMoveLocations, List<Location> compareLocations) {
        Location moveLocation = null;
        int shortMoveDistance = Integer.MAX_VALUE;
        for(Location location : canMoveLocations) {
            int shortDistance = Integer.MAX_VALUE;
            for(Location compareLocation : compareLocations) {
                int d = calculateDistance(compareLocation, location);
                if(d < shortDistance) {
                    shortDistance = d;
                }
            }
            
            if(shortDistance < shortMoveDistance) {
                shortMoveDistance = shortDistance;
                moveLocation = location;
            }
        }
        return moveLocation;
    }
    
    private Location runAwayEnemyRobot(List<Location> canMoveLocations, List<Location> compareLocations) {
        Location moveLocation = null;
        int longMoveDistance = 0;
        for (Location location : canMoveLocations) {
            int shortDistance = Integer.MAX_VALUE;
            for (Location compareLocation : compareLocations) {
                int d = calculateDistance(compareLocation, location);
                if (d < shortDistance) {
                    shortDistance = d;
                }
            }

            if (shortDistance > longMoveDistance) {
                longMoveDistance = shortDistance;
                moveLocation = location;
            }
        }
        return moveLocation;
    }
    
    private int calculateDistance(Location la,Location lb) {
        int lax = la.x;
        int lay = la.y;
        int lbx = lb.x;
        int lby = lb.y;
        int sizex = Math.abs(lax-lbx);
        int sizey = Math.abs(lay-lby);
    
        List<Location> allLocation = new ArrayList<>();
        for(int i = 0; i <= sizex ; i++) {
            for(int j = 0; j <= sizey; j++) {
                int tx = 0; 
                int ty = 0;
                if(lax > lbx) {
                    tx = lax - i;
                }else {
                    tx = lax + i;
                }
                
                if(lay > lby) {
                    ty = lay - j;
                }else {
                    ty = lay + j;
                }
                Location tmpL = new Location(tx,ty);
                allLocation.add(tmpL);
            }
        }
        int d = Math.abs(la.x-lb.x) + Math.abs(la.y-lb.y);
        List<Location> landMine  = this.hasLandmine(allLocation);
        if(landMine.size() >= (d-1)) {
            return d+2;
        }else {  
            return d;
        }
    }
 
    
    private List<FightRobotBaseInfo> attackRobots(FightRobotBaseInfo selfRobotInfo) {
        List<FightRobotBaseInfo> enemyRobotList = new ArrayList<>();
        for(FightRobotBaseInfo robot : ENEMY_ROBOTS) {
            if(robot.currentLocation.equals(selfRobotInfo.currentLocation) && !robot.name.equals(selfRobotInfo.name)) {
                enemyRobotList.add(robot);
            }
        }
        return enemyRobotList;
    }
    
    private Location calculateDistance(Location tempLocation,Location nearestLocation,Location currentLocation) {
        if(null == nearestLocation) {
            return tempLocation;
        }
        
        int laxdistance = tempLocation.x - currentLocation.x;
        int laydistance = tempLocation.y - currentLocation.y;
        int ladistance = Math.abs(laxdistance) + Math.abs(laydistance);
        
        int lbxdistance = nearestLocation.x - currentLocation.x;
        int lbydistance = nearestLocation.y - currentLocation.y;
        int lbdistance = Math.abs(lbxdistance) + Math.abs(lbydistance);
        
        if(ladistance < lbdistance) {
            return tempLocation;
        }else {
            return nearestLocation;
        }
        
    }
 
    private List<Location> filterDangerEnemyRobot(List<Location> moveToLocations) {
        if(isEmptyLoction(moveToLocations)) {
            return null;
        }
        
        List<Location> cantAttack =  new ArrayList<>();
        for(Location moveToLocation : moveToLocations) {
            List<Location> aroundLocation = getNextLocations(moveToLocation);
            List<FightRobotBaseInfo> dangerLocation = getEnemyRobots(aroundLocation);
            if(!dangerLocation.isEmpty()) {
                boolean canAttack = true;
                if(ENEMY_ROBOTS.size() == 1) {
                    for(FightRobotBaseInfo enemy : dangerLocation) {
                        if(enemy.bloodNum - SELF_BLOOD_NUM >= 0 && SELF_BLOOD_NUM <= 4) {
                            canAttack = false;
                        }
                    }
                }else {
                    if(SELF_BLOOD_NUM <= 4) {
                        canAttack = false;
                    }else{
                        for(FightRobotBaseInfo enemy : dangerLocation) {
                            if(enemy.bloodNum - SELF_BLOOD_NUM >= 0) {
                                canAttack = false;
                            }
                        }
                    }
                }
                
                if(!canAttack) {
                    cantAttack.add(moveToLocation);
                }
            }
        }
        moveToLocations.removeAll(cantAttack);
        return moveToLocations;
    }
     
 
    private List<Location> getSafetyBloodBagsLocations(List<Location> nextLocations){
        List<Location> bloodBagsLocations  = hasBloodBags(nextLocations);
        return filterDangerEnemyRobot(bloodBagsLocations);
    }
    
    private List<Location> getCanAttackRobotLocations(List<Location> nextLocations){
        List<Location> robotLocations  = getEnemyRobotLocations(nextLocations);
        return filterDangerEnemyRobot(robotLocations);
    }
    
    private List<Location> getNextSafeLocations(List<Location> nextLocations){
        List<Location> moveLocation = new ArrayList<>();
        for(Location location :  nextLocations) {
            moveLocation.add(location);
        }
        List<Location> landmines = hasLandmine(nextLocations);
        moveLocation.removeAll(landmines);
        return filterDangerEnemyRobot(moveLocation);
    }
    
    private List<Location> getEnemyRobotLocations(List<Location> checkLocations){
        return  hasElements(checkLocations, ENEMY_ROBOTS_LOCATIONS);
     }
    
    private List<FightRobotBaseInfo> getEnemyRobots(List<Location> checkLocations){
        List<FightRobotBaseInfo> containsLocations  = new ArrayList<>();
        
        if(isEmptyLoction(checkLocations)) {
            return containsLocations;
        }
        
        for (Location location : checkLocations) {
            for (FightRobotBaseInfo robot : ENEMY_ROBOTS) {
                if (location.equals(robot.currentLocation)) {
                    containsLocations.add(robot);
                }
            }
        }
        
        return  containsLocations;
     }
    
    
    private List<Location> hasBloodBags(List<Location> checkLocations){
       return  hasElements(checkLocations ,BLOOD_BAG_LOCATIONS);
    }
    
    private List<Location> hasLandmine(List<Location> checkLocations){
        return  hasElements(checkLocations, LANDMINE_LOCATIONS);
     }
    
    private List<Location> hasElements(List<Location> checkLocations,List<Location> elementLocations){
        List<Location> containsLocations  = new ArrayList<>();
        
        if(null == checkLocations || checkLocations.isEmpty()) {
            return containsLocations;
        }
        if(null == elementLocations || elementLocations.isEmpty()) {
            return containsLocations;
        }
        
        for (Location location : checkLocations) {
            for (Location elementLocation : elementLocations) {
                if (location.equals(elementLocation)) {
                    containsLocations.add(elementLocation);
                }
            }
        }
       return  containsLocations;
    }
    
    private void initVariableParameter(FightRobotSeeEntity fightRobotSeeEntity ) {
        MAP_SIZE = fightRobotSeeEntity.mapInfo.size;
        BLOOD_BAG_LOCATIONS = new ArrayList<>();
        LANDMINE_LOCATIONS = new ArrayList<>();
        ENEMY_ROBOTS_LOCATIONS = new ArrayList<>();
        ENEMY_ROBOTS = new ArrayList<>();
        FightRobotBaseInfo selfRobotInfo = (FightRobotBaseInfo)fightRobotSeeEntity.robotBaseInfo;
        SELF_BLOOD_NUM  = selfRobotInfo.bloodNum;
//        DANGER_LOCATIONS = new ArrayList<>();
    }
    
    private void resetMapInfo(FightRobotSeeEntity fightRobotSeeEntity) {
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        if(null == elementLists) {
            return;
        }
        
        FightRobotBaseInfo seleRobotInfo = (FightRobotBaseInfo)fightRobotSeeEntity.robotBaseInfo;
        
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        BLOOD_BAG_LOCATIONS.add(((BloodBag)element).location);
                    }else  if (ElementTypeEnum.LANDMINE == element.elementType) {
                        LANDMINE_LOCATIONS.add(((Landmine)element).location);
                    }else  if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo robotInfo = (FightRobotBaseInfo)element;
                        if(!robotInfo.name.equals(seleRobotInfo.name)){
                            ENEMY_ROBOTS.add(robotInfo);
                            ENEMY_ROBOTS_LOCATIONS.add(robotInfo.currentLocation);
                        }
                    }
                }
            }
        }
    }
    
    private List<Location> getNextLocations(Location currentLocation) {
            return getNextLocations(currentLocation,1);
    }
    
    private List<Location> getNextLocations(Location currentLocation,int step) {
        List<Location> nextLocations = new ArrayList<>();
        if(step < 0) {
            return nextLocations;
        }
        
        for(int i = 0; i <= step; i++) {
            for(int j = (step-i); j >= 0 ; j-- ) {
                if(i == 0 && j == 0) {
                    continue;
                }
                addLocation(nextLocations,currentLocation.x+i,currentLocation.y+j);
                addLocation(nextLocations,currentLocation.x+i,currentLocation.y-j);
                addLocation(nextLocations,currentLocation.x-i,currentLocation.y+j);
                addLocation(nextLocations,currentLocation.x-i,currentLocation.y-j);
            }
        }
        return nextLocations;
    }
    
    private MoveActionCommandEnum moveActionCommandEnum(Location currentLocation,Location nextLocation) {
        if(nextLocation == null) {
            System.out.println("====================");
        }
        MoveActionCommandEnum moveActionCommand = null;
        //竟然是：向右y，向下x
        if(currentLocation.x == nextLocation.x) {
            if(nextLocation.y == currentLocation.y - 1) {
                moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
            }else if(nextLocation.y == currentLocation.y + 1) {
                moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
            }else {
                System.out.println("TODO:程序异常了，走错了.x==x,y!=");
            }
        }else if(currentLocation.y == nextLocation.y) {
            if(nextLocation.x == currentLocation.x - 1) {
                moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
            }else if(nextLocation.x == currentLocation.x + 1) {
                moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
            }else {
                System.out.println("TODO:程序异常了，走错了.y==y,x!=");
            }
        }else {
            System.out.println("TODO:程序异常了，走错了");
        }
        return moveActionCommand;
    }
    
    private void addLocation(List<Location> locationList, int x, int y) {
        if(isOutOfMap(x, y)) {
            return;
        }
        Location location = new Location(x, y);
        if(!locationList.contains(location)) {
            locationList.add(location);
        }
    }
    
    private boolean isEmptyLoction(List<Location> locations) {
        return (null == locations || locations.isEmpty());
    }
    
    private boolean isOutOfMap(int x,int y) {
        return (x<0 || y<0 || x>= MAP_SIZE || y >= MAP_SIZE);
    }
    
    public static void main(String[] args) {
        KingOfFightRobot kingOfFightRobot = new KingOfFightRobot();
//        MAP_SIZE = 15;
        Location la = new Location(5,2);
        Location lb = new Location(2,4);
//        List<Location> nextLocations = kingOfFightRobot.getNextLocations(currentLocation, 3);
//        for(Location location : nextLocations) {
//            System.out.println(location.x +""+ location.y);
//        }
        
        kingOfFightRobot.calculateDistance(la, lb);
    }
}
