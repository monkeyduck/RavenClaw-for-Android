package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;


public class Utils {
	// public static members
	public static final int STRING_MAX = 65535;
	
	// D: convert a String list of attribute values into a STRING2STRING
	// representation
	public static HashMap<String,String> StringToS2SHash(String sString) {
		
		String sSeparator=",";
		String sEquals="=";
		 
	  // extract the pairs
	  ArrayList<String> vsPairs = PartitionString(sString,sSeparator);
	  // form the hash
	  HashMap<String,String> s2s = new HashMap<String,String>();
	  // construct a SpiltReturn Object
	  SplitReturnType srt = new SplitReturnType();
	  
	  for(int i = 0; i < vsPairs.size(); i++) {
	      String sAttr, sValue;
	      srt = SplitOnFirst(vsPairs.get(i), sEquals); 
	      sAttr = srt.FirstPart;
	      sValue = srt.SecondPart;
	      s2s.put(sAttr.trim(), sValue.trim());
	  }
	  // finally, return the constructed hash
	  return s2s;
	}
	// A: partitions a String into tokens divided by any of a set of specified characters.
	public static ArrayList<String> PartitionString(String sString, String pDividers) {
		ArrayList<String> saResult = new ArrayList<String>();
		String sTemp="";
		SplitReturnType srt = new SplitReturnType();
		while (sString != "" )
		{
			srt = SplitOnFirst(sString, pDividers);
			sTemp = srt.FirstPart;
			sString = srt.SecondPart;
			if ( sTemp != "" )
				saResult.add(sTemp);
		}
		return saResult;
	} 
	// A: splits the String in 2 parts, around and not including the first 
	//  occurence of any of a set of specified characters. Returns true on success
	public static SplitReturnType SplitOnFirst(String sOriginal, String pDividers) {
		
		SplitReturnType srt = new SplitReturnType();
		int iCharPos = sOriginal.indexOf(pDividers);
	  if(iCharPos == -1) {
			// if the character was not found
		  
			srt.FirstPart = sOriginal;
			srt.SecondPart = "";
			srt.IsSplitSuccessful=false;
		} else {
			// if the character was found
			srt.FirstPart = sOriginal.substring(0, iCharPos);
			srt.SecondPart = sOriginal.substring(iCharPos + 1);
			srt.IsSplitSuccessful = true;			
		}
	  return srt;
	}
	// D: function similar to SplitOnFirst. It takes as an extra argument a char
	// that act as quote characters, and therefore any occurence of the dividers 
	// within that is not considered
	public static SplitReturnType SplitOnFirst(String sOriginal, String pDividers, char cQuote) {
		SplitReturnType srt = new SplitReturnType();
	    int i = 0;
	    boolean bWithinQuotes = false;
	    int l = sOriginal.length();
	    while(i < l) {
	        // if we/re within quotes, just skip over everything until 
	        // a new quote character is met
	        if(bWithinQuotes) {
	            while((sOriginal.charAt(i) != cQuote) && (i < l)) 
	                i++;
	            // check that we didn't reach the end
	            if(i == l) {
	                srt.FirstPart = sOriginal;
	                srt.SecondPart = "";
	                srt.IsSplitSuccessful= false;
	                return srt;
	            }
	            // o/w increment i;
	            i++;
	            // and set ourselves out of quotes
	            bWithinQuotes = false;
	        } else if(sOriginal.charAt(i) == cQuote) {
	            // o/w if we just saw a quote, put ourselves in quote mode
	            bWithinQuotes = true;
	            // and move on
	            i++;
	        } else if(pDividers.indexOf(sOriginal.charAt(i))!=-1) {
	            // o/w if we hit on one of the dividers
	            srt.FirstPart = sOriginal.substring(0, i);
	            srt.SecondPart = sOriginal.substring(i + 1, sOriginal.length()+1);
	            srt.IsSplitSuccessful= true;
	            return srt;
	        } else {
	            i++;
	        }
	    }
	
	    // if we got out of the loop, it means we reached the end without returning, 
	    // so then there are no dividers
	    srt.FirstPart = sOriginal;
	    srt.SecondPart = "";
	    srt.IsSplitSuccessful= false;
	    return srt;
	}
	// A: splits the String in 2 parts, around and not including the last
	//  occurence of any of a set of specified characters. Returns true on success
	public static SplitReturnType SplitOnLast(String sOriginal, String pDividers) {
		SplitReturnType srt = new SplitReturnType();
	  int iCharPos = sOriginal.lastIndexOf(pDividers);
		if(iCharPos == -1) {
			// if the character was not found
			srt.FirstPart = "";
			srt.SecondPart = sOriginal;
			srt.IsSplitSuccessful =false;
			return srt;
		} else {
			// if the character was found
			srt.FirstPart = sOriginal.substring(0, iCharPos);
			srt.SecondPart = sOriginal.substring(iCharPos + 1,sOriginal.length());
			srt.IsSplitSuccessful = true;
			return srt;			
		}
	}
	// D: extracts the first line of a String, and returns it (the String is 
	//  chopped)
	public static SplitReturnType ExtractFirstLine(String rString) {
		SplitReturnType srt = SplitOnFirst(rString, "\n");
		return srt;
	}

	// D: find the corresponding closing quote
	public static int FindClosingQuoteChar(String sString, int iStartPos, 
									  char cOpenQuote, char cCloseQuote) {

		int iOpenBraces = 1;	
		int iPos = iStartPos;
		while((iOpenBraces > 0) && (iPos < sString.length())) {
			if(sString.charAt(iPos) == cOpenQuote) 
				iOpenBraces++;
			else if(sString.charAt(iPos) == cCloseQuote)
				iOpenBraces--;
			iPos++;
		}

		// finally return the position
		return iPos;
	}
	
	// D: add to a S2S hash from a String description
	public static void AppendToS2S(HashMap<String, String> rs2sInto, HashMap<String, String> rs2sFrom) {
	    rs2sInto.putAll(rs2sFrom);
	}
	
	public static Calendar GetTime(){
		return Calendar.getInstance();
	}
	public static String TrimLeft(String sString, String pToTrim) {
		String sResult = "";
		if (pToTrim.equals(" ")){
			sResult = sString.substring(sString.indexOf(sString.trim().charAt(0)));
		}
		else{
			int firstIndex = sString.indexOf(pToTrim);
			sResult = sString.substring(firstIndex);
		}
		return sResult;
	}
	// A: trim specified characters (default spaces) from the String on the right
	public static String TrimRight(String sString, String pToTrim) {
		String sResult = "";
		if (pToTrim.equals(" ")){
			if (sString.startsWith(" ")){
				sResult = sString.substring(0,sString.indexOf(sString.trim().substring(0, 1))
						+sString.trim().length());
			}
			else	sResult = sString.trim();
		}
		else{
			int lastIndex = sString.lastIndexOf(pToTrim);
			if (lastIndex!=-1)
				sResult = sString.substring(0, lastIndex);
			else
				sResult = sString.trim();
		}
		return sResult;
	}
	// D: convert a STRING2STRING hash of attribute values into a String 
	//  representation								  
	public static String S2SHashToString(HashMap<String,String> s2sHash, 
				String sSeparator, String sEquals) {
	  // store the String
	  String sResult = "";
		if (!s2sHash.isEmpty()) {
			// go through the mapping and find something that matches the focus
			Set<Map.Entry<String, String>> key = new HashSet<Map.Entry<String,String>>();
			key = s2sHash.entrySet();
			Iterator<Map.Entry<String, String>> iterator = key.iterator();
			boolean isFirst = true;
			while(iterator.hasNext()) {
				if (!isFirst) {
					isFirst = false;
					sResult += sSeparator;
				}
				Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
				sResult += entry.getKey() + sEquals + entry.getValue();
			}
		}
	  // finally, return the String
	  return sResult;
	}
	// Overload
	public static String S2SHashToString(HashMap<String,String> s2sHash) {
		String sSeparator = ",";
		String sEquals = "=";
		// store the String
		String sResult = "";
		if (!s2sHash.isEmpty()) {
			// go through the mapping and find something that matches the focus
			Set<Map.Entry<String, String>> key = new HashSet<Map.Entry<String,String>>();
			key = s2sHash.entrySet();
			Iterator<Map.Entry<String, String>> iterator = key.iterator();
			boolean isFirst = true;
			while(iterator.hasNext()) {
				if (!isFirst) {
					isFirst = false;
					sResult += sSeparator;
				}
				Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
				sResult += entry.getKey() + sEquals + entry.getValue();
			}
		}
		  // finally, return the String
		  return sResult;
	}
	// Overload
	public static String S2SHashToString(HashMap<String,String> s2sHash,String sSeparator) {
		String sEquals = "=";
		// store the String
		String sResult = "";
		if (!s2sHash.isEmpty()) {
			// go through the mapping and find something that matches the focus
			Set<Map.Entry<String, String>> key = new HashSet<Map.Entry<String,String>>();
			key = s2sHash.entrySet();
			Iterator<Map.Entry<String, String>> iterator = key.iterator();
			boolean isFirst = true;
			while(iterator.hasNext()) {
				if (!isFirst) {
					isFirst = false;
					sResult += sSeparator;
				}
				Map.Entry<String, String> entry = (Map.Entry<String, String>)iterator.next();
				sResult += entry.getKey() + sEquals + entry.getValue();
			}
		}
		  // finally, return the String
		  return sResult;
	}
	public static String ReplaceSubString(String sSource, 
			String sToReplace, String sReplacement) {
	    // the resulting String
	    String sResult = sSource;
	    int pos = 0;
	    while((pos = sResult.indexOf(sToReplace, pos)) >= 0) {
	        sResult = sResult.replace(sToReplace, sReplacement);
			pos += sReplacement.length();
	    }
	    return sResult;
	}
	public static String GetValueGivenSlot(String Semantic,String SlotName){
		if (Semantic.contains(SlotName)){
			int iStartIndex = Semantic.indexOf(SlotName);
			int iColon = Semantic.indexOf(':', iStartIndex);
			int iLeftQuotation = Semantic.indexOf('"', iColon);
			int iRightQuotation= Semantic.indexOf('"', iLeftQuotation+1);
			return Semantic.substring(iLeftQuotation+1,iRightQuotation).trim();
		}
		else
			return "";
	}

	public static void DMI_SendEndSession(){
		
	}
	public static void DMI_SetTimeoutPeriod(int iATimeoutPeriod){
		
	}
	public static int atoi(String sString) {
		// TODO Auto-generated method stub
		try{
			int iValue = Integer.parseInt(sString);
			return iValue;
		}catch(Exception e){
			Log.e("DMCoreAgent","parse string to int error in Utils.atoi");
			return -10000;
		}
	}
	public static String Trim(String sResult, String string) {
		// TODO Auto-generated method stub
		return TrimRight(TrimLeft(sResult,string),string);
	}
	public static String DeletePunctuation(String text, char punctuation) {
		// TODO Auto-generated method stub
		int iIndex = text.indexOf(punctuation);
		return text.substring(0,iIndex);
	}
}


