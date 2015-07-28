package dmcore.grounding.groundingmodel;

import java.util.ArrayList;

public class CBeliefDistribution {
	public static final float INVALID_EVENT =((float)-1000000.203040);

	//-----------------------------------------------------------------------------
	// D: CBeliefDistribution - models a probability distribution over a set of 
	//	                          events
	//-----------------------------------------------------------------------------
	
	//---------------------------------------------------------------------
	// private class members
	//---------------------------------------------------------------------

    // the vector of probabilities for each event (action or state, or 
    // anything else
    private ArrayList<Float> vfProbability=new ArrayList<Float>();

	// and the lower and upper bounds for the confidence interval on that 
	// vector
    private ArrayList<Float> vfProbabilityLowBound=new ArrayList<Float>();
    private ArrayList<Float> vfProbabilityHiBound=new ArrayList<Float>();
    
    //---------------------------------------------------------------------
  	// public method
  	//---------------------------------------------------------------------
    
	/*// D: resize the event space for the distribution
    public void Resize(int iNumEvents) {
        //resize the probabilities vector
        vfProbability.resize(iNumEvents, 0.0);
    	vfProbabilityLowBound.resize(iNumEvents, INVALID_EVENT);
    	vfProbabilityHiBound.resize(iNumEvents, INVALID_EVENT);
    }*/
    
     //-----------------------------------------------------------------------------
	 // D: Access to distribution
	 //-----------------------------------------------------------------------------
	 // D: Access to probabilities, via the [] operator
	 public float getIndexing (int iIndex) {
	     // resize the vector if we're falling out of it
	     if(iIndex >= vfProbability.size()) {
	        /*vfProbability.resize(iIndex + 1, 0.0);
	 		vfProbabilityLowBound.resize(iIndex + 1, INVALID_EVENT);
	 		vfProbabilityHiBound.resize(iIndex + 1, INVALID_EVENT);*/
	    	 return (float)0.0;
	     } 
	     return vfProbability.get(iIndex);
	 }
	
	 // D: Access to low bounds
	 public float LowBound(int iIndex) {
	     // resize the vector if we're falling out of it
	     if(iIndex >= vfProbabilityLowBound.size()) {
	       /*  vfProbability.resize(iIndex + 1, 0.0);
	 		vfProbabilityLowBound.resize(iIndex + 1, INVALID_EVENT);
	 		vfProbabilityHiBound.resize(iIndex + 1, INVALID_EVENT);*/
	    	 return (float)0.0;
	     }
	     return vfProbabilityLowBound.get(iIndex);
	 }
	
	 // D: Access to hi bounds
	 public float HiBound(int iIndex) {
	     // resize the vector if we're falling out of it
	     if(iIndex >= vfProbabilityHiBound.size()) {
	        /* vfProbability.resize(iIndex + 1, 0.0);
	 		vfProbabilityLowBound.resize(iIndex + 1, INVALID_EVENT);
	 		vfProbabilityHiBound.resize(iIndex + 1, INVALID_EVENT);*/
	    	 return (float)0.0;
	     }
	     return vfProbabilityHiBound.get(iIndex);
	 }
	
	 // D: Access to the number of valid events
	 public int GetValidEventsNumber() {
	     int iEventsNumber = 0; 
	     for(int i = 0; i < vfProbability.size(); i++)
	         if(vfProbability.get(i) != INVALID_EVENT)
	             iEventsNumber++;
	     return iEventsNumber;
	 }
	
	 //-----------------------------------------------------------------------------
	 // D: Functions for transforming the distribution
	 //-----------------------------------------------------------------------------
	
	 // D: Normalize the distribution
	 public void Normalize() {
	     // compute the normalization constant
	     float fNormalizer = 0;
	     for(int i = 0; i < vfProbability.size(); i++)
	         if(vfProbability.get(i) != INVALID_EVENT) {
	             fNormalizer += vfProbability.get(i);
	         }
	     // normalize
	     if(fNormalizer != 0) {
	         for(int i = 0; i < vfProbability.size(); i++) 
	             if(vfProbability.get(i) != INVALID_EVENT) {
	                 vfProbability.set(i,vfProbability.get(i) / fNormalizer);
	             }
	     }
	 }
	
	 //-----------------------------------------------------------------------------
	 // Functions for choosing a particular action from the distribution
	 //-----------------------------------------------------------------------------
	
	 // D: return the action with the highest probability/utility
	 public int GetModeEvent() {
	     int iMaxIndex = -1;
	     float fMaxProbability = 0;
	     for(int i = 0; i < vfProbability.size(); i++) {
	         if(vfProbability.get(i) > fMaxProbability) {
	             fMaxProbability = vfProbability.get(i);
	             iMaxIndex = i;
	         }
	     }
	     return iMaxIndex;
	 }
	
	 // D: return the action with the highest upper bound on the probability/utility
	//     if multiple events in the distribution have the same highest upper bound, 
	//     choose randomly between them
	 public int GetMaxHiBoundEvent() {
	
	 	// store a vector with the indices that had the max highest bound
	     ArrayList<Integer> viMaxIndex = new ArrayList<Integer>();
	 	viMaxIndex.add(-1);
	     float fMaxProbability = 0;
	     for(int i = 0; i < vfProbabilityHiBound.size(); i++) {
	         if(vfProbabilityHiBound.get(i) > fMaxProbability) {
	             fMaxProbability = vfProbabilityHiBound.get(i);
	 			viMaxIndex.clear();
	 			viMaxIndex.add(i);
	         } else if(vfProbabilityHiBound.get(i) == fMaxProbability) {
	 			// if we have another maximum store it in the vector
	 			viMaxIndex.add(i);
	 		}
	     }
	
	 	// now return randomly from that vector
	 	if(viMaxIndex.size() == 1)
	 		return viMaxIndex.get(0);
	 	else
	 		return viMaxIndex.get((int) (((int)100*Math.random()) % viMaxIndex.size()));
	 }
	
	 // D: return the event according to an epsilon-greedy policy
	 public int GetEpsilonGreedyEvent(float fEpsilon) {
	     float fRandom = (float)Math.random();
	     if(fRandom > fEpsilon) {
	         // if outside of epsilon, then choose the mode
	         return GetModeEvent();
	     } else {
	         // if within epsilon, then randomly choose between the
	         // non-mode events
	         int iMode = GetModeEvent();
	         
	         // check that there are other actions
	         int iValidEventsNumber = GetValidEventsNumber();
	         if(iValidEventsNumber <= 1)
	             return iMode;
	
	         // if we have other actions, choose one of them
	         int iTemp = (int) (100*Math.random() % (GetValidEventsNumber() - 1));
	         // find the first valid non-mode event
	         int iChoose = 0;
	         while((iChoose == iMode) || (vfProbability.get(iChoose) == INVALID_EVENT))
	             iChoose++;
	
	         // now count down from iTemp and for each count, find the next 
	         // valid event 
	         while(iTemp!=0) {
	             iTemp--;
	             iChoose++;
	             while((iChoose == iMode) || (vfProbability.get(iChoose) == INVALID_EVENT))
	                 iChoose++;
	         }
	
	         // finally, return the chosen event
	         return iChoose;
	     }
	 }
	
	 // D: return the event according to a soft-max policy
	 public int GetSoftMaxEvent(float fTemperature) {
	     // compute the exponentiated values in a temporary belief distribution
	     CBeliefDistribution bdTemp= new CBeliefDistribution();
	     for(int i = 0; i < vfProbability.size(); i++) 
	         if(vfProbability.get(i) == INVALID_EVENT) {
	             //bdTemp.get(i) = INVALID_EVENT;
	         } else {
	             //bdTemp.get(i) = (float)(exp(vfProbability.get(i)/fTemperature));
	         }
	     // normalize
	     bdTemp.Normalize();
	     // then get a randomly drawn event
	     return bdTemp.GetRandomlyDrawnEvent();
	 }
	
	 // D: Randomly draw an event from the probability/utility distribution
	 public int GetRandomlyDrawnEvent() {
	     float fAccumulator = 0;
	     float fRandom = (float)Math.random() ;    
	     Normalize();
	     int lastI = -1;
	     for(int i = 0; i < vfProbability.size(); i++) {
	         if(vfProbability.get(i) != INVALID_EVENT) {
	             fAccumulator += vfProbability.get(i);
	             lastI = i;
	         }
	         if(fAccumulator > fRandom) return i;
	     }
	     // o/w return the last event
	     return lastI;
	 }
    
}
