package dmcore.grounding.groundingmodel;

import dmcore.agents.dialogagents.CDialogAgent;


public class CGMRequestAgent extends CGroundingModel{
	
	//---------------------------------------------------------------------
	// protected class members
	//---------------------------------------------------------------------
	protected CDialogAgent pRequestAgent;		// pointer to the dialogue agent 
												//  it is handling
	
	// D: Set the agent handled
	@Override
	public void SetRequestAgent(CDialogAgent pARequestAgent) {
	    pRequestAgent = pARequestAgent;
	}

	@Override
	public void computeFullState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeBeliefState() {
		// TODO Auto-generated method stub
		
	}
}
