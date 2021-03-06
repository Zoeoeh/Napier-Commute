package edu.napier.commute;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import edu.napier.geo.common.Location;

abstract class Commuter {

	protected Location _home;
	protected Location _work;
	protected LocalTime _workStart;
	protected LocalTime _workEnd;
	protected int _id;
	protected String _description;
	protected CJourney basicJourneyIn;
	protected CJourney basicJourneyOut;
	protected ArrayList<TransportMode> myModes = new ArrayList<TransportMode>();
	protected ArrayList<Feedback> myFeedback;
	protected CJourney choiceIn = null;
	protected CJourney choiceOut = null;
	protected TransportMode modeIn;
	protected TransportMode modeOut;

	public Commuter(int id, String desc, String strHome, String strWork, LocalTime workStart, LocalTime workEnd) {
		_id = id;
		_description = desc;
		
		
		//Need to geoLoate home and work;
		_home = GeoCoder.find(strHome);
		_work = GeoCoder.find(strWork);
		basicJourneyIn = new CJourney(_home,_work);
		basicJourneyOut = new CJourney(_home,_work);
		_workStart = workStart;
		_workEnd = workEnd;
		
	}

	public boolean addTransportMode(TransportMode mode) {
		myModes.add(mode);
		
		//Check to see if this mode is possible for this commuter
		//Return false if it is not possible
		CJourney request = new CJourney(this.basicJourneyIn);
		request.setTime( LocalTime.of(0,0,0,0));
		ArrayList<CJourney> options = this.getTransportOptions(request, mode);
		for (CJourney possible: options) {
			System.out.println("Journey option,"+ this._description +","+mode +","+possible.getStartIdentifier()+","+possible.getEndIdentifier()+","+possible.cost+","+possible.emissions+","+possible.getTravelTimeMin());
		}
		if (options.size() >0)
			return true;
		else
			return false;
		
	}
	
	
	public TransportMode getModeIn() {
		return modeIn;
	}

	public TransportMode getModeOut() {
		return modeOut;
	}

	abstract  void selectTravelOption(int day);//This method contains the selection logic
	
	public void clearFeedback() {
		myFeedback = null;
	}
	
	public void setFeedBack(ArrayList<Feedback> feedback) {
		//Set called after each day with feedback
		myFeedback = feedback;
	}

	public String getResultCSV() {
		String buffer = this._id + "," + this._description + ",";
		buffer += this._home.getDescription() +"," + this._workStart + ","+ this.modeIn +"," ;
		if (choiceIn != null)
		  buffer += this.choiceIn.getCost() + "," +this.choiceIn.getDistanceKM()+","+this.choiceIn.getEmissions() +"," + this.choiceIn.getTravelTimeMin() +",";
		else
			buffer += ",,,,";
		
		buffer += this._work.getDescription() +"," + this._workEnd + ","+ this.modeOut +",";
		if (choiceOut != null)
		  buffer += this.choiceOut.getCost() + ","+this.choiceOut.getDistanceKM()+","+this.choiceOut.getEmissions() +"," + this.choiceOut.getTravelTimeMin() +",";
		else
			buffer += ",,,,";
		buffer += "\n";
		
		return buffer;
	}
	
	
	private HashMap<String,ArrayList<CJourney>> journeyCache = new HashMap<String,ArrayList<CJourney>>();
	
	protected  ArrayList<CJourney> getTransportOptions(CJourney request, TransportMode tm) {
		//Setup and maintain a cache
		String key = tm.toString()+":"+request.getStartIdentifier()+":"+request.getEndIdentifier()+":"+request.getTime();
		ArrayList<CJourney> res = journeyCache.get(key);
		if (res == null) {
			res = TransportManager.getTransportOptions(request, tm);
			journeyCache.put(key,res);
		}
		return res;
	}

}