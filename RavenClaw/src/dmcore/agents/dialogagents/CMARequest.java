package dmcore.agents.dialogagents;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.TConceptExpectation;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;
import dmcore.agents.mytypedef.TFloorStatus;
import dmcore.concepts.CConcept;
import dmcore.outputs.COutput;

public class CMARequest extends CDialogAgent{
	// list of prompts planned by this agent
	protected ArrayList<COutput> voOutputs;

	// the input index at the time of the agent's latest execution
	protected int iInputTurnNumberAtExecution;
	
	//-----------------------------------------------------------------------
	// Constructors and Destructors
	//-----------------------------------------------------------------------

	// D: default constructor
	public CMARequest(String sAName, String sAConfiguration, String sAType){
		super(sAName, sAConfiguration, sAType);
	}
	
	public CMARequest(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CMARequest";
		this.SetType(sAType);
	}
	public CMARequest(String sAName) {
		// TODO Auto-generated constructor stub
		super(sAName);
	}
	public CMARequest(){
		
	}
	/*public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CMARequest(sAName, sAConfiguration);
	}*/

	//----------------------------------------------------------------------
	//
	// Specialized (overwritten) CDialogAgent methods
	//
	//----------------------------------------------------------------------

	// D: Execute function: issues the request prompt, 
	//	then waits for user input
	public TDialogExecuteReturnCode Execute() {
		// set bOutputCompleted value to false
		bOutputCompleted = false;
		
	    // call on the output manager to send out the output
		//DMCore.pOutputManager.Output(this, Prompt(), TFloorStatus.fsUser);
		DMCore.myoutput.synthetizeInSilence(Prompt());
	    // set the timeout period
	    DMCore.pDMCore.SetTimeoutPeriod(GetTimeoutPeriod());

	    // set the nonunderstanding threshold
	    DMCore.pDMCore.SetNonunderstandingThreshold(GetNonunderstandingThreshold());

	    // increment the execute counter
	    IncrementExecuteCounter();

		// and return with 
		return TDialogExecuteReturnCode.dercTakeFloor;
	}

	// A: Resets clears the list of outputs for this concept
	public void Reset() {
		super.Reset();
		voOutputs.clear();
	}

	// D: Declare the expectations: add to the incoming list the expectations 
//	    of this particular agent, as specified by the GrammarMapping slot
	public int DeclareExpectations(ArrayList<TConceptExpectation>
										celExpectationList) {
		
		int iExpectationsAdded = 0;
		ArrayList<TConceptExpectation> celLocalExpectationList =
				new ArrayList<TConceptExpectation>();
		boolean bExpectCondition = ExpectCondition();

		// first get the expectations from the local "expected" concept
	    String sRequestedConceptName = RequestedConceptName();
	    String sGrammarMapping = GrammarMapping();
	    if(sRequestedConceptName.length()!=0 && sGrammarMapping.length()!=0)
		    parseGrammarMapping(sRequestedConceptName, sGrammarMapping, 
			    celLocalExpectationList);

		// now go through it and add stuff to the 
		for(int i = 0; i < celLocalExpectationList.size(); i++) {
			// if the expect condition is not satisfied, disable this 
			// expectation and set the appropriate reason
			if(!bExpectCondition) {
				celLocalExpectationList.get(i).bDisabled = true;
				celLocalExpectationList.get(i).sReasonDisabled = "expect condition false";
			}
			celExpectationList.add(celLocalExpectationList.get(i));
			iExpectationsAdded++;
		}

		// now add whatever needs to come from the CDialogAgent side
		iExpectationsAdded += 
			super.DeclareExpectations(celExpectationList);

		// and finally return the total number of added expectations
		return iExpectationsAdded;
	}

	// D: Preconditions: by default, preconditions for a request agent are that 
//	    the requested concept is not available
	public boolean PreconditionsSatisfied() {
	    if(RequestedConceptName() != "") 
		    return !RequestedConcept().IsAvailableAndGrounded();
	    else
	        return true;
	}

	// D: SuccessCriteriaSatisfied: Request agents are completed when the concept 
//		  has become grounded
	public boolean SuccessCriteriaSatisfied() {
	    if(RequestedConceptName() != "")
		    return RequestedConcept().IsUpdatedAndGrounded();
	    else {
	        // if it's an open request, then it completes when it's been tried and 
	        // some concept got bound in the previous input pass
	        return ((iTurnsInFocusCounter > 0) && !DMCore.pDMCore.LastTurnNonUnderstanding());
	    }
	}

	// A: FailureCriteriaSatisfied: Request agents fail when the maximum number
//	    of attempts has been made
	public boolean FailureCriteriaSatisfied() {
		boolean bFailed = (iTurnsInFocusCounter >= GetMaxExecuteCounter()) && 
	        !SuccessCriteriaSatisfied();

		if (bFailed)
			Log.d(Const.DIALOGTASK_STREAM, "Agent reached max attempts ("
					+iTurnsInFocusCounter+" >= "+GetMaxExecuteCounter()
					+"), failing");

		return bFailed;
	}

	// D: Returns the request prompt as a String
	public String Prompt() {
	//#ifdef GALAXY
		// by default, request the name of the requested concept
	    if(RequestedConceptName() != "") {
	        // get the first requested concept
	        SplitReturnType srt =Utils.SplitOnFirst(RequestedConceptName(), ";");
	        String sConceptName = srt.FirstPart;
	        String sFoo = srt.SecondPart;
		    return sName+" agent request "+sConceptName;
	    } else
		    return "request generic agent "+sName;
	/*#endif
	#ifdef OAA
		// by default return the name of the requested concept
	    if(RequestedConceptName() != "") {
	        // get the first requested concept
	        String sConceptName, sFoo;
	        Utils.SplitOnFirst(RequestedConceptName(), ";", sConceptName, sFoo);
		    return FormatString("[%s]", sConceptName);
	    } else 
	        return "[generic]";*/
	}


	//-----------------------------------------------------------------------------
	//
	// Request Microagent specific methods
	//
	//-----------------------------------------------------------------------------

	public String GetRequiredConcept(){
		return "["+RequestedConceptName()+"]";
	}

	// D: Returns a pointer to the requested concept (uses the concept name)
	public CConcept RequestedConcept() {
	    if(RequestedConceptName() != "") {
	        // get the first requested concept
	        SplitReturnType srt = Utils.SplitOnFirst(RequestedConceptName(), ";");
	        String sConceptName = srt.FirstPart;
	        String sFoo = srt.SecondPart;
		    return getConceptFromPath(sConceptName);	    
	    } else return null;
	}

	// D: Retuns the timeout duration : by default it returns whatever the 
    //	    default value is in the DMCore
	public int GetTimeoutPeriod() {
	    return DMCore.pDMCore.GetDefaultTimeoutPeriod();
	}

	// D: Retuns the nonunderstanding threshold: by default it returns whatever the 
//	    default value is in the DMCore
	public float GetNonunderstandingThreshold() {
	    return DMCore.pDMCore.GetDefaultNonunderstandingThreshold();
	}
	
}
