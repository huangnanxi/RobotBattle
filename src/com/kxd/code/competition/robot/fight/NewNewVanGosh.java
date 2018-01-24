package com.kxd.code.competition.robot.fight;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.ElementList;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.action.CommonMoveAction;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.robotsee.FightRobotSeeEntity;
import com.kxd.code.competition.robot.AbstractFightRobot;
import com.kxd.code.competition.robot.entity.FightRobotBaseInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by new on 2018/1/17.
 */
public class NewNewVanGosh extends AbstractFightRobot {

    private FightRobotBaseInfo mSelf;
    ElementList[][] mElementLists;
    private List<Integer> mScores;

    private final int stepNum = 10; //预测步数
    private final int[] DISTANCE_WEIGHT = new int[]{20, 18, 16, 14, 12, 10, 8, 6, 4, 2};
    private final int BLOOD_BAG_SCORE = 20;
    private final int LANDMINE_SCORE = -20;
    private int ENEMY_SCORE;
    private int EMPTY_PLACE_SCORE;
    
    private int EnemyCountFlag = -1; //判断是否为团战（1:不是；-1:是）
    private boolean firstRoundFlag = false; 

    private final MoveActionCommandEnum[] DIRECTION = {
            MoveActionCommandEnum.MOVE_TOP,
            MoveActionCommandEnum.MOVE_LEFT,
            MoveActionCommandEnum.MOVE_RIGHT,
            MoveActionCommandEnum.MOVE_DOWN
    };
    
    public NewNewVanGosh() {
        this("梵高");
    }

    public NewNewVanGosh(String name) {
        super(name);
    }

    @Override
    public CommonMoveAction getNextAction() {
    	 FightRobotSeeEntity fightRobotSeeEntity = (FightRobotSeeEntity) currentSeeMazeSituation;
         mElementLists = fightRobotSeeEntity.robotGetElementList;
         mSelf = (FightRobotBaseInfo) fightRobotSeeEntity.robotBaseInfo;
         boolean mostBloodNumFlag = true;
         int n = 0;
         for (int i = 0; i < mElementLists.length; i++) {
             for (int j = 0; j < mElementLists.length; j++) {
                 ElementList elementList = mElementLists[i][j];
                 for (AbstractElement element : elementList.elements) {
                     if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                         n ++;
                     }
                 }
             }
         }
         
         if (!firstRoundFlag) {
        	 firstRoundFlag = true;
        	 if (n == 2) {
        		 EnemyCountFlag = 1; 
        	 } 
         }
         
         if (EnemyCountFlag == 1) {
        	 for (int i = 0; i < mElementLists.length; i++) {
                 for (int j = 0; j < mElementLists.length; j++) {
                     ElementList elementList = mElementLists[i][j];
                     for (AbstractElement element : elementList.elements) {
                         if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                        	 if (!((FightRobotBaseInfo) element).name.equals(mSelf.name)) {
                        		 ENEMY_SCORE = ((((FightRobotBaseInfo) element).bloodNum - mSelf.bloodNum) > 1) ? 1 : 2;
                                 EMPTY_PLACE_SCORE = ((((FightRobotBaseInfo) element).bloodNum - mSelf.bloodNum) > 1) ? 2 : 1;
                        	 }	 
                         }
                     }
                 }
             }
         } else {
            	 for (int i = 0; i < mElementLists.length; i++) {
                     for (int j = 0; j < mElementLists.length; j++) {
                         ElementList elementList = mElementLists[i][j];
                         for (AbstractElement element : elementList.elements) {
                             if (ElementTypeEnum.ROBOT_INFO == element.elementType) {
                            	 if (!((FightRobotBaseInfo) element).name.equals(mSelf.name)) {
                            		 if (((FightRobotBaseInfo) element).bloodNum >= mSelf.bloodNum) {
                                		 mostBloodNumFlag = false;
                            		 } 
                            	 }	 
                             }
                         }
                     }
                 }
            	 if (n <= 3) {
            		 ENEMY_SCORE = 2;
            		 EMPTY_PLACE_SCORE = 1;
            	 } else {
            		 ENEMY_SCORE = (mostBloodNumFlag) ? 2 : 1;
                     EMPTY_PLACE_SCORE = (mostBloodNumFlag) ? 1 : 2;
            	 }	 
         }
         
        try {
            mScores = new ArrayList<>();
            int max = Integer.MIN_VALUE;
            MoveActionCommandEnum result = null;
            for (MoveActionCommandEnum dir : DIRECTION) {
                mScores.clear();
                Location currentLocation = mSelf.currentLocation.clone();
                tryTakeAction(Integer.MIN_VALUE, 0, currentLocation, dir);
                int temp = maxScores(mScores);
                if (temp > max) {
                    max = temp;
                    result = dir;
                }
            }
            //System.out.println("Current max score is: " + max);
            return new CommonMoveAction(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CommonMoveAction(DIRECTION[new Random().nextInt(4)]);
    }

    /**
     * 尝试向可能的方向走
     *
     * @param location 原位置
     * @param action   当前步的走法
     */
    private void tryTakeAction(int score, int distance, Location location, MoveActionCommandEnum action) {
        distance++; //标记当前的步数
        if (distance > stepNum) {
            mScores.add(score);
            return;
        }
        if (!isDirectionInBound(location, action)) {
            mScores.add(score);
            return;
        }
        List<AbstractElement> element = mElementLists[location.x][location.y].elements;
        AbstractElement e = element.size() == 0 ? null : element.get(0);
        //统计这一步得到的分数
        if (score == Integer.MIN_VALUE) score = 0;
        score += calculateScore(distance, e);
        //这个时候得到的location已经是走过现在这一步的位置了
        for (MoveActionCommandEnum next : DIRECTION) {
            Location currentLocation = location.clone();
            tryTakeAction(score, distance, currentLocation, next);
        }
    }

    private int calculateScore(int distance, AbstractElement element) {
        int weight = DISTANCE_WEIGHT[distance - 1];
        if (element == null)
            return weight * EMPTY_PLACE_SCORE;
        switch (element.elementType) {
            case BLOOD_BAG:
                return weight * BLOOD_BAG_SCORE;
            case ROBOT_INFO:
                if (((FightRobotBaseInfo) element).name.equals(mSelf.name))
                    return 0;
                return weight * ENEMY_SCORE;
            case LANDMINE:
                return weight * LANDMINE_SCORE;
        }
        return 0;
    }

    /**
     * 确定下一步的范围是否在场地范围内，顺便更新一下位置信息
     *
     * @param location
     * @param action
     * @return
     */
    private boolean isDirectionInBound(Location location, MoveActionCommandEnum action) {
        int size = currentSeeMazeSituation.mapInfo.size;
        switch (action) {
            case MOVE_TOP:
                //为了不影响同一级其他方向的定位(其实好像也没什么关系)，如果y满足条件，则把y-1,否则把y加回来，
                // 下面同理
                return location.x-- > 0 || ++location.x > 0;
            case MOVE_DOWN:
                return location.x++ < size - 1 || --location.x < size - 1;
            case MOVE_LEFT:
                return location.y-- > 0 || ++location.y > 0;
            case MOVE_RIGHT:
                return location.y++ < size - 1 || --location.y < size - 1;
            default:
                break;
        }
        return false;
    }

    /**
     * 返回集合中的最大值
     *
     * @param scores 数字集合
     */
    private int maxScores(List<Integer> scores) {
        int result = Integer.MIN_VALUE;
        for (int num : scores) {
            if (num > result) result = num;
        }
        return result;
    }
}
