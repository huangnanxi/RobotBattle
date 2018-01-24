package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.AbstractRobotBaseInfo;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Created by huangnx on 2017/12/21.
 */
public class BaoLeiFightRobot extends AbstractFightRobot {

    public BaoLeiFightRobot() {
        super("堡垒");
    }

    private int                   stepNum       = 0;

    private boolean               hasNextAction = false;

    private MoveActionCommandEnum nextAction    = null;

    @Override
    public CommonMoveAction getNextAction() {

        CommonMoveAction actionEntity = null;
        try {
            actionEntity = doPolicy();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (actionEntity == null) {
                    if (stepNum % 2 == 0) {
                        actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                    } else if (stepNum % 2 == 1) {
                        actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            if (actionEntity == null) {
                actionEntity = new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
            }
        }
        stepNum++;
        return actionEntity;
    }

    private CommonMoveAction doPolicy() {
        if (hasNextAction && nextAction != null) {
            // 被攻击 反击
            hasNextAction = false;
            CommonMoveAction curNextAction = new CommonMoveAction(nextAction);
            nextAction = null;
            return curNextAction;
        }

        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        AbstractRobotBaseInfo myElement = fightRobotSeeEntity.robotBaseInfo;
        int size = elementLists.length;

        // 标记危险坐标
        int[][] myDanger = new int[size][size];
        int[][] otherDanger = new int[size][size];
        List<AbstractRobotBaseInfo> otherList = new ArrayList<>();
        List<AbstractElement> landmineList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (myElement.currentLocation.x == i && myElement.currentLocation.y == j) {
                    continue;
                }
                List<AbstractElement> elementList = elementLists[i][j].elements;
                for (AbstractElement element : elementList) {
                    if (element.elementType.equals(ElementTypeEnum.LANDMINE)) {
                        myDanger[i][j] += 100;
                        otherDanger[i][j] += 100;
                        landmineList.add(element);
                    } else if (element.elementType.equals(ElementTypeEnum.ROBOT_INFO)) {
                        if (!element.equals(myElement)) {
                            myDanger[i][j] += 10;
                            if (i - 1 >= 0) {
                                myDanger[i - 1][j] += 1;
                            }
                            if (j - 1 >= 0) {
                                myDanger[i][j - 1] += 1;
                            }
                            if (i + 1 < size) {
                                myDanger[i + 1][j] += 1;
                            }
                            if (j + 1 < size) {
                                myDanger[i][j + 1] += 1;
                            }
                            otherList.add((AbstractRobotBaseInfo) element);
                        }
                    }
                }
            }
        }

        Queue<ElementPath> queue = new LinkedTransferQueue<>();
        Set<Location> queueSet = new HashSet<>();
        ElementPath firstElementPath = new ElementPath();
        firstElementPath.location = myElement.currentLocation;
        queue.offer(firstElementPath);
        queueSet.add(firstElementPath.location);

        // 自己能最早吃到
        Map<AbstractElement, Integer> myShortElement = new HashMap<>();
        Map<AbstractElement, ElementPath> myMap = new HashMap<>();
        // 广度遍历
        while (!queue.isEmpty()) {
            ElementPath elementPath = queue.poll();
            int x = elementPath.location.x;
            int y = elementPath.location.y;
            List<AbstractElement> elementList = elementLists[x][y].elements;
            for (AbstractElement element : elementList) {
                if (element.elementType.equals(ElementTypeEnum.BLOOD_BAG)) {
                    myMap.put(element, elementPath);
                    myShortElement.put(element, elementPath.path.size());
                }
            }
            if ((x - 1 >= 0) && myDanger[x - 1][y] == 0) {
                ElementPath nextElementPath = new ElementPath();
                nextElementPath.location = new Location(x - 1, y);
                nextElementPath.path.addAll(elementPath.path);
                nextElementPath.path.add(MoveActionCommandEnum.MOVE_TOP);
                if (!queueSet.contains(nextElementPath.location)) {
                    queue.offer(nextElementPath);
                    queueSet.add(nextElementPath.location);
                }
            }
            if ((y + 1 < size) && myDanger[x][y + 1] == 0) {
                ElementPath nextElementPath = new ElementPath();
                nextElementPath.location = new Location(x, y + 1);
                nextElementPath.path.addAll(elementPath.path);
                nextElementPath.path.add(MoveActionCommandEnum.MOVE_RIGHT);
                if (!queueSet.contains(nextElementPath.location)) {
                    queue.offer(nextElementPath);
                    queueSet.add(nextElementPath.location);
                }
            }
            if ((x + 1 < size) && myDanger[x + 1][y] == 0) {
                ElementPath nextElementPath = new ElementPath();
                nextElementPath.location = new Location(x + 1, y);
                nextElementPath.path.addAll(elementPath.path);
                nextElementPath.path.add(MoveActionCommandEnum.MOVE_DOWN);
                if (!queueSet.contains(nextElementPath.location)) {
                    queue.offer(nextElementPath);
                    queueSet.add(nextElementPath.location);
                }
            }
            if ((y - 1 >= 0) && myDanger[x][y - 1] == 0) {
                ElementPath nextElementPath = new ElementPath();
                nextElementPath.location = new Location(x, y - 1);
                nextElementPath.path.addAll(elementPath.path);
                nextElementPath.path.add(MoveActionCommandEnum.MOVE_LEFT);
                if (!queueSet.contains(nextElementPath.location)) {
                    queue.offer(nextElementPath);
                    queueSet.add(nextElementPath.location);
                }
            }
        }

        if (myShortElement.size() > 0) {
            // 有可达的血包
            // 其他机器人的路径1
            Map<AbstractElement, Integer> myShortElement1 = new HashMap<>();
            myShortElement1.putAll(myShortElement);
            for (AbstractRobotBaseInfo robotBaseInfo : otherList) {
                queue = new LinkedTransferQueue<>();
                queueSet = new HashSet<>();
                firstElementPath = new ElementPath();
                firstElementPath.location = robotBaseInfo.currentLocation;
                queue.offer(firstElementPath);
                queueSet.add(firstElementPath.location);
                // 广度遍历
                while (!queue.isEmpty()) {
                    ElementPath elementPath = queue.poll();
                    int x = elementPath.location.x;
                    int y = elementPath.location.y;
                    List<AbstractElement> elementList = elementLists[x][y].elements;
                    for (AbstractElement element : elementList) {
                        if (element.elementType.equals(ElementTypeEnum.BLOOD_BAG)) {
                            Integer num = myShortElement1.get(element);
                            if (num != null && num > elementPath.path.size()) {
                                myShortElement1.remove(element);
                            }
                        }
                    }
                    if ((x - 1 >= 0) && otherDanger[x - 1][y] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x - 1, y);
                        ;
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_TOP);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((y + 1 < size) && otherDanger[x][y + 1] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x, y + 1);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_RIGHT);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((x + 1 < size) && otherDanger[x + 1][y] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x + 1, y);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_DOWN);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((y - 1 >= 0) && otherDanger[x][y - 1] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x, y - 1);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_LEFT);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                }

            }

            if (myShortElement1.size() > 0) {
                myShortElement1 = sortMapValue(myShortElement1);
                ElementPath elementPath = myMap.get(myShortElement1.keySet().iterator().next());
                return new CommonMoveAction(elementPath.path.get(0));
            }

            // 其他机器人的路径2
            Map<AbstractElement, Integer> myShortElement2 = new HashMap<>();
            myShortElement2.putAll(myShortElement);
            for (AbstractRobotBaseInfo robotBaseInfo : otherList) {
                queue = new LinkedTransferQueue<>();
                queueSet = new HashSet<>();
                firstElementPath = new ElementPath();
                firstElementPath.location = robotBaseInfo.currentLocation;
                queue.offer(firstElementPath);
                queueSet.add(firstElementPath.location);
                // 广度遍历
                while (!queue.isEmpty()) {
                    ElementPath elementPath = queue.poll();
                    int x = elementPath.location.x;
                    int y = elementPath.location.y;
                    List<AbstractElement> elementList = elementLists[x][y].elements;
                    for (AbstractElement element : elementList) {
                        if (element.elementType.equals(ElementTypeEnum.BLOOD_BAG)) {
                            Integer num = myShortElement2.get(element);
                            if (num != null && num > elementPath.path.size()) {
                                myShortElement2.remove(element);
                            }
                        }
                    }
                    if ((x - 1 >= 0) && myDanger[x - 1][y] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x - 1, y);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_TOP);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((y + 1 < size) && myDanger[x][y + 1] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x, y + 1);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_RIGHT);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((x + 1 < size) && myDanger[x + 1][y] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x + 1, y);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_DOWN);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                    if ((y - 1 >= 0) && myDanger[x][y - 1] == 0) {
                        ElementPath nextElementPath = new ElementPath();
                        nextElementPath.location = new Location(x, y - 1);
                        nextElementPath.path.addAll(elementPath.path);
                        nextElementPath.path.add(MoveActionCommandEnum.MOVE_LEFT);
                        if (!queueSet.contains(nextElementPath.location)) {
                            queue.offer(nextElementPath);
                            queueSet.add(nextElementPath.location);
                        }
                    }
                }

            }

            if (myShortElement2.size() > 0) {
                myShortElement2 = sortMapValue(myShortElement2);
                ElementPath elementPath = myMap.get(myShortElement2.keySet().iterator().next());
                return new CommonMoveAction(elementPath.path.get(0));
            }

            myShortElement = sortMapValue(myShortElement);
            ElementPath elementPath = myMap.get(myShortElement.keySet().iterator().next());
            return new CommonMoveAction(elementPath.path.get(0));
        } else {
            // 没有可达的血包

            FightRobotBaseInfo otherInfo = (FightRobotBaseInfo) otherList.get(0);
            FightRobotBaseInfo myInfo = (FightRobotBaseInfo) myElement;
            if (fightRobotSeeEntity.mapInfo.size == 10) {
                // 小组赛

                if (myInfo.currentLocation.x == otherInfo.currentLocation.x
                        && myInfo.currentLocation.y == otherInfo.currentLocation.y && myInfo.currentCanStepNum == 2) {
                    int x = myInfo.currentLocation.x;
                    int y = myInfo.currentLocation.y;
                    // 被攻击了 直接干
                    // if(myInfo.bloodNum >= otherInfo.bloodNum){
                    // 主动进攻
                    if (x - 1 >= 0 && myDanger[x - 1][y] == 0) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_DOWN;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                    } else if (y + 1 < size && myDanger[x][y + 1] == 0) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_LEFT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                    } else if (x + 1 < size && myDanger[x + 1][y] == 0) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_TOP;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                    } else if (y - 1 >= 0 && myDanger[x][y - 1] == 0) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                    } else if (x - 1 >= 0) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_DOWN;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                    } else if (y + 1 < size) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_LEFT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                    } else if (x + 1 < size) {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_TOP;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                    } else {
                        hasNextAction = true;
                        nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                        return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                    }
                    // }else {
                    // //躲避
                    // }
                }
                // if(myInfo.bloodNum >= otherInfo.bloodNum + 2){
                // //主动进攻 TODO
                // }else {
                // 被动防御
                return doDefense(myInfo, myDanger);
                // }
            } else {
                if (myInfo.bloodNum <= 7) {
                    // 血量太少 防御
                    return doDefense(myInfo, myDanger);
                }
                if (landmineNum >= 3 || landmineList.size() >= 200 || otherList.size() <= 3 || myInfo.bloodNum >= 20
                        || myInfo.currentCanStepNum == 2) {
                    // 主动攻击
                    if (myInfo.currentCanStepNum == 2) {// 被攻击
                        int x = myInfo.currentLocation.x;
                        int y = myInfo.currentLocation.y;
                        long time = System.currentTimeMillis();

                        List<CommonMoveAction> action = new ArrayList<>();
                        if (x - 1 >= 0 && (myDanger[x - 1][y] % 100) / 10 > 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_DOWN;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                        } else if (y + 1 < size && (myDanger[x][y + 1] % 100) / 10 > 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_LEFT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                        } else if (x + 1 < size && (myDanger[x + 1][y] % 100) / 10 > 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_TOP;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                        } else if (y - 1 >= 0 && (myDanger[x][y - 1] % 100) / 10 > 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                        } else if (x - 1 >= 0 && myDanger[x - 1][y] == 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_DOWN;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                        } else if (y + 1 < size && myDanger[x][y + 1] == 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_LEFT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                        } else if (x + 1 < size && myDanger[x + 1][y] == 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_TOP;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                        } else if (y - 1 >= 0 && myDanger[x][y - 1] == 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                        } else if (x - 1 >= 0 && (myDanger[x - 1][y] % 100) != 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_DOWN;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                        } else if (y + 1 < size && (myDanger[x][y + 1] % 100) != 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_LEFT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                        } else if (x + 1 < size && (myDanger[x + 1][y] % 100) != 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_TOP;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                        } else if (y - 1 >= 0 && (myDanger[x][y - 1] % 100) != 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                        } else if (x - 1 >= 0) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_DOWN;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
                        } else if (y + 1 < size) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_LEFT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
                        } else if (x + 1 < size) {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_TOP;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
                        } else {
                            hasNextAction = true;
                            nextAction = MoveActionCommandEnum.MOVE_RIGHT;
                            return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
                        }

                    } else {

                        // 自己能最早吃到
                        Queue<ElementPath> attackQueue = new LinkedTransferQueue<>();
                        queueSet = new HashSet<>();
                        firstElementPath = new ElementPath();
                        firstElementPath.location = myElement.currentLocation;
                        attackQueue.offer(firstElementPath);
                        queueSet.add(firstElementPath.location);
                        myShortElement = new HashMap<>();
                        myMap = new HashMap<>();
                        // 广度遍历
                        while (!attackQueue.isEmpty()) {
                            ElementPath elementPath = attackQueue.poll();
                            int x = elementPath.location.x;
                            int y = elementPath.location.y;
                            List<AbstractElement> elementList = elementLists[x][y].elements;
                            for (AbstractElement element : elementList) {
                                if (element.elementType.equals(ElementTypeEnum.ROBOT_INFO)
                                        && !(myInfo.currentLocation.x == x && myInfo.currentLocation.y == y)) {
                                    myMap.put(element, elementPath);
                                    myShortElement.put(element, elementPath.path.size());
                                }
                            }
                            if ((x - 1 >= 0) && otherDanger[x - 1][y] == 0) {
                                ElementPath nextElementPath = new ElementPath();
                                nextElementPath.location = new Location(x - 1, y);
                                nextElementPath.path.addAll(elementPath.path);
                                nextElementPath.path.add(MoveActionCommandEnum.MOVE_TOP);
                                if (!queueSet.contains(nextElementPath.location)) {
                                    attackQueue.offer(nextElementPath);
                                    queueSet.add(nextElementPath.location);
                                }
                            }
                            if ((y + 1 < size) && otherDanger[x][y + 1] == 0) {
                                ElementPath nextElementPath = new ElementPath();
                                nextElementPath.location = new Location(x, y + 1);
                                nextElementPath.path.addAll(elementPath.path);
                                nextElementPath.path.add(MoveActionCommandEnum.MOVE_RIGHT);
                                if (!queueSet.contains(nextElementPath.location)) {
                                    attackQueue.offer(nextElementPath);
                                    queueSet.add(nextElementPath.location);
                                }
                            }
                            if ((x + 1 < size) && otherDanger[x + 1][y] == 0) {
                                ElementPath nextElementPath = new ElementPath();
                                nextElementPath.location = new Location(x + 1, y);
                                nextElementPath.path.addAll(elementPath.path);
                                nextElementPath.path.add(MoveActionCommandEnum.MOVE_DOWN);
                                if (!queueSet.contains(nextElementPath.location)) {
                                    attackQueue.offer(nextElementPath);
                                    queueSet.add(nextElementPath.location);
                                }
                            }
                            if ((y - 1 >= 0) && otherDanger[x][y - 1] == 0) {
                                ElementPath nextElementPath = new ElementPath();
                                nextElementPath.location = new Location(x, y - 1);
                                nextElementPath.path.addAll(elementPath.path);
                                nextElementPath.path.add(MoveActionCommandEnum.MOVE_LEFT);
                                if (!queueSet.contains(nextElementPath.location)) {
                                    attackQueue.offer(nextElementPath);
                                    queueSet.add(nextElementPath.location);
                                }
                            }
                        }

                        if (myShortElement.size() > 0) {
                            myShortElement = sortMapValue(myShortElement);
                            ElementPath elementPath = myMap.get(myShortElement.keySet().iterator().next());
                            return new CommonMoveAction(elementPath.path.get(0));
                        } else {
                            return doDefense(myInfo, myDanger);
                        }

                    }
                }
                return doDefense(myInfo, myDanger);
            }
        }
    }

    private CommonMoveAction doDefense(AbstractRobotBaseInfo myInfo, int[][] myDanger) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        int size = elementLists.length;
        int x = myInfo.currentLocation.x;
        int y = myInfo.currentLocation.y;
        long time = System.currentTimeMillis();

        List<CommonMoveAction> action = new ArrayList<>();
        if (x - 1 >= 0 && myDanger[x - 1][y] == 0) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
        }
        if (y + 1 < size && myDanger[x][y + 1] == 0) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
        }
        if (x + 1 < size && myDanger[x + 1][y] == 0) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
        }
        if (y - 1 >= 0 && myDanger[x][y - 1] == 0) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
        }
        if (action.size() > 0) {
            return action.get((int) (time % action.size()));
        }

        action.clear();
        if (x - 1 >= 0 && myDanger[x - 1][y] == 10) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
        }
        if (y + 1 < size && myDanger[x][y + 1] == 10) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
        }
        if (x + 1 < size && myDanger[x + 1][y] == 10) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
        }
        if (y - 1 >= 0 && myDanger[x][y - 1] == 10) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
        }
        if (action.size() > 0) {
            return action.get((int) (time % action.size()));
        }

        action.clear();
        if (x - 1 >= 0 && myDanger[x - 1][y] == 1) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
        }
        if (y + 1 < size && myDanger[x][y + 1] == 1) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
        }
        if (x + 1 < size && myDanger[x + 1][y] == 1) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
        }
        if (y - 1 >= 0 && myDanger[x][y - 1] == 1) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
        }
        if (action.size() > 0) {
            return action.get((int) (time % action.size()));
        }

        action.clear();
        if (x - 1 >= 0) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP));
        }
        if (y + 1 < size) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT));
        }
        if (x + 1 < size) {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN));
        } else {
            action.add(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT));
        }
        if (action.size() > 0) {
            landmineNum++;
            return action.get((int) (time % action.size()));
        }

        return null;
    }

    private int landmineNum = 0;

    private <T> Map<T, Integer> sortMapValue(Map<T, Integer> map) {
        // 按value进行排序
        List<Map.Entry<T, Integer>> entry = new ArrayList<Map.Entry<T, Integer>>(map.entrySet());
        Collections.sort(entry, new Comparator<Map.Entry<T, Integer>>() {
            // 升序排序
            public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2) {

                return o1.getValue().compareTo(o2.getValue());
            }

        });

        Map sortMapResult = new LinkedHashMap();

        for (Map.Entry<T, Integer> mapEntry : entry) {
            sortMapResult.put(mapEntry.getKey(), mapEntry.getValue());
        }

        return sortMapResult;
    }

    private static class ElementPath {
        public Location                    location;

        public List<MoveActionCommandEnum> path = new ArrayList<>();
    }

}
