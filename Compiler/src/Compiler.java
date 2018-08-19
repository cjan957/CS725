import java.io.File;
import java.nio.file.FileSystems;

public class Compiler
{
	public static void main(String argv[]) throws Exception 
    { 
		
		String HOME_DIRECTORY = FileSystems.getDefault().getPath("storage").toString();
		File folder = new File(HOME_DIRECTORY);
		File[] listOfFiles = folder.listFiles();
		
		String filename =  "skdfjskdlfjsfd";
		String str = "";
		str = str.concat(String.format("%s\r\n", filename));
		str = str.concat(String.format("%s\r\n", filename));

		String clientRequest = "STOR OLD new car.jpg";
		String[] requestBreakdown = clientRequest.split(" ");

		String filieName = clientRequest.substring(9);
		
		
    }
}
