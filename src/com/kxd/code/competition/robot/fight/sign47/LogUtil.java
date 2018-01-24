package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.action.CommonMoveAction;

/**
 * @author mengqingyan 2018/1/11
 */
public class LogUtil {

    public static void logAction(String prefix, CommonMoveAction commonMoveAction) {
        String msg = "NULL";
        if(commonMoveAction != null) {
            msg = commonMoveAction.getActionCommand().toString();
        }
        log(prefix, msg);
    }

    public static void log(String prefix, String msg) {
//        System.out.println(prefix + ":" + msg);
    }
}
