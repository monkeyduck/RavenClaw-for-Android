package dmcore.agents.mytypedef;

import dmcore.concepts.CConcept;

public interface ConceptFactory {
	public CConcept CreateConcept(String sAName, TConceptSource csAConceptSource);
}
