/*package dmcore.concepts.mytypedef;

import java.util.Calendar;

import dmcore.agents.mytypedef.ConceptFactory;
import dmcore.agents.mytypedef.TConceptSource;
import dmcore.agents.mytypedef.TConveyance;
import dmcore.concepts.CConcept;

public class CDateTimeConcept extends CConcept implements ConceptFactory{
	// private member
	private Calendar DateCalendar;
	
	// Constructor 
	public CDateTimeConcept(String sAName){
		super(sAName);
		this.csConceptSource = TConceptSource.csUser;
	}

	public CDateTimeConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated constructor stub
		super(sAName,csAConceptSource);
	}

	public CDateTimeConcept() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource) {
		// TODO Auto-generated method stub
		return new CDateTimeConcept(sAName,csAConceptSource);
	}
	
	

}
*/