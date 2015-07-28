package dmcore.agents.dialogagents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import android.annotation.SuppressLint;
import android.util.Log;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.coreagents.CRegistry;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.TAddSubAgentMethod;
import dmcore.agents.mytypedef.TBindMethod;
import dmcore.agents.mytypedef.TConceptExpectation;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;
import dmcore.agents.mytypedef.TFocusClaim;
import dmcore.concepts.CBoolConcept;
import dmcore.concepts.CConcept;
import dmcore.grounding.groundingmodel.CGroundingModel;

//D: definition for completion types
enum TCompletionType{
	ctSuccess,           // successful completion
    ctFailed,            // completion by failure
}
 

public class CDialogAgent extends CAgent{
	//---------------------------------------------------------------------
	// Members: Sub-agents, concepts, parent-link. various status 
	//          information
	//---------------------------------------------------------------------

	// the "small" name of the dialog agent. sName (inherited) will hold
	// the full path to the agent in the tree. Do not use sName directly, 
	// use GetName instead since the name of an agent can change as it 
	// becomes part of a larger tree of agents
	protected String sDialogAgentName="";	

	// the list of concepts that this dialog agent holds
	protected ArrayList<CConcept> Concepts = new ArrayList<CConcept>();

	// the list of subagents
	protected ArrayList<CDialogAgent> SubAgents = 
			new ArrayList<CDialogAgent>();

	// pointer to the parent dialog agent
	protected CDialogAgent pdaParent;

	// pointer to the dialog agent which represents the context
	// of this agent in the task tree (mostly for agents that are
	// attached to the tree like grounding agents)
	protected CDialogAgent pdaContextAgent;

	// pointer to the grounding model
	protected CGroundingModel pGroundingModel;

	// indicates if the agent has completed or not
	protected boolean bCompleted;

    // indicates how the agent completed
	protected TCompletionType ctCompletionType;

	// indicates if this agent is blocked or not
	protected boolean bBlocked;

    // a boolean indicated if the agent was added to the tree at runtime
	protected boolean bDynamicAgent;

    // a dynamic id for the agent (for dynamically generated agents)
	protected String sDynamicAgentID;

    // holds the grammar mapping for the commands that trigger then agent
	protected String sTriggeredByCommands;

    // holds the grounding model spec to be used for the commands that 
    // trigger the agent
	protected String sTriggerCommandsGroundingModelSpec;

    // indicates how many times the agent was attempted since the last 
    // reset/reopen
	protected int iExecuteCounter;

    // indicates how many times the agent was reset so far
	protected int iResetCounter;

    // indicates how many times the agent was reopened since the last reset
	protected int iReOpenCounter;

    // indicates for how many turns the agent was in focus since the 
    // last reset/reopen
	protected int iTurnsInFocusCounter;

    // holds an index (for the input manager) to the last input for this
    // agent
	protected int iLastInputIndex;
	protected int iLastExecutionInputIndex;

	// holds an index (for the output manager) to the last execution of this
	// agent
	protected int iLastExecutionIndex;

	// holds an index (for the core agent) to the last binding results for
    // this agent (both for the last event and the last user turn)
	protected int iLastBindingsIndex;

	// J: hash of configuration slot/values for input line
	protected HashMap<String, String> s2sInputLineConfiguration=new HashMap<String, String>();

	// J: indicates whether parent's input line configuration has been inherited
	protected boolean bInheritedParentInputConfiguration;
	
	// LILINCHUAN:indicates whether output query has been completed
	// default value is true because some agents do not need to output
	// in Execute method of the specific output agents turn its value to false 
	public boolean bOutputCompleted = true;
	//-----------------------------------------------------------------------------
	//
	// Constructors and destructors
	//
	//-----------------------------------------------------------------------------
	// D: Default constructor
	public CDialogAgent(String sAName, String sAConfiguration, String sAType){
		super(sAName, sAConfiguration, sAType);
		// initialize the class members: concepts, subagents, parent, etc
		sDialogAgentName = sAName;
		pdaParent = null;
		pdaContextAgent = null;
	    pGroundingModel = null; 
		bCompleted = false;
	    ctCompletionType = TCompletionType.ctFailed;
		bBlocked = false;
	    bDynamicAgent = false;
	    sDynamicAgentID = "";
	    sTriggeredByCommands = "";
	    sTriggerCommandsGroundingModelSpec = "";
	    iExecuteCounter = 0;
	    iResetCounter = 0;
	    iReOpenCounter = 0;
	    iTurnsInFocusCounter = 0;
	    iLastInputIndex = -1;
	    iLastExecutionIndex = -1;
	    iLastBindingsIndex = -1;
		bInheritedParentInputConfiguration = false;
	}
	public CDialogAgent() {
		// TODO Auto-generated constructor stub
	}
	public CDialogAgent(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName, sAConfiguration);
		this.sType = "CAgent:CDialogAgent";
		// initialize the class members: concepts, subagents, parent, etc
		sDialogAgentName = sAName;
		pdaParent = null;
		pdaContextAgent = null;
	    pGroundingModel = null; 
		bCompleted = false;
	    ctCompletionType = TCompletionType.ctFailed;
		bBlocked = false;
	    bDynamicAgent = false;
	    sDynamicAgentID = "";
	    sTriggeredByCommands = "";
	    sTriggerCommandsGroundingModelSpec = "";
	    iExecuteCounter = 0;
	    iResetCounter = 0;
	    iReOpenCounter = 0;
	    iTurnsInFocusCounter = 0;
	    iLastInputIndex = -1;
	    iLastExecutionIndex = -1;
	    iLastBindingsIndex = -1;
		bInheritedParentInputConfiguration = false;
	}
	public CDialogAgent(String sAName) {
		// TODO Auto-generated constructor stub
		super(sAName);
		this.sType = "CAgent:CDialogAgent";
		// initialize the class members: concepts, subagents, parent, etc
		sDialogAgentName = sAName;
		pdaParent = null;
		pdaContextAgent = null;
	    pGroundingModel = null; 
		bCompleted = false;
	    ctCompletionType = TCompletionType.ctFailed;
		bBlocked = false;
	    bDynamicAgent = false;
	    sDynamicAgentID = "";
	    sTriggeredByCommands = "";
	    sTriggerCommandsGroundingModelSpec = "";
	    iExecuteCounter = 0;
	    iResetCounter = 0;
	    iReOpenCounter = 0;
	    iTurnsInFocusCounter = 0;
	    iLastInputIndex = -1;
	    iLastExecutionIndex = -1;
	    iLastBindingsIndex = -1;
		bInheritedParentInputConfiguration = false;
	}
	//-----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		// this method should never end up being called (since CDialogAgent is an 
		// abstract class) , therefore do a fatal error if this happens
		//Log.e(Const.DIALOGTASK_STREAM_TAG,"AgentFactory called on CDialogAgent (abstract) class.");
		return new CDialogAgent(sAName, sAConfiguration);
	}
	
	//-----------------------------------------------------------------------------
	//
	// CAgent overwritten methods
	//
	//-----------------------------------------------------------------------------

	// D: Registers the agent - for a dialog agent, we are registering using the
	//	    full path in the dialog tree, and we are registering the children
	public void Register() {
		// register this agent
		CRegistry.AgentsRegistry.RegisterAgent(sName, this);
		// and all its subagents
	    for(int i=0; i < SubAgents.size(); i++)
	        SubAgents.get(i).Register();
	    // finally, create the trigger concept
		CreateTriggerConcept();
	}

	// D: Initializes the agent, gets called after creation
	public void Create() {
		// creates the concepts
		CreateConcepts();
		// calls OnCreation
		OnCreation();
	}

	// D: Initializes the agent, gets called after construction
	public void Initialize() {	
	    // calls OnInitialization
	    OnInitialization();
	}
	// D: the OnInitialization method: by default, nothing happens upon 
	//  initialization
	public void OnInitialization() {
	}
	
	//D: the OnCompletion method: by default, nothing happens upon completion
	public void OnCompletion() {
	}
	
	//D: the ReOpen method: by default, ReOpen calls upon ReOpenConcepts
	//  and ReOpenTopic
	public void ReOpen() {
	  ReOpenConcepts();
	  ReOpenTopic();
	}
	
	//D: the ReOpenConcepts method: by default, ReOpenConcepts calls ReOpen on
	//  all the concepts held by that agent (and its children)
	public void ReOpenConcepts() {
	  // call ReOpen on all the concepts
	  for(int i = 0; i < Concepts.size(); i++)
	      Concepts.get(i).ReOpen();
	  // call ReOpenConcepts on all the subagents
		for(int i = 0; i < SubAgents.size(); i++)
			SubAgents.get(i).ReOpenConcepts();
	}
	
	//D: the ReOpenTopic method
	public void ReOpenTopic() {
	  // set completion to false
	  bCompleted = false;
	  ctCompletionType = TCompletionType.ctFailed;
	  // unblock the agent
		bBlocked = false;
	  // reset the counters
		iExecuteCounter = 0;
	  iReOpenCounter++;
	  iTurnsInFocusCounter = 0;
		// call ReOpenTopic on all the subagents
		for(int i = 0; i < SubAgents.size(); i++)
			SubAgents.get(i).ReOpenTopic();
	  // call the OnInitialization method
	  OnInitialization();
	}
	
	//D: the IsAMainTopic: implicitly the agent is not a main topic
	public boolean IsAMainTopic() {
		return false;
	}
	
	//D: the prompt: by default nothing
	public String Prompt() {
		return "";
	}
	
	//D: the timeout prompt: by default returns the same thing as the prompt
	//  but adds version=timeout
	public String TimeoutPrompt() {
	  String sPrompt = Prompt().trim();
	  // check that the prompt is not empty
	  if(sPrompt.equals("")) 
	      return "";
	
	  String sTimeoutPrompt = "";
	
	  // check if we are dealing with a composed prompt
	  if(sPrompt.charAt(0) == '{') {
	      // then insert timeout in each of them
	      do {
	          // find the closing bracket
	          int iClosingPos = Utils.FindClosingQuoteChar(sPrompt, 1, '{', '}');
	          // add to the timeout prompt
	          sTimeoutPrompt += sPrompt.substring(0, iClosingPos - 1);
	          sTimeoutPrompt += " version=timeout}";
	          // check if we're done
	          if(iClosingPos >= (int)sPrompt.length()) break;
	          sPrompt = sPrompt.substring(iClosingPos, sPrompt.length());
	      } while(sPrompt != "");
	
	  } else {
	      // o/w the prompt is simple, so just add version=timeout at 
	      // the end
	      sTimeoutPrompt = sPrompt + " version=timeout";
	  }
	
	  return sTimeoutPrompt;
	}
	
	//D: the explain more prompt: by default adds a version=explain_more to 
	//  the default prompt
	public String ExplainMorePrompt() {
	  
	  String sPrompt = Prompt().trim();
	  // check that the prompt is not empty
	  if(sPrompt.equals("")) 
	      return "";
	
	  String sExplainMorePrompt="";
	
	  // check if we are dealing with a composed prompt
	  if(sPrompt.charAt(0) == '{') {
	      // then insert explain more in each of them
	      do {
	          // find the closing bracket
	          int iClosingPos = Utils.FindClosingQuoteChar(sPrompt, 1, '{', '}');
	          // add to the explain-more prompt
	          sExplainMorePrompt += sPrompt.substring(0, iClosingPos - 1);
	          sExplainMorePrompt += " version=explain_more}";
	          // check if we're done
	          if(iClosingPos >= (int)sPrompt.length()) break;
	          sPrompt = sPrompt.substring(iClosingPos,sPrompt.length() );
	      } while(sPrompt != "");
	  } else {
	      // o/w the prompt is simple, so just add version=explain_more at 
	      // the end
	      sExplainMorePrompt = sPrompt + " version=explain_more";
	  }
	
	  return sExplainMorePrompt;
	}
	
	//D: this function creates a versioned prompt
	public String CreateVersionedPrompt(String sVersion) {
	  String sPrompt = Prompt().trim();
	  // check that the prompt is not empty
	  if(sPrompt.equals(""))
	      return "";
	
	  // the versioned prompt
	  String sVersionedPrompt="";
	
	  // check if we are dealing with a composed prompt
	  if(sPrompt.charAt(0) == '{') {
	      // then insert the version in each of them
	      do {
	          // find the closing bracket
	          int iClosingPos = Utils.FindClosingQuoteChar(sPrompt, 1, '{', '}');
	          // add to the timeout prompt
	          sVersionedPrompt += sPrompt.substring(0, iClosingPos - 1);
	          sVersionedPrompt += " version=" + sVersion + "}";
	          // check if we're done
	          if(iClosingPos >= (int)sPrompt.length()) break;
	          sPrompt = sPrompt.substring(iClosingPos,sPrompt.length());
	      } while(sPrompt != "");
	
	  } else {
	      // o/w the prompt is simple, so just add version=timeout at 
	      // the end
	      sVersionedPrompt = sPrompt + " version=" + sVersion;
	  }
	
	  return sVersionedPrompt;
	}
	
	//D: the establish_context prompt: by default calls upon the parent agent, 
	//  if there is one
	public String EstablishContextPrompt() {
		if(pdaContextAgent!=null && (pdaContextAgent != this))
			return pdaContextAgent.EstablishContextPrompt();
		else if(pdaParent != null)
			return pdaParent.EstablishContextPrompt();
		else
			return "";
	}
	
	//D: the "what can i say" prompt: by default returns the default prompt
	//  but adds version=what_can_i_say
	public String WhatCanISayPrompt() {
	  String sPrompt = Prompt().trim();
	  // check that the prompt is not empty
	  if(sPrompt.equals("") )
	      return "";
	
	  String sWhatCanISayPrompt="";
	
	  // check if we are dealing with a composed prompt
	  if(sPrompt.charAt(0) == '{') {
	      // then insert what can i say in each of them
	      do {
	          // find the closing bracket
	          int iClosingPos = Utils.FindClosingQuoteChar(sPrompt, 1, '{', '}');
	          // add to the what_can_i_say prompt
	          sWhatCanISayPrompt += sPrompt.substring(0, iClosingPos - 1);
	          sWhatCanISayPrompt += " version=what_can_i_say}";
	          // check if we're done
	          if(iClosingPos >= (int)sPrompt.length()) break;
	          sPrompt = sPrompt.substring(iClosingPos, sPrompt.length());
	      } while(sPrompt != "");
	  } else {
	      // o/w the prompt is simple, so just add version=what_can_i_say at 
	      // the end
	      sWhatCanISayPrompt = sPrompt + " version=what_can_i_say";
	  }
	
	  return sWhatCanISayPrompt;
	}
	
	//D: Virtual function which specifies if this is a task agent or not
	//  (by default all agents are task agents)
	public boolean IsDTSAgent() {
	  return true;
	}
	
	//A: Virtual function which specifies if the execution of this agent
	//  has to be synchronized with the actual flow of the conversation
	//  or if it can be anticipated (i.e. whether this execution yields
	//  side effects for the conversation)
	public boolean IsConversationSynchronous() {
	  return false;
	}
	
	//A: Virtual function which specifies if this agent requires the 
	//  floor for its execution (by default, agents do not)
	public boolean RequiresFloor() {
	  return false;
	}
	// D: resets the agent completion status
	public void ResetCompleted() {
	    bCompleted = false;
	    ctCompletionType = TCompletionType.ctFailed;
	}
	
	//Virtual function used to cancel the effects of an agent which 
	//was executed within the DM but not realized (i.e. there was an
	//interruption of the normal flow of the dialogue due to a user
	//barge-in or another external event)
	//By default: decrement execution counter and set to incomplete
	public void Undo() {
		iExecuteCounter--;
		ResetCompleted();
	}

	// D: Reset triggers an initialize
	public void Reset() {
		// clears all the concepts
		for(int i = 0; i < Concepts.size(); i++) 
			Concepts.get(i).Clear();
		// and calls reset on all the subagents
		for(int i = 0; i < SubAgents.size(); i++)
			SubAgents.get(i).Reset();
	    // reset the other member variables
		bCompleted = false;
	    ctCompletionType = TCompletionType.ctFailed;
		bBlocked = false;
		iExecuteCounter = 0;
	    iReOpenCounter = 0;
	    iResetCounter++;
	    iTurnsInFocusCounter = 0;
	    iLastInputIndex = -1;
	    iLastExecutionIndex = -1;
	    iLastBindingsIndex = -1;
	    // finally, call the OnInitialization
	    OnInitialization();
	}
	// D: the Execute: for this class, it merely returns continue execution
	public TDialogExecuteReturnCode Execute() {
	    // increment the execute counter
	    IncrementExecuteCounter();

		return TDialogExecuteReturnCode.dercContinueExecution;
	}
	// J: Start copy from Agent.cpp and renamed s2sConfiguration . s2sInputLineConfiguration
	// A: Parses a configuration String into a hash of parameters
	public void SetInputConfiguration( String sConfiguration) {
		
		String sItem, sSlot, sValue;
		
		// while there are still thing left in the String
		while(sConfiguration.length()!=0) {
			SplitReturnType srt = new SplitReturnType();
			// get the first item
			srt = Utils.SplitOnFirst(sConfiguration, ",",'%');
			sItem = srt.FirstPart.trim();
			sConfiguration=srt.SecondPart;

			// gets the slot and the value
			srt = Utils.SplitOnFirst(sItem, "=", '%');
			sSlot = srt.FirstPart.trim();
			sValue = srt.SecondPart.trim();

			// fills in the configuration hash
			SetInputConfigurationParameter( sSlot, sValue);
		}	
	}

	// A: sets an individual parameter
	public void SetInputConfigurationParameter( String sSlot, String sValue) {
		s2sInputLineConfiguration.put( sSlot, sValue);
	}

	// A: tests if a given parameter exists in the configuration
	public boolean HasInputConfigurationParameter( String sSlot) {
		return s2sInputLineConfiguration.containsKey(sSlot);
	}

	// A: gets the value for a given parameter
	public String GetInputConfigurationParameterValue( String sSlot) {
		if (!HasInputConfigurationParameter(sSlot)) {
			return "";
		}
		else {
			return s2sInputLineConfiguration.get(sSlot);
		}
	}
	// J: End copy from Agent.cpp and renamed s2sConfiguration . s2sInputLineConfiguration

	// J: Default Input Config Init String (will be overridden) by most dialog agents
	public String InputLineConfigurationInitString() {
		return "";
	}

	// J: Returns the name of the derived input line config. Only calculates derived config
	// once per session
	public HashMap<String,String> GetInputLineConfiguration() {
		if (!bInheritedParentInputConfiguration)
		{
			bInheritedParentInputConfiguration = true;
			// Sets hash based on init String
			SetInputConfiguration(InputLineConfigurationInitString());
			if(pdaParent != null)
				InheritParentInputConfiguration();
		}
		return s2sInputLineConfiguration;
	}

	// J: Selectively inherits parent's input line configuration
	public void InheritParentInputConfiguration() {
		HashMap<String,String> s2sParentConfig = pdaParent.GetInputLineConfiguration();
		if(s2sParentConfig.size() > 0) {
			Iterator<Map.Entry<String, String>> iterator = 
					s2sParentConfig.entrySet().iterator();
			Map.Entry<String, String> iPtr = null;
			while(iterator.hasNext()) {
				iPtr = iterator.next();
				// only if the agent hasn't had this slot filled in yet
				// will it take its parent's value
				if (!HasInputConfigurationParameter(iPtr.getKey()))
					SetInputConfigurationParameter(iPtr.getKey(), iPtr.getValue());
			}
		}
	}
	//-----------------------------------------------------------------------------
	//
	// Fundamental Dialog Agent methods. Most of these are to be 
	// overwritten by derived agent classes
	//
	//-----------------------------------------------------------------------------
	// D: create the concepts for this agent: does nothing, is to be overwritten 
	//  by derived classes
	public void CreateConcepts() {
	}
	// D: the OnCreation method: by default, nothing happens upon the creation
	//  of the agent
	public void OnCreation() {
	}
	// D: returns true if the dialog agent is executable - by default, returns true
	public boolean IsExecutable() {
		return true;
	}
	
	// D: the GetMaxExecuteCounter function specifies how many times an agent it
	//  to be executed before it terminates with a failure (by default a 
	//  very large int)
	public int GetMaxExecuteCounter() {
		return 10000;
	}
	// D: the SuccessCriteriaSatisfied method: by default an agent completes 
	//  successfully when all the subagents have completed
	public boolean SuccessCriteriaSatisfied() {
		// check that all subagents have completed
		for(int i = 0; i < SubAgents.size(); i++)
			if(!SubAgents.get(i).HasCompleted())
				return false;
	
		return false;
	}
	
	// D: the FailureCriteriaSatisfied method: by default an agent completes 
	//  with a failure when the number of attempts at execution exceeds the 
	//  number of maximum attempts, and the success criteria has not been
	//  met yet
	public boolean FailureCriteriaSatisfied() {
		boolean bFailed = (iExecuteCounter >= GetMaxExecuteCounter()) && 
	      !SuccessCriteriaSatisfied();
	
		if (bFailed)
			Log.d(Const.DIALOGTASK_STREAM_TAG, "Agent reached max attempts ("+iExecuteCounter+" >= "+
					GetMaxExecuteCounter()+"), failing");
	
		return bFailed;
	}
	
	// D: create a grounding model for this agent
	public void CreateGroundingModel(String sGroundingModelSpec) {
	    if(!(DMCore.pGroundingManager.GetConfiguration().bGroundTurns) || 
			(sGroundingModelSpec.equals("none")) || 
			(sGroundingModelSpec.equals("")))
	        pGroundingModel = null;
	    else {
			// extract the model type and policy
			String sModelType="", sModelPolicy="";
			SplitReturnType srt = new SplitReturnType();
			srt = Utils.SplitOnFirst(sGroundingModelSpec, ".");
			sModelType = srt.FirstPart;
			sModelPolicy = srt.SecondPart;
			if(!srt.IsSplitSuccessful) {
				// if there is no model type, set it to the default 
				// grounding manager configuration
				sModelType = DMCore.pGroundingManager.GetConfiguration().sTurnGM;
				sModelPolicy = sGroundingModelSpec;
			}
			// create the grounding model
	        pGroundingModel = 
	            DMCore.pGroundingManager.CreateGroundingModel(sModelType, sModelPolicy);
			// intialize it
	        pGroundingModel.Initialize();
			// set the request agent
	        pGroundingModel.SetRequestAgent(this);
	    }
	}
	// D: the DeclareExpectations: for this class, it calls DeclareExpectations for
	//  all the subagents; also, if the agent is triggered by a command, 
	//  it adds that expectation
	public int DeclareExpectations(ArrayList<TConceptExpectation> rcelExpectationList) {
		int iExpectationsAdded = 0;
		boolean bExpectCondition = ExpectCondition();
	
		// if there's a trigger, construct the expectation list for that trigger
		if(!TriggeredByCommands().equals("")) {
			ArrayList<TConceptExpectation> celTriggerExpectationList = 
					new ArrayList<TConceptExpectation>();
	
			// construct the expectation list for the triggering commands
			parseGrammarMapping(
	          getConceptFromPath(sDialogAgentName).GetAgentQualifiedName(), 
	          TriggeredByCommands(),celTriggerExpectationList);
	
			// go through it and add stuff to the current agent expectation list
			for( int i = 0; i < celTriggerExpectationList.size(); i++) {
				// set the expectation to bind the trigger to true
				celTriggerExpectationList.get(i).bmBindMethod = TBindMethod.bmExplicitValue;
				celTriggerExpectationList.get(i).sExplicitValue = "true";
	
				// if the expect condition is not satisfied, disable this 
				// trigger expectation and set the appropriate reason
				if(!bExpectCondition) {
					celTriggerExpectationList.get(i).bDisabled = true;
					celTriggerExpectationList.get(i).sReasonDisabled = 
						"expect condition false";
				}
	
				rcelExpectationList.add(celTriggerExpectationList.get(i));
				iExpectationsAdded++;
			}
		}
	
		// finally go through the subagents and gather their expectations
		for( int i=0; i < SubAgents.size(); i++) {
			iExpectationsAdded += 
				SubAgents.get(i).DeclareExpectations(rcelExpectationList);
		}
		return iExpectationsAdded;
	}
	
	// D: the DeclareFocusClaims: for this class, it checks it's own ClaimsFocus
	//  condition and it's command trigger condition if one exists, 
	//  then it calls DeclareFocusClaims for all the subagents
	public int DeclareFocusClaims(ArrayList<TFocusClaim> fclFocusClaims) {
		int iClaimsAdded = 0;
	
		// check its own claim focus condition and command trigger condition if 
		// one exists
		boolean bDeclareFocusClaim = ClaimsFocus();
		if(TriggeredByCommands().length()!=0) {
			bDeclareFocusClaim = bDeclareFocusClaim || 
	          getConceptFromPath("_"+sDialogAgentName+"_trigger").IsUpdatedAndGrounded();
		}
	
		// declare the focus claim, in case we have one
	  if(bDeclareFocusClaim) {
			TFocusClaim fcClaim = new TFocusClaim();
			fcClaim.sAgentName = sName;
			fcClaim.bClaimDuringGrounding = ClaimsFocusDuringGrounding();
			fclFocusClaims.add(fcClaim);
			iClaimsAdded++;
			// and also clear the triggering concept, if there is one
			if(TriggeredByCommands().length()!=0)
				getConceptFromPath("_"+sDialogAgentName+"_trigger").Clear();
		}
	
		// then call it for the subagents, so that they can also claim focus
		// if needed 
		for(int i=0; i < SubAgents.size(); i++) {
			iClaimsAdded += SubAgents.get(i).DeclareFocusClaims(fclFocusClaims);
		}
	
		// finally return the number of claims added
		return iClaimsAdded;
	}
	// D: the Precondition: for this class, it does nothing (always returns
	//  true)
	public boolean PreconditionsSatisfied() {
		return true;
	}
	
	//D: the focus claim condition: for this class, it does always returns
	//  false
	public boolean ClaimsFocus() {
		return false;
	}
	
	//D: indicates if the agent claims the focus while grounding is in progress
	//  by default, this is false
	public boolean ClaimsFocusDuringGrounding() {
	  return false;
	}
	// D: the String describing the grammar concepts which trigger this 
	//  agent: the default agent implicitly is not triggered by 
	//  commands
	public String TriggeredByCommands() {
		return "";
	}
	// D: Returns a boolean indicating if the expectations declared are active 
	//  or not. For this class, it always returns true.
	public boolean ExpectCondition() {
		return true;
	}
	
	// D: the DeclareBindingPolicy function: for the default agent, it always
	//  returns MIXED_INITIATIVE
	public String DeclareBindingPolicy() {
		return "bind-anything";
	}
	
	
	//-----------------------------------------------------------------------------
	// Operations related to setting and getting the parent for a dialog 
	// agent
	//-----------------------------------------------------------------------------
	// D: set the parent
	public void SetParent(CDialogAgent pdaAParent) {
		// set the new parent
		pdaParent = pdaAParent;
		// and update the name of the agent
		UpdateName();
	}
	// D: return the parent
	public CDialogAgent GetParent() {
		return pdaParent;
	}
	// D: updates the name of the agent, by looking up the parent and concatenating
	//  names as /name/name/name. Also calls UpdateName for the children, since 
	//  their names need to be updated in this case, too
	public void UpdateName() {
		// analyze if we have or not a parent, and update the name
		if(pdaParent!=null) {
			sName = pdaParent.GetName() + "/" + sDialogAgentName;
		} else {
			sName = "/" + sDialogAgentName;
		}
		
		// and now update the children, too
		for(int i=0; i < SubAgents.size(); i++) 
			SubAgents.get(i).UpdateName();
	}
	//-----------------------------------------------------------------------------
	// Adding and Deleting subagents
	//-----------------------------------------------------------------------------

	// D: add a subagent, in the location indicated by pdaWhere and mmMethod. for
	//	    the mmLastChild and mmFirstChild methods, pdaWhere will be null
	public void AddSubAgent(CDialogAgent pdaWho, CDialogAgent pdaWhere, 
								   TAddSubAgentMethod asamaslastchild) {
		/*Iterator<CDialogAgent> iterator = SubAgents.iterator();
		CDialogAgent iPtr = iterator.next();*/
	    // insert it in the right place
		switch(asamaslastchild) {
			case asamAsFirstChild:
				SubAgents.add(0, pdaWho);
				break;
			case asamAsLastChild:
				SubAgents.add(pdaWho);
				break;	
			case asamAsRightSibling:
				SubAgents.add(SubAgents.indexOf(pdaWhere)+1, pdaWho);
				break;
			case asamAsLeftSibling:
				SubAgents.add(SubAgents.indexOf(pdaWhere), pdaWho);
				break;
		}
	    // set the parent
		pdaWho.SetParent(this);
	    // set it to dynamic
	    pdaWho.SetDynamicAgent();
	    // and register it
	    pdaWho.Register();
	}

	//-----------------------------------------------------------------------------
	// Access to Blocked/Unblocked information
	//-----------------------------------------------------------------------------
	// D: return true if one of the agent's ancestors is blocked
	public boolean IsAgentPathBlocked() {
		// go recursively through the parents to find out if there's anything 
		// blocked
		if(pdaParent==null)
			return pdaParent.IsAgentPathBlocked() || IsBlocked();
		else 
			return IsBlocked();
	}

	// D: return true if the agent is blocked
	public boolean IsBlocked() {
		return bBlocked;
	}

	// D: block the agent
	public void Block() {
		// set blocked to true
		bBlocked = true;
		// and call recursively for all the subagents
		for( int i=0; i < SubAgents.size(); i++) 
			SubAgents.get(i).Block();
	}

	// D: unblock the agent
	public void UnBlock() {
		// set blocked to false
		bBlocked = false;
		// and call recursively for all the subagents
		for( int i=0; i < SubAgents.size(); i++) 
			SubAgents.get(i).UnBlock();
	}
	//-----------------------------------------------------------------------------
	// Obtaining the main topic
	//-----------------------------------------------------------------------------
	// D: returns the main topic for an agent
	public CDialogAgent GetMainTopic() {
		// if this is a main topic, return it
		if(IsAMainTopic()) return this;
		else if(pdaParent==null) {
			Log.d(Const.DMCORE_STREAM_TAG, sName+" has no parent -> MainTopic=NULL");
			// if it's not a main topic and it doesn't have a parent, return NULL
			return null;
		} else {
			// return the main topic of its parent
			return pdaParent.GetMainTopic();
		}
	}
	//-----------------------------------------------------------------------------
	// Access to the last input index 
	//-----------------------------------------------------------------------------
	// D: set the last input index 
	public void SetLastInputIndex(int iInputIndex) {
	    iLastInputIndex = iInputIndex;
	}

	// D: obtain a pointer to the last input index
	public int GetLastInputIndex() {
	    return iLastInputIndex;
	}
	//-----------------------------------------------------------------------------
	// Access to turns in focus counter
	//-----------------------------------------------------------------------------
	// D: increment the turns in focus counter
	public void IncrementTurnsInFocusCounter() {
	    iTurnsInFocusCounter++;
	}

	// D: obtain the value of the turns in focus counter
	public int GetTurnsInFocusCounter() {
	    return iTurnsInFocusCounter;
	}
	//-----------------------------------------------------------------------------
	// Access to the last execution index 
	//-----------------------------------------------------------------------------
	// A: set the last execution index 
	public void SetLastExecutionIndex(int iExecutionIndex) {
	    iLastExecutionIndex = iExecutionIndex;
	}
	//-----------------------------------------------------------------------------
	// Access to the last bindings index
	//-----------------------------------------------------------------------------
	// D: set the last bindings index 
	public void SetLastBindingsIndex(int iBindingsIndex) {
	    iLastBindingsIndex = iBindingsIndex;
	}
	
	//-----------------------------------------------------------------------------
	// Access to completed, reset, reopen information
	//-----------------------------------------------------------------------------

	// D: return the agent completion status
	public boolean HasCompleted() {
	    // if the agent has the completed flag set, return true
	    if(bCompleted) return true;

	    // o/w check HasSucceeded and HasFailed
		return HasSucceeded() || HasFailed();
	}
	// D: indicates if the agent has completed successfully
	public boolean HasSucceeded() {

	    // if the agent is already marked as succeeded, return true
	    if(bCompleted && (ctCompletionType == TCompletionType.ctSuccess)) 
	        return true;

	    // o/w check if the success criterion was recently matched
	    return SuccessCriteriaSatisfied();
	}
	// D: indicates if the agent has completed with a failure
	public boolean HasFailed() {

	    // if the agent is already marked as failed, return true
	    if(bCompleted && (ctCompletionType == TCompletionType.ctFailed))
	        return true;
	    
	    // o/w check if the failure condition was recently matched
	    return FailureCriteriaSatisfied();
	}
	
	//-----------------------------------------------------------------------------
	// Access to dynamic agent ID information
	//-----------------------------------------------------------------------------
	// D: sets the dynamic agent flag recursively
	public void SetDynamicAgent() {
	    bDynamicAgent = true;
	    for(int i = 0; i < SubAgents.size(); i++)
	        SubAgents.get(i).SetDynamicAgent();
	}
	// D: returning the dynamic agent ID
	public String GetDynamicAgentID() {
	    return sDynamicAgentID;
	}
	//-----------------------------------------------------------------------------
	// 
	// Protected methods for parsing various declarative constructs
	//
	//-----------------------------------------------------------------------------
		
	// D: Parse a grammar mapping into a list of expectations
	@SuppressLint("DefaultLocale")
	public void parseGrammarMapping(String sConceptNames, 
		
		String sGrammarMapping, ArrayList<TConceptExpectation> rcelExpectationList) {

		// empty the list
		rcelExpectationList.clear();

		// parse it, construct the appropriate expectation structures and add them
		// to the list 

		String sItem="";	// take one item at a time
		
		// while there are still thing left in the String
		while(sGrammarMapping.length()!=0) {
			
			TConceptExpectation ceExpectation=new TConceptExpectation(); 
									// the concept expectation definition

			// get the first item
			SplitReturnType srt = new SplitReturnType();
			srt = Utils.SplitOnFirst(sGrammarMapping, ",");
			sItem = srt.FirstPart;
			sGrammarMapping=srt.SecondPart;
			sItem = sItem.trim();

			// process the item and construct the expectation
			String sLeftSide="", sRightSide="";

			// decide what is the binding method
			srt = Utils.SplitOnFirst(sItem, ">");
			sLeftSide = srt.FirstPart;
			sRightSide= srt.SecondPart;
	        if(srt.IsSplitSuccessful) {
	            sRightSide = sRightSide.trim();
	            if(sRightSide.charAt(0) == ':') 
	                ceExpectation.bmBindMethod = TBindMethod.bmBindingFilter;
	            else
	                ceExpectation.bmBindMethod = TBindMethod.bmExplicitValue;
	        } else {
	            ceExpectation.bmBindMethod = TBindMethod.bmSlotValue;
	        }

			sLeftSide = sLeftSide.trim();

			// analyze if the expectation is open at this point or not
			// (i.e. do we have [slot] or ![slot] or @[slot] or @(agent,agent)[slot])
			if(sLeftSide.charAt(0) == '[') {
				// if a simple concept mapping, then we declare it only if it's 
				// under the main topic (disable it otherwise)
				ceExpectation.bDisabled = 
	                !DMCore.pDTTManager.IsAncestorOrEqualOf(
	                    DMCore.pDMCore.GetCurrentMainTopicAgent().GetName(), sName);
				if(ceExpectation.bDisabled) {
					ceExpectation.sReasonDisabled = "[] not under topic";
				}
				ceExpectation.sGrammarExpectation = sLeftSide;
	            ceExpectation.sExpectationType = "";
			} else if(sLeftSide.charAt(0) == '!') {
				// if a ![] concept mapping, declare it only if we are under focus
				ceExpectation.bDisabled = !DMCore.pDMCore.AgentIsInFocus(this);
				if(ceExpectation.bDisabled) {
					ceExpectation.sReasonDisabled = "![] not under focus";
				}
				ceExpectation.sGrammarExpectation = 
	                sLeftSide.substring(1, sLeftSide.length());
	            ceExpectation.sExpectationType = "!";
			} else if((sLeftSide.charAt(0) == '@') || (sLeftSide.charAt(0) == ' ')) {
	            if(sLeftSide.charAt(1) == '[') {
				    // if a @[] or [] concept mapping, then always declare it
				    ceExpectation.bDisabled = false;
				    ceExpectation.sGrammarExpectation = 
	                    sLeftSide.substring(1, sLeftSide.length());
	            } else if(sLeftSide.charAt(1) == '(') {
	                // if a @(agent,agent)[] or (agent,agent)[] concept mapping, 
	                // then declare it only if the focus is under one of those agents
	                // start by constructing the list of agents
	                String sAgents="";
	                srt = Utils.SplitOnFirst( sLeftSide, ")");
	                sAgents=srt.FirstPart;
	                ceExpectation.sGrammarExpectation=srt.SecondPart;
	                sAgents = sAgents.substring(2, sAgents.length());
	                ArrayList<String> vsAgents = new ArrayList<String>();
	                vsAgents = Utils.PartitionString(sAgents, ";");

	                // figure out the focused task agent
	                CDialogAgent pdaDTSAgentInFocus = DMCore.pDMCore.GetDTSAgentInFocus();
	                if(pdaDTSAgentInFocus==null) 
	                    Log.e(Const.DIALOGTASK_STREAM_TAG,"Could not find a DTS agent in focus.");
	                String sFocusedAgentName = pdaDTSAgentInFocus.GetName();

	                // go through the agents in the list and figure out if they contain the
	                // focus
	                ceExpectation.bDisabled = true;
	                for(int i = 0; i < vsAgents.size(); i++) {
	                    if(DMCore.pDTTManager.IsAncestorOrEqualOf(
	                    		getAgentFromPath(vsAgents.get(i)).GetName(), 
	                            sFocusedAgentName)) {
	                        ceExpectation.bDisabled = false;
	                        break;
	                    }
	                }
	                if(ceExpectation.bDisabled) {
	                    ceExpectation.sReasonDisabled = 
	                    		sLeftSide.charAt(0)+"("+sAgents+") not containing focus"; 
	                }        
	            }
	            // finally, set the expectation type
	            ceExpectation.sExpectationType = String.valueOf(sLeftSide.charAt(0));
			}

			// close the expectation if the agent path is blocked
			if(IsAgentPathBlocked()) {
				ceExpectation.bDisabled = true;
				ceExpectation.sReasonDisabled = "agent path blocked";
			}

			// if we bind an explicitly specified concept value
			if(ceExpectation.bmBindMethod == TBindMethod.bmExplicitValue) {
				// set the sExplicitValue member
				ceExpectation.sExplicitValue = sRightSide;
	        } else if(ceExpectation.bmBindMethod == TBindMethod.bmBindingFilter) {
	            // set the sBindingFilterName member
	            ceExpectation.sBindingFilterName = 
	                sRightSide.substring(1, sRightSide.length());
	        }
			
			// fill in the rest of the members of the expectation structure
			ceExpectation.pDialogAgent = this;
			//?????????????????????????????????????????????????????
			ceExpectation.vsOtherConceptNames = Utils.PartitionString(sConceptNames, ";");
		    // now go through this and replace the names with the agent qualified
		    // versions
		    for( int i = 0; i < ceExpectation.vsOtherConceptNames.size(); i++){
		    	ceExpectation.vsOtherConceptNames.set(i,
			            getConceptFromPath(ceExpectation.vsOtherConceptNames.get(i).trim()).
			            GetAgentQualifiedName());
		    }
		        
		    // now assing the name of the main expected concept
			ceExpectation.sConceptName = ceExpectation.vsOtherConceptNames.get(0);
			// and delete the first one from the others list
			ceExpectation.vsOtherConceptNames.remove(0);

	        // finally lowecase the grammar expectation
	        ceExpectation.sGrammarExpectation = 
	            ceExpectation.sGrammarExpectation.toLowerCase();

			// add the expectation to the list
			rcelExpectationList.add(ceExpectation);
		}
	}
	
	
	//-----------------------------------------------------------------------------
	// 
	// Other methods, mainly for providing access to public and protected members
	//
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// Relative access to Concepts
	//-----------------------------------------------------------------------------
	// D: the function returns a reference to the concept pointed by the 
	//	    relative concept path in sConceptPath
	public CConcept getConceptFromPath(String sConceptPath) {	
		
		String sAgentPath="", sConceptName="";
		SplitReturnType srt = Utils.SplitOnLast(sConceptPath, "/");
		sAgentPath =srt.FirstPart;
		sConceptName=srt.SecondPart;
		// check if we have an agent path in the concept name
		if(srt.IsSplitSuccessful) {
			// if yes, identify the agent and call the local concept retrieval for
			// that agent
			return getAgentFromPath(sAgentPath).LocalC(sConceptName);
		} else {
			// if not, go and try to find the concept locally
			return LocalC(sConceptPath);
		}
	}
	// D: the function returns a pointer to a local concept indicated by 
	//  sConceptName. If the concept is not found locally, we try to locate
	//  it in the parent
	public CConcept LocalC(String sConceptName) {
	
		// if the agent has a defined context agent,
		// look for the concept there
		CDialogAgent pdaNextContext = null;
		if (pdaContextAgent!=null) {
			pdaNextContext = pdaContextAgent;
		} else {
			pdaNextContext = pdaParent;
		}
	
	  // convert the eventual # signs with agent dynamic id
	  sConceptName = sConceptName.replace("#", GetDynamicAgentID());
	
		// Optimization code: if no concepts, then try the parent directly 
		// (if a parent exists)
		if(Concepts.size() == 0) {
			if(pdaNextContext!=null) return pdaNextContext.LocalC(sConceptName);
			else {
				// if there's no parent, we're failing
				Log.e(Const.DIALOGTASK_STREAM_TAG,"Concept " + sConceptName + 
						   " could not be located (accessed).");
				return null;
			}
		}
	
		// split the concept into base and rest in case we deal with a complex
		// concept (i.e. arrays or structures i.e. hotel.name)
		String sBaseConceptName="", sRest="";
		SplitReturnType srt = Utils.SplitOnFirst(sConceptName, ".");
		sBaseConceptName=srt.FirstPart;
		sRest = srt.SecondPart;
	
		// A: Checks if we want a merged history version of the concept
		boolean bMergeConcept = false;
		if (sBaseConceptName.charAt(0) == '@') {
			bMergeConcept = true;
			sBaseConceptName = sBaseConceptName.substring(1,sBaseConceptName.length());
		}
	
		// now go through the list of concepts and see if you can find it
		for(int i=0; i < Concepts.size(); i++) {
			if(Concepts.get(i).GetName().equals(sBaseConceptName)) {
				// if we have found it
				if(bMergeConcept) {
					CConcept pcMerged = Concepts.get(i).getIndexing(sRest).CreateMergedHistoryConcept();
					return pcMerged;
				} else {
					return Concepts.get(i).getIndexing(sRest);
				}
			}
		}
	
		// if it was not in the concepts owned by this agency, try in the parent/context
		// check if there is any parent/context
		if(pdaNextContext==null) {
			// if there's no parent, we're failing
			Log.e(Const.DIALOGTASK_STREAM_TAG,"Concept " + sConceptName + 
					" could not be identified in the dialog task hierarchy.");
			return null;
		} else {
			// if there's a parent/context, check in there
			return pdaNextContext.LocalC(sConceptName);
		}
	}	
	//-----------------------------------------------------------------------------
	// Relative access to Agents
	//-----------------------------------------------------------------------------
	// D: the function returns a pointer to the agent pointed by the relative
	//	    agent path in sDialogAgentPath
	public CDialogAgent getAgentFromPath(String sDialogAgentPath) {

		// split the relative agent path into the first component (until /)
		// and the rest
		String sFirstComponent="", sRest="";
		SplitReturnType srt = Utils.SplitOnFirst(sDialogAgentPath, "/");
		sFirstComponent = srt.FirstPart;
		sRest = srt.SecondPart;
		if(srt.IsSplitSuccessful) {

			// if split is successful, recurse based on the first component 
			if(sFirstComponent.equals("")) {

				// the path starts at the root, so it's an absolute path
				// to an agent. We can (optimally) find the agent using the 
				// registry
				CDialogAgent pdaAgent = (CDialogAgent)
						CRegistry.AgentsRegistry.getAgentGivenName(sDialogAgentPath);
				if(pdaAgent!=null) {
					// if the agent was found
					return pdaAgent;
				} else {
					// if the agent was not found, fail
					Log.e(Const.DIALOGTASK_STREAM_TAG,"Agent " + sDialogAgentPath + 
							" does not exist in the dialog task hierarchy.");
					return null;
				}

			} else if(sFirstComponent.equals("..")) {
				
				// then start at the parent
				if(pdaParent==null) {
					// if there's no parent, we're failing
					Log.e(Const.DIALOGTASK_STREAM_TAG,"Agent "
					+ sDialogAgentPath 
					+ " could not be identified in the dialog task hierarchy relative to "
					+ GetName() + ".");
					return null;
				} else {
					// otherwise recurse on the parent
					return pdaParent.getAgentFromPath(sRest);
				}

			} else if (sFirstComponent.equals(".")) {
				
				// then it's in this agent
				return getAgentFromPath(sRest);

			} else {

				// then it must be one of the descendants. Locate quickly using
				// the registry
				CDialogAgent pdaAgent = 
						(CDialogAgent)CRegistry.AgentsRegistry.getAgentGivenName(
								GetName() + "/" + sDialogAgentPath);
				if(pdaAgent!=null) {
					// if the agent was found
					return pdaAgent;
				} else {
					// if the agent was not found, fail
					Log.e(Const.DIALOGTASK_STREAM_TAG,"Agent " 
					+ sDialogAgentPath 
					+ " could not be identified in the dialog task hierarchy relative to "
					+ GetName() + ".");
					return null;
				}
			}

		} else {
			// we have no path

			// it is possible that the agent referent is just "" at this point, 
			// which means we need to return the current agent
			if((sDialogAgentPath.equals("")) || (sDialogAgentPath.equals(".")))
				return this;

			// or it is possible that it's just a reference to the parent ..
			if(sDialogAgentPath.equals("..")) {
				if(pdaParent==null) {
					// if there's no parent, we're failing
					Log.e(Const.DIALOGTASK_STREAM_TAG,"Agent .. could not be "
					+"identified in the dialog task hierarchy relative to "
					+ GetName() + ".");
					return null;
				} else {
					// otherwise recurse on the parent
					return pdaParent;
				}
			}

			// if not, try and find the agent locally (it has to 
			// be one of the subagents). Locate quickly using the registry.
			CDialogAgent pdaAgent = (CDialogAgent)
					CRegistry.AgentsRegistry.getAgentGivenName(GetName() + "/" + 
													sDialogAgentPath);
			if(pdaAgent!=null) {
				// if the agent was found
				return pdaAgent;
			} else {
				// if the agent was not found, fail
				Log.e(Const.DIALOGTASK_STREAM_TAG,"Agent " + sDialogAgentPath 
						+ " could not be identified in the dialog task hierarchy "
						+ "relative to " + GetName() + ".");
				return null;
			}
		} 
	}
	// D: this method creates a triggering concept, in case one is needed (if the
	//  agent is to be triggered by a command
	public void CreateTriggerConcept() {
		// if the agent is to be triggered by a user command, 
		if(TriggeredByCommands().length()!=0) {
	      // add a trigger concept
			CBoolConcept ncbBoolConcept=new CBoolConcept(
					"_"+sDialogAgentName+"_trigger",TConceptSource.csUser);
	      Concepts.add(ncbBoolConcept);
	      // set the grounding model
	      Concepts.get(Concepts.size()-1).CreateGroundingModel(
	          sTriggerCommandsGroundingModelSpec);
	      // and set it's owner dialog agent
	      Concepts.get(Concepts.size()-1).SetOwnerDialogAgent(this);
		}
	}
	//-----------------------------------------------------------------------------
	// Execute counter methods, Turns in focus count methods
	//-----------------------------------------------------------------------------
	// D: increment the execute count
	public void IncrementExecuteCounter() {
	    iExecuteCounter++;
	}
	// Macro definition
	public boolean AVAILABLE(String ConceptName){
		return getConceptFromPath(ConceptName).IsAvailableAndGrounded();
	}
	public boolean IS_TRUE(String ConceptName) {
		return AVAILABLE(ConceptName) && (getConceptFromPath(ConceptName)!=null);
	}
	// LILINCHUAN: Returns the concept required by this agent
	// (Will be override by MARequestAgent)
	public String GetRequiredConcept(){
		Log.e(Const.DIALOGTASK_STREAM_TAG,
				"GetRequiredConcept called by DialogAgent");
		return "";
	}
	// D: Returns a String describing the concept mapping (nothing for this class)
	//	    Is to be overwritten by derived classes
	public String GrammarMapping() {
		return "";
	}

	// D: Returns the name of the requested concept (nothing for this class); is to 
	//	    be overwritten by derived classes
	public String RequestedConceptName() {
		return "";
	}
	public void OnDestruction() {
		 // go through all the subagents and call the method
		for(int i=0; i < SubAgents.size(); i++) 
			SubAgents.get(i).OnDestruction();
	}
	public String GetDialogName() {
		// TODO Auto-generated method stub
		return sDialogAgentName;
	}
}
