/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.FileSystems; 
class TCPClient { 
	
	private Socket clientSocket;
	
	private BufferedReader inFromUser;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	
	private DataInputStream dataInFromServer;
	//private OutputStream dataOutToServer;
	
	private FileOutputStream dataWriteLocal;
	
	private String fileNameToSave = "";
	
	boolean loggedIn = false;
	boolean userOK = false;
	
	private final String HOME_DIRECTORY = FileSystems.getDefault().getPath("storage").toString();
	
	private enum ResponseCodes {
		SUCCESS, ERROR, LOGGEDIN, EMPTY
	}
	
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
		
		dataInFromServer = 
			new DataInputStream(clientSocket.getInputStream());
		
//		dataOutToServer = 
//			new DataOutputStream(clientSocket.getOutputStream());
		
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
			 if(checkResponseCode(serverWelcome).equals(ResponseCodes.SUCCESS))
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
				String singleCommand = requestBreakdown[0].toUpperCase();

				switch (singleCommand)
				{
					case "DONE":
						closeConnection(sentence);
					break;
				}
			}
			else
			{
				String command = requestBreakdown[0].toUpperCase();
				String argument = sentence.substring(sentence.indexOf(' ') + 1);
				
				if(command.equals("RETR"))
				{
					fileNameToSave = argument;
				}
			}
	
			if(sendMessageToServer(sentence))
			{
				reply = readMessageFromServer();
				
				//Server responded RETR command with the size of the file to be sent
				if(checkResponseCode(reply).equals(ResponseCodes.EMPTY))
				{
					checkSpaceAndAcknowledge(reply);
				}
			    
				//Check for ! to see whether user has been logged in on the server, this will
				//unlock other commands.
				if(checkResponseCode(reply).equals(ResponseCodes.LOGGEDIN))
				{
					loggedIn = true;
				}
				else if(!loggedIn && checkResponseCode(reply).equals(ResponseCodes.SUCCESS))
				{
					userOK = true;
				}
				
			    System.out.println("FROM SERVER: " + reply);
			}
		    
		}
		
	   // clientSocket.close(); 
	}
	
	private void checkSpaceAndAcknowledge(String reply) throws IOException 
	{
		String[] replyBreakdown = reply.split(" ");
		
		if(replyBreakdown.length == 1 || replyBreakdown.length > 2)
		{
			//wrong
		}
		else
		{
			File localPath = new File(HOME_DIRECTORY);
			long availableSpace = localPath.getUsableSpace();
			long spaceRequired = Long.parseLong(replyBreakdown[1]);
			
			if(spaceRequired < availableSpace)
			{
				//ok
				dataWriteLocal = new FileOutputStream(HOME_DIRECTORY + "/" + fileNameToSave);
				BufferedOutputStream outStream = new BufferedOutputStream(dataWriteLocal);
				
				int fileSize = (int) spaceRequired;
				
				
				byte[] bytes = new byte[1];
				int count;
				int totalCount = 0;
				sendMessageToServer("SEND");
				
				while(    (count = dataInFromServer.read(bytes)) > 0)
				{
					outStream.write(bytes,0,count);
					totalCount += count;
					if(totalCount == 921653) {
						System.out.println(totalCount);
					}
				}
				
				System.out.println("Total read: " + totalCount);
				
				dataWriteLocal.close();
				outStream.close();
	
			}
			else
			{
				//no space
				sendMessageToServer("STOP");
			}
			
		}
	}


	private void closeConnection(String sentence)
	{
		sendMessageToServer(sentence);
	}
	
	private ResponseCodes checkResponseCode(String message)
	{
		switch (message.charAt(0))
		{
			case '!':
				return ResponseCodes.LOGGEDIN;
			case '+':
				return ResponseCodes.SUCCESS;
			case '-':
				return ResponseCodes.ERROR;
			case ' ':
				return ResponseCodes.EMPTY;
			default:
				return ResponseCodes.ERROR;
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

			if(lowerCommand.equals("done") || lowerCommand.equals("send") || lowerCommand.equals("stop"))
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
