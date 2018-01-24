package com.kxd.code.competition.robot.fight.mvp;

import com.kxd.code.competition.entity.Location;
import com.kxd.code.competition.entity.element.child.BloodBag;

import java.util.*;

public class MvpSortByDis {
    private Location myLoation;
    private Queue<MvpElement> queue;
    private List<BloodBag> bloodBags;

    public MvpSortByDis(Location myLoation, List<BloodBag> bloodBags) {
        this.bloodBags = bloodBags;
        queue = new PriorityQueue<>(bloodBags.size(), disComparator);
        this.myLoation = myLoation;
    }

    private static Comparator<MvpElement> disComparator = new Comparator<MvpElement>(){

        @Override
        public int compare(MvpElement c1, MvpElement c2) {
            return (int) (c1.minStep - c2.minStep);
        }
    };

//    public List<MvpElement> SortByDis() {
    public List<Location> SortByDis() {
        for(BloodBag bloodBag : bloodBags) {
            MvpElement element = new MvpElement(this.myLoation, bloodBag.location);
            queue.add(element);
        }

//        List<MvpElement> list = new ArrayList<>(this.queue);
        List<Location> locations = new ArrayList<>();

        Iterator it = queue.iterator();

//        System.out.println(" SortByDis ");
        while(it.hasNext()) {
            MvpElement element = queue.poll();
//            System.out.println(" dis " + element.minStep);
            locations.add(element.targetlocation);
        }

        return locations;
    }
}
