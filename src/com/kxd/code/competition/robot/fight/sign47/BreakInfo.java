package com.kxd.code.competition.robot.fight.sign47;

import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mengqingyan on 2018/1/20.
 */
public class BreakInfo {
    private Location breakPointLoc;


    private List<BreakDetailInfo> bloodBagInfos = new ArrayList<>();

    public Location getBreakPointLoc() {
        return breakPointLoc;
    }

    public void setBreakPointLoc(Location breakPointLoc) {
        this.breakPointLoc = breakPointLoc;
    }

    public void addBloodBagInfo(BreakDetailInfo breakDetailInfo) {
        this.bloodBagInfos.add(breakDetailInfo);
    }

    public void clearBloodBagLocationSet() {
        this.bloodBagInfos.clear();
    }

    public List<BreakDetailInfo> getBloodBagInfos() {
        return bloodBagInfos;
    }

    public MaxBenifit getMaxBenift(RobotContext robotContext) {
        MaxBenifit maxBenifit = new MaxBenifit();
        int maxBenifitV = Integer.MIN_VALUE;
        BreakDetailInfo maxBenifitBloodInfo = null;
        for (BreakDetailInfo breakDetailInfo :bloodBagInfos) {
            int thisBenifit = breakDetailInfo.getAvailableBloodNum() - breakDetailInfo.getLandMineNumToBreak();
            List<FightRobotBaseInfo> enemies = robotContext.getEnemies(breakDetailInfo.bloodLoc);
            if(enemies.size() > 0) {
                FightRobotBaseInfo maxBlood = robotContext.getMaxBlood(enemies);
                if(robotContext.canAttackGambleForWildWarWithEnemy(maxBlood, 5, 22)) {
                    thisBenifit += (robotContext.currentFightRobotBaseInfo.bloodNum-maxBlood.bloodNum)/2;
                }
            }

            if(thisBenifit > maxBenifitV) {
                maxBenifitV = thisBenifit;
                maxBenifitBloodInfo = breakDetailInfo;
            }
        }
        maxBenifit.setBenifit(maxBenifitV);
        maxBenifit.setBestBreakDetailInfo(maxBenifitBloodInfo);
        return maxBenifit;
    }

    public static class MaxBenifit {
        private int benifit = Integer.MIN_VALUE;
        private BreakDetailInfo bestBreakDetailInfo;

        public int getBenifit() {
            return benifit;
        }

        public void setBenifit(int benifit) {
            this.benifit = benifit;
        }

        public BreakDetailInfo getBestBreakDetailInfo() {
            return bestBreakDetailInfo;
        }

        public void setBestBreakDetailInfo(BreakDetailInfo bestBreakDetailInfo) {
            this.bestBreakDetailInfo = bestBreakDetailInfo;
        }
    }

    public static class BreakDetailInfo {
        private Location bloodLoc;
        private Location breakTargetLocation;
        private int availableBloodNum;
        private int landMineNumToBreak;
        private int arrivableNum;

        public Location getBreakTargetLocation() {
            return breakTargetLocation;
        }

        public void setBreakTargetLocation(Location breakTargetLocation) {
            this.breakTargetLocation = breakTargetLocation;
        }

        public int getArrivableNum() {
            return arrivableNum;
        }

        public void setArrivableNum(int arrivableNum) {
            this.arrivableNum = arrivableNum;
        }

        public Location getBloodLoc() {
            return bloodLoc;
        }

        public void setBloodLoc(Location bloodLoc) {
            this.bloodLoc = bloodLoc;
        }

        public int getAvailableBloodNum() {
            return availableBloodNum;
        }

        public void setAvailableBloodNum(int availableBloodNum) {
            this.availableBloodNum = availableBloodNum;
        }

        public int getLandMineNumToBreak() {
            return landMineNumToBreak;
        }

        public void setLandMineNumToBreak(int landMineNumToBreak) {
            this.landMineNumToBreak = landMineNumToBreak;
        }
    }
}
