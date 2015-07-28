package dmcore.agents.mydialogagents;

import utils.Utils;
import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMARequest;
import dmcore.agents.mytypedef.AgentFactory;

public class CRequestDate extends CMARequest implements AgentFactory{
	
	public CRequestDate(String sAName){
		super(sAName);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestDate";
	}
	public CRequestDate(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestDate";
	}
	public CRequestDate() {
		// TODO Auto-generated constructor stub
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CRequestDate(sAName, sAConfiguration);
	}
	public boolean PreconditionsSatisfied() {
		return !IS_TRUE("flight_query.startDate");
	}
	//??????????????????????????????????????????????????????
	public String RequestedConceptName() {
	    return(Utils.ReplaceSubString("flight_query.startDate", 
	    		"#", GetDynamicAgentID()));
	}
	public String Prompt() {
		return "请问您要查询什么时间的航班？";
	}
	public String GrammarMapping() {
		return " [flight_query.startDate]";
	}
    public boolean SuccessCriteriaSatisfied() {
		return IS_TRUE("flight_query.startDate");
    }
}
