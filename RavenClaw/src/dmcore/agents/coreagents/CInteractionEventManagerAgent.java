package dmcore.agents.coreagents;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import user.definition.UserInput;
import utils.Const;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechUnderstander;
import com.iflytek.cloud.speech.SpeechUnderstanderListener;
import com.iflytek.cloud.speech.TextUnderstander;
import com.iflytek.cloud.speech.TextUnderstanderListener;
import com.iflytek.cloud.speech.UnderstanderResult;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import dmcore.events.CInteractionEvent;
import dmcore.outputs.MyOutput;

import dmcore.agents.mytypedef.AgentFactory;

//-----------------------------------------------------------------------------
// CInteractionEventManagerAgent Class - 
//   This class is an agent which handles events from the Interaction Manager
//-----------------------------------------------------------------------------
public class CInteractionEventManagerAgent extends CAgent implements AgentFactory {
	//---------------------------------------------------------------------
	// Private members
	//---------------------------------------------------------------------
	//

	// handle for a new interaction event signal
	private	Handler hNewInteractionEvent;                     
		
	// queue of current (unprocessed) events
	private Queue<CInteractionEvent> qpieEventQueue=
			new ConcurrentLinkedQueue<CInteractionEvent>();

	// history of past events
	private	ArrayList<CInteractionEvent> vpieEventHistory=
			new ArrayList<CInteractionEvent>();;

	// pointer to most recently processed event
	private	CInteractionEvent pieLastEvent;

	// pointer to most recently processed user input
	private	CInteractionEvent pieLastInput;
	
	private Message message;

	//语义理解对象
	private SpeechUnderstander speechUnderstander;
	//---------------------------------------------------------------------
	// Constructor and destructor
	//---------------------------------------------------------------------
	//
	// A: Default constructor
	public CInteractionEventManagerAgent(String sAName,
										   String sAConfiguration,  
										   String sAType){
		super(sAName, sAConfiguration, sAType);

		// create a "NewInput" event to handle communication across the threads
		hNewInteractionEvent = new Handler();
		assert(hNewInteractionEvent != null);
	}
	// Overload:Default constructor
	public CInteractionEventManagerAgent(String sAName,
										   String sAConfiguration){
		super(sAName, sAConfiguration);
		String sAType = "CAgent:CInteractionEventManagerAgent";
		this.SetType(sAType);
		// create a "NewInput" event to handle communication across the threads
		hNewInteractionEvent = new Handler();
		assert(hNewInteractionEvent != null);
	}
						   

	public CInteractionEventManagerAgent() {
		// TODO Auto-generated constructor stub
	}
	// A: static function for dynamic agent creation
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CInteractionEventManagerAgent(sAName, sAConfiguration);
	}

	//---------------------------------------------------------------------
	// CAgent Class overwritten methods 
	//---------------------------------------------------------------------
	//
	public void Reset() {
	}

	public void Initialize() {
		pieLastEvent = null;
		pieLastInput = null;
	}

	//---------------------------------------------------------------------
	// InteractionEventManagerAgent class specific public methods
	//---------------------------------------------------------------------

	// A: Indicates if the queue has at least one event
	public boolean HasEvent() {
		return !qpieEventQueue.isEmpty();
	}

	// A: Dequeues one event from the queue
	public CInteractionEvent GetNextEvent() {
		if (!qpieEventQueue.isEmpty()) {
		
			// pops the event from the event queue
			CInteractionEvent pieNext = qpieEventQueue.poll();

			// updates pointer to last event processed
			pieLastEvent = pieNext;
			// also if the event was an input
			if (pieNext.GetType() == Const.IET_USER_UTT_END) {
				pieLastInput = pieNext;
			}

			// pushes the event in the history
			vpieEventHistory.add(pieNext);

			return pieNext;
		}
		else {
			return null;
		}
	}

	// A: Returns a pointer to the last event processed
	public CInteractionEvent GetLastEvent() {
		return pieLastEvent;
	}

	// A: Returns a pointer to the last user input processed
	public CInteractionEvent GetLastInput() {
		return pieLastInput;
	}
		
	// A: Check if the last event matches a certain grammar expectation
	public boolean LastEventMatches(String sGrammarExpectation) {
		// delegate it to the InteractionEvent class
		return pieLastEvent.Matches(sGrammarExpectation);
	}

	// A: Check if the last user input matches a certain grammar expectation
	public boolean LastInputMatches(String sGrammarExpectation) {
		// delegate it to the InteractionEvent class
		return pieLastInput.Matches(sGrammarExpectation);
	}

	// A: Check if the last event is a complete or a partial event
	public boolean LastEventIsComplete() {
		return pieLastEvent.IsComplete();
	}

	// A: Returns the confidence score for the last event
	public float GetLastEventConfidence() {
	    // delegate it to the Input class
	    return pieLastEvent.GetConfidence();
	}

	// D: Returns the String value of a grammar concept
	public String GetValueForExpectation(String sGrammarExpectation) {
		// delegate it to the Input class
		return pieLastEvent.GetValueForExpectation(sGrammarExpectation);
	}

	// A: Waits for an interaction event to arrive from the Interaction Manager
	public void WaitForEvent() {

		if (qpieEventQueue.isEmpty()) {
			// retrieve the current thread id
			//DWORD dwThreadId = GetCurrentThreadId();
			
			/*UserInput.JsonResult = "{\"semantic\": \"slots\": {\"startDate\": {\"date\""
					+": \"2014-05-14\", \"type\": \"DT_BASIC\", \"dateOrig\": \"明天\"},"
					+"\"startLoc\": {\"poi\": \"首都国际机场\", \"cityAddr\": \"北京\","
					+" \"city\": \"北京市\", \"type\": \"LOC_POI\"},"
					+" \"airline\": \"海航\", \"endLoc\": {\"poi\": \"遥墙机场\","
					+" \"cityAddr\": \"济南\", \"city\": \"济南市\",\"type\": \"LOC_POI\"}}";*/
			
			UserInput.JsonResult="";
			while(UserInput.JsonResult.equals("")){
				Log.e(Const.INPUTMANAGER_STREAM,"Input not arrived");
			}
			
			Log.d(Const.INPUTMANAGER_STREAM,UserInput.JsonResult);
			/*// send a message to the galaxy interface to wait for input
			// message.what = 1 means 
			message = new Message();
			message.what = ASK_USER_INPUT;
			message.obj=UserInput.JsonResult;
			mhMainHandler.sendMessage(message);*/
			//PostThreadMessage(g_idDMInterfaceThread, WM_WAITINTERACTIONEVENT, 0, dwThreadId);
			
			// log that we started waiting for an input
			Log.d(Const.INPUTMANAGER_STREAM, "Waiting for interaction event ...");
			

			// and then wait for the utterance to appear
			//WaitForSingleObject(hNewInteractionEvent, INFINITE);

			// process the new event
			CInteractionEvent pieEvent = new CInteractionEvent(Const.IET_USER_UTT_END);
			pieEvent.SetThreadId((int)Thread.currentThread().getId());
			pieEvent.SetProperties(UserInput.JsonResult);
			pieEvent.SetCompleted(true);
			

			// identify the type of event
			/*String sType = (String)Gal_GetString((Gal_Frame)gfLastEvent, ":event_type");
			Gal_Frame gfEventFrame = Gal_CopyFrame((Gal_Frame)gfLastEvent);

			// create the appropriate event object
			pieEvent = new CGalaxyInteractionEvent(gfEventFrame);
			 */
			if (sType == Const.IET_USER_UTT_END) {
				Log.d(Const.INPUTMANAGER_STREAM, "New user input [User:"+
						pieEvent.GetStringProperty("[uttid]")+"]");
			}

			// push the event at the end of the event queue
			qpieEventQueue.add(pieEvent);

			// log it
			Log.d(Const.INPUTMANAGER_STREAM, "New interaction event (");
					/*+sType+") arrived (dumped below)\n"+pieEvent.ToString()); */

		}

	}
	
	// A: Used by the Galaxy Bridge to signal that a new event has arrived
	public void SignalInteractionEventArrived() {
		// signal that the input has appeared
		/*SetEvent(hNewInteractionEvent);*/
	}
	
	
	
}
