package user.definition;

import dmcore.agents.coreagents.CRegistry;
import dmcore.agents.mydialogagents.CExecuteFlight;
import dmcore.agents.mydialogagents.CFlightRoot;
import dmcore.agents.mydialogagents.CInformLogout;
import dmcore.agents.mydialogagents.CInformResults;
import dmcore.agents.mydialogagents.CLogout;
import dmcore.agents.mydialogagents.CRequestDate;
import dmcore.agents.mydialogagents.CRequestEndLoc;
import dmcore.agents.mydialogagents.CRequestStartLoc;
import dmcore.agents.mydialogagents.CTask;
import dmcore.agents.mydialogagents.CWelcome;
import dmcore.agents.mytypedef.AgentFactory;

public class UserDefinition {
	public static void Register_All_AgentType(){
		AgentFactory aftAgentFactoryType;
		aftAgentFactoryType = new CFlightRoot();
		RegisterAgent("CFlightRoot",aftAgentFactoryType);
		aftAgentFactoryType = new CWelcome();
		RegisterAgent("CWelcome",aftAgentFactoryType);
		aftAgentFactoryType = new CTask();
		RegisterAgent("CTask",aftAgentFactoryType);
		aftAgentFactoryType = new CRequestDate();
		RegisterAgent("CRequestDate",aftAgentFactoryType);
		aftAgentFactoryType = new CRequestStartLoc();
		RegisterAgent("CRequestStartLoc",aftAgentFactoryType);
		aftAgentFactoryType = new CRequestEndLoc();
		RegisterAgent("CRequestEndLoc",aftAgentFactoryType);
		aftAgentFactoryType = new CInformResults();
		RegisterAgent("CInformResults",aftAgentFactoryType);
		aftAgentFactoryType = new CExecuteFlight();
		RegisterAgent("CExecuteFlight",aftAgentFactoryType);
		aftAgentFactoryType = new CInformLogout();
		RegisterAgent("CInformLogout",aftAgentFactoryType);
		aftAgentFactoryType = new CLogout();
		RegisterAgent("CLogout",aftAgentFactoryType);
		
		
		
		
	}
	public static void RegisterAgent(String sTypeName,AgentFactory agentfactory){
		// get Class name and substring(6) to delete string"class "
		CRegistry.AgentsRegistry.RegisterAgentType(sTypeName,agentfactory);
	}

	public static void DialogTaskOnBeginSession(){
		DeclareLibraryAgent();
		DeclareBindingFilter();
		
	}
	public static void DeclareLibraryAgent(){
		
	}
	public static void DeclareBindingFilter(){
		
	}
}
