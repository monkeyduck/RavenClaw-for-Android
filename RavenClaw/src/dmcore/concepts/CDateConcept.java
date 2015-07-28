package dmcore.concepts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import android.util.Log;

import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TConveyance;

class CDateHyp extends CHyp{
	protected Calendar cValue;
	
	//-----------------------------------------------------------------------------
	// CDateHyp class - this class keeps a pair: <Boolean-value, conf>
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// CDateHyp: Constructors and Destructors
	//-----------------------------------------------------------------------------

	// D: default constructor
	public CDateHyp() {
		ctHypType = TConceptType.ctDate;
		cValue = Calendar.getInstance();
		fConfidence = (float) 1.0;
	}

	// D: copy constructor
	public CDateHyp(CDateHyp rADateHyp) {
		ctHypType = TConceptType.ctDate;
		cValue = rADateHyp.cValue;
		fConfidence = rADateHyp.fConfidence;
	}

	// D: from bool + confidence constructor
	public CDateHyp(Calendar cAValue, float fAConfidence) {
		ctHypType = TConceptType.ctDate;
		cValue = cAValue;
		fConfidence = fAConfidence;
	}

	//-----------------------------------------------------------------------------
	// CDateHyp: Overwritten, CDateHyp specific methods
	//-----------------------------------------------------------------------------

	

	// D: Indexing operator - return an error, should never be called
	public CHyp getIndexing(String sItem) {
		Log.e(Const.CDATECONCEPT,"Indexing operator [] called on CDateHyp.");
	    return null;
	}

	// D: Convert value to String
	public String ValueToString() {
		SimpleDateFormat sdf = 
				new SimpleDateFormat("yyyyÄêMMÔÂddÈÕ");
		String sStringDate = sdf.format(cValue.getTime());
		return sStringDate;
	}

	// D: Convert hyp to String
	public String ToString() {
		String sResult = "|"+fConfidence;
		sResult = ValueToString() + sResult;
		return sResult;
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
			sNewValue = sNewValue.substring(1, sNewValue.length()-1);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			this.cValue.setTime(sdf.parse(sNewValue));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// get the confidence 
	    if(sConfidence == "") {
	        fConfidence = (float) 1.0;
	    } else {
	    	try{
	    		fConfidence = Float.parseFloat(sConfidence);
	    	}catch(Exception e){
	    		 Log.e(Const.CDATECONCEPT,"Cannot perform conversion to CStringHyp from "
	    				 +sString);
	    	}
	    }
	}

}
public class CDateConcept extends CConcept implements ConceptFactory{
	


	//-----------------------------------------------------------------------------
	// CDateConcept class - this is the boolean concept class. It overloads and
//							provides some new constructors, and operators
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// CDateConcept: Constructors and destructors
	//-----------------------------------------------------------------------------

	// D: constructor by name
	CDateConcept(String sAName, TConceptSource csAConceptSource){
	    super(sAName, csAConceptSource);
		ctConceptType = TConceptType.ctDate;
	}

	// D: constructor from name + value + confidence
	public CDateConcept(String sAName, Calendar cAValue, float fAConfidence,
							   TConceptSource csAConceptSource) {
		ctConceptType = TConceptType.ctDate;
		csConceptSource = csAConceptSource;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
	    vhCurrentHypSet.add(new CDateHyp(cAValue, fAConfidence));
	    iNumValidHyps = 1;
	    iCardinality = 100;
	    iTurnLastUpdated = -1;
	    bWaitingConveyance = false;
		cConveyance = TConveyance.cNotConveyed;
	    SetHistoryConcept(false);
	    pPrevConcept = null;
	}
	public CDateConcept() {
		// TODO Auto-generated constructor stub
	}
	//-----------------------------------------------------------------------------
	// Overwritten methods for overall concept manipulation
	//-----------------------------------------------------------------------------

	// D: assignment operator from boolean
	public CConcept Assignment(Calendar cAValue) {
	    // check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CDATECONCEPT,"Cannot assign to concept ("+sName+") history.");

		// o/w clear the current hypothesis set
	    ClearCurrentHypSet();
		// add the hypothesis (this will notify the change)
	    AddHyp(new CDateHyp(cAValue, (float) 1.0));
		// set the concept to grounded
		SetGroundedFlag(true);
		return this;
	}

	// D: returns an empty clone of the concept (basically just preserving the
//	    type, but not the contents
	public CConcept EmptyClone() {
	    return new CDateConcept();
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods related to the current hypothesis set and belief
	// manipulation
	//-----------------------------------------------------------------------------

	// D: factory method for hypotheses
	public CHyp HypFactory() {
		return new CDateHyp();
	}
	@Override
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated method stub
		return new CDateConcept(sAName, csAConceptSource);
	}

}
