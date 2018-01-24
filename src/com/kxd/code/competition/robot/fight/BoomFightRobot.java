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
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BoomFightRobot extends AbstractFightRobot {

    public BoomFightRobot() {
        super("Boom");
    }

    @Override
    public CommonMoveAction getNextAction() {
        try {
            MoveActionCommandEnum moveActionCommand = null;

            AbstractElement element = getBestBlood();
            if (element == null) {
                element = getNoneBloodBestRobot();
                if (element == null) {
                    moveActionCommand = generateNoLandmineAction();
                } else {
                    FightRobotBaseInfo robot = (FightRobotBaseInfo) element;
                    moveActionCommand = getNextStep(robot.currentLocation.x, robot.currentLocation.y);
                }
            } else {
                BloodBag bloodBag = (BloodBag) element;
                moveActionCommand = getNextStep(bloodBag.location.x, bloodBag.location.y);
            }
            return new CommonMoveAction(moveActionCommand);
        } catch (Exception e) {
            return new CommonMoveAction(generateNoLandmineAction());
        }

    }

    private MoveActionCommandEnum getNextStep(int targetX, int targetY) {

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        List<MoveActionCommandEnum> moveActionCommands = new ArrayList<>();
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        if (targetX < fightRobotSeeEntity.robotBaseInfo.currentLocation.x) {
            if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x - 1,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_TOP);
            } else if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y - 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_LEFT);
            } else if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y + 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_RIGHT);
            } else {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_DOWN);
            }
        } else if (targetX > fightRobotSeeEntity.robotBaseInfo.currentLocation.x) {
            if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x + 1,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_DOWN);
            } else if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y - 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_LEFT);
            } else if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y + 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_RIGHT);
            } else {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_TOP);
            }
        }
        if (targetY < fightRobotSeeEntity.robotBaseInfo.currentLocation.y) {
            if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y - 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_LEFT);
            } else if (!isHasLandmine(elementLists,
                    new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x - 1,
                            fightRobotSeeEntity.robotBaseInfo.currentLocation.y),
                    fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_TOP);
            } else if (!isHasLandmine(elementLists,
                    new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x + 1,
                            fightRobotSeeEntity.robotBaseInfo.currentLocation.y),
                    fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_DOWN);
            } else {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_RIGHT);
            }
        } else {
            if (!isHasLandmine(elementLists, new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x,
                    fightRobotSeeEntity.robotBaseInfo.currentLocation.y + 1), fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_RIGHT);
            } else if (!isHasLandmine(elementLists,
                    new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x - 1,
                            fightRobotSeeEntity.robotBaseInfo.currentLocation.y),
                    fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_TOP);
            } else if (!isHasLandmine(elementLists,
                    new Location(fightRobotSeeEntity.robotBaseInfo.currentLocation.x + 1,
                            fightRobotSeeEntity.robotBaseInfo.currentLocation.y),
                    fightRobotSeeEntity.mapInfo)) {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_DOWN);
            } else {
                moveActionCommands.add(MoveActionCommandEnum.MOVE_LEFT);
            }
        }
        if (targetX < fightRobotSeeEntity.robotBaseInfo.currentLocation.x
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_TOP)
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_DOWN)) {
            moveActionCommands.remove(MoveActionCommandEnum.MOVE_DOWN);
        }
        if (targetX > fightRobotSeeEntity.robotBaseInfo.currentLocation.x
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_TOP)
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_DOWN)) {
            moveActionCommands.remove(MoveActionCommandEnum.MOVE_TOP);
        }
        if (targetY < fightRobotSeeEntity.robotBaseInfo.currentLocation.y
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_TOP)
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_DOWN)) {
            moveActionCommands.remove(MoveActionCommandEnum.MOVE_RIGHT);
        }
        if (targetY > fightRobotSeeEntity.robotBaseInfo.currentLocation.y
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_TOP)
                && moveActionCommands.contains(MoveActionCommandEnum.MOVE_DOWN)) {
            moveActionCommands.remove(MoveActionCommandEnum.MOVE_LEFT);
        }
        return moveActionCommands
                .get(Math.abs(new Random(System.currentTimeMillis()).nextInt()) % (moveActionCommands.size()));
    }

    private AbstractElement getBestBlood() {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;

        int mixStep = 10000000;
        AbstractElement targetBlood = null;
        List<AbstractElement> list = new ArrayList<AbstractElement>();
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        BloodBag bloodBag = (BloodBag) element;
                        // 查找自己是否是某个血包的最近机器人
                        ElementList[][] elementLists2 = fightRobotSeeEntity.robotGetElementList;
                        int mixStep2 = 10000000;
                        AbstractElement targetRobot = null;
                        for (int k = 0; k < elementLists2.length; k++) {
                            for (int l = 0; l < elementLists2.length; l++) {
                                ElementList elementList2 = elementLists2[k][l];
                                for (AbstractElement element2 : elementList2.elements) {
                                    if (ElementTypeEnum.ROBOT_INFO == element2.elementType) {
                                        FightRobotBaseInfo robot = (FightRobotBaseInfo) element2;
                                        int targetX = robot.currentLocation.x;
                                        int targetY = robot.currentLocation.y;
                                        int currentX = bloodBag.location.x;
                                        int currentY = bloodBag.location.y;
                                        if (Math.abs(targetX - currentX) + Math.abs(targetY - currentY) < mixStep2) {
                                            mixStep2 = Math.abs(targetX - currentX) + Math.abs(targetY - currentY);
                                            targetRobot = element2;
                                        }
                                    }
                                }
                            }
                        }
                        if (targetRobot != null) {
                            FightRobotBaseInfo nearestRobot = (FightRobotBaseInfo) targetRobot;
                            if (nearestRobot.currentLocation
                                    .equals(fightRobotSeeEntity.robotBaseInfo.currentLocation)) {
                                list.add(element);
                            }
                        }
                        if (list == null || list.size() == 0) {
                            // 如果自己不是任何血包的最近机器人，则找距离自己最近的血包
                            int targetX = bloodBag.location.x;
                            int targetY = bloodBag.location.y;
                            int currentX = fightRobotSeeEntity.robotBaseInfo.currentLocation.x;
                            int currentY = fightRobotSeeEntity.robotBaseInfo.currentLocation.y;
                            if (Math.abs(targetX - currentX) + Math.abs(targetY - currentY) < mixStep) {
                                mixStep = Math.abs(targetX - currentX) + Math.abs(targetY - currentY);
                                targetBlood = element;
                            }
                        } else {
                            Iterator<AbstractElement> it = list.iterator();
                            int shortestStep = 1000000000;
                            AbstractElement nearestBlood = null;
                            while (it.hasNext()) {
                                BloodBag ae = (BloodBag) it.next();
                                int targetX = ae.location.x;
                                int targetY = ae.location.y;
                                int currentX = fightRobotSeeEntity.robotBaseInfo.currentLocation.x;
                                int currentY = fightRobotSeeEntity.robotBaseInfo.currentLocation.y;
                                if (Math.abs(targetX - currentX) + Math.abs(targetY - currentY) < shortestStep) {
                                    shortestStep = Math.abs(targetX - currentX) + Math.abs(targetY - currentY);
                                    nearestBlood = element;
                                }
                            }
                            return (BloodBag) nearestBlood;
                        }
                    }
                }
            }
        }
        return targetBlood;
    }

    private AbstractElement getNoneBloodBestRobot() {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        AbstractElement targetRobot = null;
        int mixStep = 10000000;
        for (int k = 0; k < elementLists.length; k++) {
            for (int l = 0; l < elementLists.length; l++) {
                ElementList elementList = elementLists[k][l];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        FightRobotBaseInfo robot = (FightRobotBaseInfo) element;
                        int targetX = robot.currentLocation.x;
                        int targetY = robot.currentLocation.y;
                        int currentX = fightRobotSeeEntity.robotBaseInfo.currentLocation.x;
                        int currentY = fightRobotSeeEntity.robotBaseInfo.currentLocation.y;
                        int step = Math.abs(targetX - currentX) + Math.abs(targetY - currentY);
                        if (step % 2 == 1) {
                            if (Math.abs(targetX - currentX) + Math.abs(targetY - currentY) < mixStep) {
                                mixStep = Math.abs(targetX - currentX) + Math.abs(targetY - currentY);
                                targetRobot = element;
                            }
                        }

                    }
                }
            }
        }
        return targetRobot;
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

}
