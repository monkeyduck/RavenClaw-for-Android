package dmcore.events;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dmcore.agents.coreagents.DMCore;

import android.annotation.SuppressLint;
import android.util.Log;

import utils.Const;
import utils.SplitReturnType;
import utils.Utils;

public class CInteractionEvent {
	
	//---------------------------------------------------------------------
	// Protected members
	//---------------------------------------------------------------------
	//

	// The type of the event
	protected String sType;

	// Event ID
	protected int iID;

	// Flag indicating whether this event is completed or not
	protected boolean bComplete;

	// The confidence that the system has in its detection/understanding
	// of the event
	protected float fConfidence = (float) 1.0;

	// The hash of properties for this event
	protected HashMap<String,String> s2sProperties=new HashMap<String,String>();
	
	// L:Flag indicating whether slot matched successfully
	private boolean bSlotMatchSuccess=false;
	//---------------------------------------------------------------------
	// Constructor and destructor
	//---------------------------------------------------------------------
	//

	// A: Default constructor
	public CInteractionEvent() {}

	// A: Specifies an event type
	public CInteractionEvent(String sAType) {
		sType = sAType;
	}

	//---------------------------------------------------------------------
	// Public methods to access private members
	//---------------------------------------------------------------------
	//

	// A: Returns a String describing the event type
	public String GetType() {
		return sType;
	}

	// A: Returns the event ID
	public int GetID() {
		return iID;
	}

	// A: Indicates whether this event is a completed one or a partial update
	public boolean IsComplete() {
		return bComplete;
	}

	// A: Returns the confidence about the event
	public float GetConfidence() {
		return fConfidence;
	}
	public void SetThreadId(int tidID){
		iID = tidID;
	}
	public void SetProperties(String semantic){
		// Check if semantic not null
		if (semantic.equals(""))
			Log.e(Const.WARNING_STREAM,
					"There is no input words to set interaction event properties");
		// Check if the semantic is not be understood
		if (Utils.SplitOnFirst(semantic, ":").FirstPart.equals("{\"text\"")
				&&Utils.SplitOnLast(semantic, ":").SecondPart.equals("4}")){
			// Not be understood
			// Check the current agent
			String slot=DMCore.pDMCore.GetAgentInFocus().GetRequiredConcept();
			String sResult="",text="";
			text = Utils.SplitOnFirst(semantic, ",").FirstPart;
			text = Utils.SplitOnFirst(text, ":").SecondPart;
			// delete the quotas ""
			text = text.substring(text.indexOf('\"')+1,text.lastIndexOf('\"'));
			text = Utils.DeletePunctuation(text,'。');
			// If the current agent needs date concept
			if (slot.equals("[flight_query.startDate]")){
				sResult = BindingDateSlot(text);
				if (bSlotMatchSuccess)
					s2sProperties.put(slot, sResult);
				
			}
			else if (slot.equals("[flight_query.startLoc]")||
					slot.equals("[flight_query.endLoc]")){
				sResult = BindingLocSlot(text);
				if (bSlotMatchSuccess)
					s2sProperties.put(slot, sResult);
			}else{
				Log.e(Const.INPUTMANAGER_STREAM,"Required error concept");
			}
			
			
		}
		// Check if semantic contains key word "slots"
		else if(semantic.indexOf("slots")>0){
			if (semantic.contains("startDate")){
				int iStart = semantic.indexOf("date");
				semantic = semantic.substring(iStart);
				SplitReturnType srt = Utils.SplitOnFirst(semantic, ":");
				semantic = srt.SecondPart;
				srt = Utils.SplitOnFirst(semantic, ",");
				String sResult = srt.FirstPart.trim();
				sResult = sResult.substring(1, sResult.length()-1);
				s2sProperties.put("[flight_query.startDate]",sResult);
			}
			if (semantic.contains("startLoc")){
				int iStart = semantic.indexOf("cityAddr");
				semantic = semantic.substring(iStart);
				SplitReturnType srt = Utils.SplitOnFirst(semantic, ":");
				semantic = srt.SecondPart;
				srt = Utils.SplitOnFirst(semantic, ",");
				String sResult = srt.FirstPart.trim();
				sResult = sResult.substring(1, sResult.length()-1);
				s2sProperties.put("[flight_query.startLoc]",sResult);
			}
			if (semantic.contains("endLoc")){
				int iStart = semantic.indexOf("cityAddr");
				semantic = semantic.substring(iStart);
				SplitReturnType srt = Utils.SplitOnFirst(semantic, ":");
				semantic = srt.SecondPart;
				srt = Utils.SplitOnFirst(semantic, ",");
				String sResult = srt.FirstPart.trim();
				sResult = sResult.substring(1, sResult.length()-1);
				s2sProperties.put("[flight_query.endLoc]",sResult);
			}
		}
	}
	
	// LILINCHUAN: Parse oral date to specific date
	@SuppressLint("SimpleDateFormat")
	public String BindingDateSlot(String sText){
		Calendar cal = Calendar.getInstance();
		// Tell which date form is the text
		if (sText.contains("月")&&(sText.contains("日"))){
			// The specific date:xx月xx日/号
			bSlotMatchSuccess = true;
			SplitReturnType srt = Utils.SplitOnFirst(sText, "月");
			int iMonth = Integer.parseInt(srt.FirstPart);
			int iDay = Integer.parseInt(Utils.SplitOnFirst(srt.SecondPart, "日")
					.FirstPart);
			cal.set(Calendar.MONTH, iMonth-1);
			cal.set(Calendar.DAY_OF_MONTH,iDay);
			
		}
		else if (sText.contains("月")&&sText.contains("号")){
			// The specific date:xx月xx日/号
			bSlotMatchSuccess = true;
			SplitReturnType srt = Utils.SplitOnFirst(sText, "月");
			int iMonth = Integer.parseInt(srt.FirstPart);
			int iDay = Integer.parseInt(Utils.SplitOnFirst(srt.SecondPart, "号")
					.FirstPart);
			cal.set(Calendar.MONTH, iMonth-1);
			cal.set(Calendar.DAY_OF_MONTH,iDay);
		}
		else if (sText.equals("明天")){
			bSlotMatchSuccess=true;
			cal.add(Calendar.DATE, 1);
		}
		else if (sText.equals("今天")){
			bSlotMatchSuccess=true;
		}
		else if (sText.equals("后天")){
			bSlotMatchSuccess=true;
			cal.add(Calendar.DATE, 2);
		}
		else{
			SplitReturnType srt = new SplitReturnType();
			srt = Utils.SplitOnFirst(sText, "周");
			if (srt.IsSplitSuccessful)
			{
				bSlotMatchSuccess=true;
				String sDay=srt.SecondPart;
				int iDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				int iDay=0;
				if (sDay.equals("一")){
					iDay=2;
				}
				else if (sDay.equals("二")){
					iDay=3;
				}
				else if (sDay.equals("三")){
					iDay=4;
				}
				else if (sDay.equals("四")){
					iDay=5;
				}
				else if (sDay.equals("五")){
					iDay=6;
				}
				else if (sDay.equals("六")){
					iDay=7;
				}
				else if (sDay.equals("日")||sDay.equals("天")){
					iDay=8;
				}
				else{
					Log.e(Const.INPUTMANAGER_STREAM,"Input date error");
				}
				// Indicates this week
				if (srt.FirstPart.equals("")||srt.FirstPart.equals("本")){
					int iValue = iDay - iDayOfWeek;
					cal.add(Calendar.DATE, iValue);
				}
				// Indicates next week
				else if (srt.FirstPart.equals("下")){
					int iValue = iDay + 7 - iDayOfWeek;
					cal.add(Calendar.DATE, iValue);
				}
			}
		
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String sResult = sdf.format(cal.getTime());
		return sResult;
	}
	
	// L: Check if there is a airport in this location
	public String BindingLocSlot(String sText){
		int iStartIndex = Const.CityAirport.indexOf(sText);
		if (iStartIndex>=0){
			bSlotMatchSuccess = true;
		}
		return sText;
	}
	// A: Returns the String value for a property of the event
	public String GetStringProperty(String sSlot) {
		if (s2sProperties.containsKey(sSlot)) {
			return s2sProperties.get(sSlot);
		} else {
			Log.w(Const.WARNING_STREAM, "Property "+sSlot+" not found in event. "
				+"Returned empty String.");
			return "";
		}
	}

	// A: Returns the int value for a property of the event
	public int GetIntProperty(String sSlot) {
		if (s2sProperties.containsKey(sSlot)) {
			return Integer.valueOf(s2sProperties.get(sSlot));
		} else {
			Log.w(Const.WARNING_STREAM, "Property "+sSlot+" not found in event. "
				+"Returned 0.");
			return 0;
		}
	}

	// A: Returns the float value for a property of the event
	public float GetFloatProperty(String sSlot) {
		if (s2sProperties.containsKey(sSlot)) {
			return Float.valueOf(s2sProperties.get(sSlot));
		} else {
			Log.w(Const.WARNING_STREAM, "Property "+sSlot+" not found in event. "
				+"Returned 0.");
			return 0.0f;
		}
	}

	// A: Checks if a certain property is defined for the event
	public boolean HasProperty(String sSlot) {
		return s2sProperties.containsKey(sSlot);
	}

	// A: Sets a property value
	public void SetProperty(String sSlot, String sValue) {
		s2sProperties.put(sSlot, sValue);
	}
	// A: Sets complete flag
	public void SetCompleted(boolean bCompleted){
		bComplete=bCompleted;
	}

	// A: Returns the hash of event properties
	public HashMap<String,String> GetProperties() {
		return s2sProperties;
	}

	// A: Returns a String representation of the event
	public String ToString() {
		String sEvent;

		sEvent = "Type\t"+sType+"\n";
		sEvent += "Complete\t"+bComplete+"\n";

		// returns the contents of the input hash
		if(s2sProperties.size() > 0) {
			String iPtr="";
			Iterator<String> iterator=s2sProperties.keySet().iterator();
			while(iterator.hasNext()) {
				iPtr = iterator.next();
				sEvent += "  "+iPtr+" = "+s2sProperties.get(iPtr)+"\n";
				
			}
		}
		
		// finally return the String
		return sEvent;
	}

	//---------------------------------------------------------------------
	// Methods to test and access event properties
	//---------------------------------------------------------------------

	// A: Check if a certain expectation is matched by the input 
	// D: fixed bug to match case-insensitive
	public boolean Matches(String sGrammarExpectation) {

		// remove the "[" and "]" around the expectation
		String sTemp = sGrammarExpectation.substring(
				1, sGrammarExpectation.length()-1);	

		// extracts the expectation channel from the grammar expectation String
		SplitReturnType srt =Utils.SplitOnFirst(sTemp, ":");
		String sChannel=srt.FirstPart;
		String sExpectation=srt.SecondPart;
		if (sExpectation == "") {
			sExpectation = sChannel;
			sChannel = "";
		}

		// the expectation is not for this type of event, no match
		if ((sChannel != "")&&(sChannel != sType)) {
			return false;
		}

		// traverses the hash and matches the given expectation
		Iterator<String> iterator = s2sProperties.keySet().iterator();
		String iPtr="";
		while(iterator.hasNext()) {
			iPtr=iterator.next();
			if (matchesSlot(sExpectation, iPtr)) {
				return true;
			}
		}
		
		return false;
	}

	// A: Returns the String corresponding to a given expectation in the input
	public String GetValueForExpectation(String sGrammarExpectation) {
		String matched_value;

		// remove the "[" and "]" around the expectation
		String sTemp = sGrammarExpectation.substring( 1, sGrammarExpectation.length()-1);	

		// extracts the expectation channel from the grammar expectation String
		SplitReturnType srt = Utils.SplitOnFirst(sTemp, ":");
		String sChannel = srt.FirstPart;
		String sExpectation=srt.SecondPart;
		if (sExpectation == "") {
			sExpectation = sChannel;
			sChannel = "";
		}

		// the expectation is not for this type of event, no match
		if ((sChannel != "")&&(sChannel != sType)) {
			Log.w(Const.WARNING_STREAM,"Channel mismatch for "+
					sGrammarExpectation+", empty value used.");
			return "";
		}

		// traverses the hash, searching for the best match for the expectation
		// "best" = (slot name matches) & (shallowest/broadest)
		Iterator<Map.Entry<String, String>> iterator = 
				s2sProperties.entrySet().iterator();
		Map.Entry<String, String> iPtr;
		while(iterator.hasNext()) {
			iPtr = iterator.next();
			if (matchesSlot(sExpectation, iPtr.getKey())) {
		
				// returns the value of the matched slot
				matched_value = iPtr.getValue();
				if (matched_value == "") {
					Log.w(Const.WARNING_STREAM,"Event property "+sGrammarExpectation+
							" has empty value.");
				}
				return matched_value;
			}
		}

		// no matching property found, log a warning and return ""
		Log.w(Const.WARNING_STREAM,"No event property found matching "
				+sGrammarExpectation+".");
		return "";
	}

	// A: Matches a slot with an expectation pattern
	//	    allowing for wild cards
	public boolean matchesSlot(String pattern, String slot) {
		// removes the "[" and "]" around the slot
		slot = slot.substring( 1, slot.length()-1);
		return pattern.equalsIgnoreCase(slot);
		/*int pos_slot = 0;
		int len_slot = 0;
		int pos_patt = 0;
		int len_patt = 0;
		String sub_slot="", sub_patt="";

		// removes the "[" and "]" around the slot
		slot = slot.substring( 1, slot.length()-1);

		// gets the first level in the pattern
		if ((int)pattern.indexOf( '.', pos_patt) > (int)pos_patt) {
			len_patt = pattern.indexOf( '.', pos_patt) - pos_patt;
		}
		else {
			// no more '.', get the end of the pattern
			len_patt = pattern.length() - pos_patt;
		}
		sub_patt = pattern.substring( pos_patt, len_patt);
		pos_patt += len_patt + 1;

		// gets the first level in the slot name
		if ((int)slot.indexOf( '.', pos_slot) > (int)pos_slot) {
			len_slot = slot.indexOf( '.', pos_slot) - pos_slot;
		}
		else {
			// no more '.', get the end of the slot name
			len_slot = slot.length() - pos_slot;
		}
		sub_slot = slot.substring( pos_slot, len_slot);
		pos_slot += len_slot + 1;

		// traverses the expectation pattern and the slot name
		while (true) {

			// compare the level names
			if (sub_slot.equalsIgnoreCase(sub_patt)) {
				
				// the final subslot of both the pattern and the slot matched:
				// we won!
				if ((pos_patt >= pattern.length())&&(pos_slot >= slot.length())) {
					return true;
				}

				// we reached the end of the pattern but not that of the slot => fail
				if (pos_patt >= pattern.length()) {
					return false;
				}

				if ((int)pattern.indexOf( '.', pos_patt) > (int)pos_patt) {
					len_patt = pattern.indexOf( '.', pos_patt) - pos_patt;
				}
				else {
					// no more '.', get the end of the pattern
					len_patt = pattern.length() - pos_patt;
				}
				sub_patt = pattern.substring( pos_patt, len_patt);

				// move the position index for the pattern
				pos_patt += len_patt + 1;
			}

			// we reached the end of the slot but not that of the pattern => fail
			if (pos_slot >= slot.length()) {
				return false;
			}

			// gets the next level in the slot name
			if ((int)slot.indexOf( '.', pos_slot) > (int)pos_slot) {
				len_slot = slot.indexOf( '.', pos_slot) - pos_slot;
			}
			else {
				// no more '.', get the end of the slot name
				len_slot = slot.length() - pos_slot;
			}
			sub_slot = slot.substring( pos_slot, len_slot);

			// moves the position index for the slot
			pos_slot += len_slot + 1;
		}*/
	}
}
