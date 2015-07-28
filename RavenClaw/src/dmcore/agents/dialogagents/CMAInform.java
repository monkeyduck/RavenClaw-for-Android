package dmcore.agents.dialogagents;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.ravenclaw.OutputActivity;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.coreagents.CInteractionEventManagerAgent;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.TConveyance;
import dmcore.agents.mytypedef.TDialogExecuteReturnCode;
import dmcore.agents.mytypedef.TFloorStatus;
import dmcore.outputs.COutput;
import dmcore.outputs.MyOutput;
//-----------------------------------------------------------------------------
//
// D: the CMAInform class -- the microagent for Inform
//
//-----------------------------------------------------------------------------
public class CMAInform extends CDialogAgent{
	// list of prompts planned by this agent
	protected ArrayList<COutput> voOutputs = new ArrayList<COutput>();
	

	//-----------------------------------------------------------------------------
	// Constructors and Destructors
	//-----------------------------------------------------------------------------
	// D: constructor
	public CMAInform(String sAName, String sAConfiguration, String sAType){
		super(sAName, sAConfiguration, sAType) ;
	}

	public CMAInform(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CMAInform";
		this.SetType(sAType);
	}
	public CMAInform(){}

	//-----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	/*public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CMAInform(sAName, sAConfiguration);
	}*/

	//-----------------------------------------------------------------------------
	//
	// Specialized (overwritten) CDialogAgent methods
	//
	//-----------------------------------------------------------------------------

	// D: the Execute routine: calls upon the Output Manager to send a prompt out
	public TDialogExecuteReturnCode Execute() {
		
		// call on the output manager to send out the output
		/*ArrayList<COutput> voTemp = 
				DMCore.pOutputManager.Output(this, Prompt(), TFloorStatus.fsFree);
		voOutputs.addAll(voTemp);*/
		
		// set bOutputCompleted value to false
		bOutputCompleted = false;
		// call output method
		DMCore.myoutput.synthetizeInSilence(Prompt());
		// LILINCHUAN:Inform Agent completed once it call synthetizaInSilence
		bCompleted = true;
		
	    // increment the execute counter
	    IncrementExecuteCounter();

		// and return with continue execution
		return TDialogExecuteReturnCode.dercContinueExecution;
	}

	// D: the ReOpenTopic method
	public void ReOpenTopic() {
		// Clears the list of emitted output prompts
		voOutputs.clear();
		// Do the standard ReOpenTopic
		super.ReOpenTopic();
	}

	// A: Resets clears the list of outputs for this concept
	public void Reset() {
		super.Reset();
		voOutputs.clear();
	}

	// D: SuccessCriteriaSatisfied: returns true if the inform agent has conveyed all its prompts
	public boolean SuccessCriteriaSatisfied() {

		// no output prompts: the agent wasn't executed at all yet
		if (voOutputs.isEmpty()) return false;

		// otherwise completes once all the prompts it have been
		// spoken or canceled (due to barge-in)
		boolean bCompleted = true;
		for (int i = 0; i < voOutputs.size(); i++) {
			if (voOutputs.get(i).GetConveyance() == TConveyance.cNotConveyed) {
				bCompleted = false;
				break;
			}
		}
		return bCompleted;
	}

	// D: The Prompt method
	public String Prompt() {
	// by default, returns the name of the agent
	return "{inform "+sDialogAgentName+" agent="+sName+"}";
	// returns the name of the agent
	//return FormatString("[%s]", sDialogAgentName);
}
}
