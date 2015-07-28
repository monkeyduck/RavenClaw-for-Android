package dmcore.concepts;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;
import android.util.Log;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TConveyance;

class CStringHyp extends CHyp{
	

	//---------------------------------------------------------------------
    // Protected member variables 
	//---------------------------------------------------------------------
	//
	protected String sValue="";
	
	//-----------------------------------------------------------------------------
	// CStringHyp: Constructors and Destructors
	//-----------------------------------------------------------------------------

	// D: default constructor
	public CStringHyp() {
		ctHypType = TConceptType.ctString;
		sValue = "";
		fConfidence = (float) 1.0;
	}

	// D: copy constructor
	public CStringHyp(CStringHyp rAStringHyp) {
		ctHypType = TConceptType.ctString;
		sValue = rAStringHyp.sValue;
		fConfidence = rAStringHyp.fConfidence;
	}

	// D: from value + confidence constructor
	public CStringHyp(String sAValue, float fAConfidence) {
		ctHypType = TConceptType.ctString;
		sValue = sAValue;
		fConfidence = fAConfidence;
	}

	//-----------------------------------------------------------------------------
	// CStringHyp: Overwritten, CStringHyp specific methods
	//-----------------------------------------------------------------------------

	// D: assignment operator from String
	public CHyp Assignment(String sAValue) {
		sValue = sAValue;
		fConfidence = (float) 1.0;
		return this;
	}

	// D: assignment operator from another ValConf
	public CHyp Assignment(CHyp rAHyp) {
	    if(rAHyp != this) {
		    // check the type
		    if(rAHyp.GetHypType() != TConceptType.ctString) {
			    // if it's not a String, signal an error
			    Log.e(Const.CSTRING_TAG,"Assignment operator from a different hyp type called on "+
				    "String hyp. Cannot perform conversion.");
			    return this;
		    }

		    CStringHyp rAStringHyp = (CStringHyp)rAHyp;
		    sValue = rAStringHyp.sValue;
		    fConfidence = rAStringHyp.fConfidence;
	    }
		return this;
	}
	@Override
	// D: equality operator
	public boolean equals(Object rAHyp) {
	    
		// check the type
		if(((CHyp)rAHyp).GetHypType() != TConceptType.ctString) {
			// if it's not a String, signal an error
			Log.e(Const.CSTRING_TAG,"Equality operator with a different hyp type called on "+
			  	  "String hyp. Cannot perform conversion.");
			return false;
		}

	    return sValue == ((CStringHyp)rAHyp).sValue;
	}

	// D: Comparison operator
	public boolean lessthan(CHyp rAHyp) {    
	    // signal an error
		Log.e(Const.CSTRING_TAG,"Comparison operator < called on CStringHyp.");
		return false;
	}

	// D: Comparison operator
	public boolean greaterthan(CHyp rAHyp) {    
	    // signal an error
		Log.e(Const.CSTRING_TAG,"Comparison operator > called on CStringHyp.");
		return false;
	}

	// D: Comparison operator
	public boolean lessequal(CHyp rAHyp) {    
	    // signal an error
		Log.e(Const.CSTRING_TAG,"Comparison operator <= called on CStringHyp.");
		return false;
	}

	// D: Comparison operator
	public boolean greaterequal(CHyp rAHyp) {    
	    // signal an error
		Log.e(Const.CSTRING_TAG,"Comparison operator >= called on CStringHyp.");
		return false;
	}

	// D: Indexing operator - return an error, should never be called
	public CHyp getIndexing(String sItem) {
		Log.e(Const.CSTRING_TAG,"Indexing operator [] called on CStringHyp.");
	    return null;
	}

	// D: Convert value to String
	public String ValueToString() {
		return sValue;
	}

	// D: Convert hyp to String
	public String ToString() {
		return sValue + "|"+fConfidence;
	}

	// D: Get the hyp from a String
	public void FromString(String sString) {
		// separate the String into value and confidence 
		SplitReturnType srt = Utils.SplitOnFirst(sString, "|");
		String sNewValue=srt.FirstPart.trim();
		String sConfidence=srt.SecondPart.trim();

		// cut the apostrophes if in there
		if((sNewValue.charAt(0) == '"') && 
		   (sNewValue.charAt(sNewValue.length()-1) == '"') && 
		   (sNewValue.length()>1)) {
			sValue = sNewValue.substring(1, sNewValue.length()-1);
		}
		else{
			this.sValue=sNewValue;
		}
		
		// get the confidence 
	    if(sConfidence == "") {
	        fConfidence = (float) 1.0;
	    } else {
	    	try{
	    		fConfidence = Float.parseFloat(sConfidence);
	    	}catch(Exception e){
	    		 Log.e(Const.CSTRING_TAG,"Cannot perform conversion to CStringHyp from "
	    				 +sString);
	    	}
	    }
	}
}
public class CStringConcept extends CConcept implements ConceptFactory{
	//-----------------------------------------------------------------------------
	// CStringConcept: Constructors and destructors
	//-----------------------------------------------------------------------------

	// D: constructor by name
	public CStringConcept(String sAName, TConceptSource csAConceptSource){
	    super(sAName, csAConceptSource, 1000);
		ctConceptType = TConceptType.ctString;
	}

	// D: constructor from name + value + confidence
	public CStringConcept(String sAName, String sAValue, float fAConfidence,
								   TConceptSource csAConceptSource) {
		ctConceptType = TConceptType.ctString;
		csConceptSource = csAConceptSource;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
	    vhCurrentHypSet.add(new CStringHyp(sAValue, fAConfidence));
	    iNumValidHyps = 1;
	    iCardinality = 1000;
	    iTurnLastUpdated = -1;
	    bWaitingConveyance = false;
		cConveyance = TConveyance.cNotConveyed;
	    SetHistoryConcept(false);
	    pPrevConcept = null;
	}

	public CStringConcept() {
		// TODO Auto-generated constructor stub
	}

	// D: returns an empty clone of the concept (basically just preserving the
	//	    type, but not the contents
	public CConcept EmptyClone() {
	    return new CStringConcept();
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods related to the current hypothesis set and belief
	// manipulation
	//-----------------------------------------------------------------------------

	// D: factory method for hypotheses
	public CHyp HypFactory() {
		return new CStringHyp();
	}
	@Override
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated method stub
		return new CStringConcept(sAName,csAConceptSource);
	}
}
