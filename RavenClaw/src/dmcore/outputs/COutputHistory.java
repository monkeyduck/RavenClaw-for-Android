package dmcore.outputs;

import java.util.ArrayList;

import android.util.Log;

public class COutputHistory {
	// Log tag
	public static final String OUTHISTORY_TAG="OutHistory";
	// private members
	//
	
	private ArrayList<String> vsUtterances = 
			new ArrayList<String>();		// history of utterances as strings
	private ArrayList<COutput> vopOutputs = 
			new ArrayList<COutput>();		// history of outputs

	private int uiCurrentID;			// next id to be added to history

	//-----------------------------------------------------------------------------
	// COutputHistory public methods 
	//-----------------------------------------------------------------------------
	// DA: Generates a String representation of the history out outputs
	public String ToString() {
		// build the String in sResult
		String sResult = "OUTPUT HISTORY\nid\t utterance\n";
		sResult += "-----------------------------------------------------------"+
			       "---------------------\n";

		// add all the utterances in the history in reverse chronological order
		for (int i = vsUtterances.size() - 1; 
			 (i >= 0) && (5 >= vsUtterances.size() - i); i--) 
			sResult += vopOutputs.get(i).iOutputId+"\t"+vsUtterances.get(i)+"\n";
	    return sResult;
	}

	// A: appends request to history as sent to the external output agent/server.
	//	    Should get called on DM Interface thread
	public int AddOutput(COutput pOutput, String sUtterance) {

		// push the output and actual utterance on the history vectors
		vopOutputs.add(pOutput);
		vsUtterances.add(sUtterance);
		uiCurrentID++;

		// return the current id
		return uiCurrentID;
	}

	// A: deletes history and starts a new one, beginning with ID uiNewStart
	//	    should be called on DM Core thread
	public void Clear() {
		// destroy all the output objects in the history
		
		vopOutputs.clear();
		vsUtterances.clear();
		uiCurrentID = 0;
	}

	// DA: return the size of the output history
	public int GetSize() {
		return vopOutputs.size();
	}

	// DA: return the utterance at a particular index, starting from the
	//	     most recent
	public String GetUtteranceAt(int iIndex) {
		int iSize = vsUtterances.size();
	    if ( (int)(iSize - 1 - iIndex) < 0 ) {
	    	Log.e(OUTHISTORY_TAG,"Invalid index in output utterance history: "+
		            "index = "+iSize+", history_size = "+iIndex);
	        return "";
	    }
		else return vsUtterances.get(iSize - 1 - iIndex);
	}

	// DA: accesses the indexth element of the array, counting 0 as the
	//	     most recent.
	public COutput GetOutputAt(int iIndex) {
		int iSize = vopOutputs.size();
	    if ( (int)(iSize - 1 - iIndex) < 0 ) {
	        Log.e(OUTHISTORY_TAG,"Invalid index in output utterance history: "+
	            "index = "+iSize+", history_size = "+iIndex);
			return null;
	    }
		else return vopOutputs.get(iSize - 1 - iIndex);
	}

	

}