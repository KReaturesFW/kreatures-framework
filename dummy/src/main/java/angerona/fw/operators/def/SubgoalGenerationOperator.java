package angerona.fw.operators.def;

import java.io.StringReader;
import java.util.Set;

import net.sf.tweety.logics.firstorderlogic.parser.FolParserB;
import net.sf.tweety.logics.firstorderlogic.parser.ParseException;
import net.sf.tweety.logics.firstorderlogic.syntax.Atom;
import net.sf.tweety.logics.firstorderlogic.syntax.FolFormula;
import net.sf.tweety.logics.firstorderlogic.syntax.FolSignature;
import net.sf.tweety.logics.firstorderlogic.syntax.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import angerona.fw.Agent;
import angerona.fw.Desire;
import angerona.fw.Skill;
import angerona.fw.Subgoal;
import angerona.fw.comm.Answer;
import angerona.fw.comm.Query;
import angerona.fw.comm.Inform;
import angerona.fw.logic.AngeronaAnswer;
import angerona.fw.logic.AnswerValue;
import angerona.fw.logic.Desires;
import angerona.fw.operators.BaseSubgoalGenerationOperator;
import angerona.fw.operators.parameter.SubgoalGenerationParameter;
import angerona.fw.reflection.Context;

/**
 * Default subgoal generation generates the atomic actions need to react on the
 * different speech acts. Subclasses can use the default behavior to react to speech
 * acts.
 * Also implements specialized methods for the simple version of the strike-committee-example.
 * @author Tim Janus
 */
public class SubgoalGenerationOperator extends BaseSubgoalGenerationOperator {

	/** reference to the logback instance used for logging */
	private static Logger LOG = LoggerFactory.getLogger(SubgoalGenerationOperator.class);
	
	@Override
	protected Boolean processInt(SubgoalGenerationParameter pp) {
		LOG.info("Run Default-Subgoal-Generation");
		Agent ag = pp.getActualPlan().getAgent();

		boolean reval = processPersuadeOtherAgentsDesires(pp, ag);

		Desires des = ag.getDesires();
		if(des != null) {
			Set<Desire> actual;
			actual = des.getDesiresByPredicate(GenerateOptionsOperator.prepareQueryProcessing);
			for(Desire d : actual) {
				reval = reval || answerQuery(d, pp, ag);
			}
			
			actual = des.getDesiresByPredicate(GenerateOptionsOperator.prepareRevisionRequestProcessing);
			for(Desire d : actual) {
				reval = reval || revisionRequest(d, pp, ag);
			}
			
			// Todo prepare reason
		}
		
		if(!reval)
			report("No new subgoal generated.", ag);
		return reval;
	}

	/**
	 * This is a helper method: Which searches for desires starting with the prefix 'v_'.
	 * It creates RevisionRequests for such desires.
	 * @param pp		The data-structure containing parameters for the operator.
	 * @param ag		The agent.
	 * @return			true if a new subgoal was created and added to the master-plan, false otherwise.
	 */
	protected boolean processPersuadeOtherAgentsDesires(
			SubgoalGenerationParameter pp, Agent ag) {
		boolean reval = false;
		if(ag.getDesires() == null)
			return false;
		
		for(Desire desire : ag.getDesires().getDesires()) {
			Atom atom = desire.getAtom();
			if(atom.toString().trim().startsWith("v_")) {
				int si = atom.toString().indexOf("_")+1;
				int li = atom.toString().indexOf("(", si);
				if(si == -1 || li == -1)
					continue;
				String recvName = atom.toString().substring(si, li);
				
				si = atom.toString().indexOf("(")+1;
				li = atom.toString().indexOf(")");
				if(si == -1 || li == -1)
					continue;
				String content = atom.toString().substring(si,li);
				
				LOG.info("'{}' wants '"+recvName+"' to believe: '{}'",  ag.getName(), content);
		
				Skill rr = (Skill) ag.getSkill("RevisionRequest");
				if(rr == null) {
					LOG.warn("'{}' has no Skill: '{}'.", ag.getName(), "RevisionRequest");
					continue;
				}
				Subgoal sg = new Subgoal(ag, desire);
				FolParserB parser = new FolParserB(new StringReader(content));
				Atom a = null;
				try {
					a = parser.atom(new FolSignature());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Context c = new Inform(ag.getName(), recvName, a).getContext();
				sg.newStack(rr, c);
				ag.getPlanComponent().addPlan(sg);
				report("Add the new atomic action '"+rr.getName()+"' to the plan, choosed by desire: " + desire.toString(), ag.getPlanComponent());
				reval = true;
			}
		}
		return reval;
	}

	/**
	 * Helper method: Reacts on desires which were created by a Query. The default implementation creates two answers for the query:
	 * one with the answer-value true the other with the answer-value false. Complex queries are not supported by this method.
	 * @param des	The desire containing the query to answer
	 * @param pp	The data-structure containing parameters for the operator.
	 * @param ag	The agent
	 * @return		true if a new subgoal was created and added to the master-plan, false otherwise.
	 */
	protected Boolean answerQuery(Desire des, SubgoalGenerationParameter pp, Agent ag) {
		Skill qaSkill = (Skill) ag.getSkill("QueryAnswer");
		if(qaSkill == null) {
			LOG.warn("Agent '{}' does not have Skill: 'QueryAnswer'", ag.getName());
			return false;
		}
		
		Subgoal answer = new Subgoal(ag, des);
		Query q = (Query) des.getPerception();
		answer.newStack(qaSkill, 
				new Answer(q.getReceiverId(), q.getSenderId(), q.getQuestion(), AnswerValue.AV_TRUE));
		
		answer.newStack(qaSkill,
				new Answer(q.getReceiverId(), q.getSenderId(), q.getQuestion(), AnswerValue.AV_FALSE));
		
		ag.getPlanComponent().addPlan(answer);
		report("Add the new atomic action '"+qaSkill.getName()+"' to the plan", ag.getPlanComponent());
		return true;
	}
	
	/**
	 * Helper method for handling desires which are created by a revision-request of an other agent.
	 * The default implementation does nothing generic and subclasses are free to implement their own
	 * behavior. Nevertheless the method 'implements' the default behavior for the simple SCM 
	 * scenario: It queries the sender of the revision-request 'excused' for 'attend_scm'.
	 * @param des	The desire created by the revision-request perception
	 * @param pp	The data-structure containing parameters for the operator.
	 * @param ag	The agent.
	 * @return		true if a new subgoal was created and added to the master-plan, false otherwise.
	 */
	protected Boolean revisionRequest(Desire des, SubgoalGenerationParameter pp, Agent ag) {
		// three cases: accept, query (to ensure about something) or deny.
		// in general we will accept all Revision queries but for the scm example
		// it is proofed if the given atom is 'excused' and if this is the case
		// first of all attend_scm is queried.
		if(!(des.getPerception() instanceof Inform))
			return false;
		
		Inform rr = (Inform) des.getPerception();
		if(rr.getSentences().size() == 1) {
			FolFormula ff = rr.getSentences().iterator().next();
			if(	ff instanceof Atom && 
				((Atom)ff).getPredicate().getName().equalsIgnoreCase("excused")) {
				Atom reasonToFire = new Atom(new Predicate("attend_scm"));
				AngeronaAnswer aa = ag.getBeliefs().getWorldKnowledge().reason(reasonToFire);
				if(aa.getAnswerValue() == AnswerValue.AV_UNKNOWN) {
					Skill query = (Skill) ag.getSkill("Query");
					Subgoal sg = new Subgoal(ag, des);
					sg.newStack(query, new Query(ag.getName(), rr.getSenderId(), reasonToFire).getContext());
					ag.getPlanComponent().addPlan(sg);
					report("Add the new atomic action '" + query.getName() + "' to the plan.", ag.getPlanComponent());
				} else if(aa.getAnswerValue() == AnswerValue.AV_FALSE) {
					return false;
				}
				return true;
			}
		}
		
		
		return false;
	}

}
