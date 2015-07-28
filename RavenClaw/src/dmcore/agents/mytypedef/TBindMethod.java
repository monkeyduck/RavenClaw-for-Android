package dmcore.agents.mytypedef;

public enum TBindMethod {
	// D: definition of things that get bound to a concept
	 bmSlotValue,       // bind the value of the slot 
	 bmExplicitValue,   // bind an explicit value specified in the 
	                  	//  expectation 
	 bmBindingFilter    // bind the result of applying a custom 
	                  	//  binding filter                              

}
