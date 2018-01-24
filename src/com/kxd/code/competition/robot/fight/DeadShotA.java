package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;

public class DeadShotA extends AbstractFightRobot {

    private final static Map<String, MoveActionCommandEnum> map         = new HashMap<>();

    public static MoveActionCommandEnum                     beforeMove;

    public static Index                                     targetBloodIndex;

    public static Index                                     findBloodIndex;

    public static int                                       targetBloodStep;

    public static int                                       count       = 0;

    public static List<String>                              excludeKeys = new ArrayList<>();

    static {
        map.put("LEFT", MoveActionCommandEnum.MOVE_LEFT);
        map.put("RIGHT", MoveActionCommandEnum.MOVE_RIGHT);
        map.put("UP", MoveActionCommandEnum.MOVE_TOP);
        map.put("DOWN", MoveActionCommandEnum.MOVE_DOWN);
    }

    public DeadShotA() {
        super("DeadShot");
    }

    // X - 1 向上；
    // X + 1 向下；
    // Y - 1 向左；
    // Y + 1 向右；

    @Override
    public CommonMoveAction getNextAction() {
        excludeKeys.clear();
        CommonMoveAction commonMoveAction = null;
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        // TODO one
        FightRobotBaseInfo myself = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        try {
            FightRobotBaseInfo targetRobot = myself;
            int myStepNum = myself.currentCanStepNum;
            int myBloodNum = myself.bloodNum;
            Index targetIndex = new Index();
            targetIndex.x = myself.currentLocation.x;
            targetIndex.y = myself.currentLocation.y;
            ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
            Map<String, Integer> nearOnePoint = new HashMap<>();
            // 获取当前周边环境
            // 根据优先逻辑选取路径；
            // if (myStepNum == 1) {
            // 获取指定做标周围环境
            System.out.println(targetIndex.x + "||" + targetIndex.y);
            Map<String, NearElement> nearElementMap = findNearElement(targetIndex, fightRobotSeeEntity);
            // 计算当前环境
            nearOnePoint = calcOneTargetGradePoint(nearElementMap, fightRobotSeeEntity);

            // } else if (myStepNum == 2) {
            // Map<String, NearElement> nearElementMap =
            // findNearElement(targetIndex,fightRobotSeeEntity);
            // // 计算当前环境
            // nearOnePoint =
            // calcOneTargetGradePoint(nearElementMap,fightRobotSeeEntity);
            // }

            commonMoveAction = MoveAction(nearOnePoint);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return commonMoveAction;
    }

    private CommonMoveAction MoveAction(Map<String, Integer> nearPoint) {
        List<String> goKey = new ArrayList<>();
        Integer value = -100;

        Set<Map.Entry<String, Integer>> set = nearPoint.entrySet();
        Iterator<Map.Entry<String, Integer>> it = set.iterator();
        Map.Entry<String, Integer> entry = null;

        boolean noElement = true;

        while (it.hasNext()) {
            entry = it.next();

            System.out.println("key:" + entry.getKey() + "||value:" + entry.getValue());

            if (entry.getValue() != 0) {
                noElement = false;
            }

            if (entry.getValue() < 0) {
                excludeKeys.add(map.get(entry.getKey()).name());
            }
            if (entry.getValue() > value) {
                value = entry.getValue();
                goKey.clear();
                goKey.add(entry.getKey());
            } else if (entry.getValue() == value) {
                goKey.add(entry.getKey());
            }

        }
        MoveActionCommandEnum moveActionCommand = null;
        if (!noElement && goKey.size() != 0) {
            boolean isContinusCalc = false;
            for (String key : goKey) {
                if (0 == nearPoint.get(key)) {
                    isContinusCalc = true;
                    break;
                }
            }
            if (!isContinusCalc) {
                moveActionCommand = map.get(goKey.get(Math.abs(new Random().nextInt()) % goKey.size()));
            }
        }

        if (null == moveActionCommand) {
            Index index = randomGo();
            moveActionCommand = randomMoveAction(index);

        }

        CommonMoveAction actionEntity = new CommonMoveAction(moveActionCommand);
        return actionEntity;
    }

    private MoveActionCommandEnum randomMoveAction(Index index) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        int x = fightRobotSeeEntity.robotBaseInfo.currentLocation.x;
        int y = fightRobotSeeEntity.robotBaseInfo.currentLocation.y;
        MoveActionCommandEnum moveActionCommandEnum = null;
        int random = Math.abs(new Random(10).nextInt()) % 2;
        List<MoveActionCommandEnum> moveActionCommandEnums = new ArrayList<>();
        boolean isLandmine = true;
        boolean isExclue = true;
        while (isExclue) {
            while (true) {
                System.out.println(index.x + index.y);
                if (index.x < x) {
                    if (!isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x - 1, y)) || !isLandmine)
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_TOP);
                } else if (index.x > x) {
                    if (!isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x + 1, y)) || !isLandmine)
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_DOWN);
                }
                if (index.y < y) {
                    if (!isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x, y - 1)) || !isLandmine)
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_LEFT);
                } else if (index.y > y) {
                    if (!isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x, y + 1)) || !isLandmine)
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_RIGHT);
                }

                Iterator<MoveActionCommandEnum> it = moveActionCommandEnums.iterator();
                MoveActionCommandEnum m1 = null;
                while (it.hasNext()) {
                    m1 = it.next();
                    if (excludeKeys.contains(m1.name()))
                        if (m1.getCode().equals(null == beforeMove ? null : beforeMove.getCode()))
                            it.remove();
                }

                if (moveActionCommandEnums.isEmpty()) {

                    if (!isOut(new Index(x - 1, y), fightRobotSeeEntity)
                            && !isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x - 1, y)))
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_TOP);
                    if (!isOut(new Index(x + 1, y), fightRobotSeeEntity)
                            && !isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x + 1, y)))
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_DOWN);

                    if (!isOut(new Index(x, y - 1), fightRobotSeeEntity)
                            && !isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x, y - 1)))
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_LEFT);
                    if (!isOut(new Index(x, y + 1), fightRobotSeeEntity)
                            && !isHasLandmine(fightRobotSeeEntity.robotGetElementList, new Index(x, y + 1)))
                        moveActionCommandEnums.add(MoveActionCommandEnum.MOVE_RIGHT);

                }

                if (moveActionCommandEnums.isEmpty()) {
                    isLandmine = false;
                } else {
                    break;
                }

            }

            Iterator<MoveActionCommandEnum> it = moveActionCommandEnums.iterator();
            MoveActionCommandEnum m1 = null;
            while (it.hasNext()) {
                m1 = it.next();
                if (excludeKeys.contains(m1.name())
                        || (m1.getCode().equals(null == beforeMove ? null : beforeMove.getCode()) && isExclue))
                    it.remove();
            }
            if (moveActionCommandEnums.size() > 0) {
                moveActionCommandEnum = moveActionCommandEnums.get(random % moveActionCommandEnums.size());
                break;
            } else {
                isExclue = false;
                if (!isExclue) {
                    moveActionCommandEnum = beforeMove;
                }
            }
        }

        beforeMove = coverBefore(moveActionCommandEnum);

        return moveActionCommandEnum;
    }

    private MoveActionCommandEnum coverBefore(MoveActionCommandEnum moveActionCommandEnum) {
        if (moveActionCommandEnum == null) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_RIGHT;
        }
        MoveActionCommandEnum m1 = null;

        if (MoveActionCommandEnum.MOVE_TOP.getCode().equals(moveActionCommandEnum.getCode())) {
            m1 = MoveActionCommandEnum.MOVE_DOWN;
        } else if (MoveActionCommandEnum.MOVE_DOWN.getCode().equals(moveActionCommandEnum.getCode())) {
            m1 = MoveActionCommandEnum.MOVE_TOP;
        } else if (MoveActionCommandEnum.MOVE_LEFT.getCode().equals(moveActionCommandEnum.getCode())) {
            m1 = MoveActionCommandEnum.MOVE_RIGHT;
        } else {
            m1 = MoveActionCommandEnum.MOVE_LEFT;
        }
        return m1;
    }

    private NearElement getNearByElement(FightRobotSeeEntity fightRobotSeeEntity, Index index) {
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;

        ElementList elementList = elementLists[index.x][index.y];
        List<AbstractElement> abstractElements = new ArrayList<>();
        for (AbstractElement element : elementList.elements) {
            if (ElementTypeEnum.BLOOD_BAG == element.elementType || ElementTypeEnum.LANDMINE == element.elementType
                    || ElementTypeEnum.ROBOT_INFO == element.elementType) {
                abstractElements.add(element);
            }
        }

        NearElement nearElement = coverNearElement(abstractElements, index);

        return nearElement;
    }

    private Map<String, Integer> calcOneTargetGradePoint(Map<String, NearElement> nearElementMap,
            FightRobotSeeEntity fightRobotSeeEntity) {
        Map<String, Integer> map = new HashMap<>();
        int leftPoint = 0;
        int rightPoint = 0;
        int upPoint = 0;
        int downPoint = 0;
        FightRobotBaseInfo myself = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        NearElement leftNearElement = nearElementMap.get("LEFT");
        NearElement rightNearElement = nearElementMap.get("RIGHT");
        NearElement upNearElement = nearElementMap.get("UP");
        NearElement downNearElement = nearElementMap.get("DOWN");

        boolean continues = true;
        // 计算一级距离
        if (leftNearElement != null) {
            leftPoint = calcOneGradePoint(leftNearElement, myself, fightRobotSeeEntity);
        } else {
            leftPoint = -89;
        }

        if (rightNearElement != null) {
            rightPoint = calcOneGradePoint(rightNearElement, myself, fightRobotSeeEntity);
        } else {
            rightPoint = -89;
        }

        if (upNearElement != null) {
            upPoint = calcOneGradePoint(upNearElement, myself, fightRobotSeeEntity);
        } else {
            upPoint = -89;
        }

        if (downNearElement != null) {
            downPoint = calcOneGradePoint(downNearElement, myself, fightRobotSeeEntity);
        } else {
            downPoint = -89;
        }

        map.put("LEFT", leftPoint);
        map.put("RIGHT", rightPoint);
        map.put("UP", upPoint);
        map.put("DOWN", downPoint);

        return map;
    }

    private Integer calcTwoTargetGradePoint(Map<String, NearElement> nearElementMap,
            FightRobotSeeEntity fightRobotSeeEntity) {
        // Map<String,Integer> map = new HashMap<>();
        int leftPoint = 0;
        int rightPoint = 0;
        int upPoint = 0;
        int downPoint = 0;

        Integer twoGradePoint = 0;
        FightRobotBaseInfo myself = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        NearElement leftNearElement = nearElementMap.get("LEFT");
        NearElement rightNearElement = nearElementMap.get("RIGHT");
        NearElement upNearElement = nearElementMap.get("UP");
        NearElement downNearElement = nearElementMap.get("DOWN");

        List<FightRobotBaseInfo> robots = new ArrayList<>();
        List<BloodBag> bloodBags = new ArrayList<>();
        List<Landmine> landmines = new ArrayList<>();
        if (null != leftNearElement && null != leftNearElement.robotBaseInfos)
            robots.addAll(leftNearElement.robotBaseInfos);
        if (null != rightNearElement && null != rightNearElement.robotBaseInfos)
            robots.addAll(rightNearElement.robotBaseInfos);
        if (null != upNearElement && null != upNearElement.robotBaseInfos)
            robots.addAll(upNearElement.robotBaseInfos);
        if (null != downNearElement && null != downNearElement.robotBaseInfos)
            robots.addAll(downNearElement.robotBaseInfos);

        if (myself.bloodNum < 2 * robots.size()) {
            twoGradePoint += -79;
        } else {
            int robotBlood = myself.bloodNum;
            for (FightRobotBaseInfo robot : robots) {
                robotBlood = robotBlood - robot.bloodNum;
            }

            if (robotBlood >= 3) {
                twoGradePoint += 1;
            }
            if (robotBlood < 0) {
                twoGradePoint += -79;
            }
        }

        if (null != leftNearElement && null != leftNearElement.bloodBags)
            bloodBags.addAll(leftNearElement.bloodBags);
        if (null != rightNearElement && null != rightNearElement.bloodBags)
            bloodBags.addAll(rightNearElement.bloodBags);
        if (null != upNearElement && null != upNearElement.bloodBags)
            bloodBags.addAll(upNearElement.bloodBags);
        if (null != downNearElement && null != downNearElement.bloodBags)
            bloodBags.addAll(downNearElement.bloodBags);

        twoGradePoint += 4 * bloodBags.size();

        // if (leftNearElement != null) {
        // leftPoint = calcTwoGradePoint(leftNearElement, myself,
        // fightRobotSeeEntity);
        // twoGradePoint += leftPoint;
        // }
        //
        // if (rightNearElement != null) {
        // rightPoint = calcTwoGradePoint(rightNearElement, myself,
        // fightRobotSeeEntity);
        // twoGradePoint += rightPoint;
        // }
        //
        // if (upNearElement != null) {
        // upPoint = calcTwoGradePoint(upNearElement, myself,
        // fightRobotSeeEntity);
        // twoGradePoint += upPoint;
        // }
        //
        // if (downNearElement != null) {
        // downPoint = calcTwoGradePoint(downNearElement, myself,
        // fightRobotSeeEntity);
        // twoGradePoint += downPoint;
        // }
        //
        // map.put("LEFT",leftPoint);
        // map.put("RIGHT",rightPoint);
        // map.put("UP",upPoint);
        // map.put("DOWN",downPoint);

        return twoGradePoint;
    }

    private Integer calcOneGradePoint(NearElement nearElement, FightRobotBaseInfo myself,
            FightRobotSeeEntity fightRobotSeeEntity) {
        Integer gradePoint = 0;
        // TODO 敌人判断
        if (!nearElement.robotBaseInfos.isEmpty()) {

            if (nearElement.robotBaseInfos.get(0).bloodNum <= 2) {
                gradePoint += 3;
            }

            if (myself.bloodNum >= nearElement.robotBaseInfos.get(0).bloodNum) {
                gradePoint += 2;
            } else {
                gradePoint += -89;
            }
        }
        // TODO 血包判断
        if (!nearElement.bloodBags.isEmpty()) {
            gradePoint += 4;
        }
        // TODO 边界判断
        if (isOut(nearElement.index, fightRobotSeeEntity)) {
            gradePoint += -89;
        }
        // TODO 地雷判断
        if (!nearElement.landmines.isEmpty()) {
            gradePoint += -2;
        }

        if (myself.currentCanStepNum == 1) {
            Map<String, NearElement> twoNearElementMap = findNearElement(nearElement.index, fightRobotSeeEntity);
            Integer twoGradePoint = calcTwoTargetGradePoint(twoNearElementMap, fightRobotSeeEntity);
            gradePoint += twoGradePoint;
        }
        return gradePoint;
    }

    private Index randomGo() {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        FightRobotBaseInfo myself = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        Index greatIndex = null;

        List<BloodBag> bloodBags = new ArrayList<>();
        List<FightRobotBaseInfo> robotInfos = new ArrayList<>();

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (element instanceof BloodBag) {
                        bloodBags.add((BloodBag) element);
                    }

                    if (element instanceof FightRobotBaseInfo) {
                        FightRobotBaseInfo robot = (FightRobotBaseInfo) element;
                        if (myself.currentLocation.x != robot.currentLocation.x
                                || myself.currentLocation.y != robot.currentLocation.y)
                            robotInfos.add((FightRobotBaseInfo) element);
                    }
                }
            }
        }
        Index myIndex = new Index(myself.currentLocation.x, myself.currentLocation.y);
        bloodBags = sortBlood(bloodBags, myIndex);
        boolean isGo = true;
        for (BloodBag bloodBag : bloodBags) {
            if (findBloodIndex != null && calcLong(myIndex, findBloodIndex) == 0) {
                count++;
            }
            if (targetBloodIndex != null
                    && calcLong(targetBloodIndex, new Index(bloodBag.location.x, bloodBag.location.y)) == 0
                    && (targetBloodStep / 2 > calcLong(findBloodIndex, myIndex) || count > 3)) {
                continue;
            }
            if (calcLong(myIndex, new Index(bloodBag.location.x, bloodBag.location.y)) < 2) {
                continue;
            }
            for (FightRobotBaseInfo robot : robotInfos) {
                if (calcLong(new Index(myIndex.x, myIndex.y),
                        new Index(bloodBag.location.x, bloodBag.location.y)) >= calcLong(
                                new Index(robot.currentLocation.x, robot.currentLocation.y),
                                new Index(bloodBag.location.x, bloodBag.location.y))) {
                    isGo = false;
                    break;
                }
            }
            if (isGo) {
                if (targetBloodIndex == null
                        || calcLong(targetBloodIndex, new Index(bloodBag.location.x, bloodBag.location.y)) != 0) {
                    targetBloodIndex = new Index(bloodBag.location.x, bloodBag.location.y);
                    findBloodIndex = new Index(myself.currentLocation.x, myself.currentLocation.y);
                    count = 0;
                    targetBloodStep = 0;
                } else {
                    targetBloodStep++;
                }
                greatIndex = new Index(bloodBag.location.x, bloodBag.location.y);
                break;
            }
        }

        if (null == greatIndex) {
            if (!bloodBags.isEmpty() && !isGo) {
                greatIndex = new Index(bloodBags.get(0).location.x, bloodBags.get(0).location.y);
            } else {

                List<FightRobotBaseInfo> robotByBloods = sortRobotByBlood(robotInfos);

                for (FightRobotBaseInfo robotBaseInfo : robotByBloods) {
                    if (robotBaseInfo.bloodNum < myself.bloodNum) {
                        greatIndex = new Index(robotBaseInfo.currentLocation.x, robotBaseInfo.currentLocation.y);
                        break;
                    }
                }

                if (null == greatIndex) {
                    List<FightRobotBaseInfo> robotByIndexs = sortRobotByIndex(robotInfos, myIndex);
                    for (FightRobotBaseInfo robotBaseInfo : robotByIndexs) {
                        if (robotBaseInfo.bloodNum > myself.bloodNum) {
                            if (robotBaseInfo.currentLocation.x > myIndex.x) {
                                excludeKeys.add("DOWN");
                            } else if (robotBaseInfo.currentLocation.x < myIndex.x) {
                                excludeKeys.add("UP");
                            }

                            if (robotBaseInfo.currentLocation.y > myIndex.y) {
                                excludeKeys.add("RIGHT");
                            } else if (robotBaseInfo.currentLocation.y < myIndex.y) {
                                excludeKeys.add("LEFT");
                            }
                        }
                    }
                }
                if (null == greatIndex) {
                    int size = fightRobotSeeEntity.mapInfo.size;
                    if (myIndex.x != (size / 2) || myIndex.y != (size / 2))
                        greatIndex = new Index(size / 2, size / 2);
                    else
                        greatIndex = new Index(0, 0);
                }
            }
        }
        return greatIndex;
    }

    private List<BloodBag> sortBlood(List<BloodBag> bloodBags, Index index) {

        // FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity)
        // currentSeeMazeSituation;
        // int mapSize = fightRobotSeeEntity.mapInfo.size;

        final Index ti = new Index(index.x, index.y);
        Collections.sort(bloodBags, new Comparator<BloodBag>() {
            @Override
            public int compare(BloodBag o1, BloodBag o2) {

                Index i1 = new Index(o1.location.x, o1.location.y);

                Index i2 = new Index(o2.location.x, o2.location.y);

                int l1 = calcLong(ti, i1);
                int l2 = calcLong(ti, i2);
                return l1 > l2 ? 1 : -1;
            }
        });

        return bloodBags;
    }

    private List<FightRobotBaseInfo> sortRobotByBlood(List<FightRobotBaseInfo> robots) {

        Collections.sort(robots, new Comparator<FightRobotBaseInfo>() {
            @Override
            public int compare(FightRobotBaseInfo o1, FightRobotBaseInfo o2) {
                return o1.bloodNum < o2.bloodNum ? 1 : -1;
            }
        });

        return robots;
    }

    private List<FightRobotBaseInfo> sortRobotByIndex(List<FightRobotBaseInfo> robots, Index index) {

        final Index ti = new Index(index.x, index.y);
        Collections.sort(robots, new Comparator<FightRobotBaseInfo>() {
            @Override
            public int compare(FightRobotBaseInfo o1, FightRobotBaseInfo o2) {
                Index i1 = new Index(o1.currentLocation.x, o1.currentLocation.y);

                Index i2 = new Index(o2.currentLocation.x, o2.currentLocation.y);

                int l1 = calcLong(ti, i1);
                int l2 = calcLong(ti, i2);
                return l1 > l2 ? 1 : -1;
            }
        });

        return robots;
    }

    private int calcLong(Index a, Index b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private int calcTwoGradePoint(NearElement nearElement, FightRobotBaseInfo myself,
            FightRobotSeeEntity fightRobotSeeEntity) {
        int gradePoint = 0;
        // TODO 敌人判断
        if (!nearElement.robotBaseInfos.isEmpty()) {
            if (!myself.name.equals(nearElement.robotBaseInfos.get(0).name)) {
                if (myself.bloodNum >= nearElement.robotBaseInfos.get(0).bloodNum + 2) {
                    gradePoint += 4;
                } else {
                    gradePoint += -89;
                }
            }
        }
        // TODO 血包判断
        if (!nearElement.bloodBags.isEmpty()) {
            gradePoint += 2;
        }
        // TODO 边界判断
        if (isOut(nearElement.index, fightRobotSeeEntity)) {
            gradePoint += -89;
        }

        return gradePoint;
    }

    private Boolean isHasLandmine(ElementList[][] elementLists, Index index) {
        Boolean isHasLandmine = false;

        for (AbstractElement element : elementLists[index.x][index.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                isHasLandmine = true;
                break;
            }
        }
        return isHasLandmine;
    }

    private Map<String, NearElement> findNearElement(Index targetIndex, FightRobotSeeEntity fightRobotSeeEntity) {
        Map<String, NearElement> nearElementMap = new HashMap<>();
        if (targetIndex.x - 1 >= 0) {
            Index upIndex = new Index(targetIndex.x - 1, targetIndex.y);
            nearElementMap.put("UP", getNearByElement(fightRobotSeeEntity, upIndex));
        }
        if (targetIndex.x + 1 <= fightRobotSeeEntity.mapInfo.size - 1) {
            Index downIndex = new Index();
            downIndex.x = targetIndex.x + 1;
            downIndex.y = targetIndex.y;
            nearElementMap.put("DOWN", getNearByElement(fightRobotSeeEntity, downIndex));
        }
        if (targetIndex.y - 1 >= 0) {
            Index leftIndex = new Index();
            leftIndex.x = targetIndex.x;
            leftIndex.y = targetIndex.y - 1;
            nearElementMap.put("LEFT", getNearByElement(fightRobotSeeEntity, leftIndex));
        }

        if (targetIndex.y + 1 <= fightRobotSeeEntity.mapInfo.size - 1) {
            Index rightIndex = new Index();
            rightIndex.x = targetIndex.x;
            rightIndex.y = targetIndex.y + 1;
            nearElementMap.put("RIGHT", getNearByElement(fightRobotSeeEntity, rightIndex));
        }

        return nearElementMap;
    }

    private NearElement coverNearElement(List<AbstractElement> elements, Index index) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        // TODO one
        FightRobotBaseInfo myself = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        List<FightRobotBaseInfo> robots = new ArrayList<>();
        List<BloodBag> bloodBags = new ArrayList<>();
        List<Landmine> landmines = new ArrayList<>();
        for (AbstractElement abstractElement : elements) {
            if (ElementTypeEnum.ROBOT_INFO.equals(abstractElement.elementType)) {
                FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) abstractElement;
                if (fightRobotBaseInfo.bloodNum != 0 && fightRobotBaseInfo.name.equals(myself.name))
                    robots.add(fightRobotBaseInfo);
            }
            if (ElementTypeEnum.BLOOD_BAG.equals(abstractElement.elementType)) {
                bloodBags.add((BloodBag) abstractElement);
            }
            if (ElementTypeEnum.LANDMINE.equals(abstractElement.elementType)) {
                landmines.add((Landmine) abstractElement);
            }
        }
        NearElement nearElement = new NearElement();
        nearElement.bloodBags = bloodBags;
        nearElement.landmines = landmines;
        nearElement.robotBaseInfos = robots;
        Index nearIndex = new Index();
        nearIndex.x = index.x;
        nearIndex.y = index.y;
        nearElement.index = nearIndex;
        return nearElement;
    }

    private boolean isOut(Index index, FightRobotSeeEntity fightRobotSeeEntity) {
        int mapSize = fightRobotSeeEntity.mapInfo.size;
        return index.x < 0 || index.x + 1 > mapSize || index.y < 0 || index.y + 1 > mapSize;
    }

    public class Index {
        public int x;

        public int y;

        public Index(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Index() {
        }
    }

    public class NearElement {
        public Index                    index;

        public List<BloodBag>           bloodBags;

        public List<Landmine>           landmines;

        public List<FightRobotBaseInfo> robotBaseInfos;

        public Integer                  point;

    }

}