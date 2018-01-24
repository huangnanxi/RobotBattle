package com.kxd.code.competition.robot.fight.av;

import com.kxd.code.competition.constants.CommonConstant;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author arthur
 * @author vulcan
 */
public class AVFightRobot extends AbstractFightRobot {

    public AVFightRobot() {
        super("A&V");
    }

    private Boolean solo = null;

    @Override
    public CommonMoveAction getNextAction() {
        CommonMoveAction commonMoveAction = null;
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        List<BloodBag> bloodBagList = new ArrayList<>();
        List<FightRobotBaseInfo> fightRobotBaseInfoList = new ArrayList<>();
        List<Landmine> landmineList = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        bloodBagList.add((BloodBag) element);
                    } else if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        // 不统计自身
                        if (!fightRobotBaseInfo.name.equals(fightRobotSeeEntity.robotBaseInfo.name)) {
                            fightRobotBaseInfoList.add((FightRobotBaseInfo) element);
                        }
                    } else if (ElementTypeEnum.LANDMINE == element.elementType) {
                        landmineList.add((Landmine) element);
                    }
                }
            }
        }
        if (solo == null) {
            solo = fightRobotBaseInfoList.size() <= 1 ? true : false;
        }

        try {
            RuntimeContext context = new RuntimeContext(fightRobotSeeEntity, bloodBagList, fightRobotBaseInfoList, landmineList);
            AVLocation location = new AVLocation(fightRobotSeeEntity.robotBaseInfo.currentLocation);
            MoveActionCommandEnum commandEnum = getNextActionByKillRobot(location, context);
            if (null != commandEnum) {
                commonMoveAction = new CommonMoveAction(commandEnum);
            } else {
                if (bloodBagList.size() > 0) {
                    commonMoveAction = new CommonMoveAction(getNextActionByFindBloodBag(location, context));
                } else if (fightRobotBaseInfoList.size() > 2) {
                    commonMoveAction = new CommonMoveAction(getNextActionByMore2Robot(location, context));
                } else {
                    commonMoveAction = new CommonMoveAction(getNextActionBy2Robot(location, context));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return commonMoveAction;
    }

    private MoveActionCommandEnum getNextActionByKillRobot(AVLocation cur, RuntimeContext context) {
        List<Integer> tryList = buildTryList(cur, context);
        for (int i : tryList) {
            MoveActionCommandEnum commandEnum = CommonConstant.directCodes.get(i);
            Integer nextX = cur.x + CommonConstant.directXY.get(i);
            Integer nextY = cur.y + CommonConstant.directXY.get(i + 1);
            Location nextLocation = new Location(nextX, nextY);
            if (!isCrossLine(nextLocation, context) && !isHasLandmine(nextLocation, context) && !willBeAttacked(nextLocation, context)) {
                ElementList elementList = context.fightRobotSeeEntity.robotGetElementList[nextX][nextY];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        if (fightRobotBaseInfo.bloodNum < 3) {
                            return commandEnum;
                        }
                    }
                }
            }
        }
        ElementList elementList = context.fightRobotSeeEntity.robotGetElementList[cur.x][cur.y];
        for (AbstractElement element : elementList.elements) {
            if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                if (!fightRobotBaseInfo.name.equals(context.fightRobotSeeEntity.robotBaseInfo.name) && fightRobotBaseInfo.bloodNum < 3) {
                    return generateNoLandmineAction(context);
                }
            }
        }
        return null;
    }

    private MoveActionCommandEnum getNextActionBy2Robot(AVLocation cur, RuntimeContext context) {
        FightRobotBaseInfo self = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        FightRobotBaseInfo robot = context.fightRobotBaseInfoList.get(0);
        List<MoveActionCommandEnum> shortestPath = findShortestPathByBFS(cur, new AVLocation(robot.currentLocation), context);
        if (null != shortestPath) {
            // 调整攻击性
            if (canAttack(self.bloodNum, robot.bloodNum, shortestPath.size(), context)) {
                return shortestPath.get(0);
            }
        }
        return generateNoLandmineAction(context);
    }

    private boolean canAttack(int selfBloodNum, int robotBloodNum, int shortestPathSize, RuntimeContext context) {
        if (shortestPathSize % 2 == 1) {
            if ((selfBloodNum % 2 == 1 && (selfBloodNum - robotBloodNum > -2)) || (selfBloodNum % 2 == 0 && selfBloodNum >= robotBloodNum)) {
                return true;
            }
        } else {
            if (selfBloodNum <= 2) {
                return false;
            }
            if ((selfBloodNum % 2 == 1 && selfBloodNum > robotBloodNum) || (selfBloodNum % 2 == 0 && (selfBloodNum - robotBloodNum > 1))) {
                return true;
            }
        }
        return false;
    }

    private MoveActionCommandEnum getNextActionByMore2Robot(AVLocation cur, RuntimeContext context) {
        List<FightRobotBaseInfo> robotList = new ArrayList<>();
        for (FightRobotBaseInfo robot : context.fightRobotBaseInfoList) {
            if (robot.bloodNum <= 2) {
                robotList.add(robot);
            }
        }
        if (robotList.isEmpty()) {
            if (findSelfBloodBagIndex(context) == 1) {
                return getActionByRobotList(cur, context, context.fightRobotBaseInfoList);
            }
            return generateNoLandmineAction(context);
        } else {
            return getActionByRobotList(cur, context, robotList);
        }
    }

    private MoveActionCommandEnum getActionByRobotList(AVLocation cur, RuntimeContext context, List<FightRobotBaseInfo> robotList) {
        List<FindElementSolution<FightRobotBaseInfo>> bestSolutionList = new ArrayList<>();

        FightRobotBaseInfo self = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        for (FightRobotBaseInfo robot : robotList) {
            List<MoveActionCommandEnum> shortestPath = findShortestPathByBFS(cur, new AVLocation(robot.currentLocation), context);
            if (null != shortestPath) {
                FindElementSolution<FightRobotBaseInfo> solution = new FindElementSolution<>();
                solution.t = robot;
                solution.path = shortestPath;
                solution.distance = shortestPath.size();
                if (canAttack(self.bloodNum, robot.bloodNum, shortestPath.size(), context)) {
                    bestSolutionList.add(solution);
                }
            }
        }
        FindElementSolution<FightRobotBaseInfo> solution;
        if (!bestSolutionList.isEmpty()) {
            Collections.sort(bestSolutionList);
            solution = bestSolutionList.get(0);
            optimizeFindElementSolution(solution.path, context);
            return solution.path.get(0);
        } else {
            return generateNoLandmineAction(context);
        }
    }

    private int findSelfBloodBagIndex(RuntimeContext context) {
        FightRobotBaseInfo self = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        int greatCount = 0;
        for (FightRobotBaseInfo enemyRobot : context.fightRobotBaseInfoList) {
            if (enemyRobot.bloodNum >= self.bloodNum) {
                greatCount++;
            }
        }
        return (greatCount + 1);
    }

    private MoveActionCommandEnum getNextActionByFindBloodBag(AVLocation cur, RuntimeContext context) {
        FightRobotBaseInfo self = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        Map<BloodBag, FindElementSolution<BloodBag>> curSolutionMap = new HashMap<>();
        Map<BloodBag, List<FindElementSolution<BloodBag>>> enemyRobotSolutionMap = new HashMap<>();
        List<BloodBag> bloodBagList = context.bloodBagList;
        for (BloodBag bloodBag : bloodBagList) {
            List<FindElementSolution<BloodBag>> solutionList = new ArrayList<>();
            AVLocation avLocation = new AVLocation(bloodBag.location);
            List<MoveActionCommandEnum> curMoveAction = findShortestPathByBFS(cur, avLocation, context);
            // 无可达路径
            if (null == curMoveAction) {
                continue;
            }
            FindElementSolution<BloodBag> solution = new FindElementSolution<>();
            solution.t = bloodBag;
            solution.path = curMoveAction;
            solution.distance = curMoveAction.size();
            solutionList.add(solution);

            initEnemyRobotBloodMap(bloodBag, enemyRobotSolutionMap, context);
            solutionList.addAll(enemyRobotSolutionMap.get(bloodBag));
            Collections.sort(solutionList);
            for (int i = 0; i < solutionList.size(); i++) {
                if (solution.equals(solutionList.get(i))) {
                    solution.rank = i;
                    curSolutionMap.put(bloodBag, solution);
                    break;
                }
            }
        }

        if (!curSolutionMap.isEmpty()) {
            return getBestActionByBloodBag(curSolutionMap, context);
        } else if (self.bloodNum > 1) {
            List<FindElementSolution<BloodBag>> overLandmineSolution = new ArrayList<>();

            for (BloodBag bloodBag : bloodBagList) {
                initEnemyRobotBloodMap(bloodBag, enemyRobotSolutionMap, context);
            }
            for (Landmine landmine : context.landmineList) {
                AVLocation landmineLocation = new AVLocation(landmine.location);
                List<MoveActionCommandEnum> cur2LandmineAction = findShortestPathByBFS(cur, landmineLocation, context);
                if (null == cur2LandmineAction) {
                    continue;
                }
                for (BloodBag bloodBag : bloodBagList) {
                    List<FindElementSolution<BloodBag>> solutionList = new ArrayList<>(enemyRobotSolutionMap.get(bloodBag));
                    AVLocation avLocation = new AVLocation(bloodBag.location);
                    List<MoveActionCommandEnum> curMoveAction = findShortestPathByBFS(landmineLocation, avLocation, context);
                    if (null == curMoveAction) {
                        continue;
                    }
                    cur2LandmineAction.addAll(curMoveAction);
                    FindElementSolution<BloodBag> solution = new FindElementSolution<>();
                    solution.t = bloodBag;
                    solution.path = cur2LandmineAction;
                    solution.distance = cur2LandmineAction.size();
                    solutionList.add(solution);
                    Collections.sort(solutionList);
                    for (int i = 0; i < solutionList.size(); i++) {
                        if (solution.equals(solutionList.get(i))) {
                            solution.rank = i;
                            overLandmineSolution.add(solution);
                            break;
                        }
                    }
                }
            }
            if (!overLandmineSolution.isEmpty()) {
                Collections.sort(overLandmineSolution);
                FindElementSolution<BloodBag> bestSolution = overLandmineSolution.get(0);
                optimizeFindElementSolution(bestSolution.path, context);
                return bestSolution.path.get(0);
            }
        }
        // 所有血包无可达路径
        return getNextActionByMore2Robot(cur, context);
    }

    private void initEnemyRobotBloodMap(BloodBag bloodBag, Map<BloodBag, List<FindElementSolution<BloodBag>>> enemyRobotSolutionMap, RuntimeContext context) {
        enemyRobotSolutionMap.put(bloodBag, new ArrayList<FindElementSolution<BloodBag>>());
        for (FightRobotBaseInfo robot : context.fightRobotBaseInfoList) {
            AVLocation robotLocation = new AVLocation(robot.currentLocation), bloodBagLocation = new AVLocation(bloodBag.location);
            List<MoveActionCommandEnum> robotMoveAction = findShortestPathByBFS(robotLocation, bloodBagLocation, context);
            FindElementSolution<BloodBag> robotSolution = new FindElementSolution<>();
            robotSolution.t = bloodBag;
            robotSolution.path = robotMoveAction;
            robotSolution.distance = (null == robotMoveAction ? Integer.MAX_VALUE : robotMoveAction.size());
            enemyRobotSolutionMap.get(bloodBag).add(robotSolution);
        }
    }

    private MoveActionCommandEnum getBestActionByBloodBag(Map<BloodBag, FindElementSolution<BloodBag>> curSolutionMap, RuntimeContext context) {
        List<FindElementSolution<BloodBag>> findElementSolutions = new ArrayList<>(curSolutionMap.values());
        Collections.sort(findElementSolutions);
        FindElementSolution<BloodBag> bestSolution = findElementSolutions.get(0);
        optimizeFindElementSolution(bestSolution.path, context);
        return bestSolution.path.get(0);
    }

    private void optimizeFindElementSolution(List<MoveActionCommandEnum> path, RuntimeContext context) {
        FightRobotBaseInfo baseInfo = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        MoveActionCommandEnum nextAction = path.get(0);
        Location nextLocation = getNextLocation(baseInfo.currentLocation, nextAction);
        if (willBeAttacked(nextLocation, context)) {
            Set<MoveActionCommandEnum> actionHistory = new HashSet<>(4);
            actionHistory.add(nextAction);
            for (int i = 1; i < path.size(); i++) {
                if (!actionHistory.contains(path.get(i))) {
                    nextLocation = getNextLocation(baseInfo.currentLocation, path.get(i));
                    if (!isCrossLine(nextLocation, context) && !isHasLandmine(nextLocation, context) && !willBeAttacked(nextLocation, context)) {
                        path.set(0, path.get(i));
                        path.set(i, nextAction);
                        return;
                    }
                    actionHistory.add(path.get(i));
                }
            }

            // 需要根据目标位置优化
            List<Integer> tryList = buildTryList(context.fightRobotSeeEntity.robotBaseInfo.currentLocation, context);
            for (int i : tryList) {
                MoveActionCommandEnum moveActionCommand = CommonConstant.directCodes.get(i);
                if (!actionHistory.contains(moveActionCommand)) {
                    nextLocation = getNextLocation(baseInfo.currentLocation, moveActionCommand);
                    if (!isCrossLine(nextLocation, context) && !isHasLandmine(nextLocation, context) && !willBeAttacked(nextLocation, context)) {
                        path.add(0, moveActionCommand);
                        return;
                    }
                }
            }
        }
    }

    private Location getNextLocation(Location cur, MoveActionCommandEnum action) {
        switch (action) {
            case MOVE_TOP:
                return new Location(cur.x - 1, cur.y);
            case MOVE_DOWN:
                return new Location(cur.x + 1, cur.y);
            case MOVE_LEFT:
                return new Location(cur.x, cur.y - 1);
            case MOVE_RIGHT:
                return new Location(cur.x, cur.y + 1);
            default:
                return null;
        }
    }

    private List<MoveActionCommandEnum> findShortestPathByBFS(AVLocation cur, AVLocation target, RuntimeContext context) {
        Map<AVLocation, List<MoveActionCommandEnum>> shortestPathMap = new HashMap<>();
        if (cur.equals(target)) {
            // 被攻击的情况下
            List<MoveActionCommandEnum> shortestPath = new ArrayList<>();
            MoveActionCommandEnum action = generateNoLandmineAction(context);
            shortestPath.add(action);
            shortestPath.add(getOppositeAction(action));
            return shortestPath;
        }

        int size = context.fightRobotSeeEntity.mapInfo.size;
        AVLocation[][] locations = new AVLocation[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                locations[i][j] = new AVLocation(i, j);
            }
        }
        Queue<AVLocation> queue = new LinkedList<>();
        queue.offer(cur);
        shortestPathMap.put(cur, new ArrayList<MoveActionCommandEnum>());
        cur.willBeVisit = true;
        while (!queue.isEmpty()) {
            AVLocation location = queue.poll();
            location.hasBeenVisit = true;
            List<MoveActionCommandEnum> beforeShortestPath = shortestPathMap.get(location);
            List<Integer> tryList = buildTryList(cur, context, target);
            for (int i : tryList) {
                MoveActionCommandEnum commandEnum = CommonConstant.directCodes.get(i);
                Integer nextX = location.x + CommonConstant.directXY.get(i);
                Integer nextY = location.y + CommonConstant.directXY.get(i + 1);
                if (isCrossLine(new Location(nextX, nextY), context)) {
                    continue;
                }
                AVLocation next = locations[nextX][nextY];
                if (next.equals(target)) {
                    List<MoveActionCommandEnum> nextShortestPath = new ArrayList<>(beforeShortestPath);
                    nextShortestPath.add(commandEnum);
                    return nextShortestPath;
                }
                boolean hasLandmine = isHasLandmine(next, context);
                if (!hasLandmine && !next.hasBeenVisit && !next.willBeVisit) {
                    queue.offer(next);
                    next.willBeVisit = true;
                    List<MoveActionCommandEnum> nextShortestPath = new ArrayList<>(beforeShortestPath);
                    nextShortestPath.add(commandEnum);
                    shortestPathMap.put(next, nextShortestPath);
                }
            }
        }
        return null;
    }

    private boolean willBeAttacked(Location location, RuntimeContext context) {
        FightRobotBaseInfo self = (FightRobotBaseInfo) context.fightRobotSeeEntity.robotBaseInfo;
        List<FightRobotBaseInfo> fightRobotBaseInfos = context.fightRobotBaseInfoList;
        boolean willBeAttacked = false;
        int bloodWillAdd = 0;
        ElementList elementList = context.fightRobotSeeEntity.robotGetElementList[location.x][location.y];
        for (AbstractElement element : elementList.elements) {
            if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                bloodWillAdd = 1;
                break;
            }
        }
        int selfBloodNum = self.bloodNum + bloodWillAdd;
        for (FightRobotBaseInfo info : fightRobotBaseInfos) {
            int xLength = Math.abs(location.x - info.currentLocation.x), yLength = Math.abs(location.y - info.currentLocation.y);
            if (((xLength + yLength) == 1 && !canAttack(selfBloodNum, info.bloodNum, 2, context)) || (xLength + yLength == 0 && !canAttack(selfBloodNum, info.bloodNum - 2, 2, context))) {
                willBeAttacked = true;
                break;
            }
        }
        return willBeAttacked;
    }

    private boolean isCrossLine(Location location, RuntimeContext context) {
        FightMapInfo fightMapInfo = (FightMapInfo) context.fightRobotSeeEntity.mapInfo;
        boolean crossLine = false;
        if (location.x >= fightMapInfo.size || location.y >= fightMapInfo.size || location.x < 0 || location.y < 0) {
            crossLine = true;
        }
        return crossLine;
    }

    private Boolean isHasLandmine(Location location, RuntimeContext context) {
        Boolean isHasLandmine = false;
        if (isCrossLine(location, context)) {
            return true;
        }
        FightRobotSeeEntity fightRobotSeeEntity = context.fightRobotSeeEntity;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        for (AbstractElement element : elementLists[location.x][location.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                isHasLandmine = true;
                break;
            }
        }
        return isHasLandmine;
    }

    private MoveActionCommandEnum generateNoLandmineAction(RuntimeContext context) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;

        MoveActionCommandEnum moveActionCommand, worseCommand = null, worsetCommand = null;

        int centerSize = context.fightRobotSeeEntity.mapInfo.size / 2;
        AVLocation cur = new AVLocation(currentLocation), target = new AVLocation(centerSize, centerSize);
        if (!cur.equals(target)) {
            List<MoveActionCommandEnum> toCenterPath = findShortestPathByBFS(cur, target, context);
            if (null != toCenterPath) {
                optimizeFindElementSolution(toCenterPath, context);
                return toCenterPath.get(0);
            }
        }

        List<Integer> tryList = buildTryList(currentLocation, context);
        for (int i : tryList) {
            moveActionCommand = CommonConstant.directCodes.get(i);

            Integer nextX = currentLocation.x + CommonConstant.directXY.get(i);
            Integer nextY = currentLocation.y + CommonConstant.directXY.get(i + 1);
            Location location = new Location(nextX, nextY);
            if (isCrossLine(location, context)) {
                continue;
            }
            if (!isHasLandmine(location, context)) {
                if (!willBeAttacked(location, context)) {
                    return moveActionCommand;
                } else {
                    worsetCommand = moveActionCommand;
                }
            } else {
                worseCommand = moveActionCommand;
            }
        }
        if (worseCommand != null) {
            return worseCommand;
        }
        if (worsetCommand != null) {
            return worsetCommand;
        }

        return null;
    }

    private List<Integer> buildTryList(Location cur, RuntimeContext context, Location ...target) {
        List<Integer> tryList = new ArrayList<>();
        Integer targetX, targetY;
        if (null != target && target.length == 1) {
            targetX = target[0].x;
            targetY = target[0].y;
        } else {
            targetX = context.fightRobotSeeEntity.mapInfo.size / 2;
            targetY = context.fightRobotSeeEntity.mapInfo.size / 2;
        }
        int sizeX = Math.abs(cur.x - targetX), sizeY = Math.abs(cur.y - targetY);
        if (sizeX > sizeY) {
            if (cur.x > targetX) {
                tryList.add(0);
            } else {
                tryList.add(2);
            }
            if (cur.y > targetY) {
                tryList.add(3);
            } else {
                tryList.add(1);
            }
        } else if (sizeX < sizeY) {
            if (cur.y > targetY) {
                tryList.add(3);
            } else {
                tryList.add(1);
            }
            if (cur.x > targetX) {
                tryList.add(0);
            } else {
                tryList.add(2);
            }
        }
        List<Integer> normalList = Arrays.asList(0, 1, 2, 3);
        for (int i : normalList) {
            if (!tryList.contains(i)) {
                tryList.add(i);
            }
        }
        return tryList;
    }

    private MoveActionCommandEnum getOppositeAction(MoveActionCommandEnum action) {
        switch (action) {
            case MOVE_TOP:
                return MoveActionCommandEnum.MOVE_DOWN;
            case MOVE_DOWN:
                return MoveActionCommandEnum.MOVE_TOP;
            case MOVE_LEFT:
                return MoveActionCommandEnum.MOVE_RIGHT;
            case MOVE_RIGHT:
                return MoveActionCommandEnum.MOVE_LEFT;
            default:
                return null;
        }
    }

    private static class RuntimeContext {
        private FightRobotSeeEntity fightRobotSeeEntity;
        private List<BloodBag> bloodBagList;
        private List<FightRobotBaseInfo> fightRobotBaseInfoList;
        private List<Landmine> landmineList;

        RuntimeContext(FightRobotSeeEntity fightRobotSeeEntity, List<BloodBag> bloodBagList, List<FightRobotBaseInfo> fightRobotBaseInfoList, List<Landmine> landmineList) {
            this.fightRobotSeeEntity = fightRobotSeeEntity;
            this.bloodBagList = bloodBagList;
            this.fightRobotBaseInfoList = fightRobotBaseInfoList;
            this.landmineList = landmineList;
            int size = fightRobotSeeEntity.mapInfo.size;
        }
    }
}
