package dmcore.agents.dialogagents;

import android.util.Log;
import dmcore.agents.coreagents.CAgent;
import dmcore.agents.coreagents.CRegistry;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;

public class CDialogAgency extends CDialogAgent{
	//-----------------------------------------------------------------------------
	// CDialogAgency class: this class implements a dialog agency
	//-----------------------------------------------------------------------------

	// D: defines for the execution policies

	// the left-to-right-open execution policy: the agency attempts to execute the
	// subagents in a left-to-right order. Preconditions and non-blocked conditions
	// are checked for each agent. This is the DEFAULT policy
	public static final String LEFT_TO_RIGHT_OPEN ="left_to_right_open";

	// the left-to-right-forced execution policy: the agency executes the agents
	// in strict order from left to right; it blocks all the agents that are not
	// currently under execution, and it doesn't check for any preconditions
	public static final String LEFT_TO_RIGHT_ENFORCED= "left_to_right_enforced";
	
	// Log tag
	public static final String DIALOGAGENCY_TAG = "DialogAgency";
	//-----------------------------------------------------------------------------
	//
	// Constructors and destructors
	//
	//-----------------------------------------------------------------------------
	// D: default constructor
	public CDialogAgency(String sAName, 
								 String sAConfiguration, 
								 String sAType){
		super(sAName, sAConfiguration, sAType);
		// does nothing
	};

	public CDialogAgency(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDialogAgent:CDialogAgency";
	}

	public CDialogAgency(String sAName) {
		// TODO Auto-generated constructor stub
		super(sAName);
	}
	public CDialogAgency(){
		
	}

	//-----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CDialogAgency(sAName, sAConfiguration);
	}

	//-----------------------------------------------------------------------------
	//
	// CAgent specific overwritten methods
	//
	//-----------------------------------------------------------------------------
	// D: Create:
	public void Create() {
		// creates the concepts
		CreateConcepts();
		// creates the subagents
		CreateSubAgents();
		// finally calls the OnCreation method
		OnCreation();
	}

	// D: Initialize: 
	public void Initialize() {
	    // calls the OnInitialization method
	    OnInitialization();
	}

	//-----------------------------------------------------------------------------
	//
	// CDialogAgent specific overwritten methods
	//
	//-----------------------------------------------------------------------------

	// D: create the subagents: does nothing, is to be overwritten by derived 
//	    classes
	public void CreateSubAgents() {
	}

	// D: the Execute routine: plans the execution of subagents, 
	public TDialogExecuteReturnCode Execute() {
		
	    // plan the next agent we just determined according to execution policy
		CDialogAgent pDialogAgent = NextAgentToExecute();
		if(pDialogAgent != null) {
			DMCore.pDMCore.ContinueWith(this, pDialogAgent);
	        // increment the execute counter
	        IncrementExecuteCounter();
			// and continue execution
			return TDialogExecuteReturnCode.dercContinueExecution;
		} else {
			// o/w it means there's no more agents we can execute, so trigger 
			// a fatal error *** actually maybe we want a "failed" on agencies, 
			// or do we ?
			Log.e(DIALOGAGENCY_TAG,"Agency " + GetName() + " failed to complete.");
			return TDialogExecuteReturnCode.dercFinishDialogAndCloseSession;
		}
	}

	// D: returns a pointer to the subagent which should be executed 
//	    next, according to the current execution policy
	public CDialogAgent NextAgentToExecute() {
		// figure out which is the next subagent to execute, according to 
		// the current execution policy
		String sCurrentExecutionPolicy = GetExecutionPolicy();
		
		if(sCurrentExecutionPolicy == LEFT_TO_RIGHT_OPEN) {
			// for the left-to-right-open execution policy, 
			// go from the first subagent to the last, and find the one which 
			// has not completed yet and has preconditions satisfied and is not
			// blocked; program it for execution

			for(int i=0; i < SubAgents.size(); i++) 
				if(!SubAgents.get(i).HasCompleted() && 
				   SubAgents.get(i).PreconditionsSatisfied() &&
				   !SubAgents.get(i).IsBlocked()) {
					// plan that one for execution
					return SubAgents.get(i);
				}

			// o/w go through all agents again and describe why they didn't execute
		    String sWarning = "Failure information for Agency " + GetName() + "\n";
			for(int i=0; i < SubAgents.size(); i++) 
				sWarning+="Agent " + SubAgents.get(i).GetName() + " has " + 
					(SubAgents.get(i).HasCompleted()?"":"not ") + "completed, does " + 
					(SubAgents.get(i).PreconditionsSatisfied()?"":"not ") + 
					"have preconditions satisfied, and is " + 
					(SubAgents.get(i).IsBlocked()?"":"not ") + "blocked.\n";
			Log.w(DIALOGAGENCY_TAG,sWarning);
			// finally return null
			return null;
		}
		
		else if(sCurrentExecutionPolicy == LEFT_TO_RIGHT_ENFORCED) {
			// for the left-to-right-enforced execution policy, 
			// go from the first subagent to the last, and find the first 
			// uncompleted one. Block all the others, and plan this one
			// for execution

			int i=0; 
			while((i < SubAgents.size()) && (SubAgents.get(i).HasCompleted())) {
				SubAgents.get(i).Block();
				i++;
			}

			// there should be at least an incompleted agent
			if(i == SubAgents.size()) {
				Log.e(DIALOGAGENCY_TAG,"All agents are completed in NextAgentToExecute for " + 
						   sName + " agency (with left-to-right-enforced policy).");
			}
					       
			// unblock and remember this one
			SubAgents.get(i).UnBlock();
			int iIndexNextAgent = i;

			// block all the other ones 
	        for(i++; i < SubAgents.size(); i++) 
				SubAgents.get(i).Block();

			//
			return SubAgents.get(iIndexNextAgent);
		}

		// no other policy is known, so return null
		return null;
	}


	// D: the GetExecutionPolicy method: by default, agencies have a left-to-right-open 
	//	    execution policy
	public String GetExecutionPolicy() {
		return LEFT_TO_RIGHT_OPEN;
	}
	//-----------------------------------------------------------------------------
	//
	// Change the macro define
	//
	//-----------------------------------------------------------------------------
	// D: macros for defining a subagent within the subagent definition section
	public void SUBAGENT(String SubAgentName,String SubAgentType,String GroundingModelSpec){
		CDialogAgent pNewAgent = new CDialogAgent();
		pNewAgent = (CDialogAgent)
                CRegistry.AgentsRegistry.CreateAgent(SubAgentType, 
                                           SubAgentName);
		pNewAgent.SetParent(this);
	    pNewAgent.CreateGroundingModel(GroundingModelSpec);
		SubAgents.add(pNewAgent);
		pNewAgent.Initialize();	
	}
	// D: macro for defining a concept within the concept definition section
	public void USER_CONCEPT(String ConceptName,ConceptFactory ConceptType, String GroundingModelSpec){
	    Concepts.add(ConceptType.CreateConcept(ConceptName, TConceptSource.csUser));
	    Concepts.get(Concepts.size()-1).CreateGroundingModel(GroundingModelSpec);
	    Concepts.get(Concepts.size()-1).SetOwnerDialogAgent(this);
	}
	public void SYSTEM_CONCEPT(String ConceptName,ConceptFactory ConceptType){
	    Concepts.add(ConceptType.CreateConcept(ConceptName, TConceptSource.csSystem));
		Concepts.get(Concepts.size()-1).CreateGroundingModel("none");
	    Concepts.get(Concepts.size()-1).SetOwnerDialogAgent(this);	
	}
}
