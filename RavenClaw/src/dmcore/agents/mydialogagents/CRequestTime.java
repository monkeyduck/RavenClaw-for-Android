package dmcore.agents.mydialogagents;

import utils.Utils;
import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMARequest;
import dmcore.agents.mytypedef.AgentFactory;

public class CRequestTime extends CMARequest implements AgentFactory{
	
	public CRequestTime(String sAName){
		super(sAName);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestTime";
	}
	public CRequestTime(String sAName,String sAConfiguration){
		super(sAName,sAConfiguration);
		this.sType = "CAgent:CDialogAgent:CMARequest:CRequestTime";
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CRequestTime(sAName, sAConfiguration);
	}
	public boolean PreconditionsSatisfied() {
		return !AVAILABLE("room_query.date_time.time.start_time") &&
				 !AVAILABLE("room_query.date_time.time.end_time") &&
				 !AVAILABLE("room_query.date_time.time.time_duration");
	}
	//??????????????????????????????????????????????????????
	public String RequestedConceptName() {
	    return(Utils.ReplaceSubString("room_query.date_time;"+
	    		"room_query.date_time.time.start_time;"
	    		+"room_query.date_time.time.end_time","#", GetDynamicAgentID()));
	}
	public String Prompt() {
		return "request what_time";
	}
	public String GrammarMapping() {
		return "@(/RoomLine/Task;/RoomLine/AnythingElse)[NeedRoom.date_time]>:datetime,"+
                    "@(/RoomLine/Task;/RoomLine/AnythingElse)[DateTimeSpec.date_time]>:datetime";
	}
    public boolean SuccessCriteriaSatisfied() {
		return  AVAILABLE("room_query.date_time.time.start_time") ||
		        AVAILABLE("room_query.date_time.time.end_time");
		
    }
}
