package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CDialogAgency;

public class CLogout extends CDialogAgency {
	public CLogout(String sAName){
		super(sAName);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CLogout";
        this.sType = sAType;
		}
	public CLogout(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CLogout";
        this.sType = sAType;
	}
	public CLogout() {
		// TODO Auto-generated constructor stub
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CLogout(sAName, sAConfiguration);
	}
	public boolean IsAMainTopic() {
		return true;
	}
	public void CreateConcepts() {
		//SUBAGENT("RequestSatisfied", "CRequestSatisfied", "request_default"); 
        SUBAGENT("InformLogout", "CInformLogout", "");
	}
	public String EstablishContextPrompt(){
		return "establish_context logout";
	}
	public boolean SuccessCriteriaSatisfied() {
		return getAgentFromPath("InformLogout").HasCompleted();
	}
}
