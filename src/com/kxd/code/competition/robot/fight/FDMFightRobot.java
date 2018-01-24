package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.CommonConstant;
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
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;

public class FDMFightRobot extends AbstractFightRobot {

    private static final String KEY_NEAREST = "KEY_NEAREST";

    public FDMFightRobot() {
        super("伏地魔");
    }

    @Override
    public CommonMoveAction getNextAction() {

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        //
        List<AbstractElement> bloodBags = getBloodBags(elementLists);
        List<AbstractElement> landmines = getLandmines(elementLists);
        List<AbstractElement> robotInfos = getRobotInfos(elementLists);

        MoveActionCommandEnum moveActionCommand = generateNoLandmineAction(bloodBags, landmines, robotInfos);

        CommonMoveAction actionEntity = new CommonMoveAction(moveActionCommand);
        return actionEntity;
    }

    /**
     * 找最近的血包，追踪血包，遇人杀人，遇雷避雷；没有血包，找人去干，注意避雷。
     * @param bloodBags
     * @param landmines
     * @param robotInfos
     * @return
     */
    private MoveActionCommandEnum generateNoLandmineAction(List<AbstractElement> bloodBags,
            List<AbstractElement> landmines, List<AbstractElement> robotInfos) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        //
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;

        Random random = new Random(System.currentTimeMillis());
        int index = Math.abs(random.nextInt()) % 4;
        MoveActionCommandEnum moveActionCommand = CommonConstant.directCodes.get(index);
        // MoveActionCommandEnum moveActionCommand =
        // MoveActionCommandEnum.MOVE_DOWN;
        try {
            if (bloodBags != null && bloodBags.size() > 0) {
                // 有血包，计算距离最近的血包--need增加绕路和预险和反击机制
                BloodBag bloodBag = (BloodBag) getNearest(bloodBags, currentLocation, null);
                moveActionCommand = getMoveDis(bloodBag.location.x, bloodBag.location.y, currentLocation, elementLists);
            } else {
                // 没血包
                if (robotInfos != null && robotInfos.size() > 0) {
                    List<AbstractElement> exceptEs = new ArrayList<>();
                    int i = 0;
                    // 有人,干人--增加一个判断目标机器人是否干的过，干不过别追他了追个血量比自己少的,如果没有比自己少的。。自己随便走？还是选择性攻击自杀
                    while (true) {
                        FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) getNearest(robotInfos,
                                currentLocation, exceptEs);
                        if (fightRobotBaseInfo != null) {
                            if (isCrazyKill(fightRobotBaseInfo)) {
                                moveActionCommand = getMoveDis(fightRobotBaseInfo.currentLocation.x,
                                        fightRobotBaseInfo.currentLocation.y, currentLocation, elementLists);
                                break;
                            } else {
                                exceptEs.add(fightRobotBaseInfo);
                                if (exceptEs.size() == (robotInfos.size() - 1) || i == 25) {
                                    moveActionCommand = calNextAction(moveActionCommand, currentLocation, elementLists);
                                    break;
                                }
                            }
                        } else {
                            moveActionCommand = calNextAction(moveActionCommand, currentLocation, elementLists);
                            break;
                        }
                        i++;
                    }
                } else {
                    // 没人随便走，存活，避雷且防止出界
                    moveActionCommand = calNextAction(moveActionCommand, currentLocation, elementLists);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 没人随便走，存活，避雷且防止出界
            moveActionCommand = calNextAction(moveActionCommand, currentLocation, elementLists);
        }

        return moveActionCommand;
    }

    /**
     * 判断下一步的四周是否有敌机，预险机制
     * @return
     */
    private boolean judgeNextLocationHasDanger(Location currentLocation, MoveActionCommandEnum moveActionCommand,
            ElementList[][] elementLists) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        FightRobotBaseInfo fightRobotBaseInfoNow = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        String nowName = fightRobotBaseInfoNow.name;
        Location nextLocation = getNewLocationByMove(moveActionCommand, currentLocation);
        int nextX = nextLocation.x;
        int nextY = nextLocation.y;
        int maxX = elementLists.length;
        int maxY = elementLists[0].length;
        if ((nextX + 1) <= (maxX - 1)) {
            List<AbstractElement> es = elementLists[nextX + 1][nextY].elements;
            if (es != null && es.size() != 0) {
                AbstractElement downElement = es.get(0);
                if (ElementTypeEnum.ROBOT_INFO == downElement.elementType) {
                    FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) downElement;
                    if (!nowName.equals(fightRobotBaseInfo.name)) {
                        return true;
                    }
                }
            }
        }
        if ((nextX - 1) >= 0) {
            List<AbstractElement> es = elementLists[nextX - 1][nextY].elements;
            if (es != null && es.size() != 0) {
                AbstractElement topElement = es.get(0);
                if (ElementTypeEnum.ROBOT_INFO == topElement.elementType) {
                    FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) topElement;
                    if (!nowName.equals(fightRobotBaseInfo.name)) {
                        return true;
                    }
                }
            }
        }
        if ((nextY + 1) <= (maxY - 1)) {
            List<AbstractElement> es = elementLists[nextX][nextY + 1].elements;
            if (es != null && es.size() != 0) {
                AbstractElement rightElement = es.get(0);
                if (ElementTypeEnum.ROBOT_INFO == rightElement.elementType) {
                    FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) rightElement;
                    if (!nowName.equals(fightRobotBaseInfo.name)) {
                        return true;
                    }
                }
            }
        }
        if ((nextY - 1) >= 0) {
            List<AbstractElement> es = elementLists[nextX][nextY - 1].elements;
            if (es != null && es.size() != 0) {
                AbstractElement leftElement = es.get(0);
                if (ElementTypeEnum.ROBOT_INFO == leftElement.elementType) {
                    FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) leftElement;
                    if (!nowName.equals(fightRobotBaseInfo.name)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 根据血量，决定疯狂杀戮true还是存活机制false
     * @return
     */
    private boolean isCrazyKill(FightRobotBaseInfo fightRobotBaseInfoTarget) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        FightRobotBaseInfo fightRobotBaseInfoNow = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        int bloodNumNow = fightRobotBaseInfoNow.bloodNum;
        int bloodNumTar = fightRobotBaseInfoTarget.bloodNum;
        if (bloodNumNow > bloodNumTar + 2) {
            return true;
        }
        return false;
    }

    /**
     * 自动分配下一个方向，且不出界的且避开雷且预险安全的，如果没法避开雷，分配一个不出界的且预险安全的，如果没有预险安全的分配一个不出界的
     * @param moveActionCommand
     * @param currentLocation
     * @param elementLists
     * @return
     */
    private MoveActionCommandEnum calNextAction(MoveActionCommandEnum moveActionCommand, Location currentLocation,
            ElementList[][] elementLists) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        List<MoveActionCommandEnum> allocatedMoveAction = new ArrayList<>();
        int j = 0;
        while (true) {
            j++;
            if (!allocatedMoveAction.contains(moveActionCommand)) {
                allocatedMoveAction.add(moveActionCommand);
            }
            CommonMoveAction action = new CommonMoveAction(moveActionCommand);
            // 4种方向都循环过了，再进入循环时，选择第一个未出界的走且预险安全的走
            if (j > 4 && !judgeIsOutOfArray(action, currentLocation, elementLists)) {
                if (!judgeNextLocationHasDanger(currentLocation, moveActionCommand, elementLists)) {
                    return moveActionCommand;
                }
                // 4种方向循环过了没有绝对安全的,就分配不出界的第一个就好了
                if (j > 8) {
                    return moveActionCommand;
                }
            }
            if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                    fightRobotSeeEntity.mapInfo)) {
                return moveActionCommand;
            } else {
                if (!allocatedMoveAction.contains(MoveActionCommandEnum.MOVE_DOWN)) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
                } else if (!allocatedMoveAction.contains(MoveActionCommandEnum.MOVE_TOP)) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
                } else if (!allocatedMoveAction.contains(MoveActionCommandEnum.MOVE_RIGHT)) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
                } else if (!allocatedMoveAction.contains(MoveActionCommandEnum.MOVE_LEFT)) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
                }
            }
        }
    }

    class ActionResult {
        private MoveActionCommandEnum moveActionCommandEnum;

        private boolean               needContinue;

        public MoveActionCommandEnum getMoveActionCommandEnum() {
            return moveActionCommandEnum;
        }

        public void setMoveActionCommandEnum(MoveActionCommandEnum moveActionCommandEnum) {
            this.moveActionCommandEnum = moveActionCommandEnum;
        }

        public boolean isNeedContinue() {
            return needContinue;
        }

        public void setNeedContinue(boolean needContinue) {
            this.needContinue = needContinue;
        }

    }

    private ActionResult randomMoveAction(int n, int tarX, int tarY, Location currentLocation,
            ElementList[][] elementLists, FightRobotSeeEntity fightRobotSeeEntity) {
        ActionResult actionResult = new ActionResult();
        int curX = currentLocation.x;
        int curY = currentLocation.y;
        MoveActionCommandEnum moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
        if (n == 0) {
            moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
            if (curX < tarX) {
                if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                        fightRobotSeeEntity.mapInfo)) {
                    actionResult.setMoveActionCommandEnum(moveActionCommand);
                    actionResult.setNeedContinue(false);
                    return actionResult;
                }
            }
        }
        if (n == 1) {
            moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
            if (curX > tarX) {
                if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                        fightRobotSeeEntity.mapInfo)) {
                    actionResult.setMoveActionCommandEnum(moveActionCommand);
                    actionResult.setNeedContinue(false);
                    return actionResult;
                }
            }
        }
        if (n == 2) {
            moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
            if (curY < tarY) {
                if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                        fightRobotSeeEntity.mapInfo)) {
                    actionResult.setMoveActionCommandEnum(moveActionCommand);
                    actionResult.setNeedContinue(false);
                    return actionResult;
                }
            }
        }
        if (n == 3) {
            moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
            if (curY > tarY) {
                if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                        fightRobotSeeEntity.mapInfo)) {
                    actionResult.setMoveActionCommandEnum(moveActionCommand);
                    actionResult.setNeedContinue(false);
                    return actionResult;
                }
            }
        }
        actionResult.setMoveActionCommandEnum(moveActionCommand);
        actionResult.setNeedContinue(true);
        return actionResult;
    }

    /**
     * 移动方向且自动判断是否越界，自动判定避开地雷
     * @param tarX
     * @param tarY
     * @param currentLocation
     * @param elementLists
     * @return
     */
    private MoveActionCommandEnum getMoveDis(int tarX, int tarY, Location currentLocation,
            ElementList[][] elementLists) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        int curX = currentLocation.x;
        int curY = currentLocation.y;
        MoveActionCommandEnum moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
        List<Integer> randomList = new ArrayList<>();
        while (true) {
            // 产生的是0-3的随机数
            Integer ranI = new Random().nextInt(4);
            if (!randomList.contains(ranI)) {
                randomList.add(ranI);
            }
            if (randomList.size() == 4) {
                break;
            }
        }
        for (Integer n : randomList) {
            ActionResult actionResult = randomMoveAction(n, tarX, tarY, currentLocation, elementLists,
                    fightRobotSeeEntity);
            moveActionCommand = actionResult.getMoveActionCommandEnum();
            if (!actionResult.needContinue) {
                return moveActionCommand;
            }
        }
        // 可能已经在一条线上了，前方有雷，这个时候去目标点需要绕开。
        // 如果下一个点是雷，除了当前方向的水平方向不走外，随机垂直的方向
        boolean isHasLandmine = isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                fightRobotSeeEntity.mapInfo);
        if (isHasLandmine) {
            MoveActionCommandEnum moveActionCommandOld = moveActionCommand;
            // 垂直方向绕一下
            if (MoveActionCommandEnum.MOVE_LEFT == moveActionCommand
                    || MoveActionCommandEnum.MOVE_RIGHT == moveActionCommand) {
                //
                int[] nums = { 1, 3 };
                List<Integer> moves = new ArrayList<>();
                while (true) {
                    Integer randomNum = randomDesignatedNumber(nums);
                    if (!moves.contains(randomNum)) {
                        moves.add(randomNum);
                    }
                    if (moves.size() == nums.length) {
                        break;
                    }
                }
                for (int i = 0; i < moves.size(); i++) {
                    if (1 == moves.get(i)) {
                        moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
                        if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                                fightRobotSeeEntity.mapInfo)) {
                            return moveActionCommand;
                        }
                    }
                    if (3 == moves.get(i)) {
                        moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
                        if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                                fightRobotSeeEntity.mapInfo)) {
                            return moveActionCommand;
                        }
                    }
                }

                // moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
                // if (!isHasLandmine(elementLists,
                // getNewLocationByMove(moveActionCommand, currentLocation),
                // fightRobotSeeEntity.mapInfo)) {
                // return moveActionCommand;
                // }
                // moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
                // if (!isHasLandmine(elementLists,
                // getNewLocationByMove(moveActionCommand, currentLocation),
                // fightRobotSeeEntity.mapInfo)) {
                // return moveActionCommand;
                // }
                // 可能会存在三周都有雷，反向
                if (MoveActionCommandEnum.MOVE_LEFT == moveActionCommandOld) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
                    return moveActionCommand;
                }
                if (MoveActionCommandEnum.MOVE_RIGHT == moveActionCommandOld) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
                    return moveActionCommand;
                }
            }
            if (MoveActionCommandEnum.MOVE_TOP == moveActionCommand
                    || MoveActionCommandEnum.MOVE_DOWN == moveActionCommand) {
                //
                int[] nums2 = { 2, 4 };
                List<Integer> moves2 = new ArrayList<>();
                while (true) {
                    Integer randomNum = randomDesignatedNumber(nums2);
                    if (!moves2.contains(randomNum)) {
                        moves2.add(randomNum);
                    }
                    if (moves2.size() == nums2.length) {
                        break;
                    }
                }
                for (int i = 0; i < moves2.size(); i++) {
                    if (2 == moves2.get(i)) {
                        moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
                        if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                                fightRobotSeeEntity.mapInfo)) {
                            return moveActionCommand;
                        }
                    }
                    if (4 == moves2.get(i)) {
                        moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
                        if (!isHasLandmine(elementLists, getNewLocationByMove(moveActionCommand, currentLocation),
                                fightRobotSeeEntity.mapInfo)) {
                            return moveActionCommand;
                        }
                    }
                }

                // moveActionCommand = MoveActionCommandEnum.MOVE_LEFT;
                // if (!isHasLandmine(elementLists,
                // getNewLocationByMove(moveActionCommand, currentLocation),
                // fightRobotSeeEntity.mapInfo)) {
                // return moveActionCommand;
                // }
                // moveActionCommand = MoveActionCommandEnum.MOVE_RIGHT;
                // if (!isHasLandmine(elementLists,
                // getNewLocationByMove(moveActionCommand, currentLocation),
                // fightRobotSeeEntity.mapInfo)) {
                // return moveActionCommand;
                // }
                // 可能会存在三周都有雷，反向
                if (MoveActionCommandEnum.MOVE_TOP == moveActionCommandOld) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;
                    return moveActionCommand;
                }
                if (MoveActionCommandEnum.MOVE_DOWN == moveActionCommandOld) {
                    moveActionCommand = MoveActionCommandEnum.MOVE_TOP;
                    return moveActionCommand;
                }
            }
            return moveActionCommand;
        }
        return moveActionCommand;
    }

    /**
     * 指定数组随机数
     * @param nums
     * @return
     */
    private int randomDesignatedNumber(int[] nums) {
        int i = (int) (Math.random() * nums.length);
        return nums[i];
    }

    /**
     * 根据移动的行为判断下一步location
     * @param MoveActionCommandEnum
     * @param currentLocation
     * @return
     */
    @SuppressWarnings("static-access")
    private Location getNewLocationByMove(MoveActionCommandEnum moveActionCommandEnum, Location currentLocation) {
        int finalX = currentLocation.x, finalY = currentLocation.y;
        if (MoveActionCommandEnum.MOVE_DOWN == moveActionCommandEnum) {
            finalX = currentLocation.x + 1;
        }
        if (MoveActionCommandEnum.MOVE_TOP == moveActionCommandEnum) {
            finalX = currentLocation.x - 1;
        }
        if (MoveActionCommandEnum.MOVE_RIGHT == moveActionCommandEnum) {
            finalY = currentLocation.y + 1;
        }
        if (MoveActionCommandEnum.MOVE_LEFT == moveActionCommandEnum) {
            finalY = currentLocation.y - 1;
        }
        return new Location(finalX, finalY);
    }

    /**
     * 根据行为判断下一步是否出界
     * @param action
     * @param currentLocation
     * @param elementLists
     * @return
     */
    private boolean judgeIsOutOfArray(CommonMoveAction action, Location currentLocation, ElementList[][] elementLists) {
        MoveActionCommandEnum MoveActionCommandEnum = (MoveActionCommandEnum) action.getActionCommand();
        Location newLocation = getNewLocationByMove(MoveActionCommandEnum, currentLocation);
        int finalX = newLocation.x, finalY = newLocation.y;
        // if (MoveActionCommandEnum.MOVE_DOWN == MoveActionCommandEnum) {
        // finalX = currentLocation.x + 1;
        // }
        // if (MoveActionCommandEnum.MOVE_TOP == MoveActionCommandEnum) {
        // finalX = currentLocation.x - 1;
        // }
        // if (MoveActionCommandEnum.MOVE_RIGHT == MoveActionCommandEnum) {
        // finalY = currentLocation.y + 1;
        // }
        // if (MoveActionCommandEnum.MOVE_LEFT == MoveActionCommandEnum) {
        // finalY = currentLocation.y - 1;
        // }
        return (finalX >= elementLists.length || finalY >= elementLists[0].length || finalX < 0 || finalY < 0) ? true
                : false;
        // return CommonUtil.judgeIsOutOfArray(elementLists, finalX, finalY);
    }

    /**
     * 判断目标是否有雷或出界（true不能走，false能走）
     * @param elementLists
     * @param location
     * @param fightMapInfo
     * @return
     */
    private Boolean isHasLandmine(ElementList[][] elementLists, Location location, AbstractMapInfo fightMapInfo) {
        Boolean isHasLandmine = false;
        if (location.x >= fightMapInfo.size || location.y >= fightMapInfo.size || location.x < 0 || location.y < 0) {
            return true;
        }

        for (AbstractElement element : elementLists[location.x][location.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                isHasLandmine = true;
                break;
            }
        }
        return isHasLandmine;
    }

    /**
     * (|x-curX|)*(|x-curX|)+(|y-curY|)*(|y-curY|)最小的
     * @param es
     * @param currentLocation
     * @return
     */
    private AbstractElement getNearest(List<AbstractElement> es, Location currentLocation,
            List<AbstractElement> exceptEs) {
        int curX = currentLocation.x;
        int curY = currentLocation.y;
        int minDistance = 0;
        Map<String, AbstractElement> map = new HashMap<>();
        for (AbstractElement e : es) {
            int x = 0, y = 0;
            if (ElementTypeEnum.BLOOD_BAG == e.elementType) {
                BloodBag bloodBag = (BloodBag) e;
                x = bloodBag.location.x;
                y = bloodBag.location.y;
            } else if (ElementTypeEnum.LANDMINE == e.elementType) {
                Landmine landmine = (Landmine) e;
                x = landmine.location.x;
                y = landmine.location.y;
            } else if (ElementTypeEnum.ROBOT_INFO == e.elementType) {
                FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) e;
                FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
                FightRobotBaseInfo fightRobotBaseInfoNow = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
                if (fightRobotBaseInfoNow.name.equals(fightRobotBaseInfo.name)) {
                    continue;
                }
                if (exceptEs != null && exceptEs.contains(fightRobotBaseInfo) && es.size() > 2) {
                    // 机器人数目>2的时候再躲？如果只剩两个机器人，不需要躲了，直接去追着一决高下
                    // 就怕场上我血量最低还一直躲又不干，最后分数还是最低的，这边要想个策略
                    // TODO
                    continue;
                }
                x = fightRobotBaseInfo.currentLocation.x;
                y = fightRobotBaseInfo.currentLocation.y;
            }
            int xdis = 0;
            if (x > curX) {
                xdis = x - curX;
            } else {
                xdis = curX - x;
            }
            int ydis = 0;
            if (y > curY) {
                ydis = y - curY;
            } else {
                ydis = curY - y;
            }
            int dis = xdis * xdis + ydis * ydis;
            if (map.get(KEY_NEAREST) == null) {
                minDistance = dis;
                map.put(KEY_NEAREST, e);
            }
            if (dis < minDistance) {
                minDistance = dis;
                map.put(KEY_NEAREST, e);
            }
        }
        return map.get(KEY_NEAREST);
    }

    /**
     * 获取血包
     * @param elementLists
     * @return
     */
    private List<AbstractElement> getBloodBags(ElementList[][] elementLists) {
        // 当前所见的血包存到list
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

    /**
     * 获取地雷
     * @param elementLists
     * @return
     */
    private List<AbstractElement> getLandmines(ElementList[][] elementLists) {
        // 当前所见的血包存到list
        List<AbstractElement> landmines = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.LANDMINE == element.elementType) {
                        landmines.add(element);
                    }
                }
            }
        }
        return landmines;
    }

    /**
     * 获取机器人
     * @param elementLists
     * @return
     */
    private List<AbstractElement> getRobotInfos(ElementList[][] elementLists) {
        // 当前所见的血包存到list
        List<AbstractElement> robotInfos = new ArrayList<>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        robotInfos.add(element);
                    }
                }
            }
        }
        return robotInfos;
    }

}
