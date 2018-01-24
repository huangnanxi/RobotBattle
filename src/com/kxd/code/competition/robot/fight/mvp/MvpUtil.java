package com.kxd.code.competition.robot.fight.mvp;


import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MvpUtil {

    public Location                           currentRobotLocation;

    public FightRobotSeeEntity                fightRobotSeeEntity;

    public FightRobotBaseInfo fightRobotBaseInfo;

    public AbstractMapInfo                    mapInfo;

    public MvpUtil(FightRobotSeeEntity fightRobotSeeEntity) {

        this.fightRobotSeeEntity = fightRobotSeeEntity;
        this.fightRobotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
        this.mapInfo = fightRobotSeeEntity.mapInfo;
        this.currentRobotLocation = this.fightRobotBaseInfo.currentLocation;
    }

    public static int[] dirx = {0, 1, 0, -1};
    public static int[] diry = {1, 0, -1, 0};

    public void PrintMap(ElementList[][] elementLists) {
        for (int i = 0; i < elementLists.length; i++) {
            for (int j = 0; j < elementLists.length; j++) {
                ElementList elementList = elementLists[i][j];
                for (AbstractElement element : elementList.elements) {
                }
            }
        }
    }


    public List<Location> getAroundLocations(Location currentLocation) {
        List<Location> locations = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            Location nextLocation = new Location(0, 0);
            nextLocation.x = currentLocation.x + dirx[i];
            nextLocation.y = currentLocation.y + diry[i];

            if(!isValidLocation(nextLocation.x, nextLocation.y))
            {
                continue;
            }

            locations.add(nextLocation);
        }

        return locations;
    }

    public boolean isValidLocation(int x, int y)
    {
        if(x < 0 || x >= mapInfo.size || y < 0 || y >= mapInfo.size)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public <T> boolean isEmpty(Collection<T> collection) {
        if (collection == null) {
            return true;
        }
        return collection.isEmpty();
    }

    public CommonMoveAction getActionToTarget(Location targetLocation) {

        Location currentLocation = this.currentRobotLocation;

        if (targetLocation == null) {
            return null;
        }

        MoveActionCommandEnum moveActionCommandEnum;

        if (targetLocation.x < currentLocation.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_TOP;
        }
        else if (targetLocation.x > currentLocation.x) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_DOWN;
        }
        else if (targetLocation.y < currentLocation.y) {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_LEFT;
        }
        else {
            moveActionCommandEnum = MoveActionCommandEnum.MOVE_RIGHT;
        }

        CommonMoveAction action = new CommonMoveAction(moveActionCommandEnum);
        return action;
    }

    public Location getNextLocation(Location targetLocation)
    {
        Location currentLocation = this.currentRobotLocation;
        Location nextLocation = new Location(currentLocation.x, currentLocation.y);

        if (targetLocation.x < currentLocation.x) {
            nextLocation.x--;
        }
        else if (targetLocation.x > currentLocation.x) {
            nextLocation.x++;
        }
        else if (targetLocation.y < currentLocation.y) {
            nextLocation.y--;
        }
        else {
            nextLocation.y++;
        }
        return nextLocation;
    }

    //TODO modify
    public void addNextLocations(List<Location> nextLoctations, Location targetLocation)
    {
        Location currentLocation = this.currentRobotLocation;

        if (targetLocation.x < currentLocation.x) {
            Location nextLocation = new Location(currentLocation.x - 1, currentLocation.y);
            nextLoctations.add(nextLocation);
        }
        if (targetLocation.x > currentLocation.x) {
            Location nextLocation = new Location(currentLocation.x + 1, currentLocation.y);
            nextLoctations.add(nextLocation);
        }
        if (targetLocation.y < currentLocation.y) {
            Location nextLocation = new Location(currentLocation.x, currentLocation.y - 1);
            nextLoctations.add(nextLocation);
        }
        if (targetLocation.y > currentLocation.y) {
            Location nextLocation = new Location(currentLocation.x, currentLocation.y + 1);
            nextLoctations.add(nextLocation);
        }
    }

    public List<Location> getNextLocations(List<Location> locations)
    {
        List<Location> nextLocations = new ArrayList<>();
        for(Location location : locations) {
//            Location nextLocation = getNextLocation(location);
//            nextLocations.add(nextLocation);
            addNextLocations(nextLocations, location);
        }
        return nextLocations;
    }
}
