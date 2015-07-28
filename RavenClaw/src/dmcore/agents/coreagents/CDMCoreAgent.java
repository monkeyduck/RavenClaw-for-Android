package dmcore.agents.coreagents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Const;
import utils.Utils;

import android.os.Handler;
import android.util.Log;

import dmcore.agents.dialogagents.CDialogAgent;
import dmcore.agents.mytypedef.TBindMethod;
import dmcore.agents.mytypedef.TBindingFilterFunct;
import dmcore.agents.mytypedef.TConceptExpectation;
import dmcore.agents.mytypedef.TCustomStartOverFunct;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;
import dmcore.agents.mytypedef.TFloorStatus;
import dmcore.agents.mytypedef.TFocusClaim;
import dmcore.agents.mytypedef.TSystemActionOnConcept;
import dmcore.concepts.CConcept;
import dmcore.concepts.CHyp;
import dmcore.events.CInteractionEvent;
import dmcore.agents.mytypedef.AgentFactory;

class TExecutionStackItem {
	// D: structure holding an execution stack item
	public CDialogAgent pdaAgent; // the agent that is on the stack
	public int iEHIndex; // an index into it's correspondent history entry

}

// D: structure holding a execution history item
class TExecutionHistoryItem {
	String sCurrentAgent; // the name of the agent that is executed
	String sCurrentAgentType; // the type of that agent
	String sScheduledBy; // the agent that scheduled this one for
							// execution
	boolean bScheduled; // has the history item been scheduled
	// for execution?
	boolean bExecuted; // has the history item been executed?
	boolean bCommitted; // has the history item been committed
	// to history?
	boolean bCanceled; // has the history item been canceled
	// before being committed?
	Calendar timeScheduled; // the time when the agent was scheduled
							// for execution
	ArrayList<Calendar> vtExecutionTimes = new ArrayList<Calendar>();
	// the times when the agent was actually
	// executed
	Calendar timeTerminated; // the time when the agent completed
	// execution
	int iStateHistoryIndex; // the index in the history of dialog
							// states when the agent was executed
}

// D: structure describing one particular binding
class TBinding {
	boolean bBlocked; // indicates whether the binding was
						// blocked or not
	String sGrammarExpectation; // the expected grammar slot
	String sValue; // the value in the binding
	float fConfidence; // the confidence score for the binding
	int iLevel; // the level in the agenda
	String sAgentName; // the name of the agent that declared the
						// expectation
	String sConceptName; // the name of the concept that will bind
	String sReasonDisabled; // if the binding was blocked, the reason
							// the expectation was disabled
}

// D: structure describing a particular forced update

class TForcedConceptUpdate {
	String sConceptName; // the name of the concept that had a
							// forced update
	int iType; // the type of the forced update
				// public static final String Const.FCU_EXPLICIT_CONFIRM 1
				// public static final String Const.FCU_IMPLICIT_CONFIRM 2
				// public static final String Const.FCU_UNPLANNED_IMPLICIT_CONFIRM 3
	boolean bUnderstanding; // the update changed the concept
							// enough that the grounding action
							// on it is different, and therefore
							// we consider that we have an actual
							// understanding occuring on that
							// concept
}

// D: structure holding a binding history item, describing the bindings in a
// turn
class TBindingsDescr {
	// int iTurnNumber; // the input turn number
	String sEventType; // the type of event to which this
						// binding corresponds
	boolean bNonUnderstanding; // was the turn a non-understanding?
								// i.e. no concepts bound
	int iConceptsBound; // the number of bound concepts
	int iConceptsBlocked; // the number of blocked concepts
	int iSlotsMatched; // the number of slots that matched
	int iSlotsBlocked; // the number of slots that were blocked
	ArrayList<TBinding> vbBindings = new ArrayList<TBinding>(); // the vector of
																// bindings
	ArrayList<TForcedConceptUpdate> vfcuForcedUpdates = new ArrayList<TForcedConceptUpdate>();
	// the vector of forced updates
}

// -----------------------------------------------------------------------------
// D: a compiled concept expectation: implemented as a mapping between a
// String (the grammar concept), and a vector of integers (pointers in the
// concept expectation list. This representation is used for faster
// checking of grammar concept expectations

class TCompiledExpectationLevel {
	HashMap<String, ArrayList<Integer>> mapCE = new HashMap<String, ArrayList<Integer>>(); // the
	// hash
	// of
	// compiled
	// expectations
	CDialogAgent pdaGenerator; // the agent that represents that level
								// of expectations
}

// D: the struct representation for the compiled expectation agenda. It
// contains the expectation list gathered from the dialog task tree,
// and a "compiled" representation for each of the levels
class TExpectationAgenda {
	// the full system expectations, as gathered from the dialog task tree
	ArrayList<TConceptExpectation> celSystemExpectations = new ArrayList<TConceptExpectation>(); // ¼ûdialogagent.h

	// an array holding the expectations of different levels (on index 0,
	// are the expectations of the focused agent, on index 1 those of the
	// immediate upper level agent, etc)
	ArrayList<TCompiledExpectationLevel> vCompiledExpectations = new ArrayList<TCompiledExpectationLevel>();
}

// D: structure maintaining a description of the current system action on the
// various concepts
class TSystemAction {
	Set<CConcept> setcpRequests = new HashSet<CConcept>();
	Set<CConcept> setcpExplicitConfirms = new HashSet<CConcept>();
	Set<CConcept> setcpImplicitConfirms = new HashSet<CConcept>();
	Set<CConcept> setcpUnplannedImplicitConfirms = new HashSet<CConcept>();
}

public class CDMCoreAgent extends CAgent implements AgentFactory {
	

	// ---------------------------------------------------------------------
	// Private members
	// ---------------------------------------------------------------------
	//
	ArrayList<TExecutionStackItem> esExecutionStack = new ArrayList<TExecutionStackItem>(); // the
																							// execution
																							// stack
	private ArrayList<TExecutionHistoryItem> ehExecutionHistory = new ArrayList<TExecutionHistoryItem>(); // the
																											// execution
																											// history
	private ArrayList<TBindingsDescr> bhBindingHistory = new ArrayList<TBindingsDescr>(); // the
																							// binding
																							// history
	TExpectationAgenda eaAgenda = new TExpectationAgenda(); // the expectation
															// agenda
	private ArrayList<TFocusClaim> fclFocusClaims = new ArrayList<TFocusClaim>(); // the
																					// list
																					// of
																					// focus
																					// claims
	public TSystemAction saSystemAction=new TSystemAction(); // the current system action

	private int iTimeoutPeriod; // the current timeout period
	private int iDefaultTimeoutPeriod; // the default timeout period

	private float fNonunderstandingThreshold; // the current nonunderstanding
												// threshold
	private float fDefaultNonunderstandingThreshold;// the default
													// nonunderstanding
													// threshold

	private Map<String, TBindingFilterFunct> s2bffFilters = // the register of
	new HashMap<String, TBindingFilterFunct>(); // binding filters

	private boolean bFocusClaimsPhaseFlag; // indicates whether we should
											// run focus claims
	protected boolean bAgendaModifiedFlag; // indicates if the agenda should
											// be recompiled

	TFloorStatus fsFloorStatus; // indicates who has the floor
	int iTurnNumber; // stores the current turn number
	private TCustomStartOverFunct csoStartOverFunct;// a custom start over
													// function

	private ArrayList<String> vsFloorStatusLabels = new ArrayList<String>();

	// ---------------------------------------------------------------------
	// Constructor and destructor
	// ---------------------------------------------------------------------
	//
	// D: constructor
	public CDMCoreAgent(String sAName, String sAConfiguration) {
		super(sAName, sAConfiguration);
		this.sType = "CAgent:CDMCoreAgent";
		bFocusClaimsPhaseFlag = false;
		fsFloorStatus = TFloorStatus.fsSystem;
		iTurnNumber = 0;
		csoStartOverFunct = null;
		vsFloorStatusLabels.add("unknown");
		vsFloorStatusLabels.add("user");
		vsFloorStatusLabels.add("system");
		vsFloorStatusLabels.add("free");
	}

	public CDMCoreAgent(String sAName, String sAConfiguration, String sAType) {
		super(sAName, sAConfiguration, sAType);
		bFocusClaimsPhaseFlag = false;
		fsFloorStatus = TFloorStatus.fsSystem;
		iTurnNumber = 0;
		csoStartOverFunct = null;
		vsFloorStatusLabels.add("unknown");
		vsFloorStatusLabels.add("user");
		vsFloorStatusLabels.add("system");
		vsFloorStatusLabels.add("free");
	}

	public CDMCoreAgent() {
		// TODO Auto-generated constructor stub
	}

	// -----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	// -----------------------------------------------------------------------------
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CDMCoreAgent(sAName, sAConfiguration);
	}

	// -----------------------------------------------------------------------------
	// CAgent class overwritten methods
	// -----------------------------------------------------------------------------

	// D: the overwritten Reset method
	@Override
	public void Reset() {
		// clear the class members
		esExecutionStack.clear();
		ehExecutionHistory.clear();
		bhBindingHistory.clear();
		eaAgenda.celSystemExpectations.clear();
		eaAgenda.vCompiledExpectations.clear();
	}

	// -----------------------------------------------------------------------------
	//
	// CoreAgent specific methods
	//
	// -----------------------------------------------------------------------------

	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------
	//
	// DIALOG CORE EXECUTION
	//
	// -----------------------------------------------------------------------------
	// -----------------------------------------------------------------------------

	// -----------------------------------------------------------------------------
	// D: this function does the actual dialog task execution, by executing the
	// agents that are on the stack, and issuing input passes when appropriate
	// -----------------------------------------------------------------------------

	public void Execute(Handler MainHandler) {
		Log.d("ThreadId", "" + Thread.currentThread().getId());
		// create & initialize the dialog task
		DMCore.pDTTManager.CreateDialogTree();
		
		// The floor is initially free
		SetFloorStatus(TFloorStatus.fsFree);

		// put the root of the dialog task on the execution stack
		Log.d(Const.DMCORE_STREAM_TAG, "Starting Dialog Task execution.");
		ContinueWith(this, DMCore.pDTTManager.GetDialogTaskTreeRoot());

		// creates the initial dialog state
		DMCore.pStateManager.UpdateState();

		// do the while loop for execution
		while (!(esExecutionStack.isEmpty()||DMCore.bForceExit)) {

			String sTurnId = "User:???";
			if (DMCore.pInteractionEventManager.GetLastInput() != null)
				sTurnId = "User:"
						+ DMCore.pInteractionEventManager.GetLastInput()
								.GetStringProperty("[uttid]");

			Log.d(Const.DMCORE_STREAM_TAG, "Starting DMCore processing [" + sTurnId
					+ "]");

			// eliminate all the agents that have completed from the execution
			// stack
			popCompletedFromExecutionStack();

			Log.d(Const.DMCORE_STREAM_TAG, "Eliminated completed agents from stack ["
					+ sTurnId + "]");

			// if the execution stack is empty now, break the loop
			if (esExecutionStack.isEmpty()||DMCore.bForceExit)
				break;

			// Performs grounding only when the floor is free and
			// we got all our notifications
			if ((GetFloorStatus() == TFloorStatus.fsFree) && true) {
				/*
				 * (DMCore.pOutputManager.GetPromptsWaitingForNotification() ==
				 * "")) {
				 */

				if (DMCore.pGroundingManager.HasPendingRequests()
						|| DMCore.pGroundingManager
								.HasScheduledConceptGroundingRequests())
					DMCore.pGroundingManager.Run();
				// now pop completed
				int iPopped = popCompletedFromExecutionStack();

				Log.d(Const.DMCORE_STREAM_TAG, "Performed grounding [" 
						+ sTurnId+ "]");

				// now while new grounding requests appear, keep running the
				// grounding process
				while ((iPopped > 0)
						&& DMCore.pGroundingManager
								.HasUnprocessedConceptGroundingRequests()) {
					// run it
					DMCore.pGroundingManager.Run();
					// eliminate all the agents that have completed (potentially
					// as a
					// result of the grounding phase) from the execution stack
					iPopped = popCompletedFromExecutionStack();
				}

				Log.d(Const.DMCORE_STREAM_TAG, "Completed grounding on [" 
						+ sTurnId+ "]");
			}

			// now, run the focus analysis process if the core was flagged to
			// do so, and if there are no scheduled grounding activities
			if (bFocusClaimsPhaseFlag) {
				// Analyze the need for a focus shift, and resolve it if
				// necessary
				if (assembleFocusClaims() != 0)
					resolveFocusShift();
				// reset the flag
				bFocusClaimsPhaseFlag = false;
			}

			// if the execution stack is empty now, break the loop
			if (esExecutionStack.isEmpty()||DMCore.bForceExit)
				break;

			// grab the first (executable) dialog agent from the stack
			CDialogAgent pdaAgentInFocus = GetAgentInFocus();

			// check that we found a proper one (if there's nothing else to be
			// executed, we're done)
			if (pdaAgentInFocus == null)
				break;

			// if the floor is not free (or we're still waiting for
			// notifications),
			// do not execute
			// agents that require the floor, just wait for the next event
			if (pdaAgentInFocus.RequiresFloor()
					&& !((GetFloorStatus() == TFloorStatus.fsFree) && true))
			// (DMCore.pOutputManager.GetPromptsWaitingForNotification() ==
			// "")))
			{
				AcquireNextEvent();
				continue;
			}

			// and execute it
			Log.d(Const.DMCORE_STREAM_TAG, "Executing dialog agent "
					+ pdaAgentInFocus.GetName() + " [" + sTurnId + "]");

			// mark the time it was executed
			ehExecutionHistory.get(esExecutionStack.get(0).iEHIndex).vtExecutionTimes
					.add(Utils.GetTime());

			// execute it
			TDialogExecuteReturnCode dercResult = pdaAgentInFocus.Execute();

			while(!pdaAgentInFocus.bOutputCompleted){
				Log.e(Const.DMCORE_STREAM_TAG,"Output has not been completed");
			}
			
			if (pdaAgentInFocus.GetDialogName().equals("RequestDate")){
				// If CWelcome agent completed, the background 
				// should change to activity_output
				
				// Handler send emptyMessage
				MainHandler.sendEmptyMessage(Const.CHANGEBACKGROUND);
				
			}
			ehExecutionHistory.get(esExecutionStack.get(0).iEHIndex).bExecuted = true;

			// and now analyze the return
			switch (dercResult) {
			case dercContinueExecution:
				// continue the execution
				break;

			case dercFinishDialog:
				// finish the dialog
				Log.d(Const.DMCORE_STREAM_TAG,
						"Dialog Task Execution completed. Dialog finished");
				return;

			case dercFinishDialogAndCloseSession:
				// tell the hub to close the session
				Log.d(Const.DMCORE_STREAM_TAG, "Sending close_session to the hub");
				Utils.DMI_SendEndSession();
				// finish the dialog
				Log.d(Const.DMCORE_STREAM_TAG,
						"Dialog Task Execution completed. Dialog finished");
				return;

			case dercRestartDialog:
				// call the start over routine
				StartOver();
				break;

			case dercYieldFloor:

				// gives the floor to the user
				SetFloorStatus(TFloorStatus.fsUser);

				// wait for the next event
				AcquireNextEvent();
				break;

			case dercTakeFloor:

				// gives the floor to the system
				SetFloorStatus(TFloorStatus.fsSystem);

				// wait for the next event
				AcquireNextEvent();
				break;

			case dercWaitForEvent:

				// wait for the next event
				AcquireNextEvent();
				break;
			}
		}

		// tell the hub to close the session
		Log.d(Const.DMCORE_STREAM_TAG, "Sending close_session to the hub");
		// Close the dialog & destroy the dialog tree
		CloseDialog();
		// Declare in Utils.java
		Utils.DMI_SendEndSession();
		Log.d(Const.DMCORE_STREAM_TAG,
				"Dialog Task Execution completed. Dialog finished.");
	}

	// -----------------------------------------------------------------------------
	// A: Waits for and processes the next real-world event
	// -----------------------------------------------------------------------------
	public void AcquireNextEvent() {

		DMCore.pInteractionEventManager.WaitForEvent();

		// Unqueue event
		CInteractionEvent pieEvent = DMCore.pInteractionEventManager
				.GetNextEvent();

		// try and bind concepts
		TBindingsDescr bdBindings = new TBindingsDescr();
		bindConcepts(bdBindings);
		// add the binding results to history
		bhBindingHistory.add(bdBindings);

		// Set the bindings index on the focused agent
		GetAgentInFocus().SetLastBindingsIndex(bhBindingHistory.size() - 1);

		// signal the need for a focus claims phase
		SignalFocusClaimsPhase(true);

		Log.d(Const.DMCORE_STREAM_TAG, "Acquired new " + pieEvent.GetType()
				+ " event.");

		// update the floor status if the event specifies it
		if (pieEvent.HasProperty("[floor_status]"))
			SetFloorStatus(StringToFloorStatus(pieEvent
					.GetStringProperty("[floor_status]")));

		// Process event
		if (pieEvent.GetType() == Const.IET_USER_UTT_START) {

		} else if (pieEvent.GetType() == Const.IET_USER_UTT_END
				|| pieEvent.GetType() == Const.IET_GUI) {

			if (pieEvent.IsComplete()) {
				// Set the last input on the focused agent
				GetAgentInFocus().SetLastInputIndex(iTurnNumber);
				GetAgentInFocus().IncrementTurnsInFocusCounter();

				iTurnNumber++;

				// signal the need for a turn grounding
				DMCore.pGroundingManager.RequestTurnGrounding(true);
				DMCore.pStateManager.UpdateState();

				Log.d(Const.DMCORE_STREAM_TAG, "Processed new input [User:"
						+ pieEvent.GetStringProperty("[uttid]") + "]");

				return;
			}

		} else if (pieEvent.GetType() == Const.IET_SYSTEM_UTT_START) {

			// sends notification information to the OutputManager
			/*
			 * DMCore.pOutputManager.PreliminaryNotify(pieEvent.GetIntProperty(
			 * "[utt_count]"), pieEvent.GetStringProperty("[tagged_prompt]"));
			 */

			Log.d(Const.DMCORE_STREAM_TAG,
					"Processed preliminary output notification.");
		} else if (pieEvent.GetType() == Const.IET_SYSTEM_UTT_END) {

			// sends notification information to the OutputManager
			/*
			 * DMCore.pOutputManager.Notify(pieEvent.GetIntProperty("[utt_count]"
			 * ), pieEvent.GetIntProperty("[bargein_pos]"),
			 * pieEvent.GetStringProperty("[conveyance]"),
			 * pieEvent.GetStringProperty("[tagged_prompt]"));
			 */

			Log.d(Const.DMCORE_STREAM_TAG, "Processed output notification.");
		} else if (pieEvent.GetType() == Const.IET_SYSTEM_UTT_CANCELED) {

			// sends notification information to the OutputManager
			/*
			 * DMCore.pOutputManager.Notify(pieEvent.GetIntProperty("[utt_count]"
			 * ), 0, "", "");
			 */

			Log.d(Const.DMCORE_STREAM_TAG, "Output cancel notification processed.");
		} else if (pieEvent.GetType() == Const.IET_DIALOG_STATE_CHANGE) {

			DMCore.pStateManager.UpdateState();
			// DMCore.pStateManager.BroadcastState();

		} else {
		}
	}

	// -----------------------------------------------------------------------------
	// D: Focus Claims functions
	// -----------------------------------------------------------------------------
	// D: assembles a list of focus claims, and returns the size of that list
	public int assembleFocusClaims() {
		Log.d(Const.DMCORE_STREAM_TAG, "Focus Claims Assembly Phase initiated.");

		// gather the focus claims, starting with the root of the dialog task
		// tree
		int iClaims = 0;
		ArrayList<TFocusClaim> fclTempFocusClaims = new ArrayList<TFocusClaim>();
		iClaims = DMCore.pDTTManager.GetDialogTaskTreeRoot()
				.DeclareFocusClaims(fclTempFocusClaims);

		// log the list of claiming agents
		String sLogString;
		if (fclTempFocusClaims.size() == 0)
			sLogString = "0 agent(s) claiming focus.";
		else
			sLogString = fclTempFocusClaims.size()
					+ " agent(s) claiming focus (dumped below):\n";
		for (int i = 0; i < fclTempFocusClaims.size(); i++)
			sLogString += "  " + fclTempFocusClaims.get(i).sAgentName + "\n";
		Log.d(Const.DMCORE_STREAM_TAG, sLogString);

		// now prune the claims of agents that have their completion criteria
		// satisfied
		Log.d(Const.DMCORE_STREAM_TAG, "Pruning Focus Claims list.");
		// check if we undergoing some grounding action
		boolean bDuringGrounding = DMCore.pGroundingManager
				.HasScheduledConceptGroundingRequests();
		int iClaimsEliminated = 0;
		fclFocusClaims.clear();
		sLogString = "";
		for (int i = 0; i < fclTempFocusClaims.size(); i++) {
			CDialogAgent pdaFocusClaimingAgent = (CDialogAgent) CRegistry.AgentsRegistry
					.getAgentGivenName(fclTempFocusClaims.get(i).sAgentName);
			if (pdaFocusClaimingAgent.SuccessCriteriaSatisfied()
					|| pdaFocusClaimingAgent.FailureCriteriaSatisfied()
					|| AgentIsActive(pdaFocusClaimingAgent)
					|| (!fclTempFocusClaims.get(i).bClaimDuringGrounding && bDuringGrounding)) {
				// eliminate the agent from the list of agents claiming focus
				// if they already have the success criteria satisfied or if
				// they are already in focus, or if they cannot trigger during
				// grounding
				iClaimsEliminated++;
				// and mark it in the log String
				sLogString += "  " + fclTempFocusClaims.get(i).sAgentName
						+ "\n";
			} else {
				// o/w add it to the real list of claims
				fclFocusClaims.add(fclTempFocusClaims.get(i));
			}
		}
		// finally, add the prefix for the log String of eliminated agents
		if (iClaimsEliminated == 0)
			sLogString = "0 agents(s) eliminated from the focus claims list.";
		else
			sLogString = iClaimsEliminated
					+ " agent(s) eliminated from the focus claims list "
					+ "(dumped below):\n" + sLogString;

		Log.d(Const.DMCORE_STREAM_TAG, sLogString);

		Log.d(Const.DMCORE_STREAM_TAG, "Focus Claims Assembly Phase completed " + "("
				+ iClaims + " claim(s)).");
		return iClaims;
	}

	// D: resolves focus shifts
	public void resolveFocusShift() {

		// send out a warning if we have a multiple focus shift
		if (fclFocusClaims.size() > 1) {
			String sMessage = "Ambiguous focus shift (claiming agents dump below).\n";
			for (int i = 0; i < fclFocusClaims.size(); i++)
				sMessage += fclFocusClaims.get(i).sAgentName + "\n";
			Log.w(Const.DMCORE_STREAM_TAG, sMessage);
		}
		;

		// put the agents on the stack
		for (int i = 0; i < fclFocusClaims.size(); i++) {
			String sClaimingAgent = fclFocusClaims.get(i).sAgentName;
			Log.d(Const.DMCORE_STREAM_TAG, "Adding focus-claiming agent "
					+ sClaimingAgent + " on the execution stack.");
			ContinueWith(this,
					(CDialogAgent) CRegistry.AgentsRegistry
							.getAgentGivenName(sClaimingAgent));
		}
	}

	// -----------------------------------------------------------------------------
	//
	// METHOD FOR SIGNALING THE NEED FOR A FOCUS CLAIMS PHASE
	//
	// -----------------------------------------------------------------------------

	// D: signal a focus claims phase - set a flag so that the core agent will
	// run
	// a focus claims phase the next chance it gets
	public void SignalFocusClaimsPhase(boolean bAFocusClaimsPhaseFlag) {
		bFocusClaimsPhaseFlag = bAFocusClaimsPhaseFlag;
	}

	// -----------------------------------------------------------------------------
	//
	// TIMEOUT RELATED METHODS
	//
	// -----------------------------------------------------------------------------
	// D: sets the timeout period
	public void SetTimeoutPeriod(int iATimeoutPeriod) {
		iTimeoutPeriod = iATimeoutPeriod;
		Utils.DMI_SetTimeoutPeriod(iTimeoutPeriod);
	}

	// D: returns the current timeout period
	public int GetTimeoutPeriod() {
		return iTimeoutPeriod;
	}

	// D: sets the default timeout period
	public void SetDefaultTimeoutPeriod(int iADefaultTimeoutPeriod) {
		iDefaultTimeoutPeriod = iADefaultTimeoutPeriod;
	}

	// D: returns the current timeout period
	public int GetDefaultTimeoutPeriod() {
		return iDefaultTimeoutPeriod;
	}

	// -----------------------------------------------------------------------------
	//
	// METHODS FOR ACCESSING THE NONUNDERSTANDING THRESHOLD
	//
	// -----------------------------------------------------------------------------

	// D: sets the nonunderstanding threshold
	public void SetNonunderstandingThreshold(float fANonunderstandingThreshold) {
		fNonunderstandingThreshold = fANonunderstandingThreshold;
	}

	// D: gets the nonunderstanding threshold
	public float GetNonunderstandingThreshold() {
		return fNonunderstandingThreshold;
	}

	// D: sets the default nonunderstanding threshold
	public void SetDefaultNonunderstandingThreshold(float fANonuThreshold) {
		fDefaultNonunderstandingThreshold = fANonuThreshold;
	}

	// D: returns the default nonunderstanding threshold
	public float GetDefaultNonunderstandingThreshold() {
		return fDefaultNonunderstandingThreshold;
	}

	// ---------------------------------------------------------------------
	// METHODS FOR SIGNALING FLOOR CHANGES
	// ---------------------------------------------------------------------

	public void SetFloorStatus(TFloorStatus fsaFloorStatus) {
		Log.d(Const.DMCORE_STREAM_TAG,
				"Set floor status to " + fsaFloorStatus.toString());
		fsFloorStatus = fsaFloorStatus;
	}

	public void SetFloorStatus(String sAFloorStatus) {
		SetFloorStatus(StringToFloorStatus(sAFloorStatus));
	}

	public TFloorStatus GetFloorStatus() {
		return fsFloorStatus;
	}

	public String FloorStatusToString(TFloorStatus fsAFloor) {
		return fsAFloor.toString();
	}

	public TFloorStatus StringToFloorStatus(String sAFloor) {
		for (int i = 0; i < vsFloorStatusLabels.size(); i++) {
			if (vsFloorStatusLabels.get(i).equals(sAFloor))
				return TFloorStatus.valueOf(sAFloor);
		}
		return TFloorStatus.fsUnknown;
	}

	// -----------------------------------------------------------------------------
	//
	// ACCESS TO VARIOUS PRIVATE FIELDS (mostly state information for the
	// State Manager Agent)
	//
	// -----------------------------------------------------------------------------

	// D: returns the number of concepts bound in the last input pass
	public int LastTurnGetConceptsBound() {
		if (bhBindingHistory.size() == 0)
			return -1;
		else {
			int iIndex = bhBindingHistory.size() - 1;
			return bhBindingHistory.get(iIndex).iConceptsBound;

		}
	}

	// D: returns true if the last turn was a non-understanding
	public boolean LastTurnNonUnderstanding() {
		for (int i = bhBindingHistory.size() - 1; i >= 0; i--) {
			if (bhBindingHistory.get(i).sEventType == Const.IET_USER_UTT_END
					|| bhBindingHistory.get(i).sEventType == Const.IET_GUI) {
				if (bhBindingHistory.get(i).bNonUnderstanding)
					return true;
				else
					return false;
			}
		}
		return false;
	}

	// A: returns the number of consecutive non-understandings so far
	public int GetNumberNonUnderstandings() {
		int iNumNonunderstandings = 0;
		for (int i = bhBindingHistory.size() - 1; i >= 0; i--) {
			if (bhBindingHistory.get(i).sEventType == Const.IET_USER_UTT_END
					|| bhBindingHistory.get(i).sEventType == Const.IET_GUI) {
				if (bhBindingHistory.get(i).bNonUnderstanding) {
					iNumNonunderstandings++;
				} else {
					break;
				}
			}
		}
		return iNumNonunderstandings;
	}

	// A: returns the total number of non-understandings in the current dialog
	// so far
	public int GetTotalNumberNonUnderstandings() {
		int iNumNonunderstandings = 0;
		for (int i = 0; i < (int) bhBindingHistory.size(); i++) {
			if (bhBindingHistory.get(i).bNonUnderstanding) {
				iNumNonunderstandings++;
			}
		}
		return iNumNonunderstandings;
	}

	// -----------------------------------------------------------------------------
	//
	// EXECUTION STACK AND HISTORY METHODS
	//
	// -----------------------------------------------------------------------------

	// D: Pushes a new dialog agent on the execution stack
	public void ContinueWith(CAgent paPusher, CDialogAgent pdaDialogAgent) {

		// check that the agent is not already on top of the stack
		if (!esExecutionStack.isEmpty()
				&& (esExecutionStack.get(0).pdaAgent == pdaDialogAgent)) {
			Log.d(Const.DMCORE_STREAM_TAG, "Agent " + pdaDialogAgent.GetName()
					+ " already on top of the "
					+ "execution stack.ContinueWith request ignored.");
			return;
		}
		// add an entry in the history; fill in all the slots
		TExecutionHistoryItem ehi = new TExecutionHistoryItem();
		ehi.sCurrentAgent = pdaDialogAgent.GetName();
		ehi.sCurrentAgentType = pdaDialogAgent.GetType();
		ehi.bScheduled = true;
		ehi.sScheduledBy = paPusher.GetName();
		ehi.timeScheduled = Utils.GetTime();
		ehi.bExecuted = false;
		ehi.bCommitted = false;
		ehi.bCanceled = false;
		ehi.iStateHistoryIndex = -1;
		// ***ehi.timeTerminated = 0;
		ehExecutionHistory.add(ehi);

		// and put it on the stack
		TExecutionStackItem esi = new TExecutionStackItem();
		esi.pdaAgent = pdaDialogAgent;
		esi.iEHIndex = ehExecutionHistory.size() - 1;
		esExecutionStack.add(0, esi);

		// stores the execution index in the agent
		pdaDialogAgent.SetLastExecutionIndex(esi.iEHIndex);

		// signals that the agenda needs to be recomputed
		bAgendaModifiedFlag = true;

		Log.d(Const.DMCORE_STREAM_TAG, "Agent " + ehi.sCurrentAgent.toString()
				+ " added on the execution stack by " + ehi.sScheduledBy);
	}

	// D: Restarts a topic
	/*
	 * public void RestartTopic(CDialogAgent pdaDialogAgent) { // first, locate
	 * the agent TExecutionStack::iterator iPtr; for(iPtr =
	 * esExecutionStack.begin(); iPtr != esExecutionStack.end(); iPtr++) {
	 * if(iPtr.pdaAgent == pdaDialogAgent) break; }
	 * 
	 * // if the agent was nothere in the list, trigger a fatal error if(iPtr ==
	 * esExecutionStack.end()) { Log.w(Const.DMCORE_STREAM_TAG,"Cannot restart the " +
	 * pdaDialogAgent.GetName() +
	 * " agent. Agent not found on execution stack."); return; }
	 * 
	 * Log.d(Const.DMCORE_STREAM_TAG, "Restarting agent %s.",
	 * pdaDialogAgent.GetName());
	 * 
	 * // store the planner of this agent CDialogAgent* pdaScheduler =
	 * (CDialogAgent *)
	 * AgentsRegistry[ehExecutionHistory[iPtr.iEHIndex].sScheduledBy];
	 * 
	 * // now clean it off the execution stack
	 * PopTopicFromExecutionStack(pdaDialogAgent);
	 * 
	 * // reopen it pdaDialogAgent.ReOpen();
	 * 
	 * // and readd it to the stack ContinueWith(pdaScheduler, pdaDialogAgent);
	 * }
	 * 
	 * // D: Registers a custom start over function public void
	 * RegisterCustomStartOver(TCustomStartOverFunct csoAStartOverFunct) {
	 * csoStartOverFunct = csoAStartOverFunct; }
	 */
	
	// L: Close the dialog
	public void CloseDialog(){
		Log.d(Const.DMCORE_STREAM_TAG,"Start to close the dialog...");
		// Clear all the history
		esExecutionStack.clear();
		ehExecutionHistory.clear();
		
		// destroy the dialog task tree
		DMCore.pDTTManager.DestroyDialogTree();
	}
	// D: Restarts the dialog
	public void StartOver() {
		if (csoStartOverFunct == null) {
			// restart the dialog clear the execution stack
			esExecutionStack.clear();
			// destroy the dialog task tree
			DMCore.pDTTManager.DestroyDialogTree();
			// recreate the dialog task tree
			//DMCore.pDTTManager.ReCreateDialogTree();
			// restart the execution by putting the root on the stack
			Log.d(Const.DMCORE_STREAM_TAG, "Restarting Dialog Task execution.");
			ContinueWith(this, DMCore.pDTTManager.GetDialogTaskTreeRoot());

			DMCore.pStateManager.UpdateState();
		} else {
			// (*csoStartOverFunct)();
		}
	}

	// D: Pops a dialog agent from the execution stack, together with all the
	// other agents it has ever planned for execution
	public void popTopicFromExecutionStack(CDialogAgent pdaADialogAgent,
			ArrayList<String> rvsAgentsEliminated) {
		// check for empty stack condition
		if (esExecutionStack.isEmpty()) {
			Log.e(Const.DMCORE_STREAM_TAG,
					"Cannot pop the " + pdaADialogAgent.GetName()
							+ " agent off the execution stack. Stack is empty.");
		}

		// first, locate the agent
		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		TExecutionStackItem iPtr = new TExecutionStackItem();
		while (iterator.hasNext()) {
			iPtr = iterator.next();
			if (iPtr.pdaAgent == pdaADialogAgent)
				break;
		}

		// if the agent was not in the list, trigger a fatal error
		if (!iterator.hasNext()) {
			Log.e(Const.DMCORE_STREAM_TAG,
					"Cannot pop the "
							+ pdaADialogAgent.GetName()
							+ " agent off the execution stack. Agent not found.");
		}

		// the set of eliminated agents
		HashSet<CDialogAgent> sEliminatedAgents = new HashSet<CDialogAgent>();

		// initialize it with the starting agent
		sEliminatedAgents.add(iPtr.pdaAgent);

		// mark the time this agent's execution was terminated
		ehExecutionHistory.get(iPtr.iEHIndex).timeTerminated = Utils.GetTime();

		// call the agent's OnCompletion method
		iPtr.pdaAgent.OnCompletion();

		// and add it to the list of eliminated agents
		rvsAgentsEliminated.add(iPtr.pdaAgent.GetName());

		// eliminate the agent from the stack

		// Changed by @Lilinchuan---------------------------------------
		esExecutionStack.remove(iPtr);
		// Changed-------------------------------------------------------

		// now enter in a loop going through the stack repeatedly until
		// we didn't find anything else to remove
		boolean bFoundAgentToRemove = true;

		while (bFoundAgentToRemove) {

			bFoundAgentToRemove = false;
			iterator = esExecutionStack.iterator();
			// now traverse the stack
			while (iterator.hasNext()) {
				iPtr = iterator.next();
				if (iPtr.iEHIndex == 0) {
					break;
				}
				// check to see who planned the current agent
				CDialogAgent pdaScheduler = (CDialogAgent) CRegistry.AgentsRegistry
						.getAgentPointer(ehExecutionHistory.get(iPtr.iEHIndex).sScheduledBy);
				if (sEliminatedAgents.contains(pdaScheduler)) {
					// then we need to eliminate this one; so first add it to
					// the
					// list of eliminated agents
					sEliminatedAgents.add(iPtr.pdaAgent);
					// mark the time this agent execution was terminated
					ehExecutionHistory.get(iPtr.iEHIndex).timeTerminated = Utils
							.GetTime();
					// call the agent's OnCompletion method
					iPtr.pdaAgent.OnCompletion();
					// and add it to the list of eliminated agents
					rvsAgentsEliminated.add(iPtr.pdaAgent.GetName());
					// eliminate the agent from the stack
					esExecutionStack.remove(iPtr);
					// set found one to true
					bFoundAgentToRemove = true;
					// and break the for loop
					break;
				}
			}
		}

		bAgendaModifiedFlag = true;
	}

	// D: Pops all the completed agents (and all the agents they have ever
	// planned
	// for off the execution stack
	public int popCompletedFromExecutionStack() {
		boolean bFoundCompleted; // indicates if completed agents were still
									// found

		ArrayList<String> vsAgentsEliminated = new ArrayList<String>();
		// when no more completed agents can be found, return
		do {
			Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
			bFoundCompleted = false;
			TExecutionStackItem iPtr = null;
			// go through the execution stack
			while (iterator.hasNext()) {
				iPtr = iterator.next();
				// if you find an agent that has completed
				if (iPtr.pdaAgent.HasCompleted()) {
					// pop it off the execution stack
					popTopicFromExecutionStack(iPtr.pdaAgent,
							vsAgentsEliminated);
					bFoundCompleted = true;
					break;
				}
			}
		} while (bFoundCompleted);

		// when no more completed agents can be found, log and return
		if (vsAgentsEliminated.size() != 0) {
			String sAgents = "";
			for (int i = 0; i < vsAgentsEliminated.size(); i++)
				sAgents += (vsAgentsEliminated.get(i) + "\n");

			// sAgents = TrimRight(sAgents);

			Log.d(Const.DMCORE_STREAM_TAG,
					"Eliminated "
							+ vsAgentsEliminated.size()
							+ " completed agent(s) (dumped below) from the execution stack.\n"
							+ sAgents);
		}

		// return the number of agents eliminated
		return vsAgentsEliminated.size();
	}

	// D: Returns true if the specified agent is in focus
	public boolean AgentIsInFocus(CDialogAgent pdaDialogAgent) {

		// if it's not executable, return false
		if (!pdaDialogAgent.IsExecutable())
			return false;

		// if it's not a task agent
		if (!pdaDialogAgent.IsDTSAgent()) {
			return GetAgentInFocus() == pdaDialogAgent;
		}

		// if it is a task agent then get the task agent in focus and check
		// against that
		return GetDTSAgentInFocus() == pdaDialogAgent;
	}

	// D: Returns the agent on top of the execution stack
	public CDialogAgent GetAgentInFocus() {
		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		TExecutionStackItem iPtr = new TExecutionStackItem();
		while (iterator.hasNext()) {
			iPtr = iterator.next();
			if (iPtr.pdaAgent.IsExecutable()) {
				return iPtr.pdaAgent;
			}
		}
		// o/w return null
		return null;
	}

	// D: Returns the task agent closest to the top of the execution stack
	public CDialogAgent GetDTSAgentInFocus() {
		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		TExecutionStackItem iPtr = new TExecutionStackItem();
		while (iterator.hasNext()) {
			iPtr = iterator.next();
			if (iPtr.pdaAgent.IsDTSAgent() && iPtr.pdaAgent.IsExecutable())
				return iPtr.pdaAgent;
		}
		// o/w if no task agent found, return null
		return null;
	}

	// D: assembles the expectation agenda
	public void assembleExpectationAgenda() {

		Log.d(Const.DMCORE_STREAM_TAG, "Expectation Agenda Assembly Phase initiated.");

		// first collect and compile the expectation agenda
		compileExpectationAgenda();

		// then enforce the binding policies as specified on each level
		enforceBindingPolicies();

		// dump agenda to the log
		Log.d(Const.EXPECTATIONAGENDA_STREAM_TAG,
				"Concept expectation agenda dumped below:"
						+ expectationAgendaToString());

		Log.d(Const.DMCORE_STREAM_TAG,
				"Expectation Agenda Assembly Phase completed ("
						+ eaAgenda.vCompiledExpectations.size() + " levels).");
	}

	// D: gathers the expectations and compiles them in an fast accessible
	// form

	public void compileExpectationAgenda() {

		// log the activity
		Log.d(Const.DMCORE_STREAM_TAG, "Compiling Expectation Agenda ...");

		// first clear up the last agenda
		eaAgenda.celSystemExpectations.clear();
		eaAgenda.vCompiledExpectations.clear();

		// get the list of system expectations. To do this, we traverse
		// the execution stack, and add expectations from all the agents, each
		// on the appropriate level; also keep track of the expectations
		// that are already declared so as not to duplicate them by this
		// traversal
		int iLevel = 0;
		// the set of agents already seen on the previous levels
		Set<CDialogAgent> setPreviouslySeenAgents = new HashSet<CDialogAgent>(); 
		// the set of agents seen on the current level
		Set<CDialogAgent> setCurrentlySeenAgents = new HashSet<CDialogAgent>(); 

		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		TExecutionStackItem iPtr = new TExecutionStackItem();
		while (iterator.hasNext()) {
			iPtr = iterator.next();
			// remember how big the system expectation agenda was so far
			int iStartIndex = eaAgenda.celSystemExpectations.size();

			// gather expectations of the agent on the stack indicated by iPtr
			iPtr.pdaAgent.DeclareExpectations(eaAgenda.celSystemExpectations);

			// now go thourgh those new expectations and compile them (create
			// the corresponding entry into the vCompiledExpectations array)
			TCompiledExpectationLevel celLevel = new TCompiledExpectationLevel();
			// set the agent that generated this level
			celLevel.pdaGenerator = iPtr.pdaAgent;
			for (int i = iStartIndex; i < eaAgenda.celSystemExpectations.size(); i++) {

				// check that the agent was not already seen on the previous
				// level (in this case, avoid duplicating its expectation)
				if (setPreviouslySeenAgents
						.contains(eaAgenda.celSystemExpectations.get(i).pDialogAgent)) {
					continue;
				}

				// insert this agent in the list of currently seen agents
				setCurrentlySeenAgents.add(eaAgenda.celSystemExpectations
						.get(i).pDialogAgent);

				String sSlotExpected = eaAgenda.celSystemExpectations.get(i).sGrammarExpectation;
				if (celLevel.mapCE.containsKey(sSlotExpected)) {
					// if this grammar slot is already expected at this level
					// just add to the vector of pointers
					ArrayList<Integer> rvIndices = new ArrayList<Integer>();
					rvIndices = celLevel.mapCE.get(sSlotExpected);
					rvIndices.add(i);
				} else {
					// if the concept is NOT already expected at this level
					// then add it to the hash of compiled expectations
					ArrayList<Integer> ivTemp = new ArrayList<Integer>();
					ivTemp.add(i);
					celLevel.mapCE.put(sSlotExpected, ivTemp);
				}
			}

			// finally, we have assembled and compiled this level of
			// expectations,
			// push it on the array,
			eaAgenda.vCompiledExpectations.add(celLevel);

			// update the set of already seen agents
			setPreviouslySeenAgents.addAll(setCurrentlySeenAgents);

			// and move to the next level
			iLevel++;
		}

		// log the activity
		Log.d(Const.DMCORE_STREAM_TAG, "Compiling Expectation Agenda completed.");
	}

	// D: goes through the compiled agenda, and modifies it according to the
	// binding policies as specified by each level's generator agents
	public void enforceBindingPolicies() {

		// log the activity
		Log.d(Const.DMCORE_STREAM_TAG, "Enforcing binding policies ...");

		// at this point, this only consists of blocking the upper levels if a
		// Const.WITHIN_TOPIC_ONLY policy is detected
		for (int i = 0; i < eaAgenda.vCompiledExpectations.size(); i++) {
			// get the binding policy for this level
			String sBindingPolicy = eaAgenda.vCompiledExpectations.get(i).pdaGenerator
					.DeclareBindingPolicy();
			if (sBindingPolicy.equals(Const.WITHIN_TOPIC_ONLY)) {
				// if Const.WITHIN_TOPIC_ONLY, then all the expectations from the
				// upper
				// levels of the agenda are disabled
				for (int l = i + 1; l < eaAgenda.vCompiledExpectations.size(); l++) {
					// go through the whole level and disable all expectations

					Set<Map.Entry<String, ArrayList<Integer>>> allSet = eaAgenda.vCompiledExpectations
							.get(l).mapCE.entrySet();
					Iterator<Map.Entry<String, ArrayList<Integer>>> iterator = allSet
							.iterator();
					Map.Entry<String, ArrayList<Integer>> iPtr;
					while (iterator.hasNext()) {
						iPtr = iterator.next();
						// access the indices
						ArrayList<Integer> rivTemp = iPtr.getValue();
						for (int ii = 0; ii < rivTemp.size(); ii++) {
							int iIndex = rivTemp.get(ii);
							// don't disable it if it's a *-type expectation
							if (eaAgenda.celSystemExpectations.get(iIndex).sExpectationType != "*") {
								eaAgenda.celSystemExpectations.get(iIndex).bDisabled = true;
								eaAgenda.celSystemExpectations.get(iIndex).sReasonDisabled = "within-topic binding policy"; // ***
																															// add
																															// on
																															// what
							}
						}
					}
				}
				// break the for loop since it already doesn't matter what the
				// policy is on upper contexts
				break;
			}
		}

		// log the activity
		Log.d(Const.DMCORE_STREAM_TAG, "Enforcing binding policies completed.");
	}

	// D: generates a String representation of the expectation agenda
	// this String is used so far only for logging purposes
	public String expectationAgendaToString() {
		String sResult = "";
		// go through all the levels of the agenda
		for (int l = 0; l < eaAgenda.vCompiledExpectations.size(); l++) {
			sResult += "\n Level "
					+ l
					+ ": generated by "
					+ eaAgenda.vCompiledExpectations.get(l).pdaGenerator
							.GetName();
			Set<Map.Entry<String, ArrayList<Integer>>> allSet = eaAgenda.vCompiledExpectations
					.get(l).mapCE.entrySet();
			Iterator<Map.Entry<String, ArrayList<Integer>>> iterator = allSet
					.iterator();
			Map.Entry<String, ArrayList<Integer>> iPtr;
			// iterate through the compiled expectations from that level
			while (iterator.hasNext()) {
				iPtr = iterator.next();
				String sSlotExpected = iPtr.getKey();
				ArrayList<Integer> rvIndices = new ArrayList<Integer>();
				rvIndices = iPtr.getValue();
				// convert expectations to String description
				for (int i = 0; i < rvIndices.size(); i++) {
					TConceptExpectation rceExpectation = eaAgenda.celSystemExpectations
							.get(rvIndices.get(i));
					sResult += (rceExpectation.bDisabled) ? "\n  X " : "\n  O ";
					sResult += rceExpectation.sGrammarExpectation + " . ("
							+ rceExpectation.pDialogAgent.GetName() + ")"
							+ rceExpectation.sConceptName;
					if (rceExpectation.bDisabled) {
						sResult += " [X-" + rceExpectation.sReasonDisabled
								+ "]";
					}
				}
			}
		}
		// finally, return the String
		return sResult;
	}

	// D: Returns a description of the system action taken on a particular
	// concept
	// (***** this function will need to be elaborated more *****)
	public TSystemActionOnConcept GetSystemActionOnConcept(CConcept pConcept) {
		TSystemActionOnConcept saoc = new TSystemActionOnConcept();

		// check if the concept is among the requested concepts
		if (saSystemAction.setcpRequests.contains(pConcept)) {
			// then we have a request
			saoc.sSystemAction = Const.SA_REQUEST;
		}

		// check if the concept is among the explicitly confirmed concepts
		else if (saSystemAction.setcpExplicitConfirms.contains(pConcept)) {
			// then we have an explicit confirm
			saoc.sSystemAction = Const.SA_EXPL_CONF;
		}

		// check if the concept is among the implicitly confirmed concepts
		else if (saSystemAction.setcpImplicitConfirms.contains(pConcept)) {
			// then we have an implicit confirm
			saoc.sSystemAction = Const.SA_IMPL_CONF;
		}

		// check if the concept is among the unplanned implicitly confirmed
		// concepts
		else if (saSystemAction.setcpUnplannedImplicitConfirms
				.contains(pConcept)) {
			// then we have an implicit confirm
			saoc.sSystemAction = Const.SA_UNPLANNED_IMPL_CONF;
		}

		// check if the concept is among the implicitly confirmed concepts
		else {
			// then we have an "other" type request
			saoc.sSystemAction = Const.SA_OTHER;
		}

		// return
		return saoc;
	}

	// A: generates a String representation of the current agenda
	public String expectationAgendaToBroadcastString() {
		return expectationAgendaToBroadcastString(eaAgenda);
	}

	// D: generates a String representation of the expectation agenda
	// that is used to broadcast it to the outside world
	public String expectationAgendaToBroadcastString(
			TExpectationAgenda eaBAgenda) {
		String sResult = "";
		HashMap<String, String> s2sAllOpenGrammarExpectations = new HashMap<String, String>();
		// go through all the levels of the agenda
		for (int l = 0; l < eaBAgenda.vCompiledExpectations.size(); l++) {
			sResult += "\n" + l + ":";
			Map.Entry<String, ArrayList<Integer>> iPtr = null;
			Iterator<Map.Entry<String, ArrayList<Integer>>> iterator = eaBAgenda.vCompiledExpectations
					.get(l).mapCE.entrySet().iterator();
			// iterate through the compiled expectations from that level
			while (iterator.hasNext()) {
				iPtr = iterator.next();
				String sSlotExpected = iPtr.getKey();
				ArrayList<Integer> rvIndices = iPtr.getValue();

				ArrayList<Integer> vOpenIndices = new ArrayList<Integer>();
				HashSet<CConcept> scpOpenConcepts = new HashSet<CConcept>();
				ArrayList<Integer> vClosedIndices = new ArrayList<Integer>();
				HashSet<CConcept> scpClosedConcepts = new HashSet<CConcept>();

				for (int i = 0; i < rvIndices.size(); i++) {
					TConceptExpectation rceExpectation = eaBAgenda.celSystemExpectations
							.get(rvIndices.get(i));
					// determine the concept under consideration
					CConcept pConcept = rceExpectation.pDialogAgent
							.getConceptFromPath(rceExpectation.sConceptName);

					// test that the expectation is not disabled
					if (!eaBAgenda.celSystemExpectations.get(rvIndices.get(i)).bDisabled) {
						if (!scpOpenConcepts.contains(pConcept)) {
							// add it to the open indices list
							vOpenIndices.add(rvIndices.get(i));
							// add the concept to the open concepts list
							scpOpenConcepts.add(pConcept);
							// if by any chance it's already in the closed
							// concepts,
							Iterator<CConcept> iterator2 = scpClosedConcepts
									.iterator();
							CConcept iPtr2 = null;
							if (iterator2.hasNext()) {
								iPtr2 = iterator2.next();
								// remove it from there
								scpClosedConcepts.remove(iPtr);
							}
						}
					} else {
						// o/w if the expectation is disabled
						if ((!scpClosedConcepts.contains(pConcept))
								&& (!scpOpenConcepts.contains(pConcept))) {
							// add it to the closed indices list
							vClosedIndices.add(rvIndices.get(i));
							// add the concept to the closed concepts list
							scpClosedConcepts.add(pConcept);
						}
					}
				}

				// now add the first one in the open indices, if there is any
				// in there

				if (vOpenIndices.size() > 0) {
					TConceptExpectation rceExpectation = eaBAgenda.celSystemExpectations
							.get(vOpenIndices.get(0));
					sResult += "\n"; // (air)
					sResult += "O" + rceExpectation.sGrammarExpectation;

					sResult += (rceExpectation.bmBindMethod == TBindMethod.bmExplicitValue) ? "V,"
							: "S,";
				}

				// finally, add all the blocked ones
				for (int i = 0; i < vClosedIndices.size(); i++) {
					TConceptExpectation rceExpectation = eaBAgenda.celSystemExpectations
							.get(vClosedIndices.get(i));
					sResult += "\n"; // (air)
					sResult += "X" + rceExpectation.sGrammarExpectation;
					sResult += (rceExpectation.bmBindMethod == TBindMethod.bmExplicitValue) ? "V,"
							: "S,";
				}
			}
			// cut the last comma
			sResult = Utils.TrimRight(sResult, ",");
		}
		// finally, return the String
		return Utils.Trim(sResult, "\n");
	}

	// D: generates a String representation of the bindings description
	public String bindingsDescrToString(TBindingsDescr rbdBindings) {
		String sResult="";
		
		// go through all the attempted bindings
	  for(int i = 0; i < rbdBindings.vbBindings.size(); i++) {
	      if(rbdBindings.vbBindings.get(i).bBlocked)
	          sResult += "Fail:    Level."+rbdBindings.vbBindings.get(i).iLevel
	          +"+t"+rbdBindings.vbBindings.get(i).sGrammarExpectation
	          +".("+rbdBindings.vbBindings.get(i).sAgentName+")"
	          +rbdBindings.vbBindings.get(i).sConceptName+" ["
	          +rbdBindings.vbBindings.get(i).sReasonDisabled+"]\n";
	      else 
			    sResult += "Success: Level."+rbdBindings.vbBindings.get(i).iLevel
			    +"+t"+rbdBindings.vbBindings.get(i).sGrammarExpectation
			    +"("+rbdBindings.vbBindings.get(i).sValue+"|"
			    +rbdBindings.vbBindings.get(i).fConfidence+").("
			    +rbdBindings.vbBindings.get(i).sAgentName+")"
			    + rbdBindings.vbBindings.get(i).sConceptName+"\n";
	  }
		// go through all the forced updates
		for(int i = 0; i < rbdBindings.vfcuForcedUpdates.size(); i++) {
			if(rbdBindings.vfcuForcedUpdates.get(i).iType == Const.FCU_EXPLICIT_CONFIRM){
				String bunderstand = 
						rbdBindings.vfcuForcedUpdates.get(i).bUnderstanding?
						"understanding":"non-understanding";
				sResult += "Forced update [explicit_confirm] on "
						+rbdBindings.vfcuForcedUpdates.get(i).sConceptName
						+": "+bunderstand; 
			} 
		}
	
		// finally, return
		return sResult;
		
	}

	// D: binds the concepts from the input parse into the agenda according to
	// the
	// current interaction policy, then returns a charaterization of binding
	// success/failure in bhiResults
	public void bindConcepts(TBindingsDescr rbdBindings) {
	
		Log.d(Const.DMCORE_STREAM_TAG, "Concepts Binding Phase initiated.");
		
		// initialize to zero the number of concepts bound and blocked
		rbdBindings.sEventType = DMCore.pInteractionEventManager.
				GetLastEvent().GetType();
		rbdBindings.iConceptsBlocked = 0;
		rbdBindings.iConceptsBound = 0;
		rbdBindings.iSlotsMatched = 0;
		rbdBindings.iSlotsBlocked = 0;
	
		// hash which stores the slots that matched and how many times they did
		// so
		HashMap<String, Integer> msiSlotsMatched = new HashMap<String,Integer>();
	
		// hash which stores the slots that were blocked and how many times they were
		// blocked
		HashMap<String, Integer> msiSlotsBlocked =new HashMap<String,Integer>();
	  
		// go through each concept expectation level and try to bind things
		for(int iLevel = 0; 
			iLevel < eaAgenda.vCompiledExpectations.size(); 
			iLevel++) {
	
			// go through the hash of expected slots at that level
			Set<Map.Entry<String, ArrayList<Integer>>> allSet=
					eaAgenda.vCompiledExpectations.get(iLevel).mapCE.entrySet();
			Iterator<Map.Entry<String, ArrayList<Integer>>> iterator = 
					allSet.iterator();
			Map.Entry<String, ArrayList<Integer>> iPtr = null;
			while(iterator.hasNext()) {
				iPtr = iterator.next();
				String sSlotExpected = iPtr.getKey();	// the grammar slot expected
				ArrayList<Integer> rvIndices = iPtr.getValue();	// indices in the system 
														//   expectation list
	
				// if the slot actually exists in the parse, then try to bind it
				if(DMCore.pInteractionEventManager.LastEventMatches(sSlotExpected)) {
					
	
					Log.d(Const.DMCORE_STREAM_TAG,"Event matches "+sSlotExpected+".");
	
					// go through the array of indices and construct another array
					// which contains only the indices of "open" expectations, 
	                // excluding any expectations that redundanly match to the same 
	                // concept
					ArrayList<Integer> vOpenIndices = new ArrayList<Integer>();
	                HashSet<CConcept> scpOpenConcepts=new HashSet<CConcept>();
	
	                // also construct another array which contains the indices of 
	                // "closed" expectations, excluding any expectations that 
	                // redundantly match the same concept
	                ArrayList<Integer> vClosedIndices=new ArrayList<Integer>();
	                HashSet<CConcept> scpClosedConcepts=new HashSet<CConcept>();
	
	                for(int i = 0; i < rvIndices.size(); i++) {
						// determine the concept under consideration
	                  CConcept pConcept =eaAgenda.celSystemExpectations.get(rvIndices.get(i))
	                		  .pDialogAgent.getConceptFromPath(
	                          eaAgenda.celSystemExpectations.get(
	                        		  rvIndices.get(i)).sConceptName);
	                  
	                  // test that the expectation is not disabled
	                  if(!eaAgenda.celSystemExpectations.get(rvIndices.get(i)).bDisabled) {
	                      if(!scpOpenConcepts.contains(pConcept)) {
		                      // add it to the open indices list
							  vOpenIndices.add(rvIndices.get(i));
	                          // add the concept to the open concepts list
	                          scpOpenConcepts.add(pConcept);
	                          // if by any chance it's already in the closed concepts, 
	                          if(scpClosedConcepts.contains(pConcept)) {
	                              // remove it from there
	                              scpClosedConcepts.remove(pConcept);
	                          }
	                      }
	                  } else {
	                      // o/w if the expectation is disabled
	                      if((!scpClosedConcepts.contains(pConcept)) &&
	                         (!scpOpenConcepts.contains(pConcept))) {
		                        // add it to the closed indices list
							    vClosedIndices.add(rvIndices.get(i));
							    // add the concept to the closed concepts list
							    scpClosedConcepts.add(pConcept);
	                      }                        
	                  }
					}
	
					// the slot value to be bound
					String sSlotValue="";
	
					// and the confidence score
					float fConfidence = DMCore.pInteractionEventManager.GetLastEventConfidence();
	
					if(vOpenIndices.size() > 0) {
	                  // check that the confidence is strictly 
	                  // above the current nonunderstanding threshold
	                  if(fConfidence > fNonunderstandingThreshold) {
						    // check for multiple bindings on a level
						    if(vOpenIndices.size() > 1) {
							    // if there are multiple bindings possible, log that 
	                          // as a warning for now *** later we need to deal with 
	                          // this by adding disambiguation agencies
							    String sAgents="";
							    for(int i=0; i < vOpenIndices.size(); i++) {
								    sAgents +=eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                                  pDialogAgent.GetName() +" tries to bind to " + 
									    eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                                  sConceptName + "\n";
							    }
							    Log.w(Const.DMCORE_STREAM_TAG,"Multiple binding for grammar "+
								    "concept "+sSlotExpected+". Agents dumped below. Binding "+
								    "performed just for the first agent.\n"+sAgents);
						    }
	
						    // now bind the grammar concept to the first agent expecting 
						    // this slot; obtain the value for that grammar slot
						    sSlotValue = 
							    DMCore.pInteractionEventManager.GetValueForExpectation(sSlotExpected);
	
						    // L: Update
						    // do the actual concept binding
						    performConceptBinding(sSlotExpected, sSlotValue, 
	                          fConfidence, vOpenIndices.get(0), 
								DMCore.pInteractionEventManager.LastEventIsComplete());
	
						    // now that we've bound at this level, invalidate this 
						    // expected slot on all the other levels
						    for(int iOtherLevel = iLevel + 1; 
							    iOtherLevel < eaAgenda.vCompiledExpectations.size(); 
							    iOtherLevel++) {					
							    eaAgenda.vCompiledExpectations.get(iOtherLevel).
	                              mapCE.remove(sSlotExpected);
						    }
	                  } else {
	                      // o/w the confidence is below the nonunderstanding
	                      // threshold, so we will reject this utterance by
	                      // basically not binding anything
	                  }
					}
	
	              // write the open binding description (only for the first concept, 
	              // the one that actually bound)
	              for(int i = 0; i < vOpenIndices.size(); i++) {
	                  if(i == 0) {
	                  // check that the confidence is strictly 
	                  // above the current nonunderstanding threshold
	                  if(fConfidence > fNonunderstandingThreshold) {
	                          TBinding bBinding = new TBinding();
	                          bBinding.bBlocked = false;
	                          bBinding.iLevel = iLevel;
	                          bBinding.fConfidence = fConfidence;
	                          bBinding.sAgentName = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              pDialogAgent.GetName();
	                          bBinding.sConceptName = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              sConceptName;
	                          bBinding.sGrammarExpectation = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              sGrammarExpectation;
	                          bBinding.sValue = sSlotValue;
	                          rbdBindings.vbBindings.add(bBinding);
	                          rbdBindings.iConceptsBound++;
							    // add the slot to the list of matched slots 
							    msiSlotsMatched.put(bBinding.sGrammarExpectation,1);
							    // in case we find it in the blocked slots, (it could have gotten
							    // there on an earlier level) delete it from there
							    if((msiSlotsBlocked.containsKey(bBinding.sGrammarExpectation))) {
								    msiSlotsBlocked.remove(iPtr);
							    }
	                      } else {
	                          // o/w if the confidence is not above the threshold
	                          TBinding bBlockedBinding = new TBinding();
	                          bBlockedBinding.bBlocked = true;
	                          bBlockedBinding.iLevel = iLevel;
	                          bBlockedBinding.fConfidence = fConfidence;
	                          bBlockedBinding.sAgentName = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              pDialogAgent.GetName();
	                          bBlockedBinding.sConceptName = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              sConceptName;
	                          bBlockedBinding.sGrammarExpectation = 
	                              eaAgenda.celSystemExpectations.get(vOpenIndices.get(i)).
	                              sGrammarExpectation;
	                          bBlockedBinding.sReasonDisabled = 
	                              "confidence below nonunderstanding threshold";
	                          bBlockedBinding.sValue = sSlotValue;
	                          rbdBindings.vbBindings.add(bBlockedBinding);
	                          rbdBindings.iConceptsBlocked++;
							    // add the slot to the list of matched slots 
							    msiSlotsMatched.put(bBlockedBinding.sGrammarExpectation,1);
							    // in case we find it in the blocked slots, (it could have gotten
							    // there on an earlier level) delete it from there
							    if(msiSlotsBlocked.containsKey(bBlockedBinding.sGrammarExpectation)){
								    msiSlotsBlocked.remove(iPtr);
							    }
	                      }
	                  }
	              }
	
	              // write the blocked bindings description
	              for(int i = 0; i < vClosedIndices.size(); i++) {
	                  TBinding bBlockedBinding = new TBinding();
	                  bBlockedBinding.bBlocked = true;
	                  bBlockedBinding.iLevel = iLevel;
	                  bBlockedBinding.fConfidence = fConfidence;
	                  bBlockedBinding.sAgentName = 
	                      eaAgenda.celSystemExpectations.get(vClosedIndices.get(i)).
	                      pDialogAgent.GetName();
	                  bBlockedBinding.sConceptName = 
	                      eaAgenda.celSystemExpectations.get(vClosedIndices.get(i)).
	                      sConceptName;
	                  bBlockedBinding.sGrammarExpectation = 
	                      eaAgenda.celSystemExpectations.get(vClosedIndices.get(i)).
	                      sGrammarExpectation;
	                  bBlockedBinding.sReasonDisabled = 
	                      eaAgenda.celSystemExpectations.get(vClosedIndices.get(i)).
	                      sReasonDisabled;
	                  bBlockedBinding.sValue = sSlotValue;
	                  rbdBindings.vbBindings.add(bBlockedBinding);
	                  rbdBindings.iConceptsBlocked++;
						// add it to the list of blocked slots, if it's not already
						// in the one of matched slots
						if(!msiSlotsMatched.containsKey(bBlockedBinding.sGrammarExpectation)) {
							msiSlotsBlocked.put(bBlockedBinding.sGrammarExpectation,1);
						}
	              }
				}
			}
		}
	
	  // for user inputs, update the non-understanding flag
		if (DMCore.pInteractionEventManager.GetLastEvent().GetType() == Const.IET_USER_UTT_END ||
			DMCore.pInteractionEventManager.GetLastEvent().GetType() == Const.IET_GUI) {
			rbdBindings.bNonUnderstanding = (rbdBindings.iConceptsBound == 0);
		} else {
			rbdBindings.bNonUnderstanding = false;
		}
	
		// update the slots matched and blocked information
		rbdBindings.iSlotsMatched = msiSlotsMatched.size();
		rbdBindings.iSlotsBlocked = msiSlotsBlocked.size();
	
		// finally, for user inputs, check if the statistics match what helios 
		// predicted would happen (the helios binding features)
		if (DMCore.pInteractionEventManager.GetLastEvent().GetType() == Const.IET_USER_UTT_END) {
			if((DMCore.pInteractionEventManager.LastEventMatches("[slots_blocked]")) &&
				(DMCore.pInteractionEventManager.LastEventMatches("[slots_matched]"))) {
				boolean bHeliosMatch = true;
				String sH4SlotsBlocked = 
					DMCore.pInteractionEventManager.GetValueForExpectation("[slots_blocked]");
				String sH4SlotsMatched = 
					DMCore.pInteractionEventManager.GetValueForExpectation("[slots_matched]");
				if((sH4SlotsBlocked != "N/A") && 
					(Utils.atoi(sH4SlotsBlocked) != rbdBindings.iSlotsBlocked))
					bHeliosMatch = false;
				if((sH4SlotsMatched != "N/A") && 
					(Utils.atoi(sH4SlotsMatched) != rbdBindings.iSlotsMatched))
					bHeliosMatch = false;
				if(!bHeliosMatch) {
					Log.w(Const.DMCORE_STREAM_TAG,"Helios binding features are different from RavenClaw obtained values.");
				}
			}
			// finally, perform the forced concept updates
		    performForcedConceptUpdates(rbdBindings);
		} else if (DMCore.pInteractionEventManager.GetLastEvent().GetType() == Const.IET_GUI) {
			performForcedConceptUpdates(rbdBindings);
		}
	
		// and finally log the attempted bindings
		Log.d(Const.DMCORE_STREAM_TAG, "Attempted bindings dumped below:\n"
				+bindingsDescrToString(rbdBindings));						
	
	  // and the general statistics
		Log.d(Const.DMCORE_STREAM_TAG, "Concepts Binding Phase completed ("
				+rbdBindings.iConceptsBound+" concept(s) "+"bound, "
				+rbdBindings.iConceptsBlocked+" concept(s) blocked out).");
	}
	// D: Perform the concept binding
	public void performConceptBinding(String sSlotName, String sSlotValue,
	    float fConfidence, int iExpectationIndex, boolean bIsComplete) {
		
	    // obtain a reference to the expectation structure
		TConceptExpectation ceExpectation = 
			eaAgenda.celSystemExpectations.get(iExpectationIndex);

	    // compute the value we need to bind to that concept
	    String sValueToBind = ""; 
		if(ceExpectation.bmBindMethod == TBindMethod.bmSlotValue) {
	        // bind the slot value
	        sValueToBind = sSlotValue;
	    } else if(ceExpectation.bmBindMethod == TBindMethod.bmExplicitValue) {
		    // bind the explicit value
	        sValueToBind = ceExpectation.sExplicitValue;
	    } else {
	        // bind through a binding function
	        if((!s2bffFilters.containsKey(ceExpectation.sBindingFilterName))) {
	            Log.e(Const.DMCORE_STREAM_TAG,"Could not find binding filter :"
	            		+ceExpectation.sBindingFilterName+" for "
	            		+"expectation "+  ceExpectation.sGrammarExpectation
	            		+" generated by agent "+ceExpectation.pDialogAgent.GetName()+".");
	        }
	        // if the binding filter was found, call it
	        sValueToBind = s2bffFilters.get(ceExpectation.sBindingFilterName).
	        		CallBindingFilter(sSlotName, sSlotValue);
	    }

	    /*// reset the confidence to 1, if ALWAYS_CONFIDENT is defined
	    #ifdef ALWAYS_CONFIDENT
	    fConfidence = 1.0;
	    #endif*/

	    String sBindingString = sValueToBind+"|"+fConfidence;

	    // now bind that particular value/confidence

		if (bIsComplete) {
			// first, create a temporary concept for that
			CConcept pTempConcept =ceExpectation.pDialogAgent.
				getConceptFromPath(ceExpectation.sConceptName).EmptyClone();
			// assign it from the String
			pTempConcept.Update(Const.CU_ASSIGN_FROM_STRING, sBindingString);

			CConcept c = ceExpectation.pDialogAgent.getConceptFromPath(ceExpectation.sConceptName);

			// first if the concept has an undergoing grounding request, remove it
			if (c.IsUndergoingGrounding()){
				//DMCore.pGroundingManager.RemoveConceptGroundingRequest(&c);
			}

			// now call the binding method 
			c.Update(Const.CU_UPDATE_WITH_CONCEPT, pTempConcept);

			// finally, deallocate the temporary concept
			pTempConcept=null;
		} else {
			// perform a partial (temporary) binding
			ceExpectation.pDialogAgent.getConceptFromPath(ceExpectation.sConceptName).
				Update(Const.CU_PARTIAL_FROM_STRING, sBindingString);

		}

	    // log it
	    Log.d(Const.DMCORE_STREAM_TAG, "Slot "+sSlotName+"("+sBindingString
	    		+") bound to concept ("+ceExpectation.pDialogAgent.GetName()
	    		+")"+ceExpectation.sConceptName+".");
	}
	// D: performs the forced updates for the concepts
	public void performForcedConceptUpdates(TBindingsDescr rbdBindings) {

		Log.d(Const.DMCORE_STREAM_TAG, "Performing forced concept updates ...");

		// now go through the concepts that are waiting to be explicitly 
		// confirmed and perform the corresponding updates on them
		Iterator<CConcept>iterator=saSystemAction.setcpExplicitConfirms.iterator();
		CConcept iPtr = null;
		while(iterator.hasNext()) {
			iPtr = iterator.next();
			// check that the concept is still sealed (no update was performed 
			// on it yet)
			if((iPtr).IsSealed()) {
				
				// then perform an forced update			
				TForcedConceptUpdate fcu = new TForcedConceptUpdate();
				fcu.sConceptName = iPtr.GetName();
				fcu.iType = Const.FCU_EXPLICIT_CONFIRM;
				fcu.bUnderstanding = false;
				CHyp phOldTopHyp = iPtr.GetTopHyp();

				// log the update
				Log.d(Const.DMCORE_STREAM_TAG, "Performing forced concept update on "
				+iPtr.GetName()+" ...");
				Log.d(Const.DMCORE_STREAM_TAG, "Concept grounding status: "
				+DMCore.pGroundingManager.GetConceptGroundingRequestStatus(iPtr)); 
				iPtr.Update(Const.CU_UPDATE_WITH_CONCEPT, null);

				Log.d(Const.DMCORE_STREAM_TAG, "Concept grounding status: "
				+DMCore.pGroundingManager.GetConceptGroundingRequestStatus(iPtr));
				// now, if this update has desealed the concept, then we need
				// to run grounding on this concept 
				if(!(iPtr.IsSealed()) && 
					(DMCore.pGroundingManager.GetConceptGroundingRequestStatus(iPtr) != 
					Const.GRS_EXECUTING)) {
					// now schedule grounding on this concept
					String sAction = DMCore.pGroundingManager.ScheduleConceptGrounding(iPtr);
					// if the action scheduled is still explicit confirm
					if(sAction != "EXPL_CONF") {
						// then mark that we have an understanding
						fcu.bUnderstanding = true;
						rbdBindings.bNonUnderstanding = false;
					} else if (iPtr.GetTopHyp() == phOldTopHyp) {
						// if we are still on an explicit confirm on the same hypothesis, 
						// seal it back
						iPtr.Seal();
					}
				}

				// finally, push this into the bindings
				rbdBindings.vfcuForcedUpdates.add(fcu);
			}
		}
		iterator = saSystemAction.setcpImplicitConfirms.iterator();
		// now go through the concepts that are waiting to be implicitly 
		// confirmed and perform the corresponding updates on them
		while(iterator.hasNext()) {
			iPtr = iterator.next();
			// check that the concept is still sealed (no update was performed 
			// on it yet)
			if(iPtr.IsSealed()) {

				// then perform an forced update			
				TForcedConceptUpdate fcu = new TForcedConceptUpdate();
				fcu.sConceptName = iPtr.GetName();
				fcu.iType = Const.FCU_IMPLICIT_CONFIRM;
				fcu.bUnderstanding = false;

				// log the update
				Log.d(Const.DMCORE_STREAM_TAG, 
					"Performing forced concept update on "+iPtr.GetName()+" ..."); 
				iPtr.Update(Const.CU_UPDATE_WITH_CONCEPT, null);

				// now, if this update has desealed the concept, then we need
				// to run grounding on this concept 
				if(!(iPtr.IsSealed()) && 
					(DMCore.pGroundingManager.GetConceptGroundingRequestStatus(iPtr) != 
					Const.GRS_EXECUTING)) {
					// first check that it was not already scheduled
					if(DMCore.pGroundingManager.GetScheduledGroundingActionOnConcept(iPtr) == "")				    
	                    // if not scheduled already, schedule it now				
						DMCore.pGroundingManager.ScheduleConceptGrounding(iPtr);
					// then mark that we have an understanding
					fcu.bUnderstanding = true;
					rbdBindings.bNonUnderstanding = false;
				}

				// finally, push this into the bindings
				rbdBindings.vfcuForcedUpdates.add(fcu);
			}
		}
		iterator = saSystemAction.setcpUnplannedImplicitConfirms.iterator();
		// finally, go through the concepts that have unplanned implicit confirmations
		// on them and perform the corresponding updates on them
		while(iterator.hasNext()) {
			iPtr = iterator.next();
			// check that the concept is still sealed (no update was performed 
			// on it yet)
			if(iPtr.IsSealed()) {

				// then perform an forced update			
				TForcedConceptUpdate fcu = new TForcedConceptUpdate();
				fcu.sConceptName = iPtr.GetName();
				fcu.iType = Const.FCU_UNPLANNED_IMPLICIT_CONFIRM;
				fcu.bUnderstanding = false;

				// log the update
				Log.d(Const.DMCORE_STREAM_TAG, 
					"Performing forced concept update on "+iPtr.GetName()+" ..."); 
				iPtr.Update(Const.CU_UPDATE_WITH_CONCEPT, null);

				// now, if this update has desealed the concept, then we need
				// to run grounding on this concept 
				if(!(iPtr.IsSealed())) {
					// first check that it was not already scheduled
					if(DMCore.pGroundingManager.GetScheduledGroundingActionOnConcept(iPtr) == "")				    
	                    // if not scheduled already, schedule it now				
						DMCore.pGroundingManager.ScheduleConceptGrounding(iPtr);
					// then mark that we have an understanding
					fcu.bUnderstanding = true;
					rbdBindings.bNonUnderstanding = false;
				}

				// finally, push this into the bindings
				rbdBindings.vfcuForcedUpdates.add(fcu);
			}
		}

		// log completion of this phase
		Log.d(Const.DMCORE_STREAM_TAG, "Forced concept updates completed.");
	}
	// D: Returns the current main topic agent
	// A: The main topic is now identified by going down the stack
	// instead of using the tree
	public CDialogAgent GetCurrentMainTopicAgent() {
		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		while (iterator.hasNext()) {
			TExecutionStackItem iPtr = iterator.next();
			if (iPtr.pdaAgent.IsAMainTopic()) {
				return iPtr.pdaAgent;
			}
		}

		// No main topic found on the stack (probably an error)
		return null;
	}

	// D: Returns true if the agent is an active topic
	public boolean AgentIsActive(CDialogAgent pdaDialogAgent) {
		TExecutionStackItem iPtr = new TExecutionStackItem();
		Iterator<TExecutionStackItem> iterator = esExecutionStack.iterator();
		while (iterator.hasNext()) {
			iPtr = iterator.next();
			if (iPtr.pdaAgent == pdaDialogAgent)
				return true;
		}
		return false;
	}

	// A: Returns the last input turn number
	public int GetLastInputTurnNumber() {
		return iTurnNumber;
	}
}
