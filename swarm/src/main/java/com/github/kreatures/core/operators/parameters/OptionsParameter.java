package com.github.kreatures.core.operators.parameters;

import javax.management.AttributeNotFoundException;

import com.github.kreatures.core.Agent;
import com.github.kreatures.core.error.ConversionException;
import com.github.kreatures.core.operators.parameter.GenericOperatorParameter;
import com.github.kreatures.core.operators.parameter.OperatorPluginParameter;

public class OptionsParameter extends OperatorPluginParameter {

	public OptionsParameter() {
		// TODO Auto-generated constructor stub
	}

	public OptionsParameter(Agent caller) {
		super(caller);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void fromGenericParameter(GenericOperatorParameter param) 
			throws ConversionException, AttributeNotFoundException {
		super.fromGenericParameter(param);
		
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated constructor stub
		return false;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated constructor stub
		return 0;
	}
}
