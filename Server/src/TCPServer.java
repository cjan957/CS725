/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat; 

class TCPServer { 
	private  Socket connectionSocket;
	private  BufferedReader inFromClient;
	private  DataOutputStream outToClient;
	
	private  boolean passwordValid;
	private  boolean usernameValid;
	private  boolean accountValid;
	
	private boolean loggedIn;
	
	private String transmissionType = "B"; //default binary
	
	private final String HOME_DIRECTORY = FileSystems.getDefault().getPath("storage").toString();
	private String currentDirectory = HOME_DIRECTORY;
	
	Users userDetail;
	Database db;
	
	//private static String accountFile = "";
	private  enum ResponseCodes
	{
		SUCCESS, ERROR, LOGGEDIN
	}

	
	public static void main(String argv[]) throws IOException
	{
		TCPServer server = new TCPServer();
		server.start();
	}
	
	public void start()
	{
		String clientRequest;
		
		while(true)
		{
			if(!connectionSocket.isClosed())
			{
				clientRequest = readMessageFromClient();
				boolean validRequest = validateClientRequest(clientRequest);
										
				if(validRequest)
				{ 	
					performRequest(clientRequest);
				}
			}
			else
			{
				System.out.println("no connection found");
				break;
			}
			
		}
	}
	
    public TCPServer() throws IOException 
    { 
    			
		ServerSocket welcomeSocket = new ServerSocket(6789);

	    connectionSocket = welcomeSocket.accept(); 

	    inFromClient = 
			new BufferedReader(new
		    InputStreamReader(connectionSocket.getInputStream())); 
		    
	    outToClient = 
			new DataOutputStream(connectionSocket.getOutputStream()); 
	    
	    sendMessageToClient("cjan957 SFTP Service", ResponseCodes.SUCCESS);
    }

    
	private  void performRequest(String clientRequest) {
		// TODO Auto-generated method stub
		String[] requestBreakdown = clientRequest.split(" ");
		String command = requestBreakdown[0];
						
		if(command.equals("user"))
		{
			userCommand(clientRequest);
		}
		else if(command.equals("pass"))
		{
			passCommand(clientRequest);
		}
		else if(command.equals("acct"))
		{
			acctCommand(clientRequest);
		}
		else if(command.equals("type"))
		{
			typeCommand(clientRequest);
		}
		else if(command.equals("list"))
		{
			listCommand(clientRequest);
		}
		else if(command.equals("done")) 
		{
			doneCommand();
		}
		else
		{
			sendMessageToClient("- invalid input", ResponseCodes.ERROR);
		}
		
	}
	
	private void listCommand(String clientRequest)
	{
		String[] requestBreakdown = clientRequest.split(" ");
		String mode = "";
		String listOfFile_out = "";
		
		//LIST + F or V, current directory!
		if(requestBreakdown.length == 2 || requestBreakdown.length == 3)
		{
			File filePath = new File(currentDirectory);

			if(requestBreakdown.length == 3)
			{
				filePath = new File(currentDirectory + "/" + requestBreakdown[2]);
				if(!filePath.isDirectory())
				{
					sendMessageToClient("Invalid directory", ResponseCodes.ERROR);
					return;
				}
			}
			
			mode = requestBreakdown[1].toUpperCase();
			
			if(mode.equals("F") || mode.equals("V"))
			{
				File[] fileList = filePath.listFiles();
				
				listOfFile_out = listOfFile_out.concat(String.format("%s\r\n", currentDirectory));
				
				for(int i = 0; i < fileList.length; i++)
				{
					String fileName = fileList[i].getName();

					if(fileList[i].isFile())
					{
						if(mode.equals("F"))
						{
							listOfFile_out = listOfFile_out.concat(String.format("%s\r\n", fileName));
						}
						else
						{
							try {
								BasicFileAttributes basic_attr = Files.readAttributes(fileList[i].toPath(), BasicFileAttributes.class);	
								FileOwnerAttributeView owner_attr = Files.getFileAttributeView(fileList[i].toPath(), FileOwnerAttributeView.class);
								
								long size = basic_attr.size();
								
								FileTime lastModified = basic_attr.lastModifiedTime();
								
								SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
								
								String dateModified = format.format(lastModified.toMillis());
								
								String owner = owner_attr.getOwner().getName();
								
								String fileInfo = String.format("%s %s %s %s", fileName, size, dateModified, owner);
								
								listOfFile_out = listOfFile_out.concat(String.format("%s\r\n", fileInfo));
							}
							catch(Exception e)
							{
								sendMessageToClient("Files access errors", ResponseCodes.ERROR);
								break;
							}	
						}
					}
					else 
					{
						//if folder
						String folderName = String.format("%s/\r\n", fileName);
						listOfFile_out = listOfFile_out.concat(folderName);
					}
				}
				
				if(mode.equals("F"))
				{
					//listing is terminated with <NULL> after the last <CRLF> (loop)
					listOfFile_out.concat(Character.toString('\0'));
				}
				sendMessageToClient(listOfFile_out,ResponseCodes.SUCCESS);	
			}
			else
			{
				sendMessageToClient("Invalid Mode", ResponseCodes.ERROR);
				return;
			}
			
		}
		else
		{
			sendMessageToClient("Invalid arguments", ResponseCodes.ERROR);
			return;
		}
		
	}
	
	private void doneCommand()
	{
		sendMessageToClient("Charge/Accounting info", ResponseCodes.SUCCESS);
		try {
			connectionSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void typeCommand(String clientRequest)
	{
		if(!loggedIn)
		{
			sendMessageToClient("Not logged in", ResponseCodes.ERROR);
			return;
		}
		
		String[] requestBreakdown = clientRequest.split(" ");
		if(hasArgument(requestBreakdown) && requestBreakdown.length == 2)
		{
			switch(requestBreakdown[1].toUpperCase())
			{
			case "A":
				transmissionType = "A";
				sendMessageToClient("Using Ascii mode", ResponseCodes.SUCCESS);	
				break;
			case "B":
				transmissionType = "B";
				sendMessageToClient("Using Binary mode", ResponseCodes.SUCCESS);	
				break;
			case "C":
				transmissionType = "C";
				sendMessageToClient("Using Continuous mode", ResponseCodes.SUCCESS);	
				break;
			default:
				sendMessageToClient("Type not valid", ResponseCodes.ERROR);	
			}
		}
		else
		{
			sendMessageToClient("Type not valid", ResponseCodes.ERROR);	
		}

	}
	
	private boolean hasArgument(String[] message)
	{
		if(message.length > 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void userCommand(String clientRequest) {
		
		//breakdown the request using space
		String[] requestBreakdown = clientRequest.split(" ");
		
		//i.e. USER<SPACE>cjan957, assumes username has no space
		if(requestBreakdown.length == 2)
		{
			String username = requestBreakdown[1];
			
			db = new Database();
			db.connect();
			
			userDetail = db.searchForUser(username);
			
			//username found in db
			if(userDetail.getValid() == 1)
			{
				usernameValid = true;
				passwordValid = false;
				accountValid = false;
				//has a password
				if(!userDetail.hasPassword())
				{
					if(!userDetail.hasAccounts())
					{
						sendMessageToClient(username + " logged in", ResponseCodes.LOGGEDIN);
						logIn();
					}
				}
				else
				{
					sendMessageToClient("User-id valid, send account and password", ResponseCodes.SUCCESS);
					loggedIn = false;
				}
			}
			else
			{
				usernameValid = false;
				sendMessageToClient("Invalid user-id, try again", ResponseCodes.ERROR);
			}
		}
		//i.e. USER 
		else
		{
			sendMessageToClient("Invalid user-id, try again", ResponseCodes.ERROR);
		}
	}
	
	private void passCommand(String clientRequest)
	{
		//split the PASS command from the actual password
		String command = clientRequest.substring(0, clientRequest.indexOf(' '));
		String password = clientRequest.substring(clientRequest.indexOf(' ') + 1);
		
		if(usernameValid)
		{
			if(userDetail.hasPassword())
			{
				if(userDetail.getPassword().equals(password))
				{
					passwordValid = true;
					if(!userDetail.hasAccounts() || accountValid)
					{	
						sendMessageToClient("Logged in", ResponseCodes.LOGGEDIN);
						logIn();
					}
					else
					{
						sendMessageToClient("Send Account", ResponseCodes.SUCCESS);
					}
				}
				else
				{
					sendMessageToClient("Wrong password, try again", ResponseCodes.ERROR);
				}
			}
		}
		
		
	}
	
	private void acctCommand(String clientRequest) 
	{
		// ACCT accountName
		String[] requestBreakdown = clientRequest.split(" ");
		String accountName = requestBreakdown[1];
		boolean foundAccount = false;
		
		if(usernameValid)
		{
			//ACCT <cjan957>
			if(requestBreakdown.length == 2)
			{
				//user from db has an account
				if(userDetail.hasAccounts())
				{
					String[] accountArray = userDetail.getAccounts();
					for(int i = 0; i < accountArray.length; i++)
					{
						if(accountArray[i].equals(accountName))
						{
							//Account found
							foundAccount = true;
							break;
						}
					}
					
					//account specified by client matches db
					if(foundAccount)
					{
						accountValid = true;
						if(passwordValid)
						{
							sendMessageToClient("Account valid, logged-in", ResponseCodes.LOGGEDIN);
							logIn();
						}
						else if(!passwordValid && userDetail.hasPassword())
						{
							sendMessageToClient("Account valid, send password", ResponseCodes.SUCCESS);
						}
						else if(!userDetail.hasPassword())
						{
							sendMessageToClient("Account valid, logged-in", ResponseCodes.LOGGEDIN);
							logIn();
						}
					}
					else
					{
						sendMessageToClient("Invalid account, try again", ResponseCodes.ERROR);
					}
				}
				//if user has no account in remote / db
				else
				{
					if(passwordValid)
					{
						sendMessageToClient("Account valid, logged-in", ResponseCodes.LOGGEDIN);
						logIn();
					}
					else if(!passwordValid && userDetail.hasPassword())
					{
						sendMessageToClient("Account valid, send password", ResponseCodes.SUCCESS);
					}
					else if(!userDetail.hasPassword())
					{
						sendMessageToClient("Account valid, logged-in", ResponseCodes.LOGGEDIN);
						logIn();

					}
				}
			}
		}
	}
	
	private void logIn()
	{
		loggedIn = true;
	}
	
	private boolean isLoggedIn()
	{
		return loggedIn;
	}

	private String readMessageFromClient()
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
			String charBychar = Character.toString((char)character);
			requestBuffer = requestBuffer.concat(charBychar);
		}
		return requestBuffer;
	}
	
	private  void sendMessageToClient(String message, ResponseCodes status)
	{
		String statusSymbol = "";
		switch(status) {
		case SUCCESS:
			statusSymbol = "+";
			break;
		case ERROR:
			statusSymbol = "-";
			break;
		case LOGGEDIN:
			statusSymbol = "!";
			break;
		}
		try {
			statusSymbol = statusSymbol.concat(message).concat(Character.toString('\0'));
			outToClient.writeBytes(statusSymbol); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean validateClientRequest(String request) {
		
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

