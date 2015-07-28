package dmcore.agents.mydialogagents;

import utils.Utils;
import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMARequest;
import dmcore.agents.mytypedef.AgentFactory;

public class CRequestStartLoc extends CMARequest implements AgentFactory{
	public CRequestStartLoc(String sAName){
		super(sAName);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestStartLoc";
	}
	public CRequestStartLoc(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestStartLoc";
	}
	public CRequestStartLoc() {
		// TODO Auto-generated constructor stub
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CRequestStartLoc(sAName, sAConfiguration);
	}
	public boolean PreconditionsSatisfied() {
		return !IS_TRUE("flight_query.startLoc");
	}
	//??????????????????????????????????????????????????????
	public String RequestedConceptName() {
	    return(Utils.ReplaceSubString("flight_query.startLoc", 
	    		"#", GetDynamicAgentID()));
	}
	public String Prompt() {
		return "请说起始地点。";
	}
	public String GrammarMapping() {
		return " [flight_query.startLoc]";
	}
    public boolean SuccessCriteriaSatisfied() {
		return IS_TRUE("flight_query.startLoc");
    }
}
