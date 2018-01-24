package com.kxd.code.competition.robot.fight.mvp;

import com.kxd.code.competition.constants.CommonConstant;
import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;

import java.util.Random;

public class MvpFightRobot extends AbstractFightRobot {

    public MvpFightRobot(String name) {
        super(name);
    }

    public MvpFightRobot() {
        super("mvp");
    }

    @Override
    public CommonMoveAction getNextAction() {

        // init map
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        CommonMoveAction commonMoveAction;

        MvpAction tuzhiAction = new MvpAction(fightRobotSeeEntity);

        // attack
        commonMoveAction = getAttackAction(tuzhiAction);
        if (commonMoveAction != null) {
            return commonMoveAction;
        }

        // blood
        commonMoveAction = getBloodAction(tuzhiAction);
        if (commonMoveAction != null) {
            return commonMoveAction;
        }

        // idle
        commonMoveAction = getIdleAction(tuzhiAction);
        if (commonMoveAction != null) {
            return commonMoveAction;
        }

        // not attacked
        commonMoveAction = getNotAttackedAction(tuzhiAction);
        if (commonMoveAction != null) {
            return commonMoveAction;
        }

        // forced
        commonMoveAction = getForcedAction(tuzhiAction);
        if (commonMoveAction != null) {
            return commonMoveAction;
        }

        // random
        MoveActionCommandEnum moveActionCommand = null;
        moveActionCommand = generateNoLandmineAction();

        commonMoveAction = new CommonMoveAction(moveActionCommand);

        return commonMoveAction;
    }

    private CommonMoveAction getAttackAction(MvpAction TuzhiAction) {
        return TuzhiAction.moveActionForAttack();
    }

    private CommonMoveAction getBloodAction(MvpAction TuzhiAction) {
        return TuzhiAction.moveActionForBlood();
    }

    private CommonMoveAction getIdleAction(MvpAction TuzhiAction) {
        return TuzhiAction.moveActionIdle();
    }

    private CommonMoveAction getNotAttackedAction(MvpAction TuzhiAction) {
        return TuzhiAction.moveActionNotAttacked();
    }

    private CommonMoveAction getForcedAction(MvpAction TuzhiAction) {
        return TuzhiAction.moveActionForced();
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
