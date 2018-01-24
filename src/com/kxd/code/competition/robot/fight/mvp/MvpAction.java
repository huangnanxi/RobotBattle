package com.kxd.code.competition.robot.fight.mvp;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.ArrayList;
import java.util.List;


public class MvpAction {

    public Location                           currentRobotLocation;

    public FightRobotSeeEntity                fightRobotSeeEntity;

    public FightRobotBaseInfo                 fightRobotBaseInfo;

    public AbstractMapInfo                    mapInfo;

    private List<Landmine>                     landmines                = new ArrayList<>();

    private List<BloodBag>                     bloodBags                = new ArrayList<>();

    private List<FightRobotBaseInfo>           otherFightRobotBaseInfos = new ArrayList<>();

    private MvpUtil mvpUtil;

    public MvpAction(FightRobotSeeEntity fightRobotSeeEntity) {

        mvpUtil = new MvpUtil(fightRobotSeeEntity);

        this.fightRobotSeeEntity = fightRobotSeeEntity;
        this.fightRobotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        this.mapInfo = fightRobotSeeEntity.mapInfo;
        this.currentRobotLocation = this.fightRobotBaseInfo.currentLocation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
//                locationElementListMap.put(new Location(i, j), elementList);
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        bloodBags.add((BloodBag) element);
                    }
                    else if (ElementTypeEnum.LANDMINE == element.elementType) {
                        landmines.add((Landmine) element);
                    }
                    else if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        if (!isMyOwn(fightRobotBaseInfo)) {
                            otherFightRobotBaseInfos.add(fightRobotBaseInfo);
                        }
                    }
                }
            }
        }
    }

    private boolean isMyOwn(FightRobotBaseInfo fightRobotBaseInfo) {
        if (fightRobotBaseInfo.currentLocation.equals(currentRobotLocation)) {
            return true;
        }
        return false;
    }

    public void PrintMap(ElementList[][] elementLists) {

    }

    public boolean isLocationHasLandmine(Location location) {
        for (Landmine landmine : this.landmines) {
            if (location.equals(landmine.location)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAroundHasEnemies(Location location) {
        List<FightRobotBaseInfo> aroundEnemies = getAroundEnemies(location);
        if (!aroundEnemies.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isLocationDangerous(Location location) {

        if (isLocationHasLandmine(location)) {
            return true;
        }
        if (isAroundHasEnemies(location)) {
            return true;
        }

        return false;
    }

//    private Location getBestLocation(List<Location> targetLocations) {
//        List<Location> bestLocations = new ArrayList<>();
//        for (Location targetLocation : targetLocations) {
//            if (!isLocationDangerous(targetLocation)) {
//                bestLocations.add(targetLocation);
//            }
//        }
//        if (tuzhiUtil.isEmpty(bestLocations)) {
//            return null;
//        }
//
//        return this.getSaferLocation(bestLocations);
//    }

    private Location getFisrtSafeLocation(List<Location> targetLocations) {
        for (Location targetLocation : targetLocations) {
            if (!isLocationDangerous(targetLocation)) {
                return targetLocation;
            }
        }
        return null;
    }

    private List<FightRobotBaseInfo> getAroundEnemies(Location currentLocation) {
        List<FightRobotBaseInfo> aroundEnemies = new ArrayList<>();
        List<Location> aroundLocations = mvpUtil.getAroundLocations(currentLocation);

        for (FightRobotBaseInfo otherFightRobotBaseInfo : otherFightRobotBaseInfos) {

            for (Location aroundLocation : aroundLocations) {
                if (otherFightRobotBaseInfo.currentLocation.equals(aroundLocation)) {
//                    System.out.println( " current " + currentLocation.x + " " + currentLocation.y + " around " + aroundLocation.x + " " + aroundLocation.y );
                    aroundEnemies.add(otherFightRobotBaseInfo);
                }
            }
        }

        return aroundEnemies;
    }

    public CommonMoveAction moveActionForAttack() {
         List<FightRobotBaseInfo> aroundEnemies = this.getAroundEnemies(this.currentRobotLocation);

        if (!mvpUtil.isEmpty(aroundEnemies)) {
            // attack

            //TODO modify
            return mvpUtil.getActionToTarget(aroundEnemies.get(0).currentLocation);

        }
        return null;
    }

    private List<BloodBag> getAroundBloodBags(Location currentLocation) {
        List<BloodBag> aroundBloodBags = new ArrayList<>();
        List<Location> aroundLocations = mvpUtil.getAroundLocations(currentLocation);

        for (BloodBag bloodBag : this.bloodBags) {
            for (Location aroundLocation : aroundLocations) {
                if (bloodBag.location.equals(aroundLocation)) {
                    aroundBloodBags.add(bloodBag);
                }
            }
        }
        return aroundBloodBags;
    }

    public CommonMoveAction moveActionForBlood() {

        if(mvpUtil.isEmpty(bloodBags))
        {
            return null;
        }
        //get aroundBloodBags
        List<BloodBag> aroundBloodBags = this.getAroundBloodBags(this.currentRobotLocation);

        if (!mvpUtil.isEmpty(aroundBloodBags)) {

            return mvpUtil.getActionToTarget(aroundBloodBags.get(0).location);

            //TODO modify
//            List<Location> targetLocations = new ArrayList<>();
//            for (BloodBag bloodBag : aroundBloodBags) {
//
//                targetLocations.add(bloodBag.location);
//            }
//            if (!MvpUtil.isEmpty(targetLocations)) {
//                Location bestLocation = determineBestLocation(targetLocations);
//                return this.getMoveToTargetAction(bestLocation);
//            }
        }

        //get nearestBloodBag
        MvpSortByDis tuzhiSortByDis = new MvpSortByDis(this.currentRobotLocation, this.bloodBags);
        List<Location> bloodBagsSorted = tuzhiSortByDis.SortByDis();

        if (!mvpUtil.isEmpty(bloodBagsSorted)) {

//            return tuzhiUtil.getActionToTarget(bloodBagsSorted.get(0).targetlocation);
//            return tuzhiUtil.getActionToTarget(bloodBagsSorted.get(0));

            List<Location> nextLocations = mvpUtil.getNextLocations(bloodBagsSorted);
            return mvpUtil.getActionToTarget(getFisrtSafeLocation(nextLocations));
        }

        /*
        NearestWayInfo nearestBloodBagLocationWay = getNearestBloodBagLocationWay();
        if (nearestBloodBagLocationWay == null) {
            return null;
        }
        List<Location> nextLocationsForTarget = nearestBloodBagLocationWay.getNextLocationsForTarget();
        if (!isEmpty(nextLocationsForTarget)) {
            return this.getMoveToTargetAction(determineBestLocation(nextLocationsForTarget));
        }
        */

        return null;
    }

    public Location getSaferLocation(List<Location> locations) {
        Location location = null;
        int center = mapInfo.size / 2;
        int minDis = 10000;
        int maxSafeCoef = 0;

        for (Location tmp : locations){
            int dis = Math.abs(center - tmp.x) + Math.abs(center - tmp.y);
            int safeCoef = this.getSafeAroundLocations(tmp).size();
            if( safeCoef > maxSafeCoef){
                maxSafeCoef = safeCoef;
                location = new Location(tmp.x, tmp.y);
                minDis = dis;
            }
            else if(safeCoef == maxSafeCoef){
                if( dis < minDis ){
                    minDis = dis;
                    location = new Location(tmp.x, tmp.y);
                }
            }
        }
        return location;
    }

    public List<Location> getSafeAroundLocations(Location currentLocation) {
        List<Location> locations = new ArrayList<>();
        List<Location> aroundLocations = mvpUtil.getAroundLocations(currentLocation);
        for (Location location : aroundLocations) {
            if (isLocationDangerous(location)) {
                continue;
            }
            locations.add(location);
        }
        return locations;
    }

    public List<Location> getNoEnemiesAroundLocations(Location currentLocation) {
        List<Location> locations = new ArrayList<>();
        List<Location> aroundLocations = mvpUtil.getAroundLocations(currentLocation);
        for (Location location : aroundLocations) {
            if (isAroundHasEnemies(location)) {
                continue;
            }
            locations.add(location);
        }
        return locations;
    }

    public CommonMoveAction moveActionIdle() {
        List<Location> safeAroundLocations = this.getSafeAroundLocations(this.currentRobotLocation);
        if (mvpUtil.isEmpty(safeAroundLocations)) {
            return null;
        }
        return mvpUtil.getActionToTarget(this.getSaferLocation(safeAroundLocations));
    }

    public CommonMoveAction moveActionNotAttacked() {
        List<Location> noEnemiesAroundLocations = this.getNoEnemiesAroundLocations(this.currentRobotLocation);
        if (mvpUtil.isEmpty(noEnemiesAroundLocations)) {
            return null;
        }
        return mvpUtil.getActionToTarget(this.getSaferLocation(noEnemiesAroundLocations));
    }

    public CommonMoveAction moveActionForced() {
        List<Location> AroundLocations = mvpUtil.getAroundLocations(this.currentRobotLocation);
        if (mvpUtil.isEmpty(AroundLocations)) {
            return null;
        }
        return mvpUtil.getActionToTarget(this.getSaferLocation(AroundLocations));
    }
}
