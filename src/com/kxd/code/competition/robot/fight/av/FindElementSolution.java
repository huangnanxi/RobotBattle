package com.kxd.code.competition.robot.fight.av;

import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.element.AbstractElement;

import java.util.ArrayList;
import java.util.List;

public class FindElementSolution <T extends AbstractElement> implements Comparable<FindElementSolution<T>> {

    protected T t;

    protected Integer distance = 0;

    protected Integer rank = 0;

    protected List<MoveActionCommandEnum> path = new ArrayList<>();

    @Override
    public int compareTo(FindElementSolution o) {
        if (null == o) {
            return 1;
        } else {
            if (this.rank < o.rank) {
                return -1;
            } else if (this.rank > o.rank) {
                return 1;
            } else {
                if (this.distance < o.distance) {
                    return -1;
                } else if (this.distance > o.distance) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
