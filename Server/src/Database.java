import java.sql.*;
import java.util.ArrayList;

public class Database {
	
	private Connection conn = null;
	
	public void connect()
	{
		try {
			String url = "jdbc:sqlite:peopleDB.db";
			conn = DriverManager.getConnection(url);
			System.out.println("established");
		}
		catch(Exception e){
			System.out.println("connection failed " + e);
		}
			
	}
	
	
	public String[] searchForAccount(String username)
	{
		String sql = "SELECT account FROM accountList WHERE username = ?";
		ArrayList<String> accountList = new ArrayList<String>();
		int size;
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, username);
			
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				accountList.add(rs.getString("account"));
			}
			
		}catch(SQLException e) {
			System.out.println("Died trying to access account list " + e);
		}
		
		return accountList.toArray(new String[0]);
		
	}
	
	public Users searchForUser(String username)
	{
		Users user = new Users();
		String sql = "SELECT username, password, hasAccount FROM userlist WHERE username = ?";
		
		try{
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, username);
			
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				//if an entry exist with this user name...
				user.setValid();
				user.setUsername(username);
				user.setPassword(rs.getString("password"));
				if(rs.getString("hasAccount").equals("1"))
				{
					String[] userList = searchForAccount(username);
					user.setAccounts(userList);
				}
				return user;
			}
		
		}catch(SQLException e)
		{
			System.out.println("sdkfjsdkl");
		}
		
		return user;
		
	}

}
