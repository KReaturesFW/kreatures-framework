package com.github.kreatures.example.logic;

import com.github.kreatures.core.BaseBeliefbase;
import com.github.kreatures.core.logic.BaseChangeBeliefs;
import com.github.kreatures.core.operators.parameter.ChangeBeliefbaseParameter;

public class ExampleExpansion extends BaseChangeBeliefs {

	@Override
	public Class<? extends BaseBeliefbase> getSupportedBeliefbase() {
		return ExampleBeliefbase.class;
	}

	@Override
	protected BaseBeliefbase processImpl(ChangeBeliefbaseParameter param) {
		ExampleBeliefbase bb = (ExampleBeliefbase) param.getSourceBeliefBase();
		ExampleBeliefbase nbb = (ExampleBeliefbase) param.getNewKnowledge();
		bb.fbs.addAll(nbb.fbs);
		
		return bb;
	}
}
