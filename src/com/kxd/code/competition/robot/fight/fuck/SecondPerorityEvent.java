package com.kxd.code.competition.robot.fight.fuck;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;

/**
 * 第二优先级：找血
 * @author miaoqingquan
 *
 */
public class SecondPerorityEvent extends AbstractEvent{

	@Override
	public MoveActionCommandEnum getNextStep(MapContext mapContext) {
		Location location = mapContext.getOnselfLoction();
		List<BloodBag> bloodBags = mapContext.getBloodBags();
		List<BloodBag> bloodBagList = new ArrayList<>();
		for (BloodBag bloodBag : bloodBags) {
			bloodBagList.add(bloodBag);
		}
		return getBestActionCommandEnum(location,bloodBagList,mapContext);
	}

	public MoveActionCommandEnum getBestActionCommandEnum(Location location,List<BloodBag> bloodBags,MapContext mapContext){
		int leastdBloodDistance = 100;
		BloodBag leastdBloodBag = null;
		int index = 0;
        //血包开启扫描的最大视野
		if(bloodBags.size() != 0){
			for (AbstractElement element : bloodBags) {
	        	BloodBag bloodBag = (BloodBag) element;
	        	int bloodx = bloodBag.location.x;
	        	int bloody = bloodBag.location.y;
	        	//总距离
	        	int distanceY = Math.abs(bloody - location.y);
	        	int distanceX = Math.abs(bloodx - location.x);
	        	int distance =  distanceY+ distanceX; 
	        	if(leastdBloodDistance > distance || bloodBags.indexOf(bloodBag) == 0){
	        		index = bloodBags.indexOf(bloodBag);
	        		leastdBloodDistance = distance;
	        		leastdBloodBag = bloodBag;
	        	}
			}
	        SaienLoction start = new SaienLoction(location.x, location.y);
	        SaienLoction end = new SaienLoction(leastdBloodBag.location.x, leastdBloodBag.location.y);
	        Stack<SaienLoction> stack= printPath(start, end, mapContext,ElementTypeEnum.BLOOD_BAG);
	        if(stack.size()  == 0){
	        	bloodBags.remove(index);
	        	if(bloodBags.size() != 0){
	        		return getBestActionCommandEnum(location,bloodBags,mapContext);
	        	}
	        }else{
	        	return stack.get(stack.size()-1).getMoveActionCommandEnum();
	        }
	        /*if(stack.size() == 0){
	        	bloodBags.remove(index);
	        	if(bloodBags.size() != 0){
	        		return getBestActionCommandEnum(location,bloodBags,mapContext);
	        	}else{
	        		MoveActionCommandEnum moveActionCommandEnum = landmineIsFuck(start,mapContext);
		        	if(moveActionCommandEnum != null){
		        		return moveActionCommandEnum;
		        	}else{
		        		return null;
		        	}
	        	}
	        } else {
				return stack.get(stack.size()-1).getMoveActionCommandEnum();
			}*/
		}
		return null;
	}
}
