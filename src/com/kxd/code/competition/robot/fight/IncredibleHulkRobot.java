package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.CommonConstant;
import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;

/**
 * Created by seastliu on 2017/12/21.
 */
public class IncredibleHulkRobot extends AbstractFightRobot {

    public IncredibleHulkRobot() {
        super("无敌浩克");
    }

    // 地图上相关只读信息
    private FightRobotSeeEntity fightRobotSeeEntity = null;
    private FightRobotBaseInfo myRobotInfo = null;
    private Location myLocation = null;
    private int mapSize;
    private ElementList[][] elementLists = null;

    // 其他机器人和血包信息
    private List<AbstractElement> otherRobotsInfo = new ArrayList<>();
    private List<AbstractElement> bloodBags = new ArrayList<>();

    // 自定义地图
    private int[][] mark;
    private int[][] tempMark;

    // 最短路径
    private LinkedList<Location> shortPath = new LinkedList<>();

    private List<Location> locationList10 = new ArrayList<>();

    private List<Location> locationList20 = new ArrayList<>();

    private static class Constants {

        private final static List<Location> specialLocationList10 =
                new ArrayList<Location>() {
                    {
                        add(new Location(5,5));
                        add(new Location(3,7));
                        add(new Location(5,5));
                        add(new Location(7,3));
                        add(new Location(5,5));
                    }
                };

        private final static List<Location> specialLocationList20 =
                new ArrayList<Location>() {
                    {
                        add(new Location(10,10));
                        add(new Location(7,13));
                        add(new Location(10,10));
                        add(new Location(13,7));
                        add(new Location(10,10));
                    }
                };
    }

    @Override
    public CommonMoveAction getNextAction() {
        // 初始化参数
        this.initParameters();

        // 重新绘制地图
        this.constructCustomMap();

        MoveActionCommandEnum moveActionCommand = null;
        try {
            // 确认周围是否全为地雷
            moveActionCommand = this.checkAllLandMineAround();
            if (moveActionCommand != null) {
                return new CommonMoveAction(moveActionCommand);
            }
            // 获取下一步移动指令
            moveActionCommand = this.getNextActionCommand();
        } catch (Exception e) {

        } finally {
            if (moveActionCommand == null) {
                moveActionCommand = this.generateRandomAction();
            }
            return new CommonMoveAction(moveActionCommand);
        }
    }

    private void initParameters() {
        // 获取可见信息
        fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        myRobotInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        myLocation = myRobotInfo.currentLocation;
        mapSize = fightRobotSeeEntity.mapInfo.size;
        elementLists = fightRobotSeeEntity.robotGetElementList;

        // 获取地图上其他机器人信息
        otherRobotsInfo = this.getOtherRobotsInfo();
        // 获取地图上血包信息
        bloodBags = this.getAllBloodBagsInfo();
    }

    private MoveActionCommandEnum getNextActionCommand() throws Exception {
        if (otherRobotsInfo.size() == 1) {
            // 只有1人进单人PK模式
            return this.getNextMoveActionBySinglePK();
        } else {
            // 超过1人进行多人PK模式
            return this.getNextMoveActionByMultiplePK();
        }
    }

    private MoveActionCommandEnum getNextMoveActionByMultiplePK() {
        MoveActionCommandEnum moveActionCommand = null;

        if (bloodBags.size() > 0) {
            locationList20.clear();
            for (Location location : Constants.specialLocationList20) {
                locationList20.add(location);
            }

            // 根据血包位置获取移动指令
            moveActionCommand = this.getMoveActionByBloodBagPosition();
            if (moveActionCommand != null) {
                return moveActionCommand;
            }
        }

        // 根据其他机器人位置获取移动指令
        moveActionCommand = this.getMoveActionByOtherRobotPosition();
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        moveActionCommand = this.getMoveActionBySpecialList(locationList20);
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        // 随机行动
        moveActionCommand = this.generateRandomAction();
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        return null;
    }

    private MoveActionCommandEnum getNextMoveActionBySinglePK() {
        MoveActionCommandEnum moveActionCommand = null;
        FightRobotBaseInfo enemyInfo = (FightRobotBaseInfo) otherRobotsInfo.get(0);

        if (bloodBags.size() > 0) {
            locationList10.clear();
            for (Location location : Constants.specialLocationList10) {
                locationList10.add(location);
            }

            moveActionCommand = this.getMoveActionByBloodBagPosition();
            if (moveActionCommand != null) {
                return moveActionCommand;
            }
        }

        List<NextLocationInfo> nextLocationInfoList = this.getEnenyNumAround(myLocation);
        if (nextLocationInfoList.get(0).getTargetNum() > 0) {
            if (myRobotInfo.bloodNum >= enemyInfo.bloodNum ||
                    ((myRobotInfo.bloodNum+1) == enemyInfo.bloodNum && myRobotInfo.bloodNum%2 == 1)) {
                return nextLocationInfoList.get(0).getMoveActionCommand();
            }
        }

        if (myRobotInfo.bloodNum > (enemyInfo.bloodNum + 1) ||
                (myRobotInfo.bloodNum == (enemyInfo.bloodNum+1) && myRobotInfo.bloodNum%2 == 1)) {
            moveActionCommand = this.move2TargetLocation(enemyInfo.currentLocation);
            if (moveActionCommand != null) {
                return moveActionCommand;
            }
        }

        moveActionCommand = this.getMoveActionByOtherRobotPosition();
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        moveActionCommand = this.getMoveActionBySpecialList(locationList10);
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        // 随机行动
        moveActionCommand = this.generateRandomAction();
        if (moveActionCommand != null) {
            return moveActionCommand;
        }

        return null;
    }

    private MoveActionCommandEnum getMoveActionBySpecialList(List<Location> locationList) {
        if (locationList.size() > 0) {
            Iterator locationIterator = locationList.iterator();
            while (locationIterator.hasNext()) {
                Location location = (Location) locationIterator.next();
                // 已经到达指定位置，切换到下一位置
                if (location.equals(myLocation)) {
                    locationIterator.remove();
                    continue;
                }
                // 指定位置是否有异常
                int errorNum = this.hasErrorAtTargetLocation(location);
                if (errorNum < 0) {
                    locationIterator.remove();
                    continue;
                }
                MoveActionCommandEnum moveActionCommand = this.move2TargetLocation(location);
                if (moveActionCommand != null) {
                    return moveActionCommand;
                }
            }
        }
        return null;
    }

    // 根据其他机器人位置，获取下一步移动指令
    private MoveActionCommandEnum getMoveActionByOtherRobotPosition() {
        // 获取我到各个机器人的最短路径，并从小到大排序
        List<Move2ElementShortPathInfo> myRobot2OtherRobotInfoList =
                this.getMyRobot2OtherRobotsInfo();
        for (Move2ElementShortPathInfo myRobot2OtherRobotInfo :
                myRobot2OtherRobotInfoList) {
            LinkedList<Location> shortPath = myRobot2OtherRobotInfo.getShortPath();
            FightRobotBaseInfo enemyInfo =
                    (FightRobotBaseInfo) myRobot2OtherRobotInfo.getElement();

            // 位置为1
            if (shortPath.size() == 1) {
                if (this.isCanAttack(enemyInfo.name) || enemyInfo.bloodNum <= 2) {
                    Location nextLocation = shortPath.removeLast();
                    return this.getMoveActionCommand(myLocation, nextLocation);
                }
                // 逃跑
                Location nextLocation = shortPath.removeLast();
                return this.getMoveActionExceptLocation(nextLocation);
            }

            // 位置为2
            if (shortPath.size() == 2) {
                // 逃跑
                Location nextLocation = shortPath.removeLast();
                return this.getMoveActionExceptLocation(nextLocation);
            }

            if (shortPath.size() % 2 == 1 && isCanAttack(enemyInfo.name)) {
                Location nextLocation = shortPath.removeLast();
                return this.getMoveActionCommand(myLocation, nextLocation);
            }
        }
        return null;
    }

    // 获取除指定位置下一步执行指令
    private MoveActionCommandEnum getMoveActionExceptLocation(Location exceptLocation) {
        for (int i = 0; i < 4; i++) {
            MoveActionCommandEnum moveAction = CommonConstant.directCodes.get(i);
            Integer nextX = myLocation.x + CommonConstant.directXY.get(i);
            Integer nextY = myLocation.y + CommonConstant.directXY.get(i + 1);
            Location nextLocation = new Location(nextX, nextY);
            if (nextLocation.equals(exceptLocation)) {
                continue;
            }

            int errorNum = this.isErrorAtNextLocation(nextLocation);
            if (errorNum == 0) {
                return this.getMoveActionCommand(myLocation, nextLocation);
            }
        }

        return null;
    }

    // 获取我到各个机器人最短路径信息
    private List<Move2ElementShortPathInfo> getMyRobot2OtherRobotsInfo() {
        return this.getTargetLocation2OtherRobotsInfo(myLocation);
    }

    // 获取指定位置到其他机器人最短路径信息
    private List<Move2ElementShortPathInfo> getTargetLocation2OtherRobotsInfo(Location targetLocation) {
        List<Move2ElementShortPathInfo> move2ElementShortPathList = new ArrayList<>();
        for (AbstractElement element : otherRobotsInfo) {
            FightRobotBaseInfo otherRobot = (FightRobotBaseInfo) element;

            LinkedList<Location> shortPath = this.getShortPath2TargetLocation(
                    targetLocation, otherRobot.currentLocation);
            if (shortPath.size() == 0) {
                continue;
            }
            Move2ElementShortPathInfo move2ElementShortPathInfo = new Move2ElementShortPathInfo();
            move2ElementShortPathInfo.setElement(otherRobot);
            move2ElementShortPathInfo.setShortPath(shortPath);

            move2ElementShortPathList.add(move2ElementShortPathInfo);
        }
        Collections.sort(move2ElementShortPathList);
        return move2ElementShortPathList;
    }

    // 根据血包位置，获取下一步移动指令
    private MoveActionCommandEnum getMoveActionByBloodBagPosition() {
        /*// 踩血包过程中避免被人攻击
        List<Move2ElementShortPathInfo> myRobot2OtherRobotInfoList =
                this.getMyRobot2OtherRobotsInfo();
        for (Move2ElementShortPathInfo move2ElementShortPathInfo :
                myRobot2OtherRobotInfoList) {
            LinkedList<Location> shortPath = move2ElementShortPathInfo.getShortPath();
            // 位置为2，逃跑
            if (shortPath.size() == 2) {
                Location nextLocation = shortPath.removeLast();
                return this.getMoveActionExceptLocation(nextLocation);
            }
        }*/

        // 获取我到各个血包的最短路径，并从小到大排序
        List<Move2ElementShortPathInfo> myRobot2BloodBagInfoList = this.getMyRobot2BloodBagsInfo();
        for (Move2ElementShortPathInfo myRobot2BloodBagInfo : myRobot2BloodBagInfoList) {
            BloodBag bloodBag = (BloodBag) myRobot2BloodBagInfo.getElement();
            LinkedList<Location> myRobot2BloodBagShortPath = myRobot2BloodBagInfo.getShortPath();

            List<Move2ElementShortPathInfo> otherRobots2BloodBagInfoList =
                    this.getOtherRobots2BloodBagInfo(bloodBag);
            int nearerRobotNum = 0;
            for (Move2ElementShortPathInfo otherRobot2BloodBagInfo : otherRobots2BloodBagInfoList) {
                LinkedList<Location> otherRobot2BloodBagShortPath = otherRobot2BloodBagInfo.getShortPath();
                if (otherRobot2BloodBagShortPath.size() <= myRobot2BloodBagShortPath.size()) {
                    nearerRobotNum ++;
                }
            }

            if (nearerRobotNum > 1) {
                continue;
            }

            if (nearerRobotNum == 0) {
                // 向最近血包移动
                LinkedList<Location> shortPath = myRobot2BloodBagShortPath;
                Location nextLocation = shortPath.removeLast();
                return getMoveActionCommand(myLocation, nextLocation);
            }
        }

        if (myRobot2BloodBagInfoList.size() > 1) {
            LinkedList<Location> shortPath = myRobot2BloodBagInfoList.get(1).shortPath;
            Location nextLocation = shortPath.removeLast();
            return getMoveActionCommand(myLocation, nextLocation);
        }

        return null;
    }

    // 获取我到各个血包最短路径信息
    private List<Move2ElementShortPathInfo> getMyRobot2BloodBagsInfo() {
        List<Move2ElementShortPathInfo> move2ElementShortPathList = new ArrayList<>();
        for (AbstractElement element : bloodBags) {
            BloodBag bloodBag = (BloodBag)element;
            Location bloodBagLocation = bloodBag.location;

            LinkedList<Location> shortPath = this.getShortPath2TargetLocation(
                    myLocation, bloodBagLocation);
            // 若无法达到血包，则直接过滤
            if (shortPath.size() == 0) {
                continue;
            }
            Move2ElementShortPathInfo move2ElementShortPathInfo = new Move2ElementShortPathInfo();
            move2ElementShortPathInfo.setElement(bloodBag);
            move2ElementShortPathInfo.setShortPath(shortPath);
            move2ElementShortPathList.add(move2ElementShortPathInfo);
        }
        Collections.sort(move2ElementShortPathList);
        return move2ElementShortPathList;
    }

    // 获取其他机器人到指定血包最短路径信息
    private List<Move2ElementShortPathInfo> getOtherRobots2BloodBagInfo(BloodBag bloodBag) {
        List<Move2ElementShortPathInfo> move2ElementShortPathList = new ArrayList<>();
        for (AbstractElement element : otherRobotsInfo) {
            FightRobotBaseInfo otherRobot = (FightRobotBaseInfo) element;
            Location bloodBagLocation = bloodBag.location;

            LinkedList<Location> shortPath = this.getShortPath2TargetLocation(
                    otherRobot.currentLocation, bloodBagLocation);
            if (shortPath.size() == 0) {
                continue;
            }
            Move2ElementShortPathInfo move2ElementShortPathInfo = new Move2ElementShortPathInfo();
            move2ElementShortPathInfo.setElement(otherRobot);
            move2ElementShortPathInfo.setShortPath(shortPath);

            move2ElementShortPathList.add(move2ElementShortPathInfo);
        }
        Collections.sort(move2ElementShortPathList);
        return move2ElementShortPathList;
    }

    // 获取到指定位置的最短路径，并获取下一步移动指令
    private MoveActionCommandEnum move2TargetLocation(Location targetLocation) {
        LinkedList<Location> shortPath = getShortPath2TargetLocation(myLocation, targetLocation);
        Location nextLocation = shortPath.removeLast();
        return getMoveActionCommand(myLocation, nextLocation);
    }

    // 获取从指定位置到指定位置的最短路径
    private LinkedList<Location> getShortPath2TargetLocation(
            Location currentLocation, Location targetLocation) {
        // 对最短路径进行初始化
        shortPath = new LinkedList<>();
        // 移动坐标初始化
        tempMark = new int[mapSize][mapSize];
        for (int i=0; i<mark.length; i++) {
            tempMark[i] = mark[i].clone();
        }

        //当前位置标记为1
        tempMark[currentLocation.x][currentLocation.y] = 1;
        recursionFindTargetElement(new LinkedList<Location>(), currentLocation, targetLocation);

        return shortPath;
    }

    // 递归获取到达血包路径
    private void recursionFindTargetElement(LinkedList<Location> path,
                                            Location beginPos, Location endPos) {
        if (beginPos.equals(endPos)) {
            if (path.size() < shortPath.size() || shortPath.isEmpty()) {
                shortPath = (LinkedList<Location>)path.clone();
                return;
            }
        }
        for (int i = 0; i < 4; i++) {
            Integer nextX = beginPos.x + CommonConstant.directXY.get(i);
            Integer nextY = beginPos.y + CommonConstant.directXY.get(i + 1);
            Location nextLocation = new Location(nextX, nextY);
            if (isCanGo(tempMark[beginPos.x][beginPos.y], nextLocation)) {
                tempMark[nextX][nextY] = tempMark[beginPos.x][beginPos.y] + 1;
                path.push(nextLocation);
                recursionFindTargetElement(path, nextLocation, endPos);
                path.pop();
            }
        }
    }

    // 递归获取血包，路径探测
    private boolean isCanGo(int value, Location nextLocation) {
        if (this.isCrossArray(nextLocation) ||
                tempMark[nextLocation.x][nextLocation.y] == -1) {
            return false;
        } else if (tempMark[nextLocation.x][nextLocation.y] == 0) {
            return true;
        } else {
            return value+1 < tempMark[nextLocation.x][nextLocation.y];
        }
    }

    // 根据地图上地雷信息，重新绘制自定义地图
    private void constructCustomMap() {
        mark = new int[mapSize][mapSize];
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                // 默认为0
                mark[i][j] = 0;
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.LANDMINE == element.elementType) {
                        // 若为地雷，标识为-1
                        mark[i][j] = -1;
                    }
                }
            }
        }
    }

    // 获取地图上所有的血包
    private List<AbstractElement> getAllBloodBagsInfo() {
        List<AbstractElement> bloodBags = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        bloodBags.add(element);
                    }
                }
            }
        }
        return bloodBags;
    }

    // 获取地图上除自身外的机器人信息
    private List<AbstractElement> getOtherRobotsInfo() {
        List<AbstractElement> robotsInfo = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                        if (myRobotInfo.name.equals(fightRobotBaseInfo.name)) {
                            continue;
                        }
                        robotsInfo.add(element);
                    }
                }
            }
        }
        return robotsInfo;
    }

    // 获取四周敌人数量
    private List<NextLocationInfo> getEnenyNumAround(Location currentLocation) {
        List<NextLocationInfo> nextLocationInfoList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MoveActionCommandEnum moveActionCommand = CommonConstant.directCodes.get(i);
            Integer nextX = currentLocation.x + CommonConstant.directXY.get(i);
            Integer nextY = currentLocation.y + CommonConstant.directXY.get(i + 1);
            Integer enemyNum = this.getEnenyNumAtTargetLocation(new Location(nextX, nextY));

            NextLocationInfo nextLocationInfo = new NextLocationInfo();
            nextLocationInfo.setMoveActionCommand(moveActionCommand);
            nextLocationInfo.setTargetNum(enemyNum);
            nextLocationInfoList.add(nextLocationInfo);
        }

        Collections.sort(nextLocationInfoList);

        return nextLocationInfoList;
    }

    // 校验四周是否全是地雷或越界，全是则踩一个地雷
    private MoveActionCommandEnum checkAllLandMineAround() {
        List<NextLocationInfo> nextLocationInfoList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MoveActionCommandEnum moveActionCommand = CommonConstant.directCodes.get(i);
            Integer nextX = myLocation.x + CommonConstant.directXY.get(i);
            Integer nextY = myLocation.y + CommonConstant.directXY.get(i + 1);
            int errorNum = this.hasErrorAtTargetLocation(new Location(nextX, nextY));

            NextLocationInfo nextLocationInfo = new NextLocationInfo();
            nextLocationInfo.setMoveActionCommand(moveActionCommand);
            nextLocationInfo.setTargetNum(errorNum);
            nextLocationInfoList.add(nextLocationInfo);
        }

        Collections.sort(nextLocationInfoList);

        int totalErrorNum = 0;
        for (NextLocationInfo nextLocationInfo : nextLocationInfoList) {
            if (nextLocationInfo.getTargetNum() < 0) {
                totalErrorNum ++;
            }
        }
        if (totalErrorNum == 4 || totalErrorNum == 3) {
            return nextLocationInfoList.get(0).getMoveActionCommand();
        }
        return null;
    }

    private int isErrorAtNextLocation(Location nextLocation) {
        int errorNum = this.hasErrorAtTargetLocation(nextLocation);
        if (errorNum < 0) {
            return errorNum;
        }

        List<Move2ElementShortPathInfo> targetLocation2OtherRobotInfoList=
                this.getTargetLocation2OtherRobotsInfo(nextLocation);
        for (Move2ElementShortPathInfo targetLocation2OtherRobotInfo :
                targetLocation2OtherRobotInfoList) {
            if (targetLocation2OtherRobotInfo.getShortPath().size() == 1) {
                return -3;
            }
        }

        return 0;
    }

    // 获取指定位置是否有异常
    private int hasErrorAtTargetLocation(Location targetLocation) {
        if (this.isCrossArray(targetLocation)) {
            return -2;
        }
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        for (AbstractElement element : elementLists[targetLocation.x][targetLocation.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                return -1;
            }
        }

        return 0;
    }

    // 指定位置敌人数量
    private Integer getEnenyNumAtTargetLocation(Location targetLocation) {
        if (this.isCrossArray(targetLocation)) {
            return -1;
        }

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        List<AbstractElement> enemyList = this.getEnemyInfoAtTargetLocation(targetLocation);
        if (enemyList.size() > 0) {
            // 该位置上有敌人，则不可能再有血包和地雷
            return enemyList.size();
        }
        return -2;
    }

    // 指定位置是否有敌人
    private List<AbstractElement> getEnemyInfoAtTargetLocation(Location targetLocation) {
        List<AbstractElement> elementList = new ArrayList<>();
        for (AbstractElement element :
                elementLists[targetLocation.x][targetLocation.y].elements) {
            if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                if (myRobotInfo.name.equals(fightRobotBaseInfo.name)) {
                    continue;
                }
                elementList.add(fightRobotBaseInfo);
            }
        }
        return elementList;
    }

    // 是否越界
    private boolean isCrossArray(Location nextLocation) {
        if (nextLocation.x >= mapSize || nextLocation.y >= mapSize
                || nextLocation.x < 0 || nextLocation.y < 0) {
            return true;
        }
        return false;
    }

    // 获取移动到下一步移动指令
    private MoveActionCommandEnum getMoveActionCommand(
            Location currentLocation, Location nextLocation) {
        int sourceX = currentLocation.x;
        int sourceY = currentLocation.y;
        int targetX = nextLocation.x;
        int targetY = nextLocation.y;

        MoveActionCommandEnum moveAction = null;
        for (int i = 0; i < 4; i++) {
            int nextX = sourceX + CommonConstant.directXY.get(i);
            int nextY = sourceY + CommonConstant.directXY.get(i + 1);
            if (targetX == nextX && targetY == nextY) {
                moveAction = CommonConstant.directCodes.get(i);
                break;
            }
        }
        return moveAction;
    }

    private boolean isCanAttack(String robotName) {
        if (otherRobotsInfo.size() >= 7 && myRobotInfo.bloodNum >= 15) {
            return true;
        }

        if (otherRobotsInfo.size() >= 5 && myRobotInfo.bloodNum >= 11) {
            return true;
        }

        if (otherRobotsInfo.size() >=3 && myRobotInfo.bloodNum >= 7) {
            return true;
        }

        if (myRobotInfo.bloodNum <=5) {
            return false;
        }
        return false;
    }

    // 随机移动
    private MoveActionCommandEnum generateRandomAction() {
        Random random = new Random(System.currentTimeMillis());
        MoveActionCommandEnum moveAction = null;

        int tryNum = 0;
        while (true) {
            tryNum++;

            int index = Math.abs(random.nextInt()) % 4;
            moveAction = CommonConstant.directCodes.get(index);

            Integer nextX = myLocation.x + CommonConstant.directXY.get(index);
            Integer nextY = myLocation.y + CommonConstant.directXY.get(index + 1);
            if (this.isErrorAtNextLocation(new Location(nextX, nextY)) == 0 || tryNum == 10) {
                break;
            }
        }

        return moveAction;
    }

    // 下一步移动指令对应目标数量，支持从大到小排序
    class NextLocationInfo implements Comparable<NextLocationInfo> {
        private MoveActionCommandEnum moveActionCommand;

        private Integer targetNum;

        public MoveActionCommandEnum getMoveActionCommand() {
            return moveActionCommand;
        }

        public void setMoveActionCommand(MoveActionCommandEnum moveActionCommand) {
            this.moveActionCommand = moveActionCommand;
        }

        public Integer getTargetNum() {
            return targetNum;
        }

        public void setTargetNum(Integer targetNum) {
            this.targetNum = targetNum;
        }

        @Override
        public int compareTo(NextLocationInfo o) {
            return o.targetNum - this.targetNum;
        }
    }

    // 到达指定元素的最短路径，支持按最短路径从小到大排序
    class Move2ElementShortPathInfo implements Comparable<Move2ElementShortPathInfo> {

        private AbstractElement element;

        private LinkedList<Location> shortPath = new LinkedList<>();

        public AbstractElement getElement() {
            return element;
        }

        public void setElement(AbstractElement element) {
            this.element = element;
        }

        public LinkedList<Location> getShortPath() {
            return shortPath;
        }

        public void setShortPath(LinkedList<Location> shortPath) {
            this.shortPath = shortPath;
        }

        @Override
        public int compareTo(Move2ElementShortPathInfo o) {
            return this.getShortPath().size() - o.getShortPath().size();
        }
    }
}
