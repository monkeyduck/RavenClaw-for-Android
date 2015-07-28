package dmcore.agents.mytypedef;

import dmcore.agents.coreagents.CAgent;

public interface AgentFactory {
	public CAgent AgentFactory(String sAName, String sAConfiguration);
}
