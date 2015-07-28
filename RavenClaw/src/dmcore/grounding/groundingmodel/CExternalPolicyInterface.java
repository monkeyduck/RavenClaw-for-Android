package dmcore.grounding.groundingmodel;

import utils.SplitReturnType;
import utils.Utils;
import android.util.Log;

//-----------------------------------------------------------------------------
//CExternalPolicyInterface Class - 
//This class implements an interface to an external policy (a policy that 
//  is actually implemented by an external module, and accessed over a 
//  socket)
//-----------------------------------------------------------------------------
		
public class CExternalPolicyInterface {
	
	// Log tag
	public static final String GROUNDINGMANAGER_STREAM_TAG = "GroManager";
	//---------------------------------------------------------------------
	// Private members
	//---------------------------------------------------------------------
	
	private int sSocket;         // the socket connection

	//---------------------------------------------------------------------
	// Constructors and destructors
	//---------------------------------------------------------------------
 
	public CExternalPolicyInterface(String sAHost){
		// log the action
	    Log.d(GROUNDINGMANAGER_STREAM_TAG, "Creating external grounding policy interface to "
	    		+sAHost+" ...");

	    Log.d(GROUNDINGMANAGER_STREAM_TAG, "Opening socket connection to "+sAHost+" ...");

	    // get the host name and the port
	    String sHostName;
	    int iPort;
	    SplitReturnType srt = Utils.SplitOnFirst(sAHost, ":");
	    sHostName =srt.FirstPart;
	    sAHost = srt.SecondPart;
	    iPort = Integer.parseInt(sAHost) ;

	    // initialize the sockets library
	    /*WORD wVersion = MAKEWORD(2,0);
	    WSADATA wsaData;

	    if (WSAStartup (wVersion, &wsaData) != 0)
	        FatalError("Error (on WSAStartup) in creating the external policy interface");

	    // get host information
	    struct hostent *phHost;
	    if (!(phHost = gethostbyname(sHostName.c_str()))) {
	        WSACleanup();
		    FatalError("Error in gethostbyname.");
	    }
	    
	    // open a TCP socket
	    int iTries;
	    for(iTries = 0; iTries < 5; iTries++) {
		    if ((sSocket = (unsigned long int) 
	                socket (AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET) {
	            WSACleanup();
	            FatalError("Error creating socket");
	        }
	    	
		    // connect to the server
	        struct sockaddr_in saAddress;
		    memset ((char *) &saAddress, sizeof(saAddress), 0);
		    saAddress.sin_family = AF_INET;
		    memcpy (&saAddress.sin_addr, phHost->h_addr, phHost->h_length);
		    saAddress.sin_port = htons((u_short) iPort);	
	        if (connect ((SOCKET)sSocket, (const struct sockaddr *)&saAddress, sizeof(saAddress)) == 0)
		        break;        
		    closesocket((SOCKET)sSocket);

		    Warning(FormatString(
	            "Could not connect to external grounding policy provider at %s:%d. "
	            "Reattempting in 1 second.", 
	            sHostName.c_str(), iPort));
		
	    	Sleep(200);
	    }

	    // if we exhausted the 5 tries, give a fatal error
	    if(iTries == 5) {
	        FatalError(FormatString(
	            "Error connecting to external grounding policy provider at %s:%d",
	            sHostName.c_str(), iPort));
	    }
	    
	    // o/w if everything is okay

	    // set socket to unbuffered mode (ie, execute sends immediately)
	    unsigned long int iFlag = 1;
	    if (setsockopt((SOCKET)sSocket, IPPROTO_TCP, TCP_NODELAY, 
	        (char *)(&iFlag), sizeof(iFlag)) == SOCKET_ERROR) {
	        closesocket((SOCKET)sSocket);
	        FatalError("Error in setsockopt.");
	    }
	    
	    // set socket to non-blocking mode
	    iFlag = 1;
	    if (ioctlsocket((SOCKET)sSocket, FIONBIO, &iFlag) == SOCKET_ERROR) {
	        closesocket((SOCKET)sSocket);
	        FatalError("Error in ioctlsocket.");
	    }

	    // log the success
	    Log.d(GROUNDINGMANAGER_STREAM_TAG, "Socket connection to %s:%d established ",
	        "successfully.", sHostName.c_str(), iPort);
	    Log.d(GROUNDINGMANAGER_STREAM_TAG, 
	        "External grounding policy creation completed.");
*/
	}
	

	//---------------------------------------------------------------------
	// Compute the suggested action index
	//---------------------------------------------------------------------

	
}
