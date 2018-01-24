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

/**
 * Created by dingxd on 2018/1/18.
 */
public class LittleBaby extends AbstractFightRobot {

	public LittleBaby(String name) {
		super(name);
	}

	public LittleBaby() {
		super("LittleBaby");
	}

	private CommonMoveAction secondMove;

	private List<FightRobotBaseInfo> otherRobotList;

	private List<Location> bloodBagLocations;

	private List<Location> otherRobotLocations;

	private List<Location> landmineLocations;

	private int[][] map;

	private List<List<Location>> availableLocations;

	private ElementList[][] elementLists;

	private int currentCanStepNum;

	private List<Location> minLandmineLocations;

	private Integer currentBloodNum;

	private Integer mapSize;

	private Location currentLocation;

	@Override
	public CommonMoveAction getNextAction() {
		try {
			// 第二步移动
			if (null != secondMove) {
				CommonMoveAction temp = secondMove;
				secondMove = null;
				return temp;
			}
			// init 场面数据
			initAllInstance();

			List<Location> targetLocation;
			// 找出能移动的位置(如果步数是2，一定要走两步,不能超出边界，优先走对角线)
			availableLocations = generateAvailableLocationByStepNum(currentLocation, currentCanStepNum, mapSize);

			// 排除掉移动后就会被别人比自己血多的人攻击的位置
			availableLocations = eliminateAvailableLocationByAvoidBeAttacked(availableLocations, elementLists, mapSize,false);

			// 位置上有没有机器人，有就攻击
			// 没有人比我血多
			if(noOtherRobotHasMoreBlood()){
				targetLocation = tryToAttack(availableLocations, elementLists);
				if (null != targetLocation) {
					return generateMoveAction(currentLocation, targetLocation);
				}
			}

			// 位置上有没有血包，有就直接移动
			targetLocation = tryToGetBloodBag(availableLocations, elementLists);
			if (null != targetLocation) {
				return generateMoveAction(currentLocation, targetLocation);
			}

			// 排除掉地雷
			availableLocations = eliminateAvailableLocationByLandmine();

			// 向离自己最近的血包移动
			targetLocation = tryToApproachBloodBag();
			if (null != targetLocation) {
				return generateMoveAction(currentLocation, targetLocation);
			}

			// 向奇数步数的敌人移动
			if (otherRobotList != null && otherRobotList.size() == 1&&noOtherRobotHasMoreBlood()) {
				targetLocation = tryToApproachNearestOddStepEnemy();
				if (null != targetLocation) {
					return generateMoveAction(currentLocation, targetLocation);
				}
			}

			// 向雷最少的，最中心的区域移动
			targetLocation = tryToApproachCenter(availableLocations, mapSize);
			if (null != targetLocation) {
				return generateMoveAction(currentLocation, targetLocation);
			}

			// 随机移动
			targetLocation = tryToMoveRandom(availableLocations);
			if (null != targetLocation) {
				return generateMoveAction(currentLocation, targetLocation);
			}

			// 得到边界内完全随机的movement
			return generateAllRandomMoveActionWhenExeception(mapSize, currentLocation);
		} catch (Exception e) {
			e.printStackTrace();
			return generateAllRandomMoveActionWhenExeception(
					((FightRobotSeeEntity) currentSeeMazeSituation).mapInfo.size,
					((FightRobotSeeEntity) currentSeeMazeSituation).robotBaseInfo.currentLocation);
		}
	}

	private List<Location> tryToApproachNearestOddStepEnemy() {
		int myStepNum = 0, minStepNum = Integer.MAX_VALUE;
		List<Location> targetRobotLocations = new ArrayList<>();
		for (Location otherRobotLocation : otherRobotLocations) {
			myStepNum = findShorestPathStepNum(currentLocation, otherRobotLocation);
			if (myStepNum % 2 == 1 && myStepNum < minStepNum) {
				minStepNum = myStepNum;
				targetRobotLocations.clear();
				targetRobotLocations.add(otherRobotLocation);
			}
		}

		return findShortestPathToTarget(availableLocations, targetRobotLocations);
	}

	private boolean noOtherRobotHasMoreBlood() {
		for (FightRobotBaseInfo robot : otherRobotList) {
			if (robot.bloodNum >= currentBloodNum) {
				return false;
			}
		}
		return true;
	}
	private void initAllInstance() {
		FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
		FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
		elementLists = fightRobotSeeEntity.robotGetElementList;
		currentLocation = fightRobotSeeEntity.robotBaseInfo.currentLocation;
		currentCanStepNum = fightRobotSeeEntity.robotBaseInfo.currentCanStepNum;
		currentBloodNum = fightRobotBaseInfo.bloodNum;
		mapSize = fightRobotSeeEntity.mapInfo.size;
		// 得到所有机器人
		searchOtherRobot(elementLists);
		// 得到所有血包
		bloodBagLocations = searchTargetLocation(elementLists, ElementTypeEnum.BLOOD_BAG);
		// 得到所有地雷
		landmineLocations = searchTargetLocation(elementLists, ElementTypeEnum.LANDMINE);
		// 初始化迷宫地图
		map = initMazeMap(mapSize, landmineLocations);
		// 初始化雷最少的区域
		minLandmineLocations = initMinLandmineLocations(mapSize);
	}

	private int[][] initMazeMap(Integer mapSize, List<Location> landmineLocations) {
		int[][] map = new int[mapSize][mapSize];
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++) {
				map[i][j] = 1;
			}
		}
		for (Location location : landmineLocations) {
			map[location.y][location.x] = 0;
		}
		return map;
	}

	private List<Location> tryToApproachBloodBag() {
		int myStepNum = 0, otherRobotStep = 0;
		List<Location> targetBloodBagLocations = new ArrayList<>();
		boolean iAmTheNearest = true;
		for (Location bloodBagLocation : bloodBagLocations) {
			myStepNum = findShorestPathStepNum(currentLocation, bloodBagLocation);

			// 如果离这个血包，有比自己距离更近的机器人，则放弃
			iAmTheNearest = true;
			for (Location otherRobotLocation : otherRobotLocations) {
				otherRobotStep = findShorestPathStepNum(otherRobotLocation, bloodBagLocation);
				if (otherRobotStep < myStepNum) {
					iAmTheNearest = false;
					break;
				}
			}
			if(iAmTheNearest){
				targetBloodBagLocations.add(bloodBagLocation);
			}
		}

		// 向最近的血包移动
		return findShortestPathToTarget(availableLocations, bloodBagLocations);
	}

	private List<Location> findShortestPathToTarget(List<List<Location>> availableLocations,
			List<Location> targetLocations) {
		List<List<Location>> result = new ArrayList<>();
		int distance = 0, minDistance = Integer.MAX_VALUE;
		Location temp = null;
		for (Location targetLocation : targetLocations) {
			for (List<Location> availableLocation : availableLocations) {
				temp = availableLocation.get(availableLocation.size() - 1);
				distance = findShorestPathStepNum(temp, targetLocation);
				if (distance < minDistance) {
					minDistance = distance;
					result.clear();
					result.add(availableLocation);
				} else if (distance == minDistance) {
					result.add(availableLocation);
				}
			}
		}

		if (result.size() == 0) {
			return null;
		} else {
			Random random = new Random();
			return result.get(random.nextInt(result.size()));
		}
	}

	private boolean hasTarget(List<AbstractElement> elementList, ElementTypeEnum elementType) {
		for (AbstractElement element : elementList) {
			if (element.elementType == elementType) {
				if (element instanceof AbstractRobotBaseInfo) {
					AbstractRobotBaseInfo robot = (AbstractRobotBaseInfo) element;
					if (!robot.name.equals(this.name)) {
						return true;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	private List<Location> tryToAttack(List<List<Location>> availableLocation, ElementList[][] elementLists) {
		return tryToMove(availableLocation, elementLists, ElementTypeEnum.ROBOT_INFO);
	}

	private List<Location> tryToMove(List<List<Location>> availableLocation, ElementList[][] elementLists,
			ElementTypeEnum moveTargetType) {
		List<List<Location>> toRemoveLocations = new ArrayList<>();
		for (List<Location> locations : availableLocation) {
			if (locations.size() == 1) {
				// 只能走一步：直接移动攻击
				if (hasTarget(elementLists[locations.get(0).x][locations.get(0).y].elements, moveTargetType)) {
					return locations;
				}
			} else {
				// 能够两步
				// 第一步可以攻击，且第二步不能有雷的路
				if (hasTarget(elementLists[locations.get(0).x][locations.get(0).y].elements, moveTargetType)) {
					if (!hasTarget(elementLists[locations.get(1).x][locations.get(1).y].elements,
							ElementTypeEnum.LANDMINE)) {
						return locations;
					}
				}

				// 第一步没有雷，第二步能攻击的路
				if (!hasTarget(elementLists[locations.get(0).x][locations.get(0).y].elements,
						ElementTypeEnum.LANDMINE)) {
					if (hasTarget(elementLists[locations.get(1).x][locations.get(1).y].elements, moveTargetType)) {
						// 如果原来位置上敌人的血比我，不走回原来位置
						boolean iHaveMoreBlood = true;
						if (locations.get(1).equals(currentLocation)) {
							for (FightRobotBaseInfo otherRobot : otherRobotList) {
								if (otherRobot.currentLocation.equals(currentLocation)) {
									// 对手的血量多
									if (otherRobot.bloodNum > currentBloodNum) {
										iHaveMoreBlood = false;
									}
								}
							}
						}
						if (iHaveMoreBlood&&!toRemoveLocations.contains(locations)) {
							return locations;
						} else {
							toRemoveLocations.add(locations);
						}
					}

					// 如果当前血量大于2，且目标是血包，第一步可以有雷
					if (currentBloodNum > 2&&moveTargetType == ElementTypeEnum.BLOOD_BAG) {
						if (hasTarget(elementLists[locations.get(0).x][locations.get(0).y].elements,
								ElementTypeEnum.LANDMINE)
								&& hasTarget(elementLists[locations.get(1).x][locations.get(1).y].elements,
								moveTargetType)) {
							return locations;
						}
					}
				}
			}
		}

		if (!toRemoveLocations.isEmpty()) {
			List<List<Location>> availableLocationsCopy = copy(availableLocations);
			availableLocationsCopy.removeAll(toRemoveLocations);
			if (!availableLocationsCopy.isEmpty()) {
				availableLocations = availableLocationsCopy;
			}
		}

		return null;
	}

	private List<Location> tryToMoveRandom(List<List<Location>> availableLocation) {
		Random random = new Random();
		return availableLocation.get(random.nextInt(availableLocation.size()));
	}

	private List<Location> tryToApproachCenter(List<List<Location>> availableLocations, int mapSize) {
		double centerPoint = mapSize / 2.0;

		// 找到离中心最近的
		List<Location> closeToCenterLocations = new ArrayList<>();
		double minDistance = Double.MAX_VALUE;
		double xDistance = 0;
		double yDistance = 0;
		double distance = 0;
		for (Location location : minLandmineLocations) {
			xDistance = Math.abs(centerPoint - location.x);
			yDistance = Math.abs(centerPoint - location.y);
			distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
			if (distance < minDistance) {
				minDistance = distance;
				closeToCenterLocations.clear();
				closeToCenterLocations.add(location);
			}
		}

		return findShortestPathToTarget(availableLocations, closeToCenterLocations);
	}

	private List<Location> initMinLandmineLocations(int mapSize) {
		// 寻找3*3区域内雷最少，距离中心最近的点
		int areaSize = 3;
		List<Location> minLandmineLocations = new ArrayList<>();

		int center = mapSize / 2;

		Location tempLocation = null;
		int maxLandmineNum = areaSize * areaSize;
		int tempLandmineNum = 0;
		for (int i = center - areaSize; i < center + areaSize&&i<mapSize; i++) {
			for (int j = center - areaSize; j < center + areaSize&&i<mapSize; j++) {
				tempLocation = new Location(i, j);
				tempLandmineNum = 0;
				for (int ii = i - 1; ii < i + 2; ii++) {
					for (int jj = j - 1; jj < j + 2; jj++) {
						if (map[jj][ii] == 0) {
							tempLandmineNum++;
						}
					}
				}
				if (maxLandmineNum > tempLandmineNum) {
					maxLandmineNum = tempLandmineNum;
					minLandmineLocations.clear();
					minLandmineLocations.add(tempLocation);
				} else if (maxLandmineNum == tempLandmineNum) {
					minLandmineLocations.add(tempLocation);
				}
			}
		}
		return minLandmineLocations;
	}

	private List<Location> tryToGetBloodBag(List<List<Location>> availableLocation, ElementList[][] elementLists) {
		return tryToMove(availableLocation, elementLists, ElementTypeEnum.BLOOD_BAG);
	}

	private CommonMoveAction generateMoveAction(Location currentLocation, List<Location> targetLocation) {
		if (1 == targetLocation.size()) {
			return covertMoveAction(currentLocation, targetLocation.get(0));
		} else {
			secondMove = covertMoveAction(targetLocation.get(0), targetLocation.get(1));
			return covertMoveAction(currentLocation, targetLocation.get(0));
		}
	}

	private CommonMoveAction generateAllRandomMoveActionWhenExeception(Integer mapSize, Location currentLocation) {
		List<Location> locations = inBoundaryMove(mapSize, currentLocation);
		Random random = new Random();
		Location targetLocation = locations.get(random.nextInt(locations.size()));
		return covertMoveAction(currentLocation, targetLocation);
	}

	private CommonMoveAction covertMoveAction(Location currentLocation2, Location targetLocation) {
		if (currentLocation2.x < targetLocation.x) {
			return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
		} else if (currentLocation2.x > targetLocation.x) {
			return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
		} else if (currentLocation2.y < targetLocation.y) {
			return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
		} else {
			return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
		}
	}

	private List<List<Location>> eliminateAvailableLocationByLandmine() {
		List<List<Location>> availableLocationsCopy = copy(availableLocations);
		Set<List<Location>> temp;

		if (1 == currentCanStepNum) {
			// 只能走一步
			temp = new HashSet<>();
			for (List<Location> availableLocation : availableLocationsCopy) {
				if (hasTarget(elementLists[availableLocation.get(0).x][availableLocation.get(0).y].elements,
						ElementTypeEnum.LANDMINE)) {
					// 区域里有雷，需要排掉
					temp.add(availableLocation);
				}
			}

			availableLocationsCopy.removeAll(temp);
			if (availableLocationsCopy.isEmpty()) {
				// 如果所有位置都被排掉了，就直接返回原始位置
				return availableLocations;
			} else {
				return availableLocationsCopy;
			}
		} else {
			// 能走两步
			// 排除掉两步都有雷的情况
			availableLocationsCopy = copy(availableLocations);
			temp = new HashSet<>();
			for (List<Location> availableLocation : availableLocationsCopy) {
				List<AbstractElement> firstStepOn = elementLists[availableLocation.get(0).x][availableLocation
						.get(0).y].elements;
				List<AbstractElement> secondStepOn = elementLists[availableLocation.get(1).x][availableLocation
						.get(1).y].elements;
				if (hasTarget(firstStepOn, ElementTypeEnum.LANDMINE)
						&& hasTarget(secondStepOn, ElementTypeEnum.LANDMINE)) {
					// 连续两步都有雷，需要排掉
					temp.add(availableLocation);
				}
			}

			availableLocationsCopy.removeAll(temp);
			if (availableLocationsCopy.isEmpty()) {
				// 如果所有位置都被排掉了，就直接返回原始位置
				return availableLocations;
			} else {
				// 排除掉一步有雷的情况
				availableLocations = availableLocationsCopy;
				availableLocationsCopy = copy(availableLocations);
				temp = new HashSet<>();
				for (List<Location> availableLocation : availableLocationsCopy) {
					List<AbstractElement> firstStepOn = elementLists[availableLocation.get(0).x][availableLocation
							.get(0).y].elements;
					List<AbstractElement> secondStepOn = elementLists[availableLocation.get(1).x][availableLocation
							.get(1).y].elements;
					if (hasTarget(firstStepOn, ElementTypeEnum.LANDMINE)
							|| hasTarget(secondStepOn, ElementTypeEnum.LANDMINE)) {
						// 任意一步有雷，需要排掉
						temp.add(availableLocation);
					}
				}
				availableLocationsCopy.removeAll(temp);
				if (availableLocationsCopy.isEmpty()) {
					// 如果所有位置都被排掉了，就直接返回原始位置
					return availableLocations;
				} else {
					return availableLocationsCopy;
				}
			}
		}
	}

	private List<List<Location>> eliminateAvailableLocationByAvoidBeAttacked(List<List<Location>> availableLocations,
			ElementList[][] elementLists, Integer mapSize, boolean iHaveMoreBlood) {
		// 得到所有机器人下一步可能的位置
		Set<Location> potentialLocationsOfOtherRobot = new HashSet<>();
		for (FightRobotBaseInfo otherRobot : otherRobotList) {
			if (iHaveMoreBlood && otherRobot.bloodNum >= currentBloodNum) {
				// 只需要排除掉所有比我血量高的敌人
				List<List<Location>> otherRobotAvailableLocations = generateAvailableLocationByStepNum(
						otherRobot.currentLocation, otherRobot.currentCanStepNum, mapSize);
				for (List<Location> otherRobotAvailableLocation : otherRobotAvailableLocations) {
					potentialLocationsOfOtherRobot
							.add(otherRobotAvailableLocation.get(otherRobotAvailableLocation.size() - 1));
				}
			} else if (!iHaveMoreBlood) {
				// 排除掉所有敌人
				List<List<Location>> otherRobotAvailableLocations = generateAvailableLocationByStepNum(
						otherRobot.currentLocation, otherRobot.currentCanStepNum, mapSize);
				for (List<Location> otherRobotAvailableLocation : otherRobotAvailableLocations) {
					potentialLocationsOfOtherRobot
							.add(otherRobotAvailableLocation.get(otherRobotAvailableLocation.size() - 1));
				}
			}
		}

		List<List<Location>> availableLocationsCopy = copy(availableLocations);

		// 排除掉可能被其他机器人攻击的区域
		Set<List<Location>> temp = new HashSet<>();
		for (List<Location> availableLocation : availableLocationsCopy) {
			for (Location potentialLocationOfOtherRobot : potentialLocationsOfOtherRobot) {
				// 危险类型是机器人，只要最后的位置被踢掉就可以
				if (potentialLocationOfOtherRobot.equals(availableLocation.get(availableLocation.size() - 1))) {
					temp.add(availableLocation);
				}
			}
		}
		availableLocationsCopy.removeAll(temp);
		if (availableLocationsCopy.isEmpty()) {
			// 如果所有位置都被排掉了，就直接返回原始位置
			return availableLocations;
		} else {
			return availableLocationsCopy;
		}
	}

	private List<List<Location>> copy(List<List<Location>> source) {
		List<List<Location>> result = new ArrayList<>();
		for (List<Location> sourceLocations : source) {
			List<Location> targetLocations = new ArrayList<>();
			for (Location sourceLocation : sourceLocations) {
				targetLocations.add(new Location(sourceLocation.x, sourceLocation.y));
			}
			result.add(targetLocations);
		}
		return result;
	}

	private void searchOtherRobot(ElementList[][] elementLists) {
		otherRobotLocations = new ArrayList<>();
		otherRobotList = new ArrayList<>();
		List<AbstractElement> targetList = searchTarget(elementLists, ElementTypeEnum.ROBOT_INFO);
		FightRobotBaseInfo robot = null;
		for (AbstractElement element : targetList) {
			robot = (FightRobotBaseInfo) element;
			if (!robot.name.equals(this.getName())) {
				otherRobotList.add(robot);
				otherRobotLocations.add(robot.currentLocation);
			}
		}

		// 初始化其他机器人下一回合的移动步数
		//		List<AbstractElement> elementsOnOtherRobotLocation = null;
		//		for (FightRobotBaseInfo otherRobot : otherRobotList) {
		//			elementsOnOtherRobotLocation = elementLists[otherRobot.currentLocation.x][otherRobot.currentLocation.y].elements;
		//			if (elementsOnOtherRobotLocation.size() > 1
		//					&& elementsOnOtherRobotLocation.get(0).elementType == ElementTypeEnum.ROBOT_INFO) {
		//				robot = (FightRobotBaseInfo) elementsOnOtherRobotLocation.get(0);
		//				if (!robot.name.equals(otherRobot.name)) {
		//					robot = (FightRobotBaseInfo) elementsOnOtherRobotLocation.get(elementsOnOtherRobotLocation.size()-1);
		//					if (!robot.name.equals(otherRobot.name)) {
		//						otherRobot.currentCanStepNum = 2;
		//					}
		//					for (int i = 0; i < elementsOnOtherRobotLocation.size(); i++) {
		//						if (elementsOnOtherRobotLocation.get(i) instanceof AbstractRobotBaseInfo) {
		//							robot = (FightRobotBaseInfo) elementsOnOtherRobotLocation.get(i);
		//							if (robot.name.equals(otherRobot.name) && i != elementsOnOtherRobotLocation.size()-1) {
		//							}
		//						}
		//					}
		//				}
		//			}
		//		}

	}

	private List<AbstractElement> searchTarget(ElementList[][] elementLists, ElementTypeEnum elementType) {
		List<AbstractElement> targetList = new ArrayList<>();
		for (int i = 0; i < elementLists.length; i++) {
			for (int j = 0; j < elementLists.length; j++) {
				ElementList elementList = elementLists[i][j];
				for (AbstractElement element : elementList.elements) {
					if (elementType == element.elementType) {
						targetList.add(element);
					}
				}
			}
		}
		return targetList;
	}

	private List<Location> searchTargetLocation(ElementList[][] elementLists, ElementTypeEnum elementType) {
		List<Location> locationList = new ArrayList<>();
		for (int i = 0; i < elementLists.length; i++) {
			for (int j = 0; j < elementLists.length; j++) {
				ElementList elementList = elementLists[i][j];
				if (hasTarget(elementList.elements, elementType)) {
					locationList.add(new Location(i, j));
				}
			}
		}
		return locationList;
	}

	private List<List<Location>> generateAvailableLocationByStepNum(Location currentLocation, Integer currentCanStepNum,
			Integer mapSize) {
		List<List<Location>> result = new ArrayList<>();

		if (1 != currentCanStepNum && 2 != currentCanStepNum) {
			System.out.println("LittleBaby Cry: error currentCanStepNum:" + currentCanStepNum);
		}

		// 得到边界内的上下左右四个位置
		List<Location> firstStepList = inBoundaryMove(mapSize, currentLocation);

		if (1 == currentCanStepNum) {
			return covert(firstStepList);
		} else {
			// 可以移动两步
			// List<Location> finalLocationList = new ArrayList<>();
			List<Location> temp;
			for (Location firstLocation : firstStepList) {
				// 得到边界内第二步的上下左右四个位置
				List<Location> secondLocationList = inBoundaryMove(mapSize, firstLocation);
				for (Location secondLocation : secondLocationList) {
					// 循环第二步，去重复路径
					// if (!finalLocationList.contains(secondLocation)) {
					// finalLocationList.add(secondLocation);
					temp = new ArrayList<>();
					temp.add(firstLocation);
					temp.add(secondLocation);
					result.add(temp);
					// }
				}
			}

			// 优先走对角线
			List<List<Location>> startResult = new ArrayList<>();
			List<List<Location>> endResult = new ArrayList<>();
			for (List<Location> tempLocations : result) {
				if (1 == Math.abs(tempLocations.get(1).x - currentLocation.x)
						&& 1 == Math.abs(tempLocations.get(1).y - currentLocation.y)) {
					startResult.add(tempLocations);
				} else {
					endResult.add(tempLocations);
				}
			}

			Collections.shuffle(startResult);
			Collections.shuffle(endResult);

			result.clear();
			result.addAll(startResult);
			result.addAll(endResult);
			return result;
		}
	}

	private List<List<Location>> covert(List<Location> firstStepList) {
		List<List<Location>> result = new ArrayList<>();
		List<Location> temp;
		for (Location location : firstStepList) {
			temp = new ArrayList<>();
			temp.add(new Location(location.x, location.y));
			result.add(temp);
		}
		return result;
	}

	private List<Location> inBoundaryMove(Integer mapSize, Location location) {
		List<Location> result = new ArrayList<>();
		Location left = new Location(location.x - 1, location.y);
		Location right = new Location(location.x + 1, location.y);
		Location down = new Location(location.x, location.y + 1);
		Location up = new Location(location.x, location.y - 1);
		if (!(left.x >= mapSize || left.y >= mapSize || left.x < 0 || left.y < 0)) {
			result.add(left);
		}
		if (!(right.x >= mapSize || right.y >= mapSize || right.x < 0 || right.y < 0)) {
			result.add(right);
		}
		if (!(up.x >= mapSize || up.y >= mapSize || up.x < 0 || up.y < 0)) {
			result.add(up);
		}
		if (!(down.x >= mapSize || down.y >= mapSize || down.x < 0 || down.y < 0)) {
			result.add(down);
		}
		return result;
	}

	public static void main(String[] args) {
		LittleBaby littleBaby = new LittleBaby("littleBaby");
		/**
		 * test inBoundaryMove
		 */
		// for(Location location:littleBaby.inBoundaryMove(3,new
		// Location(2,2))){
		// System.out.println(location.x+","+location.y);
		// }

		/**
		 * test generateAvailableLocationByStepNum
		 */
		// for (List<Location> locationList :
		// littleBaby.generateAvailableLocationByStepNum(new Location(0, 0), 2,
		// 5)) {
		// for (Location location : locationList) {
		// System.out.print(location.x + "," + location.y);
		// System.out.print("->");
		// }
		// System.out.println();
		// }

		/**
		 * test eliminateDangerousLocation
		 */
		// for (List<Location> locationList : littleBaby
		// .eliminateDangerousLocation(littleBaby.generateAvailableLocationByStepNum(new
		// Location(0, 0), 2, 5),
		// new ArrayList<Location>() {{
		// add(new Location(0, 0));
		// add(new Location(0, 2));
		// add(new Location(2, 2));
		// add(new Location(1, 1));
		// add(new Location(2, 0));
		// }})) {
		// System.out.println(locationList.get(locationList.size()-1).x + "," +
		// locationList.get(locationList.size()-1).y);
		// }
	}

	private int findShorestPathStepNum(Location startLocation, Location endLocation) {
		Node startPoint = new LittleBaby().new Node(startLocation.y, startLocation.x, null);
		Node endPoint = new LittleBaby().new Node(endLocation.y, endLocation.x, null);
		int distance = seachWay(map, startPoint, endPoint, map.length, map[0].length);
		if(-1 == distance){
			return Integer.MAX_VALUE;
		}else{
			return distance;
		}
	}

	/**
	 * 搜寻最短路径
	 *
	 * @param arr
	 * @param startPoint
	 * @param endPoint
	 */
	private int seachWay(int[][] arr, Node startPoint, Node endPoint, int row, int col) {
		final int CONST_HENG = 10;// 垂直方向或水平方向移动的路径评分
		List<Node> openList = new ArrayList<Node>();// 开启列表
		List<Node> closeList = new ArrayList<Node>();// 关闭列表
		Node curNode = startPoint;
		if (startPoint.x < 0 || startPoint.y > col || endPoint.x < 0 || endPoint.y > col
				|| arr[startPoint.x][startPoint.y] == 0 || arr[endPoint.x][endPoint.y] == 0) {
			// throw new IllegalArgumentException("坐标参数错误！！");
			return -1;
		}

		openList.add(startPoint);
		while (!openList.isEmpty() && !openList.contains(endPoint)) {
			curNode = minList(openList);
			if (curNode.x == endPoint.x && curNode.y == endPoint.y || openList.contains(endPoint)) {
				// System.out.println("找到最短路径");
				int length = 0;
				while (!(curNode.x == startPoint.x && curNode.y == startPoint.y)) {
					length++;
					if (curNode.parentNode != null) {
						curNode = curNode.parentNode;
					}
				}
				return length;
			}
			// 上
			if (curNode.y - 1 >= 0) {
				checkPath(curNode.x, curNode.y - 1, curNode, endPoint, CONST_HENG, arr, openList, closeList);
			}
			// 下
			if (curNode.y + 1 < col) {
				checkPath(curNode.x, curNode.y + 1, curNode, endPoint, CONST_HENG, arr, openList, closeList);
			}
			// 左
			if (curNode.x - 1 >= 0) {
				checkPath(curNode.x - 1, curNode.y, curNode, endPoint, CONST_HENG, arr, openList, closeList);
			}
			// 右
			if (curNode.x + 1 < row) {
				checkPath(curNode.x + 1, curNode.y, curNode, endPoint, CONST_HENG, arr, openList, closeList);
			}
			openList.remove(curNode);
			closeList.add(curNode);
		}
		if (!openList.contains(endPoint)) {
			// System.out.println("一条路径都未找到！！！");
			return -1;
		}

		return -1;

	}

	// 核心算法---检测节点是否通路
	private boolean checkPath(int x, int y, Node preNode, Node endPoint, int c, int[][] map, List<Node> openList,
			List<Node> closeList) {
		Node node = new LittleBaby().new Node(x, y, preNode);
		// 查找地图中是否能通过
		if (map[x][y] == 0) {
			closeList.add(node);
			return false;
		}
		// 查找关闭列表中是否存在
		if (isListContains(closeList, x, y) != -1) {// 存在
			return false;
		}
		// 查找开启列表中是否存在
		int index = -1;
		if ((index = isListContains(openList, x, y)) != -1) {// 存在
			// G值是否更小，即是否更新G，F值
			if ((preNode.g + c) < openList.get(index).g) {
				countG(node, endPoint, c);
				countF(node);
				openList.set(index, node);
			}
		} else {
			// 不存在，添加到开启列表中
			node.setParentNode(preNode);
			count(node, endPoint, c);
			openList.add(node);
		}
		return true;
	}

	// 计算G,H,F值
	private void count(Node node, Node eNode, int cost) {
		countG(node, eNode, cost);
		countH(node, eNode);
		countF(node);
	}

	// 计算G值
	private void countG(Node node, Node eNode, int cost) {
		if (node.getParentNode() == null) {
			node.setG(cost);
		} else {
			node.setG(node.getParentNode().getG() + cost);
		}
	}

	// 计算H值
	private void countH(Node node, Node eNode) {
		node.setF((Math.abs(node.getX() - eNode.getX()) + Math.abs(node.getY() - eNode.getY())) * 10);
	}

	// 计算F值
	private void countF(Node node) {
		node.setF(node.getG() + node.getH());
	}

	// 集合中是否包含某个元素(-1：没有找到，否则返回所在的索引)
	private int isListContains(List<Node> list, int x, int y) {
		for (int i = 0; i < list.size(); i++) {
			Node node = list.get(i);
			if (node.getX() == x && node.getY() == y) {
				return i;
			}
		}
		return -1;
	}

	// 找最小值
	private Node minList(List<Node> list) {
		Iterator<Node> i = list.iterator();
		Node candidate = i.next();

		while (i.hasNext()) {
			Node next = i.next();
			if (next.compareTo(candidate) < 0)
				candidate = next;
		}
		return candidate;
	}

	// 节点类
	private class Node {
		private int x;// X坐标
		private int y;// Y坐标
		private Node parentNode;// 父类节点
		private int g;// 当前点到起点的移动耗费
		private int h;// 当前点到终点的移动耗费，即曼哈顿距离|x1-x2|+|y1-y2|(忽略障碍物)
		private int f;// f=g+h

		public Node(int x, int y, Node parentNode) {
			this.x = x;
			this.y = y;
			this.parentNode = parentNode;
		}

		public int compareTo(Node candidate) {
			return this.getF() - candidate.getF();
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public Node getParentNode() {
			return parentNode;
		}

		public void setParentNode(Node parentNode) {
			this.parentNode = parentNode;
		}

		public int getG() {
			return g;
		}

		public void setG(int g) {
			this.g = g;
		}

		public int getH() {
			return h;
		}

		public int getF() {
			return f;
		}

		public void setF(int f) {
			this.f = f;
		}

		public String toString() {
			return "(" + x + "," + y + "," + f + ")";
		}
	}

	// 节点比较类
	class NodeFComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			return o1.getF() - o2.getF();
		}

	}
}
