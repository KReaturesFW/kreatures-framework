package com.github.kreatures.app;

import java.io.File;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.github.kreatures.gui.KReaturesGUIDataStorage;
import com.github.kreatures.gui.KReaturesWindow;
import com.github.kreatures.core.serialize.SimulationConfiguration;
import com.github.kreatures.core.util.Utility;
public class GUIApplication {
	/** logging facility */
//	private static Logger LOG = LoggerFactory.getLogger(KReaturesWindow.class);
	
	public static void main(String[] args) throws Exception {	

		KReaturesWindow.get().init();
		
		// load simulation per commandline.
		for(int k=0; k<args.length; ++k) {
			if(args[k].equalsIgnoreCase("-presentation")) {
				Utility.presentationMode = true;
			}
			
			String [] ary = args[k].split("=");
			if(ary.length == 2 && ary[0].equalsIgnoreCase("simulation")) {
				SimulationConfiguration config = SimulationConfiguration.loadXml(new File(ary[1]));
				KReaturesGUIDataStorage.get().getSimulationControl().setSimulation(config);
			}
		}
	}

}
