package com.github.kreatures.swarm.comm;

import java.util.Hashtable;
import java.util.Set;

import com.github.kreatures.core.Perception;
import com.github.kreatures.core.comm.SpeechAct;
import com.github.kreatures.core.comm.SpeechAct.SpeechActType;
import com.github.kreatures.swarm.components.DefaultStation;
import com.github.kreatures.swarm.components.StatusAgentComponents;

import net.sf.tweety.logics.fol.syntax.FolFormula;

/**
 * 
 * @author donfack
 *
 */
public class SwarmPerception implements Perception{// extends SpeechAct {
	private static Hashtable<String, DefaultStation> stationStatusList;
	private int receiverId=0;
	private String receiverName;
	private String currentStation;
	private String targetStation;
	private int distatnce;
	private boolean visited;
	
	

	/**
	 * 
	 * @param receiverName a name of the receiver.
	 */
	public SwarmPerception(String receiverName){
		this.receiverName=receiverName;
	}
	/**
	 * 
	 * @param receiverId a identify of a receiver.
	 */
	public SwarmPerception(int receiverId){
		this.receiverId=receiverId;
	}
	/**
	 * 
	 * @param receiverId a identify of a receiver.
	 * @param stationStatusList all stations, which belong the scenario.
	 */
	public SwarmPerception(int receiverId,Hashtable<String,DefaultStation> stationStatusList){
	//	super(agent, currentStation);
		this.receiverId=receiverId;
		SwarmPerception.stationStatusList=stationStatusList;
	}
	
	/**
	 * 
	 * @param receiverId a identify of a receiver.
	 * @param stationStatusList all stations, which belong the scenario.
	 * @param StatusAgentComponents the status of agent which is running the scenario.
	 */
	public SwarmPerception(Hashtable<String, DefaultStation> stationStatusList,StatusAgentComponents statusAgent){
		//this.receiverId=statusAgent.getAgent();
		SwarmPerception.stationStatusList=stationStatusList;
		receiverName=statusAgent.getAgent().getName();
		currentStation=statusAgent.getCurrent();
		targetStation=statusAgent.getTarget();
		distatnce=statusAgent.getDistance();
		visited=statusAgent.getVisited();
	}
	
	/**
	 * 
	 * @param receiverId a identify of a receiver.
	 * @param StatusAgentComponents the status of agent which is running the scenario.
	 */
	public SwarmPerception(int receiverId,StatusAgentComponents statusAgent){
		this.receiverId=receiverId;
		//SwarmPerception.stationStatusList=stationStatusList;
		receiverName=statusAgent.getAgent().getName();
		currentStation=statusAgent.getCurrent();
		targetStation=statusAgent.getTarget();
		distatnce=statusAgent.getDistance();
		visited=statusAgent.getVisited();
	}
	
	/**
	 * 
	 * @param receiverName a name of the receiver.
	 * @param stationStatusList all stations, which belong the scenario.
	 */
	public SwarmPerception(String receiverName,Hashtable<String, DefaultStation> stationStatusList){
		this.receiverName=receiverName;
		SwarmPerception.stationStatusList=stationStatusList;
	}
	/**
	 * 
	 * @return This return a String as receiverId. Here, that is a name
	 */
	@Override
	public String getReceiverId() {
		return receiverName;
	}
	/**
	 * 
	 * @return This return a Integer as receiverId. Here, that is a identify.
	 */
	public int getReceiverIdId() {
		return receiverId;
	}
	
	/**
	 * 
	 * @return a status of all stations from the receiver's seite. 
	 */
	public Hashtable <String,DefaultStation > getStationStatusList(){
		return stationStatusList;
	}
	@Override
	public String toString() {
		return "[Receiver: " + receiverName + "; CurrentStation: " + currentStation + "; Next Station: " + targetStation + "; rest way: " + distatnce + "; visited: "+visited+" ]";
	}
	//@Override
	public SpeechActType getType() {
		// TODO Auto-generated method stub
		return null;
	}
	//@Override
	public Set<FolFormula> getContent() {
		// TODO Auto-generated method stub
		return null;
	}
}