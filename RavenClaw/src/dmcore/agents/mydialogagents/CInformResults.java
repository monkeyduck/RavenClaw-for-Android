package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMAInform;
import dmcore.agents.mytypedef.AgentFactory;

public class CInformResults extends CMAInform implements AgentFactory {
	// default constructor
	public CInformResults(){
		
	}
	public CInformResults(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CInformResults";
        this.sType = sAType;
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CInformResults(sAName, sAConfiguration);
	}
	@Override
	public String Prompt(){
		String sResult = getConceptFromPath("/FlightRoot/Task/flight_query")
				.PromptToString();
		return "您查询的是"+sResult+"。系统正在查询，请稍后。";
	}
}
