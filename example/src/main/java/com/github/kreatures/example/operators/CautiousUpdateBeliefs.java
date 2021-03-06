package com.github.kreatures.example.operators;

import com.github.kreatures.core.BaseBeliefbase;
import com.github.kreatures.core.Perception;
import com.github.kreatures.core.comm.Inform;
import com.github.kreatures.core.logic.Beliefs;
import com.github.kreatures.core.operators.UpdateBeliefsOperator;
import com.github.kreatures.core.operators.parameter.EvaluateParameter;
import com.github.kreatures.core.util.Utility;

/**
 * This Update Beliefs operator acts more cautious by only updating the view
 * on other agent when receiving an inform speech-act in every other aspect
 * it does the same asthe {@link UpdateBeliefsOperator}.
 * 
 * @author Tim Janus
 */
public class CautiousUpdateBeliefs extends UpdateBeliefsOperator {

	@Override
	protected Beliefs processImpl(EvaluateParameter param) {
		if(param.getAtom() instanceof Inform) {
			Beliefs beliefs = param.getBeliefs();
			Beliefs oldBeliefs = beliefs.clone();
			Inform i = (Inform) param.getAtom();
			BaseBeliefbase bb = null;
			boolean receiver = Utility.equals(i.getReceiverId(), 
					param.getAgent().getName());
			
			// inform speech act only updates views in those scenarios
			String out = "Inform as ";
			if(receiver) {
				bb = beliefs.getViewKnowledge().get(i.getSenderId());
				bb.addKnowledge(i);
				param.report(out + "receiver adapt view on '" + i.getSenderId() + "'", bb);
			}
			
			// inform other components about the update
			if(beliefs.getCopyDepth() == 0) {
				param.getAgent().onUpdateBeliefs((Perception)param.getAtom(), oldBeliefs);
			}
			
			return beliefs;
		} else {
			return super.processImpl(param);
		}
	}
	
}
