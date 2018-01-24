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
import com.kxd.code.competition.robot.entity.AbstractRobotBaseInfo;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by huangnx on 2017/12/21.
 */
public class SYCFightRobot extends AbstractFightRobot {

    public SYCFightRobot() {
        super("大富翁");
    }

    static final int                 FAKE_MAX            = 10000;

    /**
     * 当前剩余血包信息
     */
    private List<BloodBag>           bloodBagList        = new ArrayList<>();

    /**
     * 当前地雷信息
     */
    private List<Landmine>           landmineList        = new ArrayList<>();

    /**
     * 地图上其他机器人信息
     */
    private List<FightRobotBaseInfo> otherRobotList      = new ArrayList<>();

    /**
     * 可移动的位置list
     */
    private List<Location>           canMoveLocationList = new ArrayList<>();

    /**
     * 我的移动轨迹记录
     */
    private List<Location>           oldLocationList     = new ArrayList<>();

    /**
     * 当前位置
     */
    private Location                 myLocation          = null;

    /**
     * 下一步位置
     */
    private Location                 nextLocation        = null;

    /**
     * 自身血量
     */
    private int                      bloodNum            = 0;

    /**
     * 地图尺寸
     */
    private int                      mapSize             = 0;

    /**
     * 可以移动的步数
     */
    private int                      currentCanStepNum   = 1;

    /**
     * 地图信息
     */
    ElementList[][]                  elementLists;

    /**
     * 被攻击次数
     */
    private int                      beAttackedNum       = 0;

    @Override
    public CommonMoveAction getNextAction() {
        // 先初始化所有数据信息
        this.setUp();
        try {
            if (nextLocation == null) {
                // 第一优先级，踩 可走步数内 即可踩到的人
                // this.isFireToRobot();
                if (null != nextLocation) {
                    // System.out.print("踩人, ");
                    printMyStep(nextLocation);
                }
            }
            if (nextLocation == null) {
                // 第二优先级，找血包
                this.isGetBloodBag();
                if (null != nextLocation) {
                    // System.out.print("找血包, ");
                    printMyStep(nextLocation);
                }
            }
            if (nextLocation == null) {
                // 第三优先级，找一个最近机器人去踩，距离是偶数的机器人
                this.closeOtherRobot();
                if (null != nextLocation) {
                    // System.out.print("朝敌方移动, ");
                    printMyStep(nextLocation);
                }
            }
            if (nextLocation == null) {
                // 没有距离是偶数的机器人，又没有血包，找一个安全的位置移动
                this.getGetSafeSetp();
                if (null != nextLocation) {
                    // System.out.print("找安全位置, ");
                    printMyStep(nextLocation);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (nextLocation == null) {
            // 实在没有安全位置，只能找随便移动一次
            this.randomMove();
            if (null != nextLocation) {
                // System.out.print("随机移动, ");
                printMyStep(nextLocation);
            }
        }
        // 当前位置记录轨迹
        oldLocationList.add(this.nextLocation);
        if (isHasLandmine(nextLocation)) {
            System.out.println("不知道在哪踩雷了。");
        }

        printNextStepInfo();

        if (calcLocationSteps(nextLocation, myLocation) != 1) {
            System.out.println("nextLocation设值错误，与当前位置步数不为1");
        }
        CommonMoveAction nextStep = new CommonMoveAction(this.getMoveActionCommandEnum(myLocation, nextLocation));
        return nextStep;
    }

    /**
     * 初始化信息
     */
    private void setUp() {
        // 清理上次的步骤
        this.nextLocation = null;
        // 清理可以移动步骤
        this.canMoveLocationList.clear();
        // 清空血包信息
        this.bloodBagList.clear();
        // 清空机器人信息
        this.otherRobotList.clear();
        // 清空地雷信息
        this.landmineList.clear();
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        // 更新地图信息
        elementLists = fightRobotSeeEntity.robotGetElementList;
        // 更新当前位置
        myLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;
        // 更新生命值
        this.bloodNum = fightRobotBaseInfo.bloodNum;
        // 地图尺寸
        this.mapSize = fightRobotSeeEntity.mapInfo.size;
        // 可走的步数
        this.currentCanStepNum = fightRobotSeeEntity.robotBaseInfo.currentCanStepNum;

        // 更机器人等信息
        this.doSeeInfo(elementLists);
        // 获取所有可移动的位置
        this.canMoveLocationList = this.getCanMoveList(myLocation, currentCanStepNum);
    }

    /**
     * 更新可见信息
     */
    private void doSeeInfo(ElementList[][] elementLists) {
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists[i].length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    switch (element.elementType) {
                    case BLOOD_BAG:
                        BloodBag bloodBag = (BloodBag) element;
                        this.bloodBagList.add(bloodBag);
                        break;
                    case LANDMINE:
                        this.landmineList.add((Landmine) element);
                        break;
                    case ROBOT_INFO:
                        AbstractRobotBaseInfo robotBaseInfo = (AbstractRobotBaseInfo) element;
                        if (robotBaseInfo.name != this.name) {
                            // 把自己的机器人排除掉
                            FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) element;
                            this.otherRobotList.add(fightRobotBaseInfo);
                        }
                        break;
                    default:
                        System.out.println("doSeeInfo 循环的数据异常，element：" + element);
                        break;

                    }

                }
            }
        }
    }

    /**
     * 判断下一步能否踩中其他机器人，注意不能踩当前位置的，否则就互相伤害了
     */
    private void isFireToRobot() {
        for (FightRobotBaseInfo robot : this.otherRobotList) {
            for (Location location : canMoveLocationList) {
                if (isOneLocation(location, robot.currentLocation)) {
                    // 我的可移动位置上有机器人
                    // lhldyf modify -1 改为了-2
                    if (oldLocationList.size() > 0
                            && isOneLocation(oldLocationList.get(oldLocationList.size() - 2), location)) {
                        if (this.bloodNum < robot.bloodNum + 2) {
                            // 比对方多两点血以上时，才去干他
                            continue;
                        }

                    }
                    if (calcLocationSteps(myLocation, location) == 1) {
                        // 只有这一步，直接移动就好了
                        nextLocation = location;
                        break;
                    } else { // 需要移动2次的
                        Location safeLocation = getNextSafeLocation(myLocation, location);
                        if (safeLocation != null) {
                            nextLocation = safeLocation;
                        } else {
                            fbiWarning();
                        }
                    }

                }
            }
        }
    }

    private void fbiWarning() {
        System.out.println("不应该啊");
    }

    // 判断下一步能否踩中血包

    /**
     * 找血包
     */
    private void isGetBloodBag() {

        if (bloodBagList.isEmpty()) {
            return;
        }

        // 根据距离排序
        List<Location> tempList = new ArrayList<>();
        for (BloodBag bloodBag : bloodBagList) {
            tempList.add(bloodBag.location);
        }

        this.sortLocation(tempList);

        // 离我最近的血包
        Location locationD = tempList.get(0);

        // 最近血包与敌人离该血包的步数
        int enamyStep = 0;
        // 找到一个血包，这个血包比所有机器人都离自己更近
        for (Location location : tempList) {
            // lhldyf mark
            int myNeedSteps = calcLocationSteps(myLocation, location);
            for (FightRobotBaseInfo fightRobotBaseInfo : this.otherRobotList) {
                int otherNeedSteps = this.calcLocationSteps(fightRobotBaseInfo.currentLocation, location);
                if (otherNeedSteps - myNeedSteps > enamyStep) {
                    // 离血包近，则使用该血包
                    locationD = location;
                    enamyStep = otherNeedSteps - myNeedSteps;
                }

            }

        }

        Location nextLocationTemp = getNextSafeLocation(myLocation, locationD);

        if (nextLocationTemp != null) {
            nextLocation = nextLocationTemp;
        } else {
            fbiWarning();
        }
    }

    // 找一个安全的地方，不能被其他机器人踩（>2格位置才是安全）以及不能走雷
    private void getGetSafeSetp() { // 周边找一个没雷的位置走
        for (Location location : this.canMoveLocationList) {
            if (this.isSafeLocation(location)) {
                this.nextLocation = this.getNextSafeLocation(myLocation, location);
                if (this.nextLocation != null) {
                    if (isHasLandmine(nextLocation)) {
                        System.out.println("以为找到安全位置，实际踩雷了");
                    }
                    break;
                }
            }
        }
    }

    /**
     * 更新获取所有可移动的位置的list
     * @param location
     * @param n
     * @return
     */
    private List<Location> getCanMoveList(Location location, int n) {
        List<Location> locationList = new ArrayList<>();
        if (n > 0) {
            if (location.x + n < mapSize) {
                locationList.add(new Location(location.x + n, location.y));
            }
            if (location.x - n >= 0) {
                locationList.add(new Location(location.x - n, location.y));
            }
            if (location.y + n < mapSize) {
                locationList.add(new Location(location.x, location.y + n));
            }
            if (location.y - n >= 0) {
                locationList.add(new Location(location.x, location.y - n));
            }
            n = n - 1;
            this.getCanMoveList(location, n);
        }
        return locationList;
    }

    // 找出2个位置是否存在1条最进距离且没有雷的路线的下一步,不需要判断是否被踩

    // private Location hasNextSafeLocation(Location locationS, Location
    // locationD) {
    // Location nextLocationTemp = null;
    // int n = calcLocationSteps(locationS, locationD);
    // if (n == 1) {
    // nextLocationTemp = locationD;
    // }
    // if (n > 1) {
    // List<Location> tempList=getCanMoveList(locationD,1);
    // this.sortLocation(tempList);
    //
    // for(Location locationTemp:tempList){
    // //为了防止无限次递归，控制范围
    // if(!isHasLandmine(locationTemp) &&
    // calcLocationSteps(myLocation,locationD)>calcLocationSteps(myLocation,locationTemp)){
    // nextLocationTemp=locationTemp;
    // break;
    // }
    // }
    // if (nextLocationTemp != null) {
    // hasNextSafeLocation(locationS, nextLocationTemp);
    // }
    //
    // }
    // return nextLocationTemp;
    // }

    // 判断一串Locat中，后续可移动位置最多的一个location
    private Location getNiceLocationFromLocations(List<Location> locationList) {
        Location niceLocation = null;
        int steps = 0;
        for (Location location : locationList) {
            if (niceLocation == null) {
                niceLocation = location;
                steps = getSafeNextLocatNum(location);
            } else {
                if (steps > getSafeNextLocatNum(location)) {
                    continue;
                } else {
                    niceLocation = location;
                    steps = getSafeNextLocatNum(location);
                }

            }
        }
        if (isHasLandmine(niceLocation)) {
            System.out.println("未来后面更好的位置，踩雷了");
        }
        return niceLocation;
    }

    private int getSafeNextLocatNum(Location location) {
        int num = 0;
        List<Location> locationList = this.getCanMoveList(location, 1);
        for (Location locationTemp : locationList) {
            if (isSafeLocation(locationTemp)) {
                num++;
            }
        }

        return num;
    }

    /**
     * 计算两个位置之间的最少步数（不管地雷）
     * @param locationA
     * @param locationB
     */
    private int calcLocationSteps(Location locationA, Location locationB) {
        return Math.abs(locationA.x - locationB.x) + Math.abs(locationA.y - locationB.y);
    }

    /**
     * 判断location是否是同一个位置,0表示同一个位置
     * @param locationA
     * @param locationB
     * @return
     */
    private boolean isOneLocation(Location locationA, Location locationB) {
        return calcLocationSteps(locationA, locationB) == 0;
    }

    /**
     * 根据2个location获取移动策略
     * @param locationS
     * @param locationD
     * @return
     */
    private MoveActionCommandEnum getMoveActionCommandEnum(Location locationS, Location locationD) {
        MoveActionCommandEnum moveActionCommandEnum = null;
        if (locationS.x < locationD.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_DOWN;
        }
        if (locationS.x > locationD.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_TOP;
        }
        if (locationS.y < locationD.y) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_RIGHT;
        }
        if (locationS.y > locationD.y) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_LEFT;
        }
        return moveActionCommandEnum;
    }

    /**
     * 判断一个位置是否是安全的
     *
     * @param location
     * @return 安全返回true，否则返回fasle
     */
    private Boolean isSafeLocation(Location location) {
        Boolean flag = true;
        if (isHasLandmine(location) || this.canBeFire(location)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 判断位置上是否有雷
     * @param location
     * @return
     */
    private Boolean isHasLandmine(Location location) {
        Boolean isHasLandmine = false;
        for (AbstractElement element : this.elementLists[location.x][location.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                isHasLandmine = true;
                break;
            }
        }
        return isHasLandmine;
    }

    /**
     * 打印下一步的位置内容
     */
    private void printNextStepInfo() {
        Location location = oldLocationList.get(oldLocationList.size() - 1);
        printMyStep(location);
    }

    private void printMyStep(Location location) {
        String locationInfo = "空位置";
        for (AbstractElement element : this.elementLists[location.x][location.y].elements) {
            switch (element.elementType) {
            case LANDMINE:
                locationInfo = "地雷";
                break;
            case BLOOD_BAG:
                locationInfo = "血包";
                break;
            case ROBOT_INFO:
                AbstractRobotBaseInfo robotBaseInfo = (AbstractRobotBaseInfo) element;
                locationInfo = "机器人" + robotBaseInfo.name;
                break;
            default:
                break;
            }
        }
        // System.out.println("nextStep("+location.x+","+location.y+"), info:"
        // +locationInfo);
    }

    /**
     * 判断一个位置是否会被踩
     *
     * @param location
     * @return 被踩返回true 否则返回fasle
     */
    private Boolean canBeFire(Location location) {
        for (FightRobotBaseInfo fightRobotBaseInfo : otherRobotList) {
            if (calcLocationSteps(location, fightRobotBaseInfo.currentLocation) > 2) {
                return false;
            }
            if (calcLocationSteps(location, fightRobotBaseInfo.currentLocation) < 2) {
                return true;
            }
            // 别人被踩了后，可能会得到2次机会，直接踩中我，所以这步不要了
            // if (isOneLocation(location, fightRobotBaseInfo.currentLocation)
            // == 2 && fightRobotBaseInfo.currentCanStepNum == 2) {
            // return true;
            // } else {
            // return false;
            // }

        }
        return false;
    }

    // 随机移动一步,找空白多的地方移动
    private void randomMove() {
        this.nextLocation = getNiceLocationFromLocations(canMoveLocationList);
        // Location location=null;
        // Random random = new Random(System.currentTimeMillis());
        // for (Location targetLocation : canMoveLocationList) { //这个位置不会被踩，就走这个
        // if (this.canBeFire(targetLocation)) {
        // continue;
        // }
        // if (isOneLocation(myLocation, targetLocation) == 1) {
        // location = targetLocation;
        // } else {
        // location = hasNextSafeLocation(myLocation, targetLocation);
        // if (location != null) {
        // break;
        // }
        //
        // }
        // }
        // while (location == null) { //没有安全位置，则随机找一个
        // location = canMoveLocationList.get(Math.abs(random.nextInt()) %
        // canMoveLocationList.size());
        // if (this.isOneLocation(location, myLocation) == 1) {
        // break;
        // }
        // }
        // this.nextLocation = location;
    }

    // private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K,
    // V> map) {
    // Map<K, V> result = new LinkedHashMap<K, V>();
    // if (map.size()==0){
    // return result;
    // }
    // List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K,
    // V>>(map.entrySet());
    // Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
    // public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
    // int a=0;
    // try {
    // a= (o1.getValue()).compareTo(o2.getValue());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return a;
    // }
    //
    // });

    // for (Map.Entry<K, V> entry : list) {
    // result.put(entry.getKey(), entry.getValue());
    // }
    // return result;
    // }
    private void sortLocation(List<Location> locationList) {
        Collections.sort(locationList, new SortByLocationSteps());
    }

    class SortByLocationSteps implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Location l1 = (Location) o1;
            Location l2 = (Location) o2;
            if (calcLocationSteps(l1, myLocation) >= calcLocationSteps(l2, myLocation)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 找一个距离是偶数的机器人靠近
     */
    private void closeOtherRobot() {
        List<Location> tempList = new ArrayList<>();
        for (FightRobotBaseInfo fightRobotBaseInfo : this.otherRobotList) {
            tempList.add(fightRobotBaseInfo.currentLocation);
        }
        this.sortLocation(tempList);
        for (Location location : tempList) {
            if (calcLocationSteps(myLocation, location) % 2 == 1) {
                Location targetLocationTemp = getNextSafeLocation(myLocation, location);
                if (targetLocationTemp != null) {
                    if ((currentCanStepNum == 1 && !canBeFire(targetLocationTemp)) || currentCanStepNum > 1) {
                        this.nextLocation = targetLocationTemp;
                        break;
                    }
                }
            }
        }
    }

    // /**
    // * 找到两个位置之间最短距离
    // * @param lastLocation
    // * @param locationS
    // * @param locationD
    // * @return
    // */
    // int findMinStepBetween(Location lastLocation, Location locationS,
    // Location locationD, int step) {
    //
    // // 当两个位置只差1时，则该路径是最短路径
    // if(calcLocationSteps(locationS, locationD) <= 1) {
    // return step + 1;
    // }
    //
    // // 如果xStep 小于0向下移，大于0向上移
    // int xStep = locationS.x - locationD.x;
    // // 如果yStep 小于0则向右移，大于0则向左移
    // int yStep = locationS.y - locationD.y;
    //
    // int leftStep = FAKE_MAX, rightStep = FAKE_MAX, topStep = FAKE_MAX,
    // downStep = FAKE_MAX;
    // // 左移一步，找最短路径
    // if(ableMove(locationS, MoveActionCommandEnum.MOVE_LEFT)) {
    // Location tempLocation = locationAfterMove(locationS,
    // MoveActionCommandEnum.MOVE_LEFT);
    // if(!isOneLocation(lastLocation, tempLocation) && yStep > 0 &&
    // !isHasLandmine(tempLocation)) {
    // leftStep = findMinStepBetween( locationS, tempLocation, locationD, step+1
    // );
    // }
    // }
    //
    // // 右移一步，找最短路径
    // if(ableMove(locationS, MoveActionCommandEnum.MOVE_RIGHT)) {
    // Location tempLocation = locationAfterMove(locationS,
    // MoveActionCommandEnum.MOVE_RIGHT);
    // if(!isOneLocation(lastLocation, tempLocation) && yStep < 0 &&
    // !isHasLandmine(tempLocation)) {
    // rightStep = findMinStepBetween( locationS, tempLocation, locationD,
    // step+1 );
    // }
    // }
    //
    // // 下移一步，找最短路径
    // if(ableMove(locationS, MoveActionCommandEnum.MOVE_DOWN)) {
    // Location tempLocation = locationAfterMove(locationS,
    // MoveActionCommandEnum.MOVE_DOWN);
    // if(!isOneLocation(lastLocation, tempLocation) && xStep < 0 &&
    // !isHasLandmine(tempLocation)) {
    // downStep = findMinStepBetween( locationS, tempLocation, locationD, step+1
    // );
    // }
    // }
    //
    // // 上移一步，找最短路径
    // if(ableMove(locationS, MoveActionCommandEnum.MOVE_TOP)) {
    // Location tempLocation = locationAfterMove(locationS,
    // MoveActionCommandEnum.MOVE_TOP);
    // if(!isOneLocation(lastLocation, tempLocation) && xStep > 0 &&
    // !isHasLandmine(tempLocation)) {
    // topStep = findMinStepBetween( locationS, tempLocation, locationD, step+1
    // );
    // }
    // }
    //
    // // 找到这四步中的最短路径
    // int minStep = Integer.MAX_VALUE;
    // MoveActionCommandEnum moveAction = null;
    // if(minStep > leftStep) {
    // // 往左
    // moveAction = MoveActionCommandEnum.MOVE_LEFT;
    // // 检查是否是雷，有雷的优先级降低
    // if(isHasLandmine(locationAfterMove(locationS, moveAction)) && minStep -
    // leftStep > mapSize) {
    // minStep = leftStep + mapSize;
    // } else {
    // minStep = leftStep;
    // }
    // }
    //
    // if(minStep > rightStep) {
    // // 往右
    // moveAction = MoveActionCommandEnum.MOVE_RIGHT;
    // // 检查是否是雷，有雷的优先级降低
    // if(isHasLandmine(locationAfterMove(locationS, moveAction)) && minStep -
    // rightStep > mapSize) {
    // minStep = rightStep + mapSize;
    // } else {
    // minStep = rightStep;
    // }
    // }
    //
    // if(minStep > downStep) {
    // // 往下
    // moveAction = MoveActionCommandEnum.MOVE_DOWN;
    // // 检查是否是雷，有雷的优先级降低
    // if(isHasLandmine(locationAfterMove(locationS, moveAction)) && minStep -
    // downStep > mapSize) {
    // minStep = downStep + mapSize;
    // } else {
    // minStep = downStep;
    // }
    // }
    //
    // if(minStep > topStep) {
    // // 往上
    // moveAction = MoveActionCommandEnum.MOVE_TOP;
    // // 检查是否是雷，有雷的优先级降低
    // if(isHasLandmine(locationAfterMove(locationS, moveAction)) && minStep -
    // topStep > mapSize) {
    // minStep = topStep + mapSize;
    // } else {
    // minStep = topStep;
    // }
    // }
    //
    // return step + minStep;
    // }

    boolean ableMove(Location location, MoveActionCommandEnum moveAction) {

        switch (moveAction) {
        case MOVE_DOWN:
            if (location.x + 1 >= mapSize) {
                return false;
            } else {
                return true;
            }
        case MOVE_TOP:
            if (location.x - 1 < 0) {
                return false;
            } else {
                return true;
            }
        case MOVE_LEFT:
            if (location.y - 1 < 0) {
                return false;
            } else {
                return true;
            }
        case MOVE_RIGHT:
            if (location.y + 1 >= mapSize) {
                return false;
            } else {
                return true;
            }
        default:
            return false;
        }
    }

    Location locationAfterMove(Location location, MoveActionCommandEnum moveAction) {
        Location nextLocation = null;
        switch (moveAction) {
        case MOVE_DOWN:
            nextLocation = new Location(location.x + 1, location.y);
            break;
        case MOVE_TOP:
            nextLocation = new Location(location.x - 1, location.y);
            break;
        case MOVE_LEFT:
            nextLocation = new Location(location.x, location.y - 1);
            break;
        case MOVE_RIGHT:
            nextLocation = new Location(location.x, location.y + 1);
            break;
        default:
            break;
        }
        return nextLocation;
    }

    Location getNextSafeLocation(Location locationS, Location locationD) {
        // 不走回头路
        Location nextLocation = null;

        // 如果xStep 小于0向下移，大于0向上移
        int xStep = locationS.x - locationD.x;
        // 如果yStep 小于0则向右移，大于0则向左移
        int yStep = locationS.y - locationD.y;

        for (int i = 0; i <= 10; i++) {

            if (i > 1) {
                // 如果下一步会踩雷或者会被人踩，则不强求这个方向，而是四个方向试探一圈
                nextLocation = randomMove(locationS);
                if (null != nextLocation) {
                    return nextLocation;
                }
            }

            if (yStep > 0) {
                nextLocation = returnMatchSafeLevel(locationS, MoveActionCommandEnum.MOVE_LEFT, i);
                if (null != nextLocation) {
                    return nextLocation;
                } else if (i > 1) {

                }
            }

            if (yStep < 0) {
                nextLocation = returnMatchSafeLevel(locationS, MoveActionCommandEnum.MOVE_RIGHT, i);
                if (null != nextLocation) {
                    return nextLocation;
                }
            }

            if (xStep < 0) {
                nextLocation = returnMatchSafeLevel(locationS, MoveActionCommandEnum.MOVE_DOWN, i);
                if (null != nextLocation) {
                    return nextLocation;
                }
            }

            if (xStep > 0) {
                nextLocation = returnMatchSafeLevel(locationS, MoveActionCommandEnum.MOVE_TOP, i);
                if (null != nextLocation) {
                    return nextLocation;
                }
            }
        }

        if (nextLocation == null) {
            fbiWarning();
        }

        return nextLocation;
    }

    Location randomMove(Location locationS) {
        List<MoveActionCommandEnum> list = new ArrayList<>();
        list.add(MoveActionCommandEnum.MOVE_DOWN);
        list.add(MoveActionCommandEnum.MOVE_LEFT);
        list.add(MoveActionCommandEnum.MOVE_RIGHT);
        list.add(MoveActionCommandEnum.MOVE_TOP);
        for (MoveActionCommandEnum move : list) {
            if (ableMove(locationS, move)) {
                Location nextLocation = returnMatchSafeLevel(locationS, move, 0);
                if (null != nextLocation) {
                    return nextLocation;
                }
            }
        }

        for (MoveActionCommandEnum move : list) {
            Location nextLocation = returnMatchSafeLevel(locationS, move, 1);
            if (null != nextLocation) {
                return nextLocation;
            }
        }

        return null;
    }

    /**
     * 该策略若绝对安全，则走该步骤
     * @param location
     * @param moveActionCommandEnum
     * @param safeLevel
     * @return
     */
    Location returnMatchSafeLevel(Location location, MoveActionCommandEnum moveActionCommandEnum, int safeLevel) {
        Location nextLocation;
        if (ableMove(location, moveActionCommandEnum)) {
            // 不管怎么样，都要这步是能走的

            nextLocation = locationAfterMove(location, moveActionCommandEnum);

            if (safeLevel == 0) {
                // 安全级别最高时，要求下一步没有雷且不会被踩，且不是之前走过的步骤
                if (!isHasLandmine(nextLocation) && !hasEnemy(nextLocation) && notLast2Location(nextLocation)) {
                    // System.out.println("下一步安全级别:" + safeLevel);
                    return nextLocation;
                }
            } else if (safeLevel == 1) {
                // 可以是之前走过的步骤
                if (!isHasLandmine(nextLocation) && !hasEnemy(nextLocation)) {
                    // System.out.println("下一步安全级别:" + safeLevel);
                    return nextLocation;
                }
            } else if (safeLevel == 2) {
                // 可以是雷
                if (!hasEnemy(nextLocation)) {
                    // System.out.println("下一步安全级别:" + safeLevel);
                    return nextLocation;
                }
            } else if (safeLevel == 3) {
                // 可以是敌人
                if (!isHasLandmine(nextLocation)) {
                    // System.out.println("下一步安全级别:" + safeLevel);
                    return nextLocation;
                }
            }
        }
        return null;
    }

    /**
     * 检查这个位置有没有敌人
     * @param location
     * @return
     */
    boolean hasEnemy(Location location) {
        for (FightRobotBaseInfo robotBaseInfo : otherRobotList) {
            if (calcLocationSteps(robotBaseInfo.currentLocation, location) == 0
                    && this.bloodNum - robotBaseInfo.bloodNum > 2) {
                // 下一步有比我少两血的敌人，就是干
                return false;
            }

            if (this.currentCanStepNum == 1) {
                // 若我只能移动一步，那么下一步不能再敌人移动范围内
                if (calcLocationSteps(robotBaseInfo.currentLocation, location) <= robotBaseInfo.currentCanStepNum) {
                    return true;
                }
            }

            if (this.currentCanStepNum == 2) {
                // 若我能移动两步，就不管了。
                return false;
            }

        }
        return false;
    }

    boolean notLast2Location(Location location) {
        if (oldLocationList.size() > 1) {
            Location last2Location = oldLocationList.get(oldLocationList.size() - 2);
            if (last2Location.x == location.x && last2Location.y == location.y) {
                return false;
            }
        }

        return true;
    }
}
