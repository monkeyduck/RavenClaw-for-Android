package dmcore.concepts.mytypedef;

import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.concepts.CConcept;
import dmcore.concepts.CDateConcept;
import dmcore.concepts.CStringConcept;
import dmcore.concepts.CStructConcept;

public class CFlightQuery extends CStructConcept implements ConceptFactory{

	public CFlightQuery(String sAName, TConceptSource csAConceptSource) {
		super(sAName, csAConceptSource);
	}
	public CFlightQuery(String sAName){
		super(sAName);
		this.csConceptSource = TConceptSource.csUser;
	}
	public CFlightQuery() {
	}
	@Override
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource) {
		return new CFlightQuery(sAName,csAConceptSource);
	}
	
	public void CreateStructure(){
		ConceptFactory ConceptType;
		ConceptType = new CDateConcept();
		CUSTOM_ITEM("startDate", ConceptType );
		ConceptType = new CStringConcept();
        CUSTOM_ITEM("startLoc", ConceptType );
        //CUSTOM_ITEM("airline",ConceptType);
        CUSTOM_ITEM("endLoc",ConceptType);
        
	}
	
	// Prompt to User
	public String PromptToString(){
		String sDate = ItemMap.get("startDate").TopHypToString();
		String sStartLoc = ItemMap.get("startLoc").TopHypToString();
		String sEndLoc = ItemMap.get("endLoc").TopHypToString();
		return sDate+"´Ó"+sStartLoc+"·ÉÍù"+sEndLoc+"µÄº½°à";
	}
	
	// Prompt to database to query
	public String QueryToString(){
		String sDate = ItemMap.get("startDate").TopHypToString();
		String sStartLoc = ItemMap.get("startLoc").TopHypToString();
		String sEndLoc = ItemMap.get("endLoc").TopHypToString();
		return "startDate:"+sDate+",startLoc:"+sStartLoc+",endLoc:"+sEndLoc;
	}
	

}
