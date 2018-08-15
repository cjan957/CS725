
public class Users {
	
	private String username;
	private String password;
	private String[] accounts;
	private int valid;
		
	public Users()
	{
		this.valid = 0;
	}
	
	public void setValid()
	{
		this.valid = 1;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public void setAccounts(String[] accounts)
	{
		this.accounts = accounts;
	}

	public String getPassword()
	{
		return password;
	}
	
	public String[] getAccounts()
	{
		return accounts;
	}
	
	public boolean hasPassword()
	{
		if(password != "")
		{
			return true;
		}
		return false;
	}
	
	public boolean hasAccounts()
	{
		if(accounts.length != 0)
		{
			return true;
		}
		return false;
	}

}
