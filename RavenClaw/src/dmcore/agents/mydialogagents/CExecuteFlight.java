package dmcore.agents.mydialogagents;

import dmcore.agents.coreagents.CAgent;
import dmcore.agents.dialogagents.CMAExecute;
import dmcore.agents.mytypedef.AgentFactory;

public class CExecuteFlight extends CMAExecute implements AgentFactory{
	// default constructor
	public CExecuteFlight(){
		
	}
	public CExecuteFlight(String sAName, String sAConfiguration) {
		// TODO Auto-generated constructor stub
		super(sAName,sAConfiguration);
		String sAType = "CAgent:CDialogAgent:CDialogAgency:CExecuteFlight";
        this.sType = sAType;
	}
	public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new CExecuteFlight(sAName, sAConfiguration);
	}
	
	// D: Returns the execution call as a String.
	public String GetExecuteCall() {
		String sQuery = getConceptFromPath("/FlightRoot/Task/flight_query")
				.QueryToString();
		return sQuery;
	}
}
