package dmcore.grounding.groundingaction;

import java.util.HashMap;

import utils.Utils;

public class CGroundingAction {
	protected HashMap<String, String> s2sConfiguration = new HashMap<String, String>();
	// Constructor with configuration
	public CGroundingAction(String sNewConfiguration) {
		SetConfiguration(sNewConfiguration);
	}

	// Sets the configuration string
	//
	public void SetConfiguration(String sNewConfiguration) {
	    // extract the configuration hash
		s2sConfiguration = Utils.StringToS2SHash(sNewConfiguration);
	}
}
