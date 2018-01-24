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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by old boy on 2018/01/18.
 */
public class OldBoyFightRobot extends AbstractFightRobot {

    public OldBoyFightRobot() {
        super("达人");
    }

    @Override
    public CommonMoveAction getNextAction() {

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;

        // 当前所见的血包存到list
        int myrobotlocation = fightRobotSeeEntity.robotBaseInfo.currentLocation.x
                * fightRobotSeeEntity.robotBaseInfo.currentLocation.y;
        int minLength = 0;
        int count = 0;
        BloodBag bloodBagminLength = null;
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        BloodBag bloodBag = (BloodBag) element;
                        int targetX = bloodBag.location.x;
                        int targetY = bloodBag.location.y;

                        int tabloodbag = targetX * targetY;

                        if (count == 0) {
                            minLength = tabloodbag - myrobotlocation;
                            if (minLength < 0) {
                                minLength = 0 - minLength;
                            }
                            bloodBagminLength = bloodBag;
                        } else {
                            int tempminLength = tabloodbag - myrobotlocation;
                            if (tempminLength < 0) {
                                tempminLength = 0 - tempminLength;
                            }

                            if (tempminLength < minLength) {
                                minLength = tempminLength;
                                bloodBagminLength = bloodBag;
                            }
                        }
                        count++;
                    }

                }
            }
        }

        MoveActionCommandEnum moveActionCommand = null;
        if (bloodBagminLength != null) {

            int targetX = bloodBagminLength.location.x;
            int targetY = bloodBagminLength.location.y;

            moveActionCommand = getNextStep(targetX, targetY);

        } else {
            moveActionCommand = generateNoLandmineAction();
        }

        CommonMoveAction actionEntity = new CommonMoveAction(moveActionCommand);
        return actionEntity;
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
}
