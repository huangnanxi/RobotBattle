package com.kxd.code.competition.robot.fight;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by wally on 1/4/18.
 */
public class FightForFree extends AbstractFightRobot {

    private int currentRound;

    private boolean isAttacked;

    private int preBlood;

    private ElementList[][] preElementList;

    private FightRobotBaseInfo robotBaseInfo;

    private Stack<CommonMoveAction> lines;

    private Location target;

    private List<FightRobotBaseInfo> robotBaseInfos = new ArrayList<>();

    private List<String> beforemeRobot;

    private List<String> aftermeRobot;

    public FightForFree() {
        super("飞车");
        isAttacked = false;
        currentRound = 1;
        preBlood = 10;
        beforemeRobot = new ArrayList<>();
        aftermeRobot = new ArrayList<>();
    }

    @Override
    public CommonMoveAction getNextAction() {

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        loadRobots(elementLists);
        if (currentRound == 1) {
            preElementList = new ElementList[currentSeeMazeSituation.mapInfo.size][currentSeeMazeSituation.mapInfo.size];
            scanMap(elementLists);
        }
        if (currentRound == 2) {
            determineOrder(elementLists);
        }

        robotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        isAttacked = robotBaseInfo.bloodNum < preBlood;
        preBlood = robotBaseInfo.bloodNum;
        currentRound = isAttacked ? currentRound : currentRound + 1;
        List<BloodBag> bloodBags = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        bloodBags.add((BloodBag) element);
                    }

                }
            }
        }
        boolean targetExit = false;
        if (target != null) {
            for (AbstractElement element : elementLists[target.x][target.y].elements) {
                if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    targetExit = true;
                }
            }
        }
        if (!targetExit) {
            lines = null;
            target = null;
        }
        if (lines == null || lines.size() == 0) {
            Stack<CommonMoveAction> bestLines = null;
            if (bloodBags.size() == 0) {
                return fightMove(elementLists);
            }
            for (BloodBag bag : bloodBags) {
                Stack<CommonMoveAction> lines = caculateLine(elementLists, bag.location);
                if (bestLines == null) {
                    bestLines = lines;
                }
                if (lines == null)
                    continue;
                if (bestLines.size() > lines.size()) {
                    bestLines = lines;
                    target = bag.location;
                }
            }
            this.lines = bestLines;
            if (lines == null || lines.size() == 0) {

                for (BloodBag bag : bloodBags) {
                    calcBad(elementLists, bag.location);
                }
            }


        }


        if (robotBaseInfo.bloodNum < 5) {


        } else {

        }


        return lines == null ? fightMove(elementLists) : lines.pop();
    }

    private void scanMap(ElementList[][] elementLists) {
        for (int i = 0; i < elementLists.length; i++) {
            System.arraycopy(elementLists[i], 0, preElementList[i], 0, elementLists[i].length);
        }
    }


    private Stack<CommonMoveAction> calcBad(ElementList[][] elementLists, Location target) {


        return null;
    }

//    private NodeInfo chanege(ElementList[][] elementLists, List<NodeInfo> open, List<NodeInfo> closed, Location target) {
//        Location myLocation = robotBaseInfo.currentLocation;
//        Location tryLocation1 = new Location(myLocation.x + 1, myLocation.y);
//        Location tryLocation2 = new Location(myLocation.x - 1, myLocation.y);
//        Location tryLocation3 = new Location(myLocation.x, myLocation.y - 1);
//        Location tryLocation4 = new Location(myLocation.x, myLocation.y + 1);
//
//
//    }
//
//    private boolean isBetween(Location a, Location b, Location target) {
//        boolean isx = false;
//        if (a.x < b.x) {
//            isx = (a.x < target.x) && target.x <= b.x;
//        }else if(a.x>b.x){
//            isx = ()
//        }
//
//    }


    private void loadRobots(ElementList[][] elementLists) {
        List<FightRobotBaseInfo> robotBaseInfos = new ArrayList<>();

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo baseInfo = ((FightRobotBaseInfo) element);
                        if (!baseInfo.name.equals(this.name) && baseInfo.bloodNum > 0)
                            robotBaseInfos.add((FightRobotBaseInfo) element);

                    }

                }
            }
        }
        this.robotBaseInfos = robotBaseInfos;
    }

    private void determineOrder(ElementList[][] elementLists) {
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists[i].length; j++) {
                for (AbstractElement element : elementLists[i][j].elements) {
                    if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        boolean isMove = true;
                        for (AbstractElement preElement : preElementList[i][j].elements) {
                            if (preElement.elementType == ElementTypeEnum.ROBOT_INFO) {
                                FightRobotBaseInfo preFightRobotBaseInfo = (FightRobotBaseInfo) element;
                                if (preFightRobotBaseInfo.name.equals(fightRobotBaseInfo.name)) {
                                    isMove = false;
                                }
                            }
                        }
                        if (isMove)
                            beforemeRobot.add(fightRobotBaseInfo.name);
                        else
                            aftermeRobot.add(fightRobotBaseInfo.name);
                    }
                }
            }
        }
    }

    private CommonMoveAction fightMove(ElementList[][] elementLists) {
        FightRobotBaseInfo target = getWeakest(robotBaseInfos);
        if (target == null) {
            return defendMove(elementLists);
        }
        if (target.bloodNum > robotBaseInfo.bloodNum || isDanger()) {
            return defendMove(elementLists);
        } else {
            Stack<CommonMoveAction> lines = caculateLine(elementLists, target.currentLocation);
            return lines == null ? moveCenter(elementLists) : lines.pop();
        }
    }


    private boolean isDanger() {
        int m = robotBaseInfos.size();
        int order = 1;
        for (FightRobotBaseInfo r : robotBaseInfos) {
            if (r.bloodNum > robotBaseInfo.bloodNum) {
                order++;
            }
        }
        return order > m / 2;

    }


    private boolean isLandMine(ElementList[][] elementLists, Location location) {
        for (AbstractElement element : elementLists[location.x][location.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                return true;
            }
        }
        return false;
    }


    private FightRobotBaseInfo getWeakest(List<FightRobotBaseInfo> robotBaseInfos) {
        FightRobotBaseInfo robotBaseInfo = null;
        boolean findTarget = false;
        for (FightRobotBaseInfo r : robotBaseInfos) {
            if (robotBaseInfo == null) {
                robotBaseInfo = r;
            }
            if (r.bloodNum < robotBaseInfo.bloodNum && r.bloodNum <= robotBaseInfo.bloodNum && twoPoint(r.currentLocation, robotBaseInfo.currentLocation) % 2 == 1) {
                robotBaseInfo = r;
                findTarget = true;
            }
        }
        return findTarget ? robotBaseInfo : null;
    }

    private CommonMoveAction defendMove(ElementList[][] elementLists) {

        Location myLocation = robotBaseInfo.currentLocation;
        List<Location> enemyLocations = getCloseEnemy(elementLists, 3);
        List<CommonMoveAction> canMoves = getCanMove();
        removeInvalid(canMoves, elementLists);
        if (enemyLocations.size() == 0) {
            return moveCenter(elementLists);
        }
        for (Location location : enemyLocations) {
            if (location.x > myLocation.x) {
                removeTarget(canMoves, MoveActionCommandEnum.MOVE_DOWN);
            } else if (location.x < myLocation.x) {
                removeTarget(canMoves, MoveActionCommandEnum.MOVE_TOP);
            }
            if (location.y > myLocation.y) {
                removeTarget(canMoves, MoveActionCommandEnum.MOVE_RIGHT);
            }
            if (location.y < myLocation.y) {
                removeTarget(canMoves, MoveActionCommandEnum.MOVE_LEFT);
            }


        }
        if (canMoves.size() != 0) {
            if (canMoves.size() == 1) {
                return canMoves.get(0);
            }
            int a = canMoves.size();
            Random random = new Random();
            int index = random.nextInt(a);
            return canMoves.get(index);
        }

        return saveMove(elementLists);


    }

    private List<Location> getCloseEnemy(ElementList[][] elementLists, int i) {
        List<Location> enemy = new ArrayList<>();
        for (FightRobotBaseInfo f : robotBaseInfos) {
            if (twoPoint(f.currentLocation, robotBaseInfo.currentLocation) < i) {
                enemy.add(f.currentLocation);
            }
        }
        return enemy;
    }

    private void removeInvalid(List<CommonMoveAction> actions, ElementList[][] elementLists) {
        Location myLocation = robotBaseInfo.currentLocation;
        Location tryLocation1 = new Location(myLocation.x - 1, myLocation.y);
        if (!isinBoard(tryLocation1) || isLandMine(elementLists, tryLocation1)) {
            removeTarget(actions, MoveActionCommandEnum.MOVE_TOP);
        }
        Location tryLocation2 = new Location(myLocation.x + 1, myLocation.y);
        if (!isinBoard(tryLocation2) || isLandMine(elementLists, tryLocation2)) {
            removeTarget(actions, MoveActionCommandEnum.MOVE_DOWN);
        }
        Location tryLocation3 = new Location(myLocation.x, myLocation.y - 1);
        if (!isinBoard(tryLocation3) || isLandMine(elementLists, tryLocation3)) {
            removeTarget(actions, MoveActionCommandEnum.MOVE_LEFT);
        }
        Location tryLocation4 = new Location(myLocation.x, myLocation.y + 1);
        if (!isinBoard(tryLocation4) || isLandMine(elementLists, tryLocation4)) {
            removeTarget(actions, MoveActionCommandEnum.MOVE_RIGHT);
        }
    }

    private List<CommonMoveAction> getCanMove() {
        List<CommonMoveAction> commonMoveActions = new ArrayList<>();
        commonMoveActions.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
        commonMoveActions.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
        commonMoveActions.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
        commonMoveActions.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
        return commonMoveActions;
    }

    private void removeTarget(List<CommonMoveAction> actions, MoveActionCommandEnum moveActionCommandEnum) {
        CommonMoveAction action = null;
        for (CommonMoveAction a : actions) {
            if (a.getActionCommand() == moveActionCommandEnum) {
                action = a;
            }
        }
        if (action != null)
            actions.remove(action);
    }


    public CommonMoveAction saveMove(ElementList[][] elementLists) {
        int x = 0, y = 0, z = 0, v = 0;
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists[i].length; j++) {
                if (i < elementLists.length / 2 && j < elementLists[i].length / 2) {
                    for (AbstractElement element : elementLists[i][j].elements) {
                        if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                            x++;
                        }
                    }
                }
                if (i < elementLists.length / 2 && j >= elementLists[i].length / 2) {
                    for (AbstractElement element : elementLists[i][j].elements) {
                        if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                            y++;
                        }
                    }
                }
                if (i >= elementLists.length / 2 && j < elementLists[i].length / 2) {
                    for (AbstractElement element : elementLists[i][j].elements) {
                        if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                            z++;
                        }
                    }
                }
                if (i > elementLists.length / 2 && j > elementLists[i].length / 2) {
                    for (AbstractElement element : elementLists[i][j].elements) {
                        if (element.elementType == ElementTypeEnum.ROBOT_INFO) {
                            v++;
                        }
                    }
                }
            }
        }

        if (x <= y && x <= z && x <= v) {
            Stack<CommonMoveAction> actions = caculateLine(elementLists, new Location(elementLists.length / 4, elementLists.length / 4));
            if (actions != null) {
                return actions.pop();
            }

        } else if (y <= x && y <= z && y <= v) {
            Stack<CommonMoveAction> actions = caculateLine(elementLists, new Location(elementLists.length / 4, elementLists.length / 4 + elementLists.length / 2));
            if (actions != null) {
                return actions.pop();
            }
        } else if (z <= x && z <= y && z <= v) {
            Stack<CommonMoveAction> actions = caculateLine(elementLists, new Location(elementLists.length / 4 + elementLists.length / 2, elementLists.length / 4));
            if (actions != null) {
                return actions.pop();
            }
        } else if (v <= z && v <= x && v <= y) {
            Stack<CommonMoveAction> actions = caculateLine(elementLists, new Location(elementLists.length / 4 + elementLists.length / 2, elementLists.length / 4 + elementLists.length / 2));
            if (actions != null) {
                return actions.pop();
            }
        }
        return tryMove(elementLists);
    }

    public CommonMoveAction tryMove(ElementList[][] elementLists) {
        Location myLocation = robotBaseInfo.currentLocation;
        Location tryLocation = new Location(myLocation.x, myLocation.y + 1);
        if (isinBoard(tryLocation) && !isLandMine(elementLists, tryLocation)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
        }
        Location tryLocation1 = new Location(myLocation.x, myLocation.y - 1);
        if (isinBoard(tryLocation1) && !isLandMine(elementLists, tryLocation1)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
        }
        Location tryLocation2 = new Location(myLocation.x + 1, myLocation.y);
        if (isinBoard(tryLocation2) && !isLandMine(elementLists, tryLocation2)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
        }
        Location tryLocation3 = new Location(myLocation.x - 1, myLocation.y);
        if (isinBoard(tryLocation3) && !isLandMine(elementLists, tryLocation3)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
        }

        if (isinBoard(tryLocation)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
        }
        if (isinBoard(tryLocation1)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
        }
        if (isinBoard(tryLocation2)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
        }
        if (isinBoard(tryLocation3)) {
            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
        }
        return null;
    }

    private CommonMoveAction moveCenter(ElementList[][] elementLists) {
        Location myLocation = robotBaseInfo.currentLocation;
        Stack<CommonMoveAction> lines = caculateLine(elementLists, new Location(elementLists.length / 2, elementLists.length / 2));
        if (lines == null || lines.size() == 0) {
            CommonMoveAction action = tryMove(elementLists);
            if (action != null) {
                return action;
            }
        }
        return lines.pop();
    }

    private FightRobotBaseInfo getClosest() {
        FightRobotBaseInfo closest = null;
        int a = Integer.MAX_VALUE;
        for (FightRobotBaseInfo r : robotBaseInfos) {
            if (closest == null)
                closest = r;
            int b = twoPoint(r.currentLocation, robotBaseInfo.currentLocation);
            if (a > b) {
                closest = r;
                a = b;
            }
        }
        return closest;
    }

    private Stack<CommonMoveAction> caculateLine(ElementList[][] elementLists, Location target) {
        List<NodeInfo> open = new ArrayList<>();
        List<NodeInfo> closed = new ArrayList<>();
        init(closed, elementLists);

        NodeInfo result = aStar(open, closed, new NodeInfo(robotBaseInfo.currentLocation, null, 0, 0, 0), new NodeInfo(target, null, 0, 0, 0));
        if (result == null) {
            return null;
        } else {
            Stack<CommonMoveAction> moveStack = new Stack<>();
            NodeInfo parent = result.getParent();
            if (parent == null) {
                return null;
            }
            while (!parent.location.equals(robotBaseInfo.currentLocation)) {
                if (parent.location.x < result.location.x)
                    moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
                else if (parent.location.x > result.location.x)
                    moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
                else if (parent.location.y > result.location.y)
                    moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
                else if (parent.location.y < result.location.y) {
                    moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
                }
                result = parent;
                parent = parent.getParent();
            }
            if (parent.location.x < result.location.x)
                moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
            else if (parent.location.x > result.location.x)
                moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
            else if (parent.location.y > result.location.y)
                moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
            else if (parent.location.y < result.location.y) {
                moveStack.push(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
            }

            return moveStack;
        }
    }

    private NodeInfo aStar(List<NodeInfo> open, List<NodeInfo> closed, NodeInfo current, NodeInfo target) {
        if (current.location.equals(target.location)) {
            return current;
        } else {
            Location currentLocation = current.location;
            Location openLocation = new Location(currentLocation.x, currentLocation.y + 1);
            Location openLocation1 = new Location(currentLocation.x, currentLocation.y - 1);
            Location openLocation2 = new Location(currentLocation.x + 1, currentLocation.y);
            Location openLocation3 = new Location(currentLocation.x - 1, currentLocation.y);
            addOpen(open, closed, openLocation, current, target);
            addOpen(open, closed, openLocation1, current, target);
            addOpen(open, closed, openLocation2, current, target);
            addOpen(open, closed, openLocation3, current, target);
            NodeInfo best = findBest(open);
            if (best == null)
                return null;
            open.remove(best);
            closed.add(best);
            return aStar(open, closed, best, target);
        }
    }


    private NodeInfo findBest(List<NodeInfo> infos) {
        NodeInfo best = null;
        if (infos == null || infos.size() == 0) {
            return null;
        }
        for (NodeInfo in : infos) {
            if (best == null) {
                best = in;
            }
            if (best.f > in.f) {
                best = in;
            }
        }
        return best;
    }

    private void addOpen(List<NodeInfo> open, List<NodeInfo> closed, Location openLocation, NodeInfo current, NodeInfo target) {
        if (!containElement(closed, openLocation) && isinBoard(openLocation)) {
            int g = current.g + 1;
            int h = twoPoint(openLocation, target.location);
            int f = g + h;
            if (containElement(open, openLocation)) {
                NodeInfo old = getElement(open, openLocation);
                assert old != null;
                if (old.f > f) {
                    old.setF(f);
                    old.setG(g);
                    old.setParent(current);
                }
            } else {
                open.add(new NodeInfo(openLocation, current, f, g, h));
            }
        }
    }

    private int twoPoint(Location location1, Location location2) {
        return Math.abs(location1.x - location2.x) + Math.abs(location1.y - location2.y);
    }

    private boolean isinBoard(Location location) {
        return !(location.x < 0 || location.y < 0 || location.x >= currentSeeMazeSituation.mapInfo.size || location.y >= currentSeeMazeSituation.mapInfo.size);

    }

    private boolean containElement(List<NodeInfo> nodeInfos, Location location) {
        for (NodeInfo nodeInfo : nodeInfos) {
            if (nodeInfo.location.equals(location))
                return true;
        }
        return false;
    }

    private NodeInfo getElement(List<NodeInfo> infos, Location location) {
        for (NodeInfo n : infos) {
            if (n.location.equals(location))
                return n;
        }
        return null;
    }

    private void init(List<NodeInfo> closed, ElementList[][] elementLists) {
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists[i].length; j++) {
                for (AbstractElement e : elementLists[i][j].elements) {
                    if (e.elementType == ElementTypeEnum.LANDMINE) {
                        closed.add(new NodeInfo(((Landmine) e).location, null, 0, 0, 0));
                    }
                }
            }
        }
    }

    class NodeInfo {

        private NodeInfo parent;

        private Location location;

        private int f;

        private int g;

        private int h;

        public NodeInfo(Location location, NodeInfo parent, int f, int g, int h) {
            this.parent = parent;
            this.f = f;
            this.g = g;
            this.h = h;
            this.location = location;
        }

        public NodeInfo getParent() {
            return parent;
        }

        public void setParent(NodeInfo parent) {
            this.parent = parent;
        }


        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }


        public int getF() {
            return f;
        }

        public void setF(int f) {
            this.f = f;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }
    }


}
