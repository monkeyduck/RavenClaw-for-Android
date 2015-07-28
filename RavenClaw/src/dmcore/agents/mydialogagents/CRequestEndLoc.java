package dmcore.agents.mydialogagents;

import utils.Utils;
import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMARequest;
import dmcore.agents.mytypedef.AgentFactory;

public class CRequestEndLoc extends CMARequest implements AgentFactory{
	public CRequestEndLoc(String sAName){
		super(sAName);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestEndLoc";
	}
	public CRequestEndLoc(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestEndLoc";
	}
	public CRequestEndLoc() {
		// TODO Auto-generated constructor stub
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CRequestEndLoc(sAName, sAConfiguration);
	}
	public boolean PreconditionsSatisfied() {
		return !IS_TRUE("flight_query.endLoc");
	}
	//??????????????????????????????????????????????????????
	public String RequestedConceptName() {
	    return(Utils.ReplaceSubString("flight_query.endLoc","#", GetDynamicAgentID()));
	}
	public String Prompt() {
		return "请说目的地。";
	}
	public String GrammarMapping() {
		return " [flight_query.endLoc]";
	}
    public boolean SuccessCriteriaSatisfied() {
		return IS_TRUE("flight_query.endLoc");
    }
}
