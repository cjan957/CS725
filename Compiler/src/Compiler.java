
public class Compiler
{
	public static void main(String argv[]) throws Exception 
    { 
		String clientRequest = "PAScjan957sdfkj";
		String[] requestBreakdown = clientRequest.split(" ");

		String command = clientRequest.substring(0, clientRequest.indexOf(' '));
		String password = clientRequest.substring(clientRequest.indexOf(' ') + 1);
		
		
    }
}
