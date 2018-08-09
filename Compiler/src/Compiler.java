
public class Compiler
{
	public static void main(String argv[]) throws Exception 
    { 
		 String test = "user abc123";
	        String test2 = "user";
	        String test3 = "user   cjan957";
	        
	        String user = "cjan957::12323";
	        String userx = "cjan957::";
	        
	        String[] test1bd = test.split("\\s+");
	        String[] test2bd = test2.split("\\s+");
	        String[] test3bd = test3.split("\\s+");
	        String[] test3bd_s = test3.split(" ");
	        String[] testuserx = userx.split("::");
	        
	        System.out.println(test2bd[0]);
	        
    }
}
