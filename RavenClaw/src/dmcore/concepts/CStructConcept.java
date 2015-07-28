package dmcore.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import dmcore.agents.coreagents.DMCore;
import dmcore.agents.dialogagents.CDialogAgent;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.grounding.groundingmodel.CGMConcept;
import dmcore.grounding.groundingmodel.CGroundingModel;

import android.util.Log;


class CStructHyp extends CHyp {

    //---------------------------------------------------------------------
    // Protected member variables 
	//---------------------------------------------------------------------
	
    

	// pointer to the ItemMap for the structure
	protected HashMap<String,CConcept> pItemMap=new HashMap<String, CConcept>();

	// pointer to the svItems for the structure
	protected ArrayList<String> psvItems = new ArrayList<String>();

    // an index indicating which hypothesis is captured
	protected int	iHypIndex;
	
	//-----------------------------------------------------------------------------
	// CStructHyp: Constructors and destructors
	//-----------------------------------------------------------------------------

	// D: default constructor
	public CStructHyp(HashMap<String,CConcept> pAItemMap, ArrayList<String> psvAItems, 
						   int iAHypIndex, boolean bComplete) {
		ctHypType = TConceptType.ctStruct;
		pItemMap = pAItemMap;
		psvItems = psvAItems;
		iHypIndex = iAHypIndex;
		
	    // now get the confidence from the confidence of the items in the map
	    fConfidence = (float) 1.0;
	    for(int i = 0; i < psvItems.size(); i++) {
			CHyp pItemHyp;
			if (bComplete) {
				pItemHyp = pItemMap.get(psvItems.get(i)).GetHyp(iAHypIndex);
			} else {
				pItemHyp = pItemMap.get(psvItems.get(i)).GetPartialHyp(iAHypIndex);
			}
	        if(pItemHyp != null) {
	            if(fConfidence == 0.0)
	                fConfidence = pItemHyp.GetConfidence();
	            else if(fConfidence != pItemHyp.GetConfidence()) {
	                Log.e(Const.CSTRUCT_TAG, "Inconsistent confidence scores on structure items ");
	                   /* "%s (%.2f) and %s (%.2f) on index %d.",
	                    pItemMap.begin().first,
	                    fConfidence, 
						psvItems.get(i),
	                    pItemHyp.GetConfidence(),
	                    iAHypIndex));*/
	            }
	        }
	    }
	}

	// D: copy constructor
	public CStructHyp(CStructHyp rAStructHyp) {
		ctHypType = TConceptType.ctStruct;
		pItemMap = rAStructHyp.pItemMap;
		psvItems = rAStructHyp.psvItems;
		iHypIndex = rAStructHyp.iHypIndex;
		fConfidence = rAStructHyp.fConfidence;
	}

	
	//-----------------------------------------------------------------------------
	// CStructHyp specific methods
	//-----------------------------------------------------------------------------

	// D: set the HypIndex
	public void SetHypIndex(int iAHypIndex) {
	    iHypIndex = iAHypIndex;
	}

	//-----------------------------------------------------------------------------
	// CStructHyp: Overwritten, CStructHyp specific methods
	//-----------------------------------------------------------------------------

	// D: assignment operator from another hyp
	public CHyp Assignment (CHyp rAHyp) {
	    // check against self-assignment
	    if(rAHyp != this) {
		    // check the type
		    if(rAHyp.GetHypType() != TConceptType.ctStruct) {
			    // if it's not an atomic structure, signal an error
			    Log.e(Const.CSTRUCT_TAG,"Assignment operator from a different hyp type called on "+
			  	    "structure hyp. Cannot perform conversion.");
			    return this;
		    }

			// convert it to an atomic structure valconf
		    CStructHyp rAStructHyp = (CStructHyp)rAHyp;
		   
		    // now iterate through the map
			for(int i = 0; i < psvItems.size(); i++) {
				// check that the other one has it
	           
	            if(!rAStructHyp.pItemMap.containsKey(psvItems.get(i))) {
	    		    // if it's not an atomic structure, signal an error
		    	    Log.e(Const.CSTRUCT_TAG,"Assignment operator from a different hyp type called on "+
			      	    "structure hyp. Cannot perform conversion.");
			        return this;                
	            }
				// then copy that hypothesis into the right location
				pItemMap.get(psvItems.get(i)).SetHyp(iHypIndex, 
	                rAStructHyp.pItemMap.get(psvItems.get(i)).GetHyp(rAStructHyp.iHypIndex));
			}

			// finally, set the confidence
		    fConfidence = rAStructHyp.fConfidence;
	    }
		return this;
	}

	// D: set the confidence score
	public void SetConfidence(float fAConfidence) {
	    // call the inherited method first
	    super.SetConfidence(fAConfidence);
	    // then iterate through the map and set the confidence for each item
		for(int i = 0; i < psvItems.size(); i++) {
			// get this item hyp
			CHyp pItemHyp = 
				pItemMap.get(psvItems.get(i)).GetHyp(iHypIndex);
	        if(pItemHyp != null) {
	            pItemHyp.SetConfidence(fAConfidence);
	        }
	    }
	}

	@Override
	// D: equality operator
	public boolean equals(Object rAHyp) {
	    
		// check the type
		if(((CHyp) rAHyp).GetHypType() != TConceptType.ctStruct) {
			// if it's not an atomic struct, signal an error
			Log.e(Const.CSTRUCT_TAG,"Equality operator with a different hyp type called on "+
			  	  "atomic struct hyp. Cannot perform conversion.");
			return false;
		}

		// convert it to a structure hyp
		CStructHyp rAStructHyp = (CStructHyp)rAHyp;

		// now iterate through the map
		for(int i = 0; i < psvItems.size(); i++) {
			// get this item hyp
	        CHyp pItemHyp = 
				pItemMap.get(psvItems.get(i)).GetHyp(iHypIndex);
	        // check that the other one has it
	        if(!rAStructHyp.pItemMap.containsKey(psvItems.get(i))) {
	    		// if it's not an atomic structure, signal an error
		    	Log.e(Const.CSTRUCT_TAG,"Comparison operator from a different hyp type called on "+
			      	"structure hyp. Cannot perform conversion.");
			    return false;                
	        }
	        // then check it
	        if(pItemHyp == null) {
	            if(rAStructHyp.pItemMap.get(psvItems.get(i)).GetHyp(rAStructHyp.iHypIndex) != null) 
	                return false;
	        } else {
	            if(rAStructHyp.pItemMap.get(psvItems.get(i)).GetHyp(rAStructHyp.iHypIndex) == null) 
	                return false;
	            if(!(pItemHyp == (rAStructHyp.pItemMap.get(psvItems.get(i)).GetHyp(rAStructHyp.iHypIndex))))
	                return false;
	        }
		}

		// if we got here, it's all equal, so return true
		return true;
	}

	// D: comparison operator
	public boolean lessthan(CHyp rAHyp) {   
	    // signal an error, cannot compare bools
		Log.e(Const.CSTRUCT_TAG,"Comparison operator < called on CStructHyp.");
		return false;
	}

	// D: comparison operator
	public boolean greaterthan(CHyp rAHyp) {   
	    // signal an error, cannot compare bools
		Log.e(Const.CSTRUCT_TAG,"Comparison operator > called on CStructHyp.");
		return false;
	}

	// D: comparison operator
	public boolean lessequal(CHyp rAHyp) {   
	    // signal an error, cannot compare bools
		Log.e(Const.CSTRUCT_TAG,"Comparison operator <= called on CStructHyp.");
		return false;
	}

	// D: comparison operator
	public boolean greaterequal(CHyp rAHyp) {   
	    // signal an error, cannot compare bools
		Log.e(Const.CSTRUCT_TAG,"Comparison operator >= called on CStructHyp.");
		return false;
	}

	// D: indexing operator
	public CHyp getConceptIndexing(String sItem) {
		return pItemMap.get(sItem).GetHyp(iHypIndex);
	}

	// D: Convert value to String
	public String ValueToString() {
		// assemble the result
		String sResult = "{\n";

		// now iterate through the map
		for(int i = 0; i < psvItems.size(); i++) {
			CHyp pItemHyp = 
				pItemMap.get(psvItems.get(i)).GetHyp(iHypIndex);
			// and add the String representation
	        if(pItemHyp != null)
			    sResult += psvItems.get(i)+"+t"+pItemHyp.ValueToString()+"+n";
		}

		// close it
		sResult += "}\n";

		// finally, return it
		return sResult;
	}

	// D: Convert valconf to String
	public String ToString() {
		// assemble the result
		String sResult = "{\n";

		// now iterate through the map
		for(int i = 0; i < psvItems.size(); i++) {
			CHyp pItemHyp = 
				pItemMap.get(psvItems.get(i)).GetHyp(iHypIndex);
			// and add the String representation
	        if(pItemHyp != null)
	    		sResult += psvItems.get(i)+"+t"+pItemHyp.ToString()+"+n";
		}

		// close it
		sResult += "}\n";

		// finally, return it
		return sResult;
	}

	// D: Get the hyp from a String
	public void FromString(String sString) {
		Log.e(Const.CSTRUCT_TAG,"FromString called on CStructHyp. Call failed.");
	}
}
public class CStructConcept extends CConcept{
	// Log tag
	public static final int DEFAULT_STRUCT_CARDINALITY = 1000;
	//---------------------------------------------------------------------
	// protected members 
    //---------------------------------------------------------------------

	// the list of items (pointers to concepts) held by the structure as 
    // a hash
	protected HashMap<String,CConcept> ItemMap = new HashMap<String, CConcept>();

	// String vector holding the list of structure elements (this is used
	//  for accessing the order of the elements)
	protected ArrayList<String> svItems = new ArrayList<String>();
	
	//-----------------------------------------------------------------------------
	// CStructConcept: Constructors and destructors
	//-----------------------------------------------------------------------------
	// D: constructor
	public CStructConcept(){
		
	}
	public CStructConcept(String sAName, TConceptSource csAConceptSource){
		super(sAName, csAConceptSource, DEFAULT_STRUCT_CARDINALITY);
		// call create structure to initialize the ItemMap accordingly
		ctConceptType = TConceptType.ctStruct;
		CreateStructure();
	}
		
	//-----------------------------------------------------------------------------
	// CStructConcept class concept manipulation
	//-----------------------------------------------------------------------------

	public CStructConcept(String sAName) {
		// TODO Auto-generated constructor stub
		super(sAName);
		ctConceptType = TConceptType.ctStruct;
		CreateStructure();
	}

	// D: create the items of a structure - does nothing, should be overwritten
	public void CreateStructure() {
	}

	// D: destroy the structure
	public void DestroyStructure() {
		// go through each item, and delete it
		// finally, clear the map
		ItemMap.clear();
		// and the items
		svItems.clear();
	}

	//-----------------------------------------------------------------------------
	// CStructConcept: Access to various class members
	//-----------------------------------------------------------------------------

	// D: Comparison operator
	public boolean lessthan(CConcept rAConcept) {
	    Log.e(Const.CSTRUCT_TAG,"Comparison operator < called on structure concept.");
	    return false;
	}

	// D: Comparison operator
	public boolean greaterthan(CConcept rAConcept) {
	    Log.e(Const.CSTRUCT_TAG,"Comparison operator > called on structure concept.");
	    return false;
	}

	// D: Comparison operator
	public boolean lessequal(CConcept rAConcept) {
	    Log.e(Const.CSTRUCT_TAG,"Comparison operator <= called on structure concept.");
	    return false;
	}

	// D: Comparison operator
	public boolean greaterequle(CConcept rAConcept) {
	    Log.e(Const.CSTRUCT_TAG,"Comparison operator >= called on structure concept.");
	    return false;
	}

	// D: operator for accessing structure members
	public CConcept getIndexing(String sAItemName) {

		String sItemName="";	// the item in this structure we are trying to access
		String sFollowUp="";	// something else after that item, in case we have 
							// nested structures i.e. hotel.chain.name

		// if empty itemname, then return this concept
		if(sAItemName.length()==0) 
			return this;

		// separate the item name into the first part and the follow-up part
		SplitReturnType srt =Utils.SplitOnFirst(sAItemName, ".");
		sItemName = srt.FirstPart;
		sFollowUp = srt.SecondPart;
		int iFirstIndex = 0;
		try{
			// convert it to an int
		    iFirstIndex = Integer.parseInt(sItemName);
		}catch(Exception e){
			Log.e(Const.CSTRUCT_TAG,"Can not parse string to int");
		}		
	    // check if index is negative and there is an active history, then 
	    // return the structured concept in history
	    if((iFirstIndex < 0) && (pPrevConcept != null)) {
	        // if adressing previous in history, return that
	        if(iFirstIndex == -1) {
	            if(sFollowUp.length()==0)
	                return pPrevConcept;
	            else
	                return pPrevConcept.getIndexing(sFollowUp);
	        } else {
	            // o/w go recursively
	        	int tmpint = iFirstIndex+1;
	            return pPrevConcept.getIndexing(tmpint+"."+sFollowUp);
	        }
	    }
		
	     else {
	        // o/w deal with it as a member access in the current value of the 
	        // structure

	        // identify the item corresponding to sItemName
		    if(!ItemMap.containsKey(sItemName)) {
			    Log.e(Const.CSTRUCT_TAG, "Accessing invalid item in structured concept: "+sName+"."+sAItemName);
			    return null;	
		    }
		    CConcept pConcept = ItemMap.get(sItemName);
	    	
		    // return the appropriate concept
		    if(sFollowUp.length()==0) 
			    return pConcept;
		    else 
			    return pConcept.getIndexing(sFollowUp);
	    }
	}

	// D: Clones the struct concept
	public CConcept Clone(boolean bCloneHistory) {

		// start with an empty clone
		CStructConcept pConcept = (CStructConcept)EmptyClone();

		// now destroy the structure and clone all the items
		pConcept.DestroyStructure();
		for(int i = 0; i < svItems.size(); i++) {
			// clone a subitem
			CConcept pConceptToInsert = ItemMap.get(svItems.get(i)).Clone(true);
			// reassign the owner concept
			pConceptToInsert.SetOwnerConcept(pConcept);
			// insert it
	        pConcept.ItemMap.put(svItems.get(i), pConceptToInsert);
			// and add stuff to svItems
			pConcept.svItems.add(svItems.get(i));
		}

		// now set the other members
	    pConcept.SetConceptType(ctConceptType);
	    pConcept.SetConceptSource(csConceptSource);
	    pConcept.sName = sName;
	    pConcept.pOwnerDialogAgent = pOwnerDialogAgent;
		pConcept.SetOwnerConcept(pOwnerConcept);
		// a clone has no grounding model
		pConcept.pGroundingModel = null;
		pConcept.bSealed = bSealed;
		// a clone does not notify changes
		pConcept.DisableChangeNotification();
		// reconstruct the current hyp set
	    for(int i = 0; i < (int)vhCurrentHypSet.size(); i++)
			if(vhCurrentHypSet.get(i) == null)
				pConcept.vhCurrentHypSet.add(null);
			else
				pConcept.vhCurrentHypSet.add(
					new CStructHyp(pConcept.ItemMap, pConcept.svItems, i,true));
		pConcept.iNumValidHyps = iNumValidHyps;
		pConcept.SetGroundedFlag(bGrounded);
		pConcept.iCardinality = iCardinality;
	    pConcept.SetTurnLastUpdated(iTurnLastUpdated);
	    pConcept.cConveyance = cConveyance;
		// a clone does not wait for conveyance
	    pConcept.bWaitingConveyance = false;
	    pConcept.SetHistoryConcept(bHistoryConcept);
	    if(bCloneHistory && (pPrevConcept != null)) 
			pConcept.pPrevConcept = pPrevConcept.Clone(true);
		else
	        pConcept.pPrevConcept = null;

		// finally, return the clone
	    return pConcept;
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods implementing various types of updates in the 
	// naive probabilistic update scheme
	//-----------------------------------------------------------------------------

	// D: naive probabilistic update - assign from String method
	public void Update_NPU_AssignFromString(Object pUpdateData) {
	    // first, check that it's not a history concept
	    if(bHistoryConcept) 
	        Log.e(Const.CSTRUCT_TAG,"Cannot perform (AssingFromString) update on concept "+
	            "("+sName+") history.");

		// o/w 
	    String sString = (String)pUpdateData;
		String sWorkingString = Utils.TrimLeft(sString," ");

	    // clear the current hyp set
	    ClearCurrentHypSet();

		// the first character has to be "{"
		if(sWorkingString.charAt(0) != '{') {
			// fail the whole process
			Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to "+
	            "<CStructConcept> "+sName+" from String (dump below).\n"+sString);
		}

		// advance over the "{"
		sWorkingString = Utils.TrimLeft(sWorkingString.substring(1, 
	        sWorkingString.length()-1)," ");

		// find the corresponding closing bracket
		int uiPos = Utils.FindClosingQuoteChar(sWorkingString, 0, '{', '}');

		// skip tabs/spaces/newlines between the concept
		// and the confidence separator
		while((uiPos < sWorkingString.length() - 1)&&
				((sWorkingString.charAt(uiPos) == '\n')||
				(sWorkingString.charAt(uiPos) == '\t')||
				(sWorkingString.charAt(uiPos) == ' '))) {
			uiPos++;	
		}

		// check if there's anything left after iCount (that should be the 
	    // confidence score)
		String sConfidence = "";
			
	    if(uiPos < sWorkingString.length() - 1) {

			sConfidence = sWorkingString.substring(uiPos + 1, 
				sWorkingString.length() - uiPos - 1);
			float fConfidence = (float) 0.0;
			try{
				fConfidence = Float.parseFloat(sConfidence);
			}catch(Exception e){
				// fail the whole process if we can't find a valid number for confidence
				Log.e(Const.CSTRUCT_TAG,"Cannot perform update to <CStructConcept> "+sName
	                	+" from String (dump below).\n"+sString);
			}
		}

		// now go through all the lines and convert the items 
		String sLine="";

		// while keeping track of the ones that were updated and how many hypotheses they
		// had
		HashSet<String> ssUpdated = new HashSet<String>();
		int iUpdatedNumHyps = -1;
		SplitReturnType srt =Utils.ExtractFirstLine(sWorkingString);
		while(((sLine = srt.FirstPart.trim()) != "}") && 
			(sLine != "") && (sLine.charAt(0) != '}')) {
			sWorkingString = srt.SecondPart;
			srt = Utils.ExtractFirstLine(sWorkingString);
			// split at the equals sign
			SplitReturnType srt2 = Utils.SplitOnFirst(sLine, "= \t");
			String sItem = srt2.FirstPart;
			String sValue= srt2.SecondPart;
			if(srt2.IsSplitSuccessful) {
				// if we successfully made the split
				// check if sValue starts with {. 
				if((sValue.charAt(0) == '{') || (sValue.charAt(0) == ':')) {
					// in this case, we are dealing with a nested structure 
					// (or an array), so identify where it ends, and correct 
	                // the sValue;

					// basically go forward counting the matching {}s and
					// terminate when the matching one is found
					sWorkingString = Utils.TrimLeft(sValue + sWorkingString," ");
					int iCount = Utils.FindClosingQuoteChar(sWorkingString, 
						sWorkingString.indexOf('{') + 1, '{', '}');

					// if we ran out of the String, signal an error
					if(iCount >= sWorkingString.length()) {
						Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> "+
	                        		sName+" from String (dump below).\n"+sWorkingString);
					}

					// set the value to the enclosed String
					sValue = sWorkingString.substring(0, iCount);
					// and the working String to whatever was left
					sWorkingString = Utils.TrimLeft(sWorkingString.substring(iCount + 1, 
						sWorkingString.length() - iCount - 1)," ");
				}

				// look for that item in the structure
				if(!ItemMap.containsKey(sItem)) {
					Log.e(Const.CSTRUCT_TAG,
	                    "Item "+sItem+" not found in structured concept "+sName+
						" while converting from String. ");
				} else {
					// and if found, set its value accordingly
	                if(sConfidence != "") 
						sValue += "|" + sConfidence;
					ItemMap.get(sItem).Update(Const.CU_ASSIGN_FROM_STRING, sValue);
					// and mark it as updated
					ssUpdated.add(sItem);
					// set the number of hypotheses
	                if(iUpdatedNumHyps == -1) 
						iUpdatedNumHyps = ItemMap.get(sItem).GetNumHyps();
	                else if(ItemMap.get(sItem).GetNumHyps() != iUpdatedNumHyps) {
	                    Log.e(Const.CSTRUCT_TAG,"Variable number of hypotheses in item "+sItem+" while"+
	                        " converting concept "+sName+" from String.");
	                }
				}
			} else {
				// if no equals sign (split unsuccessful), fail the whole process
				Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> "+sName+
					" from String (dump below).\n"+sString);
			} 
		}

		// at this point, we should get out of the loop 
		if(sLine.charAt(0) != '}') {
			// fail the whole process
			Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> from "+sString);
		}

		// finally, go through all the items again and update all the ones that were 
		//  not updated
		for(int i = 0; i < svItems.size(); i++) {
			if(ssUpdated.contains(svItems.get(i))) {
				// then update this concept with empty hypotheses
				for(int h = 0; h < iUpdatedNumHyps; h++) {
					// this will notify the update
					ItemMap.get(svItems.get(i)).AddNullHyp();
				}
			}
		}

		// finally, update the valconf set
	    // first clear it (but without deleting the hypotheses from the 
	    //  concepts
	    super.ClearCurrentHypSet();
	    for(int h = 0; h < iUpdatedNumHyps; h++) {
			// this will notify the change
	        AddHyp(new CStructHyp(ItemMap, svItems, h,true));
	    }			
	    iNumValidHyps = iUpdatedNumHyps;
	}

	// A: update partial hypotheses from a String
	public void Update_PartialFromString(Object pUpdateData) {
		updateFromString(pUpdateData, Const.CU_PARTIAL_FROM_STRING);
	}

	// updates the concept from a String representation
	public void updateFromString(Object pUpdateData, String sUpdateType) {
	    // first, check that it's not a history concept
	    if(bHistoryConcept) 
	        Log.e(Const.CSTRUCT_TAG,"Cannot perform (AssingFromString) update on concept "+
	            "("+sName+") history.");


		// o/w 
	    String sString = (String)pUpdateData;
		String sWorkingString = Utils.TrimLeft(sString," ");

		Log.d(Const.CSTRUCT_TAG, "updateFromString: "+sString);

	    // clear the current hyp set
	    ClearCurrentHypSet();

		// the first character has to be "{"
		if(sWorkingString.charAt(0) != '{') {
			// fail the whole process
			Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to "+
	            "<CStructConcept> "+sName+" from String (dump below).\n"+sString); 
		}

		// advance over the "{"
		sWorkingString = Utils.TrimLeft(sWorkingString.substring(1, 
	        sWorkingString.length()-1)," ");

		// find the corresponding closing bracket
		int uiPos = Utils.FindClosingQuoteChar(sWorkingString, 0, '{', '}');

		// skip tabs/spaces/newlines between the concept
		// and the confidence separator
		while((uiPos < sWorkingString.length() - 1)&&
				((sWorkingString.charAt(uiPos) == '\n')||
				(sWorkingString.charAt(uiPos) == '\t')||
				(sWorkingString.charAt(uiPos) == ' '))) {
			uiPos++;	
		}

		// check if there's anything left after iCount (that should be the 
	    // confidence score)
		String sConfidence = " ";
			
	    if(uiPos < sWorkingString.length() - 1) {

			sConfidence = sWorkingString.substring(uiPos + 1, 
				sWorkingString.length() - uiPos - 1);

			float fConfidence = (float)0;
			try{
				fConfidence = Float.parseFloat(sConfidence);
			}catch(Exception e){
				// fail the whole process if we can't find a valid number for confidence
				Log.e(Const.CSTRUCT_TAG,"Cannot perform update to <CStructConcept> "+sName+
					" from String (dump below).\n"+sString);
					}
			
		}

		// now go through all the lines and convert the items 
		String sLine="";

		// while keeping track of the ones that were updated and how many hypotheses they
		// had
		HashSet<String> ssUpdated = new HashSet<String>();
		int iUpdatedNumHyps = -1;
		SplitReturnType srt =Utils.ExtractFirstLine(sWorkingString);
		while(((sLine = srt.FirstPart.trim()) != "}") && 
			(sLine != "") && (sLine.charAt(0) != '}')) {
			sWorkingString = srt.SecondPart;
			srt = Utils.ExtractFirstLine(sWorkingString);
			// split at the equals sign
			SplitReturnType srt2 = Utils.SplitOnFirst(sLine, "= \t");
			String sItem = srt2.FirstPart;
			String sValue= srt2.SecondPart;
			if (srt.IsSplitSuccessful){
				// if we successfully made the split
				// check if sValue starts with {. 
				if((sValue.charAt(0) == '{') || (sValue.charAt(0) == ':')) {
					// in this case, we are dealing with a nested structure 
					// (or an array), so identify where it ends, and correct 
	                // the sValue;

					// basically go forward counting the matching {}s and
					// terminate when the matching one is found
					sWorkingString = Utils.TrimLeft(sValue + sWorkingString," ");
					int iCount = Utils.FindClosingQuoteChar(sWorkingString, 
						sWorkingString.indexOf('{') + 1, '{', '}');

					// if we ran out of the String, signal an error
					if(iCount >= sWorkingString.length()) {
						Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> "+sName+
	                        " from String (dump below).\n"+sWorkingString);
					}

					// set the value to the enclosed String
					sValue = sWorkingString.substring(0, iCount);
					// and the working String to whatever was left
					sWorkingString = Utils.TrimLeft(sWorkingString.substring(iCount + 1, 
						sWorkingString.length() - iCount - 1)," ");
				}

				// look for that item in the structure
				if(!ItemMap.containsKey(sItem)) {
					Log.e(Const.CSTRUCT_TAG,"Item "+sItem+" not found in structured concept "+sName+
						" while converting from String. ");
				} else {
					// and if found, set its value accordingly
	                if(sConfidence != "") 
						sValue += "|" + sConfidence;
					ItemMap.get(sItem).Update(sUpdateType, sValue);
					// and mark it as updated
					ssUpdated.add(sItem);
					if (sUpdateType == Const.CU_PARTIAL_FROM_STRING) {
						// set the number of hypotheses
						if(iUpdatedNumHyps == -1) 
							iUpdatedNumHyps = ItemMap.get(sItem).GetNumPartialHyps();
						else if(ItemMap.get(sItem).GetNumPartialHyps() != iUpdatedNumHyps) {
							Log.e(Const.CSTRUCT_TAG,"Variable number of partial hypotheses in item "+sItem+" while"+
								" converting concept "+sName+" from String."); 
						}
					} else {
						// set the number of hypotheses
						if(iUpdatedNumHyps == -1) 
							iUpdatedNumHyps = ItemMap.get(sItem).GetNumHyps();
						else if(ItemMap.get(sItem).GetNumHyps() != iUpdatedNumHyps) {
							Log.e(Const.CSTRUCT_TAG,"Variable number of hypotheses in item "+sItem+" while"+
								" converting concept "+sName+" from String."); 
						}
					}
				}
			} else {
				// if no equals sign (split unsuccessful), fail the whole process
				Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> "+sName+
					" from String (dump below).\n"+sString);
			} 
		}

		// at this point, we should get out of the loop 
		if(sLine.charAt(0) != '}') {
			// fail the whole process
			Log.e(Const.CSTRUCT_TAG,"Cannot perform conversion to <CStructConcept> from "+sString); 
		}

		// finally, go through all the items again and update all the ones that were 
		//  not updated
		for(int i = 0; i < svItems.size(); i++) {
			if(!ssUpdated.contains(svItems.get(i))) {
				// then update this concept with empty hypotheses
				for(int h = 0; h < iUpdatedNumHyps; h++) {
					if (sUpdateType == Const.CU_PARTIAL_FROM_STRING) {
						ItemMap.get(svItems.get(i)).AddNullPartialHyp();
					} else {
						// this will notify the update
						ItemMap.get(svItems.get(i)).AddNullHyp();
					}
				}
			}
		}

		// finally, update the valconf set
	    // first clear it (but without deleting the hypotheses from the 
	    //  concepts
		if (sUpdateType == Const.CU_PARTIAL_FROM_STRING) {
			super.ClearPartialHypSet();
			for(int h = 0; h < iUpdatedNumHyps; h++) {
				// this will notify the change
				AddPartialHyp(new CStructHyp(ItemMap, svItems, h, false));
			}
		    iNumValidPartialHyps = iUpdatedNumHyps;
		} else {
			super.ClearCurrentHypSet();
			for(int h = 0; h < iUpdatedNumHyps; h++) {
				// this will notify the change
				AddHyp(new CStructHyp(ItemMap, svItems, h,true));
			}
		    iNumValidHyps = iUpdatedNumHyps;
	    }			
	}



	//-----------------------------------------------------------------------------
	// Overwritten methods implementing String conversion
	//-----------------------------------------------------------------------------

	// D: conversion to value/conf;value/conf... format
	public String HypSetToString() {
		// go through the hypset, and convert each one to String
		String sResult="";
		if(IsUpdated()) {
		    sResult = "{\n";
		    // go through the items and add them to the String
			for(int i = 0; i < svItems.size(); i++) {		
			    if(ItemMap.get(svItems.get(i)).IsUpdated()||
				   ItemMap.get(svItems.get(i)).HasPartialHyp()) {
				    sResult += svItems.get(i)+"\t"+ItemMap.get(svItems.get(i)).HypSetToString();	                   
			    }
	        }
		    // and finally add the closing braces and return 
		    sResult += "}\n";
		    return sResult;
		} else {
			return IsInvalidated()?Const.INVALIDATED_CONCEPT:Const.UNDEFINED_CONCEPT;
		}
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods providing access to concept name
	//-----------------------------------------------------------------------------

	// D: set the concept name
	public void SetName(String sAName) {
	    // call the inherited
	    super.SetName(sAName);
	    // sets the name recursively on each of the items
		for(int i = 0; i < svItems.size(); i++)
			ItemMap.get(svItems.get(i)).SetName(sName + "." + svItems.get(i));
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods providing access to the owner dialog agent
	//-----------------------------------------------------------------------------
	    
	// set the owner dialog agent
	public void SetOwnerDialogAgent(CDialogAgent pADialogAgent) {
		// call the inherited
		super.SetOwnerDialogAgent(pADialogAgent);
		// then set the owner an all subitems
		for(int i = 0; i < svItems.size(); i++) 
			ItemMap.get(svItems.get(i)).SetOwnerDialogAgent(pADialogAgent);
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods related to the grounding model
	//-----------------------------------------------------------------------------

	// D: create a grounding model for this concept
	public void CreateGroundingModel(String sGroundingModelSpec) {

		String sDefaultGroundingModelSpec = "";
		String sThisSpec = "";

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
				SplitReturnType srt = Utils.SplitOnFirst(vsGMSpecs.get(i), "=");
				String sHead = srt.FirstPart;
				String sTail = srt.SecondPart;
				// A: found a default grounding model spec
				if (sHead == "@default") {
					sDefaultGroundingModelSpec = sTail;
				}
				else {
				    Log.e(Const.CSTRUCT_TAG,"Could not create a grounding model for struct "+GetAgentQualifiedName()+
						" . Ill formed grounding model specification: "+vsGMSpecs.get(i)+".");
				}
			}
		}
		
		// A: no specification given, fall back on the default one
		if (sThisSpec == "") {
			sThisSpec = sDefaultGroundingModelSpec;
		}

		// create the grounding model for this struct
	    if(!(DMCore.pGroundingManager.GetConfiguration().bGroundConcepts) ||
			(sThisSpec == "none") || (sThisSpec == "")) {
	        pGroundingModel = null;
	    } else {
	        pGroundingModel = new CGMConcept(sThisSpec);
	        pGroundingModel.Initialize();
	        pGroundingModel.SetConcept(this);
	    }

		// now go through each of the elements of the structure
		for(int in = 0; in < svItems.size(); in++) {
			// get the item name
			String sItemName = svItems.get(in);
			// now check it against each of the specs
			// and construct the spec for that item
			SplitReturnType srt = null;
			String sItemGroundingModelSpec = "";
			for(int i = 0; i < vsGMSpecs.size(); i++) {
				srt = Utils.SplitOnFirst(vsGMSpecs.get(i), " =.");
				String sHead = srt.FirstPart;
				String sRest = srt.SecondPart;
				if(sHead == sItemName) {
					sRest = Utils.TrimLeft(sRest," ");
					if(sRest.length() > 1) { 
						sItemGroundingModelSpec += sRest + ", ";
					}
				}
			}
			// now create the grounding model on the item
			ItemMap.get(svItems.get(in)).CreateGroundingModel(Utils.TrimRight(sItemGroundingModelSpec, ", "));
		}
	}

	// D: Declare the grounding models subsumed by this concept
	public void DeclareGroundingModels(
	    ArrayList<CGroundingModel> rgmpvModels,
	    HashSet<CGroundingModel> rgmpsExclude) {

	    // first call the inherited method
	    super.DeclareGroundingModels(rgmpvModels, rgmpsExclude);

	    // then add the grounding models for the subsumed concepts
		for(int i = 0; i < svItems.size(); i++)
			ItemMap.get(svItems.get(i)).DeclareGroundingModels(rgmpvModels, rgmpsExclude);
	}

	// D: Declare the subsumed concepts
	public void DeclareConcepts(
	    ArrayList<CConcept> rcpvConcepts, 
		HashSet<CConcept> rcpsExclude) {

	    // just add the current concept
		super.DeclareConcepts(rcpvConcepts, rcpsExclude);

	    // then go through all the items and have them declare the concepts, too
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).DeclareConcepts(rcpvConcepts, rcpsExclude);
	}

	//-----------------------------------------------------------------------------
	// Methods related to signaling concept changes
	//-----------------------------------------------------------------------------

	// D: Set the concept change notifications flag
	public void SetChangeNotification(
		boolean bAChangeNotification) {
		bChangeNotification = bAChangeNotification;
		// and set it for items
		for(int i = 0; i < svItems.size(); i++)
			ItemMap.get(svItems.get(i)).SetChangeNotification(bAChangeNotification);
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods related to the current hypotheses set and belief 
	// manipulation
	//-----------------------------------------------------------------------------

	// D: factory method for hypotheses
	public CHyp HypFactory() {
		Log.e(Const.CSTRUCT_TAG,"HypFactory call on structured concept "+sName+" concept failed. "+
	        "Structured hypotheses should not be generated through the HypFactory "+
	        "method.");
		return null;
	}

	// D: adds a new hypothesis to the current set of hypotheses
	public int AddNewHyp() {
	    // add a hypothesis in all the members
		for(int i = 0; i < svItems.size(); i++) 
			ItemMap.get(svItems.get(i)).AddNewHyp();
	    // then add the corresponding entry in the hypset (this notifies the 
		// change)
	    return AddHyp(new CStructHyp(ItemMap, svItems, vhCurrentHypSet.size(),true));
	}

	// D: adds a null hypothesis to the current set of hypotheses
	public int AddNullHyp() {
	    // add a null hypothesis in all the members
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).AddNullHyp();
	    // then create the corresponding entry (this will notify the change)
		return super.AddNullHyp();
	}

	// D: sets a hypothesis into a location
	public void SetHyp(int iIndex, CHyp pHyp) {
	    // first set it to null (this potentially notifies the change)
	    SetNullHyp(iIndex);
	    // check if pHyp is null, then we're done return
	    if(pHyp==null) return;
	    // create a new hypothesis at that location
	    // copy the contents (which will automatically copy into members)
	    vhCurrentHypSet.add(iIndex,pHyp);
	    vhCurrentHypSet.remove(iIndex+1);
	    
	    iNumValidHyps++;
	    // notify the change
	    NotifyChange();
	}

	// D: deletes a hypothesis 
	public void DeleteHyp(int iIndex) {

	    // delete the hypothesis in all the members
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).DeleteHyp(iIndex);

		if(vhCurrentHypSet.get(iIndex) != null) {
			// if it's not null, destroy it
			
			iNumValidHyps--;
		}

		// then delete it from the array
		vhCurrentHypSet.remove(iIndex);

		// and reset the iHypIndex for all the hypotheses following
	    for(int i = iIndex; i < (int)vhCurrentHypSet.size(); i++) 
	        if(vhCurrentHypSet.get(i) != null)
	            ((CStructHyp)vhCurrentHypSet.get(i)).SetHypIndex(i);

	    // notify the change
	    NotifyChange();
	}

	// D: sets a null hypothesis into a location
	public void SetNullHyp(int iIndex) {
	    // if it's already null, return
	    if(vhCurrentHypSet.get(iIndex) == null) return;
	    // and set it to null
	    vhCurrentHypSet.add(iIndex,null);
	    vhCurrentHypSet.remove(iIndex+1);
	    // and call the same on all member items
	    // add a null hypothesis in all the members
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).SetNullHyp(iIndex);
	    // finally decrease the number of valid hypotheses and return
	    iNumValidHyps--;
	    // notify the change
	    NotifyChange();
	}

	// D: clear the current set of hypotheses for the concept
	public void ClearCurrentHypSet() {
	    // call the inherited method (this will notify the change)
	    super.ClearCurrentHypSet();
	    // clear the current hypsets for all the subsumed concepts
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).ClearCurrentHypSet();
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods for partial hypotheses manipulation
	//-----------------------------------------------------------------------------

	// A: adds a new partial hypothesis to the current set of partial hypotheses
	public int AddNewPartialHyp() {
	    // add a hypothesis in all the members
		for(int i = 0; i < svItems.size(); i++) 
			ItemMap.get(svItems.get(i)).AddNewPartialHyp();

		Log.d(Const.CONCEPT_STREAM_TAG, "index of new hyp: "+vhPartialHypSet.size());

	    // then add the corresponding entry in the hypset
	    return AddPartialHyp(new CStructHyp(ItemMap, svItems, vhPartialHypSet.size(), false));
	}

	// A: adds a null hypothesis to the current set of partial hypotheses
	public int AddNullPartialHyp() {
	    // add a null partial hypothesis in all the members
	    for(int i = 0; i < svItems.size(); i++)
	        ItemMap.get(svItems.get(i)).AddNullPartialHyp();
	    // then create the corresponding entry
		return super.AddNullPartialHyp();
	}

	//-----------------------------------------------------------------------------
	// Overwritten methods for concept history manipulation
	//-----------------------------------------------------------------------------

	// D: set the history concept flag
	public void SetHistoryConcept(boolean bAHistoryConcept) {
		bHistoryConcept = bAHistoryConcept;
		// and set it for all the subconcepts
	    for(int i = 0; i < svItems.size(); i++)
			ItemMap.get(svItems.get(i)).SetHistoryConcept(bAHistoryConcept);
	}
	
	// D: Struct to String method to promp to user
	public String PromptToString(){
		return "";
	}
	
	//---------------------------------------------------------------------------
	// D: Macro for defining a derived structure concept class
	//---------------------------------------------------------------------------

	// D: Macro for defining items in the derived structure class
	public void CUSTOM_ITEM(String Name, ConceptFactory ConceptType){
		ItemMap.put(Name, ConceptType.CreateConcept(sName + "." + Name, csConceptSource));
        ItemMap.get(Name).SetOwnerDialogAgent(pOwnerDialogAgent);
        ItemMap.get(Name).SetOwnerConcept(this);
		svItems.add(Name);
	}
	
		
}
