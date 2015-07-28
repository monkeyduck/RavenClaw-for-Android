package dmcore.agents.dialogagents;

import android.util.Log;
import utils.Const;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;

//--------------------------------------------------------------------------
//
// D: the CMAExecute class -- the microagent for Execute, On execution, this 
//	     microagent executes a call to another agent, as described by the 
//	     ExecuteCall function. Derived classes can implement anything they 
//	     want in the execution section of this agent. 
// 
//--------------------------------------------------------------------------
public class CMAExecute extends CDialogAgent{
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	// default constructor
	public CMAExecute(){
		
	}
	public CMAExecute(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CMAExecute";
		this.SetType(sAType);
	}

	/*// static function for dynamic agent creation
	public CAgent AgentFactory(String sAName, String sAConfiguration){
		
	}*/
	//----------------------------------------------------------------------
	//
	// Specialized (overwritten) CDialogAgent methods
	//
	//---------------------------------------------------------------------

	// D: Execute function: just executes the call, as given by ExecuteCall()
	public TDialogExecuteReturnCode Execute() {

		// call the execute routine
		ExecuteRoutine();
		
		// Only after the output completed do this goes on
		while(!this.bOutputCompleted){
			Log.e(Const.EXECUTE_STREAM_TAG,"Output has not been completed");
		}

	    // increment the execute counter
	    IncrementExecuteCounter();

		// and return with continue execution
		return TDialogExecuteReturnCode.dercContinueExecution;
	}

	// D: SuccessCriteriaSatisfied: Request agents are completed as soon as 
	//	    they have executed
	public boolean SuccessCriteriaSatisfied() {
		return (iExecuteCounter > 0);
	}

	//------------------------------------------------------------------
	//
	// Execute Microagent specific methods
	//
	//-------------------------------------------------------------------

	// D: the actual routine to be executed by this agent
	public void ExecuteRoutine() {
		// first obtain the execute call
		String sExecuteCall = GetExecuteCall();
	    
		// set the flag to default value:false
		bOutputCompleted = false;
	    // if there is a call to be made
	    if(sExecuteCall.length()!=0) {
		    // call on the method to match items in the database
		    String sPromptResult = 
		    		DMCore.fdhDatabaseHelper.MatchQuery(sExecuteCall);
		    
		    // Output the match result,if we find the required flight,
		    // it prompts the detailed information; else it prompts "no flight"
		    DMCore.myoutput.synthetizeInSilence(sPromptResult);
	    }
	}

	// D: Returns the execution call as a String. Derived classes are to 
	//	    overwrite this
	public String GetExecuteCall() {
		// by default, nothing
		return "";
	}
}
