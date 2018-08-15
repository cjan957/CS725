/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 
class TCPClient { 
	
	Socket clientSocket;
	
	BufferedReader inFromUser;
	DataOutputStream outToServer;
	BufferedReader inFromServer;
	
	boolean loggedIn = false;
	boolean userOK = false;
	
	public TCPClient() throws UnknownHostException, IOException
	{
		inFromUser = 
			new BufferedReader(new InputStreamReader(System.in)); 
		
		clientSocket = new Socket("localhost", 6789); 	
	        
		outToServer = 
		    new DataOutputStream(clientSocket.getOutputStream()); 
		
		inFromServer = 
			new BufferedReader(new
				InputStreamReader(clientSocket.getInputStream())); 
	}
	
	private void start() throws IOException
	{
		String serverWelcome;
		String sentence;
		String reply;
		
		//check if connection was established
		while(true)
		{
			 serverWelcome = readMessageFromServer();
			 if(isSuccessStatus(serverWelcome))
			 {
				 System.out.println("FROM SERVER: " + serverWelcome);
				 break;
			 }
		}
		
		while(true) {
			sentence = inFromUser.readLine();
	
			String[] requestBreakdown = sentence.split(" ");
			if(requestBreakdown.length == 1)
			{
				String singleCommand = requestBreakdown[0].toLowerCase();
				if(singleCommand.equals("done"))
				{
					closeConnection(sentence);
				}
			}

				
			if(sendMessageToServer(sentence))
			{
				reply = readMessageFromServer();
			    
			    if(isLoggedInStatus(reply))
			    {
			    	loggedIn = true;
			    }
			    else if(!loggedIn && isSuccessStatus(reply))
			    {	
			    	userOK = true;
			    }
				
			    System.out.println("FROM SERVER: " + reply);
			}
		    
		}
		
	   // clientSocket.close(); 
	}
	
	private void closeConnection(String sentence)
	{
		sendMessageToServer(sentence);
		
	}
	
	private boolean isLoggedInStatus(String message)
	{
		if(message.charAt(0) == '!')
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean isSuccessStatus(String message)
	{
		if(message.charAt(0) == '+')
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean sendMessageToServer(String message)
	{
		
		if(verifyMessageToServer(message))
		{
			try {
				outToServer.writeBytes(message.concat(Character.toString('\0')));
				return true;
			} catch (IOException e) {
				try {
					clientSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return false;
			}
		}
		else
		{
			System.out.println("Local Message: Invalid/Unauthorised command");
			return false;
		}
	
		 
	}
	
	public boolean verifyMessageToServer(String message)
	{
		String[] requestBreakdown = message.split(" ");
		
		if(requestBreakdown.length > 1)
		{
			String command = message.substring(0, message.indexOf(' '));
			String lowerCommand = command.toLowerCase();
			
			if(!loggedIn)
			{
				if(!userOK)
				{
					//first command must be USER only
					if(lowerCommand.equals("user"))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					if(lowerCommand.equals("acct") || lowerCommand.equals("pass") || lowerCommand.equals("done"))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
			else
			{
				return true;
			}
		}
		else
		{
			String lowerCommand = requestBreakdown[0].toLowerCase();

			if(lowerCommand.equals("done"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
	}
	
    public static void main(String argv[]) throws Exception 
    { 
    	TCPClient client = new TCPClient();
    	client.start();
    } 
    
    private String readMessageFromServer()
	{
		String requestBuffer = "";
		int character = 0;
		
		while(true)
		{
			try {
				character = inFromServer.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//Check for null
			if(character == 0)
			{
				break;
			}
			String charBychar = Character.toString((char)character);
			requestBuffer = requestBuffer.concat(charBychar);
		}
		return requestBuffer;
	}
} 
