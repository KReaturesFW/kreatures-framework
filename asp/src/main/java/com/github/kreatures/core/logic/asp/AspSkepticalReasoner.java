package com.github.kreatures.core.logic.asp;

import java.util.List;
import java.util.Set;

import net.sf.tweety.lp.asp.syntax.DLPLiteral;
import net.sf.tweety.lp.asp.util.AnswerSet;

import com.github.kreatures.core.operators.parameter.ReasonerParameter;

/**
 * A reasoner for extended logic programs under the answer set semantics using
 * a skeptical algorithm, that means it creates the intersection of all literals 
 * in all answer sets. 
 * 
 * @author Tim Janus
 */
public class AspSkepticalReasoner extends AspReasoner {
	public AspSkepticalReasoner() throws InstantiationException {
		super();
	}

	@Override
	protected Set<DLPLiteral> selectAnswerSet(ReasonerParameter params,
			List<AnswerSet> answerSets) {
		return skepticalSelection(answerSets);
	}
}
