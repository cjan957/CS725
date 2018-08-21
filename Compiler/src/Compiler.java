import java.io.File;
import java.util.regex.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Compiler
{
	public static void main(String argv[]) throws Exception 
    { 
		ArrayList al = new ArrayList();
		String test = "test(jpg)(1).jpg";
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(test);
		
		
		
		while(m.find()) {
			al.add(m.group(1));
		}
		
		System.out.println("Generation number is " + al.get(al.size()-1));
		
		
		String fileName = "test.m.jpg";
		List<String> dotBreakdown = new LinkedList<String>(Arrays.asList(fileName.split("\\.")));
		String fileExtension = dotBreakdown.get(dotBreakdown.size() - 1);
		int size = dotBreakdown.size() - 1;
		dotBreakdown.remove(size);
		String str = String.join(".", dotBreakdown);
		System.out.println("File extension is " + fileExtension);

		
		
		
//		String HOME_DIRECTORY = FileSystems.getDefault().getPath("storage").toString();
//		File folder = new File(HOME_DIRECTORY);
//		File[] listOfFiles = folder.listFiles();
//		
//		String filename =  "skdfjskdlfjsfd";
//		String str = "";
//		str = str.concat(String.format("%s\r\n", filename));
//		str = str.concat(String.format("%s\r\n", filename));
//
//		String clientRequest = "STOR OLD new car.jpg";
//		String[] requestBreakdown = clientRequest.split(" ");
//
//		String filieName = clientRequest.substring(9);
		
		
    }
}
