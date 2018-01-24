package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/9.
 */
public class TestMyRobot {

    public static void main(String[] args) {
//        FightRobotSeeEntity fightRobotSeeEntity = new FightRobotSeeEntity();
//
//        RobotContext robotContext = new RobotContext(fightRobotSeeEntity, null);


        Map<Location,String> m = new HashMap<>();

        Location location1 = new Location(1,10);
        m.put(location1,"hello");


        Location location2 = new Location(10,1);
        m.put(location2,"world");


        Location location3 = new Location(10,1);
        System.out.println(m.get(location1));
    }
}
