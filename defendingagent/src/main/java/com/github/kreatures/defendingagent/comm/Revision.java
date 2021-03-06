package com.github.kreatures.defendingagent.comm;

import java.util.HashSet;
import java.util.Set;

import net.sf.tweety.logics.fol.syntax.FolFormula;

import org.simpleframework.xml.Element;

import com.github.kreatures.core.Agent;
import com.github.kreatures.core.comm.SpeechAct;
import com.github.kreatures.core.reflection.FolFormulaVariable;

/**
 * Implementation of the Speech-Act: Revision.
 * A Revision speech act represents a revision request by an attacking agent.
 * It contains a first order variable.
 * @author Pia Wierzoch, Sebastian Homann
 */
public class Revision extends SpeechAct {

	/** formula representing the question of the query */
	@Element(name="proposition", required=true)
	private FolFormulaVariable proposition;
	
	/** Ctor used by deserilization */
	public Revision(	@Element(name="sender") String sender, 
					@Element(name="receiver") String receiver, 
					@Element(name="proposition") FolFormulaVariable proposition) {
		super(sender, receiver);
		this.proposition = proposition;
	}
	
	/**
	 * Ctor: generating the query speech act with the following parameters.
	 * @param sender		The reference to the sendeing agent
	 * @param receiverId	unique name  of the receiver of the query.
	 * @param question		formula representing the query question.
	 */
	public Revision(Agent sender, String receiverId, FolFormula proposition) {
		super(sender, receiverId);
		this.proposition = new FolFormulaVariable(proposition);
	}
	
	/** @return formula representing the question of the query */
	public FolFormula getProposition() {
		return proposition.getInstance(getAgent() == null ? null : getAgent().getContext());
	}
	
	@Override 
	public String toString() {
		return "< " + getSenderId() + " revision " + getReceiverId() + " " + proposition + " >";
	}

	/** @return true if the question is an open question, false if the question is not open.*/
	public boolean isOpen() {
		return !proposition.getInstance(getAgent().getContext()).isGround();
	}

	@Override
	public SpeechActType getType() {
		return SpeechActType.SAT_INFORMATIVE;
	}

	@Override
	public Set<FolFormula> getContent() {
		Set<FolFormula> reval = new HashSet<>();
		reval.add(getProposition());
		return reval;
	}
}
