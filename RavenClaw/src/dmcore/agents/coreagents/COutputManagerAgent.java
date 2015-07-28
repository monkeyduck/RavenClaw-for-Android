package dmcore.agents.coreagents;

import java.util.ArrayList;

import dmcore.outputs.COutput;
import dmcore.outputs.COutputHistory;

//-----------------------------------------------------------------------------
//A: Describes destination for a prompt (output device) sent to be outputted 
// with a key name, an external server call, and any parameters that modify 
// how the Output Manager should handle the output
//-----------------------------------------------------------------------------
class TOutputDevice {
	String sName;			// name of the output device
	String sServerCall;		// external server name (module.function for galaxy)
	int iParams;			// other parameters for the output device
}
public class COutputManagerAgent extends CAgent{
	//---------------------------------------------------------------------
	// Private members
	//---------------------------------------------------------------------
	//
	// the history of outputs 
	private COutputHistory ohHistory;			

	// list of registered output devices
	private ArrayList <TOutputDevice> vodOutputDevices;
	// index to the default output device
	private int iDefaultOutputDevice;

	// list of last outputs sent, about which we haven't been notified of 
    // success
	private ArrayList <COutput> vopRecentOutputs; 	
	
	// counter for generating output IDs
	private int iOutputCounter;

	// critical section object for enforcing concurrency control
	//?????????private CRITICAL_SECTION csCriticalSection;

    // the class of outputs to be used
	private String sOutputClass;
	//-----------------------------------------------------------------------------
	//
	// Methods for the COutputManagerAgent class
	//
	//-----------------------------------------------------------------------------

	//-----------------------------------------------------------------------------
	// AD: Constructors and destructors
	//-----------------------------------------------------------------------------
	// AD: constructor
	public COutputManagerAgent(String sAName,String sAConfiguration, 
											 String sAType){ 
		super(sAName, sAConfiguration, sAType);

		// set an invalid value for the output device since we don't have 
		// any output devices registered yet
		iDefaultOutputDevice = 0;

		// initialize prompt ids
		iOutputCounter = 0;

		// create the critical section object
	    //?????InitializeCriticalSection(csCriticalSection);

	    // initialize the output class by default to frameoutput
	    sOutputClass = "FrameOutput";
	}

	//-----------------------------------------------------------------------------
	// D: Static function for dynamic agent creation
	//-----------------------------------------------------------------------------
	/*public CAgent AgentFactory(String sAName, String sAConfiguration) {
		return new COutputManagerAgent(sAName, sAConfiguration);
	}

	//-----------------------------------------------------------------------------
	// D: CAgent class overwritten methods
	//-----------------------------------------------------------------------------

	// D: the overwritten Reset method
	public void Reset() {
		// reinitialize everything
		ohHistory.Clear();
		iOutputCounter = 0;

		// delete the list of recent outputs
		for(int i=0; i < vopRecentOutputs.size(); i++) 
			delete vopRecentOutputs[i];
		vopRecentOutputs.clear();
	}

	//-----------------------------------------------------------------------------
	// D: Method for setting the output class
	//-----------------------------------------------------------------------------
	public void SetOutputClass(String sAOutputClass) {
	    sOutputClass = sAOutputClass;
	}

	//-----------------------------------------------------------------------------
	// A: Methods for registering output devices
	//-----------------------------------------------------------------------------

	// A: adds output device to list of registered output devices. Output devices
	//	    have to be registered before they are used
	public boolean RegisterOutputDevice(String sName, String sServerCall,
												   int iParams ) {
		// check if the device is not already registered
		for ( int i = 0; i < vodOutputDevices.size(); i++ )
			if ( vodOutputDevices[i].sName == sName ) {
				Log(OUTPUTMANAGER_STREAM, "Device %s already registered", 
					                      sName.c_str());
				return false;
			}

		// if not, add it to the list; start by constructing the appropriate 
		// structure
		TOutputDevice od;
		od.sName = sName;
		od.sServerCall = sServerCall;
		od.iParams = iParams;

		// finally, add it to the list
		vodOutputDevices.push_back(od);
		return true;
	}

	// A: Sets the default output device. Will invoke a fatal error if device 
//	    does not exist.
	public void SetDefaultOutputDevice(String sName) {
		// look for the device in the list of registered devices
		for ( int i = 0; i < vodOutputDevices.size(); i++ )
			if ( vodOutputDevices[i].sName == sName ) {
				iDefaultOutputDevice = i;
				return;
			}
		FatalError("Cannot set default output device to " + sName + ". Device "\
			       "not registered.");
	}

	// A: looks output device up in registered list; returns null if it's not found
	public TOutputDevice GetOutputDevice(String sName) {
		// if no name specified, return the default output device
		if ( sName == "" )
			return GetDefaultOutputDevice();
		// check through the the list of output devices
		for ( int i = 0; i < vodOutputDevices.size(); i++ )
			if ( sName == vodOutputDevices[i].sName )
				return &(vodOutputDevices[i]);
		return NULL;
	}

	// AD: returns pointer to default output device, used when no device was 
//	     specified in a prompt
	public TOutputDevice GetDefaultOutputDevice() {
		if((iDefaultOutputDevice >= 0) && 
		   (iDefaultOutputDevice < vodOutputDevices.size())) {
			return &(vodOutputDevices[iDefaultOutputDevice]);
		} else {
			return NULL;
		}
	}

	// D: returns the name of the default output device
	public String GetDefaultOutputDeviceName() {
		if((iDefaultOutputDevice >= 0) && 
		   (iDefaultOutputDevice < vodOutputDevices.size())) {
			return vodOutputDevices[iDefaultOutputDevice].sName;
		} else {
			return "";
		}
	}

	//-----------------------------------------------------------------------------
	// A: Output methods
	//-----------------------------------------------------------------------------

	// AD: Constructs the appropriate output for each prompt and sends it to device
	//	     specified in sPrompts.  Also sends out any enqueued prompts first.
	//	     syntax for sPrompts: {...prompt1...}{...prompt2...}...{...promptn...}
	//	     see documentation for internal details of prompts
	ArrayList<COutput> Output(CDialogAgent* pGeneratorAgent, 
									 String sPrompts, TFloorStatus fsFinalFloorStatus) {

		// check that we know which dialog agent sent ordered this output
		if ( pGeneratorAgent == NULL )
	        FatalError(FormatString("No generator agent specified for output "\
								    "(dump below).\n%s", sPrompts.c_str()));

	    // check that the prompts are correctly enclosed in braces {}
	    sPrompts = Trim(sPrompts);
	    if((sPrompts != "") && (sPrompts[0] != '{')) 
	        sPrompts = "{" + sPrompts + "}";

		// ArrayList of pointers to the generated output objects to return to the calling agent
		ArrayList <COutput *> voOutputs;

		// while there are still prompts, process them and send them out
		while ( sPrompts != "" )  {

			// get the first prompt from the queue
	    	int iFirstPromptLength = 
				FindClosingQuoteChar(sPrompts, 1, '{', '}');
		    String sFirstPrompt = sPrompts.substr(0, iFirstPromptLength);
		    sPrompts = sPrompts.substr(iFirstPromptLength, 
									   sPrompts.size() - iFirstPromptLength);

			// check that the prompt is not empty; if so, ignore it and continue
			// looping
			if(sFirstPrompt == "{}") continue;

			// Sets the post-prompt floor status
			TFloorStatus fsFloor = fsSystem;
			if (sPrompts == "")
				fsFloor = fsFinalFloorStatus;

	        // log the prompt
	        Log(OUTPUTMANAGER_STREAM, "Processing output prompt %d from %s. (dump "\
				"below)\n%s", iOutputCounter, pGeneratorAgent->GetName().c_str(),
				sFirstPrompt.c_str());

			// create the new output; if we are in a Galaxy configuration, 
			// it's a CFrameOutput; if in an OAA configuration, it's a 
			// CLFOutput
	        COutput *pOutput = NULL;

			#ifdef GALAXY
	        if(sOutputClass == "FrameOutput") 
			    pOutput = (COutput*)(new CFrameOutput);
	        else {
	            FatalError(FormatString("Output manager configured with an unknown "
	                "output class: %s", sOutputClass.c_str()));        
	        }
			#endif

			#ifdef OAA
	        if(sOutputClass == "LFOutput") 
			    COutput *pOutput = (COutput*)(new CLFOutput);
	        else {
	            FatalError(FormatString("Output manager configured with an unknown "
	                "output class: %s", sOutputClass.c_str()));        
	        }
			#endif

			if(!pOutput->Create(pGeneratorAgent->GetName(), 
								pStateManager->GetStateHistoryLength()-1,
								sFirstPrompt, fsFloor, iOutputCounter)) {
	            // if the output could not be created, deallocate it and ignore
	            delete pOutput;
	            continue;            
	        }

			// store a pointer to the created output to send back to the calling agent
			voOutputs.push_back(pOutput);

			if (pOutput->GetAct() == "repeat") {
				Repeat();
				continue;
			}

			// send the output
			String sOutputSpecification = output(pOutput);

			// log the activity, if it has successfully taken place
			String sTurnId = "User:???";
			if (pInteractionEventManager->GetLastInput())
				sTurnId = "User:" + pInteractionEventManager->GetLastInput()->GetStringProperty("[uttid]");
			if(sOutputSpecification != "") 
				Log(OUTPUTMANAGER_STREAM, 
	                "Processed output prompt %d (state %d) and sent it to %s"\
					" (dump below) [%s]\n%s", iOutputCounter, 
					pStateManager->GetStateHistoryLength()-1,
					pOutput->sOutputDeviceName.c_str(), sTurnId.c_str(),
					sOutputSpecification.c_str());		

			// and finally increase the output counter
			iOutputCounter++;
		}

		return voOutputs;
	}

	// AD: Constructs and sends prompt that will repeat last utterance
	public void Repeat() {

	    // guard for safe access using the critical section 
		EnterCriticalSection(&csCriticalSection);
		
	    int i;
		int uiChosen = (int)-1;

	    // check if there is something in history to repeat
	    if (ohHistory.GetSize() == 0) {
	        Warning("Output history is empty, there is nothing to be repeated.");
	        // leave the critical section 
	        LeaveCriticalSection(&csCriticalSection);
	        return;
	    }

		// if we have been notified of all outputs
		if ( ( vopRecentOutputs.size() == 0 ) && 
			 ( ohHistory[0]->GetConveyance() == cConveyed ) && 
			 !( ohHistory[0]->CheckFlag(":non-repeatable") ) )
	        // and the last one is repeatable, then choose that one
			uiChosen = 0;
		else {
			i = 0;
			if ( (vopRecentOutputs.size() == 0) && 
				 (ohHistory[0]->GetConveyance() != cConveyed) )
				i = 1;

	        // now go through history, most recent first, and decide which to repeat
			for ( ; i < ohHistory.GetSize(); i++ )	{
				if ( !(ohHistory[i]->CheckFlag(":non-repeatable") )) {
					uiChosen = i;
					break;
				}
			}
		}

	    // if nothing got chosen so far
		if ( uiChosen == -1 ) {
			Log(OUTPUTMANAGER_STREAM, 
				"No repeatable outputs found.  Repeating most recent.");
			if ( (vopRecentOutputs.size()) != 0 || 
				 (ohHistory[0]->GetConveyance() == cConveyed) || (ohHistory.GetSize() == 1) )
				uiChosen = 0;
			else
				uiChosen = 1;
		}

	    // log the decision 
		Log(OUTPUTMANAGER_STREAM, "Repeating: %s", ohHistory.GetUtteranceAt(uiChosen).c_str());
		
	    // clone old output and give it a new output id
		COutput *opToRepeat = ohHistory[uiChosen]->Clone(iOutputCounter);

		// set the dialog state to the current one
//		opToRepeat->SetDialogStateIndex(pStateManager->GetStateHistoryLength()-1);
//		opToRepeat->SetDialogState(pStateManager->GetStateAsString());

	    // leave the critical section 
	    LeaveCriticalSection(&csCriticalSection);

	    // send out the output
		output(opToRepeat);

	    // increment the output counter
		iOutputCounter++;
	}

	// AD: Incorporates conveyance information and move the output from the recent
//	     list to the history
	public void Notify(int iOutputId, int iBargeinPos, 
									 String sConveyance, String sTaggedUtt) {

	    // log the reception of the notify message
	    Log(OUTPUTMANAGER_STREAM, "Received final output notification frame. id: %d; "\
	                              "bargein: %d; Conveyance Info: (dump below)\n"\
	                              "%s\nTagged Utterance: (dump below)\n%s",
	                              iOutputId, iBargeinPos, sConveyance.c_str(), 
	                              sTaggedUtt.c_str());
		
		// parse the conveyance information String
		ArrayList<String> vsParsedConveyance = PartitionString(sConveyance, " ");

		// obtain an index to the corresponding output
		int iIndex = getRecentOutputIndex(iOutputId);
		if ( iIndex == -1 ) {
			Warning(FormatString("Received notification about output %d, which doesn't "\
	                             "exist is the list of recent outputs. Ignoring it.", 
	                             iOutputId));
			return;
		}

		// step through concept/position pairs and set conveyance status of concepts
		for ( int i = 0; i < vsParsedConveyance.size(); i += 2 ) {
	        if((iBargeinPos == -1) || 
	            (atoi(vsParsedConveyance[i+1].c_str()) < iBargeinPos)) {
	            vopRecentOutputs[iIndex]->NotifyConceptConveyance(
	                vsParsedConveyance[i], cConveyed);
	        } else {
	            vopRecentOutputs[iIndex]->NotifyConceptConveyance(
	                vsParsedConveyance[i], cFailed);        
	        }
		}

		// set whether or not entire output was conveyed (was if iBargeinPos == -1)
		if ( iBargeinPos == -1 )
			vopRecentOutputs[iIndex]->SetConveyance(cConveyed);
		else
			vopRecentOutputs[iIndex]->SetConveyance(cFailed);

		// increments the number of times this prompt has been uttered
		vopRecentOutputs[iIndex]->IncrementRepeatCounter();

		// guard for safe access
		EnterCriticalSection(&csCriticalSection);

		// move last prompt frame to history
	    // normalize the tagged utterance before
	    sTaggedUtt = Trim(sTaggedUtt, " \n");
		ohHistory.AddOutput(vopRecentOutputs[iIndex], sTaggedUtt);
		vopRecentOutputs.erase(vopRecentOutputs.begin()+iIndex);
		
	    // guard for safe access
	    LeaveCriticalSection(&csCriticalSection);
		
	    // finally, log the new history on the OUTPUTHISTORY_STREAM
		Log(OUTPUTHISTORY_STREAM, ohHistory.ToString());
	}

	// D: Gets information about prompts that are about to be sent out
	public void PreliminaryNotify(int iOutputId, String sTaggedUtt) {

		// log the reception of the notify message
	    Log(OUTPUTMANAGER_STREAM, 
			"Received preliminary output notification frame. id: %d; "
	        "Tagged Utterance: (dump below)\n%s",
	        iOutputId, sTaggedUtt.c_str());
		
		// obtain an index to the corresponding output
		int iIndex = getRecentOutputIndex(iOutputId);
		if ( iIndex == -1 ) {
			Warning(FormatString("Received notification about output %d, which doesn't "\
	                             "exist is the list of recent outputs. Ignoring it.", 
	                             iOutputId));
			return;
		}

	    // now go through the prompt and compute the concepts
	    ArrayList<String> svWords = PartitionString(sTaggedUtt, " ");
	    String sConcept;    
	    for(int i = 0; i < svWords.size(); i++) {
	        String sWord = Trim(svWords[i]);
	        if(sWord[0] == '<') {
	            // if we have the beginning of a concept
	            sConcept = sWord.substr(1, sWord.length()-1);
	            // and notify that we are doing an ICT on it
	            CConcept* pConcept = vopRecentOutputs[iIndex]->GetConceptByName(sConcept);
	            if(pConcept) {
					for (int j = vopRecentOutputs[iIndex]->GetDialogStateIndex();
						(j < pStateManager->GetStateHistoryLength()) &&
						((*pStateManager)[j].fsFloorStatus != fsUser); j++) 
						pDMCore->SignalUnplannedImplicitConfirmOnConcept(j, pConcept);
	                // now log that we found a concept
	                Log(OUTPUTMANAGER_STREAM, "Signaling UNPLANNED_IMPL_CONF on concept %s", 
	                    sConcept.c_str());
	            }
	        }
	    }

	}

	// D: Cancels a concept notification request
	public void CancelConceptNotificationRequest(
	    CConcept* pConcept) {
	    // guard for safe access
	    EnterCriticalSection(&csCriticalSection);
	    // remove notification from recent outputs
	    for(int i = 0; i < vopRecentOutputs.size(); i++)
	        vopRecentOutputs[i]->CancelConceptNotificationRequest(pConcept);
	    // remove notification from history outputs
	    for(int i = 0; i < ohHistory.GetSize(); i++) 
	        ohHistory[i]->CancelConceptNotificationRequest(pConcept);
	    // leave critical section
	    LeaveCriticalSection(&csCriticalSection);
	}

	// D: Changes a concept notification request
	public void ChangeConceptNotificationPointer(
	    CConcept* pOldConcept, CConcept* pNewConcept) {
	    // guard for safe access
	    EnterCriticalSection(&csCriticalSection);
	    // remove notification from recent outputs
	    for(int i = 0; i < vopRecentOutputs.size(); i++)
	        vopRecentOutputs[i]->ChangeConceptNotificationPointer(
	            pOldConcept, pNewConcept);
	    // remove notification from history outputs
	    for(int i = 0; i < ohHistory.GetSize(); i++) 
	        ohHistory[i]->ChangeConceptNotificationPointer(
	            pOldConcept, pNewConcept);
	    // leave critical section
	    LeaveCriticalSection(&csCriticalSection);
	}

	// D: Return the list of prompts that are waiting for notification
	public String GetPromptsWaitingForNotification() {
		// construct the String
		String sResult = "";
		// guard for safe access
		EnterCriticalSection(&csCriticalSection);
	    for(int i = 0; i < vopRecentOutputs.size(); i++)
			sResult = FormatString("%s %d", sResult.c_str(), 
				vopRecentOutputs[i]->iOutputId);
		
		// leave critical section
		LeaveCriticalSection(&csCriticalSection);

		// return the String 
		return sResult;
	}

	//-----------------------------------------------------------------------------
	// A: COutputManager private (helper) methods
	//-----------------------------------------------------------------------------

	// A: This is the function that actually sends the output to the interaction 
//	    manager
	public String output(COutput* pOutput) {

		// obtain the String representation of the output
		String sOutput = pOutput->ToString();

		// if we are in a Galaxy configuration, send requests through the Galaxy 
		// interface
		#ifdef GALAXY	
		TGIGalaxyActionCall gcGalaxyCall;
		gcGalaxyCall.sModuleFunction = "main";
		gcGalaxyCall.sActionType = "system_utterance";
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":inframe", 
																sOutput));
	 
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":id", 
	        FormatString("%s:%.3d", DMI_GetSessionID().c_str(), iOutputCounter)));
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":utt_count", 
	        FormatString("%d", iOutputCounter)));
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":dialog_state_index", 
	       FormatString("%d", pOutput->GetDialogStateIndex())));
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":dialog_state", 
	       pOutput->GetDialogState()));
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":dialog_act", 
	       pOutput->GetAct()));
		gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(":final_floor_status", 
			pOutput->GetFinalFloorStatusLabel()));
		// Adds the output flags to the galaxy frame
		for (int i = 0; i < pOutput->vsFlags.size(); i++) {
			gcGalaxyCall.s2sProperties.insert(STRING2STRING::value_type(
				pOutput->vsFlags[i], "true"));		
		}

	    // retrieve the current thread id
	    DWORD dwThreadId = GetCurrentThreadId();

	    // send the message to the Galaxy Interface Thread
	    PostThreadMessage(g_idDMInterfaceThread, WM_GALAXYACTIONCALL,
	                      (WPARAM)&gcGalaxyCall, dwThreadId);	
		
		// and wait for a reply
		MSG Message;
		GetMessage(&Message, NULL, WM_ACTIONFINISHED, WM_ACTIONFINISHED);
		#endif // GALAXY

		// if we are in an OAA configuration, send requests through the OAA
		// interface
		#ifdef OAA
		TOIOAACall oaacOAACall;
		oaacOAACall.picltGoal = icl_NewTermFromString((char *)sOutput.c_str());
		// check that the OAA goal was successfully created
		if(oaacOAACall.picltGoal == NULL) {
			Error(FormatString("Error creating ICL Term for output (dump below). "\
							   "Output will not be sent.\n%s", sOutput.c_str()));
			return "";
		}
		oaacOAACall.picltInitialParams = icl_NewTermFromString("[]");
		oaacOAACall.ppicltOutParams = NULL;
		oaacOAACall.ppicltSolutions = NULL;

		// retrieve the current thread id
		DWORD dwThreadId = GetCurrentThreadId();

		// send the message to the OAA Interface Thread
	    PostThreadMessage(g_idDMInterfaceThread, WM_OAACALL,
	                      (WPARAM)&oaacOAACall, dwThreadId);	
		
		// and wait for a reply
		MSG Message;
		GetMessage(&Message, NULL, WM_ACTIONFINISHED, WM_ACTIONFINISHED);
		#endif

		// record request to await notification, if notifications are enabled for 
	    // that device
		if ( GetOutputDevice(pOutput->sOutputDeviceName)->iParams & OD_NOTIFIES ) {
	        // guard access to vopRecentRequests by critical section
	        EnterCriticalSection(&csCriticalSection);
			vopRecentOutputs.push_back(pOutput);
	        LeaveCriticalSection(&csCriticalSection);
	    } else {
	        // o/w move the output directly to the history of outputs...
	        // guard for safe access
		    EnterCriticalSection(&csCriticalSection);
	        // and before moving, also set the output as conveyed, since there
	        // will be no future notification
	        pOutput->SetConveyance(cConveyed);
	        ohHistory.AddOutput(pOutput, sOutput);
	        // guard for safe access
	        LeaveCriticalSection(&csCriticalSection);
	    }

	    return sOutput;
	}

	// A: utility function that finds the index of an output in the recent outputs
//	    list, based on the output id
	public int getRecentOutputIndex(int iOutputId) {
	    // go through the list and look for that id
		for (int i = 0; i < vopRecentOutputs.size(); i++ ) {
			if ( vopRecentOutputs[i]->iOutputId == iOutputId )
				return i;
		}
	    // if not found, return -1
		return (int)-1;
	}

*/
}