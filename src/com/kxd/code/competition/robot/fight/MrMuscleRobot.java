package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * 羞羞的铁拳队（柳昆&蒋任胤）
 * 
 * Created by jiangry on 2018/1/22.
 */
public class MrMuscleRobot extends AbstractFightRobot {
    public MrMuscleRobot() {
        super("MrMuscle");
    }

    @Override
    public CommonMoveAction getNextAction() {
        Location currentLocation = getCurrentLocation();
        List<Location> oneStepAroundLocations = getOneStepAroundLocations(currentLocation);
        MoveActionCommandEnum moveAction = getNextMoveAction(oneStepAroundLocations);
        CommonMoveAction actionEntity = new CommonMoveAction(moveAction);
        return actionEntity;
    }

    private Location getCurrentLocation() {
        return currentSeeMazeSituation.robotBaseInfo.currentLocation;
    }

    private int getMapSizeIndex() {
        return currentSeeMazeSituation.mapInfo.size - 1;
    }

    private boolean isOutOfBounds(Location location) {
        if (location.x < 0 || location.x > getMapSizeIndex() || location.y < 0 || location.y > getMapSizeIndex()) {
            return true;
        } else {
            return false;
        }
    }

    private List<Location> getOneStepAroundLocations(Location currentLocation) {
        List<Location> list = new ArrayList<>();
        Location up = new Location(currentLocation.x - 1, currentLocation.y);
        Location right = new Location(currentLocation.x, currentLocation.y + 1);
        Location down = new Location(currentLocation.x + 1, currentLocation.y);
        Location left = new Location(currentLocation.x, currentLocation.y - 1);
        addLocation(list, up);
        addLocation(list, right);
        addLocation(list, down);
        addLocation(list, left);
        return list;
    }

    private List<Location> addLocation(List<Location> list, Location location) {
        if (!isOutOfBounds(location)) {
            list.add(location);
        }
        return list;
    }

    private MoveActionCommandEnum getNextMoveAction(List<Location> list) {
        FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
        ElementList[][] elementLists = fightRobotSeeEntity.robotGetElementList;
        for (Location location : list) {
            for (AbstractElement element : elementLists[location.x][location.y].elements) {
                if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                    return getMoveAction(location);
                }
            }
        }
        for (Location location : list) {
            for (AbstractElement element : elementLists[location.x][location.y].elements) {
                if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    return getMoveAction(location);
                }
            }
        }
        try {
            for (int i = list.size() - 1; i <= 0; i--) {
                Location location = list.get(i);
                List<Location> twoStepAroundLocations = getOneStepAroundLocations(location);
                for (Location loc : twoStepAroundLocations) {
                    if (loc.equals(getCurrentLocation())) {
                        continue;
                    }
                    for (AbstractElement element : elementLists[loc.x][loc.y].elements) {
                        if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                            list.remove(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        if (list.size() == 0) {
            return getRandomMoveAction();
        }
        Location bestLocation = getRandomLocation(list);
        int argument = 0;
        int index = 1;
        for (Location location : list) {
            List<Location> tempList = new ArrayList<>();
            List<Location> twoStepAroundLocations = getOneStepAroundLocations(location);
            tempList.addAll(twoStepAroundLocations);
            for (Location twoStepLocation : twoStepAroundLocations) {
                List<Location> threeStepAroundLocations = getOneStepAroundLocations(twoStepLocation);
                tempList.addAll(threeStepAroundLocations);
                for (Location threeStepLocation : threeStepAroundLocations) {
                    List<Location> fourStepAroundLocations = getOneStepAroundLocations(threeStepLocation);
                    tempList.addAll(fourStepAroundLocations);
                }
            }
            tempList = removeDuplicate(tempList);
            int tempArg = 0;
            for (AbstractElement element : elementLists[location.x][location.y].elements) {
                if (ElementTypeEnum.LANDMINE == element.elementType) {
                    tempArg -= 6;
                }
            }
            for (Location tempLocation : tempList) {
                if (tempLocation.equals(getCurrentLocation())) {
                    continue;
                }
                for (AbstractElement element : elementLists[tempLocation.x][tempLocation.y].elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        tempArg += 2;
                    } else if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                        tempArg += 2;
                    } else if (ElementTypeEnum.LANDMINE == element.elementType) {
                        tempArg -= 2;
                    } else {
                        tempArg += 1;
                    }
                }
            }
            if (index == 1 || tempArg > argument) {
                argument = tempArg;
                bestLocation = location;
                // System.out.println("argument:" + argument + ";moveAction:" +
                // getMoveAction(bestLocation));
            }
            index++;
        }
        return getMoveAction(bestLocation);
    }

    private MoveActionCommandEnum getMoveAction(Location location) {
        Location currentLocation = getCurrentLocation();
        if (location.x < currentLocation.x) {
            return MoveActionCommandEnum.MOVE_TOP;
        } else if (location.x > currentLocation.x) {
            return MoveActionCommandEnum.MOVE_DOWN;
        } else if (location.y > currentLocation.y) {
            return MoveActionCommandEnum.MOVE_RIGHT;
        } else {
            return MoveActionCommandEnum.MOVE_LEFT;
        }
    }

    private MoveActionCommandEnum getRandomMoveAction() {
        List<Location> list = getOneStepAroundLocations(getCurrentLocation());
        Random random = new Random(System.currentTimeMillis());
        int index = Math.abs(random.nextInt()) % list.size();
        return getMoveAction(list.get(index));
    }

    private Location getRandomLocation(List<Location> list) {
        Random random = new Random(System.currentTimeMillis());
        int index = Math.abs(random.nextInt()) % list.size();
        return list.get(index);
    }

    private List<Location> removeDuplicate(List<Location> duplicatedList) {
        HashSet hashSet = new HashSet(duplicatedList);
        return new ArrayList<>(hashSet);
    }
}
