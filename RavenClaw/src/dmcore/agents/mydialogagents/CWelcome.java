package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMAInform;
import dmcore.agents.mytypedef.AgentFactory;

public class CWelcome extends CMAInform implements AgentFactory{
	// default constructor
	public CWelcome(){
		
	}
	public CWelcome(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CWelcome";
        this.sType = sAType;
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CWelcome(sAName, sAConfiguration);
	}
	@Override
	public String Prompt(){
		return "   您好，欢迎使用航班查询系统。";
	}
	
}
