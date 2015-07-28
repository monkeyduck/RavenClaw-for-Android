package dmcore.agents.mytypedef;

import java.util.ArrayList;

import dmcore.agents.dialogagents.CDialogAgent;

public class TConceptExpectation {
	public CDialogAgent pDialogAgent;		// which agent expects this
	public String sConceptName;			// the name of the concept that we will
									//  bind to
	public ArrayList<String> vsOtherConceptNames = 
			new ArrayList<String>();
	                                // vector of other concept names 
	                                //  that are implicitly requested by 
	                                //  this agent (sometimes we do a 
	                                //  request on a full structure for only
	                                //  to get some of the members)
	public String sGrammarExpectation;		// the grammar slot that is expected
	public TBindMethod bmBindMethod;	    // indicates the binding method to be 
                                    //  used
	public String sExplicitValue;			// the value bound to the concept in case
									//  the grammar slot appears in the parse
									//  will be bound to the concept, o/w the
									//  value extracted from the grammar will
									//  be bound
	public String sBindingFilterName;      // the name of the registered custom 
                                    //  binding filter
	public boolean bDisabled;					// indicates that this expectation is 
									//  disabled at this moment
	public String sReasonDisabled;			// indicates why the expectation is 
									//  disabled
	public String sExpectationType;        // indicates the type of this expectation
                                    //  (i.e. ! or @ or * ... )

}
