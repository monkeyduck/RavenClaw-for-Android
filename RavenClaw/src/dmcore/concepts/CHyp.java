package dmcore.concepts;

import utils.Const;
import android.util.Log;

//-----------------------------------------------------------------------------
//CHyp class - this is the base class for the hierarchy of hypothesis
//           classes. It essentially implements a type and an associated 
//           value + confidence score
//-----------------------------------------------------------------------------
public class CHyp {
	
	//---------------------------------------------------------------------
	// Protected member variables 
	//---------------------------------------------------------------------
	//
	protected TConceptType ctHypType;			// the concept (hypothesis) type
	protected float fConfidence;              // the confidence score
	
	// D: Constructor
	public CHyp() {
		ctHypType = TConceptType.ctUnknown;
		fConfidence = (float) 1.0;
	}

	// D: Constructor from reference
	public CHyp(CHyp rAHyp) {
		ctHypType = rAHyp.ctHypType;
		fConfidence = rAHyp.fConfidence;
	}

	//-----------------------------------------------------------------------------
	// Acess to member variables
	//-----------------------------------------------------------------------------

	// D: Access method to concept (hypothesis) type
	public TConceptType GetHypType() {
		return ctHypType;
	}

	// D: Access to confidence value
	public float GetConfidence() {
	    return fConfidence;
	}

	//-----------------------------------------------------------------------------
	// Virtual concept type specific functions (to be overwritten by derived 
	// classes)
	//-----------------------------------------------------------------------------
	// D: assignment operator
	public CHyp Assignment (CHyp rAHyp) {
	    // check self assignment
		if(rAHyp != this) {
	    	ctHypType = rAHyp.ctHypType;
		    fConfidence = rAHyp.fConfidence;
	    }
		return this;
	}
	@Override
	// D: equality operator - return an error, it should never get called
	public boolean equals (Object rAHyp) {
		Log.e(Const.CONCEPT_TAG,"Equality operator called on abstract CHyp.");
	    return false;
	}

	// D: comparison operator - return an error, it should never get called
	public boolean lessthan (CHyp rAHyp) {
		Log.e(Const.CONCEPT_TAG,"Comparison operator < called on abstract CHyp.");
	    return false;
	}

	// D: comparison operator - return an error, it should never get called
	public boolean greaterthan (CHyp rAHyp) {
		Log.e(Const.CONCEPT_TAG,"Comparison operator > called on abstract CHyp.");
	    return false;
	}

	// D: comparison operator - return an error, it should never get called
	public boolean lessequal(CHyp rAHyp) {
		Log.e(Const.CONCEPT_TAG,"Comparison operator <= called on abstract CHyp.");
	    return false;
	}

	// D: comparison operator - return an error, it should never get called
	public boolean greaterequal(CHyp rAHyp) {
		Log.e(Const.CONCEPT_TAG,"Comparison operator >= called on abstract CHyp.");
	    return false;
	}

	// D: Indexing operator - return an error, should never be called
	public CHyp getIndexing(String sItem) {
		Log.e(Const.CONCEPT_TAG,"Indexing operator [] called on abstract CHyp.");
	    return null;
	}
	// D: Set the confidence score
	public void SetConfidence(float fAConfidence) {
	    fConfidence = fAConfidence;
	}

	
	// D: Convert value to String
	public String ValueToString() {
		Log.e(Const.CONCEPT_STREAM_TAG,"ValueToString called on abstract CHyp.");
		return Const.ABSTRACT_CONCEPT;
	}

	// D: Convert hypothesis to String
	public String ToString() {
		Log.e(Const.CONCEPT_STREAM_TAG,"ToString called on abstract CHyp.");
		return Const.ABSTRACT_CONCEPT;
	}

	// D: Get the hypothesis from a String
	public void FromString(String sString) {
		Log.e(Const.CONCEPT_STREAM_TAG,"FromString called on abstract CHyp. Call failed.");
	}
}
