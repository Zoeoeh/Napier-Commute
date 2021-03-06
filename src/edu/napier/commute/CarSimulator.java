package edu.napier.commute;

import java.util.ArrayList;
import java.util.HashMap;

import edu.napier.geo.common.Journey;
import edu.napier.geo.common.Location;


public class CarSimulator extends TransportSimulator {
/*
 * Obtain and store all of the journeys mde by this mode on this day - then provide feedback.
 * This simulator counts the journeys to work and checks the available parking spaces
 * 
 */
	private HashMap<String,ArrayList<CJourney>> journeysIN= new HashMap<String,ArrayList<CJourney>>();
	
	private HashMap<String,Integer> carParkCapacities = new HashMap<String,Integer>();
	

	@Override
	void newDay() {
		journeysIN.clear();
		
	}

	@Override
	void addJourney(Commuter i, CJourney j, SimParams.direction dir) {
		if (dir == SimParams.direction.IN) {
			String workplace = j.getPointB().getDescription();
			if (!journeysIN.containsKey(workplace)) {
				journeysIN.put(workplace, new ArrayList<CJourney>());
			}
			ArrayList<CJourney> arrivals = journeysIN.get(workplace);
			arrivals.add(j);
			
			
		}//Ignore journeys home, at the moment

	}

	private void setCapacities() {
		carParkCapacities.clear();
		//Add car park capacities. This is temporary 
		carParkCapacities.put("EH14 1DJ", new Integer(308));//Craiglockhart 308
		carParkCapacities.put("EH10 5DT", new Integer(54));//Mercht 54
		carParkCapacities.put("EH11 4BN", new Integer(284));//Shill	 284	
	}
	
	@Override
	void setFeedback() {
		/**
		 * Work out feedback here
		 *
		 * We have lists of journeys that end at each place of work
		 * Check if the number ending is > the car park capacity
		 * 
		 */
		//Need to order them based on arrival time
		
		//Feedback to commuters
		setCapacities();//Set car park capacities
		
		for (String wp : journeysIN.keySet()) {//Go through each wp
			ArrayList<CJourney> workplace = journeysIN.get(wp);//Get the journeys to this WP
			
			for(CJourney j : workplace) {
				
				Integer spaces = carParkCapacities.get(wp);//Remaining spaces
				if(spaces == null)
					spaces = new Integer(SimParams.getInstance().getDefaultCarParkSize());
				
				if (spaces <= 0) {
					
					Commuter c = j.getCommuter();
					ArrayList<Feedback> feedbackList = new ArrayList<Feedback>();
					Feedback fb = new Feedback();
					fb.setDescription("Out of parking spaces");
					fb.setTimePenaltyMins(20);
					fb.setDirection(SimParams.direction.IN);
					fb.setType(Feedback.Type.CAR_PARKINGCAPCITY);
					feedbackList.add(fb);
					fb = new Feedback();
					fb.setDescription("Out of parking spaces");
					fb.setTimePenaltyMins(20);
					fb.setDirection(SimParams.direction.OUT);
					fb.setType(Feedback.Type.CAR_PARKINGCAPCITY);
					feedbackList.add(fb);
					c.setFeedBack(feedbackList);
				}else {
					spaces--;
					carParkCapacities.put(wp, spaces);
				}
			}
				
		}

	}
	

}
