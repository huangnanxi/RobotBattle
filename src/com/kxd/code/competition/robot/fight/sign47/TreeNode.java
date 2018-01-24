package com.kxd.code.competition.robot.fight.sign47;

import java.util.List;

/**
 * @author mengqingyan 2017/12/6
 */
public interface TreeNode {

    <T extends TreeNode> List<T> getSons();

}
