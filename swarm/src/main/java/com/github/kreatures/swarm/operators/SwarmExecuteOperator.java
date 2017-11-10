package com.github.kreatures.swarm.operators;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kreatures.core.EnvironmentComponent;
import com.github.kreatures.core.KReaturesConst;
import com.github.kreatures.core.KReaturesPaths;
//import com.github.kreatures.core.PlanComponent;
import com.github.kreatures.core.PlanElement;
import com.github.kreatures.core.SwarmPlanComponent;
import com.github.kreatures.core.logic.FolBeliefbase;
import com.github.kreatures.core.operators.BaseExecuteOperator;
import com.github.kreatures.core.operators.parameters.ExecuteParameter;
import com.github.kreatures.core.util.Pair;
import com.github.kreatures.swarm.SwarmContextConst;
import com.github.kreatures.swarm.Utility;
import com.github.kreatures.swarm.basic.SwarmSpeechAct;
import com.github.kreatures.swarm.basic.SwarmDesires;
import com.github.kreatures.swarm.predicates.PredicateAgent;
import com.github.kreatures.swarm.predicates.PredicateAgentType;
import com.github.kreatures.swarm.predicates.PredicateCurrentStation;
import com.github.kreatures.swarm.predicates.PredicateEnterStation;
import com.github.kreatures.swarm.predicates.PredicateItemSetLoadingAgent;
import com.github.kreatures.swarm.predicates.PredicateItemSetLoadingStation;
import com.github.kreatures.swarm.predicates.PredicateName;
import com.github.kreatures.swarm.predicates.PredicateNecAgentStation;
import com.github.kreatures.swarm.predicates.PredicateProductConsumItem;
import com.github.kreatures.swarm.predicates.PredicateStation;
import com.github.kreatures.swarm.predicates.PredicateStationTypItem;
import com.github.kreatures.swarm.predicates.PredicateStationType;
import com.github.kreatures.swarm.predicates.PredicateTimeEdgeLockGet;
import com.github.kreatures.swarm.predicates.PredicateTimeEdgeLockState;
import com.github.kreatures.swarm.predicates.PredicateTimeEdgeState;
import com.github.kreatures.swarm.predicates.SwarmPredicate;
import com.github.kreatures.swarm.basic.MainDesire;

/**
 * @author Cedric Perez Donfack
 */
public class SwarmExecuteOperator extends BaseExecuteOperator {

	/** reference to the logback logger instance */
	private Logger LOG = LoggerFactory.getLogger(SwarmExecuteOperator.class);

	/**
	 * reference to environment component of this simulation.
	 */
	private EnvironmentComponent envComponent;

	// /**
	// * This attribute helps a agent to know which atoms it has to
	// * update when it leaves a station.
	// * visitTyp
	// * =0, if the agent has taken a item.
	// * =1, if the agent has placed a item.
	// * =2, if the agent has placed a item and a other agent can take it.
	// * =3, if the agent has taken and placed a item.
	// * =4, if the agent has only visit a station. This is the default value.
	// */
	// private int visitTyp=4;
	@Override
	protected Boolean processImpl(ExecuteParameter params) {
		boolean check = false;
		PlanElement pe = (PlanElement) params.getAgent().getContext().get(SwarmContextConst._ACTION);
		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);
		if (pe == null){//TODO Agent Muss be remove
			if(!desires.isLogDataLeer()){
				String simName=params.getAgent().getEnvironment().getName();
				Path logDataDir=Paths.get(KReaturesPaths.KREATURES_EXAMPLES_DIR.toString()).resolve(simName).resolve(KReaturesConst._KREaturesLogDataFolderName);
				Path logDataFile=logDataDir.resolve(params.getAgent().getName()+KReaturesConst._KREaturesLogDataFileExt);
				try {
					Files.deleteIfExists(logDataFile);
					Files.createFile(logDataFile);
					try(BufferedWriter bufferW= Files.newBufferedWriter(logDataFile)){
						for(String str:desires.getLogData()){
							bufferW.write(str);
						}
						bufferW.flush();
						desires.clearLogData();
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return check;
		}
		envComponent = params.getEnvComponent();
		SwarmSpeechAct action = (SwarmSpeechAct) pe.getIntention();

		switch (action.getActionTyp()) {
		case MOVE:
			check = doMove(action, params);


			break;

		case ENTER_STATION:
			check = doEnter(action, params);
			break;

		case CONSUM_ITEM:
			doProductConsumItem(action, params);
			break;

		case PRODUCT_CONSUM_ITEM:
			/*
			 * Product and Consume must execute at the same time. Hereby we have
			 * to use ItemSetLoading of all the agent instead of the
			 * ItemSetLoadingStation.
			 */
			doProductConsumItem(action, params);
			break;

		case PRODUCT_ITEM:
			doProductConsumItem(action, params);
			break;

		case VISIT_STATION:
			check = doVisit(action, params);
			break;

		case LEAVE_STATION:
			check = doLeave(action, params);
			break;
		case WAIT:LOG.debug("Agent is waiting.");
		default:
			return false;
		}

		desires.addLogData(Utility.logData(params.getAgent().getEnvironment().getSimulationTick(),action.getActionTyp(),((PredicateStation)desires.getCurrentDesire()).getName(),desires.getWaitTime()));
		return check;
	}

	private boolean doEnter(SwarmSpeechAct action, ExecuteParameter params) {

		String[] Options = { PredicateName.EnterStation.toString(), PredicateName.Agent.toString(),
				PredicateName.AgentType.toString(), PredicateName.Station.toString(),
				PredicateName.NecAgentStation.toString(), PredicateName.ChoiceStation.toString(),
				PredicateName.TimeEdgeState.toString(),PredicateName.TimeEdgeLockGet.toString(),PredicateName.TimeEdgeLockState.toString() };

		// get a object of FolBeliefbase
		FolBeliefbase folBB = (FolBeliefbase) params.getBaseBeliefbase();

		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);
		/**
		 * The first parameter is use to checked whether the agent can enter a
		 * station or not.
		 * 
		 * first True means that the agent can enter a station and false
		 * otherwise. second the plan whose action a agent has to repeat.
		 */
		Pair<Boolean, PlanElement> checkEnter = desires.getCheckEnterStation();
		// keep a object of FolBeliefbase program
		Set<SwarmPredicate> result = envComponent.askEnvironment(folBB, Options);
		//		get enterstation predicate with current agent and same choose station
		Set<SwarmPredicate> enterStationSet = result.stream()
				.filter(predicate -> (predicate instanceof PredicateEnterStation) && ((PredicateEnterStation) predicate)
						.getStationName().equals(desires.getCurrentStation().getStationName()))
						.collect(HashSet::new, HashSet::add, HashSet::addAll);

		Optional<PredicateEnterStation> optEnterStationReady = enterStationSet.stream()
				.map(predicate -> (PredicateEnterStation) predicate).filter(predicate -> predicate.getMotiv() < 4)
				.findFirst();

		/* add currentStation to the action */
		PredicateCurrentStation currentStation = desires.getCurrentStation();
		if (desires.getCurrentMainDesire() == MainDesire.CHOSE_STATION) {
			desires.setCurrentMainDesire(MainDesire.WAIT);
			action.getActions().add(currentStation);
		}

		/* check whether agent can enter the station. */
		if (enterStationSet.isEmpty()) {

			if (desires.getWaitTime() == 0) {
				desires.clear();
				params.getAgent().getComponent(SwarmPlanComponent.class).clear();
				desires.initWaitTime();
				currentStation.setHasChoose(false);
				action.getActions().add(currentStation);
				checkEnter.first = false;
				return false;
			}
			checkEnter.first = true;
			desires.decrWaitTime();
			return false;
		}

		/* update the Timeedgestate predicate which will be performed. */
		Set<PredicateTimeEdgeState> timeEdgeStateSet;
		//		
		PredicateEnterStation enterStationReady = optEnterStationReady.orElseGet(
				() -> enterStationSet.stream().map(predicate -> (PredicateEnterStation) predicate).findFirst().get());
		/*
		 * %motiv: 0=agent and station no time edge;
		 * %	1=agent has time edge and station no;
		 * %	2=agent hasn't time edge and station has;
		 * %	3=agent and station haven time edge. 
		 */
		if (!desires.isLock() && enterStationReady.getMotiv() != 0) {

			switch (enterStationReady.getMotiv()) {
			case 1:/* if 1 or 4 */
			case 4:
				timeEdgeStateSet = result.stream().filter(predicate -> (predicate instanceof PredicateTimeEdgeState))
				.map(predicate -> (PredicateTimeEdgeState) predicate)
				.filter(predicate -> predicate.getName().equals(enterStationReady.getAgentName())
						&& predicate.getVisitorName().equals(enterStationReady.getStationName()))
						.collect(HashSet::new, HashSet::add, HashSet::addAll);
				break;
			case 2:/* if 2 or 5 */
			case 5:
				timeEdgeStateSet = result.stream().filter(predicate -> (predicate instanceof PredicateTimeEdgeState))
				.map(predicate -> (PredicateTimeEdgeState) predicate)
				.filter(predicate -> predicate.getName().equals(enterStationReady.getStationName())&& predicate.getVisitorName().equals(enterStationReady.getAgentName()))
				.collect(HashSet::new, HashSet::add, HashSet::addAll);
				break;
			case 3:/* if 3 or 6 */
			case 6:
				timeEdgeStateSet = result.stream().filter(predicate -> (predicate instanceof PredicateTimeEdgeState))
				.map(predicate -> (PredicateTimeEdgeState) predicate)
				.filter(predicate -> (predicate.getName().equals(enterStationReady.getStationName())
						&& predicate.getVisitorName().equals(enterStationReady.getAgentName()))
						|| (predicate.getName().equals(enterStationReady.getAgentName()))
						&& predicate.getVisitorName().equals(enterStationReady.getStationName()))
						.collect(HashSet::new, HashSet::add, HashSet::addAll);
				break;
			default:
				timeEdgeStateSet = new HashSet<PredicateTimeEdgeState>();
			}

			/* 
			 * Is the current agent in waiting or ready state.
			 * %Status	=3 means: the both components are waiting without waiting time
			 * %Status	=2 means: the both components are reading after the waiting time
			 * %Status  =1 means: one component is ready and the other can begin to count.
			 * %Status  =0 means: the both components are waiting
			 * %Status  >3 means: the current agent is in waiting state.
			 */
			if (optEnterStationReady.isPresent()) {
				/*
				 * Check whether the agent has already lock some timeedge.
				 * This can happen, when a agent have to waiting before beginning. 
				 */
				if(desires.isTimeEdgeLockStateEmpty()){
					/*
					 * All the timeedgelockstate predicate.
					 */
					Set<PredicateTimeEdgeLockState> timeEdgeLockStateSet= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockState)
							.map(predicate->(PredicateTimeEdgeLockState)predicate)
							.collect(HashSet::new,HashSet::add,HashSet::addAll);

					Set<PredicateTimeEdgeLockGet> TimeEdgeLockGetSet= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockGet)
							.map(predicate->(PredicateTimeEdgeLockGet)predicate)
							.collect(HashSet::new, HashSet::add, HashSet::addAll);

					//					When the are timeedge of type NoDNoC then do following				
					Optional<PredicateTimeEdgeLockState> OptTimeEdgeLock= timeEdgeLockStateSet.stream()
							.filter(predicate->{return predicate.getEdgeType()==0 & predicate.compareToCurrentStation(currentStation);})
							.findFirst();
					if(OptTimeEdgeLock.isPresent()){
						PredicateTimeEdgeLockState timeEdgeLock=OptTimeEdgeLock.get();
						timeEdgeLock.setLock2(true);
						action.getActions().add(timeEdgeLock);
						desires.setTimeEdgeLockState(timeEdgeLock);

					}else{
						Set<PredicateTimeEdgeLockGet> TimeEdgeLockGetSetNoDNoC= TimeEdgeLockGetSet.stream()
								.filter(predicate->{return predicate.getEdgeType()==0 & predicate.compareToCurrentStation(currentStation);})
								.filter(predicate->!predicate.isLock1()&!predicate.isLock2())
								.collect(HashSet::new, HashSet::add, HashSet::addAll);

						if(!TimeEdgeLockGetSetNoDNoC.isEmpty()){
							Random random=new Random();
							int rd=random.nextInt(TimeEdgeLockGetSetNoDNoC.size());
							int i=0;
							for(PredicateTimeEdgeLockGet timeEdgeLockGet :TimeEdgeLockGetSetNoDNoC){
								if(rd==i){
									PredicateTimeEdgeLockState timeEdgeLock=timeEdgeLockGet.convertToTimeEdgeLockState();
									timeEdgeLock.setLock1(true);
									timeEdgeLock.setActiv(true);
									action.getActions().add(timeEdgeLock);
									desires.setTimeEdgeLockState(timeEdgeLock);
									break;
								}
								i++;
							}
						}
					}

					//										When the are timeedge of type DNoC then do following

					Set<PredicateTimeEdgeLockGet> TimeEdgeLockGetSetDNoC= TimeEdgeLockGetSet.stream()
							.filter(predicate->{return predicate.getEdgeType()==1;})
							.collect(HashSet::new, HashSet::add, HashSet::addAll);

					/*
					 * check whether the current agent is at the first position.
					 */

					Optional<PredicateTimeEdgeLockGet> optTimeEdgeLockGet=TimeEdgeLockGetSetDNoC.stream()
							.filter(predicate->{return predicate.compareToCurrentStation(currentStation);})
							.findFirst();

					if(optTimeEdgeLockGet.isPresent()){
						/*
						 * the current agent is at the first position
						 * */
						PredicateTimeEdgeLockState timeEdgeLock=optTimeEdgeLockGet.get().convertToTimeEdgeLockState(true);
						timeEdgeLock.setLock1(true);
						timeEdgeLock.setActiv(true);
						action.getActions().add(timeEdgeLock);
						desires.setTimeEdgeLockState(timeEdgeLock);

					}


					//					When the are timeedge of type DNoC then do following

					/*
					 * All the timeedgelockstate predicate.
					 */
					//						Set<PredicateTimeEdgeLockState> timeEdgeLockStateSet= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockState)
					//								.map(predicate->(PredicateTimeEdgeLockState)predicate)
					//								.collect(HashSet::new,HashSet::add,HashSet::addAll);
					//
					//						Set<PredicateTimeEdgeLockGet> TimeEdgeLockGetSet= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockGet)
					//								.map(predicate->(PredicateTimeEdgeLockGet)predicate)
					//								.collect(HashSet::new, HashSet::add, HashSet::addAll);

					/*
					 * check whether the current agent is at the second position.
					 */

					//						Set<PredicateTimeEdgeLockGet> TimeEdgeLockGetSetDNoC= TimeEdgeLockGetSet.stream()
					//								.filter(predicate->{return predicate.getEdgeType()==1;})
					//								.collect(HashSet::new, HashSet::add, HashSet::addAll);


					Optional<PredicateTimeEdgeLockGet> optTimeEdgeLockGet1=TimeEdgeLockGetSetDNoC.stream()
							.filter(predicate->{return predicate.compareToKorrespondElement(currentStation);})
							.findAny();

					if(optTimeEdgeLockGet1.isPresent()){
						/*
						 * the current agent is at the second position
						 */
						//							Optional<PredicateTimeEdgeLockState> 
						OptTimeEdgeLock= timeEdgeLockStateSet.stream()
								.filter(predicate->{return predicate.getEdgeType()==1 && predicate.isFinish1()&& !predicate.isLock2()&& predicate.compareToTimeEdgeLockGet(optTimeEdgeLockGet1.get()); })
								.findAny();

						if(OptTimeEdgeLock.isPresent()){
							PredicateTimeEdgeLockState timeEdgeLock= OptTimeEdgeLock.get();
							timeEdgeLock.setCurrentAgentAndStation(currentStation);
							timeEdgeLock.setLock2(true);
							action.getActions().add(timeEdgeLock);
							desires.setTimeEdgeLockState(timeEdgeLock);
						}
					}


				}


				switch (enterStationReady.getStatus()) {
				case 1:
					timeEdgeStateSet.stream().forEach(PredicateTimeEdgeState::incrTick);
					timeEdgeStateSet.stream().forEach(action.getActions()::add);
					desires.setTimeEdgeState(timeEdgeStateSet);
					checkEnter.first = true;
					return false;
				case 0:
				case 2: /* if 0 or 2 or 3 */
				case 3:
					timeEdgeStateSet.stream().forEach(predicate -> {
						predicate.setReady(true);
						//						predicate.setFinish(false);
						predicate.setWaiting(false);
					});
					break;
				default:
					throw new IllegalArgumentException("Status in EnterStation component must be between 0 and 3."); 
				}

			} else{

				if (desires.getWaitTime() == 0) {
					timeEdgeStateSet.stream().forEach(PredicateTimeEdgeState::init);
					desires.clear();
					params.getAgent().getComponent(SwarmPlanComponent.class).clear();
					desires.initWaitTime();
					checkEnter.first = false;
					timeEdgeStateSet.stream().forEach(action.getActions()::add);
					desires.setTimeEdgeState(timeEdgeStateSet);
					currentStation.setHasChoose(false);
					action.getActions().add(currentStation);
				} else{
					timeEdgeStateSet.stream().filter(predicate -> !predicate.isWaiting())
					.forEach(predicate ->{
						//timeEdgeStateSet.stream().forEach();
						predicate.setWaiting(true);
						predicate.setFinish(false);
						action.getActions().add(predicate);
						desires.setTimeEdgeState(timeEdgeStateSet);
					});
					desires.decrWaitTime();
					checkEnter.first = true;
				}

				return false;
			}

			timeEdgeStateSet.stream().forEach(action.getActions()::add);
			desires.setTimeEdgeState(timeEdgeStateSet);
		}

		if(desires.isLock()){
			Set<PredicateTimeEdgeLockState> timeEdgeLockStateSet= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockState)
					.map(predicate->(PredicateTimeEdgeLockState)predicate)
					.filter(predicate->{
						return predicate.hasCurrentStation(currentStation);
					})
					.collect(HashSet::new,HashSet::add,HashSet::addAll);
			
			desires.clearTimeEdgeLockState();
			desires.setTimeEdgeLockState(timeEdgeLockStateSet);
		}
		
		// Check whether the agent has to do its job simultaneously with a other agent
		Optional<PredicateTimeEdgeLockState> optTimeEdgeLockSate= desires.getTimeEdgeLockState().stream().
				filter(predicate->predicate.isEchTime() && (!predicate.isLock1() || !predicate.isLock2())).findFirst();
		if(optTimeEdgeLockSate.isPresent()){
			checkEnter.first = true;
			desires.decrWaitTime();
			desires.setLock(true);
			return false;
		}else{
			desires.setLock(false);
		}			
		/*
		 * At this point, the agent has fulfil all the condition and can enter the station.
		 */
		checkEnter.first = false;
		desires.initWaitTime();

		/* set that agent has enter the station */
		currentStation.setIsInStation(true);
		desires.setCurrentMainDesire(MainDesire.VISIT);
		if (desires.getCurrentMainDesire() != MainDesire.CHOSE_STATION) {
			action.getActions().add(currentStation);
		}

		/* search agent, agentType and necAgentStation */
		PredicateAgent agent = null;
		PredicateAgentType agentType = null;
		PredicateNecAgentStation necAgentStation = null;
		if (desires.getCurrentAgent() == null) {
			int breakForEach = 0;
			for (SwarmPredicate predicate : result) {
				if (breakForEach == 3)
					break;
				if (predicate instanceof PredicateAgent
						&& ((PredicateAgent) predicate).getName().equals(params.getAgent().getName())) {
					agent = (PredicateAgent) predicate;
					desires.setCurrentAgent(agent);
					breakForEach++;
					continue;
				}
				if (predicate instanceof PredicateAgentType
						&& ((PredicateAgentType) predicate).getTypeName().equals(currentStation.getAgentTypeName())) {
					agentType = (PredicateAgentType) predicate;
					desires.setCurrentAgentType(agentType);
					breakForEach++;
					continue;
				}
				if (predicate instanceof PredicateNecAgentStation
						&& ((PredicateNecAgentStation) predicate).getAgentName().equals(currentStation.getAgentName())
						&& ((PredicateNecAgentStation) predicate).getStationName()
						.equals(currentStation.getStationName())) {
					necAgentStation = (PredicateNecAgentStation) predicate;
					breakForEach++;
					continue;
				}
			}
		} else {
			agent = desires.getCurrentAgent();
			agentType = desires.getCurrentAgentType();
			for (SwarmPredicate predicate : result) {
				if (predicate instanceof PredicateNecAgentStation
						&& ((PredicateNecAgentStation) predicate).getAgentName().equals(currentStation.getAgentName())
						&& ((PredicateNecAgentStation) predicate).getStationName()
						.equals(currentStation.getStationName())) {
					necAgentStation = (PredicateNecAgentStation) predicate;
					continue;
				}
			}
		}
		/* add necAgentStation to the action */
		necAgentStation.incrementNec();
		action.getActions().add(necAgentStation);
		/* add agent to the action */
		agent.incrFrequency();
		action.getActions().add(agent);
		/* given the next actions which will be performed. */
		PredicateStation station =result.stream().filter(predicate-> predicate instanceof PredicateStation)
				.map(predicate-> (PredicateStation)predicate)
				.filter(predicate->predicate.equals(desires.getCurrentDesire()))
				.findFirst().orElseGet(()->(PredicateStation)desires.getCurrentDesire());
		desires.setCurrentDesire(station);		

		station.incrFrequency();
		station.incrSpace(agentType.getSize());

		action.getActions().add(station);
		return true;
	}

	private boolean doMove(SwarmSpeechAct action, ExecuteParameter params) {

		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);

		/* add currentStation to the action */
		if (desires.getCurrentMainDesire() == MainDesire.CHOSE_STATION) {
			desires.setCurrentMainDesire(MainDesire.MOVE);
			PredicateCurrentStation currentStation = desires.getCurrentStation();
			action.getActions().add(currentStation);
		}

		desires.getTimeEdgeState().stream().peek(PredicateTimeEdgeState::incrTick).forEach(action.getActions()::add);
		return true;
	}

	private boolean doVisit(SwarmSpeechAct action, ExecuteParameter params) {
		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);
		desires.getTimeEdgeState().stream().peek(PredicateTimeEdgeState::incrTick).forEach(action.getActions()::add);
		return true;
	}

	private boolean doLeave(SwarmSpeechAct action, ExecuteParameter params) {


		String[] Options = { PredicateName.Station.toString(),PredicateName.TimeEdgeLockState.toString()};

		// get a object of FolBeliefbase
		FolBeliefbase folBB = (FolBeliefbase) params.getBaseBeliefbase();

		/**
		 * The first parameter is use to checked whether the agent can enter a
		 * station or not.
		 * 
		 * first True means that the agent can enter a station and false
		 * otherwise. second the plan whose action a agent has to repeat.
		 */
		// keep a object of FolBeliefbase program
		Set<SwarmPredicate> result = envComponent.askEnvironment(folBB, Options);

		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);
		/* add currentStation to the action */
		PredicateCurrentStation currentStation = desires.getCurrentStation();
		desires.setCurrentMainDesire(MainDesire.CHOSE_STATION);
		currentStation.setHasChoose(false);
		currentStation.setIsInStation(false);
		action.getActions().add(currentStation);
		/* search agent and agentType */
		PredicateAgentType agentType = desires.getCurrentAgentType();
		/* add station to the action */
		PredicateStation station =result.stream().filter(predicate-> predicate instanceof PredicateStation)
				.map(predicate-> (PredicateStation)predicate)
				.filter(predicate->predicate.equals(desires.getCurrentDesire()))
				.findFirst().orElseGet(()->(PredicateStation)desires.getCurrentDesire());
		desires.setCurrentDesire(station);


		station.decrSpace(agentType.getSize());
		action.getActions().add(station);

		desires.getTimeEdgeState().stream().forEach(predicate -> {
			predicate.init();
			predicate.setFinish(true);
			action.getActions().add(predicate);
		});

		/*
		 * update or delete TimeEdgeLockState on the beliefbase.
		 */

		if(!desires.isTimeEdgeLockStateEmpty()){

			PredicateTimeEdgeLockState timeEdgeLockStateOld=desires.getTimeEdgeLockState().stream().findFirst().get();

			Optional<PredicateTimeEdgeLockState> OptTimeEdgeLock= result.stream().filter(predicate -> predicate instanceof PredicateTimeEdgeLockState)
					.map(predicate->(PredicateTimeEdgeLockState)predicate)
					.filter(predicate->predicate.equals(timeEdgeLockStateOld))
					.findFirst();

			if(OptTimeEdgeLock.isPresent()){
				desires.clearTimeEdgeLockState();
				PredicateTimeEdgeLockState timeEdgeLockStateNew=OptTimeEdgeLock.get();
				if(timeEdgeLockStateNew.compareToCurrentStation(currentStation)){
					if(timeEdgeLockStateNew.isFinish1()){
						timeEdgeLockStateNew.setActiv(false);
					}
					timeEdgeLockStateNew.setFinish2(true);
					action.getActions().add(timeEdgeLockStateNew);
				}else{
					if(timeEdgeLockStateNew.isFinish2()){
						timeEdgeLockStateNew.setActiv(false);
					}
					timeEdgeLockStateNew.setFinish1(true);
					action.getActions().add(timeEdgeLockStateNew);
				}

			}
		}
		desires.clear();

		return true;

	}

	// private boolean doProductItem() {
	// return false;
	// }
	// private boolean doConsumItem() {
	// return false;
	// }

	private boolean doProductConsumItem(SwarmSpeechAct action, ExecuteParameter params) {
		String[] Options = { PredicateName.ProductConsumItem.toString(), PredicateName.StationType.toString(),
				PredicateName.StationTypItem.toString(), PredicateName.ItemSetLoadingAgent.toString(),
				PredicateName.ItemSetLoadingStation.toString() };
		// get a object of FolBeliefbase
		FolBeliefbase folBB = (FolBeliefbase) params.getBaseBeliefbase();
		/* List of desires and related informations */
		SwarmDesires desires = params.getAgent().getComponent(SwarmDesires.class);
		// keep a object of FolBeliefbase program
		Set<SwarmPredicate> result = envComponent.askEnvironment(folBB, Options);
		PredicateProductConsumItem productConsumItem = null;
		for (SwarmPredicate predicate : result) {
			if (predicate instanceof PredicateProductConsumItem) {
				productConsumItem = (PredicateProductConsumItem) predicate;
				break;
			}
		}
		/* check whether agent can product or consume a item. */
		if (productConsumItem == null)
			return doVisit(action, params);

		/* search agent, agentType and necAgentStation */
		PredicateAgent agent = desires.getCurrentAgent();
		PredicateAgentType agentType = desires.getCurrentAgentType();
		Set<PredicateItemSetLoadingAgent> itemSetLoadingAgents = new TreeSet<>();
		Set<PredicateItemSetLoadingStation> itemSetLoadingStations = new TreeSet<>();
		PredicateStationType stationType = null;
		Set<PredicateStationTypItem> stationTypItems = new TreeSet<>();

		for (SwarmPredicate predicate : result) {
			if (predicate instanceof PredicateItemSetLoadingAgent && ((PredicateItemSetLoadingAgent) predicate)
					.getAgentName().equals(productConsumItem.getAgentName())) {
				itemSetLoadingAgents.add((PredicateItemSetLoadingAgent) predicate);
				continue;
			}
			if (predicate instanceof PredicateItemSetLoadingStation && ((PredicateItemSetLoadingStation) predicate)
					.getStationInName().equals(productConsumItem.getStationName())) {
				itemSetLoadingStations.add((PredicateItemSetLoadingStation) predicate);
				continue;
			}
			if (predicate instanceof PredicateStationType && ((PredicateStationType) predicate).getTypeName()
					.equals(productConsumItem.getStationTypeName())) {
				stationType = (PredicateStationType) predicate;
				continue;
			}
			if (predicate instanceof PredicateStationTypItem) {
				stationTypItems.add((PredicateStationTypItem) predicate);
				continue;
			}
		}

		switch (productConsumItem.getMotiv()) {
		case 0:// =0, : Agent can only take item, because there are no ingoing
			// stations.
			/* update and add itemSetLoadingAgent to the action */
			for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
				if (predicate.getStationTypeName().equals(productConsumItem.getStationTypeName())) {
					predicate.incrItemNumber(1);
					agent.incrCapacity(stationType.getItem());
					action.getActions().add(predicate);
					break;
				}
			}
			break;
		case 1:// =1, : Agent can only place item, because there are no outgoing
			// stations.
			int sumItem1 = 0;
			/* update and add itemSetLoadingAgent to the action */
			for (PredicateStationTypItem predicateStTypItem : stationTypItems) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateStTypItem.getStTypNameOut())) {
						predicate.decrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						sumItem1 += productConsumItem.getItemNumber() * predicateStTypItem.getItemDim();
						break;
					}
				}
			}

			/* update and add itemSetLoadingStation to the action */
			for (PredicateItemSetLoadingStation predicateItemStation : itemSetLoadingStations) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateItemStation.getStationOutTypeName())) {
						predicate.incrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						break;
					}
				}
			}
			agent.decrCapacity(sumItem1);
			break;
		case 2:// =2, : Agent can only place item, because there are outgoing
			// stations, but agent cannot visit it.
			int sumItem2 = 0;
			/* update and add itemSetLoadingAgent to the action */
			for (PredicateStationTypItem predicateStTypItem : stationTypItems) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateStTypItem.getStTypNameOut())) {
						predicate.decrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						sumItem2 += productConsumItem.getItemNumber() * predicateStTypItem.getItemDim();
						break;
					}
				}
			}

			/* update and add itemSetLoadingStation to the action */
			for (PredicateItemSetLoadingStation predicateItemStation : itemSetLoadingStations) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateItemStation.getStationOutTypeName())) {
						predicate.incrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						break;
					}
				}
			}
			agent.decrCapacity(sumItem2);
			break;
		case 3:// =3, : Agent can only take item, because there are ingoing
			// stations, but agent cannot visit it.
			/* update and add itemSetLoadingStation to the action */
			for (PredicateItemSetLoadingStation predicate : itemSetLoadingStations) {
				predicate.decrItemNumber(productConsumItem.getItemNumber());
				action.getActions().add(predicate);
			}
			agent.incrCapacity(productConsumItem.getItemNumber() * stationType.getItem());
			break;
		case 4:// =4, : Agent can take and place item, because there are ingoing
			// and outgoing stations, and the agent can visit it.
			int sumItem4 = 0;
			/* update and add itemSetLoadingAgent to the action */
			for (PredicateStationTypItem predicateStTypItem : stationTypItems) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateStTypItem.getStTypNameOut())) {
						predicate.decrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						sumItem4 += productConsumItem.getItemNumber() * predicateStTypItem.getItemDim();
						break;
					}
				}
			}
			agent.decrCapacity(sumItem4);

			/* update and add itemSetLoadingStation to the action */
			for (PredicateItemSetLoadingStation predicate : itemSetLoadingStations) {
				predicate.decrItemNumber(productConsumItem.getItemNumber());
				action.getActions().add(predicate);
			}
			agent.incrCapacity(productConsumItem.getItemNumber() * stationType.getItem());

		case 5:/*
		 * =5, : Agent can place and cannot take item, because there are
		 * ingoing and outgoing stations, and the condition to take is
		 * not fullfilly.
		 */
			int sumItem5 = 0;
			/* update and add itemSetLoadingAgent to the action */
			for (PredicateStationTypItem predicateStTypItem : stationTypItems) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateStTypItem.getStTypNameOut())) {
						predicate.decrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						sumItem5 += productConsumItem.getItemNumber() * predicateStTypItem.getItemDim();
						break;
					}
				}
			}

			/* update and add itemSetLoadingStation to the action */
			for (PredicateItemSetLoadingStation predicateItemStation : itemSetLoadingStations) {
				for (PredicateItemSetLoadingAgent predicate : itemSetLoadingAgents) {
					if (predicate.getItemNumber() > 0
							&& predicate.getStationTypeName().equals(predicateItemStation.getStationOutTypeName())) {
						predicate.incrItemNumber(productConsumItem.getItemNumber());
						action.getActions().add(predicate);
						break;
					}
				}
			}
			agent.decrCapacity(sumItem5);
			break;
		}

		/* add station to the action */
		//		PredicateStation station = (PredicateStation) desires.getCurrentDesire();

		// station.incrFrequency();
		// station.incrSpace(agentType.getSize());
		//		action.getActions().add(station);
		desires.getTimeEdgeState().stream().peek(PredicateTimeEdgeState::incrTick).forEach(action.getActions()::add);

		return true;
	}

	@Override
	protected void prepare(ExecuteParameter params) {

	}
}