package dmcore.agents.coreagents;

import user.definition.UserDefinition;
import utils.Const;
import dmcore.agents.mytypedef.AgentFactory;
import dmcore.agents.mytypedef.FlightDatabaseHelper;
import dmcore.outputs.MyOutput;
import android.util.Log;

public class DMCore {
	//----------------------------------------------------------------------
	// Definitions for the dialog core agents
	//----------------------------------------------------------------------
	public static CDMCoreAgent				pDMCore ;
	public static COutputManagerAgent		pOutputManager;
	public static CInteractionEventManagerAgent	pInteractionEventManager;
	public static CStateManagerAgent		pStateManager;
	public static CDTTManagerAgent			pDTTManager;
	public static CGroundingManagerAgent  	pGroundingManager;
	public static MyOutput 					myoutput;
	public static FlightDatabaseHelper 		fdhDatabaseHelper;
	public static boolean bForceExit = false;
	static{
		AgentFactory afAgentFactory;
		Log.d(Const.CORETHREAD_STREAM, "Initializing Core ...");
	    
		// Initialize the random number gererator
		/*_timeb theTime = GetTime();
		srand((unsigned)(theTime.millitm + theTime.time*1000));*/

		// Create a new Dialog Management Core Agent, and register it
		CRegistry.AgentsRegistry.Clear();
		
		afAgentFactory = new CDMCoreAgent();
		CRegistry.AgentsRegistry.RegisterAgentType("CDMCoreAgent",afAgentFactory);
		pDMCore = (CDMCoreAgent)CRegistry.AgentsRegistry.CreateAgent("CDMCoreAgent", 
															"DMCoreAgent");
		if(pDMCore==null) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create DMCore agent.");
		pDMCore.Initialize();
		pDMCore.Register();
	    // set the default core configuration 
	    /*pDMCore.SetDefaultTimeoutPeriod(
	        atoi(rcpParams.Get(RCP_DEFAULT_TIMEOUT).c_str()));
	    pDMCore.SetDefaultNonunderstandingThreshold(
	        (float)atof(
	        rcpParams.Get(RCP_DEFAULT_NONUNDERSTANDING_THRESHOLD).c_str()));
	     */
		// create all the other dialog core agents
		Log.d(Const.CORETHREAD_STREAM, "Creating auxiliary core dialog core agents ...");

		// create the interaction event manager
		afAgentFactory = new CInteractionEventManagerAgent();
		CRegistry.AgentsRegistry.RegisterAgentType("CInteractionEventManagerAgent", 
										 afAgentFactory);
		pInteractionEventManager = (CInteractionEventManagerAgent)
						CRegistry.AgentsRegistry.CreateAgent("CInteractionEventManagerAgent", 
												   "InteractionEventManagerAgent");
		if(pInteractionEventManager==null) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create InteractionEventManager agent.");
		pInteractionEventManager.Initialize();
		pInteractionEventManager.Register();

		// create the output manager
		/*CRegistry.AgentsRegistry.RegisterAgentType("COutputManagerAgent", 
										 COutputManagerAgent::AgentFactory);
		pOutputManager = (COutputManagerAgent *)
						CRegistry.AgentsRegistry.CreateAgent("COutputManagerAgent", 
												   "OutputManagerAgent");
		if(!pOutputManager) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create OutputManager agent.");
		pOutputManager.Initialize();
		pOutputManager.Register();*/

		// create the galaxy stub
		/*CRegistry.AgentsRegistry.RegisterAgentType("CTrafficManagerAgent", 
										 CTrafficManagerAgent::AgentFactory);
		pTrafficManager = (CTrafficManagerAgent *)
						CRegistry.AgentsRegistry.CreateAgent("CTrafficManagerAgent", 
												   "TrafficManagerAgent");
		if(!pTrafficManager) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create TrafficManager agent.");
		pTrafficManager.Initialize();
		pTrafficManager.Register();*/

		// create the state manager
		afAgentFactory = new CStateManagerAgent();
		CRegistry.AgentsRegistry.RegisterAgentType("CStateManagerAgent", 
										 afAgentFactory);
		pStateManager = (CStateManagerAgent)
						CRegistry.AgentsRegistry.CreateAgent("CStateManagerAgent", 
												   "StateManagerAgent");
		if(pStateManager==null) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create StateManager agent.");
		pStateManager.Initialize();
		pStateManager.Register();
	    // set the state broadcast address
		/*pStateManager.LoadDialogStateNames(rcpParams.Get(RCP_DIALOG_STATES_FILE));*/

		// create the dialog task tree manager
		afAgentFactory = new CDTTManagerAgent();
		CRegistry.AgentsRegistry.RegisterAgentType("CDTTManagerAgent", 
										 afAgentFactory);
		pDTTManager = (CDTTManagerAgent)
						CRegistry.AgentsRegistry.CreateAgent("CDTTManagerAgent", 
												   "DTTManagerAgent");
		if(pDTTManager==null) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create DTTManager agent.");
		pDTTManager.Initialize();
		pDTTManager.Register();

		// create the grounding manager
		afAgentFactory = new CGroundingManagerAgent();
		CRegistry.AgentsRegistry.RegisterAgentType("CGroundingManagerAgent", 
										 afAgentFactory);
		pGroundingManager = (CGroundingManagerAgent)
						      CRegistry.AgentsRegistry.CreateAgent("CGroundingManagerAgent", 
												         "GroundingManagerAgent");
		if(pGroundingManager==null) 
			Log.e(Const.CORETHREAD_STREAM,"Could not create GroundingManager agent.");
		pGroundingManager.Initialize();
		pGroundingManager.Register();    
	    // set the configuration
	   /* pGroundingManager.SetConfiguration(
	        rcpParams.Get(RCP_GROUNDING_MANAGER_CONFIGURATION));
	    // and load the models specifications from the grounding policies file
		if (rcpParams.Get(RCP_GROUNDING_POLICIES) != "") 
			pGroundingManager.LoadPoliciesFromString(rcpParams.Get(RCP_GROUNDING_POLICIES));
		else
			pGroundingManager.LoadPoliciesFromFile(rcpParams.Get(RCP_GROUNDING_POLICIES_FILE));*/
		// Log the core initialization parameters
		
		// create MyOutput static member
		myoutput = new MyOutput();
		myoutput.SetParameter();
		Log.d(Const.CORETHREAD_STREAM,"Output has already been initialized");
		
		// Create Database helper member
		fdhDatabaseHelper = new FlightDatabaseHelper(MyOutput.getAppContext());
		
		// Write some data into the database
		fdhDatabaseHelper.WriteIntoDatabase();
		Log.d(Const.CORETHREAD_STREAM,"DataBaseHelp has been initialized");
		
		// Log the completion of initialization
	    Log.d(Const.CORETHREAD_STREAM, "Auxiliary core dialog management agents "+
							   "created successfully.");

		Log.d(Const.CORETHREAD_STREAM, "Core initialization completed successfully.");
	}
	
	// Call the dialog task initialize function 
	public static void DialogTaskOnBeginSession(){
		UserDefinition.DialogTaskOnBeginSession();
	}

	public static void SetNull() {
		// TODO Auto-generated method stub
		pDMCore = null;
		pOutputManager=null;
		pInteractionEventManager=null;
		pStateManager=null;
		pDTTManager=null;
		pGroundingManager=null;
		myoutput=null;
		fdhDatabaseHelper=null;
	}
}
