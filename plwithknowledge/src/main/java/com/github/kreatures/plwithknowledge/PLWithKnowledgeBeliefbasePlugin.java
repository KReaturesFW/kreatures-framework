package com.github.kreatures.plwithknowledge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import com.github.kreatures.core.KReaturesPluginAdapter;
import com.github.kreatures.core.BaseBeliefbase;
import com.github.kreatures.gui.UIPlugin;
import com.github.kreatures.gui.base.ViewComponent;
import com.github.kreatures.core.logic.BaseChangeBeliefs;
import com.github.kreatures.core.logic.BaseReasoner;
import com.github.kreatures.core.logic.BaseTranslator;
import com.github.kreatures.plwithknowledge.logic.PLWithKnowledgeBeliefbase;
import com.github.kreatures.plwithknowledge.logic.PLWithKnowledgeReasoner;
import com.github.kreatures.plwithknowledge.logic.PLWithKnowledgeTranslator;
import com.github.kreatures.plwithknowledge.logic.PLWithKnowledgeUpdate;

@PluginImplementation
public class PLWithKnowledgeBeliefbasePlugin extends KReaturesPluginAdapter implements UIPlugin {
	@Override
	public List<Class<? extends BaseBeliefbase>> getBeliefbaseImpl() {
		List<Class<? extends BaseBeliefbase>> reval = new LinkedList<Class<? extends BaseBeliefbase>>();
		reval.add(PLWithKnowledgeBeliefbase.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseReasoner>> getReasonerImpl() {
		List<Class<? extends BaseReasoner>> reval = new LinkedList<Class<? extends BaseReasoner>>();
		reval.add(PLWithKnowledgeReasoner.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseChangeBeliefs>> getChangeImpl() {
		List<Class<? extends BaseChangeBeliefs>> reval = new LinkedList<Class<? extends BaseChangeBeliefs>>();
		reval.add(PLWithKnowledgeUpdate.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseTranslator>> getTranslatorImpl() {
		List<Class<? extends BaseTranslator>> reval = new LinkedList<Class<? extends BaseTranslator>>();
		reval.add(PLWithKnowledgeTranslator.class);
		return reval;
	}	
	
	public Map<String, Class<? extends ViewComponent>> getUIComponents() {
		Map<String, Class<? extends ViewComponent>> reval = new HashMap<String, Class<? extends ViewComponent>>();
		return reval;
	}

	
}
