package angerona.fw;

import angerona.fw.logic.ViolatesResult;
import angerona.fw.reflection.Context;
import angerona.fw.reflection.ContextFactory;
import angerona.fw.reflection.ContextProvider;

/**
 * Base class for different Actions used in the Angerona framework.
 * @author Tim Janus
 */
public class Action implements Perception, ContextProvider {
	/** the unique name of the sender of the action **/
	private String sender;
	
	/** the unique name of the receiver of the action, might be null or sender in later implementations */
	private String receiver;

	/** the flag contains information about the last violates run */
	private ViolatesResult violates;
	
	/** this field is used if the action should be received by every agent in the network */
	public static final String ALL = "__ALL__";
	
	@Override
	public ViolatesResult violates() {
		return violates;
	}
	
	@Override
	public void setViolates(ViolatesResult res) {
		violates = res;
	}
	
	
	/**
	 * Ctor: generates a new Action
	 * @param sender	unique name of the sender of the action
	 * @param receiver	unique name of the receiver of the action, static member ALL means everyone receives this action
	 */
	public Action(String sender, String receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}
	
	/**	@return the unique name of the sender */
	public String getSenderId() {
		return sender;
	}
	
	/** @return the unique name of the receiver, the static member ALL means everyone should receive this action */
	public String getReceiverId() {
		return receiver;
	}
	
	@Override
	public String toString() {
		return "A: " + sender + " --> " + receiver;
	}
	
	@Override
	public Context getContext() {
		return ContextFactory.createContext(this);
	}
	//************Begin Daniel's changes*************//
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Action)) 
			return false;
		
		Action a = (Action)obj;
		if(!this.sender.equals(a.sender)) {
			return false;
		}
		if(!this.receiver.equals(a.receiver))
		{
			return false;
		}
		return true;
	}
	//************End Daniel's changes*************//
}
