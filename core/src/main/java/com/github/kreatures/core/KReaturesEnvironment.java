package com.github.kreatures.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.tweety.logics.fol.syntax.FolFormula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kreatures.core.comm.SpeechAct;
import com.github.kreatures.core.error.AgentIdException;
import com.github.kreatures.core.error.AgentInstantiationException;
import com.github.kreatures.core.internal.Entity;
import com.github.kreatures.core.internal.PluginInstantiator;
import com.github.kreatures.core.logic.Beliefs;
import com.github.kreatures.core.logic.Desires;
import com.github.kreatures.core.logic.ScriptingComponent;
import com.github.kreatures.core.serialize.AgentInstance;
import com.github.kreatures.core.serialize.SimulationConfiguration;
import com.github.kreatures.core.KReaturesSimulationInfo;
import com.github.kreatures.core.util.ObservableMap;

/**
 * A simulation environment for KReatures. This is actually only used for some functional tests.
 * @author Tim Janus
 */
public class KReaturesEnvironment  {

	/** logging facility */
	private static Logger LOG = LoggerFactory.getLogger(KReaturesEnvironment.class);
	
	/** the name of the simulation */
	private String name;
	
	//public <T extends InitSwarmSimulation> iniSwarm(T); 
	/** flag indicating if the environment is correctly initialized */
	private boolean ready = false;
	
	/** a map of entity ids to the entity references */
	private Map<Long, Entity> entities = new HashMap<>();
	
	/** the behavior of the environment, allows different communication protocols and external simulations */
	private EnvironmentBehavior behavior;
	
	/** the root folder of the actual loaded simulation in this environment */
	private String simDirectory;
	/**
	 * This is use to create the appropriated agent type. 
	 * Per default, instance object of Agent class will be created.
	 */
	private AgentFactory agentF;
	
	private ObservableMap<String, AgentAbstract> agentMap = new ObservableMap<>("agentMap");
	
	/**
	 * @return a map of ID --> Entity, the map is not modifiable.
	 */
	public Map<Long, Entity> getEntityMap() {
		return Collections.unmodifiableMap(entities);
	}
	
	/**
	 * Default Ctor: Generates the default-behavior.
	 */
	public KReaturesEnvironment() {
	}
	
	/**
	 * @return a set of strings containing all agent names.
	 */
	public Set<String> getAgentNames() {
		return agentMap.keySet();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * @return the name of the simulation.
	 */
	public String getName() {
		return name;
	}
	
	/** @return 	a string identifying the root directory of the actual 
	 * 			 	running simulation or null if no simulation is initialized. 
	 */
	public String getDirectory() {
		return simDirectory;
	}
	
	/**
	 * Adds the agents with the given name to the environment
	 * @param agent	
	 * @return	true if everything was fine, false if the same agent process was already registered.
	 * @throws AgentIdException Is thrown if the name of the agent process is not unique (two processes have the same name).
	 */
	public boolean addAgent(AgentAbstract agent) throws AgentIdException {
		if(agentMap.containsKey(agent.getName())) {
			if(agentMap.get(agent.getName()) == agent)
				return false;
			
			throw new AgentIdException("agent with name: '" + agent.getName() + "' already registered.");
		}
		
		agentMap.put(agent.getName(), agent);
		KReatures.getInstance().onAgentAdded(this, agent);
		return true;
	}
	
	public Collection<AgentAbstract> getAgents() {
		return Collections.unmodifiableCollection(agentMap.values());
	}
	
	/**
	 * Gets the high level agent with the given name
	 * @param name		unique name of the agent.
	 * @return			Reference to the agent called 'name', if no agent with the given name exists null is returned.
	 */
	public AgentAbstract getAgentByName(String agentName) {
		return agentMap.get(agentName);
	}
	
	/**
	 * runs the simulation using the behavior given at construction.
	 */
	public boolean run() {
		return behavior.run(this);
	}
	
	/**
	 * runs the simulation using the behavior given at construction.
	 */
	public EnvironmentBehavior getBehavior() {
		return behavior;
	}
	
	/**
	 * runs one simulation tick. Gives every agent the ability to call its cycle method.
	 * @return true if at least one agents cylce function was called, false otherwise.
	 */
	public boolean runOneTick() {
		Long before = System.currentTimeMillis();
		boolean reval = behavior.runOneTick(this);
		Long duration = System.currentTimeMillis() - before;
		LOG.info("Tick '{}' Duration: '{}' ms", behavior.getTick(), duration);
		return reval;
	}
	
	/** @return	true if the environment is actually performing a tick, false otherwise. */
	public boolean isDoeingTick() {
		return behavior.isDoingTick();
	}
	
	/** @return true if the simulation is initialized (after the call of initSimulation), false otherwise. */
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * Initializes an KReatures simulation, with the given config. The root directory of the simulation is determined from the configuration file.
	 * @param config	reference to the data-structure containing the configuration of the simulation.
	 * @return	true if everything was fine, false if an error occurred.
	 */
	public synchronized boolean initSimulation(SimulationConfiguration config) {
		if(ready)
			return false;
	
		
		LOG.info("Starting simulation: " + config.getName());
		this.name = config.getName();
		this.simDirectory = config.getFile().getParent();
		
		/* This two following line are add by Donfack in order to 
		 * where simulation the swarm have to use as current
		 */
		KReaturesSimulationInfo.setName(this.name);
		KReaturesSimulationInfo.setSimDirectory(this.simDirectory);
		KReaturesSimulationInfo.setIscurrentSimChange(true);
		

		// inform listener of start of simulation creation:
		KReatures.getInstance().onCreateSimulation(this);

		
		
		if(!createBehavior(config))
			return false;
		
		setAgentFactory(new CreateAgentAdapter());
		
		KReatures.getInstance().onSimulationInit(this);
		if(!registerAgents(config))
			return false;
		
		if(!createAgents(config))
			return false;
				
		KReatures.getInstance().onNewSimulation(this);
		
		// report the initialized data of the agent to the report system.
		for(AgentInstance ai : config.getAgents()) {
			AgentAbstract agent = getAgentByName(ai.getName());
			agent.reportCreation();
			
			// and initialize the desires:
			for(FolFormula a : ai.getDesires()) {
				agent.getComponent(Desires.class).add(new Desire(a));
			}
			
			// and initialize the actions:
			for(SpeechAct a : ai.getActions()) {
				a.setAgent(getAgentByName(a.getSenderId()));
				agent.getComponent(ScriptingComponent.class).add(a);
			}
			
		}
		
		// post the initial perceptions defined in the simulation configuration
		// file to the environment.
		for(Perception p : config.getPerceptions()) {
			if(p instanceof Action) {
				this.sendAction((Action)p);
			}
		}
		
		KReatures.getInstance().onTickDone(this);
		
		/*
		 *  This informs that all components of the new simulation are created.
		 */
		KReaturesSimulationInfo.setIscurrentSimChange(false);
		
		return ready = true;
	}

	/**
	 * Create the agents defined in the simulation configuration. That means it
	 * instantiates the different agent components and registers those as entitys
	 * to the entity-system. 
	 * @remark 	The method assumes that all the agent instances in the config are
	 * 			already registered. That means the java objects exists in the
	 * 			environment, although they are not fully initalized yet.
	 * @param config
	 * @return 	True if the creation (instatiation) of the agents is successful or false
	 * 			if an error occurred during the creation.
	 */
	private boolean createAgents(SimulationConfiguration config) {
		for(AgentInstance ai : config.getAgents()) {
			try {
				// First instantiate the agent components:
				AgentAbstract agent = getAgentByName(ai.getName());		
				LOG.info("Start the creation of Agent '{}'.", ai.getName());
				agent.create(ai, config);
				
				// Second: Register the different agent components to the entity system:
				Beliefs b = agent.getBeliefs();
				BaseBeliefbase world = b.getWorldKnowledge();
				entities.put(agent.getGUID(), agent);
				entities.put(world.getGUID(), world);
				for(BaseBeliefbase actView : b.getViewKnowledge().values()) {
					entities.put(actView.getGUID(), actView);
				}
				
				for(AgentComponent comp : agent.getBeliefs().getComponents()) {
					entities.put(comp.getGUID(), comp);
				}
				
				LOG.info("Agent '{}' successfully created and registered.", agent.getName());
			} catch (AgentInstantiationException e) {
				errorDelegation("Cannot init simulation, something went wrong during agent instatiation: " + 
						e.getMessage());
				return false;
			}
		} 
		return true;
	}

	/***
	 * Helper method: Registers all the agents in the simulation configuration as java
	 * objects to the environment. The agents are not fully initalized after the method
	 * returned successfully.
	 * @param config
	 * @return	true if the registration process is successful or false if an error occurred 
	 * 			during initialization.
	 */
	private boolean registerAgents(SimulationConfiguration config) {
		try {
			for(AgentInstance ai : config.getAgents()) {
				
				if(ai.getType() != null && ai.getType().equals("InteractiveAgent")){
					addAgent(new InteractiveAgent(ai.getName(), this));
				}else{
					addAgent(this.agentF.createAgent(this, ai.getName()));
				}
			}
		} catch (AgentIdException e) {
			e.printStackTrace();
			errorDelegation("Cannot init simulation, something went wrong during agent registration: " + e.getMessage());
			return false;
		}
		return true;
	}

	/** 
	 * Helper method: cleans the simulation up and delegates the error message to Delegates an error message to 
	 * two sources: The logging output and all interested KReatures Error Listeners.
	 * @param errorOutput	String containing the error message.
	 */
	protected void errorDelegation(String errorOutput) {
		this.cleanupEnvironment();
		LOG.error(errorOutput);
		KReatures.getInstance().onError("Simulation Initialization", errorOutput);
	}

	/**
	 * Helper method: Creates the correct behavior class with the given class name
	 * form the simulations xml file.
	 * @param config		reference to the config.
	 * @return				true if the creation was successful, false otherwise.
	 */
	private boolean createBehavior(SimulationConfiguration config) {
		if(config.getBehaviorCls() != null) {
			String error = null;
			try {
				behavior = PluginInstantiator.getInstance().createEnvironmentBehavior(
						config.getBehaviorCls());
			} catch (InstantiationException e) {
				e.printStackTrace();
				error = e.getMessage();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				error = e.getMessage();
			}
			
			if(error != null) {
				KReatures.getInstance().onError("Simulation initialization", error);
				LOG.error(error);
				return false;
			}
		} else {
			String error = "No behavior given in simulation configuration file.";
			KReatures.getInstance().onError("Simulation initialization", error);
			LOG.error(error);
			return false;
		}
		return true;
	}
	
	/**
	 * deletes all agents from the environment and removes the information about the last simulation.
	 */
	public synchronized void cleanupEnvironment() {
		if(ready == false)
			return;
		
		agentMap.clear();
		ready = false;
		KReatures.getInstance().onSimulationDestroyed(this);
	}
	
	public void sendAction(Action action) {
		behavior.sendAction(this, (Action)action);
	}

	public int getSimulationTick() {
		return behavior.getTick();
	}
	public void setAgentFactory(AgentFactory agentFactory) {
		this.agentF=agentFactory;
	}
	public AgentFactory getAgentFactory() {
		return this.agentF;
	}
}
