
/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

class TCPClient {

	private Socket clientSocket;

	private BufferedReader inFromUser;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;

	private InputStream dataInFromServer;
	private OutputStream dataOutToServer;

	private String fileNameToSave = "";
	private String transmissionType = "B";

	boolean loggedIn = false;
	boolean userOK = false;

	private final String HOME_DIRECTORY = FileSystems.getDefault().getPath("storage").toString();

	private enum ResponseCodes {
		SUCCESS, ERROR, LOGGEDIN, EMPTY
	}

	public TCPClient() throws UnknownHostException, IOException {
		inFromUser = new BufferedReader(new InputStreamReader(System.in));

		clientSocket = new Socket("localhost", 6789);

		outToServer = new DataOutputStream(clientSocket.getOutputStream());

		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		dataInFromServer = clientSocket.getInputStream();
		dataOutToServer = clientSocket.getOutputStream();

	}

	private void start() throws IOException {
		String serverWelcome;
		String sentence;
		String reply;

		// check if connection was established
		while (true) {
			serverWelcome = readMessageFromServer();
			if (checkResponseCode(serverWelcome).equals(ResponseCodes.SUCCESS)) {
				System.out.println("FROM SERVER: " + serverWelcome);
				break;
			}
		}

		while (true) {
			boolean storeCommandTriggered = false;
			sentence = inFromUser.readLine();

			String[] requestBreakdown = sentence.split(" ");

			if (requestBreakdown.length == 1) {
				String singleCommand = requestBreakdown[0].toUpperCase();

				switch (singleCommand) {
				case "DONE":
					closeConnection(sentence);
					break;
				}
			} else {
				String command = sentence.substring(0, sentence.indexOf(' '));
				String argument = sentence.substring(sentence.indexOf(' ') + 1);

				if (command.toUpperCase().equals("RETR")) {
					fileNameToSave = argument;
				} else if (command.toUpperCase().equals("STOR")) {
					storCommand(sentence);
					storeCommandTriggered = true;
				}
			}

			if (storeCommandTriggered == false) {
				if (sendMessageToServer(sentence)) {
					reply = readMessageFromServer();

					checkTransmissionType(reply);

					// Server responded RETR command with the size of the file to be sent
					if (checkResponseCode(reply).equals(ResponseCodes.EMPTY)) {
						checkSpaceAndAcknowledge(reply);
					}

					// Check for ! to see whether user has been logged in on the server, this will
					// unlock other commands.
					if (checkResponseCode(reply).equals(ResponseCodes.LOGGEDIN)) {
						loggedIn = true;
					} else if (!loggedIn && checkResponseCode(reply).equals(ResponseCodes.SUCCESS)) {
						userOK = true;
					}

					System.out.println("FROM SERVER: " + reply);
				}

			}

		}

		// clientSocket.close();
	}

	private void checkTransmissionType(String reply) {

		if (reply.toLowerCase().contains("using ascii mode")) {
			transmissionType = "A";
			System.out.println("Local Message: Using ASCII");
		} else if (reply.toLowerCase().contains("using binary mode")) {
			transmissionType = "B";
			System.out.println("Local Message: Using Binary");

		} else if (reply.toLowerCase().contains("using continuous mode")) {
			transmissionType = "C";
			System.out.println("Local Message: Using Continuous");

		}
	}

	private void storCommand(String sentence) throws IOException {

		String[] requestBreakdown = sentence.split(" ");

		if (requestBreakdown[1].toUpperCase().equals("NEW") || requestBreakdown[1].toUpperCase().equals("OLD")
				|| requestBreakdown[1].toUpperCase().equals("APP")) {
			String fileName = sentence.substring(9); // STOR XXX {FILENAME} (9th char)

			File file = new File(HOME_DIRECTORY + "/" + fileName);

			if (!file.exists() && !file.isFile()) {
				System.out.println("Local message: no file found in storage folder");
				return;
			}

			sendMessageToServer(sentence);

			String reply = readMessageFromServer();

			System.out.println("FROM SERVER: " + reply);

			ResponseCodes fileSaveMethod = checkResponseCode(reply);

			if (fileSaveMethod.equals(ResponseCodes.ERROR)) {
				return;
			}

			BasicFileAttributes basic_attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			long size = basic_attr.size();

			sendMessageToServer("SIZE " + Long.toString(size));

			String reply_send = readMessageFromServer();

			ResponseCodes canSendFileResponse = checkResponseCode(reply_send);

			System.out.println("FROM SERVER: " + reply_send);

			//send file to server
			if (canSendFileResponse.equals(ResponseCodes.SUCCESS)) {
				if (transmissionType.equals("A")) {
					
					int buffer;
					FileInputStream in = new FileInputStream(file);
					Writer w = new OutputStreamWriter(dataOutToServer, "US-ASCII");

					while ((buffer = in.read()) > 0) {
						char ch = (char) buffer;
						w.write(ch);
					}
					
					System.out.println("Client sent A mode!");
					// r.close();
					w.flush();
					dataOutToServer.flush();
					
					System.out.println("FROM SERVER: " + readMessageFromServer());
					return;
					
				} else {
					byte buffer[] = new byte[1];
					FileInputStream in = new FileInputStream(file);

					while ((in.read(buffer)) > 0) {
						dataOutToServer.write(buffer);
					}

					System.out.println("Client sent! B/C mode");
					dataOutToServer.flush();

					System.out.println("FROM SERVER: " + readMessageFromServer());
					return;
				}
			} else {
				return;
			}
		} else {
			System.out.println("Local message: invalid arguments/file");
		}

	}

	private void checkSpaceAndAcknowledge(String reply) throws IOException {

		String[] replyBreakdown = reply.split(" ");

		if (replyBreakdown.length == 1 || replyBreakdown.length > 2) {
			// wrong
		} else {
			File localPath = new File(HOME_DIRECTORY);
			long availableSpace = localPath.getUsableSpace();
			long spaceRequired = Long.parseLong(replyBreakdown[1]);

			if (spaceRequired < availableSpace) {
				
				FileOutputStream dataWriteLocal = new FileOutputStream(HOME_DIRECTORY + "/" + fileNameToSave);
				int fileSize = (int) spaceRequired;

				if(transmissionType.equals("A"))
				{
					sendMessageToServer("SEND");

					Reader r = new InputStreamReader(dataInFromServer, "US-ASCII");
					//Writer w = new OutputStreamWriter(dataWriteLocal, "US-ASCII");
					
					int buffer;
					int totalCount = 0;
					
					while((buffer = r.read()) > 0)
					{
						char ch = (char) buffer;
						dataWriteLocal.write(ch);
						totalCount += 1;
						if(totalCount == fileSize) break;
					}
					System.out.println("DONE");
				}
				else
				{
					byte[] bytes = new byte[1];
					int  count;
					int totalCount = 0;
					sendMessageToServer("SEND");

					while ((count = dataInFromServer.read(bytes)) > 0) {
						dataWriteLocal.write(bytes, 0, count);
						totalCount += count;
						if (totalCount == fileSize)
							break;
					}

					System.out.println("DONE");
				}
			

			} else {
				// no space
				sendMessageToServer("STOP");
			}

		}
	}

	private void closeConnection(String sentence) {
		sendMessageToServer(sentence);
	}

	private ResponseCodes checkResponseCode(String message) {
		switch (message.charAt(0)) {
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

	private boolean sendMessageToServer(String message) {

		if (verifyMessageToServer(message)) {
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
		} else {
			System.out.println("Local Message: Invalid/Unauthorised command");
			return false;
		}

	}

	public boolean verifyMessageToServer(String message) {
		String[] requestBreakdown = message.split(" ");

		if (requestBreakdown.length > 1) {
			String command = message.substring(0, message.indexOf(' '));
			String lowerCommand = command.toLowerCase();

			if (!loggedIn) {
				if (!userOK) {
					// first command must be USER only
					if (lowerCommand.equals("user")) {
						return true;
					} else {
						return false;
					}
				} else {
					if (lowerCommand.equals("acct") || lowerCommand.equals("pass") || lowerCommand.equals("done")) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				return true;
			}
		} else {
			String lowerCommand = requestBreakdown[0].toLowerCase();

			if (lowerCommand.equals("done") || lowerCommand.equals("send") || lowerCommand.equals("stop")) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static void main(String argv[]) throws Exception {
		TCPClient client = new TCPClient();
		client.start();
	}

	private String readMessageFromServer() {
		String requestBuffer = "";
		int character = 0;

		while (true) {
			try {
				character = inFromServer.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Check for null
			if (character == 0) {
				break;
			}
			String charBychar = Character.toString((char) character);
			requestBuffer = requestBuffer.concat(charBychar);
		}
		return requestBuffer;
	}
}
