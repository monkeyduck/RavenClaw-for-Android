package dmcore.agents.coreagents;

import java.util.HashMap;

import android.util.Log;
import utils.Const;
import utils.Utils;
import dmcore.agents.coreagents.CRegistry;
import dmcore.agents.mytypedef.AgentFactory;

public class CAgent implements AgentFactory {
	
	
	//---------------------------------------------------------------------
	// Reference to the Registry Object
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	// Name and Type class members
	//---------------------------------------------------------------------
	//
	protected String sName;		// name of agent
	protected String sType;		// type of agent
	protected HashMap<String,String> s2sConfiguration = 
			new HashMap<String,String>();		// hash of parameters

	//---------------------------------------------------------------------
	// Constructors and destructors
	//---------------------------------------------------------------------
	// default constructor
	public CAgent(){
		sName="";
		sType="";
	}
	public CAgent(String sAName){
		sName = sAName;
		SetConfiguration("");
	}
	// Constructor with sName and sAconfiguration
	public CAgent(String sAName,String sAConfiguration){
		sName=sAName;
		sType = "CAgent";
		SetConfiguration(sAConfiguration);
				
	}
	public CAgent(String sAName, String sAConfiguration, String sAType) {
		this.sName = sAName;
		this.sType = sAType;
		this.SetConfiguration(sAConfiguration);
	}

	//-----------------------------------------------------------------------------
	// Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		// this method should never end up being called (since CAgent is an 
		// abstract class) , therefore do a fatal error if this happens
		Log.e(Const.CREATE_STREAM_TAG,"CreateAgent method called on CAgent (abstract) class.");
		return null;
	}

	//---------------------------------------------------------------------
	// Methods for access to private and protected members
	//---------------------------------------------------------------------
	
	public String GetName(){
		return sName;
	}
	public String GetType(){
		return sType;
	}
	public void SetType(String sAType){
		this.sType = sAType;
	}
	// A: Parses a configuration string into a hash of parameters
	public void SetConfiguration(String sConfiguration) {
		// append to the current list of parameters
		HashMap<String, String> lval = new HashMap<String,String>();
		lval = Utils.StringToS2SHash(sConfiguration);
		s2sConfiguration.putAll(lval);
	}

	// D: appends to the configuration from a hash
	public void SetConfiguration(HashMap<String,String> s2sAConfiguration) {
	    // append to the current configuration
	    Utils.AppendToS2S(s2sConfiguration, s2sAConfiguration);
	}

	// Sets an individual configuration parameter
	public void SetParameter(String sParam, String sValue){
		s2sConfiguration.put(sParam, sValue);
	}

	// Tests if a parameter exists in the configuration
	public boolean HasParameter(String sParam){
		return s2sConfiguration.containsKey(sParam);
	}

	// Gets the value for a given parameter
	public String GetParameterValue(String sParam){
		if (s2sConfiguration.containsKey(sParam))
			return s2sConfiguration.get(sParam);
		else
			return "";
	}

	//---------------------------------------------------------------------
	// CAgent specific methods
	//---------------------------------------------------------------------
	
	// registering and unregistering the agent
	//
	 public void Register(){
		 CRegistry.AgentsRegistry.RegisterAgent(sName, this);
	 }
	 public void UnRegister(){
		 CRegistry.AgentsRegistry.UnRegisterAgent(sName);
	 }

	// This method is called immediately after an agent is constructed 
	// by the AgentsRegistry.CreateAgent function. 
	// 
	 public void Create(){}

	// This method is called to initialize an agent (usually after it's 
	// mounted in the dialog task tree)
	//
	 public void Initialize(){}

	// resets the agent (brings it to the same state as after construction
	// and Initialize
	//
	 public void Reset(){}
	
}
