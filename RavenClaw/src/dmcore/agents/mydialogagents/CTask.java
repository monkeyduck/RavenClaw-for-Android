package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CDialogAgency;
import dmcore.agents.mytypedef.AgentFactory;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.concepts.CBoolConcept;
import dmcore.concepts.mytypedef.CFlightQuery;

public class CTask extends CDialogAgency implements AgentFactory{


	public CTask(String sAName){
		super(sAName);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CTask";
        this.sType = sAType;
		}
	public CTask(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CTask";
        this.sType = sAType;
	}
	public CTask() {
		// TODO Auto-generated constructor stub
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CTask(sAName, sAConfiguration);
	}
	public boolean IsAMainTopic() {
		return true;
	}
	public void CreateConcepts() {
		String GroundingModelSpec = "none, date_time.startDate=expl,"+
				"Loc.startLoc=expl,Loc.endLoc=expl";
		ConceptFactory ctConceptType;
		ctConceptType = new CFlightQuery();
		USER_CONCEPT("flight_query", ctConceptType, GroundingModelSpec);
	}
        
	public void CreateSubAgents() {
		SUBAGENT("RequestDate", "CRequestDate", "");
		SUBAGENT("RequestStartLoc", "CRequestStartLoc", "");
		SUBAGENT("RequestEndLoc", "CRequestEndLoc", "");
		SUBAGENT("InformResults","CInformResults","");
		SUBAGENT("ExecuteFlight","CExecuteFlight","");
		}
	public boolean SuccessCriteriaSatisfied() {
		return getAgentFromPath("RequestDate").HasCompleted() && 
                getAgentFromPath("RequestStartLoc").HasCompleted() &&
                getAgentFromPath("RequestEndLoc").HasCompleted()&&
                getAgentFromPath("InformResults").HasCompleted()&&
                getAgentFromPath("ExecuteFlight").HasCompleted();
	}    
}


