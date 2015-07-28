package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMAInform;
import dmcore.agents.mytypedef.AgentFactory;

public class CInformLogout extends CMAInform implements AgentFactory{
	// default constructor
	public CInformLogout(){
		
	}
	public CInformLogout(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CInformLogout";
        this.sType = sAType;
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CInformLogout(sAName, sAConfiguration);
	}
	@Override
	public String Prompt(){
		return "查询结束，感谢您的使用。";
	}
}
