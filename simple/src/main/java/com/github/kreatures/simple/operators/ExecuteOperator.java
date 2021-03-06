package com.github.kreatures.simple.operators;

import java.util.List;

import com.github.kreatures.core.Action;
import com.github.kreatures.core.Agent;
import com.github.kreatures.core.Intention;
import com.github.kreatures.core.PlanElement;
import com.github.kreatures.core.Subgoal;
import com.github.kreatures.secrecy.operators.parameter.PlanParameter;
import com.github.kreatures.core.operators.Operator;
import com.github.kreatures.simple.operators.parameter.SimplePlanParameter;
import com.github.kreatures.core.util.Pair;

/**
 * I decided to add an ExecuteOperator, that realizes the
 * ActionSelectionFunction from the BDI-Architecture, because apparently it has
 * not been implemented yet.
 * 
 * @author Manuel Barbi
 *
 */
public class ExecuteOperator extends Operator<Agent, PlanParameter, Void> {

	public static final String OPERATION_TYPE = "Execute";

	@Override
	public Pair<String, Class<?>> getOperationType() {
		return new Pair<String, Class<?>>(OPERATION_TYPE, ExecuteOperator.class);
	}

	@Override
	protected PlanParameter getEmptyParameter() {
		return new SimplePlanParameter();
	}

	@Override
	protected Void processImpl(PlanParameter param) {
		List<Subgoal> plans = param.getActualPlan().getPlans();

		if (!plans.isEmpty()) {
			if (!sweep(plans.get(0), param)) {
				param.report("agent has decided to wait");
			}
		} else {
			param.report("no plans executable");
		}

		return null;
	}

	/**
	 * search in chronological order for the first Intention, that is and Action
	 * and execute it
	 */
	protected boolean sweep(Intention intention, PlanParameter param) {
		if (intention instanceof Action) {
			param.getAgent().performAction((Action) intention);
			return true;
		} else if (intention instanceof Subgoal) {
			Subgoal sub = (Subgoal) intention;

			for (int i = 0; i < sub.getNumberOfStacks(); i++) {
				for (PlanElement elem : sub.getStack(i)) {
					if (sweep(elem.getIntention(), param)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	protected Void defaultReturnValue() {
		return null;
	}

}
