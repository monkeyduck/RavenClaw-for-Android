package dmcore.agents.coreagents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

import android.util.Log;

import dmcore.agents.dialogagents.CDialogAgent;
import dmcore.agents.mytypedef.TGroundingManagerConfiguration;
import dmcore.agents.mytypedef.TSystemActionOnConcept;
import dmcore.agents.mytypedef.AgentFactory;
import dmcore.concepts.CConcept;
import dmcore.grounding.groundingaction.CGroundingAction;
import dmcore.grounding.groundingmodel.CExternalPolicyInterface;
import dmcore.grounding.groundingmodel.CGroundingModel;
import dmcore.grounding.groundingmodel.FCreateGroundingModel;


//D: type for the stack of concept grounding requests
class TConceptGroundingRequest {
    CConcept pConcept;
    int iGroundingRequestStatus;
    int iSuggestedActionIndex;
    int iTurnNumber;
}

class TGroundingActionHistoryItem {
    String sGroundingModelName;         // the name of the grounding model that
                                        //  took the action
    String sActionName;                 // the name of the grounding action
    int iGroundingActionType;           // the grounding action type
    boolean bBargeIn;                      // was there a barge-in on the action
}

//-----------------------------------------------------------------------------
//
// D: CGroundingManagerAgent class -
//	      implements the grounding model which chooses the right grounding 
//	      action based on the current grounding state
//
//-----------------------------------------------------------------------------
public class CGroundingManagerAgent extends CAgent implements AgentFactory{
	//---------------------------------------------------------------------
	// private grounding manager agent specific members
	//---------------------------------------------------------------------

    // hash holding the grounding models policies (key = model_name, 
    // value=model policy)
    private HashMap<String, String> s2sPolicies = new HashMap<String, String>();

    // hash holding various constant parameters for feature computation
    private HashMap<String, Float> s2fConstantParameters=new HashMap<String, Float>();

	// hash holding the belief updating models
	private HashMap<String, HashMap<String, ArrayList<Float>>> s2s2vfBeliefUpdatingModels=
			new HashMap<String, HashMap<String,ArrayList<Float>>>();

	// hash holding information about the various concepts
	private HashMap<String, HashMap<String, ArrayList<Float>>> s2s2vfConceptValuesInfo=
			new HashMap<String, HashMap<String,ArrayList<Float>>>(); ;

	// hash holding information about the concept type
    private HashMap<String, String> s2sConceptTypeInfo = new HashMap<String, String>();

	// hash holding the precomputed belief updating features
    private HashMap<String, Float> s2fBeliefUpdatingFeatures=new HashMap<String, Float>();

    // array holding the grounding actions available 
    private ArrayList<CGroundingAction> vpgaActions = new ArrayList<CGroundingAction>();
    
    // parallel array holding the names of the grounding actions
    private ArrayList<String> vsActionNames = new ArrayList<String>();

    // hash with pointers to the externally implemented policies
    // D: auxiliary define for a map holding the list of external policies
    private HashMap<String, CExternalPolicyInterface> mapExternalPolicies =
    		new HashMap<String, CExternalPolicyInterface>();

    // the grounding manager configuration
    private TGroundingManagerConfiguration gmcConfig = new TGroundingManagerConfiguration();

    // flag which indicates if we need to ground turns
    boolean bTurnGroundingRequest;

    // vector implementing the concept grounding stack
    private ArrayList<TConceptGroundingRequest>vcgrConceptGroundingRequests =
    		new ArrayList<TConceptGroundingRequest>();

    // flag indicating if the stack is locked
    private boolean bLockedGroundingRequests;
    
    // the history of grounding actions
    private ArrayList<TGroundingActionHistoryItem> TGroundingActionHistoryItems = 
    		new ArrayList<TGroundingActionHistoryItem>();
    private ArrayList<ArrayList<TGroundingActionHistoryItem>> vgahiGroundingActionsHistory = 
    		new ArrayList<ArrayList<TGroundingActionHistoryItem>>();
    
    // the hash containing the grounding model factories
    private HashMap<String, FCreateGroundingModel> gmthGroundingModelTypeRegistry = 
    		new HashMap<String, FCreateGroundingModel>();

	//---------------------------------------------------------------------
	// Constructor and destructor
	//---------------------------------------------------------------------
	//
	// Default constructor
    public CGroundingManagerAgent(String sAName){
	     super(sAName);
		 String sAConfiguration = "";
	     String sAType = "CAgent:CGroundingManagerAgent";
	     SetConfiguration(sAConfiguration);
	     SetType(sAType);
	     // set the turn grounding signal to false
	     bTurnGroundingRequest = false;
	
	     // set the lock to false
	     bLockedGroundingRequests = false; 
    }
    public CGroundingManagerAgent(String sAName,String sAConfiguration){
        super(sAName, sAConfiguration);
        this.sType = "CAgent:CGroundingManagerAgent";
	     // set the turn grounding signal to false
	     bTurnGroundingRequest = false;
	
	     // set the lock to false
	     bLockedGroundingRequests = false; 
    }
	public CGroundingManagerAgent(String sAName, String sAConfiguration, 
	 								 String sAType){
	     super(sAName, sAConfiguration, sAType);
	     
	     // set the turn grounding signal to false
	     bTurnGroundingRequest = false;
	
	     // set the lock to false
	     bLockedGroundingRequests = false; 
	 }

	public CGroundingManagerAgent() {
		// TODO Auto-generated constructor stub
	}
	// Static function for dynamic agent creation
	public CAgent AgentFactory(String sAName, String sAConfiguration){
		return new CGroundingManagerAgent(sAName, sAConfiguration);
	}
	//-----------------------------------------------------------------------------
	//
	// GroundingManagerAgent class specific public methods
	//
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// D: Access to configuration information
	//-----------------------------------------------------------------------------

	// D: Set the configuration String
	public void SetConfigurationByGroundingManager(String sAGroundingManagerConfiguration) {
	    gmcConfig.sGroundingManagerConfiguration = 
	        sAGroundingManagerConfiguration.toLowerCase();

		Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Setting configuration: "+
				gmcConfig.sGroundingManagerConfiguration+",");

		ArrayList<String> vsTokens = new ArrayList<String>();
		vsTokens = Utils.PartitionString(gmcConfig.sGroundingManagerConfiguration, ";");

		// set the defaults
		gmcConfig.bGroundConcepts = false;
		gmcConfig.bGroundTurns = false;
		gmcConfig.sBeliefUpdatingModelName = "npu";

		for (int i = 0; i < vsTokens.size(); i++) {
			if(vsTokens.get(i) == "no_grounding") {
				// if no grounding
				gmcConfig.bGroundConcepts = false;
				gmcConfig.bGroundTurns = false;
				gmcConfig.sBeliefUpdatingModelName = "npu";
				break;
			} else {
				String sSlot = "", sValue = "default";
				SplitReturnType srt = new SplitReturnType();
				srt = Utils.SplitOnFirst(vsTokens.get(i), ":");
				sSlot = srt.FirstPart;
				sValue= srt.SecondPart;

				if (sSlot == "concepts") {
					// concept grounding model specification
					if((sValue.toLowerCase()) == "default") 
					    sValue = "concept_default";
					gmcConfig.sConceptGM = sValue;
					gmcConfig.bGroundConcepts = !(sValue == "none");
				}
				else if (sSlot == "turns") {
					// turn grounding model specification
					if((sValue.toLowerCase()) == "default") 
					    sValue = "request_default";
					gmcConfig.sTurnGM = sValue;
					gmcConfig.bGroundTurns = !(sValue == "none");
				}
				else if (sSlot == "beliefupdatingmodel") {
					// belief updating model specification
					SetBeliefUpdatingModelName(sValue);				
				}
			}
		}	
	}

	// D: Returns the configuration String
	public TGroundingManagerConfiguration GetConfiguration() {
		return gmcConfig;
	}

	//-----------------------------------------------------------------------------
	// D: Operations with the model data
	//-----------------------------------------------------------------------------

	// D: Load the model specifications
	public void LoadPoliciesFromString(String sPolicies) {

	    // log
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Loading grounding models ...");

	    // read the file line by line
		ArrayList<String> vsPolicies = new ArrayList<String>();
		vsPolicies = Utils.PartitionString(sPolicies, ";");
		for (int i=0; i < vsPolicies.size(); i++) {
	        String sModelName; 
	        String sModelFileName;
			String sModelData;

	        // check for ModelName = ModelFileName pair
			SplitReturnType srt = new SplitReturnType();
			srt = Utils.SplitOnFirst(vsPolicies.get(i), "=");
	        if(srt.IsSplitSuccessful) {

	            // extract the model name and filename
	        	sModelName = srt.FirstPart;
	        	sModelFileName = srt.SecondPart;
	            sModelName = sModelName.trim().toLowerCase();
				sModelData = loadPolicy(sModelFileName.trim());

				if (sModelData.length()==0) {
					Log.e(Const.GROUNDINGMANAGER_STREAM_TAG, "Unable to load policy for "+
							sModelName+" from file "+sModelFileName);
				} else {
					// add it to the hash
					s2sPolicies.put(sModelName, loadPolicy(sModelFileName));
				}
			}
	    }

	    // Log the models loaded
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, vsPolicies.size()+" grounding model(s) loaded.");
	}

	// A: Load the model specifications
	public void LoadPoliciesFromFile(String sFileName) {

	    // open the file
		try{
			File fid = new File(sFileName);
			if (!fid.isFile()){
				Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Grounding policies specification file ("+
						sFileName+") could not be open for reading.");
				return;
			}
			// log
		    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Loading grounding models ...");

		    // read the file line by line
		    char lpszLine[] = new char[Utils.STRING_MAX];
		    String lpszLine2="";
		    int iModelsCount = 0;
    		InputStreamReader reader = new InputStreamReader(  
                    new FileInputStream(fid)); 				// Build a input stream object reader  
            BufferedReader br = new BufferedReader(reader); // Build an object to translate code
            while ((lpszLine2 = br.readLine()) != null){
            	lpszLine = lpszLine2.toCharArray();
		        String sModelName=""; 
		        String sModelFileName="";
				String sModelData="";
		        // check for comments
		        if((lpszLine[0] == '#') || 
		           ((lpszLine[0] == '/') && lpszLine[1] == '/'))
		            continue;

		        // check for ModelName = ModelFileName pair
		        SplitReturnType srt = new SplitReturnType();
		        srt = Utils.SplitOnFirst(String.valueOf(lpszLine), "=");
		        if(srt.IsSplitSuccessful) {
		        	
		            // extract the model name and filename
		        	sModelName = srt.FirstPart;
		        	sModelFileName = srt.SecondPart;
		            sModelName = sModelName.trim().toLowerCase();
					sModelData = loadPolicy(sModelFileName.trim());

					if (sModelData.length()==0) {
						Log.e(Const.GROUNDINGMANAGER_STREAM_TAG, "Unable to load policy for "+
								sModelName+" from file "+sModelFileName);
					} else {
						// add it to the hash
						s2sPolicies.put(sModelName, sModelData);

						// and count it up
						iModelsCount++;
					}
				}
		    }
            // close reader
		    reader.close();

		    // Log the models loaded
		    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, iModelsCount+" grounding model(s) loaded.");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// D: Return the data for a certain grounding model
	public String GetPolicy(String sModelName) {
	    return s2sPolicies.get(sModelName);
	}

	// D: create an external policy interface
	public CExternalPolicyInterface CreateExternalPolicyInterface(String sAHost) {
	    CExternalPolicyInterface pEPI;
	    // check if it's a new policy
	    if(!mapExternalPolicies.containsKey(sAHost)) {
	        // create it
	        pEPI = new CExternalPolicyInterface(sAHost);
	        // add it to the hash
	        mapExternalPolicies.put(sAHost, pEPI);
	        // and return it
	        return pEPI;
	    } else {
	        // o/w return it from the hash
	        return mapExternalPolicies.get(sAHost);
	    }
	}

	// D: release the set of external policy interfaces
	public void ReleaseExternalPolicyInterfaces() {
	    // and now erase the hash
	    mapExternalPolicies.clear();
	}
	//-----------------------------------------------------------------------------
	// Methods for the belief updating model
	//-----------------------------------------------------------------------------
	
	// D: load the belief updating model from a file
	public void LoadBeliefUpdatingModel(String sAFileName) {

		try{
			// open the file
			File fid = new File (sAFileName);
			if (!fid.isFile()){
				Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Belief updating model file ("+sAFileName+
						") could not be open for reading.");
				return;
			}

		    // log that we are loading the belief updating model
		    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Loading belief updating model from "+sAFileName+" ..."); 

		    // read the file line by line
		    InputStreamReader reader = new InputStreamReader(  
                    new FileInputStream(fid));   
            BufferedReader br = new BufferedReader(reader);   
		    char lpszLine[] = new char[Utils.STRING_MAX];
		    String lpszLine2="";
			SplitReturnType srt =new SplitReturnType();

		    while((lpszLine2 = br.readLine()) != null) {
		    	lpszLine = lpszLine2.toCharArray();
				// get the line as a String
				String sLine = lpszLine2;

		        String sModelName=""; 
		        String sModelFileName="";
		        // check for comments
		        if((lpszLine[0] == '#') || 
		           ((lpszLine[0] == '/') && lpszLine[1] == '/'))
		            continue;

				// o/w, check for constants declaration
				if(sLine.substring(0, 9).toLowerCase() == "constants") {
				
					// then go on and read the constant parameters 
					while((lpszLine2 = br.readLine()) != null &&
						  lpszLine2.trim().toLowerCase() != "end"){
						// get the line
						sLine = lpszLine2;
						srt = Utils.SplitOnFirst(sLine, "=");
						String sParam = srt.FirstPart;
						String sCoef  = srt.SecondPart;
						// insert the parameters in the hash
						s2fConstantParameters.put(sParam.trim().toLowerCase(),Float.valueOf(sCoef));
					}
				}

				// o/w, check for lr_model(action)
				if(sLine.substring(0, 8).toLowerCase() == "lr_model") {
				
					// then go on and read the parameters for each individual
					// feature in the model
					srt = Utils.SplitOnFirst(sLine, "(");
					String sFoo = srt.FirstPart;
					String sAction = srt.SecondPart;
					sAction = Utils.TrimRight(sAction, " \n)");

					// check that the action is one of the ones we expect
					if((sAction != Const.SA_REQUEST) && (sAction != Const.SA_EXPL_CONF) && 
						(sAction != Const.SA_IMPL_CONF) && (sAction != Const.SA_OTHER) && 
						(sAction != Const.SA_UNPLANNED_IMPL_CONF)) {
						Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,
							"Error loading belief updating model. Unknown action "+sAction+".");
					}

					// construct the model for this action
					HashMap<String, ArrayList<Float>> s2vfMRModel =
							new HashMap<String, ArrayList<Float>>();
					
					// now continue through the lines
					while((lpszLine2 = br.readLine()) != null &&
							lpszLine2.trim().toLowerCase() != "end"){
						// get the line
						sLine = lpszLine2;
						srt = Utils.SplitOnFirst(sLine, "=");
						String sParam = srt.FirstPart;
						String sCoef  = srt.SecondPart;
						ArrayList<String> vsCoefs = new ArrayList<String>();
						vsCoefs=Utils.PartitionString(sCoef, ";");
						ArrayList<Float> vfCoefs = new ArrayList<Float>();
						
						
						for(int i = 0; i < vsCoefs.size(); i++) {
							vfCoefs.add(Float.valueOf(vsCoefs.get(i)));
						}
						// insert the parameters in the hash
						s2vfMRModel.put(sParam.trim().toLowerCase(), vfCoefs);
					}

					// now store the model
					s2s2vfBeliefUpdatingModels.put(sAction, s2vfMRModel);
				}

				// o/w, check for concept values info
				if(sLine.substring(0, 14).toLowerCase() == "concept_values") {
				
					// then go on and read the priors and confusability for each 
					// value for that concept
					srt = Utils.SplitOnFirst(sLine, "(");
					String sFoo = srt.FirstPart;
					String sFoo2 = srt.SecondPart;
					sFoo2 = Utils.TrimRight(sFoo2, " \n)");	
					srt = Utils.SplitOnFirst(sFoo2, ":");
					String sConcept = srt.FirstPart;
					String sConceptType = srt.SecondPart;
					
					// construct the list of values for this concept
					HashMap<String, ArrayList<Float>> s2vfConceptValues = 
							new HashMap<String, ArrayList<Float>>();

					// now continue through the lines
					while((lpszLine2 = br.readLine()) != null &&
							lpszLine2.trim().toLowerCase() != "end"){
						// get the line
						sLine = lpszLine2;
						ArrayList<String> vsItems = new ArrayList<String>();
						vsItems = Utils.PartitionString(sLine, "\t");
						ArrayList<Float> vfParams = new ArrayList<Float>();
						for(int i = 1; i < vsItems.size(); i++) {
							vfParams.add(Float.valueOf(vsItems.get(i)));
						}
						// insert the parameters in the hash
						s2vfConceptValues.put(vsItems.get(0).trim().toLowerCase(), vfParams);
					}

					// now store the list of values
					s2s2vfConceptValuesInfo.put(sConcept, s2vfConceptValues);

					// and the concept type
					s2sConceptTypeInfo.put(sConcept, sConceptType);
				}
				reader.close();
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	    // Log the models loaded
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Belief updating grounding model loaded.");
	}
	
	
	// D: Sets the belief updating model
	public void SetBeliefUpdatingModelName(
		String sABeliefUpdatingModelName) {
		// store the belief updating model
		gmcConfig.sBeliefUpdatingModelName = sABeliefUpdatingModelName.toLowerCase();

		// if the model type is not NPU
		String sModelName, sModelFile;
		SplitReturnType srt = new SplitReturnType();
		srt = Utils.SplitOnFirst(gmcConfig.sBeliefUpdatingModelName, "(");
		sModelName = srt.FirstPart;
		sModelFile = srt.SecondPart;
		if(srt.IsSplitSuccessful) {
			// check that we have a calista model
			if(sModelName == "calista") {
				gmcConfig.sBeliefUpdatingModelName = "calista";
				// trim the filename
				sModelFile = Utils.TrimRight(sModelFile, ") \t\n");
				// load the belief updating model
				LoadBeliefUpdatingModel(sModelFile);
			} else {
				Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Unknown belief updating model type: "+
						sModelName+",");
			}
		}
	}
	// D: Gets the belief updating model name
	public String GetBeliefUpdatingModelName() {
		return gmcConfig.sBeliefUpdatingModelName;
	}
	// D: Indexing operator on the action name - returns the action
	public CGroundingAction getIndexing(String sGroundingActionName) {

	    return getIndexing(GroundingActionNameToIndex(sGroundingActionName));
	}

	// D: Indexing operator - returns the action corresponding to an index
	public CGroundingAction getIndexing (int iActionIndex) {
	    if(iActionIndex >= vpgaActions.size()) {    
	        Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Grounding action index "+iActionIndex+" out of bounds.");
	    }
	    return vpgaActions.get(iActionIndex);
	}
	//-----------------------------------------------------------------------------
	// D: Methods for requesting grounding needs
	//-----------------------------------------------------------------------------

	// D: signal the need for grounding the turn
	public void RequestTurnGrounding(boolean bATurnGroundingRequest) {
	    bTurnGroundingRequest = bATurnGroundingRequest && gmcConfig.bGroundTurns;
	}

	// D: signal the need for grounding a certain concept
	public void RequestConceptGrounding(CConcept pConcept) {

	    // first check that the current configuration allows to ground concepts
	    if(!gmcConfig.bGroundConcepts) return;

	    // if the queue is locked, issue a fatal error
	    if(bLockedGroundingRequests) 
	        Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Cannot add concept grounding request for "
	        		+pConcept.GetAgentQualifiedName()+": concept "
	        		+"grounding queue is locked.");
	            
	    // check if it's in the list
	    int iIndex = getConceptGroundingRequestIndex(pConcept);
	    if(iIndex != -1) {
		    // for all other grounding request status, just remove the request 
			// from the queue
	        vcgrConceptGroundingRequests.remove(iIndex);
	    }

	    // add the concept at the end
	    TConceptGroundingRequest cgr = new TConceptGroundingRequest();
	    cgr.pConcept = pConcept;
	    cgr.iGroundingRequestStatus = Const.GRS_UNPROCESSED;
	    cgr.iSuggestedActionIndex = -1;
	    cgr.iTurnNumber = DMCore.pDMCore.GetLastInputTurnNumber();
	    vcgrConceptGroundingRequests.add(cgr);
	}
	//-----------------------------------------------------------------------------
	// D: Run Method
	//-----------------------------------------------------------------------------

	// D: Run the grounding management process 
	public void Run() {

	    // and log it
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Running grounding process ...");

	    // if we are grounding a user turn 
	    /*if(bTurnGroundingRequest) {

	        // log the grounding of the last user turn
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Grounding last user turn ...");

	        // get the focused agent from the previous user input
	        CDialogAgent pdaFocusedAgent = (CDialogAgent *)
	            AgentsRegistry[pStateManager.GetLastState().sFocusedAgentName];

	        // and check that it still exists okay
	        if(!pdaFocusedAgent) {
	            // if the last focused agent is not to be found anymore signal
	            // a fatal error
	            FatalError(
	                FormatString("Could not locate the last focused agent: %s.",
	                pStateManager.GetLastState().sFocusedAgentName));
	        }

	        // first, check whether or not the last turn was a non-understanding
	        if(DMCore.pDMCore.LastTurnNonUnderstanding()) {
	            // if the last turn was a non-understanding
	            Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Last turn: non-understanding.");

	            // log which agent we're running
	            Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	                "Running grounding model for agent: %s.",
	                pdaFocusedAgent.GetName());

	            // check that the focused agent indeed has a grounding model
	            CGroundingModel *pgmGroundingModel;
	            if(pgmGroundingModel = pdaFocusedAgent.GetGroundingModel()) {
	                // take the focused agent on the stack, and compute it's state,
	                pgmGroundingModel.ComputeState();
	                // compute the suggested action index
	                int iActionIndex = 
	                    pgmGroundingModel.ComputeSuggestedActionIndex();
	                // and log the state and the suggested action
	                pgmGroundingModel.LogStateAction();
	                Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	                    "Executing last focused agent grounding action:\n%s <- %s", 
	                    GroundingActionIndexToName(iActionIndex),
	                    pdaFocusedAgent.GetName());
	                // add the action to history
	                GAHAddHistoryItem(pgmGroundingModel.GetName(), 
	                    GroundingActionIndexToName(iActionIndex), GAT_TURN);
	                // now run the action
	                pgmGroundingModel.RunAction(iActionIndex);

	                // indicate that the grounding process is completed
	                Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	                    "Grounding last user turn completed.");

	                // set the signal back to false
	                bTurnGroundingRequest = false;

	                // now we're not going to continue with grounding the 
	                // concepts, so simply return but record the completion 
	                // of the grounding process
	                Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Grounding process completed.");

	                return;

	            } else {
	                // if the focused agent does not have a grounding model
	                Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	                    "Last focused agent %s does not have a grounding "
	                    "model. No grounding action performed.", 
	                    pdaFocusedAgent.GetName());
	            }

	        } else {
	            // o/w/ notify that the turn was grounded 
	            Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Last turn: successful bindings.");
	        }

	        // indicate that the grounding process is completed
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	            "Grounding last user turn completed.");

	        // set the signal back to false
	        bTurnGroundingRequest = false;
	    }

	    // first purge the queue of completed agents
	    PurgeConceptGroundingRequestsQueue();

	    // now do the concept-level grounding, if there's anything to 
	    //   ground
	    if(DMCore.pDMCore.GetAgentInFocus().IsDTSAgent() &&
		   !vcgrConceptGroundingRequests.empty()) {

	        // log that we are grounding concepts
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Grounding concepts ...");

	        // start by locking the grounding requests queue
	        LockConceptGroundingRequestsQueue();

	        // go through all the concepts in the list, and compute their
	        // state and suggested action (also construct the String dump
	        // in parallel)
	        String sCGRDump;
	        for(int i = vcgrConceptGroundingRequests.size() - 1; i >= 0; i--) {
	            CConcept pConcept = vcgrConceptGroundingRequests.get(i).pConcept;
	            if((vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	                Const.GRS_UNPROCESSED) ||
	               (vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	                Const.GRS_PENDING)) {
	                // set the state to pending
	                vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus =
	                    Const.GRS_PENDING;
	                CGroundingModel* pGroundingModel = 
	                    pConcept.GetGroundingModel();
	                // compute the state
	                pGroundingModel.ComputeState();
	                // compute the suggested action
	                vcgrConceptGroundingRequests.get(i).iSuggestedActionIndex = 
	                    pGroundingModel.ComputeSuggestedActionIndex();        
	                // and construct the dump String
	                sCGRDump += FormatString("[PENDING]   %s (turn=%d)\n",
	                    pConcept.GetAgentQualifiedName(),                     
	                    vcgrConceptGroundingRequests.get(i).iTurnNumber);
	            } else if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	                Const.GRS_SCHEDULED) {
	                // just construct the dump String
	                sCGRDump += FormatString("[SCHEDULED] %s (turn=%d)\n",
	                    pConcept.GetAgentQualifiedName(),                     
	                    vcgrConceptGroundingRequests.get(i).iTurnNumber);
	            } else if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	                Const.GRS_EXECUTING){
	                // just construct the dump String
	                sCGRDump += FormatString("[EXECUTING] %s (turn=%d)\n",
	                    pConcept.GetAgentQualifiedName(),                     
	                    vcgrConceptGroundingRequests.get(i).iTurnNumber);
	            } else {
	                Log.e(CGR,
	                    "Invalid concept grounding request in queue (state: %d).",
	                    vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus));
	            }
	        }

	        // now log the concept grounding requests dump
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	            "Concept grounding requests dumped below:\n%s",
	            sCGRDump);

	        // now log the models
	        for(int i = vcgrConceptGroundingRequests.size() - 1; i >= 0; i--) {
	            if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	                Const.GRS_PENDING) {
	                vcgrConceptGroundingRequests.get(i).pConcept.GetGroundingModel().LogStateAction();
	            }
	        }

	        // now execute the actions, making sure that no more than 1 EC
	        // and 2 ICs are running at the same time

	        // start by counting how many we already have scheduled
	        int iScheduledExplicitConfirms = 0;
	        int iScheduledImplicitConfirms = 0;
	        for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	            TConceptGroundingRequest cgr = vcgrConceptGroundingRequests.get(i);
	            if((cgr.iGroundingRequestStatus == Const.GRS_SCHEDULED) ||                
	                (cgr.iGroundingRequestStatus == Const.GRS_EXECUTING)){
	                // get the action name
	                String sActionName = 
	                    GroundingActionIndexToName(cgr.iSuggestedActionIndex);
	                if(sActionName == "EXPL_CONF") iScheduledExplicitConfirms++;
	                else if(sActionName == "IMPL_CONF") iScheduledImplicitConfirms++;
	            }
	        }

	        // now go through them and set them to scheduled
	        for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	            // look at the pending ones
	            if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus ==
	                Const.GRS_PENDING) {
	                String sActionName = 
	                    GroundingActionIndexToName(
	                        vcgrConceptGroundingRequests.get(i).iSuggestedActionIndex);
	                if((sActionName == "EXPL_CONF") && 
	                    (iScheduledExplicitConfirms == 0)) {
	                    // the explicit confirm case
	                    vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus =
	                        Const.GRS_SCHEDULED;
	                    iScheduledExplicitConfirms++;
	                } else if((sActionName == "IMPL_CONF") && 
	                    (iScheduledImplicitConfirms < 2)) {
	                    // the implicit confirm case
	                    vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus =
	                        Const.GRS_SCHEDULED;
	                    iScheduledImplicitConfirms++;
	                } else if(sActionName == "ACCEPT") {
	                    // the accept case
	                    vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus =
	                        Const.GRS_SCHEDULED;
	                } 
	            }
	        }

			// now, go through the scheduled actions, and execute all the accepts
			// and one other action (giving preference to implicit confirms over
			// explicit confirms)        
			int iActionsTaken = 0;
	        for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	            // look at the ready ones
	            if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus ==
	                Const.GRS_SCHEDULED) {
	                String sActionName = 
	                    GroundingActionIndexToName(
	                        vcgrConceptGroundingRequests.get(i).iSuggestedActionIndex);
	                CConcept pConcept = 
	                    vcgrConceptGroundingRequests.get(i).pConcept;
	                if((sActionName == "ACCEPT") ||
					   ((iActionsTaken == 0) && (sActionName == "IMPL_CONF")) ||
					   ((iActionsTaken == 0) && (iScheduledImplicitConfirms == 0) && 
					   (sActionName == "EXPL_CONF"))) {
						// log the action we're about to take
						Log.d(GROUNDINGMANAGER_STREAM, 
							"Executing concept grounding action:\n%s <- %s",
							sActionName,
							pConcept.GetAgentQualifiedName());
						// seal the concept
						pConcept.Seal();
						// increment the actions taken if we just did an 
						//	explicit or implicit confirm
						if((sActionName == "IMPL_CONF") || (sActionName == "EXPL_CONF"))
							iActionsTaken++;
	                    // add the action to history
	                    GAHAddHistoryItem(pConcept.GetGroundingModel().GetName(), 
	                        GroundingActionIndexToName(
	                            vcgrConceptGroundingRequests.get(i).iSuggestedActionIndex), 
	                        GAT_CONCEPT);						
						// and finally, execute the action
						pConcept.GetGroundingModel().RunAction(
							vcgrConceptGroundingRequests.get(i).iSuggestedActionIndex);
					}
	            }
	        }

	        // unlock the grounding requests queue
	        UnlockConceptGroundingRequestsQueue();

	        // and purge it
	        PurgeConceptGroundingRequestsQueue();

	        // log that we are done grounding concepts
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Grounding concepts completed.");
	    }*/

	    // log that the whole grounding process has completed
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Grounding process completed.");
	}
	//-----------------------------------------------------------------------------
	// D: Private auxiliary methods
	//-----------------------------------------------------------------------------

	// D: Return the index of a concept grounding request, or -1 if not found
	public int getConceptGroundingRequestIndex(CConcept pConcept) {
	    //  go through the list    
	    for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	        if(vcgrConceptGroundingRequests.get(i).pConcept == pConcept) 
	            return i;
	    }
	    // o/w return -1
	    return -1;
	}

	// A: Load a policy from its description file
	public String loadPolicy(String sFileName) {
		// try to open the file
		try{
			File fidModel = new File(sFileName);
	        // o/w we're fine, so read the model data from the file
	        String lpszLine2="";
	        String sModelData="";
	        InputStreamReader reader = new InputStreamReader(  
                    new FileInputStream(fidModel)); 		// Build a input stream object reader  
            BufferedReader br = new BufferedReader(reader); // Build an object to translate code
            lpszLine2 = br.readLine();
            while (lpszLine2 != null){
            	sModelData = sModelData + lpszLine2;
            	lpszLine2 = br.readLine();
            }
	        
	        // close the file reader
	        reader.close();

	        // and now log that we successfully loaded it
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Loaded grounding model from "+
	        		sFileName+" .");

			return sModelData;
		}
		catch(Exception e){
            Log.e(Const.GROUNDINGMANAGER_STREAM_TAG, "Could not read grounding model from "+
            		sFileName+" .");
            return "";            
			
		}
       
	}
	//-----------------------------------------------------------------------------
	// D: Method for registering and creating various grounding model types
	//-----------------------------------------------------------------------------

	// D: register a grounding model type
	public void RegisterGroundingModelType(String sName, 
	    FCreateGroundingModel fctCreateGroundingModel) {
	    // insert it in the registry
	    gmthGroundingModelTypeRegistry.put(sName, fctCreateGroundingModel);
	}

	// D: create a grounding model for a given type
	public CGroundingModel CreateGroundingModel(String sModelType, String sModelPolicy) {
	    // find the factory method for that model type
	    if(gmthGroundingModelTypeRegistry.containsKey(sModelType)) {
	        // we found the factory, so use it
	        return gmthGroundingModelTypeRegistry.get(sModelType).
	        		GroundingModelFactory(sModelPolicy);
	    } else {
	        // o/w issue a fatal error
	        Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Could not find grounding model type "+
	        		sModelType+".");
	        return null;
	    }
	}
	// D: Find the index on an action given its name
	public int GroundingActionNameToIndex(String sGroundingActionName) {    

	    // go through the list of actions and identify by name
	    for(int i = 0; i < vsActionNames.size(); i++) {
	        if(vsActionNames.get(i) == sGroundingActionName)
	            return i;
	    }

	    // if not found generate an error
	    Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,"Grounding action "+sGroundingActionName
	    		+" is not registered with the GroundingManagerAgent.");
	    return -1;
	}
	//-----------------------------------------------------------------------------
	// D: Methods for accessing the state of the grounding management layer
	//-----------------------------------------------------------------------------

	// D: returns true if the grounding engine has been signaled
	public boolean HasPendingRequests() {
	    return HasPendingTurnGroundingRequest() || 
	        HasPendingConceptGroundingRequests();
	}

	// D: returns true if there is a pending turn grounding request
	public boolean HasPendingTurnGroundingRequest() {
	    return bTurnGroundingRequest;
	}

	// D: returns true if there are pending (UNPROCESSED or PENDING) concept 
	//	    grounding requests
	public boolean HasPendingConceptGroundingRequests() {
	    //  go through the list    
	    for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	        if((vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	            Const.GRS_UNPROCESSED) ||
	           (vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	            Const.GRS_PENDING))
	            return true;
	    }    
	    return false;
	}

	// D: determines if there are unprocessed concept grounding requests
	public boolean HasUnprocessedConceptGroundingRequests() {
	    //  go through the list    
	    for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	        if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	            Const.GRS_UNPROCESSED) 
	            return true;
	    }    
	    return false;
	}


	// D: determines if there are scheduled requests
	public boolean HasScheduledConceptGroundingRequests() {
	    //  go through the list
	    for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	        if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	            Const.GRS_SCHEDULED)
	            return true;
	    }    
	    return false;
	}

	// D: determines if there are executing requests
	public boolean HasExecutingConceptGroundingRequests() {
	    //  go through the list
	    for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
	        if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus == 
	            Const.GRS_EXECUTING)
	            return true;
	    }    
	    return false;
	}

	// D: determines if a concept is undergoing grounding 
	public boolean GroundingInProgressOnConcept(CConcept pConcept) {
	    return (getConceptGroundingRequestIndex(pConcept) != -1);
	}

	// D: check if there is a scheduled action for a concept
	public String GetScheduledGroundingActionOnConcept(CConcept pConcept) {
	    // find the request
	    int iIndex = getConceptGroundingRequestIndex(pConcept);
	    // check that it exists
	    if(iIndex == -1) return "";
	    // check that it has a scheduled status
	    if(vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus != Const.GRS_SCHEDULED)
	        return "";
	    // return the action
	    return GroundingActionIndexToName(
	        vcgrConceptGroundingRequests.get(iIndex).iSuggestedActionIndex);
	}
	

	// D: Find the name of an action given it's index
	public String GroundingActionIndexToName(int iGroundingActionIndex) {
	    return vsActionNames.get(iGroundingActionIndex);
	}
	
	//-----------------------------------------------------------------------------
	// D: Methods for manipulating the queue of concept grounding requests
	//-----------------------------------------------------------------------------

	// D: Lock the grounding requests queue
	public void LockConceptGroundingRequestsQueue() {
	    bLockedGroundingRequests = true;
	}

	// D: Unlock the grounding requests queue
	public void UnlockConceptGroundingRequestsQueue() {
	    bLockedGroundingRequests = false;
	}

	// D: Set the grounding request state
	public void SetConceptGroundingRequestStatus(
	    CConcept pConcept, int iAGroundingRequestStatus) {
	    // get the index of that concept grounding request
	    int iIndex = getConceptGroundingRequestIndex(pConcept);
	    // now check that it exists
	    if(iIndex == -1)
	        Log.e(Const.GROUNDINGMANAGER_STREAM_TAG,
	            "Could not set grounding request status on concept "
	        +pConcept.GetAgentQualifiedName()+"."); 
	    // if it exists, set the status
	    vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus =
	        iAGroundingRequestStatus;
	    // and log this 
	    Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, 
	        "Setting concept grounding request for "
	        		+pConcept.GetAgentQualifiedName()+" to ["
	        		+ Const.vsGRS[iAGroundingRequestStatus]+"]");
	}

	// Get the grounding request state for a concept
	public int GetConceptGroundingRequestStatus(CConcept pConcept) {
	    // get the index of that concept grounding request
	    int iIndex = getConceptGroundingRequestIndex(pConcept);
	    // now check that it exists
		if(iIndex == -1) {
	        Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Could not get grounding request status on concept "
		+pConcept.GetAgentQualifiedName()+" (not in the queue).");
			return -1;
		} else {
		   // if it exists, get the status
		   return vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus;
		}
	}

	// D: Signal that a concept grounding request has completed
	public void ConceptGroundingRequestCompleted(
	    CConcept pConcept) {
	    // get the index of that concept grounding request
	    int iIndex = getConceptGroundingRequestIndex(pConcept);
	    // if it exists in the queue, and it was currently executing
	    if((iIndex != -1) &&
	        (vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus == Const.GRS_EXECUTING)) {

	        // if the queue is locked, just mark it as done
	        if(bLockedGroundingRequests) 
	            vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus =
	                Const.GRS_DONE;
	        // o/w remove it altogether
	        else vcgrConceptGroundingRequests.remove(iIndex);
	    }   
	}

	// D: remove a grounding request
	public void RemoveConceptGroundingRequest(CConcept pConcept) {

	    // get the index
	    int iIndex = getConceptGroundingRequestIndex(pConcept);

	    // if it's in the list
		if(iIndex != -1) {
	        // if the queue is locked, just mark it as done
	        if(bLockedGroundingRequests) 
	            vcgrConceptGroundingRequests.get(iIndex).iGroundingRequestStatus =
	                Const.GRS_DONE;
	        else vcgrConceptGroundingRequests.remove(iIndex);
	    }
	}

	// D: Purge the list of grounding requests
	public void PurgeConceptGroundingRequestsQueue() {
	    boolean bSomethingPurged = true;
	    while(bSomethingPurged) {
	        bSomethingPurged = false;
	        for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++)
	            if(vcgrConceptGroundingRequests.get(i).iGroundingRequestStatus ==
	                Const.GRS_DONE) {
	                vcgrConceptGroundingRequests.remove(i);
	                bSomethingPurged = true;
	                break;
	            }
	    }
	}

	// D: force the grounding manager to schedule grounding for a certain concept
	public String ScheduleConceptGrounding(CConcept pConcept) {
		String sActionName="";
		// log that we are scheduling concept grounding
		Log.d(Const.GROUNDINGMANAGER_STREAM_TAG, "Scheduling concept grounding for concept "+pConcept.GetName()); 

		// first, call a simple request concept grounding
		/*RequestConceptGrounding(pConcept);
	           
	    // then get the index from the list
	    int iIndex = getConceptGroundingRequestIndex(pConcept);

		// now move to schedule this, changing the type from UNPROCESSED
			
	    // start by locking the grounding requests queue
	    LockConceptGroundingRequestsQueue();

	    // first, set the state to pending
	    vcgrConceptGroundingRequests[iIndex].iGroundingRequestStatus = 
			Const.GRS_PENDING;
		// get the grounding model
		CGroundingModel* pGroundingModel = pConcept.GetGroundingModel();
	    // compute the state
	    pGroundingModel.ComputeState();
	    // compute the suggested action
	    vcgrConceptGroundingRequests[iIndex].iSuggestedActionIndex = 
	        pGroundingModel.ComputeSuggestedActionIndex();        
	    // get the action that was suggested
	    string sActionName = GroundingActionIndexToName(
	        vcgrConceptGroundingRequests[iIndex].iSuggestedActionIndex);

	    // now log the model
	    vcgrConceptGroundingRequests[iIndex].pConcept.
			GetGroundingModel().LogStateAction();

	    // if the action is an explicit confirm, check that we don't already
		// have on scheduled (if we do, we need to take it off)
		if(sActionName == "EXPL_CONF") {
			for(int i = 0; i < (int)vcgrConceptGroundingRequests.size(); i++) {
				TConceptGroundingRequest cgr = vcgrConceptGroundingRequests[i];
				if((cgr.iGroundingRequestStatus == Const.GRS_SCHEDULED) &&
					(GroundingActionIndexToName(cgr.iSuggestedActionIndex) == "EXPL_CONF")) {
					// then we need to set this back to PENDING
					vcgrConceptGroundingRequests[i].iGroundingRequestStatus = 
						Const.GRS_PENDING;
				}
				// put a check that we don't already have something that's on stack or executing
				if((cgr.iGroundingRequestStatus == Const.GRS_EXECUTING) &&
					(GroundingActionIndexToName(cgr.iSuggestedActionIndex) == "EXPL_CONF")) {
					// then issue a fatal error
					FatalError(FormatString(
						"Could not schedule %s for grounding (EXPL_CONF), since %s"
						" is already undergoing grounding", pConcept.GetName().c_str(), 
						cgr.pConcept.GetName().c_str()));
				}
			}
		}

		// set it to scheduled
		vcgrConceptGroundingRequests[iIndex].iGroundingRequestStatus =
	        Const.GRS_SCHEDULED;

	    // unlock the grounding requests queue
	    UnlockConceptGroundingRequestsQueue();
*/
		// return the action name
		return sActionName;
	}

}
