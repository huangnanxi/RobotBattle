package com.kxd.code.competition.robot.fight.sign47;

import java.util.ArrayList;
import java.util.List;

import com.kxd.code.competition.entity.Location;

/**
 * @author mengqingyan 2018/1/9
 */
public class Sign47Location extends Location implements TreeNode {

    private RobotContext         robotContext;

    private int                  step = 0;

    private List<Sign47Location> sign47LocationSons;


    public Sign47Location(int x, int y, RobotContext robotContext) {
        super(x, y);
        this.robotContext = robotContext;
        this.sign47LocationSons = new ArrayList<>();
    }

    public void reset(RobotContext robotContext) {
        this.robotContext = robotContext;
        this.setStep(0);
        this.sign47LocationSons = new ArrayList<>();
    }

    public void resetSon() {
        this.sign47LocationSons = new ArrayList<>();
    }

    @Override
    public List<Sign47Location> getSons() {
        if (!sign47LocationSons.isEmpty()) {
            return sign47LocationSons;
        }
        List<Location> noLandMineAroundLocations = this.robotContext.getNoLandMineAroundLocations(this);

        for (Location noLandMineAroundLocation : noLandMineAroundLocations) {
            Sign47Location sign47Location = this.robotContext.getSign47Location(noLandMineAroundLocation.x,
                    noLandMineAroundLocation.y, RobotContext.locations);

            if (sign47Location.getStep() == this.getStep() - 1) {
                sign47LocationSons.add(sign47Location);
            }
        }

        return sign47LocationSons;
    }

    public int getStep() {
        return step;
    }

    private int getNextStep() {
        return (step + 1);
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void addStep(int step) {
        this.step = this.step + step;
    }

}
