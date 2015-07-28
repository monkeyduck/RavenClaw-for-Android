package dmcore.agents.coreagents;

import java.util.ArrayList;

import user.definition.UserDefinition;
import utils.Const;

import android.util.Log;

import dmcore.agents.dialogagents.CDialogAgent;
import dmcore.agents.mydialogagents.CFlightRoot;
import dmcore.agents.mytypedef.AgentFactory;
import dmcore.agents.mytypedef.TAddSubAgentMethod;
class TDiscourseAgentInfo{
	public String sDAName;
	public String sDAType;
	public FRegisterAgent fRegisterAgent;
	public String sDAConfiguration;
}
// D: enumeration type describing the various mounting methods
enum TMountingMethod{
	mmAsLastChild,
	mmAsFirstChild,
	mmAsLeftSibling,
	mmAsRightSibling
}



public class CDTTManagerAgent extends CAgent implements FRegisterAgent, AgentFactory{
	// private members
	private CDialogAgent pdaDialogTaskRoot;			// the dialog task root
	// a vector containing the information about the discourse agents to be
	// used
	private ArrayList<TDiscourseAgentInfo> vdaiDAInfo = 
			new ArrayList<TDiscourseAgentInfo>();
	@Override
	// define implemented method
	public void DeclareAgentInterface(String sAgentName){
		CRegistry.AgentsRegistry.RegisterAgent(sAgentName,this);
	}
	// Log tag
	public static final String DTTMANAGER_STREAM_TAG = "DTTManager";
	
	//-----------------------------------------------------------------------------
	// Constructors and Destructors
	//-----------------------------------------------------------------------------

	// D: constructor
	public CDTTManagerAgent(String sAName, String sAConfiguration, String sAType){
		super(sAName, sAConfiguration, sAType);
		// nothing here
	}
	// Constructor with sName
	public CDTTManagerAgent(String sAName){
		super(sAName);
		this.sType = "CAgent:CDTTManagerAgent";
	}
	
	// Constructor with sName and sConfiguration
	public CDTTManagerAgent(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDTTManagerAgent";
	}
	
	public CDTTManagerAgent() {
		// TODO Auto-generated constructor stub
	}
	//-----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CDTTManagerAgent(sAName, sAConfiguration);
	}
	

	
	//-----------------------------------------------------------------------------
	//
	// CDTTManager class specific methods
	//
	//-----------------------------------------------------------------------------

	// D: Function that specifies that a certain discourse agent will be used
	public void Use(String sDAType, String sDAName,
							   FRegisterAgent fRegisterAgent, String sDAConfiguration) {
		// fill in a structure with discourse agent information
		TDiscourseAgentInfo dai = new TDiscourseAgentInfo();
		dai.sDAName = sDAName;
		dai.sDAType = sDAType;
		dai.fRegisterAgent = fRegisterAgent;
		dai.sDAConfiguration = sDAConfiguration;

		// add it to the array holding discourse agent info
		vdaiDAInfo.add(dai);
	}
	// registers all the dialog task(developer specified) agents
	public void CreateDialogTaskAgentome(){
		Log.d(DTTMANAGER_STREAM_TAG,"Registering  dialog task agent types ...");
		// users define agents in UserDefinition class 
		UserDefinition.Register_All_AgentType();
	}
	// RootAgentType RootAgentName and GroundingModelSpec are set inside this method
	public void CreateDialogTaskTree(){
		Log.d(DTTMANAGER_STREAM_TAG, "Creating Dialog Task Tree ...");
		String RootAgentType = "CFlightRoot";
		String RootAgentName = "FlightRoot";
		String GroundingModelSpec = "";
		pdaDialogTaskRoot = (CDialogAgent)
				CRegistry.AgentsRegistry.CreateAgent(RootAgentType,RootAgentName);
	    pdaDialogTaskRoot.SetParent(null);
        pdaDialogTaskRoot.CreateGroundingModel(GroundingModelSpec);
		pdaDialogTaskRoot.Initialize();
	    pdaDialogTaskRoot.Register();
	}
	// D: This function creates the dialog tree. It first creates the dialog task
	//	    agentome and tree, and then it creates the discourse agentome and 
	//	    mounts the discourse agents
	public void CreateDialogTree() {
		Log.d(DTTMANAGER_STREAM_TAG, "Starting Dialog Tree Creation Phase ...");

		// register all the agents for the dialog task
		CreateDialogTaskAgentome();
		// create the actual task tree
		CreateDialogTaskTree();
		
		// register all the generic, task-independent discourse agents
		for(int i=0; i<vdaiDAInfo.size(); i++){
			String tmpAgentName = vdaiDAInfo.get(i).sDAName;
			vdaiDAInfo.get(i).fRegisterAgent.DeclareAgentInterface(tmpAgentName);
		}
			

		// mount all the discourse agents that were specified to be used
		for(int i=0; i<vdaiDAInfo.size(); i++)
			MountAgent(pdaDialogTaskRoot, vdaiDAInfo.get(i).sDAType, 
					vdaiDAInfo.get(i).sDAName, vdaiDAInfo.get(i).sDAConfiguration, 
					TMountingMethod.mmAsLastChild);

		Log.d(DTTMANAGER_STREAM_TAG, "Dialog Tree Creation Phase completed successfully.");
	}
	// OverLoad: Mount a subtree somewhere in the dialog task tree(with default ID="")
	public CDialogAgent MountAgent(CDialogAgent pWhere, String sAgentType, 
									           String sAgentName, String sAgentConfiguration, 
											   TMountingMethod mmHow) {
		String sDynamicAgentID = "";
		// create the agent
		CDialogAgent pNewAgent = (CDialogAgent )
				CRegistry.AgentsRegistry.CreateAgent(sAgentType, sAgentName, sAgentConfiguration);
		// place it in the tree
		MountAgent(pWhere, pNewAgent, mmHow, sDynamicAgentID);
		// and the call its Initialize method
		pNewAgent.Initialize();
		// finally, return it
		return pNewAgent;
	}
	// D: Mount a subtree somewhere in the dialog task tree
	public CDialogAgent MountAgent(CDialogAgent pWhere, String sAgentType, 
									           String sAgentName, String sAgentConfiguration, 
											   TMountingMethod mmHow, String sDynamicAgentID) {
		// create the agent
		CDialogAgent pNewAgent = (CDialogAgent )
				CRegistry.AgentsRegistry.CreateAgent(sAgentType, sAgentName, sAgentConfiguration);
		// place it in the tree
		MountAgent(pWhere, pNewAgent, mmHow, sDynamicAgentID);
		// and the call its Initialize method
		pNewAgent.Initialize();
		// finally, return it
		return pNewAgent;
	}
	// D: Mount a subtree somewhere in the dialog task tree
	public void MountAgent(CDialogAgent pdaWhere, CDialogAgent pdaWho, 
	                                  TMountingMethod mmHow, String sDynamicAgentID) {
		
		// log the mounting operation
		Log.d(DTTMANAGER_STREAM_TAG, "Mounting "+pdaWho.GetName()+
				" as "+mmHow.toString()+
				" of "+pdaWhere.GetName()+" .");

		// perform the mounting: use the AddSubAgent routine and then register
		//  the added agent; analyze depending on which mounting method is used
		switch(mmHow) {
			case mmAsLastChild:
				if(pdaWhere==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsLastChild);
				break;
			case mmAsFirstChild:
				if(pdaWhere==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.AddSubAgent(pdaWho, null, TAddSubAgentMethod.asamAsFirstChild);
				break;
			case mmAsLeftSibling:
				if(pdaWhere==null || pdaWhere.GetParent()==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.GetParent().AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsLeftSibling);
				break;
			case mmAsRightSibling:
				if(pdaWhere==null || pdaWhere.GetParent()==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.GetParent().AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsRightSibling);
				break;
		}
	}
	
	// OverLoad: Mount a subtree somewhere in the dialog task tree(with default ID="")
	public void MountAgent(CDialogAgent pdaWhere, CDialogAgent pdaWho, 
	                                  TMountingMethod mmHow) {
		String sDynamicAgentID = "";
		// log the mounting operation
		Log.d(DTTMANAGER_STREAM_TAG, "Mounting "+pdaWho.GetName()+
				" as "+mmHow.toString()+
				" of "+pdaWhere.GetName()+" .");

		// perform the mounting: use the AddSubAgent routine and then register
		//  the added agent; analyze depending on which mounting method is used
		switch(mmHow) {
			case mmAsLastChild:
				if(pdaWhere==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsLastChild);
				break;
			case mmAsFirstChild:
				if(pdaWhere==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.AddSubAgent(pdaWho, null, TAddSubAgentMethod.asamAsFirstChild);
				break;
			case mmAsLeftSibling:
				if(pdaWhere==null || pdaWhere.GetParent()==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.GetParent().AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsLeftSibling);
				break;
			case mmAsRightSibling:
				if(pdaWhere==null || pdaWhere.GetParent()==null) 
					Log.e(DTTMANAGER_STREAM_TAG,"Cannot mount " + pdaWho.GetName() + " to NULL.");
				pdaWhere.GetParent().AddSubAgent(pdaWho, pdaWhere, TAddSubAgentMethod.asamAsRightSibling);
				break;
		}
	}
	// D: This function destroys the dialog tree. Basically it calls delete on the 
	//  root agent
	public void DestroyDialogTree() {
		Log.d(Const.CDTTMANAGER_STREAM, "Starting Dialog Tree Destruction Phase ...");
	  if(pdaDialogTaskRoot != null) {
	      // call the OnDestruction method
	      pdaDialogTaskRoot.OnDestruction();
	      // then delete
	      pdaDialogTaskRoot = null;
	  }
		Log.d(Const.CDTTMANAGER_STREAM, "Dialog Tree Destruction Phase completed successfully.");
	}
	public CDialogAgent GetDialogTaskTreeRoot() {
		// TODO Auto-generated method stub
		return pdaDialogTaskRoot;
	}

	// D: returns true if sAncestorAgent is an ancestor of sAgent or if they are
	//  equal
	public boolean IsAncestorOrEqualOf(String sAncestorAgentPath, String sAgentPath) {
		try{
			return sAgentPath.substring(0, sAncestorAgentPath.length()).
					equals(sAncestorAgentPath);
		}catch(Exception e){
			return false;
		}
		
	}
	
}
