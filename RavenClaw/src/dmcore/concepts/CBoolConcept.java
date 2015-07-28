package dmcore.concepts;

import android.util.Log;
import utils.Const;
import utils.SplitReturnType;
import utils.Utils;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TConveyance;

class CBoolHyp extends CHyp{
	//---------------------------------------------------------------------
    // Protected member variables 
	//---------------------------------------------------------------------
	//
	protected boolean bValue;                    // the actual value
	// declaration of the cTrue and cFalse concepts
	//public static CBoolConcept cTrue = new CBoolConcept("_system_True", true, 1, csSystem);
	//public static CBoolConcept cFalse= new CBoolConcept("_system_False", false, 1, csSystem);

	//-----------------------------------------------------------------------------
	// CBoolHyp class - this class keeps a pair: <Boolean-value, conf>
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// CBoolHyp: Constructors and Destructors
	//-----------------------------------------------------------------------------

	// D: default constructor
	public CBoolHyp() {
		ctHypType = TConceptType.ctBool;
		bValue = false;
		fConfidence = (float)1.0;
	}

	// D: copy constructor
	public CBoolHyp(CBoolHyp rABoolHyp) {
		ctHypType = TConceptType.ctBool;
		bValue = rABoolHyp.bValue;
		fConfidence = rABoolHyp.fConfidence;
	}

	// D: from bool + confidence constructor
	public CBoolHyp(boolean bAValue, float fAConfidence) {
		ctHypType = TConceptType.ctBool;
		bValue = bAValue;
		fConfidence = fAConfidence;
	}

	//-----------------------------------------------------------------------------
	// CBoolHyp: Overwritten, CBoolHyp specific methods
	//-----------------------------------------------------------------------------

	// D: Convert value to String
	public String ValueToString() {
		return bValue?"true":"false";
	}

	// D: Convert hyp to String
	public String ToString() {
		String sResult = "|"+fConfidence;
		sResult = (bValue?"true":"false") + sResult;
		return sResult;
	}

	// D: Get the hyp from a String
	public void FromString(String sString) {
		
		// separate the String into value and confidence 
		SplitReturnType srt =Utils.SplitOnFirst(sString, "|");
		String sUpperValue = srt.FirstPart;
		String sConfidence = srt.SecondPart;
		
		sUpperValue = sUpperValue.trim().toUpperCase();
		sConfidence = sConfidence.trim();
		
		// get the value
		if(sUpperValue == "TRUE") {
			bValue = true;
		} else if(sUpperValue == "FALSE") {
			bValue = false;	
		} else {
			Log.e(Const.CONCEPT_STREAM_TAG, "Cannot perform conversion to CBoolHyp from "
					+sString+".");
	        return;
		}

		// get the confidence 
	    if(sConfidence == "") {
	        fConfidence = (float) 1.0;
	    } else {
		    try{
		    	fConfidence = Float.valueOf(sConfidence);
		    }catch(Exception e){
		    	 Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform conversion to CBoolHyp from "
				    		+sString+".");
		         return;
		    }
	    }
	}
}
public class CBoolConcept extends CConcept implements ConceptFactory{
	
	public static final int DEFAULT_BOOL_CARDINALITY=2;
	//-----------------------------------------------------------------------------
	// CBoolConcept class - this is the boolean concept class. It overloads and
	//							provides some new constructors, and operators
	//-----------------------------------------------------------------------------
	
	//-----------------------------------------------------------------------------
	// Implements method of ConceptFactory Interface
	//-----------------------------------------------------------------------------
	
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated method stub
		return new CBoolConcept(sAName,csAConceptSource);
	}
	//-----------------------------------------------------------------------------
	// CBoolConcept: Constructors and destructors
	//-----------------------------------------------------------------------------

	public CBoolConcept() {
		// TODO Auto-generated constructor stub
	}
	// D: constructor by name
	public CBoolConcept(String sAName, TConceptSource csAConceptSource){
	    super(sAName, csAConceptSource, DEFAULT_BOOL_CARDINALITY);
		ctConceptType = TConceptType.ctBool;
	}

	// D: constructor from name + value + confidence
	public CBoolConcept(String sAName, boolean bAValue, float fAConfidence,
			TConceptSource csAConceptSource) {
		ctConceptType = TConceptType.ctBool;
		csConceptSource = csAConceptSource;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
	    vhCurrentHypSet.add(new CBoolHyp(bAValue, fAConfidence));
	    iNumValidHyps = 1;
	    iCardinality = DEFAULT_BOOL_CARDINALITY;
	    iTurnLastUpdated = -1;
	    bWaitingConveyance = false;
		cConveyance = TConveyance.cNotConveyed;
	    SetHistoryConcept(false);
	    pPrevConcept = null;
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods for overall concept manipulation
	//-----------------------------------------------------------------------------

	

	// D: returns an empty clone of the concept (basically just preserving the
	//	    type, but not the contents
	public CConcept EmptyClone() {
		
	    return new CBoolConcept();
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods related to the current hypothesis set and belief
	// manipulation
	//-----------------------------------------------------------------------------

	// D: factory method for hypotheses
	public CHyp HypFactory() {
		return new CBoolHyp();
	}
	

}
