package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CDialogAgency;
import dmcore.agents.mytypedef.AgentFactory;
import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.concepts.CBoolConcept;

public class CFlightRoot extends CDialogAgency implements AgentFactory{
		public CFlightRoot(String sAName){
			super(sAName);
			String sAType = "CAgent:CDialogAgent:CDialogAgency:CFlightRoot";
	        this.sType = sAType;
			}
		public CFlightRoot(String sAName, String sAConfiguration) {
			// TODO Auto-generated constructor stub
			super(sAName,sAConfiguration);
			String sAType = "CAgent:CDialogAgent:CDialogAgency:CFlightRoot";
	        this.sType = sAType;
		}
		public CFlightRoot() {
			// TODO Auto-generated constructor stub
		}
		public CAgent AgentFactory(String sAName, String sAConfiguration) {
			return new CFlightRoot(sAName, sAConfiguration);
		}
		public boolean IsAMainTopic() {
			return true;
		}
		public void CreateConcepts() {
			ConceptFactory ctConceptType = new CBoolConcept();
			USER_CONCEPT("satisfied",ctConceptType,"expl");
	        //SYSTEM_CONCEPT("reserve_room", CRoomResult);
		}
	        
		public void CreateSubAgents() {
			//SUBAGENT("ResetDateTime", "CResetDateTime", "");
			SUBAGENT("Welcome", "CWelcome", "");
			SUBAGENT("Task", "CTask", "");
	        //SUBAGENT("AnythingElse", "CAnythingElse", "");
			//SUBAGENT("InformCancelPrevReservation", "CInformCancelPrevReservation", "");
			SUBAGENT("Logout", "CLogout", "");
			}
		public boolean SuccessCriteriaSatisfied() {
			return getAgentFromPath("/FlightRoot/Logout").HasCompleted();
		}    
}
