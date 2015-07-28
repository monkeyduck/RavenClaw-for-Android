package dmcore.agents.coreagents;

import java.util.HashMap;

import dmcore.agents.mytypedef.AgentFactory;

import android.util.Log;

interface FRegisterAgent{
	public void DeclareAgentInterface(String sAName);
}

public class CRegistry {
	// D: the AgentsRegistry object
	public static CRegistry AgentsRegistry = new CRegistry();
	// ----------------------------------------------------------------------------
	// private members
	// ---------------------------------------------------------------------------

	// hash holding agent name -> agent mapping
	private HashMap<String, CAgent> AgentsHash = new HashMap<String, CAgent>();
	// hash holding agent type -> agent mapping
	private HashMap<String, AgentFactory> AgentsTypeHash = new HashMap<String, AgentFactory>();

	// -----------------------------------------------------------------------------
	// Constructors and Destructors
	// -----------------------------------------------------------------------------

	// D: Constructor
	public CRegistry() {
		// nothing here
		Log.d("TEST","Constructor completed");
	}

	// Log Tag
	private static final String REGISTRY_STREAM_TAG = "REGISTRY";

	// D: Initializes the registry, empties everything
	public void Clear() {
		Log.d(REGISTRY_STREAM_TAG, "Clearing up the registry ...");
		// clear the hashes
		AgentsHash.clear();
		AgentsTypeHash.clear();
		Log.d(REGISTRY_STREAM_TAG,
				"Clearing up remaining registered agents completed.");
	}

	// -----------------------------------------------------------------------------
	//
	// Registry specific functions for registered agent names
	//
	// -----------------------------------------------------------------------------

	// D: return a pointer to an agent, given the agent's name. Returns NULL if
	//  the agent is not found
	public CAgent getAgentGivenName(String sAgentName) {
		if(!AgentsHash.containsKey(sAgentName)) {
			// if the agent is not found, return NULL
			return null;
		} else {
			// otherwise, return the pointer to the agent
			return AgentsHash.get(sAgentName);
		}
	}
	// D: return true if the agent is already registered
	public boolean IsRegisteredAgent(String sAgentName) {
		return AgentsHash.containsKey(sAgentName);
	}

	// D: register an agent into the registry.
	public void RegisterAgent(String sAgentName, CAgent pAgent) {
		// check that there's no agent already registered under the same name
		if (IsRegisteredAgent(sAgentName)) {
			Log.e(REGISTRY_STREAM_TAG,
					"An agent already registered under the same name ("
							+ sAgentName + ") was found.");
		}
		// register the agent
		AgentsHash.put(sAgentName, pAgent);
		// and log that
		Log.d(REGISTRY_STREAM_TAG, "Agent " + sAgentName
				+ " registered successfully.");
	}

	// D: unregister an agent
	public void UnRegisterAgent(String sAgentName) {
		if (AgentsHash.remove(sAgentName) == null) {
			Log.e(REGISTRY_STREAM_TAG, "Could not find agent " + sAgentName
					+ " to unregister.");
		}
		// and log that
		Log.d(REGISTRY_STREAM_TAG, "Agent " + sAgentName
				+ " unregistered successfully.");
	}
	
	// D: return a pointer to an agent, given the agent's name. Returns NULL if
	//  the agent is not found
	CAgent getAgentPointer(String sAgentName) {
		if(!AgentsHash.containsKey(sAgentName)){
			// if the agent is not found, return NULL
			return null;
		} else {
			// otherwise, return the pointer to the agent
			return AgentsHash.get(sAgentName);
		}
	}

	// -----------------------------------------------------------------------------
	//
	// Registry specific functions for registered agent types
	//
	// -----------------------------------------------------------------------------

	// D: return true if the agent type is already registered
	public boolean IsRegisteredAgentType(String sAgentTypeName) {
		return AgentsTypeHash.containsKey(sAgentTypeName);
	}

	// D: register an agent type into the registry.
	public void RegisterAgentType(String sAgentTypeName, AgentFactory cCreateAgent) {
		// check that there's no agent already registered under the same name
		if (IsRegisteredAgentType(sAgentTypeName)) {
			Log.e(REGISTRY_STREAM_TAG,
					"An agent type already registered under the same name ("
							+ sAgentTypeName + ") was found.");
		}

		// register the agent type
		AgentsTypeHash.put(sAgentTypeName, cCreateAgent);

		// and log that
		Log.d(REGISTRY_STREAM_TAG, "Agent type " + sAgentTypeName
				+ " registered successfully.");
	}

	// D: unregister an agent type
	public void UnRegisterAgentType(String sAgentTypeName) {
		if (AgentsTypeHash.remove(sAgentTypeName) == null) {
			Log.e(REGISTRY_STREAM_TAG, "Could not find agent type"
					+ sAgentTypeName + " to unregister.");
		}

		// and log that
		Log.d(REGISTRY_STREAM_TAG, "Agent type " + sAgentTypeName
				+ " unregistered successfully.");
	}
	
	// D: create a new agent from a given agent type
	public CAgent CreateAgent(String sAgentTypeName, String sAgentName) {
		
		// default AgentConfiguration value
		String sAgentConfiguration="";
		// test if the agent type is in the registry
		if(!AgentsTypeHash.containsKey(sAgentTypeName)) {
			// if the agent type is not in the registry, we're in bad shape
			Log.e(REGISTRY_STREAM_TAG,"Could not create agent of type " + sAgentTypeName + 
					   ". Type not found in the registry.");
			return null;
		} else {
			// if we found the agent, call the create method.
			// AgentFactory dynamically calls the create method.
			CAgent pNewAgent = AgentsTypeHash.get(sAgentTypeName).AgentFactory(sAgentName, sAgentConfiguration);
			if(pNewAgent != null) {
				// call the create function for the agent
				pNewAgent.Create();
				Log.d(REGISTRY_STREAM_TAG, "Agent "+sAgentName+" created successfully.");
				// and return a pointer to it
				return pNewAgent;
			} else {
				// if creation failed, trigger a fatal error
				Log.e(REGISTRY_STREAM_TAG,"Error creating agent of type " + sAgentTypeName + ".");
				return null;
			}
		}
	}
	// OverLoad: create a new agent from a given agent type,name and configuration
	public CAgent CreateAgent(String sAgentTypeName, String sAgentName,String sAgentConfiguration) {
		
		// test if the agent type is in the registry
		if(!AgentsTypeHash.containsKey(sAgentTypeName)) {
			// if the agent type is not in the registry, we're in bad shape
			Log.e(REGISTRY_STREAM_TAG,"Could not create agent of type " + sAgentTypeName + 
					   ". Type not found in the registry.");
			return null;
		} else {
			// if we found the agent, call the create method.
			// AgentFactory dynamically calls the create method.
			CAgent pNewAgent = AgentsTypeHash.get(sAgentTypeName).AgentFactory(sAgentName, sAgentConfiguration);
			if(pNewAgent != null) {
				// call the create function for the agent
				pNewAgent.Create();
				Log.d(REGISTRY_STREAM_TAG, "Agent "+sAgentName+" created successfully.");
				// and return a pointer to it
				return pNewAgent;
			} else {
				// if creation failed, trigger a fatal error
				Log.e(REGISTRY_STREAM_TAG,"Error creating agent of type " + sAgentTypeName + ".");
					return null;
				}
			}
		}
}
