package com.kxd.code.competition.robot.fight.fuck;

import java.util.ArrayList;
import java.util.List;

import com.kxd.code.competition.constants.ElementTypeEnum;
import com.kxd.code.competition.constants.MoveActionCommandEnum;
import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.element.AbstractElement;
import com.kxd.code.competition.entity.element.child.BloodBag;
import com.kxd.code.competition.entity.element.child.Landmine;

public class SaienLoction extends Location implements Comparable<SaienLoction>{

	public SaienLoction(int x, int y) {
		super(x, y);
		this.F = 0;
		this.G = 0;
		this.H = 0;
	}
	
	public SaienLoction(int x, int y,MoveActionCommandEnum moveActionCommandEnum) {
		super(x, y);
		this.moveActionCommandEnum = moveActionCommandEnum;
	}

	public MoveActionCommandEnum moveActionCommandEnum;
	
	public ElementTypeEnum elementTypeEnum;
	
	public List<SaienLoction> saienLoctionList = new ArrayList<>();//这个点的四周情况
	
	public SaienLoction parent;

	public int F,G,H;

	public MoveActionCommandEnum getMoveActionCommandEnum() {
		return moveActionCommandEnum;
	}

	public void setMoveActionCommandEnum(MoveActionCommandEnum moveActionCommandEnum) {
		this.moveActionCommandEnum = moveActionCommandEnum;
	}


	public ElementTypeEnum getElementTypeEnum() {
		return elementTypeEnum;
	}

	public void setElementTypeEnum(ElementTypeEnum elementTypeEnum) {
		this.elementTypeEnum = elementTypeEnum;
	}

	public List<SaienLoction> getSaienLoctionList() {
		return saienLoctionList;
	}

	public void setSaienLoctionList(List<SaienLoction> saienLoctionList) {
		this.saienLoctionList = saienLoctionList;
	}
	
	public List<SaienLoction> getNextPointAround(){
		
		return saienLoctionList;
	}

	public SaienLoction getParent() {
		return parent;
	}

	public void setParent(SaienLoction parent) {
		this.parent = parent;
	}

	public int getF() {
		return F;
	}

	public void setF(int f) {
		F = f;
	}

	public int getG() {
		return G;
	}

	public void setG(int g) {
		G = g;
	}

	public int getH() {
		return H;
	}

	public void setH(int h) {
		H = h;
	}

	@Override
	public int compareTo(SaienLoction o) {
			
		return this.F  - o.F;
	}
	
	 public boolean equals(Object obj) {
		 SaienLoction saienLoction = (SaienLoction) obj;
	     if (saienLoction.x == this.x && saienLoction.y == this.y)
	       return true;
	     return false;
	   }
	 
	 public static int getDis(SaienLoction p1, SaienLoction p2) {
	     int dis = Math.abs(p1.x - p2.x) * 10 + Math.abs(p1.y - p2.y) * 10;
	     return dis;
	   }
}
