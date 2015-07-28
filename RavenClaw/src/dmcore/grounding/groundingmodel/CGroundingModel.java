package dmcore.grounding.groundingmodel;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.SAXException;

import dmcore.agents.coreagents.DMCore;
import dmcore.agents.dialogagents.CDialogAgent;

import utils.SplitReturnType;
import utils.Utils;
import android.util.Log;

//-----------------------------------------------------------------------------
//D: Auxiliary datastructures capturing the model data
//-----------------------------------------------------------------------------

//D: First, a datastructure describing the state/action/value information
class TStateActionsValues {
	String sStateName;  // the name of the state
	HashMap<Integer, Float> i2fActionsValues = new HashMap<Integer, Float>();
                     // hash which holds the action/value information
                     // the actions are the hash keys, the values
                     // are the values
}


public abstract class CGroundingModel {
	public static final float INVALID_EVENT =((float)-1000000.203040);
	public static final String GROUNDINGMODEL_STREAM_TAG = "GroModel";
	//---------------------------------------------------------------------
	// protected class members
	//---------------------------------------------------------------------

    protected String sName;       // the model name
    protected String sModelPolicy;// the model policy name
    
    //D: Then define the policy as an array of state/action/values
    protected ArrayList<TStateActionsValues> pPolicy = 
    		new ArrayList<TStateActionsValues>();    
    					// the policy for the model: for each state, a
                        //  state-actions-values structure

    protected boolean bExternalPolicy;
                        // indicates that an external module is used for
                        //  implementing the decision policy

    protected CExternalPolicyInterface pepiExternalPolicy;

    protected String sExternalPolicyHost; 
                        // specifies the host for the socket connection 
                        //  for the external policy (hostname:port)

    protected ArrayList<Integer> viActionMappings = new ArrayList<Integer>(); 
                        // maps the local actions for this model (numbered
                        // (0..n) to the global action number (as held
                        // by the grounding manager)

    protected String sExplorationMode;  
                        // indicates the type of exploration to be performed
                        // by the model

    protected float fExplorationParameter;
                        // parameter that controls the amount of exploration
                        // the model does (epsilon for epsilon-greedy 
                        // exploration and temperature for boltzmann 
                        // exploration)
    
    protected CState stFullState;
                        // the full state of the model at a given point

    protected CBeliefDistribution bdBeliefState;
                        // the aggregated belief state of the model at 
                        // a given point

    protected CBeliefDistribution bdActionValues = new CBeliefDistribution();
                        // the values for various actions at a given point
                        // (state)

    protected int iSuggestedActionIndex;
    
    //-----------------------------------------------------------------------------
	 // D: Constructors and Destructors
	 //-----------------------------------------------------------------------------
	 // D: default constructor
	 public CGroundingModel(String sAModelPolicy, String sAName) {
	     sModelPolicy = sAModelPolicy;
	     sName = sAName;
	     bExternalPolicy = false;
	     sExternalPolicyHost = "localhost:0";
	     pepiExternalPolicy = null;
	     sExplorationMode = "epsilon-greedy";
	     fExplorationParameter = (float)0.2;
	     iSuggestedActionIndex = -1;    
	 }
	 // Overload
	 public CGroundingModel(String sAModelPolicy) {
	     sModelPolicy = sAModelPolicy;
	     sName = "Unknown";
	     bExternalPolicy = false;
	     sExternalPolicyHost = "localhost:0";
	     pepiExternalPolicy = null;
	     sExplorationMode = "epsilon-greedy";
	     fExplorationParameter = (float)0.2;
	     iSuggestedActionIndex = -1;    
	 }
	
	 // D: Constructor from reference
	 public CGroundingModel(CGroundingModel rAGroundingModel) {
	     sModelPolicy = rAGroundingModel.sModelPolicy;
	     sName = rAGroundingModel.sName;
	     bExternalPolicy = rAGroundingModel.bExternalPolicy;
	     sExternalPolicyHost = rAGroundingModel.sExternalPolicyHost;
	     pepiExternalPolicy = rAGroundingModel.pepiExternalPolicy;
	     pPolicy = rAGroundingModel.pPolicy;
	     viActionMappings = rAGroundingModel.viActionMappings;
	     sExplorationMode = rAGroundingModel.sExplorationMode;
	     fExplorationParameter = rAGroundingModel.fExplorationParameter;
	     bdBeliefState = rAGroundingModel.bdBeliefState;
	     stFullState = rAGroundingModel.stFullState;
	     iSuggestedActionIndex = rAGroundingModel.iSuggestedActionIndex;
	 }
   public CGroundingModel(){
	   
   }
   //------------------------------------------------------------------------------
   // Abstract Method
   //------------------------------------------------------------------------------
   public abstract void computeFullState();
   public abstract void computeBeliefState();
   
   	//-----------------------------------------------------------------------------
	// D: Grounding model factory method
	//-----------------------------------------------------------------------------
	
	public CGroundingModel GroundingModelFactory(String sModelPolicy) {
	    Log.e(GROUNDINGMODEL_STREAM_TAG,"Could not create abstract grounding model class.");
	    return null;
	}
	
	//-----------------------------------------------------------------------------
	// D: Member access methods
	//-----------------------------------------------------------------------------

	// D: return the type of the grounding model
	public String GetType() {
	    return "generic";
	}

	// D: return the String description of the grounding model
	public String GetModelPolicy() {
	    return sModelPolicy;
	}

	// D: return the name of the grounding model
	public String GetName() {
	    return sName;
	}

	// D: sets the name of the grounding model
	public void SetName(String sAName) {
	    sName = sAName;
	}

	// the index of the action suggested by the 
    // model from the current state (this is the
    // absolute action index, as stored by the
    // grounding manager
    //-----------------------------------------------------------------------------
  	// D: Grounding model specific public methods
  	//-----------------------------------------------------------------------------
    public void SetRequestAgent(CDialogAgent pARequestAgent){
    	Log.e(GROUNDINGMODEL_STREAM_TAG,"error:call the SetRequestAgent method " +
    			"on CGroundingModle class");
    }
	//-----------------------------------------------------------------------------
	// D: Grounding model specific public methods
	//-----------------------------------------------------------------------------

	// D: Initializes the model
	public void Initialize() {
	    if(!LoadPolicy())
	        Log.e(GROUNDINGMODEL_STREAM_TAG,"Invalid policy for grounding model "+
	        		sModelPolicy+".");
	}
	// D: Loads the model policy (from the grounding manager agent)
	public boolean LoadPolicy() {
	    // get the String data
	    String sData = DMCore.pGroundingManager.GetPolicy(sModelPolicy);

	    // parse it - first break it into lines
	    ArrayList<String> vsLines = new ArrayList<String>();
	    vsLines = Utils.PartitionString(sData, "\n");
	    boolean bExplorationModeLine = true;
	    boolean bExplorationParameterLine = false;
	    boolean bActionsLine = false;
	    boolean bExternalPolicyHostLine = true;
	    int i = 0;
	    while(i < vsLines.size()) {
	        // check that it's not a commentary or an empty line
	        if(vsLines.isEmpty() || (vsLines.get(i).charAt(0) == '#') || 
	            (vsLines.get(i).charAt(0) == '/') || (vsLines.get(i).charAt(0) == '%')) {
	            i++;
	            continue;
	        }
	        SplitReturnType srt = new SplitReturnType();
	        // o/w, if we expect the exploration mode line
	        if(bExplorationModeLine || bExternalPolicyHostLine) {
	            // read a line in the format EXPLORATION_MODE=...
	            // or external_policy_host=...
	            
	            srt = Utils.SplitOnFirst(vsLines.get(i), "=");
	            String sTemp1 = srt.FirstPart;
	            String sTemp2 = srt.SecondPart;
	            if(sTemp1.trim().toLowerCase() == "exploration_mode") {
	                // set the external policy flag to false
	                bExternalPolicy = false;
	                // set the exploration mode
	                sExplorationMode = sTemp2.trim().toLowerCase();
	                // and next, set the expectation for the exploration parameter line
	                bExplorationModeLine = false;
	                bExplorationParameterLine = true;
	                bExternalPolicyHostLine = false;
	            } else if(sTemp1.trim().toLowerCase() == "external_policy_host") {
	                // set the external policy flag to true
	                bExternalPolicy = true;
	                // set the host
	                sExternalPolicyHost = sTemp2.trim().toLowerCase();
	                bExplorationModeLine = false;
	                bExplorationParameterLine = false;
	                bExternalPolicyHostLine = false;
	                bActionsLine = true;
	            } else return false;
	        } else if(bExplorationParameterLine) {
	            // read a line in the format EXPLORATION_PARAMETER=...
	            srt = Utils.SplitOnFirst(vsLines.get(i), "=");
	            String sTemp1 = srt.FirstPart;
	            String sTemp2 = srt.SecondPart;
	            if(sTemp1.trim().toLowerCase() != "exploration_parameter")
	                return false;
	            // set the exploration parameter
	            fExplorationParameter = Float.valueOf(sTemp2.trim());
	            // and next, set the expectation for the exploration parameter line
	            bExplorationParameterLine = false;
	            bActionsLine = true;
	        } else if(bActionsLine) {
	            // obtain the list of actions
	            ArrayList<String> vsActions = new ArrayList<String>();
	            vsActions =Utils.PartitionString(vsLines.get(i), " \t");
	            // check that there are some actions in the model
	            if(vsActions.size() < 1) return false;
	            // construct the action index vector
				for(int a = 0; a < vsActions.size(); a++)
	                viActionMappings.add(
	                    DMCore.pGroundingManager.GroundingActionNameToIndex(vsActions.get(a)));
	            // set actions line to false
	            bActionsLine = false;        
	        } else if(!bExternalPolicy) {
	            // if it's not the first line, then it will be a line containing a 
	            // state and the values
	            ArrayList<String> vsValues = new ArrayList<String>();
	            vsValues = Utils.PartitionString(vsLines.get(i), " \t");
	            // check that there's enough values
	            if(vsValues.size() != viActionMappings.size() + 1) 
	                return false;
	            
	            // construct the state-action-utility datastructure
	            TStateActionsValues savData = new TStateActionsValues();
	            savData.sStateName = vsValues.get(0);
	            for(int a = 1; a < vsValues.size(); a++) {
					if(vsValues.get(a) == "-") {
	                    // check if we have an unavailable action
	                    savData.i2fActionsValues.put(a-1, (float) -1.0);
	                } else {
	                    // o/w get the utility
	                    savData.i2fActionsValues.put(a-1, Float.valueOf(vsValues.get(a)));	                        
	                }
	            }

	            // push it in the policy
	            pPolicy.add(savData);
	 			
	       }
	        i++;
	    }

	    // resize the bdActionValues vector accordingly
		//bdActionValues.Resize(viActionMappings.size());
		
	    // now if we have an external policy interface, create it
	    if(bExternalPolicy)
	        pepiExternalPolicy = 
	            DMCore.pGroundingManager.CreateExternalPolicyInterface(sExternalPolicyHost);

	    return true;
	}
	// D: Compute the state of the model
	public void ComputeState() {
	    // first compute the full state
	    computeFullState();
	    // then based on that, derive the belief state
	    computeBeliefState();
	    // and invalidate the suggested action (not computed yet for this state)
	    iSuggestedActionIndex = -1;
	}
	

	// D: Computes the expected values of the various actions and returns a 
	//	    corresponding probability distribution over the actions
	public void ComputeActionValuesDistribution() {

	    // for each action
	    /*for(int a = 0; a < viActionMappings.size(); a++) {

	        // compute its expected utility by summing over the states
	        bdActionValues.getIndexing(a) = 0;
	        boolean bUnavailableAction = true;
	        for(int s = 0; s < pPolicy.size(); s++) {
	            float fStateActionValue = pPolicy.get(s).i2fActionsValues.get(a);
	            // if the action is available from that state, 
	            if(fStateActionValue != UNAVAILABLE_ACTION) {
	                if(bdBeliefState.get(s) != 0) {
	                    // update the utility
	                    bdActionValues.get(a) += 
	                        fStateActionValue*bdBeliefState.get(s);
	                    bUnavailableAction = false;
	                } 
	            }
	        }
	        if(bUnavailableAction)
	            bdActionValues.get(a) = UNAVAILABLE_ACTION;
	    }*/
	}

	// D: Compute the suggested action index - be default, the action that 
//	    maximizes the expected utility
	public int ComputeSuggestedActionIndex() {

	    // first check if we already computed a suggested action index
	    if(iSuggestedActionIndex != -1)
	        return iSuggestedActionIndex;

	    // now check if we need to use an external policy
	    if(bExternalPolicy) {
	        /*iSuggestedActionIndex = 
	            pepiExternalPolicy.ComputeSuggestedActionIndex(stFullState);*/
	        return iSuggestedActionIndex;
	    }

	    // compute the values for actions from this state
	    ComputeActionValuesDistribution();

	    // now, depending on the type of exploration mode
	    if(sExplorationMode == "greedy") {
	        // if greedy, choose the optimal action
	        iSuggestedActionIndex = bdActionValues.GetModeEvent();
	    } else if(sExplorationMode == "stochastic") {
	        // if stochastic, just choose an action randomly from the value distr
	        iSuggestedActionIndex = bdActionValues.GetRandomlyDrawnEvent();
	    } else if(sExplorationMode == "epsilon-greedy") {
	        // if epsilon-greedy, 
	        iSuggestedActionIndex = 
	            bdActionValues.GetEpsilonGreedyEvent(fExplorationParameter);
	    } else if(sExplorationMode == "soft-max") {
	        // if soft-max
	        iSuggestedActionIndex =
	            bdActionValues.GetSoftMaxEvent(fExplorationParameter);
	    } else {
	        // o/w there's an error
	        Log.e(GROUNDINGMODEL_STREAM_TAG,"Unknown exploration mode: "+sExplorationMode+".");
	    }

	    // apply the mapping
	    iSuggestedActionIndex = viActionMappings.get(iSuggestedActionIndex);

	    // and return the value
	    return iSuggestedActionIndex;
	}

	// D: Run the grounding model 
	public void Run() {
	    // compute the action index
	    ComputeSuggestedActionIndex();
	    // run that action
	    RunAction(iSuggestedActionIndex);
	}

	// D: Run a particular action 
	public void RunAction(int iActionIndex) {
	    // obtains a pointer to the action from the grounding manager
	    // and runs it with no parameters
	    //DMCore.pGroundingManager.getIndexing(iActionIndex).Run();
	}

	// D: Log the state and the suggested action of the model
	public void LogStateAction() {
	    // log these computations
	    Log.d(GROUNDINGMODEL_STREAM_TAG,"Grounding model "+GetName()
	    	+" [TYPE="+GetType()+";POLICY="+GetModelPolicy()
	        +";EM="+sExplorationMode+";EP="+fExplorationParameter+"]:\n"
	        +"Full state: \n"+stFullState.ToString()+"Belief state: "+ beliefStateToString()
	        +"\nAction values (dumped below):\n"+actionValuesToString()+"\nSuggested action: "
	        +DMCore.pGroundingManager.GroundingActionIndexToName(iSuggestedActionIndex)+"\n");
	}

	//-----------------------------------------------------------------------------
	// D: Grounding model specific private methods
	//-----------------------------------------------------------------------------

	// D: Convert the belief state to a String representation
	public String beliefStateToString() {
	    String sResult = "";
	    for(int i = 0; i < pPolicy.size(); i++) {
	        sResult += pPolicy.get(i).sStateName+":"+bdBeliefState.getIndexing(i); 
	    }
	    return sResult;
	}

	// D: Convert the action values to a String representation
	public String actionValuesToString() {
	    String sResult = "";
	    for(int i = 0; i < viActionMappings.size(); i++) {
	        if(bdActionValues.getIndexing(i) == INVALID_EVENT) {
	            sResult +=  DMCore.pGroundingManager.GroundingActionIndexToName(
	                    viActionMappings.get(i))+"  : -\n";
	        } else {
				if(bdActionValues.LowBound(i) == INVALID_EVENT) {
					sResult += DMCore.pGroundingManager.GroundingActionIndexToName(
							viActionMappings.get(i))+"  :"+bdActionValues.getIndexing(i)+"\n";
				} else {
					sResult += DMCore.pGroundingManager.GroundingActionIndexToName(
							viActionMappings.get(i))+"  :"+bdActionValues.getIndexing(i)
							+" ["+bdActionValues.LowBound(i)+"-"+bdActionValues.HiBound(i)+"]\n";
				}
	        }
	    }
	    return sResult;
	}

	
}
