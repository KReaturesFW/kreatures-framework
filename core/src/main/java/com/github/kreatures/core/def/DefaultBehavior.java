package com.github.kreatures.core.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kreatures.core.Action;
import com.github.kreatures.core.Agent;
import com.github.kreatures.core.KReatures;
import com.github.kreatures.core.KReaturesEnvironment;
import com.github.kreatures.core.EnvironmentBehavior;
import com.github.kreatures.core.Perception;
import com.github.kreatures.core.comm.SpeechAct;

/**
 * Behavior implementing the default KReatures environment behavior.
 * runOneTick will wait till isSimulationReady returns true.
 * @author Tim Janus
 */
public class DefaultBehavior implements EnvironmentBehavior  {

	private static Logger LOG = LoggerFactory.getLogger(DefaultBehavior.class);
	
	protected boolean doingTick = false;
	
	protected boolean kreaturesReady = true;
	
	protected boolean somethingHappens = false;
	
	/** the actual simulation tick */
	protected int tick = 0;
	
	
	@Override
	public void sendAction(KReaturesEnvironment env, Action act) {
		// The action send by one agent is the perception of the other one.
		somethingHappens = true;
		
		// forward the action if it can be perceived by other agents
		if(act instanceof Perception) {
			Perception per = (Perception) act;
			String agentName = per.getReceiverId();
			localDelegate(env, per, agentName);
		}
	}

	@Override
	public void receivePerception(KReaturesEnvironment env, Perception percept) {
		String agentName = percept.getReceiverId();
		localDelegate(env, percept, agentName);
	}
	
	/**
	 * Helper method: delegates the perception/action to the local agents.
	 * @param env
	 * @param percept
	 * @param agentName
	 */
	protected void localDelegate(KReaturesEnvironment env, Perception percept, String agentName) {
		if(SpeechAct.ALL.equals(agentName)) {
			for(Agent agent : env.getAgents()) {
				agent.perceive(percept);
			}
		} else {
			Agent ag = env.getAgentByName(agentName);
			if(ag != null) {
				ag.perceive(percept);
			} else {
				LOG.warn("Action/Perception was not send, agent '{}' was not found in environment.", agentName);
			}
		}
	}

	@Override
	public boolean runOneTick(KReaturesEnvironment env) {
		doingTick = true;
		
		while(!isSimulationReady()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(!somethingHappens && tick != 0)
			return false;
		
		somethingHappens = false;
		kreaturesReady = false;
		++tick;
		KReatures.getInstance().onTickStarting(env);
		
		List<Agent> orderedAlphabetically = new ArrayList<>(env.getAgents());
		Collections.sort(orderedAlphabetically, new Comparator<Agent>() {
			@Override
			public int compare(Agent o1, Agent o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for(Agent agent : orderedAlphabetically) {
			// cycle internally sends the selected action
			// to the environment using sendAction() method.
			agent.cycle();
		}
		kreaturesReady = true;
		
		doingTick = false;
		KReatures.getInstance().onTickDone(env);
		return true;
	}

	@Override
	public boolean run(KReaturesEnvironment env) {
		boolean reval = false;
		while(reval = runOneTick(env));
		return reval;
	}

	@Override
	public boolean isKReaturesReady() {
		return kreaturesReady;
	}

	@Override
	public boolean isSimulationReady() {
		return true;
	}

	@Override
	public boolean isDoingTick() {
		return doingTick;
	}

	@Override
	public int getTick() {
		return tick;
	}

}
