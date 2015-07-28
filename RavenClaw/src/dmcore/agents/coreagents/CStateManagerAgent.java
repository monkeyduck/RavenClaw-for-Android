package dmcore.agents.coreagents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import utils.Const;
import utils.Utils;

import dmcore.agents.mytypedef.TFloorStatus;
import dmcore.agents.mytypedef.AgentFactory;

import android.util.Log;


//D: structure describing the state of the dialog manager at some point
class TDialogState {
	TFloorStatus fsFloorStatus;			// who has the floor?
	String sFocusedAgentName;			// the name of the focused agent
	ArrayList<TExecutionStackItem> esExecutionStack = 
			new ArrayList<TExecutionStackItem>();	// the execution stack
	TExpectationAgenda eaAgenda;		// the expectation agenda
	TSystemAction saSystemAction;		// the current system action
	String sInputLineConfiguration;		// String representation of the input
										// line config at this state (lm, etc)
	int iTurnNumber;					// the current turn number
	int iEHIndex;						// the execution history index matching 
										// this dialog state
	String sStateName;					// the name of the current dialog state
} 


public class CStateManagerAgent extends CAgent implements AgentFactory{
	// dialog state name definitions
	public HashMap<String, String> s2sDialogStateNames = new HashMap<String, String>();

	// private vector containing a history of the states that the DM went 
	// through
	public ArrayList<TDialogState> vStateHistory = new ArrayList<TDialogState>();

    // variable containing the state broadcast address
    public String sStateBroadcastAddress;
    
	  //-----------------------------------------------------------------------------
	 // Constructors and Destructors
	 //-----------------------------------------------------------------------------
	
	 // D: Default constructor
	 public CStateManagerAgent(String sAName,String sAConfiguration,
	 									   String sAType){
	 	super(sAName, sAConfiguration, sAType);
	 	// nothing here	
	 }
	 public CStateManagerAgent(String sAName,String sAConfiguration){
		 super(sAName,sAConfiguration);
		 this.sType = "CAgent:CStateManagerAgent";
	 }
	
	 public CStateManagerAgent() {
		// TODO Auto-generated constructor stub
	}
	//-----------------------------------------------------------------------------
	 // Static function for dynamic agent creation
	 //-----------------------------------------------------------------------------
	 public CAgent AgentFactory(String sAName, String sAConfiguration) {
	 	return new CStateManagerAgent(sAName, sAConfiguration);
	 }
	
	
	 //-----------------------------------------------------------------------------
	 // CAgent class overwritten methods
	 //-----------------------------------------------------------------------------
	
	 // D: the overwritten Reset method
	 public void Reset() {
	 	vStateHistory.clear();
	 }
    
    // D: Updates the state information
    public void UpdateState() {
        // log the activity
    	Log.d(Const.STATEMANAGER_STREAM_TAG, "Updating dialog state ...");

    	// first, if necessary, assemble the agenda of concept expectations
    	if (DMCore.pDMCore.bAgendaModifiedFlag) {
    		DMCore.pDMCore.assembleExpectationAgenda();
    	}
     
        // construct the dialog state
        TDialogState dsDialogState = new TDialogState();
    	dsDialogState.fsFloorStatus = DMCore.pDMCore.fsFloorStatus;
        dsDialogState.sFocusedAgentName = DMCore.pDMCore.GetAgentInFocus().GetName();
        dsDialogState.esExecutionStack = 
        		(ArrayList<TExecutionStackItem>) DMCore.pDMCore.esExecutionStack.clone();
        dsDialogState.eaAgenda = DMCore.pDMCore.eaAgenda;
    	dsDialogState.saSystemAction = DMCore.pDMCore.saSystemAction;
        dsDialogState.iTurnNumber = DMCore.pDMCore.iTurnNumber;
    	dsDialogState.iEHIndex = DMCore.pDMCore.esExecutionStack.get(0).iEHIndex;

    	// compute the dialog state 
    	dsDialogState.sStateName = "";
    	if(s2sDialogStateNames.isEmpty()) { 
    		dsDialogState.sStateName = dsDialogState.sFocusedAgentName;
    	} else {
    		// go through the mapping and find something that matches the focus
    		Map.Entry<String,String> entry;
    		Iterator<Map.Entry<String,String>> iterator = 
    				s2sDialogStateNames.entrySet().iterator();
    		while(iterator.hasNext()) {
    			entry = iterator.next();
    			if(dsDialogState.sFocusedAgentName.contains(entry.getKey())) {
    				dsDialogState.sStateName = entry.getValue();
    				break;
    			}
    		}	
    		// if we couldn't find anything in the mapping, then set it to 
    		// _unknown_
    		if(dsDialogState.sStateName.equals("")) 
    			dsDialogState.sStateName = "_unknown_";
    	}

    	// adds the input line configuration as part of the state
    	HashMap<String,String> s2sInputLineConfiguration = 
    		DMCore.pDMCore.GetAgentInFocus().GetInputLineConfiguration();
    	dsDialogState.sInputLineConfiguration = 
    	    Utils.S2SHashToString(s2sInputLineConfiguration);

        // and push the state in history
        vStateHistory.add(dsDialogState);

    	TDialogState ds = GetLastState();

        // log the finish
    	int tmpsize = vStateHistory.size()-1;
    	Log.d(Const.STATEMANAGER_STREAM_TAG, "Dialog state update completed: "+
    			dsDialogState.sFocusedAgentName+" at "+tmpsize+
    			"(iEHIndex="+dsDialogState.iEHIndex+"):\n");
    	//GetStateAsString());
    }
    
    // D: Access the last state
    public TDialogState GetLastState() {
        return vStateHistory.get(vStateHistory.size()-1);
    }
}
