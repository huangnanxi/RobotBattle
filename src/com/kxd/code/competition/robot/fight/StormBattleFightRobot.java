package com.kxd.code.competition.robot.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.mapinfo.AbstractMapInfo;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.AbstractRobotBaseInfo;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;
import com.kxd.code.competition.robot.fight.StormBattleFightRobot.AStar.Node;

public class StormBattleFightRobot extends AbstractFightRobot {
	
	private static final String MODE_SINGLE = "1";
	
	private static final String MODE_CHICKEN = "2";
	
    public StormBattleFightRobot(String name) {
        super(name);
    }
    
	public StormBattleFightRobot() {
		super("StormBattle");
	}

	@Override
	public CommonMoveAction getNextAction() {
		Situation situation = judgeSituation();
		try {
			
			List<Action> actions = analysisActions(situation);
			Action finalAction = arbitrateActions(actions);
			MoveActionCommandEnum moveActionCommandEnum = finalAction.getNextMove();
			CommonMoveAction commonMoveAction = new CommonMoveAction(moveActionCommandEnum);
			return commonMoveAction;
		} catch (Exception e){
			e.printStackTrace();
		}
		return getOneStep(situation.ownPosition, situation).action;
//		ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
//		int xl = robotGetElementList.length;
//		int yl = robotGetElementList[0].length;
//		int ownPX = situation.ownPosition.x;
//		int ownPY = situation.ownPosition.y;
//		
//		if(ownPX - 1 >= 0){
//			return new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP);
//		}
//		if(ownPX + 1 <= xl - 1){
//			return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
//		}
//		if(ownPY - 1 >= 0){
//			return new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT);
//		}
//		
//		if(ownPY + 1 <= yl - 1){
//			return new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT);
//		}
//		return new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN);
	}
	
	private Situation judgeSituation(){
		Situation situation = new Situation();
		situation.setOwnName(this.name);
		FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
		situation.setFightRobotSeeEntity(fightRobotSeeEntity);
		
		FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo)fightRobotSeeEntity.robotBaseInfo;
		

		Location     currentLocation = fightRobotBaseInfo.currentLocation;
		situation.setOwnPosition(new Position(currentLocation.x,currentLocation.y));
		
		Integer      currentCanStepNum = fightRobotBaseInfo.currentCanStepNum;
		situation.setMoveCount(currentCanStepNum);
		
		Integer bloodNum = fightRobotBaseInfo.bloodNum;
		situation.setOwnBloodNum(bloodNum);
		
		AbstractMapInfo abstractMapInfo = fightRobotSeeEntity.mapInfo;
		Integer size = abstractMapInfo.size;
		String mode = null;
		if(size.compareTo(10) == 0){
			mode = MODE_SINGLE;
		}
		else if(size.compareTo(20) == 0){
			mode = MODE_CHICKEN;
		}
		situation.setMode(mode);
		int aliveEnemyCount = 0;
		int bloodBagCount = 0;
		int landmineCount = 0;
		ElementList[][] robotGetElementList = fightRobotSeeEntity.robotGetElementList;
		for (int i = 0; i < robotGetElementList.length; i++) {
            for (int j = 0; j < robotGetElementList.length; j++) {
                ElementList elementList = robotGetElementList[i][j];
                for (AbstractElement element : elementList.elements) {
                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                    	AbstractRobotBaseInfo abstractRobotBaseInfo = (AbstractRobotBaseInfo)element;
                    	if(!abstractRobotBaseInfo.name.equals(this.name)){
                    		aliveEnemyCount++;
                    	}
                    }
                    else if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
                    	bloodBagCount++;
                    }
                    else if (ElementTypeEnum.LANDMINE == element.elementType) {
                    	landmineCount++;
                    }
                }
            }
        }
		situation.setAliveEnemyCount(aliveEnemyCount);
		situation.setBloodBagCount(bloodBagCount);
		situation.setLandmineCount(landmineCount);
		
		return situation;
	}
	
	private List<Action> analysisActions(Situation situation){
		List<Action> actions = new ArrayList<Action>();
		EatBloodAction eatBloodAction = new EatBloodAction();
		eatBloodAction.deal(situation);
		actions.add(eatBloodAction);
		
		AttackAction attackAction = new AttackAction();
		attackAction.deal(situation);
		actions.add(attackAction);
		
		EscapeAction escapeAction = new EscapeAction();
		escapeAction.deal(situation);
		actions.add(escapeAction);
		return actions;
	}
	
	private Action arbitrateActions(List<Action> actions){
		Action selectedAction = null;
		for(Action action:actions){
			if(selectedAction == null){
				selectedAction = action;
			}
			else {
				if(action.weight == null){
					System.out.println("1111");
				}
				if(selectedAction.weight.compare(action.weight) <= 0){
					selectedAction = action;
				}
			}
		}
		System.out.println("selectedAction is " + selectedAction.getActionType());
		return selectedAction;
	}
	
    private static class OneStep{
    	public CommonMoveAction action;
    	public Position targetPos;
    	public int weight;//0:空地 20:坦克 -20:地雷
    	
		public OneStep(CommonMoveAction action, int weight, Position targetPos) {
			this.action = action;
			this.weight = weight;
			this.targetPos = targetPos;
		}
    }
    
    public static OneStep getOneStep(Position tank, Situation situation){
    	ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
		int xl = robotGetElementList.length;
		int yl = robotGetElementList[0].length;
		int ownPX = situation.ownPosition.x;
		int ownPY = situation.ownPosition.y;
		List<OneStep> canStep = new ArrayList<OneStep>();
		if(ownPX - 1 >= 0){
			canStep.add(new OneStep(new CommonMoveAction(MoveActionCommandEnum.MOVE_TOP),judgeOneStep(new Position(ownPX-1,ownPY), situation), new Position(ownPX-1,ownPY)));
		}
		if(ownPX + 1 <= xl - 1){
			canStep.add(new OneStep(new CommonMoveAction(MoveActionCommandEnum.MOVE_DOWN),judgeOneStep(new Position(ownPX+1,ownPY), situation), new Position(ownPX+1,ownPY)));
		}
		if(ownPY - 1 >= 0){
			canStep.add(new OneStep(new CommonMoveAction(MoveActionCommandEnum.MOVE_LEFT),judgeOneStep(new Position(ownPX,ownPY-1), situation), new Position(ownPX,ownPY-1)));
		}
		if(ownPY + 1 <= yl - 1){
			canStep.add(new OneStep(new CommonMoveAction(MoveActionCommandEnum.MOVE_RIGHT),judgeOneStep(new Position(ownPX,ownPY+1), situation), new Position(ownPX,ownPY+1)));
		}
		OneStep finalStep = canStep.get(0);
		for(OneStep o:canStep){
			if(o.weight >= finalStep.weight){
				finalStep = o;
			}
		}
		System.out.println(" **select final one step position(x"+finalStep.targetPos.x+",y"+finalStep.targetPos.y+") weight is " + finalStep.weight);
		return finalStep;
    }
    
    public static int judgeOneStep(Position target, Situation situation){
    	ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
    	List<AbstractElement> targetList = robotGetElementList[target.x][target.y].elements;
    	if(targetList.size() > 0){
	    	ElementTypeEnum targetType = targetList.get(0).elementType;
	    	switch (targetType){
	    	case LANDMINE:
	    		return -20;
	    	case BLOOD_BAG:
	    		return 40;
	    	case ROBOT_INFO:
	    		return 20;
	    	}
    	}else{
    		return 0;
    	}
		return 0;
    }
	
	private static class ActionTracer {
		
	}
	
	private static class Situation  {
		
		private String ownName;
		
		private int aliveEnemyCount = 0;
		
		private int bloodBagCount = 0;
		
		private int landmineCount = 0;
		
		private int ownBloodNum = 0;
		
		private String mode;
		
		private int moveCount = 1;
		
		private Position ownPosition = null;
		
		private FightRobotSeeEntity fightRobotSeeEntity;

		public String getOwnName() {
			return ownName;
		}

		public void setOwnName(String ownName) {
			this.ownName = ownName;
		}

		public int getLandmineCount() {
			return landmineCount;
		}

		public void setLandmineCount(int landmineCount) {
			this.landmineCount = landmineCount;
		}

		public int getBloodBagCount() {
			return bloodBagCount;
		}

		public void setBloodBagCount(int bloodBagCount) {
			this.bloodBagCount = bloodBagCount;
		}

		public FightRobotSeeEntity getFightRobotSeeEntity() {
			return fightRobotSeeEntity;
		}

		public void setFightRobotSeeEntity(FightRobotSeeEntity fightRobotSeeEntity) {
			this.fightRobotSeeEntity = fightRobotSeeEntity;
		}

		public int getMoveCount() {
			return moveCount;
		}

		public void setMoveCount(int moveCount) {
			this.moveCount = moveCount;
		}

		public Position getOwnPosition() {
			return ownPosition;
		}

		public void setOwnPosition(Position ownPosition) {
			this.ownPosition = ownPosition;
		}

		public int getAliveEnemyCount() {
			return aliveEnemyCount;
		}

		public void setAliveEnemyCount(int aliveEnemyCount) {
			this.aliveEnemyCount = aliveEnemyCount;
		}

		public int getOwnBloodNum() {
			return ownBloodNum;
		}

		public void setOwnBloodNum(int ownBloodNum) {
			this.ownBloodNum = ownBloodNum;
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}
	}
	
	private abstract static class Action {
		
		private Position target;
		
		private Route route;
		
		private Weight weight;
		
		private Position ownPosition;
		
		protected void deal(Situation situation){
			this.ownPosition = situation.ownPosition;
			ActionResult actionResult = doDeal(situation);
			this.target = actionResult.target;
			this.route = actionResult.route;
			this.weight = actionResult.getWeight();
		}
		
		protected abstract ActionResult doDeal(Situation situation);

		protected Position getNextPosition(){
			List<Position> positions = route.getPositions();
			boolean next = false;
			for(int i=0;i<positions.size();i++){
				Position position = positions.get(i);
				if(next){
					return position;
				}
				if(position.equals(ownPosition)){
					next = true;
				}
			}
			throw new RuntimeException("找不到下一步行动");
		}
		
		protected MoveActionCommandEnum getNextMove(){
			if(target == null){
				throw new RuntimeException("没有目标");
			}
			Position position = getNextPosition();
			if(position.x > ownPosition.x){
				return MoveActionCommandEnum.MOVE_DOWN;
			}
			else if(position.x < ownPosition.x){
				return MoveActionCommandEnum.MOVE_TOP;
			}
			else if(position.y > ownPosition.y) {
				return MoveActionCommandEnum.MOVE_RIGHT;
			}
			else if(position.y < ownPosition.y){
				return MoveActionCommandEnum.MOVE_LEFT;
			}
			else {
				throw new RuntimeException("无法识别下一步行动");
			}
		}
		
		protected GetRouteResult getRoute(Position start,Position end,ElementTypeEnum endType,Situation situation,int maxPermitLandmineCount){
			GetRouteResult getRouteResult = new GetRouteResult();
			int[][] node  = buildAStarNode(start,end,endType,situation);
			AStar aStar = new AStar(node);
			Node startNode = new Node(start.x,start.y);
			Node endNode = new Node(end.x,end.y);
			Node parent = aStar.findPath(startNode, endNode);
			List<Position> positions = new ArrayList<Position>();
			int cost = 0;
			if(parent != null){
				cost = parent.F;
			}
			if(cost / (-WeightManager.EVENT_LANDMINE_VALUE) > maxPermitLandmineCount){
				getRouteResult.setWeight(Weight.MIN_WEIGHT);
				return getRouteResult;
			}
			while(parent != null){
				
				positions.add(new Position(parent.x,parent.y));
				parent = parent.parent;
			}
			Route route = new Route();
			
			if(!positions.isEmpty()){
				Collections.reverse(positions);
				route.setPositions(positions);
				getRouteResult.setRoute(route);
				getRouteResult.setWeight(new Weight(-cost));
			}
			else {
				getRouteResult.setWeight(Weight.MIN_WEIGHT);
			}
			
			
			
			return getRouteResult;
		}
		
		private int[][] buildAStarNode(Position start,Position end,ElementTypeEnum endType,Situation situation){
			ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
			int[][] node  = new int[robotGetElementList.length][robotGetElementList[0].length];
			for (int i = 0; i < robotGetElementList.length; i++) {
	            for (int j = 0; j < robotGetElementList.length; j++) {
	                ElementList elementList = robotGetElementList[i][j];
	                for (AbstractElement element : elementList.elements) {
	                    if (ElementTypeEnum.LANDMINE == element.elementType) {
	                    	Weight weight = WeightManager.analysis(WeightManager.EVENT_LANDMINE, new HashMap<String,Object>());
	                    	node[i][j] = - weight.value;
	                    	//node[i][j] = 1;
	                    }
	                }
	            }
	        }
			
			List<FightRobotBaseInfo> enemys = this.findEnemys(situation);
			for(FightRobotBaseInfo enemy:enemys){
				Position p = this.getPosition(enemy);
				if(ElementTypeEnum.ROBOT_INFO.equals(endType)){
					if(p.equals(end) || p.equals(start)){
						continue;
					}
				}
				Weight weight = WeightManager.analysis(WeightManager.EVENT_BE_ATTACK, new HashMap<String,Object>());
				int cost = - weight.value;
				//int cost = 1;
				int left = p.x - 1;
				if(left >= 0){
					node[left][p.y] = cost;
				}
				int right = p.x + 1;
				if(right < node.length){
					node[right][p.y] = cost;
				}
				int up = p.y - 1;
				if(up >= 0){
					node[p.x][up] = cost;
				}
				int down = p.y + 1;
				if(down < node[0].length){
					node[p.x][down] = cost;
				}
			}
			return node;
		}
		
		static class ActionResult extends Result {
			
			protected Route route;
			
			protected Position target;

			public Route getRoute() {
				return route;
			}

			public void setRoute(Route route) {
				this.route = route;
			}

			public Position getTarget() {
				return target;
			}

			public void setTarget(Position target) {
				this.target = target;
			}
			
		}
		
		static class GetRouteResult extends Result {
			private Route route;

			public Route getRoute() {
				return route;
			}

			public void setRoute(Route route) {
				this.route = route;
			}
		}
		
		
		protected boolean nearBy(Position a,Position b,Situation situation){
			int moveCount = situation.getMoveCount();
			if(moveCount == 2){
				return a.equals(b);
			}
			if(a.x == b.x && (Math.abs(a.y - b.y) == 1)) {
				return true;
			}
			else if(a.y == b.y && (Math.abs(a.x - b.x) == 1)){
				return true;
			}
			else {
				return false;
			}
		}
		
		protected List<FightRobotBaseInfo> findEnemys(Situation situation){
			List<FightRobotBaseInfo> fightRobotBaseInfos = new ArrayList<FightRobotBaseInfo>();
			ElementList[][] robotGetElementList = situation.getFightRobotSeeEntity().robotGetElementList;
			 for (int i = 0; i < robotGetElementList.length; i++) {
		            for (int j = 0; j < robotGetElementList.length; j++) {
		                ElementList elementList = robotGetElementList[i][j];
		                for (AbstractElement element : elementList.elements) {
		                    if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
		                    	FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo)element;
		                    	if(!fightRobotBaseInfo.name.equals(situation.ownName)){
		                    		fightRobotBaseInfos.add(fightRobotBaseInfo);
		                    	}
		                    }
		                }
		            }
		        }
			return fightRobotBaseInfos;
		}
		
		
		protected List<FightRobotBaseInfo> getEnemyInfoList(Position p,Situation situation){
			List<FightRobotBaseInfo> fightRobotBaseInfoList = new ArrayList<FightRobotBaseInfo>();
			List<AbstractElement> abstractElementList = getElementList(p,situation,ElementTypeEnum.ROBOT_INFO);
			String ownName = situation.getOwnName();
			for(AbstractElement abstractElement:abstractElementList){
				FightRobotBaseInfo fightRobotBaseInfo = (FightRobotBaseInfo)abstractElement;
				if(!fightRobotBaseInfo.name.equals(ownName)){
					fightRobotBaseInfoList.add(fightRobotBaseInfo);
				}
			}
			return fightRobotBaseInfoList;
		}
		
		
		protected List<AbstractElement> getElementList(Position p,Situation situation,ElementTypeEnum elementType){
			List<AbstractElement> abstractElementList = new ArrayList<AbstractElement>();
			ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
			ElementList holdElementList = robotGetElementList[p.x][p.y];
			for (AbstractElement element : holdElementList.elements) {
				if(elementType == null){
					abstractElementList.add(element);
				}
				else if(elementType.equals(element.elementType)){
					abstractElementList.add(element);
				}
				
			}
			return abstractElementList;
		}
		
		protected abstract String getActionType();
		
		protected Position getPosition(FightRobotBaseInfo fightRobotBaseInfo){
			return new Position(fightRobotBaseInfo.currentLocation.x,fightRobotBaseInfo.currentLocation.y);
		}
		
	}
	
	private static class EscapeAction extends Action {
		
		static class EscapteEnd {
			
			
			private Position end;
			
			// 0:四角
			// 1:四周中间
			// 2:中间
			// 3:徘徊
			private int type;
			
			public EscapteEnd(Position end,int type){
				this.end = end;
				this.type = type;
			}
			
		}

		@Override
		protected ActionResult doDeal(Situation situation) {
			ActionResult actionResult = new ActionResult();
			List<EscapteEnd> ends = new ArrayList<EscapteEnd>();
			ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
			int xl = robotGetElementList.length;
			int yl = robotGetElementList[0].length;
			int ownPX = situation.ownPosition.x;
			int ownPY = situation.ownPosition.y;
			ends.add(new EscapteEnd(new Position(xl/2,yl/2),2));
			ends.add(new EscapteEnd(new Position(xl/4,yl/4),1));
			ends.add(new EscapteEnd(new Position(xl*3/4,yl/4),1));
			ends.add(new EscapteEnd(new Position(xl/4,yl*3/4),1));
			ends.add(new EscapteEnd(new Position(xl*3/4,yl*3/4),1));
			ends.add(new EscapteEnd(new Position(0,0),0));
			ends.add(new EscapteEnd(new Position(0,yl-1),0));
			ends.add(new EscapteEnd(new Position(xl-1,0),0));
			ends.add(new EscapteEnd(new Position(xl-1,yl-1),0));
			if(ownPX -1 >= 0){
				ends.add(new EscapteEnd(new Position(ownPX -1,ownPY),3));
			}
			if(ownPX + 1 <= xl - 1){
				ends.add(new EscapteEnd(new Position(ownPX + 1,ownPY),3));
			}
			if(ownPY - 1 >= 0){
				ends.add(new EscapteEnd(new Position(ownPX,ownPY - 1),3));
			}
			
			if(ownPY + 1 <= yl - 1){
				ends.add(new EscapteEnd(new Position(ownPX,ownPY + 1),3));
			}
			
			WeightEscapeResult weightEscapeResult = judgeEscape(ends,situation);
			actionResult.route = weightEscapeResult.escapeRoute;
			actionResult.target = weightEscapeResult.target;
			actionResult.setWeight(weightEscapeResult.getWeight());
			if(!actionResult.getWeight().equals(Weight.MIN_WEIGHT)){
				System.out.println("selected escape position(x"+actionResult.target.x+",y"+actionResult.target.y+") weight is " +actionResult.getWeight());
			}
			else {
				System.out.println("1");
			}
			return actionResult;
		}
		
		private WeightEscapeResult judgeEscape(List<EscapteEnd> ends,Situation situation){
			WeightEscapeResult selectedWeightEscapeResult = null;
			for(EscapteEnd end:ends){
				WeightEscapeResult weightEscapeResult = weightEscape(end,situation);
				if(selectedWeightEscapeResult == null){
					selectedWeightEscapeResult = weightEscapeResult;
				}
				else {
					if(selectedWeightEscapeResult.getWeight().compare(weightEscapeResult.getWeight()) < 0){
						selectedWeightEscapeResult = weightEscapeResult;
					}
				}
			}
			return selectedWeightEscapeResult;
		}
		
		private WeightEscapeResult weightEscape(EscapteEnd end,Situation situation){
			Map<String,Object> eventContext = new HashMap<String,Object>();
			WeightEscapeResult weightEscapeResult = new WeightEscapeResult();
			GetRouteResult getRouteResult = this.getRoute(situation.ownPosition, end.end , null, situation, 0);
			if(getRouteResult.getWeight().equals(Weight.MIN_WEIGHT)){
				weightEscapeResult.setWeight(Weight.MIN_WEIGHT);
				return weightEscapeResult;
			}
			eventContext.put("mode", situation.mode);
			eventContext.put("escapeType", end.type);
			Weight weight = WeightManager.analysis(WeightManager.EVENT_ESCAPE_TYPE, eventContext);
			weightEscapeResult.addWeight(weight);
			weightEscapeResult.setEscapeRoute(getRouteResult.getRoute());
			weightEscapeResult.setTarget(end.end);
			eventContext.put("journey", getRouteResult.getRoute().getJourney());
			Weight journeyWeight = WeightManager.analysis(WeightManager.EVENT_ESCAPE_JOURNEY, eventContext);
			weightEscapeResult.addWeight(journeyWeight);
			weightEscapeResult.addWeight(getRouteResult.getWeight());
			
			weightEscapeResult.addWeight(beAttacked(end,situation));
			return weightEscapeResult;
		}
		
		private Weight beAttacked(EscapteEnd end,Situation situation){
			Position ownPosition = situation.ownPosition;
			List<FightRobotBaseInfo> enemys = this.findEnemys(situation);
			Position nearEnemy = null;
			for(FightRobotBaseInfo enemy:enemys){
				Position enemyP = this.getPosition(enemy);
				if(ownPosition.getJourney(enemyP) == 1){
					nearEnemy = enemyP;
				}
			}
			if(nearEnemy == null){
				return new Weight(0);
			}
			int ownX = ownPosition.x;
			int enemyX = nearEnemy.x;
			int endX =end.end.x;
			
			int ownY = ownPosition.y;
			int enemyY = nearEnemy.y;
			int endY = end.end.y;
			
			boolean hinder = false;
			if(enemyX > ownX && enemyX <= endX){
				hinder = true;
			}
			if(enemyX < ownX && enemyX >= endX){
				hinder = true;
			}
			if(enemyY > ownY && enemyY <= endY){
				hinder = true;
			}
			if(enemyY < ownY && enemyY >= endY){
				hinder = true;
			}
			if(hinder){
				Map<String,Object> eventContext = new HashMap<String,Object>();
				return WeightManager.analysis(WeightManager.EVENT_ESCAPE_BE_ATTACKED, eventContext);
			}
			else {
				return new Weight(0);
			}
		}
		

		@Override
		protected String getActionType() {
			return "EscapeAction";
		}
		
		private static class WeightEscapeResult extends Result {
			
			private Route escapeRoute;
			
			private Position target;

			public Route getEscapeRoute() {
				return escapeRoute;
			}

			public void setEscapeRoute(Route escapeRoute) {
				this.escapeRoute = escapeRoute;
			}

			public Position getTarget() {
				return target;
			}

			public void setTarget(Position target) {
				this.target = target;
			}
			
			
		}
		
	}
	
	private static class AttackAction extends Action {
		
		private static final int CAN_KILL_BLOOD = 2;

		@Override
		protected ActionResult doDeal(Situation situation) {
			ActionResult actionResult = new ActionResult();
			Map<String,Object> eventContext = new HashMap<String,Object>();
			eventContext.put("mode", situation.getMode());
			eventContext.put("ownBlood", situation.getOwnBloodNum());
			eventContext.put("enemyCount", situation.aliveEnemyCount);
			Weight ownBloodWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_OWN_BLOOD, eventContext);
			if(ownBloodWeigth.equals(Weight.MIN_WEIGHT)){
				actionResult.setWeight(Weight.MIN_WEIGHT);
				return actionResult;
			}
			List<FightRobotBaseInfo> fightRobotBaseInfos = findEnemys(situation);
			WeightEnemyResult weightEnemyResult = judgeEnemy(fightRobotBaseInfos,situation);
			actionResult.setRoute(weightEnemyResult.getEnemyRoute());
			actionResult.setTarget(weightEnemyResult.getEnemyPosition());
			actionResult.addWeight(weightEnemyResult.getWeight());
			if(!actionResult.getWeight().equals(Weight.MIN_WEIGHT)){
				System.out.println("selected enemy ("+weightEnemyResult.enemy.name +") position(x"+actionResult.target.x+",y"+actionResult.target.y+") weight is " + actionResult.getWeight());
			}
			return actionResult;
		}
		
		private WeightEnemyResult judgeEnemy(List<FightRobotBaseInfo> fightRobotBaseInfos,Situation situation){
			WeightEnemyResult selectedWeightEnemyResult = null;
			for(FightRobotBaseInfo fightRobotBaseInfo:fightRobotBaseInfos){
				WeightEnemyResult weightEnemyResult = weightEnemy(fightRobotBaseInfo,situation);
				if(selectedWeightEnemyResult == null){
					selectedWeightEnemyResult = weightEnemyResult;
				}
				else if(selectedWeightEnemyResult.getWeight().compare(weightEnemyResult.getWeight()) < 0){
					selectedWeightEnemyResult = weightEnemyResult;
				}
			}
			return selectedWeightEnemyResult;
			
		}
		
		private WeightEnemyResult weightEnemy(FightRobotBaseInfo enemy,Situation situation){
			Position ownPosition = situation.getOwnPosition();
			WeightEnemyResult weightEnemyResult = weightEnemy(enemy,ownPosition,situation);
			//NearByEnemyResult nearByEnemyResult = nearByEnemy(blood,ownWeightBloodResult);
			Position enemyPosition = this.getPosition(enemy);
			System.out.println("enemy ("+enemy.name +") position(x"+enemyPosition.x+",y"+enemyPosition.y+") weight is " + weightEnemyResult.getWeight());
			weightEnemyResult.setEnemy(enemy);
			return weightEnemyResult;
		}
		
		private WeightEnemyResult weightEnemy(FightRobotBaseInfo enemy,Position tankPosition,Situation situation){
			WeightEnemyResult weightEnemyResult = new WeightEnemyResult();
			Position enemyPosition = this.getPosition(enemy);
			weightEnemyResult.setEnemyPosition(enemyPosition);
			Weight weight  = new Weight();
			weightEnemyResult.setWeight(weight);
			Map<String,Object> eventContext = new HashMap<String,Object>();
			eventContext.put("mode", situation.getMode());
			eventContext.put("enemyCount", situation.aliveEnemyCount);
			Weight modeWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_MODE, eventContext);
			weight.addWeight(modeWeigth);
			Boolean nearBy = this.nearBy(enemyPosition, tankPosition, situation);
			if(nearBy){
				if(canKill(enemyPosition,tankPosition,situation)){
					
					Weight killWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_KILL, eventContext);
					weight.addWeight(killWeigth);
				}
				else {
					Weight attackWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK, eventContext);
					weight.addWeight(attackWeigth);
				}
			}
			int betweenBlood = situation.ownBloodNum - enemy.bloodNum;
			eventContext.put("betweenBlood", betweenBlood);
			Weight betweenBloodWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_BETWEEN_BLOOD, eventContext);
			weight.addWeight(betweenBloodWeigth);
			
			int multipleAttackCount = multipleAttack(enemyPosition,tankPosition,situation);
			eventContext.put("multipleAttackCount", Integer.valueOf(multipleAttackCount));
			Weight multipleAttackWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_MULTIPLE, eventContext);
			weight.addWeight(multipleAttackWeigth);
			
			int enmeyBloodCount = this.enmeyBloodCount(enemy);
			eventContext.put("enemyBloodCount", enmeyBloodCount);
			Weight enmeyBloodCountWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_ENEMY_BLOOD_COUNT, eventContext);
			weight.addWeight(enmeyBloodCountWeigth);
			eventContext.put("enemyAttackCount", enemy.attackRobotNum);
			Weight enmeyAttackCountWeigth = WeightManager.analysis(WeightManager.EVENT_ATTACK_ENEMY_ATTACK_COUNT, eventContext);
			weight.addWeight(enmeyAttackCountWeigth);
			if(nearBy){
				int moveCount = situation.getMoveCount();
				if(moveCount == 1){
					Route route = new Route();
					route.addPosition(tankPosition);
					route.addPosition(enemyPosition);
					weightEnemyResult.setEnemyRoute(route);
				}
				else {
					Route route = new Route();
					route.addPosition(tankPosition);
					route.addPosition(getOneStep(tankPosition, situation).targetPos);
//					route.addPosition(new Position(tankPosition.x+1,tankPosition.y));
					route.addPosition(enemyPosition);
					weightEnemyResult.setEnemyRoute(route);
				}
			}
			else {
				
				
				GetRouteResult enemyRouteResult = getAttackEnemyRoute(enemyPosition,tankPosition,situation,betweenBlood);
				if(enemyRouteResult.getWeight().equals(Weight.MIN_WEIGHT)){
					weightEnemyResult.setWeight(Weight.MIN_WEIGHT);
					return weightEnemyResult;
				}
				Route route = enemyRouteResult.getRoute();
				weightEnemyResult.setEnemyRoute(route);
				weightEnemyResult.addWeight(enemyRouteResult.getWeight());
				boolean beforeAttack = beforeAttack(route,situation);
				eventContext.put("beforeAttack", Boolean.valueOf(beforeAttack));
				Weight beforeAttackWeight = WeightManager.analysis(WeightManager.EVENT_ATTACK_ORDER, eventContext);
				weight.addWeight(beforeAttackWeight);
			}
			
			
			eventContext.put("journey", Integer.valueOf(weightEnemyResult.getEnemyRoute().getJourney()));
			Weight journeyWeight = WeightManager.analysis(WeightManager.EVENT_ATTACK_ENEMY_JOURNEY, eventContext);
			weight.addWeight(journeyWeight);
			
			
			return weightEnemyResult;
		}
		
		private int enmeyBloodCount(FightRobotBaseInfo enemy){
			return enemy.bloodNum;
		}
		
		private boolean beforeAttack(Route route,Situation situation){
			
			Integer journey = route.getJourney();
			return journey%2 == 0;
		}
		

		
		private boolean canKill(Position enemy,Position tankPosition,Situation situation){
			List<FightRobotBaseInfo> fightRobotBaseInfos = this.getEnemyInfoList(enemy, situation);
			for(FightRobotBaseInfo fightRobotBaseInfo:fightRobotBaseInfos){
				if(fightRobotBaseInfo.bloodNum.compareTo(CAN_KILL_BLOOD) < 0){
					return true;
				}
			}
			return false;
		}
		
		
		private int multipleAttack(Position enemy,Position tankPosition,Situation situation){
			List<FightRobotBaseInfo> fightRobotBaseInfos = this.getEnemyInfoList(enemy, situation);
			return fightRobotBaseInfos.size();
		}
		
		private GetRouteResult getAttackEnemyRoute(Position enemy,Position tankPosition,Situation situation,int betweenBlood){
			int maxPermitLandmineCount = 0;
			if(betweenBlood > 10){
				maxPermitLandmineCount = 5;
			}
			GetRouteResult getRouteResult = this.getRoute(tankPosition, enemy, ElementTypeEnum.ROBOT_INFO,situation, maxPermitLandmineCount);
			return getRouteResult;
		}
		
		private static class WeightEnemyResult extends Result {
			
			private FightRobotBaseInfo enemy;
			
			private Route enemyRoute;
			
			private Position enemyPosition;

			public FightRobotBaseInfo getEnemy() {
				return enemy;
			}

			public void setEnemy(FightRobotBaseInfo enemy) {
				this.enemy = enemy;
			}

			public Route getEnemyRoute() {
				return enemyRoute;
			}

			public void setEnemyRoute(Route enemyRoute) {
				this.enemyRoute = enemyRoute;
			}

			public Position getEnemyPosition() {
				return enemyPosition;
			}

			public void setEnemyPosition(Position enemyPosition) {
				this.enemyPosition = enemyPosition;
			}

		}

		@Override
		protected String getActionType() {
			return "AttackAction";
		}
		
	}
	
	private static class EatBloodAction extends Action {

		@Override
		protected ActionResult doDeal(Situation situation) {
			ActionResult actionResult = new ActionResult();
			List<Position> bloodPositions = findBloods(situation);
			if(bloodPositions == null || bloodPositions.size() == 0){
				actionResult.setWeight(Weight.MIN_WEIGHT);
				return actionResult;
			}
			else {
				Weight bloodWeight = WeightManager.analysis(WeightManager.EVENT_BLOOD, new HashMap<String,Object>());
				actionResult.addWeight(bloodWeight);
			}
			WeightBloodResult weightBloodResult = judgeBlood(bloodPositions,situation);
			actionResult.setRoute(weightBloodResult.getEatBloodRoute());
			actionResult.setTarget(weightBloodResult.getBloodPosition());
			actionResult.addWeight(weightBloodResult.getWeight());
			if(!actionResult.getWeight().equals(Weight.MIN_WEIGHT)){
				if(actionResult.target == null){
					System.out.println(1);
				}
				System.out.println("selected blood position(x"+actionResult.target.x+",y"+actionResult.target.y+") weight is " +actionResult.getWeight());
			}
			return actionResult;
		}
		
		private List<Position> findBloods(Situation situation){
			List<Position> bloodPositions = new ArrayList<Position>();
			ElementList[][] robotGetElementList = situation.getFightRobotSeeEntity().robotGetElementList;
			 for (int i = 0; i < robotGetElementList.length; i++) {
		            for (int j = 0; j < robotGetElementList.length; j++) {
		                ElementList elementList = robotGetElementList[i][j];
		                for (AbstractElement element : elementList.elements) {
		                    if (ElementTypeEnum.BLOOD_BAG == element.elementType) {
		                    	bloodPositions.add(new Position(i,j));
		                    }
		                }
		            }
		        }
			return bloodPositions;
		}
		
		private WeightBloodResult judgeBlood(List<Position> bloods,Situation situation){
			WeightBloodResult selectedWeightBloodResult = null;
			for(Position blood:bloods){
				WeightBloodResult weightBloodResult = weightBlood(blood,situation);
				if(selectedWeightBloodResult == null){
					selectedWeightBloodResult = weightBloodResult;
				}
				else if(selectedWeightBloodResult.getWeight().compare(weightBloodResult.getWeight()) < 0){
					selectedWeightBloodResult = weightBloodResult;
				}
			}
			
			return selectedWeightBloodResult;
			
		}
		
		private WeightBloodResult weightBlood(Position blood,Situation situation){
			Position ownPosition = situation.getOwnPosition();
			WeightBloodResult ownWeightBloodResult = weightBlood(blood,ownPosition,situation);
			System.out.println("boold position(x"+blood.x+",y"+blood.y+") weight is " + ownWeightBloodResult.getWeight());
			return ownWeightBloodResult;
		}
		
		private WeightBloodResult weightBlood(Position blood,Position tankPosition,Situation situation){
			WeightBloodResult weightBloodResult = new WeightBloodResult();
			int nearByBloodCounts = nearByBloodCounts(blood,situation);
			GetRouteResult getRouteResult = getEatBloodRoute(blood,tankPosition,situation,nearByBloodCounts);
			if(getRouteResult.getWeight().equals(Weight.MIN_WEIGHT)){
				weightBloodResult.setWeight(Weight.MIN_WEIGHT);
				return weightBloodResult;
			}
			Route route = getRouteResult.route;
			weightBloodResult.setBloodPosition(blood);
			weightBloodResult.setEatBloodRoute(route);
			weightBloodResult.addWeight(getRouteResult.getWeight());
			Map<String,Object> eventContext = new HashMap<String,Object>();
			eventContext.put("journey", Integer.valueOf(route.getJourney()));
			Weight journeyWeight = WeightManager.analysis(WeightManager.EVENT_BLOOD_JOURNEY, eventContext);
			weightBloodResult.addWeight(journeyWeight);
			
			Integer nearByEnemy= nearByEnemy(blood,situation);
			if(nearByEnemy > 0 && Integer.valueOf(route.getJourney()) -1 >=  nearByEnemy){
				weightBloodResult.setWeight(Weight.MIN_WEIGHT);
				return weightBloodResult;
			}
			return weightBloodResult;
		}
		
		private GetRouteResult getEatBloodRoute(Position blood,Position tankPosition,Situation situation,int nearByBloodCounts){
			if(nearByBloodCounts - 1 > 0){
				System.out.println(1);
			}
			GetRouteResult getRouteResult = this.getRoute(tankPosition, blood, ElementTypeEnum.BLOOD_BAG,situation, nearByBloodCounts - 1);
			return getRouteResult;
		}
		
		private int nearByBloodCounts(Position blood,Situation situation){
			ElementList[][] robotGetElementList = situation.fightRobotSeeEntity.robotGetElementList;
			List<Position> allBloods = this.findBloods(situation);
			int nearByBloodCount = 1;
			for(int i = blood.x;i>=0;i--){
				Position p = new Position(i,blood.y);
				if(i == blood.x) {
					continue;
				}
				if(allBloods.contains(p)){
					nearByBloodCount++;
				}
				else {
					if(this.getElementList(p, situation, ElementTypeEnum.LANDMINE).size() > 0){
						break;
					}
					else {
						continue;
					}
					
				}
			}
			
			for(int i = blood.x;i < robotGetElementList.length ;i++){
				if(i == blood.x) {
					continue;
				}
				Position p = new Position(i,blood.y);
				if(allBloods.contains(p)){
					nearByBloodCount++;
				}
				else {
					if(this.getElementList(p, situation, ElementTypeEnum.LANDMINE).size() > 0){
						break;
					}
					else {
						continue;
					}
				}
			}
			
			for(int j = blood.y; j >= 0; j --){
				if(j == blood.y) {
					continue;
				}
				Position p = new Position(blood.x,j);
				if(allBloods.contains(p)){
					nearByBloodCount++;
				}
				else {
					if(this.getElementList(p, situation, ElementTypeEnum.LANDMINE).size() > 0){
						break;
					}
					else {
						continue;
					}
				}
			}
			
			for(int j = blood.y; j < robotGetElementList[0].length ; j ++){
				if(j == blood.y) {
					continue;
				}
				Position p = new Position(blood.x,j);
				if(allBloods.contains(p)){
					nearByBloodCount++;
				}
				else {
					if(this.getElementList(p, situation, ElementTypeEnum.LANDMINE).size() > 0){
						break;
					}
					else {
						continue;
					}
				}
			}
			return nearByBloodCount;
		}
		
		private Integer nearByEnemy(Position blood,Situation situation){
			List<FightRobotBaseInfo> enemys = this.findEnemys(situation);
			int near = 4;
			for(FightRobotBaseInfo enemy:enemys){
				Position enemyPosition = this.getPosition(enemy);
				int journey = blood.getJourney(enemyPosition);
				if(journey <= near){
					return journey;
				}
			}
			return -1;
		}
		
		
		private static class GetEatBloodRouteResult extends Result {
			
			private Route eatBloodRoute;
			
			private int landmineCount;

			public Route getEatBloodRoute() {
				return eatBloodRoute;
			}

			public void setEatBloodRoute(Route eatBloodRoute) {
				this.eatBloodRoute = eatBloodRoute;
			}

			public int getLandmineCount() {
				return landmineCount;
			}

			public void setLandmineCount(int landmineCount) {
				this.landmineCount = landmineCount;
			}
			
		}
		
		private static class WeightBloodResult extends Result {
			
			private Route eatBloodRoute;
			
			private Position bloodPosition;
			
			public Position getBloodPosition() {
				return bloodPosition;
			}

			public void setBloodPosition(Position bloodPosition) {
				this.bloodPosition = bloodPosition;
			}

			public Route getEatBloodRoute() {
				return eatBloodRoute;
			}

			public void setEatBloodRoute(Route eatBloodRoute) {
				this.eatBloodRoute = eatBloodRoute;
			}

		}
		
		private static class NearByEnemyResult extends Result {
			
		}

		@Override
		protected String getActionType() {
			return "EatBloodAction";
		}
		

		
	}
	
	private static class Route {
		 
		private List<Position> positions = new ArrayList<Position>();

		public List<Position> getPositions() {
			return positions;
		}

		public void setPositions(List<Position> positions) {
			this.positions = positions;
		}
		
		public int getJourney(){
			return this.positions.size();
		}
		
		public void addPosition(Position position){
			this.positions.add(position);
		}
		
	}
	
	
	private static class Position {
		
		private int x;
		
		private int y;
		
		public Position(){
			
		}
		
		public Position(int x,int y){
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		
		public int getJourney(Position p){
			return Math.abs(this.x - p.x) + Math.abs(this.y - p.y);
		}
		
	}
	
	private static class Weight {
		
		private int value = 0;
		
		public static final Weight MAX_WEIGHT = new Weight(Integer.MAX_VALUE);
		
		public static final Weight MIN_WEIGHT = new Weight(Integer.MIN_VALUE);
		
		public Weight(){
			
		}
		
		public Weight(int value){
			this.value = value;
		}
		
		public void addWeight(Weight weight){
			if(this.value == MAX_WEIGHT.value){
				return;
			}
			if(this.value == MIN_WEIGHT.value){
				return;
			}
			if(weight.equals(MAX_WEIGHT) || weight.equals(MIN_WEIGHT)){
				this.value = weight.value;
			}
			else {
				this.value += weight.value;
			}
			
		}
		
		public int getValue(){
			return value;
		}
		
		public int compare(Weight weight){
			int paramValue = weight.getValue();
			if(this.value > paramValue){
				return 1;
			}
			else if(this.value == paramValue){
				return 0;
			}
			else {
				return -1;
			}
		}

		@Override
		public String toString() {
			return String.valueOf(this.value);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Weight other = (Weight) obj;
			if (value != other.value)
				return false;
			return true;
		}
		
		
		
	}
	
	private static class WeightManager {
		
		public static final String EVENT_ATTACK_MODE = "EVENT_ATTACK_MODE";
		
		public static final String EVENT_ATTACK_OWN_BLOOD = "EVENT_ATTACK_OWN_BLOOD";
		
		public static final String EVENT_ATTACK_KILL = "EVENT_ATTACK_KILL";
		
		public static final String EVENT_ATTACK = "EVENT_ATTACK";
		
		
		public static final String EVENT_ATTACK_BETWEEN_BLOOD = "EVENT_ATTACK_BETWEEN_BLOOD";
		
		public static final String EVENT_ATTACK_MULTIPLE = "EVENT_ATTACK_MULTIPLE";
		
		public static final String EVENT_BLOOD = "EVENT_BLOOD";
		
		public static final String EVENT_BLOOD_JOURNEY = "EVENT_BLOOD_JOURNEY";
		
		public static final String EVENT_ATTACK_ENEMY_JOURNEY = "EVENT_ATTACK_ENEMY_JOURNEY";
		
		public static final String EVENT_ATTACK_ENEMY_BLOOD_COUNT = "EVENT_ATTACK_ENEMY_BLOOD_COUNT";
		
		public static final String EVENT_ATTACK_ENEMY_ATTACK_COUNT = "EVENT_ATTACK_ENEMY_ATTACK_COUNT";
		
		public static final String EVENT_ATTACK_ORDER = "EVENT_ATTACK_ORDER";
		
		public static final String EVENT_ESCAPE_OWN_BLOOD = "EVENT_ESCAPE_OWN_BLOOD";
		
		public static final String EVENT_ESCAPE_TYPE = "EVENT_ESCAPE_TYPE";
		
		public static final String EVENT_ESCAPE_JOURNEY = "EVENT_ESCAPE_JOURNEY";
		
		public static final String EVENT_LANDMINE = "EVENT_LANDMINE";
		
		public static final String EVENT_BE_ATTACK = "EVENT_BE_ATTACK";
		
		public static final String EVENT_ESCAPE_BE_ATTACKED = "EVENT_ESCAPE_BE_ATTACKED";
		
		public static final Integer EVENT_LANDMINE_VALUE = -200;
		
		public static  Weight analysis(String event,Map<String,Object> eventContext){
			int value = 0;
			String mode = String.valueOf(eventContext.get("mode"));
			if(EVENT_ATTACK_MODE.equals(event)){
				if(MODE_SINGLE.equals(mode)){
					value = 0;
				}
				else if(MODE_CHICKEN.equals(mode)){
					value = -200;
				}
			}
			else if(EVENT_ATTACK_OWN_BLOOD.equals(event)){
				Integer ownBlood = Integer.valueOf(String.valueOf(eventContext.get("ownBlood")));
				Integer enemyCount = Integer.valueOf(String.valueOf(eventContext.get("enemyCount")));
				if(enemyCount.compareTo(1) > 0){
					if(ownBlood.compareTo(8) <=0){
						value = Weight.MIN_WEIGHT.value;
					}
				}
				
			}
			else if(EVENT_ATTACK_KILL.equals(event)){
				if(MODE_SINGLE.equals(mode)){
					value = Weight.MAX_WEIGHT.value;
				}
				else {
					Integer enemyCount = Integer.valueOf(String.valueOf(eventContext.get("enemyCount")));
					if(enemyCount == 1){
						value = Weight.MAX_WEIGHT.value;
					}
					else {
						value = 0;
					}
					
				}
			}
			else if(EVENT_ATTACK.equals(event)){
				value = 200;
			}
			else if(EVENT_ATTACK_BETWEEN_BLOOD.equals(event)){
				Integer betweenBlood = Integer.valueOf(String.valueOf(eventContext.get("betweenBlood")));
				Integer enemyCount = Integer.valueOf(String.valueOf(eventContext.get("enemyCount")));
				if(enemyCount == 1){
					value = betweenBlood * 200;
				}
				else if(enemyCount == 2){
					value = betweenBlood * 40;
				}
				else {
					value = 0;
				}
				
			}
			else if(EVENT_ATTACK_MULTIPLE.equals(event)){
				Integer multipleAttackCount = Integer.valueOf(String.valueOf(eventContext.get("multipleAttackCount")));
				if(multipleAttackCount.compareTo(1) == 0){
					value = 0;
				}
				else {
					value = multipleAttackCount * 200;
				}
				
			}
			else if(EVENT_ATTACK_ENEMY_JOURNEY.equals(event)){
				Integer journey = Integer.valueOf(String.valueOf(eventContext.get("journey")));
				value = 100 - journey;
			}
			else if(EVENT_ATTACK_ENEMY_BLOOD_COUNT.equals(event)){
				Integer enemyBloodCount = Integer.valueOf(String.valueOf(eventContext.get("enemyBloodCount")));
				//value = - enemyBloodCount * 10;
				value = 0;
			}
			else if(EVENT_ATTACK_ENEMY_ATTACK_COUNT.equals(event)){
				Integer enemyAttackCount = Integer.valueOf(String.valueOf(eventContext.get("enemyAttackCount")));
				value = - enemyAttackCount * 20;
			}
			else if(EVENT_ATTACK_ORDER.equals(event)){
				Boolean beforeAttack = Boolean.valueOf(String.valueOf(eventContext.get("beforeAttack")));
				if(beforeAttack){
					value = 200;
				}
				else {
					value = -200;
				}
			}
			else if(EVENT_LANDMINE.equals(event)){
				value  = EVENT_LANDMINE_VALUE;
			}
			else if(EVENT_BE_ATTACK.equals(event)){
				value = -3000;
			}
			else if(EVENT_BLOOD.equals(event)){
				value = 400;
			}
			else if(EVENT_BLOOD_JOURNEY.equals(event)){
				Integer journey = Integer.valueOf(String.valueOf(eventContext.get("journey")));
				value = 100 - journey;
			}
			
			else if(EVENT_ESCAPE_OWN_BLOOD.equals(event)){
				//Integer ownBlood = Integer.valueOf(String.valueOf(eventContext.get("ownBlood")));
				//if(ownBlood.compareTo(6) < 0){
				//	value = 1000;
				//}
				value = 0;
			}
			else if(EVENT_ESCAPE_TYPE.equals(event)){
				// 0:四角
				// 1:四周中间
				// 2:中间
				// 3:徘徊
				Integer escapeType = Integer.valueOf(String.valueOf(eventContext.get("escapeType")));
				
				if(escapeType.compareTo(3) == 0){
					value = 20;
				}
				else if(escapeType.compareTo(2) == 0){
					value  = 50;
				}
				else if(escapeType.compareTo(1) == 0){
					value = 30;
				}
				else {
					value = 10;
				}
				
			}
			else if(EVENT_ESCAPE_JOURNEY.equals(event)){
				Integer journey = Integer.valueOf(String.valueOf(eventContext.get("journey")));
				value = 50 - journey;
			}
			else if(EVENT_ESCAPE_BE_ATTACKED.equals(event)){
				value = - 200;
			}
			return new Weight(value);
		}
		
	}
	
	private static class Result {
		
		private Weight weight = new Weight();

		public Weight getWeight() {
			return weight;
		}

		public void setWeight(Weight weight) {
			this.weight = weight;
		}
		
		public void addWeight(Weight weight){
			this.weight.addWeight(weight);
		}
		
	}
	
	public static class AStar {  
		  
	    private int[][] nodes = null; 
	    
	    public AStar(int[][] nodes){
	    	this.nodes = nodes;
	    }
	    
	    public int[][] getNodes(){
	    	return this.nodes;
	    }
	  
	    public static final int STEP = 1;  
	  
	    private ArrayList<Node> openList = new ArrayList<Node>();  
	    private ArrayList<Node> closeList = new ArrayList<Node>();  
	  
	    public Node findMinFNodeInOpneList() {  
	        Node tempNode = openList.get(0);  
	        for (Node node : openList) {  
	            if (node.F < tempNode.F) {  
	                tempNode = node;  
	            }  
	        }  
	        return tempNode;  
	    }  
	  
	    public ArrayList<Node> findNeighborNodes(Node currentNode) {  
	        ArrayList<Node> arrayList = new ArrayList<Node>();  
	        // 只考虑上下左右，不考虑斜对角  
	        int topX = currentNode.x;  
	        int topY = currentNode.y - 1;  
	        if (canReach(topX, topY) && !exists(closeList, topX, topY)) {  
	            arrayList.add(new Node(topX, topY));  
	        }  
	        int bottomX = currentNode.x;  
	        int bottomY = currentNode.y + 1;  
	        if (canReach(bottomX, bottomY) && !exists(closeList, bottomX, bottomY)) {  
	            arrayList.add(new Node(bottomX, bottomY));  
	        }  
	        int leftX = currentNode.x - 1;  
	        int leftY = currentNode.y;  
	        if (canReach(leftX, leftY) && !exists(closeList, leftX, leftY)) {  
	            arrayList.add(new Node(leftX, leftY));  
	        }  
	        int rightX = currentNode.x + 1;  
	        int rightY = currentNode.y;  
	        if (canReach(rightX, rightY) && !exists(closeList, rightX, rightY)) {  
	            arrayList.add(new Node(rightX, rightY));  
	        }  
	        return arrayList;  
	    }  
	  
	    public boolean canReach(int x, int y) {  
	        if (x >= 0 && x < nodes.length && y >= 0 && y < nodes[0].length) {  
	            return nodes[x][y] != 1;  
	        }  
	        return false;  
	    }  
	  
	    public Node findPath(Node startNode, Node endNode) {  
	  
	        // 把起点加入 open list  
	        openList.add(startNode);  
	  
	        while (openList.size() > 0) {  
	            // 遍历 open list ，查找 F值最小的节点，把它作为当前要处理的节点  
	            Node currentNode = findMinFNodeInOpneList();  
	            // 从open list中移除  
	            openList.remove(currentNode);  
	            // 把这个节点移到 close list  
	            closeList.add(currentNode);  
	  
	            ArrayList<Node> neighborNodes = findNeighborNodes(currentNode);  
	            for (Node node : neighborNodes) {  
	                if (exists(openList, node)) {  
	                    foundPoint(currentNode, node);  
	                } else {  
	                    notFoundPoint(currentNode, endNode, node);  
	                }  
	            }  
	            if (find(openList, endNode) != null) {  
	                return find(openList, endNode);  
	            }  
	        }  
	  
	        return find(openList, endNode);  
	    }  
	  
	    private void foundPoint(Node tempStart, Node node) {  
	        int G = calcG(tempStart, node);  
	        if (G < node.G) {  
	            node.parent = tempStart;  
	            node.G = G;  
	            node.calcF();  
	        }  
	    }  
	  
	    private void notFoundPoint(Node tempStart, Node end, Node node) {  
	        node.parent = tempStart;  
	        node.G = calcG(tempStart, node);  
	        node.H = calcH(end, node);  
	        if(node.x == end.x || node.y == end.y){
	        	node.G += 1;
	        }
	        else {
	        	node.G -= 1;
	        }
	        node.calcF();  
	        openList.add(node);  
	    }  
	  
	    private int calcG(Node start, Node node) {  
	        int G = STEP;  
	        int parentG = node.parent != null ? node.parent.G : 0;  
	        int weight = this.nodes[node.x][node.y];
	        return G + weight + parentG;  
	    }  
	  
	    private int calcH(Node end, Node node) {  
	        int step = Math.abs(node.x - end.x) + Math.abs(node.y - end.y);  
	        return step * STEP;  
	    }  
	  
	    public static Node find(List<Node> nodes, Node point) {  
	        for (Node n : nodes)  
	            if ((n.x == point.x) && (n.y == point.y)) {  
	                return n;  
	            }  
	        return null;  
	    }  
	  
	    public static boolean exists(List<Node> nodes, Node node) {  
	        for (Node n : nodes) {  
	            if ((n.x == node.x) && (n.y == node.y)) {  
	                return true;  
	            }  
	        }  
	        return false;  
	    }  
	  
	    public static boolean exists(List<Node> nodes, int x, int y) {  
	        for (Node n : nodes) {  
	            if ((n.x == x) && (n.y == y)) {  
	                return true;  
	            }  
	        }  
	        return false;  
	    }  
	  
	    public static class Node {  
	        public Node(int x, int y) {  
	            this.x = x;  
	            this.y = y;  
	        }  
	  
	        public int x;  
	        public int y;  
	  
	        public int F;  
	        public int G;  
	        public int H;  
	  
	        public void calcF() {  
	            this.F = this.G + this.H;  
	        }  
	  
	        public Node parent;  
	    }  
	    
	} 

}
