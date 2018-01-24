package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.action.CommonMoveAction;

/**
 * @author mengqingyan 2018/1/17
 */
public class Sign47CommonMoveAction extends CommonMoveAction {
    public Sign47CommonMoveAction(MoveActionCommandEnum actionCommand) {
        super(actionCommand);
    }

    private ActionTypeEnum actionTypeEnum = ActionTypeEnum.EFFECTIVE_TRACE;

    public ActionTypeEnum getActionTypeEnum() {
        return actionTypeEnum;
    }

    public void setActionTypeEnum(ActionTypeEnum actionTypeEnum) {
        this.actionTypeEnum = actionTypeEnum;
    }
}
