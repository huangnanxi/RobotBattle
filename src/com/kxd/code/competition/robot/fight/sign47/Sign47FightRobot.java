package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;

/**
 * v0.6
 * Created by mengqingyan.
 */
public class Sign47FightRobot extends AbstractFightRobot {

    private RobotAttackedInfo robotAttackedInfo = loadRobotAttackedInfo();

    private RobotAttackedInfo loadRobotAttackedInfo() {
        return new RobotAttackedInfo();
    }

    public Sign47FightRobot() {
        this("代号47");
    }

    public Sign47FightRobot(String name) {
        super(name);
    }

    @Override
    public CommonMoveAction getNextAction() {

        CommonMoveAction commonMoveAction = doGetNextAction();
//        LogUtil.logAction(this.name, commonMoveAction);
        return commonMoveAction;
    }

    private boolean hasBlood = false;
    private CommonMoveAction doGetNextAction() {
        CommonMoveAction commonMoveAction;
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;

        RobotContext robotContext = null;
        try {
            robotAttackedInfo.increaseRound();
            robotContext = new RobotContext(fightRobotSeeEntity, robotAttackedInfo);

            commonMoveAction = getActionWhenProperlyAttacked(robotContext);
            if (commonMoveAction != null) {
                return commonMoveAction;
            }

            commonMoveAction = getAttackAction(robotContext);
            if (commonMoveAction != null) {
                return commonMoveAction;
            }
//            LogUtil.log("getBloodAction","begin");
            commonMoveAction = getBloodAction(robotContext);
//            LogUtil.logAction("getBloodAction",commonMoveAction);
            if (commonMoveAction != null) {
                hasBlood = true;
                this.robotAttackedInfo.setTraceRobotName(null);
                return commonMoveAction;
            }
            if(hasBlood) {
                this.robotAttackedInfo.addBloodRound();
            }

            hasBlood = false;


//            LogUtil.log("getBlockAction", "begin");
            commonMoveAction = getBlockAction(robotContext);
//            LogUtil.logAction("getBlockAction", commonMoveAction);
            Sign47CommonMoveAction sign47CommonMoveAction = null;
            if (commonMoveAction != null) {
                sign47CommonMoveAction = (Sign47CommonMoveAction) commonMoveAction;
                LogUtil.log("getBlockAction", sign47CommonMoveAction.getActionTypeEnum().name() + "|" + sign47CommonMoveAction.getActionCommand().toString());
                if(!ActionTypeEnum.INEFFECTIVE_TRACE.equals(sign47CommonMoveAction.getActionTypeEnum())) {
                    return sign47CommonMoveAction;
                }
            }
            LogUtil.log("getBreakAction", "begin");
            commonMoveAction = getBreakAction(robotContext);
            LogUtil.logAction("getBreakAction", commonMoveAction);
            if (commonMoveAction != null) {
                return commonMoveAction;
            } else if(sign47CommonMoveAction != null){
                return sign47CommonMoveAction;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.log("getForcedActionEx", "begin");
            commonMoveAction = getForcedAction(robotContext);
            LogUtil.logAction("getForcedActionEx", commonMoveAction);
            return commonMoveAction;
        }
//        LogUtil.log(this.getName() + ":getForcedAction","begin");
        commonMoveAction = getForcedAction(robotContext);
//        LogUtil.logAction("getForcedAction",commonMoveAction);
        return commonMoveAction;
    }

    private CommonMoveAction getBreakAction(RobotContext robotContext) {
        CommonMoveAction commonMoveAction = robotContext.bestMoveActionForBreak();

        return commonMoveAction;
    }

    private CommonMoveAction getActionWhenProperlyAttacked(RobotContext robotContext) {
        CommonMoveAction commonMoveAction = robotContext.bestMoveActionWhenProperlyAttacked();

        return commonMoveAction;
    }

    private CommonMoveAction getAttackAction(RobotContext robotContext) {
        CommonMoveAction commonMoveAction = robotContext.bestMoveActionForAttack();

        return commonMoveAction;
    }

    private CommonMoveAction getBloodAction(RobotContext robotContext) {

        return robotContext.bestMoveActionForBlood();
    }

    private Sign47CommonMoveAction getBlockAction(RobotContext robotContext) {
        return robotContext.bestMoveActionForBlock();
    }

    private CommonMoveAction getForcedAction(RobotContext robotContext) {
        return robotContext.bestMoveActionForForced();
    }
}
