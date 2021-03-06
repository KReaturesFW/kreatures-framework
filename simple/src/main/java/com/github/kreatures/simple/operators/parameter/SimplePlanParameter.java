package com.github.kreatures.simple.operators.parameter;

import javax.management.AttributeNotFoundException;

import com.github.kreatures.core.PlanComponent;
import com.github.kreatures.secrecy.operators.parameter.PlanParameter;
import com.github.kreatures.core.error.ConversionException;
import com.github.kreatures.core.operators.parameter.GenericOperatorParameter;

/**
 * 
 * @author Manuel Barbi
 *
 */
public class SimplePlanParameter extends PlanParameter {

	@Override
	public PlanComponent getActualPlan() {
		return getAgent().getComponent(PlanComponent.class);
	}

	@Override
	public void fromGenericParameter(GenericOperatorParameter gop) throws ConversionException, AttributeNotFoundException {
		try {
			super.fromGenericParameter(gop);
		} catch (ConversionException e) {
			// this is an unpleasant workaround
		}
	}

}
