package dmcore.outputs;

import java.util.ArrayList;

import utils.Utils;

import dmcore.agents.coreagents.DMCore;
import dmcore.agents.mytypedef.TConveyance;
import dmcore.agents.mytypedef.TFloorStatus;
import dmcore.concepts.CConcept;

public class COutput {
	// class members 
	//
	protected String sGeneratorAgentName;			// name of the agent that ordered this
										// output
	protected int iOutputId;						// the output id
	protected int iExecutionIndex;				// the index of the execution item
										// corresponding to the generation
										// of this output

	protected String sDialogState;				// a String representing the dialog 
										// state at which this prompt was issued
	protected String sAct;						// the act (dialog move)
	protected String sObject;					    // the object (acted on or with)
	protected ArrayList<CConcept> vcpConcepts;		// the list of concepts referred in 
										//  this output
	protected ArrayList<Boolean> vbNotifyConcept;       // parallel ArrayList indicating whether
                                        //  the concept conveyance should be 
                                        //  notified or not
	protected ArrayList<String> vsFlags;				// flags for the output
	protected String sOutputDeviceName;			// the name of the device this output
										//  should be directed to
	protected TConveyance cConveyance;			// whether the output was fully conveyed
										// to the recipient
	protected int iRepeatCounter;					// the number of times this output has 
										//  been uttered (consecutively)
	protected TFloorStatus fsFinalFloorStatus;			// the floor status at the end of this output
	//-----------------------------------------------------------------------------
	// D: Constructors and Destructor
	//-----------------------------------------------------------------------------
	// A: Constructor - Initializes the repeat counter
	public COutput() {
		iRepeatCounter = 0;
	}
	//-----------------------------------------------------------------------------
	// AD: Public methods
	//-----------------------------------------------------------------------------

	// A: Get name of the agent that generated this output
	public String GetGeneratorAgentName() {
		return sGeneratorAgentName;
	}

	// A: Get execution index corresponding to the generation of this output
	public void SetDialogStateIndex(int iAExecutionIndex) {
		iExecutionIndex = iAExecutionIndex;
	}

	// A: Get execution index corresponding to the generation of this output
	public int GetDialogStateIndex() {
		return iExecutionIndex;
	}

	// A: set dialog state information for this output
	public void SetDialogState(String sADialogState) {
		sDialogState = sADialogState;
	}

	// A: get dialog state information for this output
	public String GetDialogState() {
		return sDialogState;
	}

	// A: set function for conveyance information
	public void SetConveyance(TConveyance cAConveyance) {
		cConveyance = cAConveyance;
	}

	// A: get function for conveyance information
	public TConveyance GetConveyance() {
		return cConveyance;
	}

	// A: Set the dialog act for this output
	public void SetAct(String sAAct) {
		sAct = sAAct;
	}

	// A: Set the dialog act for this output
	public String GetAct() {
		return sAct;
	}

	// A: Set the final floor status for this output
	public void SetFinalFloorStatus(TFloorStatus fsAFloor){
		fsFinalFloorStatus = fsAFloor;
	}

	// A: Get the final floor status for this output
	public TFloorStatus GetFinalFloorStatus() {
		return fsFinalFloorStatus;
	}

	// A: Get a String representation of the final floor status for this output
	public String GetFinalFloorStatusLabel() {
		return DMCore.pDMCore.FloorStatusToString(fsFinalFloorStatus);
	}

	// D: Checks if certain flags are set for this output
	public boolean  CheckFlag(String sFlag) {
	    // go through the list of flags, check if the one we're searching for is
	    // in there
	    for(int i=0; i < vsFlags.size(); i++)
	        if(vsFlags.get(i) == sFlag) 
	            return true;
	    return false;
	}

	// D: Notifies a concept with conveyance information (used code from 
	//	    the previous FindConceptByName routine)
	/*public void NotifyConceptConveyance(String sConceptName, 
	    TConveyance cAConveyance) {

	    // check that the concept looked for is not NULL
	    if ( sConceptName == "" ) return;
	    
	    // partition the concept name on .
		ArrayList<String> vsParts = Utils.PartitionString(sConceptName, ".");

	    // find base concept
		CConcept *pBaseConcept = NULL;
	    int j = 0;
		for ( int i = 0; i < vcpConcepts.size(); i++ ) {
			// if the concept should not be notified, continue
			if(!vbNotifyConcept.get(i)) continue;

	        String sPossibleMatch = "";
			for ( j = 0; j < vsParts.size(); j++ ) {
				sPossibleMatch += vsParts[j];
				if ( vcpConcepts.get(i).GetName() == sPossibleMatch )
					break;
				sPossibleMatch += ".";
			}
			if ( j < vsParts.size() ) {
				pBaseConcept = vcpConcepts.get(i);
				break;
			}
		}

	    // if no base concept was matching, return 
		if ( pBaseConcept == NULL ) return;

	    // then index into it
	    String sIndex = "";
	    for( int i = j + 1; i < vsParts.size(); i++ )
	        sIndex += vsParts.get(i) + ".";
	    sIndex = TrimRight(sIndex, ".");

	    // log the conveyance information
	    Log(OUTPUTMANAGER_STREAM, 
	        FormatString("Setting conveyance for concept %s . %s",
	        sConceptName.c_str(), ConveyanceAsString[cAConveyance].c_str()));


	    pBaseConcept.operator[](sIndex).SetConveyance(cAConveyance);
	}

	// D: returns a pointer to the concept refered by this output, given
//	    the concept name
	public CConcept GetConceptByName(String sConceptName) {

	    // use the registry to find the agent name
	    CDialogAgent* pGeneratorAgent = 
	        (CDialogAgent *)AgentsRegistry[sGeneratorAgentName];
	    if(!pGeneratorAgent) return NULL;

	    // partition the concept name on .
		ArrayList<String> vsParts = Utils.PartitionString(sConceptName, ".");

	    // find base concept
		CConcept *pBaseConcept = NULL;
	    int j = 0;
		for ( int i = 0; i < vcpConcepts.size(); i++ ) {
	        String sPossibleMatch = "";
			for ( j = 0; j < vsParts.size(); j++ ) {
				sPossibleMatch += vsParts[j];
				if ( vcpConcepts.get(i) && (vcpConcepts.get(i).GetName() == sPossibleMatch) )
					break;
				sPossibleMatch += ".";
			}
			if ( j < vsParts.size() ) {
				pBaseConcept = vcpConcepts.get(i);
				break;
			}
		}

	    // if no base concept was matching, return NULL
		if ( pBaseConcept == NULL ) return NULL;

	    // if a base concept was matching, then index into it
	    String sIndex = "";
	    for( int i = j + 1; i < vsParts.size(); i++ )
	        sIndex += vsParts.get(i) + ".";
	    sIndex = TrimRight(sIndex, ".");
	    
	    // and search for that concept through the generator agent
	    return &(pGeneratorAgent.C(pBaseConcept.operator[](sIndex).GetName()));
	}

	// D: Cancels the notification request for one of the concepts
	public void CancelConceptNotificationRequest(CConcept* pConcept) {
	    for (int i = 0; i < vcpConcepts.size(); i++) 
	        if(vcpConcepts.get(i) == pConcept) 
	            vbNotifyConcept.get(i) = false;
	}*/

	// D: Changes the pointers for one of the concepts (this happens on 
	// reopens and other operations which change the concept pointers)
	public void ChangeConceptNotificationPointer(CConcept pOldConcept, 
	                                               CConcept pNewConcept) {
	    for (int i = 0; i < vcpConcepts.size(); i++) 
	        if(vcpConcepts.get(i) == pOldConcept) 
	        	{
		        	vcpConcepts.add(i,pNewConcept);
		        	vcpConcepts.remove(i+1);
	            }
	    		
	}

	// Gets the number of times this prompt has been uttered
	public int GetRepeatCounter() {
		return iRepeatCounter;
	}

	// Increments the repeat counter
	public void IncrementRepeatCounter() {
		iRepeatCounter++;
	}

	//-----------------------------------------------------------------------------
	// AD: Protected, auxiliary helper methods
	//-----------------------------------------------------------------------------

	// A: Auxiliary concept function for cloning an output (is to be used by 
	//	    the overwritten Clone() methods of derived classes
	public void clone(COutput opClone, int iNewOutputId) {
		// copy all the information
		opClone.sGeneratorAgentName = sGeneratorAgentName;
		opClone.iOutputId = iNewOutputId;		
		opClone.sAct = sAct;			
		opClone.sObject = sObject;
		opClone.fsFinalFloorStatus = fsFinalFloorStatus;
	    opClone.vcpConcepts = vcpConcepts;
	    opClone.vbNotifyConcept = vbNotifyConcept;
		opClone.vsFlags = vsFlags;
		opClone.sOutputDeviceName = sOutputDeviceName;
		opClone.cConveyance = TConveyance.cNotConveyed;
		opClone.iRepeatCounter = iRepeatCounter;
	}
}
