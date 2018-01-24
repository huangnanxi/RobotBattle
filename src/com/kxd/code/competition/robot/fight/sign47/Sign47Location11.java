package com.kxd.code.competition.robot.fight.sign47;//package com.kxd.code.competition.robot.fight.sign47;
//
//import com.kxd.code.competition.entity.Location;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author mengqingyan 2018/1/9
// */
//public class Sign47Location11 extends Location implements TreeNode {
//
//    private RobotContext                robotContext;
//
//    private int                         step = 0;
//
//    private List<Sign47Location11>        sign47LocationSons;
//
//    private List<Sign47Location11>        sign47LocationParents;
//
//    public Sign47Location11(int x, int y, RobotContext robotContext) {
//        super(x, y);
//        this.robotContext = robotContext;
//        this.sign47LocationParents = new ArrayList<>();
//    }
//
//    public void reset(RobotContext robotContext) {
//        this.robotContext = robotContext;
//        this.setStep(0);
//        this.sign47LocationSons = null;
//        this.sign47LocationParents = new ArrayList<>();
//    }
//
//    @Override
//    public List<Sign47Location11> getSons() {
//        if (sign47LocationSons != null) {
//            return sign47LocationSons;
//        }
//        sign47LocationSons = new ArrayList<>();
//        List<Location> safeAroundLocations = this.robotContext.getSafeAroundLocations(this);
//        for (Location safeAroundLocation : safeAroundLocations) {
//            Sign47Location11 sign47Location = this.robotContext.getSign47Location(safeAroundLocation.x,
//                    safeAroundLocation.y);
//
//            if (isParent(sign47Location)) {
//                continue;
//            }
//            List<Sign47Location11> sign47LocationParents = sign47Location.getParents();
//            if(sign47LocationParents.isEmpty()) {
//                sign47Location.addParent(this);
//                sign47LocationSons.add(sign47Location);
//                continue;
//            }
//            List<Sign47Location11> sign47LocationParentsToRemove = new ArrayList<>();
//            List<Sign47Location11> sign47LocationParentsToAdd = new ArrayList<>();
//            System.out.println("for before");
//            for (Sign47Location11 sign47LocationParent : sign47LocationParents) {
//
//                if (sign47LocationParent.getStep() > this.getStep()) {
//                    System.out.println("removeSon before");
//                    sign47LocationParent.removeSon(sign47Location);
//                    System.out.println("removeSon after");
//                    sign47LocationParentsToRemove.add(sign47LocationParent);
////                    sign47Location.addParent(this);
//                    sign47LocationParentsToAdd.add(this);
//                    sign47LocationSons.add(sign47Location);
//                } else if(sign47LocationParent.getStep() == this.getStep()) {
//                    System.out.println("== before");
////                    sign47Location.addParent(this);
//                    sign47LocationParentsToAdd.add(this);
//                    sign47LocationSons.add(sign47Location);
//                }
//            }
//            sign47Location.removeParents(sign47LocationParentsToRemove);
//            sign47Location.addParents(sign47LocationParentsToAdd);
//
//        }
//
//        return sign47LocationSons;
//    }
//
//    private void addParents(List<Sign47Location11> sign47LocationParentsToAdd) {
//        if(sign47LocationParentsToAdd == null || sign47LocationParentsToAdd.isEmpty()) {
//            return;
//        }
//        this.sign47LocationParents.addAll(sign47LocationParentsToAdd);
//    }
//
//    private void removeParents(List<Sign47Location11> sign47LocationParentsToRemove) {
//        if(sign47LocationParentsToRemove == null || sign47LocationParentsToRemove.isEmpty()) {
//            return;
//        }
//        this.sign47LocationParents.removeAll(sign47LocationParentsToRemove);
//    }
//
//    private void addParent(Sign47Location11 parentSign47Location) {
//        this.setStep(parentSign47Location.getNextStep());
//        this.sign47LocationParents.add(parentSign47Location);
//    }
//
//    private boolean isParent(Sign47Location11 sign47Location) {
//
//        return sign47LocationParents.contains(sign47Location);
//    }
//
//    private void removeSon(Sign47Location11 sign47Location) {
//        if (sign47LocationSons != null) {
//            sign47LocationSons.remove(sign47Location);
//        }
//    }
//
//    public int getStep() {
//        return step;
//    }
//
//    private int getNextStep() {
//        return (step + 1);
//    }
//
//    private void setStep(int step) {
//        this.step = step;
//    }
//
//    public List<Sign47Location11> getParents() {
//        return this.sign47LocationParents;
//    }
//}
