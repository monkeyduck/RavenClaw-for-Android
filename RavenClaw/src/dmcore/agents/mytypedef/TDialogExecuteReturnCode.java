package dmcore.agents.mytypedef;

public enum TDialogExecuteReturnCode {
		dercContinueExecution,		// continue the execution
	   dercYieldFloor,				// gives the floor to the user
	   dercTakeFloor,				// takes the floor
	   dercWaitForEvent,			// waits for a real-world event
	   dercFinishDialog,			// terminate the dialog 
	   dercFinishDialogAndCloseSession,			// terminate the dialog 
									// and sends a close session message to the hub
	   dercRestartDialog            // restart the dialog
}
