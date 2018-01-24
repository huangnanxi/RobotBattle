/**
 * 系统项目名称
 * com.kxd.code.competition.robot.fight
 * BigHuFaFightRobot.java
 * 
 * 2018年1月22日-下午10:17:42
 *  2018BlueBox软件工作室-倾心承制
 * 
 */
package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.CommonConstant;
import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;
import java.util.Map.Entry;

public class BigHuFaFightRobot extends AbstractFightRobot {
    private static int                  mapSize;

    private static LinkedList<Location> routine = null;

    /**
     * 创建一个新的实例 BigHuFaFightRobot.
     *
     * @param name
     */
    public BigHuFaFightRobot() {
        super("大护法");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.kxd.code.competition.robot.AbstractRobot#getNextAction()
     */
    @Override
    public CommonMoveAction getNextAction() {

        FightRobotSeeEntity currentfightRobotSeeStatus = null;
        Policy nextStepPolicy = Policy.LIVE_LONG;
        currentfightRobotSeeStatus = (FightRobotSeeEntity) currentSeeMazeSituation;
        CommonMoveAction nextAction = null;
        try {
            mapSize = currentfightRobotSeeStatus.mapInfo.size;
            // 默认策略,活最久
            nextStepPolicy = makeDecision(currentfightRobotSeeStatus);
            nextAction = nextStepPolicy.accquireNextMoveActiron(currentfightRobotSeeStatus);
            if (nextAction == null && nextStepPolicy instanceof FuckEveryOnePolicy) {
                nextAction = Policy.LIVE_LONG.accquireNextMoveActiron(currentfightRobotSeeStatus);
            }
            return nextAction;
        } catch (Exception e) {

        }
        return new CommonMoveAction(generateNoLandmineAction(currentfightRobotSeeStatus));
    }

    private Policy makeDecision(FightRobotSeeEntity currentStatus) {
        Policy nextStepPolicy;
        Location currentLocation = currentStatus.robotBaseInfo.currentLocation;
        EnemyBox enemyBox = new EnemyBox(currentStatus);
        FightRobotBaseInfo nearestEnemy = enemyBox.fetchNearestEnemy();
        if (nearestEnemy != null) {
            int distance = Math.abs(currentLocation.x - nearestEnemy.currentLocation.x)
                    + Math.abs(currentLocation.y - nearestEnemy.currentLocation.y);
            if (distance <= 2)
                nextStepPolicy = new FuckEveryOnePolicy(nearestEnemy, currentStatus);
            else
                nextStepPolicy = Policy.LIVE_LONG;
        }

        else {
            nextStepPolicy = Policy.LIVE_LONG;
        }
        return nextStepPolicy;
    }

    public static MoveActionCommandEnum chargeMove(Location myLocation, Location moveLocation) {
        if (myLocation.x == moveLocation.x && myLocation.y >= moveLocation.y) {
            return MoveActionCommandEnum.MOVE_LEFT;
        } else if (myLocation.x == moveLocation.x && myLocation.y < moveLocation.y) {
            return MoveActionCommandEnum.MOVE_RIGHT;
        } else if (myLocation.x < moveLocation.x && myLocation.y == moveLocation.y) {
            return MoveActionCommandEnum.MOVE_DOWN;
        } else if (myLocation.x >= moveLocation.x && myLocation.y == moveLocation.y) {
            return MoveActionCommandEnum.MOVE_TOP;
        }
        return MoveActionCommandEnum.MOVE_LEFT;
    }

    private static MoveActionCommandEnum generateNoLandmineAction(FightRobotSeeEntity fightRobotSeeEntity) {
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        Location currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;

        Random random = new Random(System.currentTimeMillis());

        MoveActionCommandEnum moveActionCommand = MoveActionCommandEnum.MOVE_DOWN;

        int tryNum = 0;
        while (true) {
            tryNum++;

            int index = Math.abs(random.nextInt()) % 4;
            moveActionCommand = CommonConstant.directCodes.get(index);

            Integer nextX = currentLocation.x + CommonConstant.directXY.get(index);
            Integer nextY = currentLocation.y + CommonConstant.directXY.get(index + 1);
            if (!isHasLandmine(elementLists, new Location(nextX, nextY), fightRobotSeeEntity.mapInfo)
                    || tryNum == 1000) {
                break;
            }
        }

        return moveActionCommand;
    }

    private static Boolean isHasLandmine(ElementList[][] elementLists, Location location,
            AbstractMapInfo fightMapInfo) {
        Boolean isHasLandmine = false;
        if (location.x >= fightMapInfo.size || location.y >= fightMapInfo.size || location.x < 0 || location.y < 0) {
            return true;
        }

        for (AbstractElement element : elementLists[location.x][location.y].elements) {
            if (ElementTypeEnum.LANDMINE == element.elementType) {
                isHasLandmine = true;
                break;
            }
        }
        return isHasLandmine;
    }

    /**
     * 干死所有人策略
     */
    public static class FuckEveryOnePolicy implements Policy {
        private FightRobotBaseInfo  enemy;

        private FightRobotSeeEntity currentStatus;

        public FuckEveryOnePolicy(FightRobotBaseInfo enemy, FightRobotSeeEntity currentStatus) {
            this.enemy = enemy;
            this.currentStatus = currentStatus;
        }

        @Override
        public CommonMoveAction accquireNextMoveActiron(FightRobotSeeEntity currentStatus) {
            Location currentLocation = currentStatus.robotBaseInfo.currentLocation;
            int distance = Math.abs(currentLocation.x - enemy.currentLocation.x)
                    + Math.abs(currentLocation.y - enemy.currentLocation.y);
            switch (distance) {
            case 1:
                return new CommonMoveAction(fuckHim(enemy, currentLocation));
            case 2:
                return new CommonMoveAction(leaveHim(enemy, currentLocation));
            default:
                break;
            }
            return null;
        }

        private MoveActionCommandEnum fuckHim(FightRobotBaseInfo enemy, Location myLocation) {
            MoveActionCommandEnum direction = null;
            if (enemy.currentLocation.x > myLocation.x) {
                direction = MoveActionCommandEnum.MOVE_DOWN;
            }
            if (enemy.currentLocation.x < myLocation.x) {
                direction = MoveActionCommandEnum.MOVE_TOP;
            }
            if (enemy.currentLocation.y > myLocation.y) {
                direction = MoveActionCommandEnum.MOVE_RIGHT;
            }
            if (enemy.currentLocation.y < myLocation.y) {
                direction = MoveActionCommandEnum.MOVE_LEFT;
            }
            return direction;
        }

        private MoveActionCommandEnum leaveHim(FightRobotBaseInfo enemy, Location myLocation) {
            return null;
        }
    }

    /**
     * 活得最长命策略
     */
    public static class LiveLongPolicy implements Policy {
        private BloodBag lastBloodBag = null;

        @Override
        public CommonMoveAction accquireNextMoveActiron(FightRobotSeeEntity currentStatus) {
            BloodBagBox currentBagBox = new BloodBagBox(currentStatus);
            LandmineBox currentMineBox = new LandmineBox(currentStatus);
            Location currentLocation = currentStatus.robotBaseInfo.currentLocation;
            Location nextLocation = null;

            // 目标血包存在,且当前未被销毁
            if (lastBloodBag != null && currentBagBox.findTargetBag(lastBloodBag.location) != null) {
                // 只需考虑地雷情况,绕过即可,无需考虑外部机器人存在的情况,只需保证与最近的机器人保持距离>=3
                // 计算获取该血包的最佳路径,外延圈可设置,默认值为1
                List<Location> routineList = calcBestMove(currentLocation, lastBloodBag, currentMineBox);
                if (routineList != null) {
                    // 生成路径表
                    routine = new LinkedList<>(routineList);
                    nextLocation = routine.pollFirst();
                }
            } else {
                lastBloodBag = currentBagBox.accquireNearestBag();
                // 如果血包没有了,就切换算法
                if (lastBloodBag == null) {
                    routine = null;
                }
                // 还有血包
                else {
                    List<Location> routineList = calcBestMove(currentLocation, lastBloodBag, currentMineBox);
                    if (routineList != null) {
                        // 生成路径表
                        routine = new LinkedList<>(routineList);
                        System.out.println("fuckA");
                        nextLocation = routine.pollFirst();
                    }
                }
            }
            if (nextLocation != null) {
                MoveActionCommandEnum direction = chargeMove(currentLocation, nextLocation);
                return new CommonMoveAction(direction);
            }

            return new CommonMoveAction(generateNoLandmineAction(currentStatus));
        }

        private List<Location> calcBestMove(Location myLocation, BloodBag targetBag, LandmineBox mineBox) {
            // 尝试最短路径
            List<Location> steps = null, reverseSteps = null;
            steps = calcBaseStep(myLocation, targetBag, mineBox);
            reverseSteps = calcBaseStepReverse(myLocation, targetBag, mineBox);
            int absDistance = Math.abs(myLocation.x - targetBag.location.x)
                    + Math.abs(myLocation.y - targetBag.location.y);

            // 最短路径成功
            if (steps != null && absDistance == steps.size())
                return steps;
            else if (reverseSteps != null && absDistance == reverseSteps.size())
                return reverseSteps;

            // 非最短路径
            // else if(steps!=null&&steps.size()>0){
            // Location lastLocation = steps.get(steps.size()-1);
            //
            //
            // for(int i=0;i<mapSize*mapSize;i++){
            // //获取最后一个move
            // incrSteps = calcBetterStep(lastLocation,targetBag,mineBox);
            // if(incrSteps != null && incrSteps.size()>0){
            // steps.addAll(incrSteps);
            // lastLocation = incrSteps.get(incrSteps.size()-1);
            // }
            // else return null;
            // }
            // }
            return null;
        }

        private List<Location> calcBetterStep(Location lastLocation, BloodBag targetBag, LandmineBox mineBox) {
            if (lastLocation.x <= targetBag.location.x && lastLocation.y <= targetBag.location.y) {
                List<Location> downSteps = null, leftSteps = null, upSteps = null;
                // 往上偏移一步,X轴-11
                lastLocation.x = lastLocation.x - 1;
                if (lastLocation.x >= 0) {
                    upSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (upSteps != null && calcDistance(lastLocation, targetBag.location) == upSteps.size())
                        return downSteps;
                }

                lastLocation.y = lastLocation.y - 1;
                // 复原
                lastLocation.x = lastLocation.x + 1;
                if (lastLocation.y >= 0) {
                    leftSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (leftSteps != null && calcDistance(lastLocation, targetBag.location) == leftSteps.size())
                        return downSteps;
                }

                lastLocation.y = lastLocation.y + 1;
                lastLocation.y = lastLocation.y + 1;

                if (lastLocation.y < mapSize - 1) {
                    downSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (downSteps != null && calcDistance(lastLocation, targetBag.location) == downSteps.size())
                        return downSteps;
                }

                return downSteps != null ? downSteps : leftSteps;
            }

            else if (lastLocation.x > targetBag.location.x && lastLocation.y <= targetBag.location.y) {
                List<Location> downSteps = null, leftSteps = null, upSteps = null;
                // 往下偏移一步,X轴+1
                lastLocation.x = lastLocation.x + 1;
                if (lastLocation.x < mapSize - 1) {
                    downSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (downSteps != null && calcDistance(lastLocation, targetBag.location) == downSteps.size())
                        return downSteps;

                }

                lastLocation.y = lastLocation.y - 1;
                // 将X轴复原
                lastLocation.x = lastLocation.x - 1;
                if (lastLocation.y >= 0) {
                    leftSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (leftSteps != null && calcDistance(lastLocation, targetBag.location) == leftSteps.size())
                        return leftSteps;

                }

                lastLocation.x = lastLocation.x - 1;
                lastLocation.y = lastLocation.y + 1;
                if (lastLocation.x >= 0) {
                    upSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (upSteps != null && calcDistance(lastLocation, targetBag.location) == upSteps.size())
                        return upSteps;
                }

                return downSteps != null ? downSteps : leftSteps;
            }

            else if (lastLocation.x > targetBag.location.x && lastLocation.y > targetBag.location.y) {
                List<Location> downSteps = null, leftSteps = null, rightSteps = null;
                // 往下偏移一步,X轴+1
                lastLocation.x = lastLocation.x + 1;
                if (lastLocation.x < mapSize - 1) {
                    downSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (downSteps != null && calcDistance(lastLocation, targetBag.location) == downSteps.size())
                        return downSteps;

                }

                lastLocation.y = lastLocation.y - 1;
                // 将X轴复原
                lastLocation.x = lastLocation.x - 1;
                if (lastLocation.y >= 0) {
                    leftSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (leftSteps != null && calcDistance(lastLocation, targetBag.location) == leftSteps.size())
                        return leftSteps;
                }

                lastLocation.y = lastLocation.y + 1;
                lastLocation.y = lastLocation.y + 1;
                if (lastLocation.x < mapSize - 1) {
                    rightSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (rightSteps != null && calcDistance(lastLocation, targetBag.location) == rightSteps.size())
                        return rightSteps;
                }

                return downSteps != null ? downSteps : leftSteps;
            }

            else if (lastLocation.x <= targetBag.location.x && lastLocation.y > targetBag.location.y) {
                List<Location> downSteps = null, upSteps = null, rightSteps = null;
                // 往下偏移一步,X轴+1
                lastLocation.x = lastLocation.x + 1;
                if (lastLocation.x < mapSize - 1) {
                    downSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (downSteps != null && calcDistance(lastLocation, targetBag.location) == downSteps.size())
                        return downSteps;

                }

                lastLocation.x = lastLocation.x - 1;
                // 将X轴复原
                lastLocation.x = lastLocation.x - 1;
                if (lastLocation.x >= 0) {
                    upSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (upSteps != null && calcDistance(lastLocation, targetBag.location) == upSteps.size())
                        return upSteps;

                }

                lastLocation.y = lastLocation.y + 1;
                // 将X轴复原
                lastLocation.x = lastLocation.x + 2;
                if (lastLocation.y < mapSize - 1) {
                    rightSteps = calcBaseStep(lastLocation, targetBag, mineBox);
                    if (rightSteps != null && calcDistance(lastLocation, targetBag.location) == rightSteps.size())
                        return rightSteps;

                }

                return downSteps != null ? downSteps : upSteps;
            }

            return null;
        }

        private int calcDistance(Location from, Location end) {
            return Math.abs(from.x - end.x) + Math.abs(end.y - from.y);
        }

        private List<Location> calcBaseStep(Location myLocation, BloodBag targetBag, LandmineBox mineBox) {
            List<Location> moveList = new ArrayList<Location>();
            // 尝试最短路径
            if (myLocation.x <= targetBag.location.x && myLocation.y <= targetBag.location.y) {
                int locationY = myLocation.y;
                if (myLocation.x == targetBag.location.x) {
                    for (int i = locationY; i <= targetBag.location.y; i++) {
                        Location step = new Location(myLocation.x, i);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else if (myLocation.y == targetBag.location.y) {
                    for (int j = myLocation.x; j <= targetBag.location.x; j++) {
                        Location step = new Location(j, locationY);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else {
                    for (int j = myLocation.x; j <= targetBag.location.x; j++) {
                        if (locationY == targetBag.location.y) {
                            Location step = new Location(j, locationY);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        } else {
                            for (int i = myLocation.y + 1; i <= targetBag.location.y; i++) {
                                Location step = new Location(j, i);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);
                                } else
                                    break;
                            }
                            locationY = targetBag.location.y;
                        }
                    }
                }

            }

            else if (myLocation.x > targetBag.location.x && myLocation.y <= targetBag.location.y) {
                int locationY = myLocation.y;
                if (myLocation.y == targetBag.location.y) {
                    for (int i = myLocation.x - 1; i >= targetBag.location.x && i >= 0; i--) {
                        Location step = new Location(i, myLocation.y);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }

                } else {
                    for (int i = myLocation.x; i >= targetBag.location.x && i >= 0; i--) {
                        if (locationY == targetBag.location.y) {
                            Location step = new Location(i, locationY);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        } else {
                            for (int j = myLocation.y + 1; j <= targetBag.location.y; j++) {
                                Location step = new Location(i, j);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);
                                } else
                                    break;
                            }
                            locationY = targetBag.location.y;
                        }

                    }
                }
            }

            else if (myLocation.x > targetBag.location.x && myLocation.y > targetBag.location.y) {
                int locationY = myLocation.y;
                for (int i = myLocation.x; i >= targetBag.location.x && i >= 0; i--) {
                    if (locationY == targetBag.location.y) {
                        Location step = new Location(i, locationY);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }

                    else {
                        for (int j = myLocation.y - 1; j >= targetBag.location.y && j >= 0; j--) {
                            Location step = new Location(i, j);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        }
                        locationY = targetBag.location.y;
                    }
                }
            }

            else if (myLocation.x <= targetBag.location.x && myLocation.y > targetBag.location.y) {
                int locationX = myLocation.x;
                if (myLocation.x == targetBag.location.x) {
                    for (int j = myLocation.y - 1; j >= targetBag.location.y; j--) {
                        Location step = new Location(myLocation.x, j);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else {
                    for (int i = myLocation.y; i >= targetBag.location.y; i--) {
                        if (locationX == targetBag.location.x) {
                            Location step = new Location(locationX, i);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {

                                moveList.add(step);
                            } else
                                break;
                        }

                        else {
                            for (int j = myLocation.x + 1; j <= targetBag.location.x; j++) {
                                Location step = new Location(j, i);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);

                                } else
                                    break;
                            }
                            locationX = targetBag.location.x;
                        }

                    }
                }
            }
            return moveList;
        }

        private List<Location> calcBaseStepReverse(Location myLocation, BloodBag targetBag, LandmineBox mineBox) {
            List<Location> moveList = new ArrayList<Location>();
            // 尝试最短路径
            if (myLocation.x <= targetBag.location.x && myLocation.y <= targetBag.location.y) {
                int locationX = myLocation.x;
                if (myLocation.x == targetBag.location.x) {
                    for (int i = myLocation.y + 1; i <= targetBag.location.y; i++) {
                        Location step = new Location(myLocation.x, i);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else if (myLocation.y == targetBag.location.y) {
                    for (int j = myLocation.x; j <= targetBag.location.x; j++) {
                        Location step = new Location(j, myLocation.y);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else {
                    for (int j = myLocation.y; j <= targetBag.location.y; j++) {
                        if (locationX == targetBag.location.x) {
                            Location step = new Location(locationX, j);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        } else {
                            for (int i = myLocation.x + 1; i <= targetBag.location.x; i++) {
                                Location step = new Location(i, j);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);
                                } else
                                    break;
                            }
                            locationX = targetBag.location.x;
                        }
                    }
                }

            }

            else if (myLocation.x > targetBag.location.x && myLocation.y <= targetBag.location.y) {
                int locationX = myLocation.x;
                if (myLocation.y == targetBag.location.y) {
                    for (int i = myLocation.x - 1; i >= targetBag.location.x && i >= 0; i--) {
                        Location step = new Location(i, myLocation.y);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }

                } else {
                    for (int i = myLocation.y; i <= targetBag.location.y && i >= 0; i++) {
                        if (locationX == targetBag.location.x) {
                            Location step = new Location(locationX, i);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        } else {
                            for (int j = myLocation.x - 1; j >= targetBag.location.y; j--) {
                                Location step = new Location(j, i);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);
                                } else
                                    break;
                            }
                            locationX = targetBag.location.x;
                        }

                    }
                }
            }

            else if (myLocation.x > targetBag.location.x && myLocation.y > targetBag.location.y) {
                int locationX = myLocation.x;
                for (int i = myLocation.y; i >= targetBag.location.y && i >= 0; i--) {
                    if (locationX == targetBag.location.x) {
                        Location step = new Location(locationX, i);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }

                    else {
                        for (int j = myLocation.x - 1; j >= targetBag.location.x && j >= 0; j--) {
                            Location step = new Location(j, i);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {
                                moveList.add(step);
                            } else
                                break;
                        }
                        locationX = targetBag.location.x;
                    }
                }
            }

            else if (myLocation.x <= targetBag.location.x && myLocation.y > targetBag.location.y) {
                int locationY = myLocation.y;
                if (myLocation.x == targetBag.location.x) {
                    for (int j = myLocation.y - 1; j >= targetBag.location.y; j--) {
                        Location step = new Location(myLocation.x, j);
                        Landmine mine = mineBox.findTargeLandmine(step);
                        if (mine == null) {
                            moveList.add(step);
                        } else
                            break;
                    }
                }

                else {
                    for (int i = myLocation.x; i <= targetBag.location.y; i++) {
                        if (locationY == targetBag.location.y) {
                            Location step = new Location(i, locationY);
                            Landmine mine = mineBox.findTargeLandmine(step);
                            if (mine == null) {

                                moveList.add(step);
                            } else
                                break;
                        }

                        else {
                            for (int j = myLocation.y - 1; j >= targetBag.location.y; j--) {
                                Location step = new Location(i, j);
                                Landmine mine = mineBox.findTargeLandmine(step);
                                if (mine == null) {
                                    moveList.add(step);

                                } else
                                    break;
                            }
                            locationY = targetBag.location.y;
                        }

                    }
                }
            }
            return moveList;
        }

    }

    public static class RandomPolicy implements Policy {

        /*
         * (non-Javadoc)
         * 
         * @see com.kxd.code.competition.robot.fight.BigHuFaFightRobot.Policy#
         * accquireNextMoveActiron(com.kxd.code.competition.entity.robotsee.
         * AbstractRobotSeeEntity)
         */
        @Override
        public CommonMoveAction accquireNextMoveActiron(FightRobotSeeEntity currentStatus) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public static interface Policy {
        public static final Policy RANDOM_STEP = new RandomPolicy();

        public static final Policy LIVE_LONG   = new LiveLongPolicy();

        public CommonMoveAction accquireNextMoveActiron(FightRobotSeeEntity currentStatus);
    }

    public static class LandmineBox {
        private Map<Location, Landmine> landMineMap = new HashMap<Location, Landmine>();

        public LandmineBox(FightRobotSeeEntity currentStatus) {
            ElementList[][] elementLists = currentStatus.robotGetElementList;
            Location myLocation = currentStatus.robotBaseInfo.currentLocation;
            for (int i = 0; i < elementLists.length; i++) {
                for (int j = 0; j < elementLists.length; j++) {
                    ElementList elementList = elementLists[i][j];
                    for (AbstractElement element : elementList.elements) {
                        if (ElementTypeEnum.LANDMINE == element.elementType) {
                            // 存储所有血包及位置
                            Landmine lindmine = (Landmine) element;
                            landMineMap.put(lindmine.location, lindmine);
                        }
                    }
                }
            }
        }

        public Landmine findTargeLandmine(Location location) {
            return landMineMap.get(location);
        }
    }

    public static class EnemyBox {
        TreeMap<Integer, FightRobotBaseInfo> enemyList = new TreeMap<Integer, FightRobotBaseInfo>();

        public EnemyBox(FightRobotSeeEntity currentStatus) {
            ElementList[][] elementLists = currentStatus.robotGetElementList;
            Location myLocation = currentStatus.robotBaseInfo.currentLocation;
            for (int i = 0; i < elementLists.length; i++) {
                for (int j = 0; j < elementLists.length; j++) {
                    ElementList elementList = elementLists[i][j];
                    for (AbstractElement element : elementList.elements) {
                        if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                            // 存储所有血包及位置
                            FightRobotBaseInfo enemy = (FightRobotBaseInfo) element;
                            int absDistance = Math.abs(myLocation.x - enemy.currentLocation.x)
                                    + Math.abs(myLocation.y - enemy.currentLocation.y);
                            enemyList.put(absDistance, enemy);
                        }
                    }
                }
            }
        }

        public FightRobotBaseInfo fetchNearestEnemy() {
            if (enemyList != null && enemyList.size() > 0) {
                return enemyList.pollFirstEntry().getValue();
            }
            return null;
        }
    }

    public static class BloodBagBox {
        private Map<Location, BloodBag>    bloodBagMap        = new HashMap<Location, BloodBag>();

        private TreeMap<Integer, BloodBag> distanceSortedBags = new TreeMap<Integer, BloodBag>();

        private BloodBag                   targetBloodBag;

        public BloodBagBox(FightRobotSeeEntity currentStatus) {
            ElementList[][] elementLists = currentStatus.robotGetElementList;
            Location myLocation = currentStatus.robotBaseInfo.currentLocation;

            for (int i = 0; i < elementLists.length; i++) {
                for (int j = 0; j < elementLists.length; j++) {
                    ElementList elementList = elementLists[i][j];
                    for (AbstractElement element : elementList.elements) {
                        if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                            // 存储所有血包及位置
                            BloodBag bloodBag = (BloodBag) element;
                            bloodBagMap.put(bloodBag.location, bloodBag);

                            int absDistance = Math.abs(myLocation.x - bloodBag.location.x)
                                    + Math.abs(myLocation.y - bloodBag.location.y);
                            distanceSortedBags.put(absDistance, bloodBag);
                        }
                    }
                }
            }
        }

        public void newBag(BloodBag bag) {
            bloodBagMap.put(bag.location, bag);
        }

        public void destroyBag(Location location) {
            bloodBagMap.remove(location);
        }

        public Set<Location> findAllLocations() {
            return bloodBagMap.keySet();
        }

        // 找寻离自己绝对距离最近且未被吃掉的血包
        public BloodBag findTargetBag(Location myLocation) {
            return bloodBagMap.get(myLocation);
        }

        public BloodBag accquireNearestBag() {
            Entry<Integer, BloodBag> entry = null;
            entry = distanceSortedBags.pollFirstEntry();
            if (entry != null) {
                return entry.getValue();
            }
            return null;
        }

        public int calcBagNumber() {
            return bloodBagMap.size();
        }
    }
}
