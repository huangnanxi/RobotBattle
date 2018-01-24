package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.CommonConstant;
import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.AbstractRobotBaseInfo;

import java.util.Random;

/**
 * Created by huangnx on 2017/12/21.
 */
public class UnverseFightRobot extends AbstractFightRobot {

    public UnverseFightRobot() {
        super("unverse");
    }

    private static BloodBag              lastBloodBag     = null;

    private static MoveActionCommandEnum befMoveActionCmd = null;

    private int                          distance         = 0;

    @Override
    public CommonMoveAction getNextAction() {
        // Random random = new Random(System.currentTimeMillis());

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;

        // AbstractElement targetElement = null;

        MoveActionCommandEnum moveActionCommand = null;
        // suncq----------begin---------
        moveActionCommand = aroundBloodOrBotor(elementLists, currentLocation.x, currentLocation.y);
        if (moveActionCommand != null) {
            befMoveActionCmd = moveActionCommand;
            return new CommonMoveAction(moveActionCommand);
        }
        lastBloodBag = getBloodBag(elementLists, currentLocation.x, currentLocation.y, lastBloodBag);

        if (lastBloodBag != null) {

            if (Math.abs(lastBloodBag.location.x - currentLocation.x) > Math
                    .abs(lastBloodBag.location.y - currentLocation.y)) {
                if (lastBloodBag.location.x > currentLocation.x && isSecurity(elementLists, currentLocation.x + 1,
                        currentLocation.y, MoveActionCommandEnum.MOVE_DOWN)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_DOWN;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                } else if (lastBloodBag.location.x < currentLocation.x && isSecurity(elementLists,
                        currentLocation.x - 1, currentLocation.y, MoveActionCommandEnum.MOVE_TOP)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_TOP;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                } else if (lastBloodBag.location.y > currentLocation.y && isSecurity(elementLists, currentLocation.x,
                        currentLocation.y + 1, MoveActionCommandEnum.MOVE_RIGHT)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_RIGHT;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                } else if (lastBloodBag.location.y < currentLocation.y && isSecurity(elementLists, currentLocation.x,
                        currentLocation.y - 1, MoveActionCommandEnum.MOVE_LEFT)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_LEFT;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                }

            } else {
                if (lastBloodBag.location.y > currentLocation.y && isSecurity(elementLists, currentLocation.x,
                        currentLocation.y + 1, MoveActionCommandEnum.MOVE_RIGHT)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_RIGHT;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                } else if (lastBloodBag.location.y < currentLocation.y && isSecurity(elementLists, currentLocation.x,
                        currentLocation.y - 1, MoveActionCommandEnum.MOVE_LEFT)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_LEFT;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                } else if (lastBloodBag.location.x > currentLocation.x && isSecurity(elementLists,
                        currentLocation.x + 1, currentLocation.y, MoveActionCommandEnum.MOVE_DOWN)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_DOWN;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                } else if (lastBloodBag.location.x < currentLocation.x && isSecurity(elementLists,
                        currentLocation.x - 1, currentLocation.y, MoveActionCommandEnum.MOVE_TOP)) {
                    befMoveActionCmd = MoveActionCommandEnum.MOVE_TOP;
                    return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                }
            }

        } else {
            AbstractRobotBaseInfo robot = getRobot(elementLists, currentLocation.x, currentLocation.y);
            if (robot != null) {
                if (Math.abs(robot.currentLocation.x - currentLocation.x) > Math
                        .abs(robot.currentLocation.y - currentLocation.y)) {
                    if (robot.currentLocation.x > currentLocation.x
                            && !MoveActionCommandEnum.MOVE_TOP.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x + 1, currentLocation.y,
                                    MoveActionCommandEnum.MOVE_DOWN)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_DOWN;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                    } else if (robot.currentLocation.x < currentLocation.x
                            && !MoveActionCommandEnum.MOVE_DOWN.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x - 1, currentLocation.y,
                                    MoveActionCommandEnum.MOVE_TOP)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_TOP;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                    } else if (robot.currentLocation.y > currentLocation.y
                            && !MoveActionCommandEnum.MOVE_LEFT.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x, currentLocation.y + 1,
                                    MoveActionCommandEnum.MOVE_RIGHT)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_RIGHT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                    } else if (robot.currentLocation.y < currentLocation.y
                            && !MoveActionCommandEnum.MOVE_RIGHT.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x, currentLocation.y - 1,
                                    MoveActionCommandEnum.MOVE_LEFT)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_LEFT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                    }

                } else {
                    if (robot.currentLocation.y > currentLocation.y
                            && !MoveActionCommandEnum.MOVE_LEFT.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x, currentLocation.y + 1,
                                    MoveActionCommandEnum.MOVE_RIGHT)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_RIGHT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                    } else if (robot.currentLocation.y < currentLocation.y
                            && !MoveActionCommandEnum.MOVE_RIGHT.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x, currentLocation.y - 1,
                                    MoveActionCommandEnum.MOVE_LEFT)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_LEFT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                    } else if (robot.currentLocation.x > currentLocation.x
                            && !MoveActionCommandEnum.MOVE_TOP.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x + 1, currentLocation.y,
                                    MoveActionCommandEnum.MOVE_DOWN)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_DOWN;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                    } else if (robot.currentLocation.x < currentLocation.x
                            && !MoveActionCommandEnum.MOVE_DOWN.getCode().equals(befMoveActionCmd.getCode())
                            && isSecurity(elementLists, currentLocation.x - 1, currentLocation.y,
                                    MoveActionCommandEnum.MOVE_TOP)) {
                        befMoveActionCmd = MoveActionCommandEnum.MOVE_TOP;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                    }
                }

            }
        }

        moveActionCommand = generateNoLandmineAction();
        befMoveActionCmd = moveActionCommand;
        return new CommonMoveAction(moveActionCommand);

        // end-----------------

    }

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

    private MoveActionCommandEnum generateNoLandmineAction() {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;

        Random random = new Random(System.currentTimeMillis());

        MoveActionCommandEnum moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;

        int tryNum = 0;
        while (true) {
            tryNum++;

            int index = Math.abs(random.nextInt()) % 4;
            moveActionCommand = CommonConstant.directCodes.get(index);

            Integer nextX = currentLocation.x + CommonConstant.directXY.get(index);
            Integer nextY = currentLocation.y + CommonConstant.directXY.get(index + 1);
            if (!isHasLandmine(elementLists, new Location(nextX, nextY), fightRobotSeeEntity.mapInfo) || tryNum == 10) {
                break;
            }
        }

        return moveActionCommand;
    }

    // 根据自己位置获取最近坦克
    private AbstractRobotBaseInfo getRobot(ElementList[][] elementLists, int x, int y) {
        AbstractRobotBaseInfo robot = null;

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        AbstractRobotBaseInfo tmpRobot = (AbstractRobotBaseInfo) element;
                        if (robot == null && tmpRobot.currentLocation.x != x && tmpRobot.currentLocation.y != y) {
                            robot = (AbstractRobotBaseInfo) element;
                        } else {
                            if (tmpRobot.currentLocation.x != x && tmpRobot.currentLocation.y != y
                                    && ((Math.abs(x - tmpRobot.currentLocation.x) + Math.abs(
                                            y - tmpRobot.currentLocation.y)) < (Math.abs(x - robot.currentLocation.x)
                                                    + Math.abs(y - robot.currentLocation.y)))) {
                                robot = tmpRobot;
                            }
                        }
                    }
                }
            }
        }

        return robot;
    }

    // 根据自己位置获取最近血包
    private BloodBag getBloodBag(ElementList[][] elementLists, int x, int y, BloodBag lastBloodBag) {
        BloodBag bloodBag = null;
        if (lastBloodBag != null) {
            for (AbstractElement element : elementLists[lastBloodBag.location.x][lastBloodBag.location.y].elements) {
                if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return lastBloodBag;
                }
            }
        }

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        if (bloodBag == null) {
                            bloodBag = (BloodBag) element;
                        } else {
                            BloodBag tmpBlood = (BloodBag) element;
                            if ((Math.abs(tmpBlood.location.x - x)
                                    + Math.abs(tmpBlood.location.y - y)) < (Math.abs(bloodBag.location.x - x)
                                            + Math.abs(bloodBag.location.y - y))) {
                                bloodBag = tmpBlood;
                            }
                        }
                    }
                }
            }
        }

        return bloodBag;
    }

    // 根据自己位置获取最近血包
    private BloodBag getBloodBagGt(ElementList[][] elementLists, int x, int y, int stepGt) {
        BloodBag bloodBag = null;

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                if ((Math.abs(i - x) + Math.abs(j - y)) < stepGt) {
                    continue;
                }

                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        BloodBag tmpBlood = (BloodBag) element;
                        if (bloodBag == null
                                && (Math.abs(tmpBlood.location.x - x) + Math.abs(tmpBlood.location.y - y)) > stepGt) {
                            bloodBag = (BloodBag) element;
                        } else {

                            if ((Math.abs(tmpBlood.location.x - x)
                                    + Math.abs(tmpBlood.location.y - y)) < (Math.abs(bloodBag.location.x - x)
                                            + Math.abs(bloodBag.location.y - y))
                                    && (Math.abs(tmpBlood.location.x - x)
                                            + Math.abs(tmpBlood.location.y - y)) > stepGt) {
                                bloodBag = tmpBlood;
                            }
                        }
                    }
                }
            }
        }

        return bloodBag;
    }

    // 根据血包附近距离小于myStep有没有坦克
    private boolean hasAroundBotorByBlood(ElementList[][] elementLists, int x, int y, int myStep) {
        boolean hasBotor = false;

        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                if ((Math.abs(i - x) + Math.abs(j - y)) > myStep) {
                    continue;
                }

                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        BloodBag tmpBlood = (BloodBag) element;
                        if ((Math.abs(tmpBlood.location.x - x) + Math.abs(tmpBlood.location.y - y)) < myStep) {
                            hasBotor = true;
                        }
                    }
                }
            }
        }

        return hasBotor;
    }

    // 两个目标之间距离
    private int getRange(int sx, int sy, int tx, int ty) {
        return Math.abs(sx - tx) + Math.abs(sy - ty);
    }

    // 周围是否有血包或坦克
    private MoveActionCommandEnum aroundBloodOrBotor(ElementList[][] elementLists, int x, int y) {
        MoveActionCommandEnum moveActionCommand = null;
        if (x - 1 > 0) {
            for (AbstractElement element : elementLists[x - 1][y].elements) {
                if (ElementTypeEnum.ROBOT_INFO == element.elementType
                        || ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return MoveActionCommandEnum.MOVE_TOP;
                }
            }

        }
        if (x + 1 < elementLists.length) {
            for (AbstractElement element : elementLists[x + 1][y].elements) {
                if (ElementTypeEnum.ROBOT_INFO == element.elementType
                        || ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return MoveActionCommandEnum.MOVE_DOWN;
                }
            }
        }

        if (y - 1 > 0) {
            for (AbstractElement element : elementLists[x][y - 1].elements) {
                if (ElementTypeEnum.ROBOT_INFO == element.elementType
                        || ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return MoveActionCommandEnum.MOVE_LEFT;
                }
            }

        }
        if (y + 1 < elementLists.length) {
            for (AbstractElement element : elementLists[x][y + 1].elements) {
                if (ElementTypeEnum.ROBOT_INFO == element.elementType
                        || ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return MoveActionCommandEnum.MOVE_RIGHT;
                }
            }
        }
        return moveActionCommand;
    }

    private boolean isSecurity(ElementList[][] elementLists, int x, int y, MoveActionCommandEnum moveActionCommand) {
        // 移动方是雷不能移动
        for (AbstractElement element : elementLists[x][y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                return false;
            }
        }

        boolean security = true;
        // 移动后紧靠着坦克不能移动
        if (MoveActionCommandEnum.MOVE_TOP.getCode().equals(moveActionCommand.getCode())
                || MoveActionCommandEnum.MOVE_DOWN.getCode().equals(moveActionCommand.getCode())) {
            if ((y - 1) >= 0) {
                for (AbstractElement element : elementLists[x][y - 1].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if ((y + 1) < elementLists.length) {
                for (AbstractElement element : elementLists[x][y + 1].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if (MoveActionCommandEnum.MOVE_TOP.getCode().equals(moveActionCommand.getCode()) && (x - 1) > 0) {
                for (AbstractElement element : elementLists[x - 1][y].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if (MoveActionCommandEnum.MOVE_DOWN.getCode().equals(moveActionCommand.getCode())
                    && (x + 1) < elementLists.length) {
                for (AbstractElement element : elementLists[x + 1][y].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
        }

        if (MoveActionCommandEnum.MOVE_LEFT.getCode().equals(moveActionCommand.getCode())
                || MoveActionCommandEnum.MOVE_RIGHT.getCode().equals(moveActionCommand.getCode())) {
            if ((x - 1) >= 0) {
                for (AbstractElement element : elementLists[x - 1][y].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if ((x + 1) < elementLists.length) {
                for (AbstractElement element : elementLists[x + 1][y].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if (MoveActionCommandEnum.MOVE_LEFT.getCode().equals(moveActionCommand.getCode()) && (y - 1) > 0) {
                for (AbstractElement element : elementLists[x][y - 1].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
            if (MoveActionCommandEnum.MOVE_RIGHT.getCode().equals(moveActionCommand.getCode())
                    && (y + 1) < elementLists.length) {
                for (AbstractElement element : elementLists[x][y + 1].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        return false;
                    }
                }
            }
        }

        return security;
    }
}
