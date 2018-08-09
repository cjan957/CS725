/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 

class TCPServer { 
	
	private static String userPassFile = "/src/userpass.txt";
	private static BufferedReader inFromClient;
	private static DataOutputStream outToClient;
	//private static String accountFile = "";
    
    public static void main(String argv[]) throws Exception 
    { 
    	
    	String clientRequest;
    			
		ServerSocket welcomeSocket = new ServerSocket(6789); 
		
		//TODO: send greeting message
		
		
		while(true) { 
		    
	        Socket connectionSocket = welcomeSocket.accept(); 
		    
		    inFromClient = 
			new BufferedReader(new
			    InputStreamReader(connectionSocket.getInputStream())); 
		    
		    outToClient = 
			new DataOutputStream(connectionSocket.getOutputStream()); 
		    		    
		    clientRequest = readMessageFromClient();
		    boolean validRequest = validateClientRequest(clientRequest);
		    
		    if(validRequest)
		    { 	
		    	performRequest(clientRequest);
		    }
		    
		    
		    //capitalizedSentence = clientRequest.toUpperCase() + '\n'; 
		    
		    //outToClient.writeBytes(capitalizedSentence); 
		} 
    }

    
    private static void sendMessage(String message)
    {
    	
    }
    
	private static void performRequest(String clientRequest) {
		// TODO Auto-generated method stub
		String[] requestBreakdown = clientRequest.split(" ");
		String command = requestBreakdown[0];
						
		if(command.equals("user"))
		{
			userCommand(clientRequest);
		}
		
	}
	
	private static void userCommand(String clientRequest) {
		
		//breakdown the request using space
		String[] requestBreakdown = clientRequest.split(" ");
		
		//i.e. USER<SPACE>cjan957
		if(requestBreakdown.length == 2)
		{
			boolean usernameFound = false;
			String username = requestBreakdown[1];
			
			String filePath = new File("").getAbsolutePath();
			String fullFilePath = filePath.concat(userPassFile);
			File f = new File(fullFilePath);
			
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			       String[] usernamePassBreakdown = line.split("::");
			       
			       if(usernamePassBreakdown[0].equals(username))
			       {
			    	   usernameFound = true;
			    	   if(usernamePassBreakdown.length == 2)
				       {
				    	   //Username found and account/password is needed
				       }
			    	   else if(usernamePassBreakdown.length == 1)
			    	   {
			    		   //Username found and no need for account/password
			    	   }
			       }	
			    }
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}	
			
			
			if(usernameFound)
			{
				//TODO: Send success message
			}
			else
			{
				//TODO: send failed
			}
			
			
		}
		//i.e. USER 
		else
		{
			//TODO:// send back bad response
		}
	}


	private static String readMessageFromClient()
	{
		String requestBuffer = "";
		int character = 0;
		
		while(true)
		{
			try {
				character = inFromClient.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//Check for null
			if(character == 0)
			{
				break;
			}
			requestBuffer.concat(Character.toString((char)character));
		}
		return requestBuffer;
	}
	
	
//	private static int sendMessageToClient()
//	{
//		
//	}
	

	private static boolean validateClientRequest(String request) {
		
		//split request to check the command
		String[] requestBreakdown = request.split(" ");
				
		String command = requestBreakdown[0];
		
		if(command.length() != 4)
		{
			return false;
		}
		
		//4 ASCII command should be of any case.
		String lowerCommand = command.toLowerCase();
		
		if(lowerCommand.equals("user") || lowerCommand.equals("acct") || lowerCommand.equals("pass") ||
		lowerCommand.equals("type") || lowerCommand.equals("list") || lowerCommand.equals("cdir") ||
		lowerCommand.equals("kill") || lowerCommand.equals("name") || lowerCommand.equals("done") ||
		lowerCommand.equals("retr") || lowerCommand.equals("stor"))
		{
			System.out.println("valid request");
			return true;
		}
		System.out.println("invalid request");
		return false;
	} 
    
    
    
} 

