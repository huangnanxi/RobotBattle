package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.io.Serializable;
import java.util.*;

/**
 * @author mengqingyan 2018/1/12
 */
public class RobotAttackedInfo implements Serializable {

    private static final long     serialVersionUID        = -8522388140898120025L;

    private Set<String>           blackRobotSet           = new HashSet<>();

    private transient int         round                   = 0;

    private transient int         myAttackRound;

    private transient Set<String> myAttackRoundRobotNames = new HashSet<>();

    private transient Location    attackedLocation;

    private transient int attackedRound;

    private int robotInitSize = 0;

    private int maxScore = 0;

    private String traceRobotName;
    private Location breakPointLoc;

    private int bloodRound = 0;

    private Deque<Location> tracedLocations = new ArrayDeque<>();

    private boolean fightRound = false;

    public boolean isFightRound() {
        return fightRound;
    }

    public void setFightRound(boolean fightRound) {
        this.fightRound = fightRound;
    }

    public void addTracedLocation(Location traceLocation) {
        if(tracedLocations.size() >= 2) {
            tracedLocations.pollLast();
        }
        tracedLocations.offerFirst(traceLocation);
    }

    public boolean containsTraceLoc(Location traceLocation) {
        return tracedLocations.contains(traceLocation);
    }

    public int getBloodRound() {
        return bloodRound;
    }

    public void addBloodRound() {
        this.bloodRound++;
    }

    public Location getBreakPointLoc() {
        return breakPointLoc;
    }

    public void setBreakPointLoc(Location breakPointLoc) {
        this.breakPointLoc = breakPointLoc;
    }

    public String getTraceRobotName() {
        return traceRobotName;
    }

    public void setTraceRobotName(String traceRobotName) {
        this.traceRobotName = traceRobotName;
    }

    public int getRobotInitSize() {
        return robotInitSize;
    }

    public void setRobotInitSize(int robotInitSize) {
        this.robotInitSize = robotInitSize;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public Set<String> getBlackRobotSet() {
        return blackRobotSet;
    }

    public boolean isBlackRobot(FightRobotBaseInfo fightRobotBaseInfo) {
        return this.blackRobotSet.contains(fightRobotBaseInfo.name);
    }

    public void setBlackRobotSet(Set<String> blackRobotSet) {
        this.blackRobotSet = blackRobotSet;
    }

    public void addBlackRobot(String robotName) {
        this.blackRobotSet.add(robotName);
    }

    public Location getAttackedLocation() {
        return attackedLocation;
    }

    public void setAttackedLocation(Location attackedLocation) {
        this.attackedLocation = attackedLocation;
        this.attackedRound = this.round;
    }

    public void increaseRound() {
        this.round = this.round + 1;
    }

    public int getRound() {
        return round;
    }

    public int getMyAttackRound() {
        return myAttackRound;
    }

    private void markMyAttackRound() {
        this.myAttackRound = this.round;
    }

    public Set<String> getMyAttackRoundRobotNames() {
        return myAttackRoundRobotNames;
    }

    public void setMyAttackRoundRobots(List<FightRobotBaseInfo> myAttackRoundRobots) {
        this.myAttackRoundRobotNames.clear();
        for (FightRobotBaseInfo fightRobotBaseInfo : myAttackRoundRobots) {
            this.myAttackRoundRobotNames.add(fightRobotBaseInfo.name);
        }
        this.markMyAttackRound();
    }

    public boolean isAttackByMeLastRound(FightRobotBaseInfo enemy) {
        if ((this.getMyAttackRound() == this.getRound() - 1) && myAttackRoundRobotNames.contains(enemy.name)) {
            return true;
        }
        return false;

    }
}
