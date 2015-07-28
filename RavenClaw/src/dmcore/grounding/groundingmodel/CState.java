package dmcore.grounding.groundingmodel;

import java.util.HashMap;

import utils.Utils;

public class CState {
	//---------------------------------------------------------------------
	// private class members
	//---------------------------------------------------------------------

    // the map holding the values for the state variables
	private HashMap<String, String> s2sStateVars = new HashMap<String, String>();

	//---------------------------------------------------------------------
	// public class members
	//---------------------------------------------------------------------

    // clear the state
    public void Clear(){
    	s2sStateVars.clear();
    }

    // adding to the state from another STRING2STRING hash
    public void Add(HashMap<String,String> s2s){
    	
    }

    // string conversion function
    public String ToString(){
    	return Utils.S2SHashToString(s2sStateVars, "\n") + "\n";
    }

}
