package dmcore.concepts;

import java.util.ArrayList;
import java.util.HashSet;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import android.util.Log;

import dmcore.agents.coreagents.CDMCoreAgent;
import dmcore.agents.coreagents.CRegistry;
import dmcore.agents.coreagents.DMCore;
import dmcore.agents.dialogagents.CDialogAgent;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TConveyance;
import dmcore.agents.mytypedef.TSystemActionOnConcept;
import dmcore.grounding.groundingmodel.CGMConcept;
import dmcore.grounding.groundingmodel.CGroundingModel;

//-----------------------------------------------------------------------------
//Definitions of concept types
//-----------------------------------------------------------------------------

enum TConceptType{ 
	ctUnknown, ctInt, ctBool, ctString, ctFloat, ctDate,ctStruct, 
            ctFrame, ctArray 
  };

//-----------------------------------------------------------------------------
//
//CConcept class - this is the base of the hierarchy of concept classes. It 
//                implements the basic properties and functionality of 
//                a concept
//
//-----------------------------------------------------------------------------
public class CConcept {
	
	//---------------------------------------------------------------------
    // Protected CConcept class members
	//---------------------------------------------------------------------

    // concept type and source
    //
	protected TConceptType ctConceptType;			
	protected TConceptSource csConceptSource;		

    // concept name
	protected String sName;						

    // the owner dialog agent
	protected CDialogAgent pOwnerDialogAgent;

    // the owner concept
	protected CConcept pOwnerConcept;

    // the grounding model
	protected CGMConcept pGroundingModel;

	// the grounded flag
	protected boolean bGrounded;

    // the invalidated flag (indicates that the concept was available, 
    // but then invalidated
	protected boolean bInvalidated;
    
    // the restored_for_grounding flag (indicates that the concept was
    // reopened, but the grounding engined restored it because the 
    // value that was pushed in history needs to undergo grounding; when
    // this flag is set the current hyp undergoes grounding, and, when
    // the current hyp is grounded, it is compared with the history hyp;
    // if they are the same, then the current hyp is deleted, and basically
    // the concept is reopened back)
	protected boolean bRestoredForGrounding;

	// the sealed flag (can be set through the Seal() function, and is 
	//  set to false by all operations that alter the concept)
	protected boolean bSealed;

	// the change notification flag (when this is set to false, the 
	// concept no longer notifies changes)
	protected boolean bChangeNotification;

    // the set of hypotheses, number of valid hypotheses, and cardinality
    //
	protected ArrayList<CHyp> vhCurrentHypSet=new ArrayList<CHyp>();
	protected int iNumValidHyps;

	// the set of partial hypotheses
	//
	protected ArrayList<CHyp> vhPartialHypSet=new ArrayList<CHyp>();
	protected int iNumValidPartialHyps;
	
	protected int iCardinality;


    // information about when the last concept update was performed
	protected int iTurnLastUpdated;

    // conveyance information (has the concept been conveyed to the user?)
    //
	protected TConveyance cConveyance;
	protected boolean bWaitingConveyance;

    // history information 
    //
	protected CConcept pPrevConcept;            
	protected boolean bHistoryConcept;
    
    // store the hypothesis that has already been explicitly confirmed 
    // for this concept (as a String);
	protected String sExplicitlyConfirmedHyp;
    
    // store the hypothesis that has already been explicitly disconfirmed 
    // for this concept (as a String);
	protected String sExplicitlyDisconfirmedHyp;
	//-----------------------------------------------------------------------------
	// CConcept: Constructors and destructors
	//-----------------------------------------------------------------------------
	// empty consturctor
	public CConcept(){
		
	}
	// D: default constructor
	public CConcept(String sAName, TConceptSource csAConceptSource, 
	                   int iACardinality) {
		ctConceptType = TConceptType.ctUnknown;
		csConceptSource = csAConceptSource;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
		bGrounded = false;
		bInvalidated = false;
		bRestoredForGrounding = false;
		bSealed = false;
		bChangeNotification = true;
		iNumValidHyps = 0;
	    iCardinality = iACardinality;
	    iTurnLastUpdated = -1;
		cConveyance = TConveyance.cNotConveyed;
	    bWaitingConveyance = false;
	    pPrevConcept = null;
	    bHistoryConcept = false;
	    sExplicitlyConfirmedHyp = "";
	    sExplicitlyDisconfirmedHyp = "";
	}
	public CConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated constructor stub
		ctConceptType = TConceptType.ctUnknown;
		csConceptSource = csAConceptSource;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
		bGrounded = false;
		bInvalidated = false;
		bRestoredForGrounding = false;
		bSealed = false;
		bChangeNotification = true;
		iNumValidHyps = 0;
	    iCardinality = 100;
	    iTurnLastUpdated = -1;
		cConveyance = TConveyance.cNotConveyed;
	    bWaitingConveyance = false;
	    pPrevConcept = null;
	    bHistoryConcept = false;
	    sExplicitlyConfirmedHyp = "";
	    sExplicitlyDisconfirmedHyp = "";
	}

	//-----------------------------------------------------------------------------
	// CConcept: Methods for overall concept manipulation
	//-----------------------------------------------------------------------------

	public CConcept(String sAName) {
		// TODO Auto-generated constructor stub
		ctConceptType = TConceptType.ctUnknown;
		sName = sAName;
	    pOwnerDialogAgent = null;
	    pOwnerConcept = null;
	    pGroundingModel = null;
		bGrounded = false;
		bInvalidated = false;
		bRestoredForGrounding = false;
		bSealed = false;
		bChangeNotification = true;
		iNumValidHyps = 0;
	    iCardinality = 100;
	    iTurnLastUpdated = -1;
		cConveyance = TConveyance.cNotConveyed;
	    bWaitingConveyance = false;
	    pPrevConcept = null;
	    bHistoryConcept = false;
	    sExplicitlyConfirmedHyp = "";
	    sExplicitlyDisconfirmedHyp = "";
	}
	
	// D: clears the contents of the concept
	public void Clear() {
	    // check if it's a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform Clear on concept ("+sName+") history.");

	    // record the initial value of the concept (if the concept has a grounding
	    //  model)
	    String sInitialValue = "";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

		// o/w delete all it's history
	    if(pPrevConcept != null) {
	        pPrevConcept = null;
	    }

	    // and clear the current value (notifies the change)
	    ClearCurrentHypSet();

	    // now log the update (if the concept has a grounding model
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG, "Concept update [clear] on "+
	        		GetAgentQualifiedName()+":\nInitial value dumped below:\n"+
	        		sInitialValue+"\nUpdated value dumped below:\n"+
	        		Utils.TrimRight(HypSetToString()," ")); 
	            
	            
	    // clear the explicitly confirmed hyp
	    sExplicitlyConfirmedHyp = "";
	    // and the explicitly disconfirmed hyp
	    sExplicitlyDisconfirmedHyp = "";
	}

	// D: clears the current value of the concept
	public void ClearCurrentValue() {
	    // check if it's a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,
	            "Cannot perform ClearCurrentValue on concept ("+
	            		sName+") history.");

	    // record the initial value of the concept (if the concept has a grounding
	    //  model)
	    String sInitialValue = "";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

		// o/w clear the current value (this notifies the change)
	    ClearCurrentHypSet();

	    // now log the update (if the concept has a grounding model
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG, "Concept update [clear_current_value] on "+
	        		GetAgentQualifiedName()+":\nInitial value dumped below:\n"+
	        		sInitialValue+"\nUpdated value dumped below:\n"+
	        		Utils.TrimRight(HypSetToString()," ")); 
	          
	}

	// D: clones the concept - essentially produces an exact replica of the concept, 
	//	     only that it does not notify changes; nor it waits for conveyance; nor it
	//	     has a grounding model since this is a newly created copy; 
	public CConcept Clone(boolean bCloneHistory) {
	    // start with an empty clone
		CConcept pConcept = EmptyClone();

	    // and fill in all the members
	    pConcept.SetConceptType(ctConceptType);
	    pConcept.SetConceptSource(csConceptSource);
	    pConcept.sName = sName;
	    pConcept.pOwnerDialogAgent = pOwnerDialogAgent;
	    pConcept.SetOwnerConcept(pOwnerConcept);
		// a clone does not have a grounding model
		pConcept.pGroundingModel = null;
		pConcept.bSealed = bSealed;
		// a clone does not notify
		pConcept.DisableChangeNotification();
		// now copy the values
	    pConcept.CopyCurrentHypSetFrom(this);
		// and set the grounded state
		pConcept.SetGroundedFlag(bGrounded);
		// set the invalidated flag
		pConcept.SetInvalidatedFlag(bInvalidated);
		// set the restored for grounding
		if(bCloneHistory) 
		    pConcept.SetRestoredForGroundingFlag(bRestoredForGrounding);
		else
		    pConcept.SetRestoredForGroundingFlag(false);
		pConcept.iCardinality = iCardinality;
	    pConcept.SetTurnLastUpdated(iTurnLastUpdated);
	    pConcept.cConveyance = cConveyance;
		// a clone does not wait for conveyance
	    pConcept.bWaitingConveyance = false;
	    pConcept.SetHistoryConcept(bHistoryConcept);
		// now clone the history if required
		if(bCloneHistory && (pPrevConcept != null)) 
			pConcept.pPrevConcept = pPrevConcept.Clone(true);
		else
			pConcept.pPrevConcept = null;		
	    // set the explicitly confirmed and disconfirmed hyps
	    pConcept.sExplicitlyConfirmedHyp = sExplicitlyConfirmedHyp;    
	    pConcept.sExplicitlyDisconfirmedHyp = sExplicitlyDisconfirmedHyp;    

		// finally, return the clone
	    return pConcept;
	}

	// D: returns an empty clone of the concept (basically just preserving the
	//	    type, but not the contents
	public CConcept EmptyClone() {
		CConcept EmptyClone = new CConcept();
	    return EmptyClone;
	}
	// D: assignment operator from String
	public CConcept Assignment(String sAValue) {
	    // call upon the AssignFromString update
		Update(Const.CU_ASSIGN_FROM_STRING, sAValue);
	    return this;
	}
	
	@Override
	// D: equality operator
	public boolean equals(Object rAConcept) {

	    // compare the top hypotheses for the 2 concepts
	    CHyp pTopHyp = GetTopHyp();
	    CHyp pATopHyp = ((CConcept) rAConcept).GetTopHyp();

		// if the two top-hyps are null (the two concepts didn't receive
		// any value), the sets are equal
		if ((pTopHyp == null) && (pATopHyp == null)) {
			return true;
		}

		// if only one of them is null they are not equal
		else if ((pTopHyp == null) || (pATopHyp == null)) {
			return false;
		}

		// last, check using the equality operator on the mode
	    return pTopHyp.equals(pATopHyp);
	}

	// D: comparison operator
	public boolean lessthan(CConcept rAConcept) {
	    // compare the 2 top hypotheses
	    CHyp pTopHyp = GetTopHyp();
	    if(pTopHyp == null) return false;
	    CHyp pATopHyp = rAConcept.GetTopHyp();
	    if(pATopHyp == null) return false;
	    return pTopHyp.lessthan(pATopHyp);
	}

	// D: comparison operator
	public boolean greaterthan(CConcept rAConcept) {
	    // compare the 2 top hypotheses
	    CHyp pTopHyp = GetTopHyp();
	    if(pTopHyp == null) return false;
	    CHyp pATopHyp = rAConcept.GetTopHyp();
	    if(pATopHyp == null) return false;
	    return pTopHyp.greaterthan(pATopHyp);
	}

	// D: comparison operator
	public boolean lessequal (CConcept rAConcept) {
	    // compare the 2 top hypotheses
	    CHyp pTopHyp = GetTopHyp();
	    if(pTopHyp == null) return false;
	    CHyp pATopHyp = rAConcept.GetTopHyp();
	    if(pATopHyp == null) return false;
	    return pTopHyp.lessequal(pATopHyp);
	}

	// D: comparison operator
	public boolean greaterequal(CConcept rAConcept) {
	    // compare the 2 top hypotheses
	    CHyp pTopHyp = GetTopHyp();
	    if(pTopHyp == null) return false;
	    CHyp pATopHyp = rAConcept.GetTopHyp();
	    if(pATopHyp == null) return false;
	    return pTopHyp.greaterequal(pATopHyp);
	}
	// D: indexing operator with integer argument
	public CConcept getIndexing(int iIndex) {
	    // if history-addressing self
	    if(iIndex == 0) 
	        return this;
	    // check if index is negative, then return the concept in history
	    if((iIndex < 0) && (pPrevConcept != null)) {
	        // if adressing previous in history, return that
	        if(iIndex == -1) {
	            return pPrevConcept;
	        } else {
	            // o/w go recursively
	            return pPrevConcept.getIndexing(iIndex + 1);
	        }
	    } else {
		    Log.e(Const.CONCEPT_STREAM_TAG,"Indexing operator ["
		    		+iIndex+"] on "+sName+" (atomic "
		    		+ctConceptType.toString()+") failed.");
		    return null;	
	    }
	}

	// D: indexing operator with String argument
	public CConcept getIndexing(String sIndex) {
	    // if index empty, return self
	    if(sIndex.length()==0) {
			return this;
		} else {
	        // get the first part of the index , if index is i.i.i
			SplitReturnType srt =Utils.SplitOnFirst(sIndex, ".");
	        String sFirstIndex=srt.FirstPart;
	        String sFollowUp = srt.SecondPart;
	        // try to convert to an integer
	        int iFirstIndex;
	        try{
	        	iFirstIndex = Integer.valueOf(sFirstIndex);
	        	// o/w chain the operators
	            CConcept pConcept = getIndexing(iFirstIndex);
	            if(sFollowUp.length()==0) 
	                return pConcept;
	            else 
	                return pConcept.getIndexing(sFollowUp);
	        }catch(Exception e){
	        	Log.e(Const.CONCEPT_STREAM_TAG,"Indexing operator ["
			    		+sIndex+"] on "+sName+" (atomic "
			    		+ctConceptType.toString()+") failed.");
			    return null;	
	        }
	        
		}
	}
	//-----------------------------------------------------------------------------
	// Virtual methods implementing various types of updates
	//-----------------------------------------------------------------------------

	// D: update the concept
	public void Update(String sUpdateType, Object pUpdateData) {

	    // record the initial value of the concept (if the concept has a grounding
	    //  model
	    String sInitialValue = "";
		String sInitialPartialValue="";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

		// call the appropriate function based on the belief updating model and 
		// on the update type
		String sBeliefUpdatingModelName = "npu";
//	DMCore.pGroundingManager.GetBeliefUpdatingModelName();
		if(sBeliefUpdatingModelName.equals("npu")){
			if(sUpdateType == Const.CU_ASSIGN_FROM_STRING)
				Update_NPU_AssignFromString(pUpdateData);
			else if(sUpdateType == Const.CU_ASSIGN_FROM_CONCEPT)
				Update_NPU_AssignFromConcept(pUpdateData);
			else if(sUpdateType == Const.CU_UPDATE_WITH_CONCEPT) 
				Update_NPU_UpdateWithConcept(pUpdateData);
			else if(sUpdateType == Const.CU_COLLAPSE_TO_MODE) 
				Update_NPU_CollapseToMode(pUpdateData);
			else if(sUpdateType == Const.CU_PARTIAL_FROM_STRING)
				Update_PartialFromString(pUpdateData);
			else Log.e(Const.CONCEPT_STREAM_TAG,
				"Unknown update type ("+sUpdateType+") in updating concept "+
						GetAgentQualifiedName());

			// if we got a final update, erase the previous partial one
			if (sUpdateType != Const.CU_PARTIAL_FROM_STRING) {
				ClearPartialHypSet();
			}

		} else if(sBeliefUpdatingModelName == "calista") {
			if(sUpdateType == Const.CU_ASSIGN_FROM_STRING)
				Update_Calista_AssignFromString(pUpdateData);
			else if(sUpdateType == Const.CU_ASSIGN_FROM_CONCEPT)
				Update_Calista_AssignFromConcept(pUpdateData);
			else if(sUpdateType == Const.CU_UPDATE_WITH_CONCEPT) 
				Update_Calista_UpdateWithConcept(pUpdateData);
			else if(sUpdateType == Const.CU_COLLAPSE_TO_MODE) 
				Update_Calista_CollapseToMode(pUpdateData);
			else Log.e(Const.CONCEPT_STREAM_TAG,
				"Unknown update type ("+sUpdateType+") in updating concept "+
						GetAgentQualifiedName());
		}

	    // now log the update (if the concept has a grounding model
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG,"Concept update ["+
	        		sUpdateType+"] on "+GetAgentQualifiedName()+
	        		":\nInitial value dumped below:\n"+sInitialValue+
	        		"\nUpdated value dumped below:\n"+Utils.TrimRight(HypSetToString()," "));
	}

	// ----------------------------------------------------------------------------
	// D: Update function for the Naive Probabilistic update model
	// ----------------------------------------------------------------------------

	// D: Naive probabilistic update scheme - assign from String
	public void Update_NPU_AssignFromString(Object pUpdateData) {
	    // first, check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (AssignFromString) update on concept ("
	        		+sName+") history.");

		// o/w 
	    String sValConf="";			            // one hyp pair
		String sRest = (String)pUpdateData;	// the rest of the String

		// first clear the current hyp-set of the concept
	    ClearCurrentHypSet();

		// then go through each hyp pair and create it from the String
		while(sRest.length() > 0) {
			// grab the first hyp pair
			SplitReturnType srt = Utils.SplitOnFirst(sRest, ";");
			sValConf = srt.FirstPart;
			sRest = srt.SecondPart;
			// create the new value-confidence (this will notify the change)
			int iIndex = AddNewHyp();

	        // acquire it from String
			vhCurrentHypSet.get(iIndex).FromString(sValConf);
		}
	}

	// D: Naive probabilistic update scheme - assign from another concept
	public void Update_NPU_AssignFromConcept(Object pUpdateData) {
	    // first, check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform AssignFromConcept update "
	        	+"on concept ("+sName+") history.");

	    // copy the set of hypotheses (this will notify the change)
	    CopyCurrentHypSetFrom((CConcept )pUpdateData);
	}

	// D: Naive probabilistic update scheme - perform update with another concept
	public void Update_NPU_UpdateWithConcept(Object pUpdateData) {
	    // first, check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (UpdateWithConcept) update "
				+"on concept ("+sName+") history.");

		// get a pointer at the concept
		CConcept pConcept = (CConcept)pUpdateData;

		// get a pointer to the top-level hypothesis
		CHyp pOldTopHyp = GetTopHyp();
		String sOldTopHyp = "";
		if(pOldTopHyp != null) sOldTopHyp = pOldTopHyp.ValueToString();

		// figure out if the concept is undergoing an explicit or an implicit 
		// confirmation
		TSystemActionOnConcept saocAction = 
			DMCore.pDMCore.GetSystemActionOnConcept(this);
		boolean bIsUndergoingExplicitConfirm = 
			(saocAction.sSystemAction == Const.SA_EXPL_CONF);
		boolean bIsUndergoingImplicitConfirm = 
			(saocAction.sSystemAction == Const.SA_IMPL_CONF);	

		// if the incoming concept is not empty, then perform a naive probabilistic 
		// update on the concept
		if(pConcept!=null && pConcept.IsUpdated()) {

			// set the cardinality to the largest cardinality
			if(iCardinality < pConcept.iCardinality)
				iCardinality = pConcept.iCardinality;

			// hold the confidences in 2 arrays vfConf1, vfConf2
			ArrayList<Float> vfConf1 = new ArrayList<Float>();
			ArrayList<Float> vfConf2 = new ArrayList<Float>();

			// copy the confidences for the first set and sum them up
			float fConf1Sum = 0;
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++) {
				if(vhCurrentHypSet.get(i) != null) {
					vfConf1.add(vhCurrentHypSet.get(i).GetConfidence());
					fConf1Sum += vfConf1.get(i);
				} else {
					vfConf1.add((float) 0);
				}        

			Log.d(Const.CONCEPT_STREAM_TAG, "vfConf1["+i+"]="+ vfConf1.get(i));
			}

			// sum up the confidences for the second set
			float fConf2Sum = 0;
			for(int i = 0; i < (int)pConcept.vhCurrentHypSet.size(); i++)
				if(pConcept.vhCurrentHypSet.get(i) != null) 
					fConf2Sum += pConcept.vhCurrentHypSet.get(i).GetConfidence();

			// compute the confidences for the "unknown" values in sets 1 and 2
			float fUnkConf1;
			if(iCardinality == (int)vhCurrentHypSet.size()) 
				fUnkConf1 = 0;
			else 
				fUnkConf1 = (1 - fConf1Sum) / (iCardinality - vhCurrentHypSet.size());

			float fUnkConf2;
			if(iCardinality == (int)pConcept.vhCurrentHypSet.size()) 
				fUnkConf2 = 0;
			else 
				fUnkConf2 = (1 - fConf2Sum) / (iCardinality - pConcept.vhCurrentHypSet.size());

			// update the vfConf2 vector
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++) 
				vfConf2.add(fUnkConf2);

			// now go through the second vector, and add the values one by one, checking
			// if they are already in the set
			int iOrigSetSize = vhCurrentHypSet.size();
			for(int i = 0; i < (int)pConcept.vhCurrentHypSet.size(); i++) {
				boolean bFound = false;
				int j = 0;
				if(pConcept.vhCurrentHypSet.get(i) != null) {
					for(j = 0; j < iOrigSetSize; j++) {
						if(pConcept.vhCurrentHypSet.get(i) == vhCurrentHypSet.get(j)) {
							bFound = true;
							break;
						}
					}
				}
				// if found in the set
				if(bFound) {
					// just set the appropriate confidence value
					vfConf2.set(j, pConcept.vhCurrentHypSet.get(i).GetConfidence());
				} else {
					// o/w add it to the set
					if(pConcept.vhCurrentHypSet.get(i) != null) {
						// add a new hypothesis (this will notify the change)
						int iIndex = AddNewHyp();
						vhCurrentHypSet.set(iIndex,pConcept.vhCurrentHypSet.get(i));
						// and set the confidences right
						vfConf1.add(fUnkConf1);
						vfConf2.add(pConcept.vhCurrentHypSet.get(i).GetConfidence());
					} else {
						AddNullHyp();
						// and set the confidences right
						vfConf1.add(fUnkConf1);
						vfConf2.add((float) 0);
					}
				}
			}
			
			// finally, multiply the scores in 
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++) {
				if(vhCurrentHypSet.get(i) != null) 
					// this will also notify the change
					SetHypConfidence(i, vfConf1.get(i) * vfConf2.get(i));
			}
			
			// compute the normalizing factor
			float fNormalizer = 0;
			for(int i = 0; i < (int)vfConf1.size(); i++)
				fNormalizer += vfConf1.get(i) * vfConf2.get(i);
			fNormalizer += (iCardinality-vfConf1.size())*fUnkConf1*fUnkConf2;

			// and update the confidences
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++)
				if(vhCurrentHypSet.get(i) != null)
					// this will also notify the change
					SetHypConfidence(i, (vfConf1.get(i) * vfConf2.get(i)) / fNormalizer);

			// now, make sure that at least Const.FREE_PROB_MASS is allocated to the rest
			fNormalizer = 0;
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++)
				if(vhCurrentHypSet.get(i) != null)
					fNormalizer += vhCurrentHypSet.get(i).GetConfidence();
			if(fNormalizer > 1 - Const.FREE_PROB_MASS) {
				// if we're over the limit
				for(int i = 0; i < (int)vhCurrentHypSet.size(); i++)
					if(vhCurrentHypSet.get(i) != null)
						// this will also notify the change
						SetHypConfidence(i, 
							vhCurrentHypSet.get(i).GetConfidence() * 
							(1 - Const.FREE_PROB_MASS) / fNormalizer);
			}
		}

		// now, if the concept is undergoing an explicit confirmation, do the 
		// update for explicit confirmations
		if(bIsUndergoingExplicitConfirm) {
			// first check that the top hypothesis has stayed the same
			CHyp pNewTopHyp = GetTopHyp();
			String sNewTopHyp = "";
			if(pNewTopHyp != null) sNewTopHyp = pNewTopHyp.ValueToString();
			if((sOldTopHyp == sNewTopHyp) && (sOldTopHyp != "")) {
				// now do the confirmation

				// check if the confirm was bound with a YES or a NO
				String sAgencyName = 
						"/_ExplicitConfirm["+GetAgentQualifiedName()+"]"; 
				CDialogAgent pdaExplConfirmAgency = 
					((CDialogAgent)CRegistry.AgentsRegistry.getAgentGivenName(sAgencyName));
				String sRequestAgentName = 
						"/_ExplicitConfirm["+GetAgentQualifiedName()+"]/RequestConfirm"; 
				CDialogAgent pdaRequestConfirmAgent = 
					(CDialogAgent)CRegistry.AgentsRegistry.getAgentGivenName(sRequestAgentName);
				CConcept rConfirmConcept = 
					pdaExplConfirmAgency.getConceptFromPath("confirm");
				boolean bYes = rConfirmConcept.IsAvailableAndGrounded() &&
					(rConfirmConcept!=null);
				boolean bNo = rConfirmConcept.IsAvailableAndGrounded() &&
					(rConfirmConcept==null);
				boolean bTooManyNonUnderstandings = false;
				if (pdaExplConfirmAgency.HasParameter("max_attempts")) {
					bTooManyNonUnderstandings = 
						pdaRequestConfirmAgent.GetTurnsInFocusCounter() >=
						pdaRequestConfirmAgent.GetMaxExecuteCounter()-1;
				}

				// now do the update depending on the confirm
				if(bYes) {
				    // get the top hypothesis number
					int iIndex = GetTopHypIndex();
					// now do the update (this will notify the change)
					SetHypConfidence(iIndex, 1 - Const.FREE_PROB_MASS);
					// do the notify change again, just in case the hyp confidence
					// was 1-Const.FREE_PROB_MASS to begin with
					NotifyChange();
					// set the explicitly confirmed hyp
					SetExplicitlyConfirmedHyp(GetHyp(iIndex));
					// and delete all the other ones before it
					for(int h = 0; h < iIndex; h++) 
						DeleteHyp(0);
					// and after it
					while(GetNumHyps() > 1) DeleteHyp(1);
					// finally, set the concept to grounded
					SetGroundedFlag(true);				
				} else if(bNo || bTooManyNonUnderstandings) {
				    // get the top hypothesis number
					int iIndex = GetTopHypIndex();
					// delete the top hypothesis (this will notify the change)
					DeleteHyp(iIndex);
					// set the explicitly disconfirmed hyp (only if the
					// user actually said "NO", not on non-understandings)
					if (bNo)
						SetExplicitlyDisconfirmedHyp(sOldTopHyp);
					// if we're not left with any valid hyps, set the
					// invalidated flag
					SetInvalidatedFlag(GetNumValidHyps()==0);
				}
			}
		}

		// finally, if the concept is undergoing an implicit confirmation, do the 
		// update for implicit confirmations; apply the heuristic only if the concept
		// is still sealed, and only if the concept has not been reopened in the 
		// meantime
		if(bIsUndergoingImplicitConfirm && IsSealed() && IsUpdated()) {
		
			// check if the confirm was bound with a YES or a NO
			String sAgencyName = 
					"/_ImplicitConfirmExpect["+GetAgentQualifiedName()+"]";
			CConcept rConfirmConcept = 
				((CDialogAgent)CRegistry.AgentsRegistry.getAgentGivenName(sAgencyName)).getConceptFromPath("confirm");
			boolean bNo = rConfirmConcept.IsAvailableAndGrounded() &&
				(rConfirmConcept==null);

			int iIndex = GetTopHypIndex();

			// if we heard a No, delete that hypothesis
			if(bNo) {
				// get the top hypothesis number
				DeleteHyp(iIndex);
				// if we're not left with any valid hyps, set the
				// invalidated flag
				SetInvalidatedFlag(GetNumValidHyps()==0);
			} else {
				// o/w we need to boost the confidence of the top hypothesis
				// to 0.95

				// now do the update (this will notify the change)
				SetHypConfidence(iIndex, 1 - Const.FREE_PROB_MASS);

				// and delete all the other ones before it
				for(int h = 0; h < iIndex; h++) 
					// this will also notify the change
					DeleteHyp(0);

				// and after it
				while(GetNumHyps() > 1) DeleteHyp(1);

				// finally, set the concept to grounded
				SetGroundedFlag(true);
			}
		}		
	}

	// D: Naive probabilistic update scheme - update the value of a concept by
	//	    collapsing it to the mode
	public void Update_NPU_CollapseToMode(Object pUpdateData) {
		Log.d(Const.CONCEPT_STREAM_TAG,"llc:call method:Update_NPU_CollapseToMode");
	    // first, check that it's not a history concept
	    /*if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (CollapseToMode) update "
				"on concept (%s) history.", sName));

	    // get the top hypothesis number
	    int iIndex = GetTopHypIndex();

	    // if there is no top hyp, return
	    if(iIndex == -1) return;

		// now do the update (this will notify the change)
		SetHypConfidence(iIndex, 1 - Const.FREE_PROB_MASS);

		// and delete all the other ones before it
		for(int h = 0; h < iIndex; h++) 
			// this will also notify the change
			DeleteHyp(0);

		// and after it
		while(GetNumHyps() > 1) DeleteHyp(1);

		// finally, set the concept to grounded
		SetGroundedFlag(true);
		*/
	}

	// A: assigns a partial hypothesis value to the concept
	public void Update_PartialFromString(Object pUpdateData) {
	    // first, check that it's not a history concept
		Log.d(Const.CONCEPT_STREAM_TAG,"llc:call method:Update_PartialFromString");
	    /*
		if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (PartialFromString) update on "
	        		+"concept ("+sName+") history.");

		// o/w 
	    String sValConf="";			            // one hyp pair
		String sRest = (String)pUpdateData;	// the rest of the String

		// reset the partial hyp set
		ClearPartialHypSet();

		Log.d(Const.CONCEPT_STREAM_TAG, "Partial update for concept "+sName+": "+sRest);

		// then go through each hyp pair and create it from the String
		while(sRest.length() > 0) {
			// grab the first hyp pair
			SplitReturnType srt = new SplitReturnType();
			srt = Utils.SplitOnFirst(sRest, ";");
			sValConf = srt.FirstPart;
			sRest=srt.SecondPart;

			// create the new value-confidence (this will notify the change)
		    int iNewIndex = AddNewPartialHyp();

			// acquire it from String
			vhPartialHypSet.get(iNewIndex).FromString(sValConf);
		}
		*/
	}

	// ----------------------------------------------------------------------------
	// D: Update function for the Calista update model
	// ----------------------------------------------------------------------------

	// D: Calista belief updating scheme - assign from String
	public void Update_Calista_AssignFromString(Object pUpdateData) {
		// redirect to the NPU update
		Update_NPU_AssignFromString(pUpdateData);
	}

	// D: Naive probabilistic update scheme - assign from another concept
	public void Update_Calista_AssignFromConcept(Object pUpdateData) {
	    // redirect to the NPU update
		Update_NPU_AssignFromConcept(pUpdateData);
	}

	// D: Calista belief updating scheme - perform update with another concept
	//	    this is basically the bind update
	public void Update_Calista_UpdateWithConcept(Object pUpdateData) {
	    
		// if we don't have a grounding model on this concept, defer to the NPU 
		// type update (Calista only updates concepts that undergo grounding)
		Log.d(Const.CONCEPT_STREAM_TAG,"llc:call method:Update_Calista_UpdateWithConcept");
		/*if(pGroundingModel==null) {
			Update_NPU_UpdateWithConcept(pUpdateData);
			return;
		}

		// first, check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (UpdateWithConcept) update "
	        		+"on concept ("+sName+") history.");

		CConcept pConcept = (CConcept )pUpdateData;

		// identify the model that we want, corresponding to the system action
		TSystemActionOnConcept saocAction = 
			DMCore.pDMCore.GetSystemActionOnConcept(this);
			
	    // now get the model based on the system action
		STRING2FLOATVECTOR s2vfModel = 
			DMCore.pGroundingManager.GetBeliefUpdatingModelForAction(
				saocAction.sSystemAction);

		// if we are doing a request on this concept or an other type update, 
		// and we don't have a new value, 
		if((pConcept==null || !pConcept.IsUpdated()) && 
			((saocAction.sSystemAction == Const.SA_REQUEST) || 
			 (saocAction.sSystemAction == Const.SA_OTHER))) 
			// then simply return
			return;

	    // now identify the top hypothesis from history
	    int iIndexH_TH = pPrevConcept != null?pPrevConcept.GetTopHypIndex():-1;
	    CHyp phH_TH = null;
		float fConfH_TH = 0;
		if(iIndexH_TH != -1) {
			phH_TH = pPrevConcept.GetHyp(iIndexH_TH);
			fConfH_TH = phH_TH.GetConfidence();
		}        

		// now identify the top hypothesis from the initial concept	
		int iIndexI_TH = GetTopHypIndex();
		CHyp phI_TH = null;
		float fConfI_TH = 0;
		if(iIndexI_TH != -1) {
			phI_TH = GetHyp(iIndexI_TH);
			fConfI_TH = phI_TH.GetConfidence();
		}
		// now store the initial hyp String
		String sInitialTopHyp = phI_TH != null?phI_TH.ValueToString():"";
		
		// identify the top hypothesis from the new concept
		int iIndexNEW1 = -1;
		if(pConcept != null)
			iIndexNEW1 = pConcept.GetTopHypIndex();
		CHyp phNEW1 = null;
		float fConfNEW1 = 0;
		if(iIndexNEW1 != -1) { 
			phNEW1 = pConcept.GetHyp(iIndexNEW1);
			fConfNEW1 = phNEW1.GetConfidence();
		}

	    // now check if we have an empty_with_history
	    boolean bEmptyWithHistory = (iIndexI_TH == -1) && (iIndexH_TH != -1);
	    
	    // now define HOHH as a flag (this indicates that we will need to 
	    // use the history hypothesis as the initial hypothesis
	    boolean bICWithHOHH = bEmptyWithHistory && 
	        (saocAction.sSystemAction == CDMCoreAgent.CDMCoreAgent.Const.SA_IMPL_CONF);
	    boolean bUICWithH0HH = bEmptyWithHistory && 
	        (saocAction.sSystemAction == Const.SA_UNPLANNED_IMPL_CONF);
	  
		// now use that belief updating model to construct the confidence
		// scores
		vector <double, allocator <double> > vfConfs;
		vfConfs.resize(3);
		vfConfs[0] = vfConfs[1] = vfConfs[2] = 0;

		// go through each feature of the model and compute the sums
		DMCore.pGroundingManager.PrecomputeBeliefUpdatingFeatures(
		    this, pConcept, saocAction.sSystemAction);
		STRING2FLOATVECTOR::iterator iPtr;
		String sLogString;
		for(iPtr = s2vfModel.begin(); iPtr != s2vfModel.end(); iPtr++) {
			// get the feature value
			float fFeatureValue = 
				DMCore.pGroundingManager.GetGroundingFeature(iPtr.first);
			vfConfs[1] += fFeatureValue  (iPtr.second)[0];
			vfConfs[2] += fFeatureValue  (iPtr.second)[1];
			sLogString += "  %s = %.4f\t%.4f\t%.4f\n", 
			    iPtr.first, fFeatureValue, 
			    fFeatureValue  (iPtr.second)[0], 
			    fFeatureValue  (iPtr.second)[1]);
		}
	    sLogString += "  [TOTAL] = 1.0\t%.4f\t%.4f\n", 
	        vfConfs[1], vfConfs[2]);
		DMCore.pGroundingManager.ClearBeliefUpdatingFeatures();

		// exponentiate and normalize
		double fNormalizer = 0;
		for(int i = 0; i < 3; i++) {
			vfConfs[i] = exp(vfConfs[i]);
			fNormalizer += vfConfs[i];
		}
		for(int i = 0; i < 3; i++) {
			vfConfs[i] = vfConfs[i] / fNormalizer;
		}

		sLogString = 
			"Calista belief update [%s] on %s:\n[H:%.4f I:%.4f N:%.4f . 1:%.4f 2:%.4f O:%.4f]\n%s", 
			saocAction.sSystemAction, GetName(), 
			fConfH_TH, fConfI_TH, fConfNEW1, 
			vfConfs[0], vfConfs[1], vfConfs[2], sLogString);

		// now do some post-processing
		// if we are doing an explicit confirmation or an implicit confirmation
		// then threshold the top hypothesis by Const.FREE_PROB_MASS
		if((vfConfs[0] < Const.FREE_PROB_MASS) &&
		    ((saocAction.sSystemAction == CDMCoreAgent.Const.SA_EXPL_CONF) ||
		     (saocAction.sSystemAction == CDMCoreAgent.Const.SA_IMPL_CONF) ||
		     (saocAction.sSystemAction == SA_UNPLANNED_IMPL_CONF))) {
			vfConfs[2] += vfConfs[0];
			vfConfs[0] = 0;
		}
		// if we are doing a request or an other update, threshold the second hyp by
		// the free probability mass
		if((vfConfs[1] < Const.FREE_PROB_MASS) &&
		    ((saocAction.sSystemAction == SA_REQUEST) ||
		     (saocAction.sSystemAction == SA_OTHER))) {
			vfConfs[2] += vfConfs[1];
			vfConfs[1] = 0;
		}	
		// if we have an explicit confirmation and a no-marker, then kill the initial hyp
	    int iMarkDisconfirm = atoi(
	        pInteractionEventManager.GetValueForExpectation("[mark_disconfirm]"));
		if((saocAction.sSystemAction == CDMCoreAgent.Const.SA_EXPL_CONF) && (iMarkDisconfirm > 0)) 
		    vfConfs[0] = 0;

		// log the update
		Log.d(BELIEFUPDATING_STREAM, sLogString);    

	    // now analyse the results. IF we are doing an implicit confirmation
	    // with h0hh then we need to deal with this separately (we need some more
	    // careful post-processing)
	    if(bICWithHOHH || bUICWithH0HH) {

	        // here I'm applying a heuristic for HOHH updates with ImplicitConfirms
	        // this heuristic either leaves the concept untouched, or brings it back to 
	        // life from a reopen
	        
	        // store if we have a new top hypothesis
	        boolean bNewTopHyp = false;
	        
	        // decide if we need to revive the concept, or simply update the history
	        boolean bReviveConcept = 
	        // we revive the concept in the following conditions
	        // 1) there is a new hypothesis which a higher confidence score
	            ((iIndexNEW1 != -1) && (vfConfs[1] > vfConfs[0]) && !(phNEW1 == phH_TH)) || 
	        // 2) the confidence score on the initial hypothesis has dropped
	            (vfConfs[0] < fConfH_TH);
	        
	        // now, if we need to revive the concept
	        if(bReviveConcept) {
	            // add the first hyp
	            if(vfConfs[0] > 0) {
	                int iIndex = AddNewHyp();
	                (vhCurrentHypSet.get(iIndex)) = phH_TH;
	                // and set the confidence
	                SetHypConfidence(iIndex, (float)vfConfs[0]);
	            }
	            // add the second hyp if one exists and is different from the first
	            if((iIndexNEW1 != -1) && !(phNEW1 == phH_TH) && (vfConfs[1] > 0)) {                
	                int iIndex = AddNewHyp();
	                (vhCurrentHypSet.get(iIndex)) = phNEW1;
	                // and set the confidence
	                SetHypConfidence(iIndex, (float)vfConfs[1]);
	                // set that we have a new top hyp
	                bNewTopHyp = vfConfs[1] > vfConfs[0];
	            }
	            // now, if the confidence for the first hyp is zero and there is no second
	            // hyp, or the second hyp has zero conf, invalidate the concept
	            if((vfConfs[0] == 0) && ((iIndexNEW1 == -1) || (vfConfs[1] == 0))) {
	                // clear the history
	                ClearHistory();
	                // set the invalidated flag
	                SetInvalidatedFlag(true);
	            }
	            // finally, set the concept as restored for grounding
	            SetRestoredForGroundingFlag();
	        }
	        
	        // now if we have revived the concept (either desealed or invalidated it)
	        if(!IsSealed() && !IsInvalidated()) {
	            // then schedule it for grounding
	            String sAction = DMCore.pGroundingManager.ScheduleConceptGrounding(this);
	            // if the action is implicit confirm or accept, then we have to 
	            // kill the concept again and only update the history
	            if(((sAction == "IMPL_CONF") || (sAction == "ACCEPT")) && !bNewTopHyp) {
	                // signal that grounding has completed for this concept (we 
	                // don't want to run IMPL_CONFS and ACCEPTS again on the same
	                // top hyp)
	                DMCore.pGroundingManager.ConceptGroundingRequestCompleted(this);
	                // then we have to move this to history
	                operator[] (-1).CopyCurrentHypSetFrom(this);
	                // clear the current concept
	                ClearCurrentHypSet();
	                // clear the restored for grounding flag
	                SetRestoredForGroundingFlag(false);        
	                // and seal it back
	                Seal();
	            }
	        }

	    } else {

	        // o/w deal with the default case    

		    // now add the new hypothesis 
		    boolean bAddedNewHyp = false;
		    if((iIndexNEW1 != -1) &&
			    ((iIndexI_TH == -1) || !(phNEW1 == phI_TH))) {
			    int iIndexNEW1 = AddNewHyp();
			    (vhCurrentHypSet[iIndexNEW1]) = phNEW1;
			    // and set the confidences right
			    SetHypConfidence(iIndexNEW1, (float)vfConfs[1]);
			    // mark that we've added a new hyp
			    bAddedNewHyp = true;
		    }	

		    // set the confidence score for the initial hypothesis
		    boolean bDeletedI_TH = false;
		    if(iIndexI_TH != -1) {
			    if(vfConfs[0] != 0) {
				    SetHypConfidence(iIndexI_TH, (float)vfConfs[0]);
				    // also notify the change since sethypconfidence does
				    // not do it unless the confidence score is different
				    NotifyChange();
			    } else {
			        // o/w delete the initial hyp
				    DeleteHyp(iIndexI_TH);
				    iIndexI_TH = -1;
				    phI_TH = null;
				    // remember that we deleted the initial
				    bDeletedI_TH = true;
			    }
		    }

		    // finally clear the hypothesis set of all the rest
		    for(int i = 0; i < vhCurrentHypSet.size(); i++) {
			    // delete everything that's not the initial or final
			    if(((phI_TH == null) || !((vhCurrentHypSet.get(i)) == phI_TH)) &&
				    ((phNEW1 == null) || !((vhCurrentHypSet.get(i)) == phNEW1))) {
				    DeleteHyp(i);
			    }
			    // delete all the hypotheses that have confidence zero
			    if(vhCurrentHypSet.get(i).GetConfidence() == 0)
				    DeleteHyp(i);
		    }
		    
		    // finally, if we deleted the initial hyp and did not add a 
		    // new one, mark the concept as invalidated
	        SetInvalidatedFlag(bDeletedI_TH && !bAddedNewHyp);	    
	    }
	    
	    // now if we have an explicit confirmation with a Yes in it, then 
	    // set the explicitly confirmed hyp
	    if(saocAction.sSystemAction == CDMCoreAgent.Const.SA_EXPL_CONF) {
	        // get the mark_confirm indicator
	        int iMarkConfirm = atoi(
	            pInteractionEventManager.GetLastInput().GetValueForExpectation("[mark_confirm]"));
	        if(iMarkConfirm > 0)
	            // set the explicitly confirmed hyp
	            SetExplicitlyConfirmedHyp(GetTopHyp());    
	        // get the mark disconfirm indicator
	        int iMarkDisconfirm = atoi(
	            pInteractionEventManager.GetLastInput().GetValueForExpectation("[mark_disconfirm]"));
	        if(iMarkDisconfirm > 0)
	            // set the explicitly confirmed hyp
	            SetExplicitlyDisconfirmedHyp(sInitialTopHyp);        
	    }
*/
	}

	// D: Calista belief updating scheme - update the value of a concept by
	//	    collapsing it to the mode
	public void Update_Calista_CollapseToMode(Object pUpdateData) {

	    // first, check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform (CollapseToMode) update "
	        		+"on concept ("+sName+") history.");

	    // get the top hypothesis number
	    int iIndex = GetTopHypIndex();

	    // if there is no top hyp, return
	    if(iIndex == -1) return;

		// and delete all the other ones before it
		for(int h = 0; h < iIndex; h++) 
			// this will also notify the change
			DeleteHyp(0);

		// and after it
		while(GetNumHyps() > 1) DeleteHyp(1);

		// finally, set the concept to grounded
		SetGroundedFlag(true);
	}

	//-----------------------------------------------------------------------------
	// Virtual methods implementing various flags (state information) on the 
	// concept
	//-----------------------------------------------------------------------------

	// D: returns true if the concept is updated, i.e. its current hypotheses set 
//	    is not empty
	public boolean IsUpdated() {
		return GetNumValidHyps() > 0;
	}

	// D: returns true if the concept is updated and grounded (its current hyp set
//	    is not empty, and a hypothesis has been grounded
	public boolean IsUpdatedAndGrounded() {
		return IsUpdated() && IsGrounded();
	}

	// D: returns true if the concept is available, i.e. its current hypotheses 
//	    set is not empty (and the concept is not currently undergoing grounding) 
//	    or if there's a historical value available
	public boolean IsAvailable() {
	    if(IsUpdated()) 
	        return true;
	    if (pPrevConcept != null)
	        return pPrevConcept.IsAvailable();
	    else
	        return false;
	}

	// D: returns true if the concept is available and grounded. By default, this 
//	    is true if the concept is available, and is not ambiguous, and it's 
//	    confidence is above the grounding threshold
	public boolean IsAvailableAndGrounded() {
		if(IsUpdatedAndGrounded())
			return true;
		if(pPrevConcept != null)
			return pPrevConcept.IsAvailableAndGrounded();
		else
			return false;
	}

	// D: returns true if the concept is grounded
	public boolean IsGrounded() {
		// if this is a history concept, it's grounded
		if(bHistoryConcept) return true;

	    // if there is no grounding model 
	    if(pGroundingModel == null) {
	        // if there is an owner concept
	        if(pOwnerConcept != null) 
	            // defer to it
	            return pOwnerConcept.IsGrounded();
	        else 
	            // o/w return true
			    return true;
	    } else
	        // if there is a grounding model
			return bGrounded;
	}

	// A: returns true if the concept has a partial hypothesis 
//	    (for concept that can be updated through partial events, 
//	     e.g. partial recognition hypothesis of user utterances)
	public boolean HasPartialUpdate() {
		return vhPartialHypSet.size() > 0;
	}

	// D: returns true if the concept is undergoing grounding (i.e. one of the 
//	    actions of its grounding model is on the stack)
	public boolean IsUndergoingGrounding() {
	    // if it has a grounding model
	    if(pGroundingModel!=null) 
	        return DMCore.pGroundingManager.GroundingInProgressOnConcept(this);
	    else
	        return false;
	}

	// D: returns true if the concept has been invalidated
	public boolean IsInvalidated() {
	    return GetInvalidatedFlag();
	}

	// D: returns true if the concept has been restored for grounding
	public boolean IsRestoredForGrounding() {
	    return GetRestoredForGroundingFlag();
	}

	//-----------------------------------------------------------------------------
	// Methods implementing conversions to String format
	//-----------------------------------------------------------------------------

	// D: Generate a String representation of the grounded hypothesis (the top
	// one, if the concept is grounded)
	public String GroundedHypToString() {
		if(IsUpdatedAndGrounded()) 
			return TopHypToString();
		else 
			return IsInvalidated()?Const.INVALIDATED_CONCEPT:Const.UNDEFINED_CONCEPT;
	}

	// A: conversion to "mode_value" format
	public String TopHypToString() {
		if(IsUpdated()) {
			// gets the top hypothesis and returns a String representation
			return GetTopHyp().ValueToString();
		} else {
			return IsInvalidated()?Const.INVALIDATED_CONCEPT:Const.UNDEFINED_CONCEPT;
		}
	}

	// D: conversion to value/conf;value/conf... format
	public String HypSetToString() {
		// go through the hypset, and convert each one to String
		/*String sResult;
		if(IsUpdated()) {
			for(int i = 0; i < (int)vhCurrentHypSet.size(); i++) 
				sResult += 
	                (vhCurrentHypSet.get(i)?vhCurrentHypSet.get(i).ToString():UNDEFINED_VALUE) + 
					";";
			return sResult.substr(0, sResult.length()-1) + "\n";
		} else {
			return IsInvalidated()?Const.INVALIDATED_CONCEPT"\n":Const.UNDEFINED_CONCEPT"\n";
		}

		if (HasPartialHyp()) {
			sResult += " (PARTIAL:";
			for(int i = 0; i < (int)vhPartialHypSet.size(); i++) 
				sResult += 
	                (vhPartialHypSet[i]?vhPartialHypSet[i].ToString():UNDEFINED_VALUE) + 
					";";
			sResult = sResult.substr(0, sResult.length()-1) + ")";
		}

		return sResult + "\n";*/
		return " ";
	}
	

	//-----------------------------------------------------------------------------
	// Methods providing access to concept type and source
	//-----------------------------------------------------------------------------

	// D: return the concept type
	public TConceptType GetConceptType() {
		return ctConceptType;
	}

	// D: set the concept type
	public void SetConceptType(TConceptType ctAConceptType) {
		ctConceptType = ctAConceptType;
	}

	// D: returns the concept source
	public TConceptSource GetConceptSource() {
		return csConceptSource;
	}

	// D: set the concept source
	public void SetConceptSource(TConceptSource csAConceptSource) {
		csConceptSource = csAConceptSource;
	}

	//-----------------------------------------------------------------------------
	// Methods providing access to concept name
	//-----------------------------------------------------------------------------

	// D: set the concept name
	public void SetName(String sAName) {
	    if(bHistoryConcept) 
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot SetName on concept ("+sName+") history.");
		sName = sAName;
	}

	// D: return the concept name
	public String GetName() {
		return sName;
	}

	// D: return the small concept name
	public String GetSmallName() {
		String sFoo="", sSmallName="";
		SplitReturnType srt = Utils.SplitOnLast(sName, ".");
		sFoo = srt.FirstPart;
		sSmallName=srt.SecondPart;
		return sSmallName;
	}

	// D: return the name of the concept, qualified with the name of the owner
//	    agent (i.e. AgentName/ConceptName)
	public String GetAgentQualifiedName() {
	    if(pOwnerDialogAgent != null) {
	        return pOwnerDialogAgent.GetName()+"/"+GetName();
	    } else {
	        return GetName();
	    }
	}

	//-----------------------------------------------------------------------------
	// Methods providing access to owner dialog agent
	//-----------------------------------------------------------------------------

	// D: Set the owner dialog agent
	public void SetOwnerDialogAgent(CDialogAgent pADialogAgent) {
	    pOwnerDialogAgent = pADialogAgent;
	}

	// D: Access to the owner dialog agent
	public CDialogAgent GetOwnerDialogAgent() {
	    return pOwnerDialogAgent;
	}

	//-----------------------------------------------------------------------------
	// Methods providing access to owner concept
	//-----------------------------------------------------------------------------

	// D: Set the owner concept
	public void SetOwnerConcept(CConcept pAConcept) {
	    pOwnerConcept = pAConcept;
		if(pPrevConcept != null) 
			pPrevConcept.SetOwnerConcept(pAConcept);
	}

	// D: Access to the owner concept
	public CConcept GetOwnerConcept() {
	    return pOwnerConcept;
	}

	//-----------------------------------------------------------------------------
	// Methods related to the grounding model
	//-----------------------------------------------------------------------------

	// D: Create a grounding model
	public void CreateGroundingModel(String sGroundingModelSpec) {

		String sThisSpec = "";
		String sDefaultGroundingModelSpec = "";

		// partition the spec by commas
		ArrayList<String> vsGMSpecs = Utils.PartitionString(sGroundingModelSpec, ",");
		// and trim it
		for(int i = 0; i < vsGMSpecs.size(); i++) {
			vsGMSpecs.set(i,Utils.TrimLeft(vsGMSpecs.get(i)," "));
			// check if it's for the current item
			if((sThisSpec == "") && (vsGMSpecs.get(i).indexOf('=') == -1)) {
				sThisSpec = vsGMSpecs.get(i);
			}
			else {
				String sHead="", sTail="";
				SplitReturnType srt = Utils.SplitOnFirst(vsGMSpecs.get(i), "=");
				sHead=srt.FirstPart;
				sTail=srt.SecondPart;
				// A: found a default grounding model spec
				if (sHead == "@default") {
					sDefaultGroundingModelSpec = sTail;
				}
				else {
				    Log.e(Const.CONCEPT_STREAM_TAG,
						"Could not create a grounding model for concept "
						+GetAgentQualifiedName()+". Ill formed grounding model specification: "
								+vsGMSpecs.get(i)+".");
				}
			}
		}
		
		// A: no specification given, fall back on the default one
		if (sThisSpec == "") {
			sThisSpec = sDefaultGroundingModelSpec;
		}

		if(!(DMCore.pGroundingManager.GetConfiguration().bGroundConcepts) ||
			(sThisSpec == "none") || (sThisSpec == "")) {
	        pGroundingModel = null;
	    } else {
	        pGroundingModel = (CGMConcept )
	            DMCore.pGroundingManager.CreateGroundingModel(
				    DMCore.pGroundingManager.GetConfiguration().sConceptGM,
				    sThisSpec);
	        pGroundingModel.Initialize();
	        pGroundingModel.SetConcept(this);
	    }
	}

	// D: Return a pointer to the grounding model
	public CGMConcept GetGroundingModel() {
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Trying to access grounding model on history "+
	            "concept "+sName+"." );

		return pGroundingModel;
	}

	// D: Sets the grounded flag on the concept
	public void SetGroundedFlag(boolean bAGrounded) {
		bGrounded = bAGrounded;
		// now if the concept was set to grounded and it was restored for grounding
		if(bGrounded && bRestoredForGrounding) {
	        // check that we indeed have a previous concept 
	        if(pPrevConcept==null)
	            Log.e(Const.CONCEPT_STREAM_TAG,"Concept "+GetName()+" was restored for grounding, "+
	                "now it's grounded, but has no history.");
	        // check if the top hypothesis of the previous concept is the same
	        // as the top hypothesis of he current one
	        CHyp pHTopHyp = pPrevConcept.GetTopHyp();
	        CHyp pTopHyp = GetTopHyp();
	        if(pHTopHyp!=null && pTopHyp!=null && (pHTopHyp == pTopHyp)) {
	            // if they are equal, then just copy the current hypset in history
	            pPrevConcept.CopyCurrentHypSetFrom(this);
	            // and delete the current hypset, but do not notify the change
	            if(bChangeNotification) {
	                DisableChangeNotification();
	                ClearCurrentHypSet();
	                EnableChangeNotification();
	            } else {
	                ClearCurrentHypSet();
	            }
	        } 
	        // set the restored for grounding flag to false
	        SetRestoredForGroundingFlag(false);
		}
	}

	// D: Reads the grounded flag on the concept
	public boolean GetGroundedFlag() {
		return bGrounded;
	}

	// D: Declare the grounding models subsumed by this concept
	public void DeclareGroundingModels(
	    ArrayList<CGroundingModel> rgmpvModels,
	    HashSet<CGroundingModel> rgmpsExclude) {

	    // check that this is not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Trying to access grounding models on history "
	            +"concept "+sName+".");

	    // now add the grounding model associated with this concept
	    if(pGroundingModel!=null && 
	        !rgmpsExclude.contains(pGroundingModel)) {
	        rgmpvModels.add(pGroundingModel);
	        rgmpsExclude.add(pGroundingModel);
	    }
	}

	// D: Declare the subsumed concepts
	public void DeclareConcepts(ArrayList<CConcept> rcpvConcepts, 
								   HashSet<CConcept> rcpsExclude) {

	    // now add this concept if it's not already in the list
		if(!rcpsExclude.contains(this)) {
	        rcpvConcepts.add(this);
	        rcpsExclude.add(this);
	    }
	}

	//-----------------------------------------------------------------------------
	// Methods related to the invalidated flag
	//-----------------------------------------------------------------------------

	// D: set the invalidated flag
	public void SetInvalidatedFlag(boolean bAInvalidated) {
	    // set the flag
	    bInvalidated = bAInvalidated;
	    // if the concept has been restored for grounding, and how has just been
	    // invalidated, then invalidate the history value
	    if(IsRestoredForGrounding() && pPrevConcept!=null) {
	        // set the invalidated flag on the history also
	        pPrevConcept.SetInvalidatedFlag(bAInvalidated);
	        // then if the concept is set to invalidated, clear the restored for
	        // grounding flag
	        if(bAInvalidated) 
	            SetRestoredForGroundingFlag(false);
	    }   
	    // and break the seal
	    BreakSeal();
	}

	// D: return the invalidated flag
	public boolean GetInvalidatedFlag() {
	    return bInvalidated;
	}

	//-----------------------------------------------------------------------------
	// Methods related to the restored for grounding flag
	//-----------------------------------------------------------------------------

	// D: set the restored for grounding flag
	public void SetRestoredForGroundingFlag(boolean bARestoredForGrounding) {
	    // set the flag
	    if(bARestoredForGrounding) {
	        bRestoredForGrounding = true;
	    } else {
	        bRestoredForGrounding = false;
	    }
	}

	// D: return the restored for grounding flag
	public boolean GetRestoredForGroundingFlag() {
	    return bRestoredForGrounding;
	}

	//-----------------------------------------------------------------------------
	// Methods related to sealing 
	//-----------------------------------------------------------------------------

	// D: Seal the concept
	public void Seal() {
		bSealed = true;
	}

	// D: Set the value of the seal flag to false
	public void BreakSeal() {
		bSealed = false;
	}

	// D: Check if the concept is sealed
	public boolean IsSealed() {
		return bSealed;
	}

	//-----------------------------------------------------------------------------
	// Methods related to signaling concept changes
	//-----------------------------------------------------------------------------

	// D: Disable concept change notifications
	public void DisableChangeNotification() {
		SetChangeNotification(false);
	}

	// D: Enable concept change notifications
	public void EnableChangeNotification() {
		SetChangeNotification(true);
	}

	// D: Set the concept change notifications flag
	public void SetChangeNotification(
		boolean bAChangeNotification) {
		bChangeNotification = bAChangeNotification;
	}

	// D: Processing that happens each time the concept changes
	public void NotifyChange() {
	    // set the grounded flag to false
	    SetGroundedFlag(false);
	    // set the invalidated flag to false
	    SetInvalidatedFlag(false);
	    // break the seal (if one existed)
	    BreakSeal();
	    // set the turn last updated information
	    MarkTurnLastUpdated();
		// set to not conveyed
		cConveyance = TConveyance.cNotConveyed;
	    // clear the conveyance waiting
	    ClearWaitingConveyance();
	    // and signal the grounding if appropriate
	    if(pGroundingModel!=null && bChangeNotification) 
	        DMCore.pGroundingManager.RequestConceptGrounding(this);
	}

	//-----------------------------------------------------------------------------
	// Methods related to the current hypotheses set and belief manipulation
	//-----------------------------------------------------------------------------

	// D: factory method for hypotheses
	public CHyp HypFactory() {
		Log.e(Const.CONCEPT_STREAM_TAG,
			"HypFactory call on "+sName+" ("+ctConceptType.toString()+" type) concept failed.");
		return null;
	}

	// D: adds a hypothesis to the current set of hypotheses
	public int AddHyp(CHyp pAHyp) {
	    vhCurrentHypSet.add(pAHyp);
	    iNumValidHyps++;
		// notify the concept change
	    NotifyChange();
		return (int)(vhCurrentHypSet.size() - 1);
	}

	// D: adds a new hypothesis to the current set of hypotheses
	public int AddNewHyp() {
	    vhCurrentHypSet.add(HypFactory());
	    iNumValidHyps++;
		// notify the concept change
	    NotifyChange();
		return (int)(vhCurrentHypSet.size() - 1);
	}

	// D: adds a null hypothesis to the current set of hypotheses
	public int AddNullHyp() {
	    vhCurrentHypSet.add(null);
		// notify the concept change
	    NotifyChange();
	    return (int)(vhCurrentHypSet.size() - 1);
	}

	// D: sets a hypothesis into a location
	public void SetHyp(int iIndex, CHyp pHyp) {
	    // first set it to null
	    SetNullHyp(iIndex);
	    // check if pHyp is null, then return
	    if(pHyp==null) return;
	    // create a new hypothesis
	    vhCurrentHypSet.add(iIndex,pHyp);
	    vhCurrentHypSet.remove(iIndex+1);
	    // copy the contents
	    iNumValidHyps++;
	    // notify the change
	    NotifyChange();
	}

	// D: sets a null hypothesis into a location
	public void SetNullHyp(int iIndex) {
	    // if it's already null, return
	    if(vhCurrentHypSet.get(iIndex) == null) return;
	    // o/w delete it
	    // and set it to null
	    vhCurrentHypSet.remove(iIndex);
	    iNumValidHyps--;
	    // notify the change
	    NotifyChange();
	}

	// D: deletes a hypothesis at a given location
	public void DeleteHyp(int iIndex) {
		if(vhCurrentHypSet.get(iIndex) != null) {
			// if it's not null, destroy it
			iNumValidHyps--;
		}
		// then delete it from the array
		vhCurrentHypSet.remove(iIndex);
	    // notify the change
	    NotifyChange();
	}

	// D: return the hypothesis at a given index
	public CHyp GetHyp(int iIndex) {
	    if((iIndex < 0) || (iIndex >= (int)vhCurrentHypSet.size())) {
	        Log.e(Const.CONCEPT_STREAM_TAG,
	            "Index out of bounds in GetHyp call on "+sName+" ("+ctConceptType.toString()+
	            " type) concept");
	    }
	    return vhCurrentHypSet.get(iIndex);
	}

	// D: return the index of a given hypothesis
	public int GetHypIndex(CHyp pHyp) {
	    for(int i = 0; i < (int)vhCurrentHypSet.size(); i++) 
	        if((vhCurrentHypSet.get(i) != null) && 
	            (pHyp == (vhCurrentHypSet.get(i))))
	            return i;
	    // o/w if we reached the end return -1
	    return -1;
	}

	// D: return the confidence of a certain hypothesis (specified by index)
	public float GetHypConfidence(int iIndex) {
	    CHyp pHyp = GetHyp(iIndex);
	    return pHyp != null?pHyp.GetConfidence():0;
	}

	// D: set the confidence for a certain hypothesis (specified by the index)
	public void SetHypConfidence(int iIndex, float fConfidence) {
		CHyp pHyp = GetHyp(iIndex);
		if(pHyp != null) {
			if(pHyp.GetConfidence() != fConfidence) {
				pHyp.SetConfidence(fConfidence);
				// notify the concept change
				NotifyChange();
			}
		} else {
			Log.e(Const.CONCEPT_STREAM_TAG,
				"Hyp ("+iIndex+") not found in SetHypConfidence on concept "+
						GetAgentQualifiedName()+".");
		}
	}

	// D: return the top hypothesis
	public CHyp GetTopHyp() {
	    int iTopHypIndex = GetTopHypIndex();
	    if(iTopHypIndex == -1)
	        return null;
	    else
	        return GetHyp(iTopHypIndex);
	}

	// D: return the top hyp index
	public int GetTopHypIndex() {
		// if no valid hyps, return -1
	    if(iNumValidHyps == 0) return -1;
	    // o/w compute the item with the max confidence
	    float fMaxConfidence = 0;
	    int iSelected = -1;
	    for(int h = 0; h < (int)vhCurrentHypSet.size(); h++) {
			if(vhCurrentHypSet.get(h) != null) {
				if(vhCurrentHypSet.get(h).GetConfidence() > fMaxConfidence) {
					fMaxConfidence = vhCurrentHypSet.get(h).GetConfidence();
					iSelected = h;
				}
			}
	    }
	    // return the computed one
	    return iSelected;
	}

	// D: return the second best hyp index
	public int Get2ndHypIndex() {
		// if we don't have at least 2 valid hyps, return -1
	    if(iNumValidHyps < 2) return -1;
	    
		// o/w get the top hypothesis index
		int iTopHypIndex = GetTopHypIndex();
		
		float fMaxConfidence = 0;
	    int iSelected = -1;
	    for(int h = 0; h < (int)vhCurrentHypSet.size(); h++) {
			if((h != iTopHypIndex) && vhCurrentHypSet.get(h) != null) {
				if(vhCurrentHypSet.get(h).GetConfidence() > fMaxConfidence) {
					fMaxConfidence = vhCurrentHypSet.get(h).GetConfidence();
					iSelected = h;
				}
			}
	    }
	    // return the computed one
	    return iSelected;
	}

	// D: return the confidence score of the top hypothesis
	public float GetTopHypConfidence() {
	    CHyp pTopHyp = GetTopHyp();
	    if(pTopHyp == null)
	        return 0;
	    else 
	        return pTopHyp.GetConfidence();
	}

	// D: check if a hypothesis is valid (confidence score > 0; valus is not 
	// null)
	public boolean IsValidHyp(int iIndex) {
	    return GetHyp(iIndex) != null;
	}

	// D: return the total number of hypotheses for a concept (including 
	// NULLs)
	public int GetNumHyps() {
	    return (int)vhCurrentHypSet.size();
	}

	// D: return the number of valid hypotheses for a concept
	public int GetNumValidHyps() {
	    return iNumValidHyps;
	}

	// D: clear the current set of hypotheses for the concept
	public void ClearCurrentHypSet() {
	    // if it's already clear, return
		if(vhCurrentHypSet.size() == 0) return;
	    vhCurrentHypSet.clear();
		// finally, reset the number of valid hypotheses
		iNumValidHyps = 0;
	    // and notify the change
	    NotifyChange();
	}

	// D: copies the current set of hypotheses from another concept
	public void CopyCurrentHypSetFrom(CConcept rAConcept) {
	    // first clear it
	    ClearCurrentHypSet();
	    // then go through all the hypotheses from the source concept
	    for(int h = 0; h < rAConcept.GetNumHyps(); h++) {
	        CHyp pHyp = new CHyp();
	        if((pHyp = rAConcept.GetHyp(h)) != null) {
				// this will notify the change
	            AddNewHyp();
	            vhCurrentHypSet.add(h, pHyp);
	        } else {
				// this will notify the change
	            AddNullHyp();
	        }
	    }
	    iNumValidHyps = rAConcept.iNumValidHyps;
	    // copy the explicitly confirmed and disconfirmed hyps
	    sExplicitlyConfirmedHyp = rAConcept.GetExplicitlyConfirmedHypAsString();
	    sExplicitlyDisconfirmedHyp = rAConcept.GetExplicitlyDisconfirmedHypAsString();
	}

	// D: sets the cardinality of the hypset
	public void SetCardinality(int iACardinality) {
	    iCardinality = iACardinality;
	}

	// D: returns the cardinality of the hypset
	public int GetCardinality() {
	    return iCardinality;
	}

	// D: returns the prior for a hypothesis
	public float GetPriorForHyp(CHyp pHyp) {
		// by default, first look at the information from the grounding manager
		// about priors on a concept
		/*float fPrior = DMCore.pGroundingManager.GetPriorForConceptHyp(
			GetSmallName(), pHyp != null?pHyp.ValueToString():"<UNDEFINED>");
		// if the grounding manager has information about it
		if(fPrior != -1) {
			// then return it
			return fPrior;
		} 
		// otherwise return a uniform prior
		return (float) 1.0 / (iCardinality + 1);*/
		return (float) 0.5;
	}

	// D: returns the confusability for a hypothesis
	public float GetConfusabilityForHyp(CHyp pHyp) {
		// by default, first look at the information from the grounding manager
		// about confusability on a concept
		/*float fConfusability = 
			DMCore.pGroundingManager.GetConfusabilityForConceptHyp(
				GetSmallName(), pHyp != null?pHyp.ValueToString():"<UNDEFINED>");
		// if the grounding manager has information about it
		if(fConfusability != -1) {
			// then return it
			return fConfusability;
		} */
		// otherwise return a neutral 0.5
		return (float) 0.5;
	}

	// D: returns the concept type information for a concept
	
	
	
	
	/*public String GetConceptTypeInfo() {
		// by default, call on the grounding manager
		return DMCore.pGroundingManager.GetConceptTypeInfoForConcept(GetSmallName());
	}*/
	
	
	
	
	
	
	
	
	

	// D: set the explicitly confirmed hyp
	public void SetExplicitlyConfirmedHyp(CHyp pHyp) {
	    SetExplicitlyConfirmedHyp(pHyp.ValueToString());
	}

	// D: alternate function for settting the explicitly confirmed hyp
	public void SetExplicitlyConfirmedHyp(String sAExplicitlyConfirmedHyp) {
	    sExplicitlyConfirmedHyp = sAExplicitlyConfirmedHyp;
	}

	// D: set the explicitly disconfirmed hyp
	public void SetExplicitlyDisconfirmedHyp(CHyp pHyp) {
	    SetExplicitlyDisconfirmedHyp(pHyp.ValueToString());
	}

	// D: alternate function for settting the explicitly disconfirmed hyp
	public void SetExplicitlyDisconfirmedHyp(String sAExplicitlyDisconfirmedHyp) {
	    sExplicitlyDisconfirmedHyp = sAExplicitlyDisconfirmedHyp;
	}

	// D: return the explicitly confirmed hyp
	public String GetExplicitlyConfirmedHypAsString() {
	    return sExplicitlyConfirmedHyp;
	}

	// D: return the explicitly confirmed hyp
	public String GetExplicitlyDisconfirmedHypAsString() {
	    return sExplicitlyDisconfirmedHyp;
	}

	// D: clears the explicitly confirmed hyp
	public void ClearExplicitlyConfirmedHyp() {
	    sExplicitlyConfirmedHyp = "";
	}

	// D: clears the explicitly confirmed hyp
	public void ClearExplicitlyDisconfirmedHyp() {
	    sExplicitlyConfirmedHyp = "";
	}

	//---------------------------------------------------------------------
	// Methods providing access to partially updated values
	//---------------------------------------------------------------------

	// A: adds a partial hypothesis to the current set of partial hypotheses
	public int AddPartialHyp(CHyp pAHyp) {
	    vhPartialHypSet.add(pAHyp);
	    iNumValidPartialHyps++;
		return (int)(vhPartialHypSet.size() - 1);
	}

	// A: adds a new partial hypothesis to the current set of partial hypotheses
	public int AddNewPartialHyp() {
	    vhPartialHypSet.add(HypFactory());
	    iNumValidPartialHyps++;
		return (int)(vhPartialHypSet.size() - 1);
	}

	// A: adds a null hypothesis to the current set of partial hypotheses
	public int AddNullPartialHyp() {
	    vhPartialHypSet.add(null);
	    return (int)(vhPartialHypSet.size() - 1);
	}

	// A: indicates whether a partial hypothesis is currently available 
	public boolean HasPartialHyp() {
		return iNumValidPartialHyps > 0;
	}

	// A: returns one partial hypothesis
	public CHyp GetPartialHyp(int iIndex) {
	    if((iIndex < 0) || (iIndex >= (int)vhPartialHypSet.size())) {
	        Log.e(Const.CONCEPT_STREAM_TAG,
	            "Index out of bounds in GetPartialHyp call on "+sName+" ("
	        +ctConceptType.toString()+" type) concept");
	    }
	    return vhPartialHypSet.get(iIndex);
	}

	// return the top partial hypothesis
	public CHyp GetTopPartialHyp() {
	    int iTopPartialHypIndex = GetTopPartialHypIndex();
	    if(iTopPartialHypIndex == -1)
	        return null;
	    else
	        return GetPartialHyp(iTopPartialHypIndex);
	}

	// A: return the top partial hyp index
	public int GetTopPartialHypIndex() {
		// if no valid hyps, return -1
	    if(iNumValidPartialHyps == 0) return -1;
	    // o/w compute the item with the max confidence
	    float fMaxConfidence = 0;
	    int iSelected = -1;
	    for(int h = 0; h < (int)vhPartialHypSet.size(); h++) {
			if(vhPartialHypSet.get(h) != null) {
				if(vhPartialHypSet.get(h).GetConfidence() > fMaxConfidence) {
					fMaxConfidence = vhPartialHypSet.get(h).GetConfidence();
					iSelected = h;
				}
			}
	    }
	    // return the computed one
	    return iSelected;
	}

	// A: return the confidence score of the top partial hypothesis
	public float GetTopPartialHypConfidence() {
	    CHyp pTopPartialHyp = GetTopPartialHyp();
	    if(pTopPartialHyp == null)
	        return 0;
	    else 
	        return pTopPartialHyp.GetConfidence();
	}

	// A: check if a partial hypothesis is valid (confidence score > 0; 
	// valus is not null)
	public boolean IsValidPartialHyp(int iIndex) {
	    return GetPartialHyp(iIndex) != null;
	}

	// A: return the total number of partial hypotheses for a concept 
	// (including NULLs)
	public int GetNumPartialHyps() {
	    return (int)vhPartialHypSet.size();
	}

	// A: return the number of valid partial hypotheses for a concept
	public int GetNumValidPartialHyps() {
	    return iNumValidPartialHyps;
	}
	// A: clears the current partial value of the concept
	public void ClearPartialHypSet() {
		// reset the partial hyp set
		// go through all the valconfs and deallocate them
		
		vhPartialHypSet.clear();
		// finally, reset the number of valid hypotheses
		iNumValidPartialHyps = 0;
	}


	//-----------------------------------------------------------------------------
	// Methods providing access to turn last updated information
	//-----------------------------------------------------------------------------

	// D: Set the turn the concept was last updated
	public void SetTurnLastUpdated(int iTurn) {
	    iTurnLastUpdated = iTurn;
	}

	// D: Mark now as the turn in which the concept was last updated
	public void MarkTurnLastUpdated() {
		SetTurnLastUpdated(DMCore.pDMCore.GetLastInputTurnNumber());
	}

	// D: Access to turn in which the concept was last updated information
	public int GetTurnLastUpdated() {
	    return iTurnLastUpdated;
	}

	// D: returns the number of turns that have elapsed since the concept was 
//	    last updated
	public int GetTurnsSinceLastUpdated() {
	    return DMCore.pDMCore.GetLastInputTurnNumber() - GetTurnLastUpdated();
	}

	//-----------------------------------------------------------------------------
	// Methods providing access to conveyance information
	//-----------------------------------------------------------------------------

	// D: sets the waiting for conveyance flag
	public void SetWaitingConveyance() {
	    bWaitingConveyance = true;
	}

	// D: clear the waiting for conveyance flag
	public void ClearWaitingConveyance() {
		/*if(bWaitingConveyance) {
			bWaitingConveyance = false;
			if(DMCore.pOutputManager != null) 
				DMCore.pOutputManager.CancelConceptNotificationRequest(this);
		}*/
	}

	// A: set the conveyance information
	public void SetConveyance(TConveyance cAConveyance) {
		cConveyance = cAConveyance;
	}

	// A: return the conveyance information
	public TConveyance GetConveyance() {
		return cConveyance;
	}

	// D: clear the concept notification pointer
	public void ClearConceptNotificationPointer() {
	    /*if(DMCore.pOutputManager != null)
	        DMCore.pOutputManager.ChangeConceptNotificationPointer(this, null);*/
	}

	//-----------------------------------------------------------------------------
	// Methods for concept history manipulation
	//-----------------------------------------------------------------------------

	// D: reopens the concept (i.e. moves current value into history, and starts
	//	    with a clean new value
	public void ReOpen() {

		// first check that it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform ReOpen on concept ("
	        		+sName+") history.");

	    // record the initial value of the concept (if the concept has a grounding
	    //  model)
	    String sInitialValue="";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

		// create a clone of the current concept (without the history)
	    CConcept pConcept = Clone(false);

	    // collapse it to the mode (this also sets it to grounded)
	    pConcept.Update(Const.CU_COLLAPSE_TO_MODE, null);

		// set the flag on it that it's a history concept
	    pConcept.SetHistoryConcept(true);

	    // links it to the current history
		pConcept.pPrevConcept = pPrevConcept;

	    // and push it into the history
	    pPrevConcept = pConcept;

	    // finally, clear the current value (which notifies the change)
	    ClearCurrentHypSet(); 

	    // now log the update (if the concept has a grounding model
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG,"Concept update [reopen] on "+GetAgentQualifiedName()+
	            ":\nInitial value dumped below:\n"+sInitialValue+
	            "\nUpdated value dumped below:\n"+Utils.TrimRight(HypSetToString()," ")); 
	}

	// D: restores the concept (i.e. restores the concept to a previous incarnation
//	    from its history
	public void Restore(int iIndex) {

		// first check if it's not a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform Restore on concept ("+sName+") history.");

	    // record the initial value of the concept (if the concept has a grounding
	    //  model)
	    String sInitialValue="";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

	    // check that the index is not zero 
	    if(iIndex == 0) return;

	    // get the history version that we need to restore to
	    CConcept pConcept = getIndexing(iIndex);

	    // restore values from that version (this will notify the change)
	    CopyCurrentHypSetFrom(pConcept);

		// set it to grounded (since it comes from history)
		SetGroundedFlag(true);

	    // and clear the history
	    ClearHistory();

	    // now log the update (if the concept has a grounding model)
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG,"Concept update [restore] on "+
	    GetAgentQualifiedName()+":\nInitial value dumped below:\n"+
	    sInitialValue+"\nUpdated value dumped below:\n"+Utils.TrimRight(HypSetToString()," ")); 
	}

	// D: clears the history of the current concept
	public void ClearHistory() {
	    // check if it's a history concept
	    if(bHistoryConcept)
	        Log.e(Const.CONCEPT_STREAM_TAG,"Cannot perform ClearHistory on concept ("+sName+") history.");

	    // o/w merely delete all its history
	    if(pPrevConcept != null) {
	        pPrevConcept = null;
	    }
	}

	// D: merges the history on the concept, and returns a new concept containing 
//	    that 
	public CConcept CreateMergedHistoryConcept() {
		
		// check if the concept is updated or invalidated
		if(IsUpdated() || IsInvalidated()) {
		
	        // o/w if the concept is updated, create clone of the current value
	        // (w/o the history)
	        CConcept pMergedHistory = Clone(false);
		    // set it to not a history concept
	        pMergedHistory.SetHistoryConcept(false);
		    // return it
	        return pMergedHistory;
	        
		} else {
		
	        // o/w check for a previous concept
	        if(pPrevConcept == null) {
	            // and if there's no history then just return null
	            return null;
	        } else {
	            // o/w defer to previous concept in history
	            return pPrevConcept.CreateMergedHistoryConcept();
	        }        
	    } 
	}

	// D: merges the history of the concept into the current value
	public void MergeHistory() {

	    // record the initial value of the concept (if the concept has a grounding
	    //  model)
	    String sInitialValue = "";
	    if(pGroundingModel!=null) 
	        sInitialValue = Utils.TrimRight(HypSetToString()," ");

	    // if the concept is updated or is invalidated, then we just clear it's history
	    if(IsUpdated() || IsInvalidated()) {
	        // then simply clear it's history
	        ClearHistory();
	        // and set the invalidated flag to false
	        SetInvalidatedFlag(false);
	        // and set the restored for grounding flag to false
	        SetRestoredForGroundingFlag(false);    
	    } else {

	        // o/w first create a merged history concept
	        CConcept pMergedHistoryConcept = CreateMergedHistoryConcept();

	        if(pMergedHistoryConcept != null) {
	            // then copy this one from it (this will notify the change)
	            CopyCurrentHypSetFrom(pMergedHistoryConcept);
			    // also set the grounding state according to the merged history
			    // concept
			    SetGroundedFlag(pMergedHistoryConcept.GetGroundedFlag());
	            // and finally, delete the auxiliary merged history concept
	        }

		    // clear the history of this concept
	        ClearHistory();        
	    }

	    // now log the update (if the concept has a grounding model)
	    if(pGroundingModel!=null) 
	        Log.d(Const.CONCEPT_STREAM_TAG, "Concept update [merge_history] on "
	        		+GetAgentQualifiedName()+":\nInitial value dumped below:\n"
	        		+sInitialValue+"\nUpdated value dumped below:\n"
	        		+Utils.TrimRight(HypSetToString()," "));
	}

	// D: returns the size of the history on the concept
	public int GetHistorySize() {
	    // recursively compute history size
	    if(pPrevConcept == null)
	        return 0;
	    else 
	        return pPrevConcept.GetHistorySize() + 1;
	}

	// D: returns a certain historical version of a concept
	public CConcept GetHistoryVersion(int iIndex) {
	    // just redirect through the operator
	    return getIndexing(iIndex);
	}

	// D: set the history concept flag
	public void SetHistoryConcept(boolean bAHistoryConcept) {
		bHistoryConcept = bAHistoryConcept;
	}

	// D: get the history concept flag
	public boolean IsHistoryConcept() {
		return bHistoryConcept;
	}

	//-----------------------------------------------------------------------------
	// Virtual methods that are array-specific
	//-----------------------------------------------------------------------------

	// D: getsize method: 
	public int GetSize() {
		Log.e(Const.CONCEPT_STREAM_TAG,"GetSize cannot be called on concept "+sName+" (" 
				+ctConceptType.toString()+" type).");
		return -1;
	}

	// D: DeleteAt method
	public void DeleteAt(int iIndex) {
		Log.e(Const.CONCEPT_STREAM_TAG,"DeleteAt cannot be called on concept "+sName+" ("+
				ctConceptType.toString()+" type).");
	}

	// J: InsertAt method
	public void InsertAt(int iIndex, CConcept rAConcept) {
		Log.e(Const.CONCEPT_STREAM_TAG,"InsertAt cannot be called on concept "+sName
				+" ("+ctConceptType.toString()+" type).");
	}
	// L: Return the string prompted to user. Override by derived classes
	public String PromptToString() {
		// TODO Auto-generated method stub
		return "";
	}
	
	// L: Return the string prompted to database. Override by derived classes
	public String QueryToString(){
		return "";
	}
	
}
