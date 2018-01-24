package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;

/**
 * @author mengqingyan 2018/1/8
 */
public class RobotContext {

    static Sign47Location[][]                  locations;

    private List<Landmine>                     landmines                = new ArrayList<>();

    private List<BloodBag>                     bloodBags                = new ArrayList<>();

    private List<FightRobotBaseInfo>           otherFightRobotBaseInfos = new ArrayList<>();

    FightRobotBaseInfo                 currentFightRobotBaseInfo;

    private AbstractMapInfo                    mapInfo;

    private Location                           currentRobotLocation;

    private Map<Location, ElementList>         locationElementListMap   = new HashMap<>();

    private Map<NearestWayKey, NearestWayInfo> nearestWayInfoMap        = new HashMap<>();

    private FightRobotSeeEntity                fightRobotSeeEntity;

    private RobotAttackedInfo                  robotAttackedInfo;

    private static final int MIN_BLOOD = 14;
    private static final int MAX_BLOOD = 26;


    public RobotContext(FightRobotSeeEntity fightRobotSeeEntity, RobotAttackedInfo robotAttackedInfo) {
        this.fightRobotSeeEntity = fightRobotSeeEntity;
        this.robotAttackedInfo = robotAttackedInfo;

        this.currentFightRobotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        this.mapInfo = fightRobotSeeEntity.mapInfo;
        this.currentRobotLocation = this.currentFightRobotBaseInfo.currentLocation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                locationElementListMap.put(new Location(i, j), elementList);
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        bloodBags.add((BloodBag) element);
                    } else if (ElementTypeEnum.LANDMINE == element.elementType) {
                        landmines.add((Landmine) element);
                    } else if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        if (!isMyOwn(fightRobotBaseInfo)) {
                            otherFightRobotBaseInfos.add(fightRobotBaseInfo);
                        }

                    }
                }
            }
        }

        if (this.robotAttackedInfo.getRobotInitSize() == 0) {
            this.robotAttackedInfo.setRobotInitSize(this.otherFightRobotBaseInfos.size() + 1);
        }
        initLocations();

        if(this.currentFightRobotBaseInfo.bloodNum <= MIN_BLOOD) {
            this.robotAttackedInfo.setFightRound(false);
        } else if(this.currentFightRobotBaseInfo.bloodNum >= MAX_BLOOD) {
            this.robotAttackedInfo.setFightRound(true);
        }
    }

    public List<Landmine> getLandmines() {
        return landmines;
    }

    public List<BloodBag> getBloodBags() {
        return bloodBags;
    }

    public FightRobotBaseInfo getCurrentFightRobotBaseInfo() {
        return currentFightRobotBaseInfo;
    }

    public Location getCurrentRobotLocation() {
        return currentRobotLocation;
    }

    private void initLocations() {
        int mapSize = this.mapInfo.size.intValue();
        if (locations == null || locations.length != mapSize) {
            locations = generateSign47Locations(mapSize);
        } else {
            clearSign47Locs(locations);
        }
    }

    private void clearSign47Locs(Sign47Location[][] locations) {
        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations[0].length; j++) {
                locations[i][j].reset(this);
            }
        }
    }

    public CommonMoveAction bestMoveActionForBreak() {
        CommonMoveAction commonMoveAction = null;

        initSign47Locations(this.currentRobotLocation, null, RobotContext.locations);

        if (!needToBreak(RobotContext.locations)) {
            return null;
        }
        List<Location> breakPoints = getBreakPoints(RobotContext.locations);
        breakPoints.add(this.currentRobotLocation);
        Map<Location, BreakInfo> breakInfoMap = getBreakInfoMap(breakPoints);

        Location breakPointLocHis = this.robotAttackedInfo.getBreakPointLoc();
        if (breakPointLocHis != null && breakInfoMap.containsKey(breakPointLocHis)
                && this.currentRobotLocation.equals(breakPointLocHis)) {
            // 突破点
            Location breakLoc = breakPointLocHis;
            BreakInfo breakInfo = breakInfoMap.get(breakLoc);
            BreakInfo.MaxBenifit maxBenift = breakInfo.getMaxBenift(this);
            BreakInfo.BreakDetailInfo bestBreakDetailInfo = maxBenift.getBestBreakDetailInfo();
            Location breakTargetLoc = bestBreakDetailInfo.getBreakTargetLocation();
            Location nextStepLoc = getNextBreakStepLoc(breakLoc, breakTargetLoc);
            if (nextStepLoc == null) {
                return null;
            }
            Location nextLocation = this.determineBestLocationOfSafe(Arrays.asList(nextStepLoc));

            return this.getMoveToTargetAction(nextLocation);
        }

        List<BreakInfo> breakInfos = new ArrayList<>(breakInfoMap.values());

        if (!isEmpty(breakInfos)) {

            Collections.sort(breakInfos, new Comparator<BreakInfo>() {
                @Override
                public int compare(BreakInfo o1, BreakInfo o2) {
                    return o2.getMaxBenift(RobotContext.this).getBenifit() - o1.getMaxBenift(RobotContext.this).getBenifit();
                }
            });

            BreakInfo breakInfo = breakInfos.get(0);
            BreakInfo.MaxBenifit maxBenift = breakInfo.getMaxBenift(RobotContext.this);
            int benifit = maxBenift.getBenifit();
            if (benifit < 0) {
                return null;
            }
            List<BreakInfo> maxBenifitBreakInfos = new ArrayList<>();
            for (BreakInfo bi:breakInfos) {
                if(bi.getMaxBenift(RobotContext.this).getBenifit() == benifit) {
                    maxBenifitBreakInfos.add(bi);
                }
            }

            BreakInfo chosenBreakInfo =  getRandomEle(maxBenifitBreakInfos);


            Location breakPointLoc = chosenBreakInfo.getBreakPointLoc();
            this.robotAttackedInfo.setBreakPointLoc(breakPointLoc);
            List<NearestWayInfo> nearestWayInfos = this.getNearestWayInfos(this.currentRobotLocation,
                    Arrays.asList(breakPointLoc));
            Set<Location> nextStepLocs = new HashSet<>();
            for (NearestWayInfo nearestWayInfo : nearestWayInfos) {
                nextStepLocs.addAll(nearestWayInfo.getNextLocationsForTarget());
            }
            Location nextLocation = this.determineBestLocation(new ArrayList<>(nextStepLocs));

            return this.getMoveToTargetAction(nextLocation);
        }

        return commonMoveAction;

    }

    private <T> T getRandomEle(List<T> eles) {
        int size = eles.size();
        Random random = new Random(System.currentTimeMillis());
        return eles.get(Math.abs(random.nextInt()) % size);

    }

    private Location getNextBreakStepLoc(Location breakLoc, Location breakTargetLoc) {
        int dis = disBetween(breakLoc, breakTargetLoc);
        List<Location> aroundLocations = getAroundLocations(breakLoc);
        for (Location nextLocation : aroundLocations) {
            if (disBetween(nextLocation, breakTargetLoc) == dis - 1) {
                if (!isAroundHasEnemies(nextLocation)) {
                    return nextLocation;
                }
            }
        }
        return null;
    }

    private Map<Location, BreakInfo> getBreakInfoMap(List<Location> breakPoints) {
        Map<Location, BreakInfo> locationBreakInfoMap = new HashMap<>();
        List<FightRobotBaseInfo> otherFightRobotBaseInfos = this.otherFightRobotBaseInfos;
        List<Location> breakTargetLocs = new ArrayList<>();
        for (BloodBag bloodBag : this.bloodBags) {
            breakTargetLocs.add(bloodBag.location);
        }
        if(otherFightRobotBaseInfos.size() <=2 && getArrivableEnemies(this.currentRobotLocation).size() == 0) {
            FightRobotBaseInfo maxBlood = getMaxBlood(this.otherFightRobotBaseInfos);
            if(canFightParamed(maxBlood, 4, 20)) {
                for (FightRobotBaseInfo enemy:otherFightRobotBaseInfos) {
                    breakTargetLocs.add(enemy.currentLocation);
                }
            }
        }

        for (Location breakPoint : breakPoints) {
            BreakInfo breakInfo = new BreakInfo();
            breakInfo.setBreakPointLoc(breakPoint);
            boolean accept = false;

            for (Location breakTargetLoc : breakTargetLocs) {
                Sign47Location[][] sign47Locations = generateSign47Locations(this.mapInfo.size);
                initSign47Locations(breakTargetLoc, null, sign47Locations);
                List<Location> arrivableLocations = getArrivableLocations(sign47Locations);
                arrivableLocations.add(breakTargetLoc);
                int minDis = Integer.MAX_VALUE;
                Location breakTargetLocation = null;
                for (Location arrivableLocation : arrivableLocations) {
                    int dis = disBetween(breakPoint, arrivableLocation);
                    if (minDis > dis) {
                        minDis = dis;
                        breakTargetLocation = arrivableLocation;
                    }
                }
                if (minDis > 3 || minDis <= 1) {
                    continue;
                }
                accept = true;

                BreakInfo.BreakDetailInfo breakDetailInfo = new BreakInfo.BreakDetailInfo();
                breakDetailInfo.setBloodLoc(breakTargetLoc);
                breakDetailInfo.setBreakTargetLocation(breakTargetLocation);
                breakDetailInfo.setLandMineNumToBreak(minDis - 1);

                int availableBloodNum = countArrivableBloodNum(sign47Locations);
                breakDetailInfo.setAvailableBloodNum(availableBloodNum);
                int availableNum = countArrivableNum(sign47Locations);
                breakDetailInfo.setArrivableNum(availableNum);

                breakInfo.addBloodBagInfo(breakDetailInfo);
            }
            if (accept) {
                locationBreakInfoMap.put(breakPoint, breakInfo);
            }
        }
        return locationBreakInfoMap;
    }

    private List<Location> getArrivableLocations(Sign47Location[][] sign47Locations) {
        List<Location> arrivableLocations = new ArrayList<>();
        int rows = sign47Locations.length;
        int cols = sign47Locations[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (sign47Locations[i][j].getStep() > 0) {
                    arrivableLocations.add(sign47Locations[i][j]);
                }
            }
        }
        return arrivableLocations;
    }

    private int countArrivableBloodNum(Sign47Location[][] sign47Locations) {
        int count = 1;
        for (BloodBag bloodBag : this.bloodBags) {
            Sign47Location sign47Location = getSign47Location(bloodBag.location.x, bloodBag.location.y,
                    sign47Locations);
            if (sign47Location.getStep() > 0) {
                count++;
            }
        }
        return count;
    }

    private List<Location> getBreakPoints(Sign47Location[][] locations) {
        List<Location> breakPoints = new ArrayList<>();
        int rows = locations.length;
        int cols = locations[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (locations[i][j].getStep() > 0) {
                    List<Location> aroundLocations = getAroundLocations(locations[i][j]);
                    for (Location location : aroundLocations) {
                        if (isLocationHasLandmine(location)) {
                            breakPoints.add(locations[i][j]);
                            break;
                        }
                    }
                }
            }
        }
        return breakPoints;
    }

    private boolean needToBreak(Sign47Location[][] locations) {
        int arrivableNum = countArrivableNum(locations);
        // int landmineSize = this.landmines.size();
        // int totalSpace = this.mapInfo.size * this.mapInfo.size;
        // int leftSpace = totalSpace - landmineSize;
        // if (leftSpace / arrivableNum >= 2) {
        // return true;
        // }
        // return false;
        int l = this.mapInfo.size* this.mapInfo.size/6;
        if (arrivableNum <= l) {
            return true;
        }
        return false;

    }

    private int countArrivableNum(Sign47Location[][] locations) {
        int count = 1;
        int rows = locations.length;
        int cols = locations[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (locations[i][j].getStep() > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private Sign47Location[][] generateSign47Locations(int mapSize) {

        Sign47Location[][] tmp = new Sign47Location[mapSize][mapSize];
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                tmp[i][j] = new Sign47Location(i, j, this);
            }
        }
        return tmp;
    }

    public Map<Location, NearestWayInfo> getNearestWays(Location startLocation, List<Location> targetLocations) {

        Map<Location, NearestWayInfo> targetLocationNearestWayMap = new HashMap<>();

        boolean allFound = true;
        for (Location targetLocation : targetLocations) {
            NearestWayKey nearestWayKey = new NearestWayKey(startLocation, targetLocation);
            NearestWayInfo nearestWayInfo = nearestWayInfoMap.get(nearestWayKey);

            if (nearestWayInfo == null) {
                allFound = false;
                break;
            }
            targetLocationNearestWayMap.put(targetLocation, nearestWayInfo);
        }
        if (allFound) {
            return targetLocationNearestWayMap;
        }

        // nearestWayInfo = new NearestWayInfo();
        // nearestWayInfoMap.put(nearestWayKey, nearestWayInfo);
        initLocations();
        initSign47Locations(startLocation, targetLocations, RobotContext.locations);

        // printLocation(startLocation, targetLocations);
        for (Location targetLocation : targetLocations) {
            Sign47Location targetSign47Location = getSign47Location(targetLocation.x, targetLocation.y,
                    RobotContext.locations);
            NearestWayInfo nearestWayInfo;
            if (targetSign47Location.getStep() > 25) {
                nearestWayInfo = new NearestWayInfo();
            } else {
                nearestWayInfo = generateNearestWayInfo(targetLocation, startLocation, RobotContext.locations);
            }
            nearestWayInfo.setTargetLocation(targetLocation);

            nearestWayInfoMap.put(new NearestWayKey(startLocation, targetLocation), nearestWayInfo);
            targetLocationNearestWayMap.put(targetLocation, nearestWayInfo);
        }

        return targetLocationNearestWayMap;
    }

    private void initSign47Locations(Location startLocation, List<Location> targetLocations,
            Sign47Location[][] sign47LocationsParam) {
        clearSign47Locs(sign47LocationsParam);

        final Sign47Location startSign47Location = getSign47Location(startLocation.x, startLocation.y,
                sign47LocationsParam);
        // startSign47Location.reset(this);
        Map<Integer, List<Sign47Location>> integerSign47LocationColMap = new HashMap<>();
        Map<Integer, Sign47Location> integerSign47LocationMap = fillLocationDownUp(startSign47Location,
                sign47LocationsParam);
        Map<Integer, List<Sign47Location>> integerSign47LocationRowMap = new HashMap<>();
        addToMapByMinStep(integerSign47LocationRowMap, integerSign47LocationMap);

        int count = 5;
        int maxTryNum = 50;
        while (count-- > 0 || isTargetLocationsStepNotSet(targetLocations, sign47LocationsParam)) {
            for (List<Sign47Location> sign47Locations : integerSign47LocationRowMap.values()) {
                for (Sign47Location rowLocation : sign47Locations) {
                    Map<Integer, Sign47Location> integerSign47LocationLRMap = fillLocationLeftRight(rowLocation,
                            sign47LocationsParam);
                    addToMapByMinStep(integerSign47LocationColMap, integerSign47LocationLRMap);

                }
            }
            for (List<Sign47Location> sign47Locations : integerSign47LocationColMap.values()) {
                for (Sign47Location colLocation : sign47Locations) {
                    Map<Integer, Sign47Location> integerSign47LocationDUMap = fillLocationDownUp(colLocation,
                            sign47LocationsParam);
                    addToMapByMinStep(integerSign47LocationRowMap, integerSign47LocationDUMap);
                }
            }
            maxTryNum--;
            if (maxTryNum < 0) {
                break;
            }

        }
    }

    private NearestWayInfo generateNearestWayInfo(final Location startSign47Location, final Location targetLocation,
            Sign47Location[][] sign47LocationsParam) {
        NearestWayInfo nearestWayInfo = new NearestWayInfo();
        Sign47Location sign47StartLocation = getSign47Location(startSign47Location.x, startSign47Location.y,
                sign47LocationsParam);

        final Set<Location> nextLocationForTarget = new HashSet<>();
        TreeWalker.walkTreePreOrder(sign47StartLocation, new TreeWalker.TreeNodeVisitor<Sign47Location>() {
            int num = 0;

            @Override
            public boolean visitNode(Sign47Location treeNode, Sign47Location parentTreeNode,
                    Map<Sign47Location, Object> visitorContext) {
                // if (treeNode != null) {
                // System.out.print("(" + treeNode.x + "," + treeNode.y + ")");
                // }
                if (treeNode == null) {
                    // printLocation(targetLocation,
                    // Arrays.asList(startSign47Location));
                    return false;
                }

                if (treeNode.equals(targetLocation)) {
                    // System.out.println();
                    nextLocationForTarget.add(parentTreeNode);
                }
                num++;
                if (num > 33554433) {
                    System.err.println("OVER MAX NUM");
                    // printLocation(targetLocation,Arrays.asList(startSign47Location));
                    return false;
                }
                return true;
            }
        });

        nearestWayInfo.setMinStep(sign47StartLocation.getStep());
        nearestWayInfo.setNextLocationsForTarget(new ArrayList<Location>(nextLocationForTarget));
        return nearestWayInfo;
    }

    private boolean isTargetLocationsStepNotSet(List<Location> targetLocations,
            Sign47Location[][] sign47LocationsParam) {
        if (isEmpty(targetLocations)) {
            return false;
        }
        for (Location targetLocation : targetLocations) {
            Sign47Location sign47Location = getSign47Location(targetLocation.x, targetLocation.y, sign47LocationsParam);

            if (sign47Location.getStep() == 0) {
                return true;
            }
        }
        return false;
    }

    private void addToMapByMinStep(Map<Integer, List<Sign47Location>> targetMap,
            Map<Integer, Sign47Location> sourceMap) {
        Iterator<Map.Entry<Integer, Sign47Location>> iterator = sourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Sign47Location> next = iterator.next();
            List<Sign47Location> existsSign47Locations = targetMap.get(next.getKey());
            if (existsSign47Locations == null) {
                existsSign47Locations = new ArrayList<>();
                targetMap.put(next.getKey(), existsSign47Locations);
            }
            if (existsSign47Locations.isEmpty()) {
                existsSign47Locations.add(next.getValue());
            } else {
                if (existsSign47Locations.contains(next.getValue())) {
                    continue;
                }
                int size = existsSign47Locations.size();

                boolean toAdd = false;
                boolean findSame = false;
                int removeIndex = -1;
                for (int i = 0; i < size; i++) {
                    Sign47Location sign47Location = existsSign47Locations.get(i);
                    boolean hasLandMinBetween = hasLandMinBetween(sign47Location, next.getValue());
                    if (hasLandMinBetween) {
                        toAdd = true;
                    } else {
                        toAdd = false;
                        findSame = true;
                        if (sign47Location.getStep() > next.getValue().getStep()) {
                            removeIndex = i;
                            toAdd = true;
                            findSame = false;
                        }
                    }
                }

                if (toAdd && !findSame) {
                    existsSign47Locations.add(next.getValue());
                    if (removeIndex != -1) {
                        existsSign47Locations.remove(removeIndex);
                    }

                }

            }

        }

    }

    private boolean hasLandMinBetween(Sign47Location firstLocation, Sign47Location secondLocation) {
        Sign47Location start;
        Sign47Location end;
        if (firstLocation.x == secondLocation.x) {
            if (firstLocation.y < secondLocation.y) {
                start = firstLocation;
                end = secondLocation;
            } else {
                start = secondLocation;
                end = firstLocation;
            }

            for (int i = start.y + 1; i < end.y; i++) {
                if (isLocationHasLandmine(locations[start.x][i])) {
                    return true;
                }
            }
        } else if (firstLocation.y == secondLocation.y) {
            if (firstLocation.x < secondLocation.x) {
                start = firstLocation;
                end = secondLocation;
            } else {
                start = secondLocation;
                end = firstLocation;
            }
            for (int i = start.x + 1; i < end.x; i++) {
                if (isLocationHasLandmine(locations[i][start.y])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void printLocation(Location startLocation, List<Location> targetLocations, Sign47Location[][] locations) {
        Set<Location> targetLocationSet = null;
        if (isEmpty(targetLocations)) {
            targetLocationSet = new HashSet<>();
        } else {
            targetLocationSet = new HashSet<>(targetLocations);
        }

        for (int i = 0; i < this.mapInfo.size; i++) {
            for (int j = 0; j < this.mapInfo.size; j++) {
                Sign47Location sign47Location = locations[i][j];

                if (sign47Location.equals(startLocation)) {
                    System.out.printf("%-5s", "A");
                } else if (targetLocationSet.contains(sign47Location)) {
                    System.out.printf("%-5s", "T(" + sign47Location.getStep() + ")");
                } else if (isLocationHasLandmine(sign47Location)) {
                    System.out.printf("%-5s", "X");
                } else {
                    System.out.printf("%-5s", sign47Location.getStep());
                }
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    private Map<Integer, Sign47Location> fillLocationDownUp(Sign47Location startSign47Location,
            Sign47Location[][] sign47LocationsParam) {
        Map<Integer, Sign47Location> canSetRowLocationMap = new HashMap<>();
        int baseStep = startSign47Location.getStep();
        int upStep = baseStep;
        int downStep = baseStep;
        // 向下
        for (int i = startSign47Location.x + 1; i < this.mapInfo.size; i++) {
            if (isLocationHasLandmine(sign47LocationsParam[i][startSign47Location.y])) {
                break;
            }
            downStep++;
            setLocationStep(i, startSign47Location.y, downStep, sign47LocationsParam);

            canSetRowLocationMap.put(i, sign47LocationsParam[i][startSign47Location.y]);
        }
        // 向上
        for (int i = startSign47Location.x - 1; i >= 0; i--) {
            if (isLocationHasLandmine(sign47LocationsParam[i][startSign47Location.y])) {
                break;
            }
            upStep++;
            setLocationStep(i, startSign47Location.y, upStep, sign47LocationsParam);

            canSetRowLocationMap.put(i, sign47LocationsParam[i][startSign47Location.y]);
        }
        canSetRowLocationMap.put(startSign47Location.x, startSign47Location);
        return canSetRowLocationMap;
    }

    private void setLocationStep(int x, int y, int step, Sign47Location[][] sign47LocationsParam) {
        int currentStep = sign47LocationsParam[x][y].getStep();
        if (currentStep == 0 || currentStep > step) {
            sign47LocationsParam[x][y].setStep(step);
        }
    }

    private Map<Integer, Sign47Location> fillLocationLeftRight(Sign47Location startSign47Location,
            Sign47Location[][] sign47LocationsParam) {
        Map<Integer, Sign47Location> canSetColLocationMap = new HashMap<>();
        int baseStep = startSign47Location.getStep();
        int rightStep = baseStep;
        int leftStep = baseStep;
        // 向右
        for (int i = startSign47Location.y + 1; i < this.mapInfo.size; i++) {
            if (isLocationHasLandmine(sign47LocationsParam[startSign47Location.x][i])) {
                break;
            }
            rightStep++;
            setLocationStep(startSign47Location.x, i, rightStep, sign47LocationsParam);

            canSetColLocationMap.put(i, sign47LocationsParam[startSign47Location.x][i]);
        }
        // 向左
        for (int i = startSign47Location.y - 1; i >= 0; i--) {
            if (isLocationHasLandmine(sign47LocationsParam[startSign47Location.x][i])) {
                break;
            }
            leftStep++;
            setLocationStep(startSign47Location.x, i, leftStep, sign47LocationsParam);

            canSetColLocationMap.put(i, sign47LocationsParam[startSign47Location.x][i]);
        }
        canSetColLocationMap.put(startSign47Location.y, startSign47Location);
        return canSetColLocationMap;
    }

    public Sign47Location getSign47Location(int x, int y, Sign47Location[][] sign47LocationsParam) {
        return sign47LocationsParam[x][y];
    }

    public List<FightRobotBaseInfo> getAroundEnemies(Location currentLocation) {
        List<FightRobotBaseInfo> aroundEnemies = new ArrayList<>();
        List<Location> aroundLocations = getAroundLocations(currentLocation);
        for (FightRobotBaseInfo otherFightRobotBaseInfo : otherFightRobotBaseInfos) {

            for (Location aroundLocation : aroundLocations) {
                if (otherFightRobotBaseInfo.currentLocation.equals(aroundLocation)) {
                    aroundEnemies.add(otherFightRobotBaseInfo);
                }
            }
        }
        return aroundEnemies;
    }

    public List<BloodBag> getAroundBloodBags(Location currentLocation) {
        List<BloodBag> aroundBloodBags = new ArrayList<>();
        List<Location> aroundLocations = getAroundLocations(currentLocation);
        for (BloodBag bloodBag : this.bloodBags) {

            for (Location aroundLocation : aroundLocations) {
                if (bloodBag.location.equals(aroundLocation)) {
                    aroundBloodBags.add(bloodBag);
                }
            }
        }
        return aroundBloodBags;
    }

    public List<Location> getSafeAroundLocations(Location locationParam) {
        Location tmpLocation = locationParam;
        List<Location> locations = new ArrayList<>();
        List<Location> aroundLocations = getAroundLocations(tmpLocation);
        for (Location location : aroundLocations) {
            if (isLocationDangerous(location)) {
                continue;
            }
            locations.add(location);
        }
        return locations;
    }

    /**
     * 可以互干
     *
     * @param location
     * @return
     */
    private boolean canAttackGamble(Location location) {
        FightRobotBaseInfo enemy = this.otherFightRobotBaseInfos.get(0);
        if (isAroundHasEnemies(location) && isAttackEachOtherWinByLessOne(null, enemy) && !isLocationHasLandmine(location)) {
            return true;
        }
        return false;
    }

    public List<Location> getNoLandMineAroundLocations(Location locationParam) {
        Location tmpLocation = locationParam;
        List<Location> locations = new ArrayList<>();
        List<Location> aroundLocations = getAroundLocations(tmpLocation);
        for (Location location : aroundLocations) {
            if (isLocationHasLandmine(location)) {
                continue;
            }
            locations.add(location);
        }
        return locations;
    }

    public Sign47CommonMoveAction getMoveToTargetAction(Location targetLocation) {

        if (targetLocation == null) {
            return null;
        }
        // if(isLocationHasLandmine(targetLocation)) {
        // System.err.println("LANDMINE");
        // }
        // if(isAroundHasEnemies(targetLocation)) {
        // System.err.println("ENEMIES AROUND");
        // }
        Location currentLocation = this.currentRobotLocation;

        MoveActionCommandEnum moveActionCommandEnum;
        if (targetLocation.x < currentLocation.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_TOP;
        } else if (targetLocation.x > currentLocation.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_DOWN;
        } else if (targetLocation.y < currentLocation.y) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_LEFT;
        } else {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_RIGHT;
        }
        Sign47CommonMoveAction action = new Sign47CommonMoveAction(moveActionCommandEnum);
        return action;
    }

    public NearestWayInfo getNearestBloodBagLocationWay() {
        NearestWayInfo nearestWayInfo = null;
        List<Location> locations = new ArrayList<>();
        if (isEmpty(bloodBags)) {
            return null;
        }
        // for (BloodBag bloodBag : bloodBags) {
        // locations.add(bloodBag.location);
        // }

        BloodBag bestBloodBagToTrace = getBestBloodBagToTrace();
        if (bestBloodBagToTrace == null) {
            return null;
        }
        locations.add(bestBloodBagToTrace.location);
        // Map<Location, NearestWayInfo> nearestWays =
        // this.getNearestWays(this.currentRobotLocation, locations);
        // Iterator<Map.Entry<Location, NearestWayInfo>> iterator =
        // nearestWays.entrySet().iterator();
        // int minStep = Integer.MAX_VALUE;
        // while (iterator.hasNext()) {
        // Map.Entry<Location, NearestWayInfo> next = iterator.next();
        // NearestWayInfo nearestWayInfo1 = next.getValue();
        // if (nearestWayInfo1.getMinStep() == 0 ||
        // isEmpty(nearestWayInfo1.getNextLocationsForTarget())) {
        // continue;
        // }
        // if (minStep > nearestWayInfo1.getMinStep()) {
        // minStep = nearestWayInfo1.getMinStep();
        // nearestWayInfo = nearestWayInfo1;
        // }
        // }

        List<NearestWayInfo> nearestWayInfos = getNearestWayInfos(this.currentRobotLocation, locations);
        if (isEmpty(nearestWayInfos)) {
            return null;
        }
        nearestWayInfo = nearestWayInfos.get(0);

        return nearestWayInfo;
    }

    private BloodBag getBestBloodBagToTrace() {
        // List<BloodTrace> bloodTraces = new ArrayList<>();
        final Sign47Location[][] sign47Locations = generateSign47Locations(this.mapInfo.size);
        List<FightRobotBaseInfo> robots = new ArrayList<>();
        robots.addAll(this.otherFightRobotBaseInfos);
        robots.add(this.currentFightRobotBaseInfo);
        Map<BloodBag, BloodTrace> bloodBagBloodTraceMap = new HashMap<>();
        BloodTrace myBestBloodTrace = null;
        for (FightRobotBaseInfo robot : robots) {
            initSign47Locations(robot.currentLocation, null, sign47Locations);
            int minStep = Integer.MAX_VALUE;
            BloodBag targetBlood = null;
            for (BloodBag bloodBag : bloodBags) {
                BloodTrace bloodTrace = bloodBagBloodTraceMap.get(bloodBag);
                if (bloodTrace == null) {
                    bloodTrace = new BloodTrace();
                    bloodTrace.setBloodBag(bloodBag);
                    bloodBagBloodTraceMap.put(bloodBag, bloodTrace);
                }

                int disStep = disStepBetween(robot.currentLocation, bloodBag.location, sign47Locations);
                if (disStep != -1 && minStep > disStep) {
                    minStep = disStep;
                    targetBlood = bloodBag;
                }
            }
            if (targetBlood == null) {
                continue;
            }
            BloodTrace bloodTrace = bloodBagBloodTraceMap.get(targetBlood);

            if (isMyOwn(robot)) {
                myBestBloodTrace = bloodTrace;
                continue;
            }
            if (bloodTrace.getMinStep() > minStep) {
                bloodTrace.setMinStep(minStep);
            }
            bloodTrace.addRobotNum();
            // bloodTraces.add(bloodTrace);

        }
        if (bloodBagBloodTraceMap.isEmpty()) {
            return null;
        }
        if (myBestBloodTrace == null) {
            return null;
        }

        initSign47Locations(this.currentRobotLocation, null, sign47Locations);
        // printLocation(this.currentRobotLocation, null, sign47Locations);
        BloodBag myBloodBag = myBestBloodTrace.getBloodBag();
        int disStepBetweenBloodBag = disStepBetween(this.currentRobotLocation, myBloodBag.location, sign47Locations);

        if (disStepBetweenBloodBag != -1 && disStepBetweenBloodBag <= myBestBloodTrace.getMinStep()) {
            return myBloodBag;
        }

        List<BloodTrace> bloodTraces = new ArrayList<>(bloodBagBloodTraceMap.values());
        // for (BloodTrace bloodTrace:bloodTraces) {
        // if(bloodTrace.getRobotNum() == 0) {
        // int dis = disStepBetween(bloodTrace.getBloodBag().location,
        // this.currentRobotLocation, sign47Locations);
        // bloodTrace.setMinStep(dis);
        // }
        // }

        Collections.sort(bloodTraces, new Comparator<BloodTrace>() {
            @Override
            public int compare(BloodTrace o1, BloodTrace o2) {
                int re = o1.getRobotNum() - o2.getRobotNum();
                if (re != 0) {
                    return re;
                }
                int dis1 = disStepBetween(RobotContext.this.currentRobotLocation, o1.getBloodBag().location,
                        sign47Locations);
                if (dis1 == -1) {
                    dis1 = Integer.MAX_VALUE;
                }
                int dis2 = disStepBetween(RobotContext.this.currentRobotLocation, o2.getBloodBag().location,
                        sign47Locations);

                return dis1 - dis2;
            }
        });

        if (isEmpty(bloodTraces)) {
            return null;
        }
        int maxBlood = Integer.MIN_VALUE;
        BloodTrace bestT = null;
//        int minRobotNum = bloodTraces.get(0).getRobotNum();
//        for (BloodTrace bloodTrace : bloodTraces) {
//            if (bloodTrace.getRobotNum() == minRobotNum) {
//                List<Location> aroundNBloodBags = getAroundNBloodBags(bloodTrace.getBloodBag().location, 5);
//                if (maxBlood < aroundNBloodBags.size()) {
//                    maxBlood = aroundNBloodBags.size();
//                    bestT = bloodTrace;
//                }
//            }
//        }
        List<BloodTrace> best = new ArrayList<>();
        for (BloodTrace bloodTrace : bloodTraces) {
            if(bloodTrace.getRobotNum() == 0) {
                best.add(bloodTrace);
                continue;
            }
            int step = disStepBetween(this.currentRobotLocation, bloodTrace.getBloodBag().location, sign47Locations);
            if(step == -1) {
                continue;
            }
            if(step < bloodTrace.getMinStep()) {
                best.add(bloodTrace);
            }
        }
        if(!isEmpty(best)) {
            for (BloodTrace bloodTrace : best) {
                List<Location> aroundNBloodBags = getAroundNBloodBags(bloodTrace.getBloodBag().location, 5);
                if (maxBlood < aroundNBloodBags.size()) {
                    maxBlood = aroundNBloodBags.size();
                    bestT = bloodTrace;
                }
            }
        }
        if(bestT == null) {
            bestT = bloodTraces.get(0);
        }

        return bestT.getBloodBag();
    }

    private List<NearestWayInfo> getNearestWayInfos(Location startLocation, List<Location> targetLocations) {
        List<NearestWayInfo> nearestWayInfo = new ArrayList<>();
        Map<Location, NearestWayInfo> nearestWays = this.getNearestWays(startLocation, targetLocations);
        Iterator<Map.Entry<Location, NearestWayInfo>> iterator = nearestWays.entrySet().iterator();
        int minStep = Integer.MAX_VALUE;
        while (iterator.hasNext()) {
            Map.Entry<Location, NearestWayInfo> next = iterator.next();
            NearestWayInfo nearestWayInfo1 = next.getValue();
            if (nearestWayInfo1.getMinStep() == 0 || isEmpty(nearestWayInfo1.getNextLocationsForTarget())) {
                continue;
            }
            if (minStep > nearestWayInfo1.getMinStep()) {
                minStep = nearestWayInfo1.getMinStep();
                nearestWayInfo.clear();
                nearestWayInfo.add(nearestWayInfo1);
            } else if (minStep == nearestWayInfo1.getMinStep()) {
                nearestWayInfo.add(nearestWayInfo1);
            }
        }
        return nearestWayInfo;
    }

    private boolean isMyOwn(FightRobotBaseInfo fightRobotBaseInfo) {
        if (fightRobotBaseInfo.currentLocation.equals(currentRobotLocation)
                && currentFightRobotBaseInfo.name.equals(fightRobotBaseInfo.name)) {
            return true;
        }
        return false;
    }

    private boolean isLocationDangerousForBlood(Location location, Location bloodLocation) {

        if (isLocationHasLandmine(location)) {
            return true;
        }
        if (isAroundHasEnemies(location)) {
            List<FightRobotBaseInfo> aroundEnemies = getAroundEnemies(location);
            FightRobotBaseInfo maxBlood = getMaxBlood(aroundEnemies);
            if ((disBetween(this.currentRobotLocation, bloodLocation) == 1)) {
                if(getArrivableEnemies(this.currentRobotLocation).size() == 1 && isAttackEachOtherWinByLessOne(1, maxBlood)) {
                    return false;
                }

            }
            return true;

        }
//        if (!safeFightWithRobotProperly(location)) {
//            return true;
//        }

        return false;
    }

    private boolean isLocationDangerous(Location location) {

        if (isLocationHasLandmine(location)) {
            return true;
        }
        if (isAroundHasEnemies(location)) {
            List<FightRobotBaseInfo> aroundEnemies = getAroundEnemies(location);
            if (aroundEnemies.size() > 1) {
                return true;
            }
//            if (this.currentFightRobotBaseInfo.bloodNum >= 12) {
//                return false;
//            }
            return true;

        }
        if (!safeFightWithRobotProperly(location)) {
            return true;
        }

        return false;
    }

    private boolean safeFightWithRobotProperly(Location enemyLocation) {

        List<FightRobotBaseInfo> enemies = this.getEnemies(enemyLocation);
        if (isEmpty(enemies)) {
            return true;
        }
        if (getArrivableEnemies(this.currentRobotLocation).size() == 1) {
            if (isAttackEachOtherWin()) {
                return true;
            } else {
                return false;
            }
        }
//        if (countBlackRobot(enemies) > 0) {
//            FightRobotBaseInfo maxBloodRobot = getMaxBlood(enemies);
//            if (canFight(maxBloodRobot)) {
//                return true;
//            }
//            return false;
//        }
        if (this.currentFightRobotBaseInfo.bloodNum <= MIN_BLOOD) {
            return false;
        }
        return true;
    }

    private boolean canFight(FightRobotBaseInfo enemy) {
        return  canFightParamed(enemy, 5, 15);
    }

    private boolean canFightParamed(FightRobotBaseInfo enemy, int bloodMore, int lessBloodNum) {
        if (this.currentFightRobotBaseInfo.bloodNum >= enemy.bloodNum + bloodMore
                || this.currentFightRobotBaseInfo.bloodNum >= lessBloodNum) {
            return true;
        }
        return false;
    }

    public FightRobotBaseInfo getMaxBlood(List<FightRobotBaseInfo> robots) {
        int max = Integer.MIN_VALUE;
        FightRobotBaseInfo maxBloodRobot = null;
        for (FightRobotBaseInfo robot : robots) {
            if (robot.bloodNum > max) {
                max = robot.bloodNum;
                maxBloodRobot = robot;
            }
        }
        return maxBloodRobot;
    }

    // 有问题
    private boolean isAroundHasEnemies(Location location) {
        List<FightRobotBaseInfo> aroundEnemies = getAroundEnemies(location);
        if (isEmpty(aroundEnemies)) {
            return false;
        }

        for (FightRobotBaseInfo aroundEnemy : aroundEnemies) {
            if (!aroundEnemy.currentLocation.equals(this.currentRobotLocation)) {
                return true;
            }
        }

        return false;
    }

    private boolean isLocationHasLandmine(Location location) {
        ElementList elementList = locationElementListMap.get(location);

        for (AbstractElement element : elementList.elements) {
            if (element.elementType == ElementTypeEnum.LANDMINE) {
                return true;
            }
        }
        return false;
    }

    private List<Location> getAroundLocations(Location currentLocation) {
        List<Location> locations = new ArrayList<>();
        if (currentLocation.x > 0) {
            Location locationTop = new Location(currentLocation.x - 1, currentLocation.y);
            locations.add(locationTop);
        }
        if (currentLocation.x < this.mapInfo.size - 1) {
            Location locationDown = new Location(currentLocation.x + 1, currentLocation.y);
            locations.add(locationDown);
        }
        if (currentLocation.y > 0) {
            Location locationLeft = new Location(currentLocation.x, currentLocation.y - 1);
            locations.add(locationLeft);
        }
        if (currentLocation.y < this.mapInfo.size - 1) {
            Location locationRight = new Location(currentLocation.x, currentLocation.y + 1);
            locations.add(locationRight);
        }
        return locations;
    }

    private List<Location> getAroundNBloodBags(Location currentLocation, int n) {
        List<Location> locations = new ArrayList<>();
        List<Location> aroundNLocations = getAroundNLocations(currentLocation, n);
        for (Location location : aroundNLocations) {
            if (isLocationHasBloodBag(location)) {
                locations.add(location);
            }
        }
        return locations;
    }

    private boolean isLocationHasBloodBag(Location location) {
        ElementList elementList = locationElementListMap.get(location);

        for (AbstractElement element : elementList.elements) {
            if (element.elementType == ElementTypeEnum.BLOOD_BAG) {
                return true;
            }
        }
        return false;
    }

    private List<Location> getAroundNLocations(Location currentLocation, int n) {
        List<Location> locations = new ArrayList<>();
        int xStart = currentLocation.x - n;
        if (xStart < 0) {
            xStart = 0;
        }
        int xEnd = currentLocation.x + n;
        if (xEnd > this.mapInfo.size) {
            xEnd = this.mapInfo.size;
        }
        int yStart = currentLocation.x - n;
        if (yStart < 0) {
            yStart = 0;
        }
        int yEnd = currentLocation.x + n;
        if (yEnd > this.mapInfo.size) {
            yEnd = this.mapInfo.size;
        }
        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                locations.add(new Location(i, j));
            }
        }
        return locations;
    }

    public CommonMoveAction bestMoveActionForAttack() {
        Location currentRobotLocation = this.getCurrentRobotLocation();
        List<FightRobotBaseInfo> aroundEnemies = this.getAroundEnemies(currentRobotLocation);

        if (!isEmpty(aroundEnemies)) {
            // attack

            List<Location> targetLocations = new ArrayList<>();
            for (FightRobotBaseInfo aroundEnemy : aroundEnemies) {
                targetLocations.add(aroundEnemy.currentLocation);
            }
            if (!isEmpty(targetLocations)) {
                Location bestLocation = determineBestLocationForAttack(targetLocations);
                if (bestLocation != null) {
                    List<FightRobotBaseInfo> myAttackRoundRobots = this.getEnemies(bestLocation);
                    this.robotAttackedInfo.setMyAttackRoundRobots(myAttackRoundRobots);
                }
                return this.getMoveToTargetAction(bestLocation);
            }
        }

        return null;

    }

    /**
     * @param targetLocations
     * @return
     */
    private Location determineBestLocationForBlood(List<Location> targetLocations, Location targetBloodLoc) {

        List<Location> safeLocations = new ArrayList<>();
        for (Location targetLocation : targetLocations) {
            Location bloodLoc = targetBloodLoc;
            if (bloodLoc == null) {
                bloodLoc = targetLocation;
            }
            if (!isLocationDangerousForBlood(targetLocation, bloodLoc)) {
                safeLocations.add(targetLocation);
            }
        }
        if (isEmpty(safeLocations)) {
            return null;
        }
        return determineBestLocationOfSafe(safeLocations);

    }

    /**
     * @param enemyLocations
     * @return
     */
    private Location determineBestLocationForAttack(List<Location> enemyLocations) {

        if(isWildWarLastOneEnemy()) {
            return enemyLocations.get(0);
        }

        List<Location> safeLocations = new ArrayList<>();
        for (Location targetLocation : enemyLocations) {
            if (!isLocationDangerous(targetLocation)) {
                safeLocations.add(targetLocation);
            }
        }
        if (isEmpty(safeLocations)) {
            return null;
        }
        return determineBestLocationOfSafe(safeLocations);

    }

    /**
     * @param targetLocations
     * @return
     */
    private Location determineBestLocation(List<Location> targetLocations) {

        List<Location> safeLocations = new ArrayList<>();
        for (Location targetLocation : targetLocations) {
            if (!isLocationDangerous(targetLocation)) {
                safeLocations.add(targetLocation);
            }
        }
        if (isEmpty(safeLocations)) {
            return null;
        }
        return determineBestLocationOfSafe(safeLocations);

    }

    public CommonMoveAction bestMoveActionForBlood() {

        List<BloodBag> aroundBloodBags = this.getAroundBloodBags(this.currentRobotLocation);

        if (!isEmpty(aroundBloodBags)) {
            List<Location> targetLocations = new ArrayList<>();
            for (BloodBag bloodBag : aroundBloodBags) {

                targetLocations.add(bloodBag.location);
            }
            if (!isEmpty(targetLocations)) {
                Location bestLocation = determineBestLocationForBlood(targetLocations, null);
                return this.getMoveToTargetAction(bestLocation);
            }
        }
        NearestWayInfo nearestBloodBagLocationWay = getNearestBloodBagLocationWay();
        if (nearestBloodBagLocationWay == null) {
            return null;
        }
        List<Location> nextLocationsForTarget = nearestBloodBagLocationWay.getNextLocationsForTarget();
        if (!isEmpty(nextLocationsForTarget)) {
            return this.getMoveToTargetAction(determineBestLocationForBlood(nextLocationsForTarget,
                    nearestBloodBagLocationWay.getTargetLocation()));
        }

        return null;

    }

    private <T> boolean isEmpty(Collection<T> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    public CommonMoveAction bestMoveActionForForced() {
        List<Location> safeAroundLocations = this.getSafeAroundLocations(this.currentRobotLocation);
        if (!isEmpty(safeAroundLocations)) {
            Location bestLocation = determineBestLocationOfSafe(safeAroundLocations);
            return this.getMoveToTargetAction(bestLocation);
        }
        // System.err.println("no safe Location");
        List<Location> aroundLocations = this.getAroundLocations(this.currentRobotLocation);
        return this.getMoveToTargetAction(getAroundLessDangerousLocation(aroundLocations));

    }

    private Location determineBestLocationOfSafe(List<Location> safeNextLocations) {
        // 去往空间大的地方
        List<Location> moreSpaceLocations = getMoreSpaceLocation(safeNextLocations);

        // 去往血包多的地方
        Location moreBloodLocation = getMoreBloodLocation(moreSpaceLocations);
        if (moreBloodLocation != null) {
            return moreBloodLocation;
        }

        // 去往人多的地方
        Location moreRobotLocation = getMoreRobotLocation(moreSpaceLocations);
        if (moreRobotLocation != null) {
            return moreRobotLocation;
        }
        return getRandomLocation(safeNextLocations);
    }

    private List<Location> getMoreSpaceLocation(List<Location> safeNextLocations) {
        List<Location> moreSpaceLocations = new ArrayList<>();
        int maxSpace = Integer.MIN_VALUE;
        for (Location location : safeNextLocations) {
            int safeSize = getSafeAroundLocations(location).size();
            if (maxSpace < safeSize) {
                maxSpace = safeSize;
                moreSpaceLocations.clear();
                moreSpaceLocations.add(location);
            } else if (safeSize == maxSpace) {
                moreSpaceLocations.add(location);
            }
        }

        return moreSpaceLocations;

    }

    /**
     * 待补充,去往血包多的地方
     *
     * @param safeNextLocations
     * @return
     */
    private Location getMoreBloodLocation(List<Location> safeNextLocations) {
        if (isEmpty(this.bloodBags)) {
            return null;
        }
        return getRandomLocation(safeNextLocations);
    }

    /**
     * 待补充,去往人多的地方
     *
     * @param safeNextLocations
     * @return
     */
    private Location getMoreRobotLocation(List<Location> safeNextLocations) {
        return getRandomLocation(safeNextLocations);
    }

    private Location getAroundLessDangerousLocation(List<Location> aroundDangerousLocations) {
        Location targetLocation = null;
        List<Location> bestLocations = new ArrayList<>();

        int locationDanger = Integer.MAX_VALUE;
        for (Location location : aroundDangerousLocations) {
            int enemiesSize = getAroundEnemies(location).size();
            int bloodSize = getAroundBloodBags(location).size();
            int tmpDanger = enemiesSize * 2 - bloodSize;
            if (isLocationHasLandmine(location)) {
                if (getAroundLandMineNum(location) > 1) {
                    tmpDanger += 3;
                } else {
                    tmpDanger++;
                }
            }
            List<FightRobotBaseInfo> enemies = getEnemies(location);
            if (enemies.size() > 0) {
                tmpDanger--;
                int blackRobotNum = countBlackRobot(enemies);
                tmpDanger += 2 * blackRobotNum;
            }

            if (locationDanger > tmpDanger) {
                locationDanger = tmpDanger;
                bestLocations.clear();
                bestLocations.add(location);
                // targetLocation = location;
            } else if (locationDanger == tmpDanger) {
                bestLocations.add(location);
            }
            //
        }

        targetLocation = getRandomLocation(bestLocations);
        return targetLocation;
    }

    private int getAroundLandMineNum(Location location) {
        int num = 0;
        List<Location> aroundLocations = this.getAroundLocations(location);
        for (Location aroundLocation : aroundLocations) {
            if (isLocationHasLandmine(aroundLocation)) {
                num++;
            }
        }
        return num;
    }

    private List<Location> getAroundFullLocations(Location currentLocation) {
        List<Location> locations = new ArrayList<>();
        if (currentLocation.x > 0) {
            Location locationTop = new Location(currentLocation.x - 1, currentLocation.y);
            locations.add(locationTop);
            if (currentLocation.y > 0) {
                Location locationLeft = new Location(currentLocation.x - 1, currentLocation.y - 1);
                locations.add(locationLeft);
            }
            if (currentLocation.y < this.mapInfo.size - 1) {
                Location locationRight = new Location(currentLocation.x - 1, currentLocation.y + 1);
                locations.add(locationRight);
            }
        }
        if (currentLocation.x < this.mapInfo.size - 1) {
            Location locationDown = new Location(currentLocation.x + 1, currentLocation.y);
            locations.add(locationDown);
            if (currentLocation.y > 0) {
                Location locationLeft = new Location(currentLocation.x + 1, currentLocation.y - 1);
                locations.add(locationLeft);
            }
            if (currentLocation.y < this.mapInfo.size - 1) {
                Location locationRight = new Location(currentLocation.x + 1, currentLocation.y + 1);
                locations.add(locationRight);
            }
        }
        if (currentLocation.y > 0) {
            Location locationLeft = new Location(currentLocation.x, currentLocation.y - 1);
            locations.add(locationLeft);
        }
        if (currentLocation.y < this.mapInfo.size - 1) {
            Location locationRight = new Location(currentLocation.x, currentLocation.y + 1);
            locations.add(locationRight);
        }
        return locations;
    }

    private int countBlackRobot(List<FightRobotBaseInfo> enemies) {
        int count = 0;
        for (FightRobotBaseInfo enemy : enemies) {
            if (this.robotAttackedInfo.isBlackRobot(enemy)) {
                count++;
            }
        }
        return count;
    }

    public boolean isLocationHasEnemy(Location location) {
        return getEnemies(location).size() > 0;
    }

    public List<FightRobotBaseInfo> getEnemies(Location location) {
        List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<>();
        ElementList elementList = this.locationElementListMap.get(location);
        for (AbstractElement element : elementList.elements) {
            if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                if (!isMyOwn(fightRobotBaseInfo)) {
                    fightRobotBaseInfos.add(fightRobotBaseInfo);
                }
            }
        }
        return fightRobotBaseInfos;
    }

    private Location getRandomLocation(List<Location> locations) {
        Random random = new Random(System.currentTimeMillis());
        return locations.get(Math.abs(random.nextInt()) % locations.size());
    }

    /**
     * 待补充，去往人多的地方
     *
     * @return
     */
    public Sign47CommonMoveAction bestMoveActionForBlock() {
        ActionTypeEnum actionTypeEnum = ActionTypeEnum.INEFFECTIVE_TRACE;
        List<Location> safeAroundLocations = new ArrayList<>();
        // this.currentFightRobotBaseInfo.name.equals("孟孟") &&
        List<FightRobotBaseInfo> arrivableEnemies = getArrivableEnemies(this.currentRobotLocation);
        if (arrivableEnemies.size() == 1) {

            if(isWildWarLastOneEnemy()) {
                Sign47Location[][] sign47Locations = generateSign47Locations(this.mapInfo.size);
                initSign47Locations(this.currentRobotLocation,null, sign47Locations);
                if(needToBreak(sign47Locations)) {
//                    FightRobotBaseInfo lastEnemy = arrivableEnemies.get(0);
//                if(lastEnemy.attackRobotNum < this.currentFightRobotBaseInfo.attackRobotNum) {
//                    safeAroundLocations.add(lastEnemy.currentLocation);
//                }
                    List<Location> aroundLocations = this.getAroundLocations(this.currentRobotLocation);
                    for (Location location : aroundLocations) {
                        if (isAroundHasEnemies(location) && !isLocationHasLandmine(location)) {
                            safeAroundLocations.add(location);
                        }
                    }
                }

            } else {
                List<Location> aroundLocations = this.getAroundLocations(this.currentRobotLocation);
                for (Location location : aroundLocations) {
                    if (canAttackGamble(location)) {
                        safeAroundLocations.add(location);
                    }
                }
            }
        } else {
            List<Location> aroundLocations = this.getAroundLocations(this.currentRobotLocation);
            for (Location location : aroundLocations) {
//                if (canAttackGambleForWildWar(location) && this.robotAttackedInfo.isFightRound()) {
                if ( this.robotAttackedInfo.isFightRound() &&isAroundHasEnemies(location) && !isLocationHasLandmine(location)) {
                    safeAroundLocations.add(location);
                }
            }
        }
        if (!isEmpty(safeAroundLocations)) {
            Sign47CommonMoveAction moveToTargetAction = this
                    .getMoveToTargetAction(determineBestLocationOfSafe(safeAroundLocations));
            moveToTargetAction.setActionTypeEnum(ActionTypeEnum.GAMBLE);
            return moveToTargetAction;
        }

        safeAroundLocations.addAll(this.getSafeAroundLocations(this.currentRobotLocation));
        if (isEmpty(safeAroundLocations)) {
            return null;
        }

        // if (this.currentFightRobotBaseInfo.name.equals("孟孟")) {
        List<Location> traceLocations = getLocationsToTrace();
        // if (!isEmpty(enemiesToTrace)) {
        // for (FightRobotBaseInfo enemyToTrace : enemiesToTrace) {
        // LogUtil.log(this.currentFightRobotBaseInfo.name, "tracing " +
        // enemyToTrace.name);
        // traceLocations.add(enemyToTrace.currentLocation);
        // }
        // }

        // if (this.otherFightRobotBaseInfos.size() == 1) {
        // Location tracedEnemyLocation =
        // this.otherFightRobotBaseInfos.get(0).currentLocation;
        // traceLocations.add(tracedEnemyLocation);
        // } else {
        // List<FightRobotBaseInfo> enemiesToTrace = getLocationsToTrace();
        // if (!isEmpty(enemiesToTrace)) {
        // for (FightRobotBaseInfo enemyToTrace : enemiesToTrace) {
        // traceLocations.add(enemyToTrace.currentLocation);
        // }
        // }
        // }

        if (!isEmpty(traceLocations)) {
            Collection<NearestWayInfo> nearestWayInfos = getNearestWays(this.currentRobotLocation, traceLocations)
                    .values();
            if (!isEmpty(nearestWayInfos)) {
                List<NextTraceLocation> nextTraceLocations = new ArrayList<>();
                for (NearestWayInfo nearestWayInfo : nearestWayInfos) {
                    nextTraceLocations.addAll(toNextTraceLocations(nearestWayInfo.getNextLocationsForTarget(),
                            nearestWayInfo.getTargetLocation()));
                }
                if (!isEmpty(nextTraceLocations)) {
                    List<NextTraceLocation> safeNextLocations = new ArrayList<>();
                    for (NextTraceLocation nextTraceLocation : nextTraceLocations) {
                        if (safeAroundLocations.contains(nextTraceLocation)) {
                            safeNextLocations.add(nextTraceLocation);
                        }
                    }
                    if (!isEmpty(safeNextLocations)) {
                        List<NextTraceLocation> bestNextTraceLocationsOfSafe = getBestNextLocationsOfSafeForTrace(
                                safeNextLocations);
                        List<Location> bestLocationsOfSafe = toLocation(bestNextTraceLocationsOfSafe);
                        Location bestLocationOfSafe = determineBestLocationOfSafe(bestLocationsOfSafe);
                        Sign47CommonMoveAction moveToTargetAction = this.getMoveToTargetAction(bestLocationOfSafe);

                        NextTraceLocation bestNextTraceLocation = (NextTraceLocation) bestLocationOfSafe;
                        Location traceTargetLocation = bestNextTraceLocation.getTargetLocation();
                        if(!isLocationHasEnemy(traceTargetLocation)) {
                            moveToTargetAction.setActionTypeEnum(ActionTypeEnum.INEFFECTIVE_TRACE);
                        } else if(this.robotAttackedInfo.containsTraceLoc(traceTargetLocation)) {
                            moveToTargetAction.setActionTypeEnum(ActionTypeEnum.INEFFECTIVE_TRACE);
                        } else {
                            int disBetweenCurrentAndTrace = disStepBetween(this.currentRobotLocation, traceTargetLocation,
                                    RobotContext.locations);
                            int disBetweenNextAndTrace = disStepBetween(bestLocationOfSafe, traceTargetLocation,
                                    RobotContext.locations);
                            if (disBetweenNextAndTrace > disBetweenCurrentAndTrace) {
                                moveToTargetAction.setActionTypeEnum(ActionTypeEnum.INEFFECTIVE_TRACE);
                            } else if (disBetweenNextAndTrace < disBetweenCurrentAndTrace) {
                                moveToTargetAction.setActionTypeEnum(ActionTypeEnum.EFFECTIVE_TRACE);
                            }
                        }
                        this.robotAttackedInfo.addTracedLocation(traceTargetLocation);

                        return moveToTargetAction;
                    } else {
                        actionTypeEnum = ActionTypeEnum.INEFFECTIVE_TRACE;
                    }
                }

            }
        }
        // }
        Sign47CommonMoveAction moveToTargetAction = this
                .getMoveToTargetAction(determineBestLocationOfSafe(safeAroundLocations));
        moveToTargetAction.setActionTypeEnum(actionTypeEnum);
        return moveToTargetAction;
    }

    private boolean canAttackGambleForWildWar(Location location) {
        List<FightRobotBaseInfo> aroundEnemies = getAroundEnemies(location);
        FightRobotBaseInfo maxBlood = getMaxBlood(aroundEnemies);
        if (isAroundHasEnemies(location) && isAttackEachOtherWinByLessOne(null,maxBlood) && !isLocationHasLandmine(location)) {

            if(canFightParamed(maxBlood, 3, MIN_BLOOD)) {
                return true;
            }
        }
        return false;
    }

    public boolean canAttackGambleForWildWarWithEnemy(FightRobotBaseInfo enemy, int bloodMore, int lessBloodNum) {
        if(isAttackEachOtherWinByLessOne(null,enemy) && canFightParamed(enemy, bloodMore, lessBloodNum)) {
            return true;
        }
        return false;
    }

    private List<FightRobotBaseInfo> getArrivableEnemies(Location startLoc) {
        List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<>();
        Sign47Location[][] sign47Locations = generateSign47Locations(this.mapInfo.size);
        initSign47Locations(startLoc, null, sign47Locations);
        for (FightRobotBaseInfo enemy : this.otherFightRobotBaseInfos) {
            Sign47Location sign47Location = getSign47Location(enemy.currentLocation.x, enemy.currentLocation.y,
                    sign47Locations);
            if (sign47Location.getStep() > 0) {
                fightRobotBaseInfos.add(enemy);
            }
        }
        return fightRobotBaseInfos;
    }

    private boolean canTrace(FightRobotBaseInfo enemy) {
        int dis = disBetween(this.currentRobotLocation, enemy.currentLocation);
        if (dis % 2 == 1) {
            return true;
        }
        return false;
    }

    private int disStepBetween(Location startLocation, Location endLocation, Sign47Location[][] locations) {
        Sign47Location startSign47Location = getSign47Location(startLocation.x, startLocation.y, locations);
        Sign47Location endSign47Location = getSign47Location(endLocation.x, endLocation.y, locations);

        if (endSign47Location.getStep() == 0) {
            return -1;
        }
        return Math.abs(endSign47Location.getStep() - startSign47Location.getStep());
    }

    private List<Location> toLocation(List<NextTraceLocation> nextTraceLocations) {
        List<Location> locations = new ArrayList<>();
        for (Location nextTraceLocation : nextTraceLocations) {
            locations.add(nextTraceLocation);
        }
        return locations;
    }

    private List<NextTraceLocation> toNextTraceLocations(List<Location> nextLocations, Location targetLocation) {
        List<NextTraceLocation> nextTraceLocations = new ArrayList<>();
        if (isEmpty(nextLocations)) {
            return nextTraceLocations;
        }
        for (Location nextLocation : nextLocations) {
            if (nextLocation == null) {
                continue;
            }

            NextTraceLocation nextTraceLocation = new NextTraceLocation(nextLocation);
            nextTraceLocation.setTargetLocation(targetLocation);
            nextTraceLocations.add(nextTraceLocation);
        }
        return nextTraceLocations;
    }

    private List<NextTraceLocation> getBestNextLocationsOfSafeForTrace(List<NextTraceLocation> safeNextLocations) {
        List<NextTraceLocation> bestNextLocations = new ArrayList();
        for (NextTraceLocation safeNextLocation : safeNextLocations) {
            if (isBestForTrace(safeNextLocation, safeNextLocation.getTargetLocation())) {
                bestNextLocations.add(safeNextLocation);
            }
        }
        if (bestNextLocations.isEmpty()) {
            return safeNextLocations;
        }
        return bestNextLocations;
    }

    private boolean isBestForTrace(Location nextLocation, Location traceLocation) {
        if (Math.abs(nextLocation.x - traceLocation.x) == 1 && Math.abs(nextLocation.y - traceLocation.y) == 1) {
            return true;
        }
        return false;
    }

    private List<Location> getLocationsToTrace() {
        List<FightRobotBaseInfo> arrivableEnemies = getArrivableEnemies(this.currentRobotLocation);


        if(arrivableEnemies.size() >= 2) {
            if (!this.robotAttackedInfo.isFightRound()) {
                List<Location> centerLocations = centerLocations();
                return centerLocations;
            }
        }





//        List<FightRobotBaseInfo> protectLayerEnemies = getProtectLayerEnemies(this.currentRobotLocation);
//        if (isEmpty(protectLayerEnemies)) {
//            List<Location> centerLocations = centerLocations();
//            return centerLocations;
//        }
//
//        if (isEmpty(traceLocations)) {
//            List<Location> centerLocations = centerLocations();
//            return centerLocations;
//        }
//        boolean test = true;
//        if (test) {
//            return traceLocations;
//        }
//
//        // int random100 = getRandom100();
//        if (this.robotAttackedInfo.getBloodRound() % 3 == 0) {
//            List<Location> centerLocations = centerLocations();
//            return centerLocations;
//        }

        initSign47Locations(this.currentRobotLocation, null, RobotContext.locations);
        String traceRobotName = this.robotAttackedInfo.getTraceRobotName();
        FightRobotBaseInfo traceRobot = null;
        if (traceRobotName != null) {
            for (FightRobotBaseInfo enemy : this.otherFightRobotBaseInfos) {
                if (enemy.name.equals(traceRobotName)) {
                    traceRobot = enemy;
                }
            }
            if (traceRobot != null && canArrive(traceRobot.currentLocation, RobotContext.locations)) {
                return Arrays.asList(traceRobot.currentLocation);
            }
        }

//        List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<>();
//        ScoreOrderInfo scoreOrderInfo = this.scoreOrder();
//        List<FightRobotScoreInfo> fightRobotScoreInfos = scoreOrderInfo.getFightRobotScoreInfos();
//        List<Location> canArriveEnemyLocs = new ArrayList<>();
//        for (FightRobotScoreInfo enemyScoreInfo : fightRobotScoreInfos) {
//            FightRobotBaseInfo robot = enemyScoreInfo.getFightRobotBaseInfo();
//            if (canArrive(robot.currentLocation, RobotContext.locations)) {
//                canArriveEnemyLocs.add(robot.currentLocation);
//                if (canTrace(robot) && !isMyOwn(robot) && !this.currentRobotLocation.equals(robot.currentLocation)) {
//                    fightRobotBaseInfos.add(robot);
//                }
//            }
//
//        }
//        if (!isEmpty(fightRobotBaseInfos)) {
//            FightRobotBaseInfo thisRoundTraceRobot = fightRobotBaseInfos.get(fightRobotBaseInfos.size() - 1);
//            this.robotAttackedInfo.setTraceRobotName(thisRoundTraceRobot.name);
//            return Arrays.asList(thisRoundTraceRobot.currentLocation);
//        }

        if (isEmpty(arrivableEnemies)) {
            List<Location> centerLocations = centerLocations();
            return centerLocations;
        }
        final int enemySize = arrivableEnemies.size();
        Collections.sort(arrivableEnemies, new Comparator<FightRobotBaseInfo>() {
            @Override
            public int compare(FightRobotBaseInfo o1, FightRobotBaseInfo o2) {
//                if(enemySize <= 3) {
//                    return o2.bloodNum - o1.bloodNum;
//                } else {
                return o1.bloodNum - o2.bloodNum;
//                }
//                return o1.attackRobotNum - o2.attackRobotNum;

            }
        });
        List<Location> canArriveEnemyLocs = new ArrayList<>();
        List<FightRobotBaseInfo> traceRobots = new ArrayList<>();
        List<FightRobotBaseInfo> traceableWhiteRobots = new ArrayList<>();
        for (FightRobotBaseInfo fightRobotBaseInfo : arrivableEnemies) {
            if (canTrace(fightRobotBaseInfo)) {
                traceRobots.add(fightRobotBaseInfo);
                if(!this.robotAttackedInfo.isBlackRobot(fightRobotBaseInfo)) {
                    traceableWhiteRobots.add(fightRobotBaseInfo);
                }
            }
            canArriveEnemyLocs.add(fightRobotBaseInfo.currentLocation);
        }
        if (!isEmpty(traceableWhiteRobots)) {
            FightRobotBaseInfo thisRoundTraceRobot = traceableWhiteRobots.get(0);
            this.robotAttackedInfo.setTraceRobotName(thisRoundTraceRobot.name);
            return Arrays.asList(thisRoundTraceRobot.currentLocation);
        }

        if (!isEmpty(traceRobots)) {
            FightRobotBaseInfo thisRoundTraceRobot = traceRobots.get(0);
            this.robotAttackedInfo.setTraceRobotName(thisRoundTraceRobot.name);
            return Arrays.asList(thisRoundTraceRobot.currentLocation);
        }

        // return canArriveEnemyLocs;
        return Arrays.asList(canArriveEnemyLocs.get(0));
    }

    private List<Location> centerLocations() {
        Set<Location> locationSet = new HashSet<>();
        int half = this.mapInfo.size / 2;
        Location halfLoc = new Location(half, half);
        Sign47Location[][] sign47Locations = generateSign47Locations(this.mapInfo.size);
        initSign47Locations(this.currentRobotLocation, null, sign47Locations);
        int n = 1;
        while (locationSet.isEmpty()) {

            List<Location> tmp = getAroundNLocations(halfLoc, n);
            for (Location location : tmp) {
                    if (!isLocationHasLandmine(location) && !location.equals(this.currentRobotLocation)
                            && canArrive(location, sign47Locations)) {
                        locationSet.add(location);
                    }
            }
            n++;

        }

        return new ArrayList<>(locationSet);

    }

    private boolean canArrive(Location traceLoc, Sign47Location[][] sign47Locations) {
        int disStepBetween = disStepBetween(this.currentRobotLocation, traceLoc, sign47Locations);
        if (disStepBetween > 0 && disStepBetween < 15) {
            return true;
        }
        return false;

    }

    private List<FightRobotBaseInfo> getProtectLayerEnemies(Location location) {
        List<FightRobotBaseInfo> retEnemies = new ArrayList<>();
        for (FightRobotBaseInfo enemy : this.otherFightRobotBaseInfos) {
            if (disBetween(enemy.currentLocation, this.currentRobotLocation) == 3) {
                retEnemies.add(enemy);
            }
        }
        return retEnemies;
    }

    private int disBetween(Location start, Location end) {
        return Math.abs(end.x - start.x) + Math.abs(end.y - start.y);
    }

    public CommonMoveAction bestMoveActionWhenProperlyAttacked() {

        List<FightRobotBaseInfo> enemies = getEnemies(this.currentRobotLocation);
        if (!isEmpty(enemies)) {

            for (FightRobotBaseInfo enemy : enemies) {
                if (this.robotAttackedInfo.isAttackByMeLastRound(enemy)) {
                    addBlackRobotName(enemy);
                }
            }
            this.robotAttackedInfo.setAttackedLocation(this.currentRobotLocation);
            return null;
        }
        return null;
    }

    private void addBlackRobotName(FightRobotBaseInfo blackRobot) {

        if (this.otherFightRobotBaseInfos.size() == 1) {
            if (isAttackEachOtherWin()) {
                return;
            }
        }

        if (getRandom100() < 60) {
            this.robotAttackedInfo.addBlackRobot(blackRobot.name);
        }
    }

    private int getRandom100() {
        Random random = new Random(System.currentTimeMillis());
        int nextInt = random.nextInt(100);
        return nextInt;
    }

    private boolean isAttackEachOtherWin() {
        if ((this.currentFightRobotBaseInfo.bloodNum + 1) / 2 >= (this.otherFightRobotBaseInfos.get(0).bloodNum + 1)
                / 2) {
            return true;
        }
        return false;
    }

    private boolean isAttackEachOtherWinByLessOne(Integer increase,FightRobotBaseInfo enemy) {
        int inc = 0;
        if (increase != null) {
            inc = increase;
        }
        if ((this.currentFightRobotBaseInfo.bloodNum + inc - 1)
                / 2 >= (enemy.bloodNum + 1) / 2) {
            return true;
        }
        return false;
    }

    private boolean isAttacked() {
        if (isLocationHasEnemy(this.currentRobotLocation)) {
            return true;
        }
        return false;
    }

    private ScoreOrderInfo scoreOrder() {
        ScoreOrderInfo scoreOrderInfo = new ScoreOrderInfo();

        List<FightRobotScoreInfo> fightRobotScoreInfos = new ArrayList<>();

        List<FightRobotBaseInfo> allRobotInfos = new ArrayList<>();
        allRobotInfos.addAll(this.otherFightRobotBaseInfos);
        allRobotInfos.add(this.currentFightRobotBaseInfo);
        Collections.sort(allRobotInfos, new Comparator<FightRobotBaseInfo>() {
            @Override
            public int compare(FightRobotBaseInfo o1, FightRobotBaseInfo o2) {
                return o2.bloodNum - o1.bloodNum;
            }
        });

        int size = allRobotInfos.size();

        for (int i = 0; i < size; i++) {
            FightRobotBaseInfo fightRobotBaseInfo = allRobotInfos.get(i);
            FightRobotBaseInfo nextFightRobotBaseInfo = null;
            if (i + 1 < size) {
                nextFightRobotBaseInfo = allRobotInfos.get(i + 1);
            }
            FightRobotScoreInfo fightRobotScoreInfo = new FightRobotScoreInfo();
            fightRobotScoreInfo.setFightRobotBaseInfo(fightRobotBaseInfo);
            fightRobotScoreInfo.setScore(calScore(fightRobotBaseInfo, nextFightRobotBaseInfo, i));
            fightRobotScoreInfos.add(fightRobotScoreInfo);
        }
        // Collections.sort(fightRobotScoreInfos, new
        // Comparator<FightRobotScoreInfo>() {
        // @Override
        // public int compare(FightRobotScoreInfo o1, FightRobotScoreInfo o2) {
        // return o2.getScore() - o1.getScore();
        // }
        // });

        scoreOrderInfo.setFightRobotScoreInfos(fightRobotScoreInfos);

        for (int i = 0; i < fightRobotScoreInfos.size(); i++) {
            FightRobotScoreInfo fightRobotScoreInfo = fightRobotScoreInfos.get(i);

            if (isMyOwn(fightRobotScoreInfo.getFightRobotBaseInfo())) {
                scoreOrderInfo.setCurrentOrder(i);
                scoreOrderInfo.setCurrentScore(fightRobotScoreInfo.getScore());
            }
        }

        int maxScoreHis = this.robotAttackedInfo.getMaxScore();
        int maxScoreThisRound = fightRobotScoreInfos.get(0).getScore();
        if (maxScoreThisRound > maxScoreHis) {
            this.robotAttackedInfo.setMaxScore(maxScoreThisRound);
            scoreOrderInfo.setMaxScore(maxScoreThisRound);
        } else {
            scoreOrderInfo.setMaxScore(maxScoreHis);
        }

        return scoreOrderInfo;

    }

    /**
     * @param fightRobotBaseInfo
     * @param nextFightRobotBaseInfo
     * @param rank from 0
     * @return
     */
    private int calScore(FightRobotBaseInfo fightRobotBaseInfo, FightRobotBaseInfo nextFightRobotBaseInfo, int rank) {
        int nextBloodNum = 0;
        if (nextFightRobotBaseInfo != null) {
            nextBloodNum = nextFightRobotBaseInfo.bloodNum;
        }
        int robotInitSize = this.robotAttackedInfo.getRobotInitSize();

        int leftBloodNum = 0;
        if (rank == 0) {
            leftBloodNum = fightRobotBaseInfo.bloodNum - nextBloodNum;
        }

        int score = (robotInitSize - rank) * 2 + leftBloodNum + fightRobotBaseInfo.getBloodBagNum
                + fightRobotBaseInfo.attackRobotNum * 2 - fightRobotBaseInfo.stepOnLandmineNum
                - fightRobotBaseInfo.exceptionNum * 2;
        return score;
    }

    public boolean isWildWarLastOneEnemy() {
        if(this.mapInfo.size == 20 && this.otherFightRobotBaseInfos.size() == 1) {
            return true;
        }
        return false;
    }
}
