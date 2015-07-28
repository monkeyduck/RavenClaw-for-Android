package dmcore.grounding.groundingmodel;

import utils.Utils;
import android.util.Log;
import dmcore.agents.coreagents.DMCore;
import dmcore.concepts.CConcept;

public class CGMConcept extends CGroundingModel{
	// Log tag
	public static final String TAG = "CGMConcept";
	public static final int SI_INACTIVE= 0;
	public static final String SS_INACTIVE ="INACTIVE";

	public static final int SI_CONFIDENT =1;
	public static final String SS_CONFIDENT= "CONFIDENT";

	public static final int SI_UNCONFIDENT =2;
	public static final String SS_UNCONFIDENT= "UNCONFIDENT";

	public static final int SI_GROUNDED= 3;
	public static final String SS_GROUNDED ="GROUNDED";
	//---------------------------------------------------------------------
	// protected class members
	//---------------------------------------------------------------------
    protected CConcept pConcept;		// pointer to the concept it is handling
    //-----------------------------------------------------------------------------
	// D: Constructors, destructors
    //-----------------------------------------------------------------------------
	
	// D: default constructor, just calls upon the CGroundingModel constructor
	public CGMConcept(String sAModelPolicy, String sAName){
	     super(sAModelPolicy, sAName);
	     pConcept = null;
	     // this model has 4 states
	     //bdBeliefState.Resize(4);
	 }
	public CGMConcept(String sAModelPolicy){
	     super(sAModelPolicy);
	     pConcept = null;
	     // this model has 4 states
	     //bdBeliefState.Resize(4);
	 }
	 // D: constructor from reference
	 public CGMConcept(CGMConcept rAGMConcept) {
	     sModelPolicy = rAGMConcept.sModelPolicy;
	     sName = rAGMConcept.sName;
	     pPolicy = rAGMConcept.pPolicy;
	     viActionMappings = rAGMConcept.viActionMappings;
	     sExplorationMode = rAGMConcept.sExplorationMode;
	     fExplorationParameter = rAGMConcept.fExplorationParameter;
	     pConcept = rAGMConcept.pConcept;
	     stFullState = rAGMConcept.stFullState;
	     bdBeliefState = rAGMConcept.bdBeliefState;
	 }
	
	 //-----------------------------------------------------------------------------
	 // D: Grounding model factory method
	 //-----------------------------------------------------------------------------
	
	 public CGroundingModel GroundingModelFactory(String sModelPolicy) {
	     return new CGMConcept(sModelPolicy);
	 }
	
	 //-----------------------------------------------------------------------------
	 // D: Member access methods
	 //-----------------------------------------------------------------------------
	
	 // D: return the type of the grounding model (as String)
	 public String GetType() {
	     return "concept_default";
	 }
	
	 // D: Set the name: block this method (the name is automatically set to the
	//     agent-qualified path of the concept it grounds)
	 public void SetName(String sAName) {
	     // issue an error
	     Log.e(TAG,"Cannot perform SetName on a concept grounding model.");
	 }
	
	 // D: Get the name of the model
	 public String GetName() {
	     if (pConcept != null) {
	         return pConcept.GetAgentQualifiedName();
	     } else {
	         return "UNKNOWN";
	     }
	 }
	
	 // D: Set the concept handled
	 public void SetConcept(CConcept pAConcept) {
	     pConcept = pAConcept;
	 }
	
	 // D: Get the concept handled
	 public CConcept GetConcept() {
	     return pConcept;
	 }
	
	 //-----------------------------------------------------------------------------
	 // D: Grounding model specific methods
	 //-----------------------------------------------------------------------------
	
	 // D: Cloning the model
	 public CGroundingModel Clone() {
	     return new CGMConcept(this);
	 }
	
	 // D: Overwritten method for loading the model policy
	 public boolean LoadPolicy() {
	     // first call the inherited LoadPolicy
	     if(!(super.LoadPolicy())) {
	         return false;
	     } else if(!bExternalPolicy) {
	         // then check that the model has the presumed state-space
	         if(pPolicy.size() != 4) {
	             Log.e(TAG,"Error in CGMConcept::LoadPolicy(). "+
	                 "Invalid state-space size for policy "+sModelPolicy+" (4 states expected, "+pPolicy.size()+
	 				" found).");
	             return false;
	         } else if((pPolicy.get(0).sStateName != SS_INACTIVE) ||
	                   (pPolicy.get(1).sStateName != SS_CONFIDENT) ||
	                   (pPolicy.get(2).sStateName != SS_UNCONFIDENT) ||
	                   (pPolicy.get(3).sStateName != SS_GROUNDED)) {
	             Log.e(TAG,"Error in CGMConcept::LoadPolicy(). Invalid "+
	                 "state-space.");
	             return false;
	         }
	     }
	     return true;
	 }
	
	 // D: Runs the action (also transmitting the concept the action is ran on
	//     as a pointer)
	 public void RunAction(int iActionIndex) {
	     // obtains a pointer to the action from the grounding manager
	     // and runs it tranmitting the concept as a parameter
	     //DMCore.pGroundingManager.getIndexing(iActionIndex).Run(pConcept);
	 }
	
	 // D: Log the state and the suggested action of the model
	 public void LogStateAction() {
	     // dumps the current concept value (hyps)
	     Log.d("GroundingModel", "Concept "+pConcept.GetAgentQualifiedName()
	    		 +" dumped below:\n"+Utils.TrimRight(pConcept.HypSetToString(), "+n"));
	     // then call the inherited method
	     super.LogStateAction();
	 }
	
	 // D: Compute the full state for this model
	 public void computeFullState() {
	     // clear the full state
	     /*stFullState.Clear();
	     // set the updated state variable
	     stFullState["updated"] = BoolToString(pConcept.IsUpdated());
	     // set the grounded state variable
	     stFullState["grounded"] = BoolToString(pConcept.IsGrounded());
	     // state the confidence state variable
	     stFullState["top_confidence"] = 
	         FloatToString(pConcept.GetTopHypConfidence());*/
	 }
	
	 // D: Compute the belief state for this model
	 public void computeBeliefState() {
	     // there are 4 states in this grounding model:
	     // INACTIVE, CONFIDENT, UNCONFIDENT, GROUNDED
	     
	     // the state is inactive if the concept was not updated (no current value
	     // since the last reopen)
	     /*bdBeliefState[SI_INACTIVE] = (stFullState["updated"] == "false");
	
	     // the state is grounded if the concept is grounded already
	     bdBeliefState[SI_GROUNDED] = !bdBeliefState[SI_INACTIVE] &&
	 		(stFullState["grounded"] == "true");
	
	 	// the state is CONFIDENT to the extent to which we are confident
	     bdBeliefState[SI_CONFIDENT] = 
	 		(bdBeliefState[SI_INACTIVE] || bdBeliefState[SI_GROUNDED])?
	         (float)0:(float)atof(stFullState["top_confidence"]);
	
	     // the state is UNCONFIDENT to the extent to which we are not confident
	     bdBeliefState[SI_UNCONFIDENT] = 
	 		(bdBeliefState[SI_INACTIVE] || bdBeliefState[SI_GROUNDED])?
	         (float)0:(1-bdBeliefState[SI_CONFIDENT]);
	
	
	     // and invalidate the suggested action (not computed yet for this state)
	     iSuggestedActionIndex = -1;*/
	 }


}