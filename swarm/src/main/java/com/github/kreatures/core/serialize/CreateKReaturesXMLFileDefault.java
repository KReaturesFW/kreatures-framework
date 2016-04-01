package com.github.kreatures.core.serialize;

import java.io.File;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.kreatures.core.serialize.BeliefbaseConfig;
import com.github.kreatures.core.serialize.SimulationConfiguration;
import com.github.kreatures.swarm.serialize.SwarmAgentTypeConfig;
import com.github.kreatures.swarm.serialize.SwarmConfigRead;
import com.github.kreatures.swarm.serialize.SwarmPerspectiveConfig;
import com.github.kreatures.core.KReaturesEnvironment;
import com.github.kreatures.core.KReaturesSimulationInfo; 

/**
 * 
 * @author donfack
 *
 */
public class CreateKReaturesXMLFileDefault implements CreateKReaturesXMLFile {
	/** logging facility */
	private static Logger LOG = LoggerFactory.getLogger(KReaturesEnvironment.class);
	
	private static Persister persister = new Persister();
	
	private static SwarmConfigRead swarmConfig=null;
	/**
	 * folder where the current simulation configuration's file is.
	 */
	private static String currentSimDirectory=null;
	/**
	 * file's name of the Abstract_Swarm's file corresponding to the current simulation without the extension '.xml'
	 */
	private static String currentSwarmFile;
	/**
	 * file's name of the loaded simulation's configuration without the extension '.xml'
	 */
	private static String fileNameLoaded;
	
	/**
	 *  folder where all simulation configuration's file are.
	 */
	private static final String kreaturesConfigSimDirectory="examples/";
	/**
	 * Folder where the Abstract_Swarm-Dateien are
	 */
	private static final String AbstractSwarmDirectory="config/swarm/abstract_swarm_file/";
	/**
	 * Folder where the default Kreatures files for Abstract_Swarm-Dateien are. 
	 */
	private static final String KReaturesSwarmDirectory="config/swarm/kreatures_default/";
	/**
	 * Folder where all Kreatures's configuration files are. 
	 */
	private static final String KReaturesConfigDirectory="config/";
	/**
	 * Folder where all Kreatures's configuration files for agents are. 
	 */
	private static final String KReaturesAgentDirectory="config/agents/";
	/**
	 * Folder where all Kreatures's configuration files for beliefbases are. 
	 */
	private static final String KReaturesBeliefbasesDirectory="config/beliefbases/";
	
	public CreateKReaturesXMLFileDefault() throws Exception{

		//this.testLoadSwarmXMLDatei(filepath); // nur für Test gegedacht, muss nachher gelöscht werden.
		loadSwarmXMLFile(); 
		/*
		 * We have to initialize the SwarmConfig's object to null, because there is no current simulation in this step.
		 */
		swarmConfig=null;
	}

	/**
	 * Load the XMLfile of Swarm into the object SwarmConfig. 
	 * @param filepaht is the way to the swarmXmlfile. 
	 * @return SwarmConfig, that is an object with all informations of swarm-scenario.
	 * @throws Exception
	 */

	private void loadSwarmXMLFile() throws Exception{
		//search all abstract_Swarm_config_files and creates the coresponding simulation file.
		for( File file : CreateKReaturesXMLFileDefault.searchSwarmConfigFile(AbstractSwarmDirectory)){
			fileNameLoaded=getFilename(file.getName());
			swarmConfig = persister.read(SwarmConfigRead.class, file);	 
			File fileKreatures=new File(kreaturesConfigSimDirectory+fileNameLoaded);			
			if(!fileKreatures.exists()){
				fileKreatures.mkdir();
			}		    	  
			fileKreatures=new File(kreaturesConfigSimDirectory+fileNameLoaded+"/"+fileNameLoaded+"_simulation.xml");
			if(!fileKreatures.exists()){
				persister.write(createSimulationConfig(), fileKreatures);
			}
			for( File kreaturesDefaultConfig : CreateKReaturesXMLFileDefault.searchSwarmConfigFile(KReaturesSwarmDirectory)){
				if(kreaturesDefaultConfig.getName().equals("_cycle.xml")){
					/*
					 * We create the commandSequence file for the new project, whose name is contented
					 * in the variable fileNameLoaded.
					 */
					fileKreatures=new File(KReaturesConfigDirectory+fileNameLoaded+"_cycle.xml");
					if(!fileKreatures.exists()){
						
						Files.copy(kreaturesDefaultConfig.toPath(),fileKreatures.toPath(),  REPLACE_EXISTING);
						//CommandSequence asmlConfigFile=persister.read(CommandSequence.class, kreaturesDefaultConfig);
						//persister.write(asmlConfigFile, fileKreatures);
					}
				}else if(kreaturesDefaultConfig.getName().equals("_agent.xml")){
					/*
					 * We create the agent's configuration file for the new project, whose name is contented
					 * in the variable fileNameLoaded.
					 */
					fileKreatures=new File(KReaturesAgentDirectory+fileNameLoaded+"_agent.xml");
					if(!fileKreatures.exists()){
						AgentConfigReal agentConfigFile=persister.read(AgentConfigReal.class, kreaturesDefaultConfig);
						agentConfigFile.name=fileNameLoaded+" Swarm";
						CommandSequenceSerializeImport asmlImport=new CommandSequenceSerializeImport();
						asmlImport.source=new File(KReaturesConfigDirectory+fileNameLoaded+"_cycle.xml");
						agentConfigFile.cylceScript=asmlImport;
						persister.write(agentConfigFile, fileKreatures);
					}
				}else if(kreaturesDefaultConfig.getName().equals("_beliefbase.xml")){
					/*
					 * We create the beliefbase's configuration file for the new project, whose name is contented
					 * in the variable fileNameLoaded.
					 */
					fileKreatures=new File(KReaturesBeliefbasesDirectory+"asp_"+fileNameLoaded+"_beliefbase.xml");
					if(!fileKreatures.exists()){
						BeliefbaseConfigReal beliefbaseConfigFile=persister.read(BeliefbaseConfigReal.class, kreaturesDefaultConfig);
						persister.write(beliefbaseConfigFile, fileKreatures);
					}
				}
			}
		}

	}
	/**
	 * @param filepath is a file's name with extension
	 * @return a file's name without extension.
	 */
	private static String getFilename(String filename){
		//TODO a Exeception has to be write
		if (filename==null)
			return null;
		String name=filename.substring(0, filename.lastIndexOf("."));
		return name;
	}



	//@Override
	private SimulationConfiguration createSimulationConfig() {

		SimulationConfiguration simulaConfig=new SimulationConfiguration() ;
		//List<SwarmPerspectiveConfig> swarmPerspectiveConfig=swarmConfig.getListPerspective() ;


		simulaConfig.name= fileNameLoaded; // the file's name is the simulation configuration's name.
		simulaConfig.category=fileNameLoaded+"OfSwarm";
		simulaConfig.behaviorCls="com.github.kreatures.swarm.basic.SwarmBehavior";

		//agentInstance.
		simulaConfig.agents=createAgentConfig();

		//simulaConfig.setName(swarmConfig.getName());
		return simulaConfig;
	}

	//@Override
	private  List<AgentInstance> createAgentConfig() {
		List<AgentInstance> listAgentInstance=new LinkedList<AgentInstance>();
		for(SwarmPerspectiveConfig perceptionSwarm: swarmConfig.getListPerspective()){
			for(SwarmAgentTypeConfig agentSwarm: perceptionSwarm.getListAgentType() ){
				AgentInstance agentInstance=new AgentInstance();
				agentInstance.name=agentSwarm.getNameSwarmAgentType();
				AgentConfigImport agentConfigImport=new AgentConfigImport();
				File sourceOfAgentConfig=new File("config/agents/"+fileNameLoaded+"_agent.xml");
				agentConfigImport.source=sourceOfAgentConfig;
				agentInstance.config= agentConfigImport;
				BeliefbaseConfigImport sourceOfBB=new BeliefbaseConfigImport();
				File sourceOfBBConfig=new File("config/beliefbases/asp_"+fileNameLoaded+"_beliefbase.xml");
				sourceOfBB.source=sourceOfBBConfig;
				agentInstance.beliefbaseConfig=sourceOfBB;
				listAgentInstance.add(agentInstance);
			}
		}
		return listAgentInstance;
	}

	//@Override
	public BeliefbaseConfig createBeliefbaseConfigReal() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Here, a instance of the SwarmConfig's classes will be create.
	 * @return all SwarmConfiguration of current simulation. 
	 * This comes from the Abstract_Swarm file and are using to initialize the environment. 
	 */
	private static SwarmConfigRead createSwarmConfigRead(){
		
		//get current simulation's directory. 	
		if (KReaturesSimulationInfo.getName()==null){
			LOG.error("The Simulation has to be initialized, sees KReaturesEnvironment Class to known why that is no the case.");
			return null;
		}
	    currentSwarmFile=KReaturesSimulationInfo.getName();
	    currentSimDirectory=KReaturesSimulationInfo.getSimDirectory();
	    
		File file =new File(AbstractSwarmDirectory+currentSwarmFile+".xml");
		try {
			persister.write(file, System.out);
			System.out.println("\n"+currentSwarmFile);
			swarmConfig = persister.read(SwarmConfigRead.class, file);
		} catch (Exception e) {
			LOG.error("The Abstract_Swarm's file doesn't exist. Check the giving directory.");
			e.printStackTrace();
		}
		return swarmConfig;
	}
	/**
	 * folder where the current simulation configuration's file is.
	 */
	public static String getCurrentSimDirectory() {
		return currentSimDirectory;
	}
	/**
	 * file's name of the Abstract_Swarm's file corresponding to the current simulation without the extension '.xml'
	 */
	public static String getCurrentSwarmFile() {
		return currentSwarmFile;
	}
	
	/**
	 * When the instance would be created, either it no exists or a other Abstract_swarm's simulation have to be loaded.
	 * @return this instance is a singleton, therefore we have to return the already created instance. 
	 */
	
	public static SwarmConfigRead getSwarmConfig() {
		if(swarmConfig==null){
			swarmConfig=createSwarmConfigRead();
		}else if (is_currentSimChange()){
			swarmConfig=createSwarmConfigRead();
		}
		return swarmConfig;
	}
	/**
	 * This method is use to check either the current simulation has been changed or not.
	 * @return true when current simulation changing or false otherwise.
	 */
	public static boolean is_currentSimChange(){
		//return !currentSwarmFile.equals(KReaturesSimulationInfo.getName());
		return KReaturesSimulationInfo.is_currentSimChange();
	}

	/**
	 * 
	 * @param dirName folder where the Abstract_Swarm-Dateien are located.
	 * @return List of all files which are into the abstract_Swarm folder.
	 */
	private static File[] searchSwarmConfigFile( String dirName){
		File dir = new File(dirName);

		return dir.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename)
			{ return filename.endsWith(".xml"); }
		} );

	}
	
	

}